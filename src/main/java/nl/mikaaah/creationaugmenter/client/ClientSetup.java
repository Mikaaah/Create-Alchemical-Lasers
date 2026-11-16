package nl.mikaaah.creationaugmenter.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import nl.mikaaah.creationaugmenter.CreationAugmenter;

@EventBusSubscriber(modid = CreationAugmenter.ID, value = Dist.CLIENT)
public final class ClientSetup {
  static {
    CreationPartials.init();
  }

  @SubscribeEvent
  public static void reloads(
      net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(
        (net.minecraft.server.packs.resources.ResourceManagerReloadListener)
            manager -> {
              CreationEffects.clear();
              CreationGeometry.clear();
              CreationAnimation.clear();
              CoreSpectrum.clear();
            });
  }

  @SubscribeEvent
  public static void itemExtensions(
      net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
    event.registerItem(
        new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
          private CreationItemRenderer renderer;

          @Override
          public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) renderer = new CreationItemRenderer();
            return renderer;
          }
        },
        CreationAugmenter.BLOCK_ITEM.get());
  }

  @SubscribeEvent
  public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(
        CreationAugmenter.BLOCK_ENTITY.get(), CreationAugmenterRenderer::new);
  }

  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          NeoForge.EVENT_BUS.addListener(CreationEffects::tick);
          dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer.builder(
                  CreationAugmenter.BLOCK_ENTITY.get())
              .factory(CreationKineticVisual::new)
              .skipVanillaRender(be -> false)
              .apply();
        });
  }
}
