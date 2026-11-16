package nl.mikaaah.creationaugmenter;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nl.mikaaah.creationaugmenter.recipe.CreationAugmentingRecipe;

/** Server-authoritative, one-item augmentation. Inputs stay on the platform until atomic commit. */
public class CreationAugmenterBlockEntity extends KineticBlockEntity {
  public enum Phase {
    IDLE,
    CHARGING,
    FIRING,
    COOLDOWN
  }

  public static final int CHARGE_TICKS = 20, FIRE_TICKS = 60, COOLDOWN_TICKS = 20;
  public static final float MIN_RPM = 128.0F;
  private ItemStack installedLens = ItemStack.EMPTY;
  private Phase phase = Phase.IDLE;
  private int cycleTicks;
  private Vec3 targetPosition;
  private UUID targetEntity;
  private BlockPos targetDepot;
  private ItemStack inputIdentity = ItemStack.EMPTY;
  private ResourceLocation recipeId;
  private boolean builtInUnbreaking;
  private CreationAugmentingRecipe activeRecipe;
  private int processingTime = FIRE_TICKS, recipeMinRpm = 128;

  public CreationAugmenterBlockEntity(BlockPos pos, BlockState state) {
    super(CreationAugmenter.BLOCK_ENTITY.get(), pos, state);
  }

