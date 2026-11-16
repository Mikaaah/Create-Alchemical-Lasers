package nl.mikaaah.creationaugmenter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;
import nl.mikaaah.creationaugmenter.CreationAugmenter;

/** Uses the complete authored housing for inventory, held-item and display contexts. */
public final class CreationItemRenderer extends BlockEntityWithoutLevelRenderer {
  public CreationItemRenderer() {
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
    var level = Minecraft.getInstance().level;
    CoreSpectrum.prepare(
        level == null ? 0 : level.getGameTime(),
        level == null ? 0 : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    pose.pushPose();
    pose.translate(.5, 0, .5);
    pose.scale(1f / 16, 1f / 16, 1f / 16);
    CreationGeometry.get(CreationAugmenter.id("geometry/machine.json"))
        .render(pose, buffers, light, overlay, (name, pivot, bones) -> {}, null, false);
    pose.popPose();
  }
}
