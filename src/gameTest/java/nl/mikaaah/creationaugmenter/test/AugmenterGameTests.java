package nl.mikaaah.creationaugmenter.test;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import nl.mikaaah.creationaugmenter.CreationAugmenter;
import nl.mikaaah.creationaugmenter.CreationAugmenterBlockEntity;
import nl.mikaaah.creationaugmenter.CreationLens;
import nl.mikaaah.creationaugmenter.recipe.CreationAugmentingRecipe;

@GameTestHolder(CreationAugmenter.ID)
@PrefixGameTestTemplate(false)
public final class AugmenterGameTests {
  private static final BlockPos MACHINE = new BlockPos(3, 4, 3),
      DEPOT = new BlockPos(3, 1, 3),
      MOTOR = new BlockPos(3, 4, 4);

  private static CreationAugmenterBlockEntity setup(GameTestHelper h) {
    h.setBlock(DEPOT, AllBlocks.DEPOT.getDefaultState());
    h.setBlock(MACHINE, CreationAugmenter.BLOCK.get().defaultBlockState());
    h.setBlock(
        MOTOR,
        AllBlocks.CREATIVE_MOTOR
            .getDefaultState()
            .setValue(DirectionalKineticBlock.FACING, Direction.NORTH));
    var motor = (CreativeMotorBlockEntity) h.getBlockEntity(MOTOR);
    motor.generatedSpeed.setValue(128);
    motor.updateGeneratedRotation();
    var machine = (CreationAugmenterBlockEntity) h.getBlockEntity(MACHINE);
    machine.exchangeLens(CreationAugmenter.LENSES.get(CreationLens.UNBREAKING).toStack());
    ((DepotBlockEntity) h.getBlockEntity(DEPOT))
        .getBehaviour(DepotBehaviour.TYPE)
        .setCenteredHeldItem(new TransportedItemStack(new ItemStack(Items.DIAMOND_PICKAXE)));
    return machine;
  }

  @GameTest(template = "empty", timeoutTicks = 10)
  public static void unbreakablePreservesOriginalAndDamage(GameTestHelper h) {
    ItemStack input = new ItemStack(Items.DIAMOND_SWORD);
    input.set(DataComponents.DAMAGE, 47);
    ItemStack output = CreationAugmenterBlockEntity.applyUnbreaking(input);
    h.assertTrue(output.has(DataComponents.UNBREAKABLE), "Expected true Unbreakable component");
    h.assertTrue(!input.has(DataComponents.UNBREAKABLE), "Transformation mutated original input");
    h.assertTrue(
        output.getDamageValue() == 47, "Unbreaking unexpectedly repaired or damaged equipment");
    h.assertTrue(
        !CreationAugmenterBlockEntity.canApplyUnbreaking(output),
        "Already-unbreakable equipment can loop forever");
    h.assertTrue(
        !CreationAugmenterBlockEntity.canApplyUnbreaking(new ItemStack(Items.COBBLESTONE)),
        "Non-equipment accepted");
    h.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 180)
  public static void poweredDepotAppliesUnbreakableOnce(GameTestHelper h) {
    setup(h);
    h.succeedWhen(
        () -> {
          ItemStack held =
              ((DepotBlockEntity) h.getBlockEntity(DEPOT))
                  .getBehaviour(DepotBehaviour.TYPE)
                  .getHeldItemStack();
          h.assertTrue(
              held.is(Items.DIAMOND_PICKAXE)
                  && held.getCount() == 1
                  && held.has(DataComponents.UNBREAKABLE),
              "Powered depot failed to preserve and augment its equipment");
        });
  }

