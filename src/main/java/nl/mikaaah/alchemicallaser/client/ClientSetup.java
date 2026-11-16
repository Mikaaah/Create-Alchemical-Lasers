package nl.mikaaah.alchemicallaser.client;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;
import nl.mikaaah.alchemicallaser.ponder.LaserPonderPlugin;

@EventBusSubscriber(modid = AlchemicalLaser.ID, value = Dist.CLIENT)
public final class ClientSetup {
  static {
    LaserPartials.init();
  }

  @SubscribeEvent
  public static void reloads(
      net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(
        (net.minecraft.server.packs.resources.ResourceManagerReloadListener)
            manager -> {
              MachineGeometry.clear();
              MachineAnimation.clear();
            });
  }

  @SubscribeEvent
  public static void particles(
      net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
    LaserParticle.register(event);
  }

  @SubscribeEvent
  public static void itemExtensions(
      net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
    event.registerItem(
        new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
          private LaserItemRenderer renderer;

          @Override
          public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (renderer == null) renderer = new LaserItemRenderer();
            return renderer;
          }
        },
        AlchemicalLaser.EMITTER_ITEM.get());
  }

  @SubscribeEvent
  public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(AlchemicalLaser.LASER_ENTITY.get(), LaserRenderer::new);
  }

  @SubscribeEvent
  public static void setup(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          PonderIndex.addPlugin(new LaserPonderPlugin());
          dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer.builder(
                  AlchemicalLaser.LASER_ENTITY.get())
              .factory(LaserKineticVisual::new)
              .skipVanillaRender(be -> false)
              .apply();
        });
  }
}
