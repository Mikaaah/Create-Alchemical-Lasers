package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;

/** Uses the complete textured authored machine in GUI, hand, ground and frame contexts. */
public final class LaserItemRenderer extends BlockEntityWithoutLevelRenderer {
  public LaserItemRenderer() {
    super(
        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
        Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void renderByItem(
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    pose.pushPose();
    pose.translate(.5, 0, .5);
    pose.scale(1f / 16, 1f / 16, 1f / 16);
    MachineGeometry.get(AlchemicalLaser.id("geometry/machine.json"))
        .render(pose, buffers, light, overlay, (name, pivot, bones) -> {}, null, false);
    pose.popPose();
  }
}
