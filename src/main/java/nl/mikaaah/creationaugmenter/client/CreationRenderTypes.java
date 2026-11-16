package nl.mikaaah.creationaugmenter.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Depth-tested SRC_ALPHA + ONE energy; eyes shader is genuinely unlit and retains soft alpha edges.
 * NEW_ENTITY still supplies UV1/UV2/normals, even though this vanilla fullbright shader ignores
 * them. Color writes only, so transparent ribbons do not occlude the world.
 */
public final class CreationRenderTypes extends RenderType {
  private CreationRenderTypes() {
    super(
        "creation_augmenter_unused",
        DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS,
        256,
        false,
        false,
        () -> {},
        () -> {});
  }

  private static RenderType additive(String name, String texture) {
    return create(
        name,
        DefaultVertexFormat.NEW_ENTITY,
        VertexFormat.Mode.QUADS,
        4096,
        false,
        false,
        CompositeState.builder()
            .setShaderState(RENDERTYPE_EYES_SHADER)
            .setTextureState(
                new TextureStateShard(
                    ResourceLocation.fromNamespaceAndPath("creationaugmenter", texture),
                    false,
                    false))
            .setTransparencyState(LIGHTNING_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY)
            .setWriteMaskState(COLOR_WRITE)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .createCompositeState(false));
  }

  public static final RenderType CORE =
      additive("creation_augmenter_core", "textures/effect/plasma.png");
  public static final RenderType HELIX =
      additive("creation_augmenter_helix", "textures/effect/helix.png");
  public static final RenderType AURA =
      additive("creation_augmenter_aura", "textures/effect/aura.png");
}
