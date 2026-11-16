package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/** Load-time atlas inspection; a cutout texture must never be treated as a solid occluder. */
public final class GeometryOpacity implements AutoCloseable {
  private final ResourceManager resources;
  private final Map<ResourceLocation, NativeImage> images = new HashMap<>();
  private final Map<Patch, Boolean> opaquePatches = new HashMap<>();

  public GeometryOpacity(ResourceManager resources) {
    this.resources = resources;
  }

  /** UV dimensions are logical atlas pixels; resource packs may supply larger physical images. */
  public boolean isOpaque(
      ResourceLocation texture, float[] uv, int logicalWidth, int logicalHeight) {
    if (logicalWidth <= 0 || logicalHeight <= 0 || uv.length != 4) return false;
    for (float value : uv) if (!Float.isFinite(value)) return false;
    NativeImage image = image(texture);
    if (image == null) return false;
    int x0 = (int) Math.floor(Math.min(uv[0], uv[2]) * image.getWidth() / logicalWidth);
    int x1 = (int) Math.ceil(Math.max(uv[0], uv[2]) * image.getWidth() / logicalWidth);
    int y0 = (int) Math.floor(Math.min(uv[1], uv[3]) * image.getHeight() / logicalHeight);
    int y1 = (int) Math.ceil(Math.max(uv[1], uv[3]) * image.getHeight() / logicalHeight);
    if (x0 < 0 || y0 < 0 || x1 > image.getWidth() || y1 > image.getHeight() || x0 >= x1 || y0 >= y1)
      return false;
    Patch patch = new Patch(texture, x0, y0, x1, y1);
    return opaquePatches.computeIfAbsent(
        patch,
        ignored -> {
          for (int y = y0; y < y1; y++)
            for (int x = x0; x < x1; x++)
              if ((image.getPixelRGBA(x, y) >>> 24) != 255) return false;
          return true;
        });
  }

  private NativeImage image(ResourceLocation texture) {
    if (images.containsKey(texture)) return images.get(texture);
    NativeImage image = null;
    try (var input = resources.getResourceOrThrow(texture).open()) {
      image = NativeImage.read(NativeImage.Format.RGBA, input);
    } catch (IOException | RuntimeException ignored) {
      // Missing or malformed resources cannot safely hide other geometry.
    }
    images.put(texture, image);
    return image;
  }

  @Override
  public void close() {
    for (NativeImage image : images.values()) if (image != null) image.close();
    images.clear();
    opaquePatches.clear();
  }

  private record Patch(ResourceLocation texture, int x0, int y0, int x1, int y1) {}
}
