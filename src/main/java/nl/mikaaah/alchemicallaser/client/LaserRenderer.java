package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import nl.mikaaah.alchemicallaser.*;

/** The model and lens share authored bones; the beam keeps the established processing endpoint. */
public final class LaserRenderer implements BlockEntityRenderer<LaserBlockEntity> {
  public LaserRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      LaserBlockEntity be,
      float partial,
      PoseStack pose,
      MultiBufferSource buffer,
      int light,
      int overlay) {
    var animation = MachineAnimation.state(be);
    var machine = MachineGeometry.get(AlchemicalLaser.id("geometry/machine.json"));
    var animator =
        animation.animator(partial, (float) Math.toDegrees(KineticShaftRenderer.angle(be)));
    pose.pushPose();
    pose.translate(.5, 0, .5);
    if (be.getBlockState().getValue(LaserBlock.HORIZONTAL_FACING).getAxis() == Direction.Axis.Z)
      pose.mulPose(Axis.YP.rotationDegrees(-90));
    pose.scale(1f / 16, 1f / 16, 1f / 16);
    machine.render(pose, buffer, light, overlay, animator, null, true);
    if (AlchemicalLaser.LENSES.values().stream().anyMatch(lens -> be.lens().is(lens.get())))
      MachineGeometry.get(
              AlchemicalLaser.id("geometry/lenses/" + VisualLens.of(be.lens()).id() + ".json"))
          .render(pose, buffer, light, overlay, animator, machine, false);
    pose.popPose();
    KineticShaftRenderer.render(be, pose, buffer, light);
    if (!be.lens().isEmpty()
        && AlchemicalLaser.LENSES.values().stream().noneMatch(lens -> be.lens().is(lens.get()))) {
      pose.pushPose();
      pose.translate(
          .5, MachineAnimation.MUZZLE_Y + .08 + animation.nozzleOffset(partial) / 16, .5);
      pose.mulPose(Axis.XP.rotationDegrees(90));
      pose.scale(.38f, .38f, .38f);
      Minecraft.getInstance()
          .getItemRenderer()
          .renderStatic(
              be.lens(),
              ItemDisplayContext.FIXED,
              light,
              OverlayTexture.NO_OVERLAY,
              pose,
              buffer,
              be.getLevel(),
              0);
      pose.popPose();
    }
    LaserBeamRenderer.render(be, partial, pose, buffer);
  }

  @Override
  public int getViewDistance() {
    return 64;
  }

  @Override
  public boolean shouldRenderOffScreen(LaserBlockEntity be) {
    return true;
  }

  @Override
  public AABB getRenderBoundingBox(LaserBlockEntity be) {
    return new AABB(be.getBlockPos()).expandTowards(0, -1.1, 0).inflate(.35);
  }
}
