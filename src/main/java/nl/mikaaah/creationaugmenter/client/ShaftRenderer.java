package nl.mikaaah.creationaugmenter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import nl.mikaaah.creationaugmenter.CreationAugmenterBlockEntity;
import org.joml.Quaternionf;

public final class ShaftRenderer {
  private ShaftRenderer() {}

  static float angle(CreationAugmenterBlockEntity be) {
    return KineticBlockEntityRenderer.getAngleForBe(
        be,
        be.getBlockPos(),
        be.getBlockState()
            .getValue(nl.mikaaah.creationaugmenter.CreationAugmenterBlock.HORIZONTAL_FACING)
            .getAxis());
  }

  public static void render(
      CreationAugmenterBlockEntity be, PoseStack pose, MultiBufferSource buffers, int light) {
    if (be.getLevel() != null && VisualizationManager.supportsVisualization(be.getLevel())) return;
    pose.pushPose();
    if (be.getBlockState()
            .getValue(nl.mikaaah.creationaugmenter.CreationAugmenterBlock.HORIZONTAL_FACING)
            .getAxis()
        == Direction.Axis.Z) {
      pose.translate(.5, 0, .5);
      pose.mulPose(new Quaternionf().rotationY(-(float) Math.PI / 2));
      pose.translate(-.5, 0, -.5);
    }
    pose.translate(.5, .5, .5);
    pose.mulPose(new Quaternionf().rotationX(angle(be)));
    pose.translate(-.5, -.5, -.5);
    CachedBuffers.partial(CreationPartials.SIDE_SHAFT, be.getBlockState())
        .light(light)
        .renderInto(pose, buffers.getBuffer(RenderType.cutoutMipped()));
    pose.popPose();
  }
}
