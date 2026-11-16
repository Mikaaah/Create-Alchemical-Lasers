package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Shared render states for glow masks backed by opaque machine surfaces. */
public final class MachineRenderTypes extends RenderType {
  private static final Function<ResourceLocation, RenderType> OPAQUE_SURFACE_EMISSIVE =
      Util.memoize(
          texture ->
              create(
                  "alchemical_machine_opaque_surface_emissive",
                  DefaultVertexFormat.NEW_ENTITY,
                  VertexFormat.Mode.QUADS,
                  1536,
                  true,
                  false,
                  CompositeState.builder()
                      .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                      .setTextureState(new TextureStateShard(texture, false, false))
                      .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                      .setCullState(NO_CULL)
                      .setWriteMaskState(COLOR_WRITE)
                      .setOverlayState(OVERLAY)
                      .createCompositeState(true)));

  private MachineRenderTypes() {
    super(
        "alchemical_machine_unused",
        DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS,
        256,
        false,
        false,
        () -> {},
        () -> {});
  }

  /**
   * Matches vanilla's translucent-emissive state without sorting opaque-backed masks. The opaque
   * base supplies depth; retain vanilla's LEQUAL test, color-only writes and unshifted geometry.
   */
  public static RenderType opaqueSurfaceEmissive(ResourceLocation texture) {
    return OPAQUE_SURFACE_EMISSIVE.apply(texture);
  }
}
