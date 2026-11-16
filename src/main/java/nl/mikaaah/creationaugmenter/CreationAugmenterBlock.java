package nl.mikaaah.creationaugmenter;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CreationAugmenterBlock extends HorizontalKineticBlock
    implements IBE<CreationAugmenterBlockEntity> {
  private static final VoxelShape OUTLINE =
      Shapes.or(Shapes.block(), box(1, -7, 1, 15, 0, 15), box(2.5, -15, 2.5, 13.5, -7, 13.5));

  public CreationAugmenterBlock(Properties properties) {
    super(properties);
  }

  @Override
  public Direction.Axis getRotationAxis(BlockState state) {
    return state.getValue(HORIZONTAL_FACING).getAxis();
  }

  @Override
  public boolean hasShaftTowards(
      LevelReader level, BlockPos pos, BlockState state, Direction face) {
    return face.getAxis() == getRotationAxis(state);
  }

  @Override
  public Class<CreationAugmenterBlockEntity> getBlockEntityClass() {
    return CreationAugmenterBlockEntity.class;
  }

  @Override
  public BlockEntityType<? extends CreationAugmenterBlockEntity> getBlockEntityType() {
    return CreationAugmenter.BLOCK_ENTITY.get();
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return OUTLINE;
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Shapes.block();
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack held,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (held.isEmpty() || held.is(AllItems.WRENCH.get()))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (CreationLens.of(held) == null)
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (!player.mayBuild()) return ItemInteractionResult.FAIL;
    if (!level.isClientSide
        && level.getBlockEntity(pos) instanceof CreationAugmenterBlockEntity be) {
      if (!be.lens().isEmpty()) {
        player.displayClientMessage(
            Component.translatable("message.creationaugmenter.remove_lens_first"), true);
        return ItemInteractionResult.CONSUME;
      }
      be.exchangeLens(held.copyWithCount(1));
      if (!player.isCreative()) held.shrink(1);
    }
    return ItemInteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player.isShiftKeyDown()
        && player.getMainHandItem().isEmpty()
        && player.mayBuild()
        && level.getBlockEntity(pos) instanceof CreationAugmenterBlockEntity be
        && !be.lens().isEmpty()) {
      if (!level.isClientSide) {
        ItemStack old = be.exchangeLens(ItemStack.EMPTY);
        if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
    }
    return InteractionResult.PASS;
  }
}