  @Override
  public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    super.addBehaviours(behaviours);
  }

  @Override
  public float calculateStressApplied() {
    return lastStressApplied = 64.0F;
  }

  public ItemStack lens() {
    return installedLens;
  }

  public CreationLens lensKind() {
    return CreationLens.of(installedLens);
  }

  public Phase getPhase() {
    return phase;
  }

  public int getCycleTicks() {
    return cycleTicks;
  }

  public boolean hasPower() {
    return level != null
        && Math.abs(getSpeed()) >= MIN_RPM
        && !isOverStressed()
        && !level.hasNeighborSignal(worldPosition);
  }

  public boolean isBeamActive() {
    return phase == Phase.FIRING && hasPower() && lensKind() != null;
  }

  public Vec3 getTargetPosition() {
    return targetPosition == null
        ? Vec3.atLowerCornerOf(worldPosition).add(.5, -2.0, .5)
        : targetPosition;
  }

  public Vec3 beamTargetPosition() {
    return getTargetPosition();
  }

  /** Direct inventory API. UI insertion still requires removing the previous lens first. */
  public ItemStack exchangeLens(ItemStack replacement) {
    if (!replacement.isEmpty()
        && (CreationLens.of(replacement) == null
            || replacement.getDamageValue() >= replacement.getMaxDamage()))
      throw new IllegalArgumentException("Not a Creation Lens");
    ItemStack previous = installedLens.copy();
    cancelCycle();
    installedLens = replacement.copyWithCount(replacement.isEmpty() ? 0 : 1);
    if (level != null && !level.isClientSide) {
      if (!previous.isEmpty() && replacement.isEmpty())
        level.playSound(
            null, worldPosition, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, .35F, 1.25F);
      else if (!replacement.isEmpty())
        level.playSound(
            null, worldPosition, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, .4F, .9F);
      setChanged();
      sendData();
    }
    return previous;
  }

  public static boolean canApplyUnbreaking(ItemStack input) {
    return !input.isEmpty()
        && !(input.getItem() instanceof CreationLensItem)
        && input.getCount() == 1
        && input.isDamageableItem()
        && !input.has(DataComponents.UNBREAKABLE);
  }

  public static ItemStack applyUnbreaking(ItemStack input) {
    if (!canApplyUnbreaking(input)) return ItemStack.EMPTY;
    ItemStack result = input.copy();
    result.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
    return result;
  }

  private RecipeHolder<CreationAugmentingRecipe> findRecipe(ItemStack input) {
    if (level == null || lensKind() == null || input.isEmpty()) return null;
    return level.getRecipeManager().getAllRecipesFor(CreationAugmenter.AUGMENTATION.get()).stream()
        .filter(recipe -> recipe.value().matches(input, lensKind()))
        .min(Comparator.comparing(recipe -> recipe.id().toString()))
        .orElse(null);
  }

  private boolean canProcess(ItemStack input) {
    if (!usableLens()) return false;
    if (lensKind() == CreationLens.UNBREAKING && canApplyUnbreaking(input)) return true;
    var recipe = findRecipe(input);
    return recipe != null && Math.abs(getSpeed()) >= recipe.value().minRpm();
  }

  private void selectOperation(ItemStack input) {
    inputIdentity = input.copy();
    // Unbreaking is the exact vanilla data component, not an enchantment or lore marker.
    builtInUnbreaking = lensKind() == CreationLens.UNBREAKING && canApplyUnbreaking(input);
    var recipe = builtInUnbreaking ? null : findRecipe(input);
    recipeId = recipe == null ? null : recipe.id();
    activeRecipe = recipe == null ? null : recipe.value();
    processingTime = activeRecipe == null ? FIRE_TICKS : activeRecipe.processingTime();
    recipeMinRpm = activeRecipe == null ? 128 : activeRecipe.minRpm();
  }

  private boolean selectTarget() {
    // Three blocks below the upper housing leaves clearance beneath the extended nozzle.
    BlockPos depotPos = worldPosition.below(3);
    if (level.getBlockEntity(depotPos) instanceof DepotBlockEntity depot) {
      var behaviour = depot.getBehaviour(DepotBehaviour.TYPE);
      ItemStack held = behaviour.getHeldItemStack();
      if (canProcess(held)) {
        targetDepot = depotPos;
        targetEntity = null;
        targetPosition = Vec3.atLowerCornerOf(depotPos).add(.5, 15.0 / 16.0, .5);
        selectOperation(held);
        return true;
      }
    }
    var center = Vec3.atLowerCornerOf(worldPosition).add(.5, 0, .5);
    AABB column =
        new AABB(
            center.x - .30,
            center.y - 2.5,
            center.z - .30,
            center.x + .30,
            center.y - 1.40,
            center.z + .30);
    ItemEntity item =
        level
            .getEntitiesOfClass(
                ItemEntity.class,
                column,
                entity -> {
                  double dx = entity.getX() - center.x, dz = entity.getZ() - center.z;
                  return entity.isAlive()
                      && entity.onGround()
                      && dx * dx + dz * dz <= .18 * .18
                      && canProcess(entity.getItem());
                })
            .stream()
            .min(Comparator.comparingInt(ItemEntity::getId))
            .orElse(null);
    if (item == null) return false;
    targetEntity = item.getUUID();
    targetDepot = null;
    // Keep the beam strictly downward; the item intersects this narrow processing column.
    targetPosition = new Vec3(center.x, item.getY() + .08, center.z);
    selectOperation(item.getItem());
    return true;
  }

  private ItemEntity targetItem() {
    if (targetEntity == null || !(level instanceof ServerLevel server)) return null;
    var entity = server.getEntity(targetEntity);
    if (!(entity instanceof ItemEntity item) || !item.isAlive()) return null;
    double dx = item.getX() - worldPosition.getX() - .5,
        dz = item.getZ() - worldPosition.getZ() - .5;
    double y = item.getY() - worldPosition.getY();
    return dx * dx + dz * dz <= .18 * .18 && y >= -2.5 && y <= -1.4 ? item : null;
  }

  private ItemStack currentInput() {
    if (targetDepot != null && level.getBlockEntity(targetDepot) instanceof DepotBlockEntity depot)
      return depot.getBehaviour(DepotBehaviour.TYPE).getHeldItemStack();
    ItemEntity item = targetItem();
    return item == null ? ItemStack.EMPTY : item.getItem();
  }

  private boolean operationValid() {
    ItemStack current = currentInput();
    if (!usableLens()
        || Math.abs(getSpeed()) < recipeMinRpm
        || !clearBeamPath()
        || current.isEmpty()
        || !ItemStack.matches(current, inputIdentity)) return false;
    if (builtInUnbreaking)
      return lensKind() == CreationLens.UNBREAKING && canApplyUnbreaking(current);
    if (recipeId == null) return false;
    var holder = level.getRecipeManager().byKey(recipeId).orElse(null);
    return holder != null
        && holder.value() instanceof CreationAugmentingRecipe recipe
        && recipe == activeRecipe
        && recipe.matches(current, lensKind());
  }

  private ItemStack result() {
    if (builtInUnbreaking) return applyUnbreaking(currentInput());
    var holder = level.getRecipeManager().byKey(recipeId).orElse(null);
    return holder != null && holder.value() instanceof CreationAugmentingRecipe recipe
        ? recipe.makeResult(currentInput())
        : ItemStack.EMPTY;
  }

  private boolean commit() {
    if (!operationValid()) return false;
    ItemStack result = result();
    if (result.isEmpty()) return false;
    if (targetDepot != null
        && level.getBlockEntity(targetDepot) instanceof DepotBlockEntity depot) {
      var handler =
          depot.getBehaviour(
              com.simibubi.create.content.kinetics.belt.behaviour
                  .TransportedItemStackHandlerBehaviour.TYPE);
      if (handler == null) return false;
      boolean[] committed = {false};
      handler.handleProcessingOnAllItems(
          stack -> {
            if (committed[0] || !ItemStack.matches(stack.stack, inputIdentity))
              return com.simibubi.create.content.kinetics.belt.behaviour
                  .TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            committed[0] = true;
            var out = stack.copy();
            out.stack = result.copy();
            out.locked = false;
            out.clearFanProcessingData();
            var remainder = stack.copy();
            remainder.stack.shrink(1);
            remainder.locked = false;
            if (remainder.stack.isEmpty())
              return com.simibubi.create.content.kinetics.belt.behaviour
                  .TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(out);
            return com.simibubi.create.content.kinetics.belt.behaviour
                .TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(
                java.util.List.of(out), remainder);
          });
      if (committed[0]) {
        depot.setChanged();
        depot.sendData();
      }
      return committed[0];
    }
    ItemEntity item = targetItem();
    if (item == null) return false;
    ItemStack remainder = item.getItem().copy();
    remainder.shrink(1);
    if (remainder.isEmpty()) item.setItem(result);
    else {
      item.setItem(remainder);
      ItemEntity output =
          new ItemEntity(level, item.getX(), item.getY() + .015, item.getZ(), result, 0, 0, 0);
      output.setDefaultPickUpDelay();
      level.addFreshEntity(output);
    }
    return true;
  }

  private boolean usableLens() {
    return lensKind() != null
        && installedLens.isDamageableItem()
        && installedLens.getDamageValue() < installedLens.getMaxDamage();
  }

  private boolean clearBeamPath() {
    if (level == null) return false;
    // The supplied 31px body leaves a separate air gap above a Depot three blocks below.
    for (int distance = 1; distance <= 2; distance++) {
      BlockPos p = worldPosition.below(distance);
      if (!level.hasChunkAt(p) || !level.getBlockState(p).getCollisionShape(level, p).isEmpty())
        return false;
    }
    return true;
  }

  private void wearLens() {
    installedLens.setDamageValue(installedLens.getDamageValue() + 1);
    if (installedLens.getDamageValue() >= installedLens.getMaxDamage()) {
      installedLens = ItemStack.EMPTY;
      if (level instanceof ServerLevel server) {
        server.sendParticles(
            new net.minecraft.core.particles.BlockParticleOption(
                net.minecraft.core.particles.ParticleTypes.BLOCK,
                net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK.defaultBlockState()),
            worldPosition.getX() + .5,
            worldPosition.getY() - 15.0 / 16,
            worldPosition.getZ() + .5,
            12,
            .12,
            .06,
            .12,
            .025);
        level.playSound(
            null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, .6f, 1.15f);
        level.playSound(
            null, worldPosition, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, .6f, 1.0f);
      }
    }
    setChanged();
    sendData();
  }

  private void setPhase(Phase next) {
    if (next == phase) return;
    if (phase == Phase.FIRING) coolingHiss();
    phase = next;
    cycleTicks = 0;
    setChanged();
    sendData();
  }

  private void coolingHiss() {
    if (level != null && !level.isClientSide)
      level.playSound(
          null,
          worldPosition.getX() + .5,
          worldPosition.getY() - 15.0 / 16.0,
          worldPosition.getZ() + .5,
          SoundEvents.LAVA_EXTINGUISH,
          SoundSource.BLOCKS,
          .5F,
          1.15F);
  }

  public void cancelCycle() {
    if (level != null && !level.isClientSide && phase == Phase.FIRING) coolingHiss();
    phase = Phase.IDLE;
    cycleTicks = 0;
    targetEntity = null;
    targetDepot = null;
    inputIdentity = ItemStack.EMPTY;
    recipeId = null;
    builtInUnbreaking = false;
    activeRecipe = null;
    if (level != null && !level.isClientSide) {
      setChanged();
      sendData();
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (level == null) return;
    if (level.isClientSide) {
      // Client-only controllers observe this clock without introducing client classes on servers.
      if (phase != Phase.IDLE) cycleTicks++;
      return;
    }
    if (!hasPower() || !usableLens()) {
      if (phase != Phase.IDLE) cancelCycle();
      return;
    }
    if (phase == Phase.IDLE) {
      if (level.getGameTime() % 5 == 0 && clearBeamPath() && selectTarget()) {
        setPhase(Phase.CHARGING);
        level.playSound(
            null, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, .5F, 1.0F);
      }
      return;
    }
    if (phase == Phase.COOLDOWN) {
      if (++cycleTicks >= COOLDOWN_TICKS) cancelCycle();
      return;
    }
    if (!operationValid()) {
      cancelCycle();
      return;
    }
    cycleTicks++;
    if (phase == Phase.CHARGING && cycleTicks >= CHARGE_TICKS) {
      setPhase(Phase.FIRING);
      level.playSound(
          null,
          worldPosition,
          CreationAugmenter.AUDIO.get("fire").get(),
          SoundSource.BLOCKS,
          .45F,
          .85F);
    } else if (phase == Phase.FIRING && cycleTicks >= processingTime) {
      if (commit()) {
        setPhase(Phase.COOLDOWN);
        wearLens();
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
      } else cancelCycle();
    }
  }

  @Override
  protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
    super.write(tag, registries, clientPacket);
    if (!installedLens.isEmpty()) tag.put("Lens", installedLens.save(registries));
    if (clientPacket) {
      tag.putInt("Phase", phase.ordinal());
      tag.putInt("CycleTicks", cycleTicks);
      Vec3 target = getTargetPosition();
      tag.putDouble("TargetX", target.x);
      tag.putDouble("TargetY", target.y);
      tag.putDouble("TargetZ", target.z);
    }
  }

  @Override
  protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
    super.read(tag, registries, clientPacket);
    installedLens = ItemStack.parseOptional(registries, tag.getCompound("Lens"));
    if (CreationLens.of(installedLens) == null || !usableLens()) installedLens = ItemStack.EMPTY;
    if (clientPacket) {
      phase = Phase.values()[Math.clamp(tag.getInt("Phase"), 0, Phase.values().length - 1)];
      cycleTicks = Math.max(0, tag.getInt("CycleTicks"));
      targetPosition =
          new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
    } else {
      // No input escrow is saved: safely restart rather than committing stale references after
      // load.
      phase = Phase.IDLE;
      cycleTicks = 0;
    }
  }

  @Override
  public void destroy() {
    if (level != null && !level.isClientSide) {
      cancelCycle();
      if (!installedLens.isEmpty()) Block.popResource(level, worldPosition, installedLens.copy());
      installedLens = ItemStack.EMPTY;
    }
    super.destroy();
  }

  @Override
  protected AABB createRenderBoundingBox() {
    return new AABB(worldPosition).expandTowards(0, -3, 0).inflate(.4);
  }

  @Override
  public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
    super.addToGoggleTooltip(tooltip, sneaking);
    tooltip.add(
        Component.translatable(
            "tooltip.creationaugmenter.lens", lensKind() == null ? "—" : lensKind().id()));
    tooltip.add(
        Component.translatable(
            "tooltip.creationaugmenter.phase", phase.name().toLowerCase(java.util.Locale.ROOT)));
    if (!installedLens.isEmpty())
      tooltip.add(
          Component.translatable(
              "tooltip.creationaugmenter.uses",
              installedLens.getMaxDamage() - installedLens.getDamageValue(),
              installedLens.getMaxDamage()));
    tooltip.add(Component.translatable("tooltip.creationaugmenter.minimum_rpm"));
    return true;
  }
}
