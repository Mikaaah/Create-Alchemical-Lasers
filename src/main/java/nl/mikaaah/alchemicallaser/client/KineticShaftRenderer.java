package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import nl.mikaaah.alchemicallaser.LaserBlockEntity;

public final class KineticShaftRenderer {
  private KineticShaftRenderer() {}

  static float angle(LaserBlockEntity be) {
    return KineticBlockEntityRenderer.getAngleForBe(
        be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be));
  }

  public static void render(
      LaserBlockEntity be, PoseStack pose, MultiBufferSource buffers, int light) {
    if (be.getLevel() != null && VisualizationManager.supportsVisualization(be.getLevel())) return;
    KineticBlockEntityRenderer.renderRotatingKineticBlock(
        be,
        KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(be)),
        pose,
        buffers.getBuffer(RenderType.cutoutMipped()),
        light);
  }

  public static void renderItem(PoseStack pose, MultiBufferSource buffers, int light) {
    CachedBuffers.block(
            KineticBlockEntityRenderer.KINETIC_BLOCK,
            KineticBlockEntityRenderer.shaft(Direction.Axis.X))
        .light(light)
        .renderInto(pose, buffers.getBuffer(RenderType.cutoutMipped()));
  }
}
