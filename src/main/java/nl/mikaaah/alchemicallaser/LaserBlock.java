package nl.mikaaah.alchemicallaser;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public class LaserBlock extends HorizontalKineticBlock implements IBE<LaserBlockEntity> {
  private static final VoxelShape SHAPE =
      Shapes.or(Shapes.block(), box(1, -6, 1, 15, 0, 15), box(2, -15, 2, 14, -6, 14));

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return Shapes.block();
  }

  @Override
  public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
    return net.minecraft.world.level.block.RenderShape.ENTITYBLOCK_ANIMATED;
  }

  public LaserBlock(Properties properties) {
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
  public Class<LaserBlockEntity> getBlockEntityClass() {
    return LaserBlockEntity.class;
  }

  @Override
  public BlockEntityType<? extends LaserBlockEntity> getBlockEntityType() {
    return AlchemicalLaser.LASER_ENTITY.get();
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
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
    if (!held.isEmpty() && !held.is(AllItems.WRENCH.get())) {
      if (!player.mayBuild()) return ItemInteractionResult.FAIL;
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof LaserBlockEntity be) {
        if (!be.lens().isEmpty()) {
          player.displayClientMessage(
              net.minecraft.network.chat.Component.translatable(
                  "message.alchemicallaser.remove_lens_first"),
              true);
          return ItemInteractionResult.CONSUME;
        }
        be.exchangeLens(held.copyWithCount(1));
        if (!player.isCreative()) held.shrink(1);
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (player.isShiftKeyDown()
        && player.getMainHandItem().isEmpty()
        && player.mayBuild()
        && level.getBlockEntity(pos) instanceof LaserBlockEntity be
        && !be.lens().isEmpty()) {
      if (!level.isClientSide) {
        ItemStack old = be.exchangeLens(ItemStack.EMPTY);
        if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
    }
    return InteractionResult.PASS;
  }

  @Override
  public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int event, int data) {
    return level.getBlockEntity(pos) instanceof LaserBlockEntity be && be.triggerEvent(event, data);
  }
}
