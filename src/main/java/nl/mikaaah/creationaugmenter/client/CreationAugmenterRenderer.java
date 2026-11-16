package nl.mikaaah.creationaugmenter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import nl.mikaaah.creationaugmenter.*;

/** Multi-texture machine and socket renderer; shaft rendering works with or without Flywheel. */
public final class CreationAugmenterRenderer
    implements BlockEntityRenderer<CreationAugmenterBlockEntity> {
  public CreationAugmenterRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      CreationAugmenterBlockEntity be,
      float partial,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    CreationEffects.track(be);
    CoreSpectrum.prepare(be.getLevel().getGameTime(), partial);
    var machine = CreationGeometry.get(CreationAugmenter.id("geometry/machine.json"));
    var animator =
        CreationAnimation.state(be)
            .animator(partial, (float) Math.toDegrees(ShaftRenderer.angle(be)));
    pose.pushPose();
    pose.translate(.5, 0, .5);
    if (be.getBlockState().getValue(CreationAugmenterBlock.HORIZONTAL_FACING).getAxis()
        == Direction.Axis.Z) pose.mulPose(Axis.YP.rotationDegrees(-90));
    pose.scale(1f / 16, 1f / 16, 1f / 16);
    machine.render(pose, buffers, light, overlay, animator, null, true);
    var lens = be.lensKind();
    if (lens != null)
      CreationGeometry.get(CreationAugmenter.id("geometry/lenses/" + lens.id() + ".json"))
          .render(pose, buffers, light, overlay, animator, machine, false);
    pose.popPose();
    ShaftRenderer.render(be, pose, buffers, light);
    CreationBeamRenderer.render(be, partial, pose, buffers);
  }

  @Override
  public boolean shouldRenderOffScreen(CreationAugmenterBlockEntity be) {
    return true;
  }

  @Override
  public int getViewDistance() {
    return 64;
  }

  @Override
  public AABB getRenderBoundingBox(CreationAugmenterBlockEntity be) {
    return new AABB(be.getBlockPos())
        .minmax(new AABB(be.getTargetPosition(), be.getTargetPosition()))
        .inflate(.65);
  }
}
