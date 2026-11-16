package nl.mikaaah.alchemicallaser.mixin;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import nl.mikaaah.alchemicallaser.LaserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DepotBehaviour.class, remap = false)
public abstract class LaserDepotProcessingMixin {
  @Redirect(
      method = "tick()V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;get(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/foundation/blockEntity/behaviour/BehaviourType;)Lcom/simibubi/create/foundation/blockEntity/behaviour/BlockEntityBehaviour;"))
  private BlockEntityBehaviour findLaserProcessing(
      BlockGetter level, BlockPos pos, BehaviourType<?> type) {
    return LaserBlockEntity.findProcessingBehaviour(level, pos);
  }
}