  @GameTest(template = "empty", timeoutTicks = 160)
  public static void redstoneInterruptDoesNotChangeInput(GameTestHelper h) {
    setup(h);
    h.runAfterDelay(45, () -> h.setBlock(MACHINE.east(), Blocks.REDSTONE_BLOCK));
    h.runAfterDelay(
        130,
        () -> {
          ItemStack held =
              ((DepotBlockEntity) h.getBlockEntity(DEPOT))
                  .getBehaviour(DepotBehaviour.TYPE)
                  .getHeldItemStack();
          var machine = (CreationAugmenterBlockEntity) h.getBlockEntity(MACHINE);
          h.assertTrue(
              held.is(Items.DIAMOND_PICKAXE)
                  && held.getCount() == 1
                  && !held.has(DataComponents.UNBREAKABLE),
              "Interrupted augmentation lost or modified input");
          h.assertTrue(
              machine.getPhase() == CreationAugmenterBlockEntity.Phase.IDLE,
              "Powered-off machine retained firing state");
          h.assertTrue(!machine.lens().isEmpty(), "Interruption consumed reusable lens");
          h.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 10)
  public static void lensSurvivesSaveWithoutStaleProcessing(GameTestHelper h) {
    h.setBlock(MACHINE, CreationAugmenter.BLOCK.get().defaultBlockState());
    var machine = (CreationAugmenterBlockEntity) h.getBlockEntity(MACHINE);
    machine.exchangeLens(CreationAugmenter.LENSES.get(CreationLens.TRANSMUTATION).toStack());
    var registry = h.getLevel().registryAccess();
    var saved = machine.saveWithFullMetadata(registry);
    var restored =
        (CreationAugmenterBlockEntity)
            BlockEntity.loadStatic(
                h.absolutePos(MACHINE), machine.getBlockState(), saved, registry);
    h.assertTrue(
        restored != null
            && restored.lensKind() == CreationLens.TRANSMUTATION
            && restored.lens().getCount() == 1,
        "Saved lens did not retain identity/count");
    h.assertTrue(
        restored.getPhase() == CreationAugmenterBlockEntity.Phase.IDLE,
        "Restored stale operation state");
    h.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 10)
  public static void recipesRespectLensAndComponentCopy(GameTestHelper h) {
    ItemStack input = new ItemStack(Items.IRON_SWORD);
    input.set(
        DataComponents.CUSTOM_NAME,
        net.minecraft.network.chat.Component.literal("Named test sword"));
    var recipe =
        new CreationAugmentingRecipe(
            CreationLens.ABSOLUTE_CONVERSION,
            Ingredient.of(Items.IRON_SWORD),
            new ItemStack(Items.DIAMOND_SWORD),
            true);
    h.assertTrue(
        recipe.matches(input, CreationLens.ABSOLUTE_CONVERSION), "Correct lens/input rejected");
    h.assertTrue(!recipe.matches(input, CreationLens.TRANSMUTATION), "Wrong lens accepted");
    ItemStack result = recipe.makeResult(input);
    h.assertTrue(
        result.is(Items.DIAMOND_SWORD)
            && result.getHoverName().getString().equals("Named test sword"),
        "Explicit component copy failed");
    h.assertTrue(
        result.getMaxDamage() == new ItemStack(Items.DIAMOND_SWORD).getMaxDamage(),
        "Input default max durability leaked onto output tier");
    h.assertTrue(input.is(Items.IRON_SWORD), "Recipe mutated original input");
    h.succeed();
  }

  private static ItemStack held(GameTestHelper h) {
    return ((DepotBlockEntity) h.getBlockEntity(DEPOT))
        .getBehaviour(DepotBehaviour.TYPE)
        .getHeldItemStack();
  }

  private static void setInput(GameTestHelper h, ItemStack item) {
    ((DepotBlockEntity) h.getBlockEntity(DEPOT))
        .getBehaviour(DepotBehaviour.TYPE)
        .setCenteredHeldItem(new TransportedItemStack(item));
  }

  @GameTest(template = "empty", timeoutTicks = 200)
  public static void noWearBelowMinimumOrBehindObstruction(GameTestHelper h) {
    var be = setup(h);
    var motor = (CreativeMotorBlockEntity) h.getBlockEntity(MOTOR);
    motor.generatedSpeed.setValue(64);
    motor.updateGeneratedRotation();
    h.runAfterDelay(
        60,
        () -> {
          h.assertTrue(
              !held(h).has(DataComponents.UNBREAKABLE) && be.lens().getDamageValue() == 0,
              "Low RPM performed an operation");
          h.setBlock(MACHINE.below(2), Blocks.STONE);
          motor.generatedSpeed.setValue(128);
          motor.updateGeneratedRotation();
        });
    h.runAfterDelay(
        160,
        () -> {
          h.assertTrue(
              !held(h).has(DataComponents.UNBREAKABLE) && be.lens().getDamageValue() == 0,
              "Blocked beam changed equipment or wore lens");
          h.succeed();
        });
  }

  @GameTest(template = "empty", timeoutTicks = 200)
  public static void finalUnbreakingUseCompletesThenShatters(GameTestHelper h) {
    var be = setup(h);
    var lens = CreationAugmenter.LENSES.get(CreationLens.UNBREAKING).toStack();
    h.assertTrue(lens.getMaxDamage() == 10, "Unbreaking lens must have exactly ten uses");
    lens.setDamageValue(9);
    be.exchangeLens(lens);
    h.succeedWhen(
        () -> {
          h.assertTrue(
              held(h).has(DataComponents.UNBREAKABLE), "Last use failed to augment equipment");
          h.assertTrue(be.lens().isEmpty(), "Exhausted lens stayed in socket");
          h.assertTrue(lens.getDamageValue() == 9, "Machine mutated caller-owned lens stack");
        });
  }

  @GameTest(template = "empty", timeoutTicks = 240)
  public static void configuredTimeAndConversionFinalUse(GameTestHelper h) {
    var be = setup(h);
    var lens = CreationAugmenter.LENSES.get(CreationLens.TRANSMUTATION).toStack();
    h.assertTrue(
        lens.getMaxDamage() == 100
            && CreationAugmenter.LENSES
                    .get(CreationLens.ABSOLUTE_CONVERSION)
                    .toStack()
                    .getMaxDamage()
                == 100,
        "Matter lenses must have 100 uses");
    lens.setDamageValue(99);
    be.exchangeLens(lens);
    setInput(h, new ItemStack(Items.DIAMOND));
    h.runAfterDelay(
        85,
        () -> {
          h.assertTrue(
              held(h).is(Items.DIAMOND) && be.lens().getDamageValue() == 99,
              "Recipe ignored configured processing time or wore early");
        });
    h.succeedWhen(
        () -> {
          h.assertTrue(
              held(h).is(Items.EMERALD) && held(h).getCount() == 2,
              "Conversion output is incorrect");
          h.assertTrue(be.lens().isEmpty(), "100th use failed to shatter lens");
        });
  }

  @GameTest(template = "empty", timeoutTicks = 400)
  public static void recipeSpecificRpmIsEnforced(GameTestHelper h) {
    var be = setup(h);
    be.exchangeLens(CreationAugmenter.LENSES.get(CreationLens.TRANSMUTATION).toStack());
    setInput(h, new ItemStack(Items.IRON_PICKAXE));
    h.runAfterDelay(
        100,
        () -> {
          h.assertTrue(
              held(h).is(Items.IRON_PICKAXE) && be.lens().getDamageValue() == 0,
              "256 RPM recipe started at 128 RPM");
          var motor = (CreativeMotorBlockEntity) h.getBlockEntity(MOTOR);
          motor.generatedSpeed.setValue(256);
          motor.updateGeneratedRotation();
        });
    h.succeedWhen(
        () -> {
          h.assertTrue(
              held(h).is(Items.DIAMOND_PICKAXE) && be.lens().getDamageValue() == 1,
              "Higher RPM did not complete exactly one operation");
        });
  }

  @GameTest(template = "empty", timeoutTicks = 1350)
  public static void unbreakingLensShattersAfterExactlyTenOperations(GameTestHelper h) {
    var be = setup(h);
    for (int operation = 1; operation <= 10; operation++) {
      final int n = operation;
      h.runAfterDelay(
          (n - 1) * 130 + 100,
          () -> {
            h.assertTrue(
                held(h).has(DataComponents.UNBREAKABLE),
                "Missing augmented output at operation " + n);
            h.assertTrue(
                n == 10 ? be.lens().isEmpty() : be.lens().getDamageValue() == n,
                "Incorrect lens wear at operation " + n);
            if (n == 10) h.succeed();
          });
      if (operation < 10)
        h.runAfterDelay(
            (operation - 1) * 130 + 110, () -> setInput(h, new ItemStack(Items.DIAMOND_PICKAXE)));
    }
  }

  @GameTest(template = "empty", timeoutTicks = 10)
  public static void recipeCodecsPreserveConfiguredFieldsAndOwnership(GameTestHelper h) {
    var registry = h.getLevel().registryAccess();
    var ops = registry.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
    var json =
        com.google.gson.JsonParser.parseString(
            "{\"lens\":\"creationaugmenter:transmutation_lens\",\"ingredient\":{\"item\":\"minecraft:diamond\"},\"result\":{\"id\":\"minecraft:emerald\"},\"processing_time\":137,\"min_rpm\":256}");
    var recipe = CreationAugmentingRecipe.CODEC.codec().parse(ops, json).getOrThrow();
    var buffer =
        new net.minecraft.network.RegistryFriendlyByteBuf(
            io.netty.buffer.Unpooled.buffer(), registry);
    try {
      var serializer = new CreationAugmentingRecipe.Serializer();
      serializer.streamCodec().encode(buffer, recipe);
      var decoded = serializer.streamCodec().decode(buffer);
      h.assertTrue(
          decoded.processingTime() == 137
              && decoded.minRpm() == 256
              && decoded.lens() == CreationLens.TRANSMUTATION,
          "Recipe network codec lost fields");
      recipe.result().shrink(1);
      h.assertTrue(
          recipe.result().getCount() == 1, "Recipe result accessor exposed mutable shared state");
    } finally {
      buffer.release();
    }
    var bad = json.getAsJsonObject().deepCopy();
    bad.addProperty("min_rpm", 16);
    h.assertTrue(
        CreationAugmentingRecipe.CODEC.codec().parse(ops, bad).error().isPresent(),
        "Recipe accepted below-endgame RPM");
    h.succeed();
  }

  @GameTest(template = "empty", timeoutTicks = 650)
  public static void stackedDepotConversionPreservesAllItems(GameTestHelper h) {
    var be = setup(h);
    be.exchangeLens(CreationAugmenter.LENSES.get(CreationLens.TRANSMUTATION).toStack());
    setInput(h, new ItemStack(Items.DIAMOND, 3));
    h.succeedWhen(
        () -> {
          var inventory =
              h.getLevel()
                  .getCapability(
                      net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                      h.absolutePos(DEPOT),
                      null);
          h.assertTrue(inventory != null, "Depot item capability missing");
          int emeralds = 0, diamonds = 0;
          for (int slot = 0; slot < inventory.getSlots(); slot++) {
            var item = inventory.getStackInSlot(slot);
            if (item.is(Items.EMERALD)) emeralds += item.getCount();
            if (item.is(Items.DIAMOND)) diamonds += item.getCount();
          }
          h.assertTrue(
              emeralds == 6 && diamonds == 0 && be.lens().getDamageValue() == 3,
              "Stacked conversion lost items or failed to spend one use per input");
        });
  }
}
