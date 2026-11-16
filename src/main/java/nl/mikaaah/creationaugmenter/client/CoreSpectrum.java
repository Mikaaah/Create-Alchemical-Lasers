package nl.mikaaah.creationaugmenter.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A single world-time clock drives the supplied core's image, beam tint and particles. Standalone
 * entity textures do not consume block-atlas animation metadata, so a live 16x16 DynamicTexture
 * performs the exact frame interpolation explicitly.
 */
public final class CoreSpectrum {
  public static final int FRAME_TICKS = 2;
  public static final ResourceLocation SOURCE =
      ResourceLocation.fromNamespaceAndPath(
          "creationaugmenter", "textures/block/creation_core.png");
  private static final ResourceLocation LIVE =
      ResourceLocation.fromNamespaceAndPath("creationaugmenter", "dynamic/creation_core_live");
  private static int[][] frames;
  private static int[] palette;
  private static DynamicTexture live;
  private static double uploadedTime = Double.NaN;

  private CoreSpectrum() {}

  /** Call on the render thread before submitting core geometry. Shared across all machines. */
  public static void update(double worldTicks) {
    loadSource();
    if (!Double.isFinite(worldTicks)) worldTicks = 0;
    double time = positiveModulo(worldTicks, (double) frames.length * FRAME_TICKS);
    if (live == null) {
      live = new DynamicTexture(16, 16, false);
      live.setFilter(false, false);
      Minecraft.getInstance().getTextureManager().register(LIVE, live);
    }
    if (Double.compare(time, uploadedTime) == 0) return;
    int frame = (int) (time / FRAME_TICKS);
    int next = (frame + 1) % frames.length;
    float blend = (float) (time / FRAME_TICKS - frame);
    NativeImage pixels = live.getPixels();
    if (pixels == null) return;
    for (int y = 0; y < 16; y++)
      for (int x = 0; x < 16; x++) {
        int index = y * 16 + x;
        pixels.setPixelRGBA(
            x, y, interpolateAbgr(frames[frame][index], frames[next][index], blend));
      }
    live.upload();
    uploadedTime = time;
  }

  public static void prepare(long gameTime, float partialTick) {
    // Reduce the integer clock before converting to floating point, retaining sub-tick precision.
    loadSource();
    update(Math.floorMod(gameTime, (long) frames.length * FRAME_TICKS) + partialTick);
  }

  /** The geometry uses the first frame's logical 16x16 UV space, not the sheet's 16x400 space. */
  public static int currentColor() {
    return color(Double.isFinite(uploadedTime) ? uploadedTime : 0);
  }

  public static ResourceLocation texture() {
    return LIVE;
  }

  public static ResourceLocation emissiveTexture() {
    return LIVE;
  }

  public static int color(long gameTime, float partialTick) {
    loadSource();
    return color(Math.floorMod(gameTime, (long) frames.length * FRAME_TICKS) + partialTick);
  }

  public static int color(double worldTicks) {
    loadSource();
    if (!Double.isFinite(worldTicks)) worldTicks = 0;
    double phase = positiveModulo(worldTicks, (double) frames.length * FRAME_TICKS) / FRAME_TICKS;
    int current = (int) phase;
    return mix(
        palette[current], palette[(current + 1) % palette.length], (float) (phase - current));
  }

  /** The client resource-reload apply stage runs on the render thread. */
  public static void clear() {
    if (live != null) Minecraft.getInstance().getTextureManager().release(LIVE);
    live = null;
    frames = null;
    palette = null;
    uploadedTime = Double.NaN;
  }

  private static void loadSource() {
    if (frames != null) return;
    try (var input =
            Minecraft.getInstance().getResourceManager().getResourceOrThrow(SOURCE).open();
        NativeImage sheet = NativeImage.read(input)) {
      if (sheet.getWidth() != 16 || sheet.getHeight() % 16 != 0)
        throw new IOException("Creation core texture must be a vertical strip of 16x16 frames");
      int count = sheet.getHeight() / 16;
      int[][] loaded = new int[count][256];
      int[] colors = new int[count];
      for (int frame = 0; frame < count; frame++) {
        for (int y = 0; y < 16; y++)
          for (int x = 0; x < 16; x++)
            loaded[frame][y * 16 + x] = sheet.getPixelRGBA(x, frame * 16 + y);
        colors[frame] = representativeColor(loaded[frame]);
      }
      frames = loaded;
      palette = colors;
    } catch (Exception error) {
      LogUtils.getLogger().error("Could not load Creation Augmenter core sprite {}", SOURCE, error);
      frames = new int[1][256];
      palette = new int[] {0xD161FF};
      for (int y = 0; y < 16; y++)
        for (int x = 0; x < 16; x++)
          frames[0][y * 16 + x] = ((x / 4 + y / 4) & 1) == 0 ? 0xFFFF00FF : 0xFF16121E;
    }
  }

  /** NativeImage RGBA ints use ABGR byte order. Exclude the core's dark neutral steel bezel. */
  private static int representativeColor(int[] pixels) {
    double red = 0, green = 0, blue = 0, total = 0;
    for (int abgr : pixels) {
      int r = abgr & 255, g = (abgr >>> 8) & 255, b = (abgr >>> 16) & 255, a = abgr >>> 24;
      int value = Math.max(r, Math.max(g, b));
      int chroma = value - Math.min(r, Math.min(g, b));
      if (a == 0 || value < 90 || chroma < value * .25) continue;
      double weight = (double) chroma * value * a;
      red += r * weight;
      green += g * weight;
      blue += b * weight;
      total += weight;
    }
    return total == 0
        ? 0xD161FF
        : ((int) Math.round(red / total) << 16)
            | ((int) Math.round(green / total) << 8)
            | (int) Math.round(blue / total);
  }

  private static int interpolateAbgr(int a, int b, float t) {
    int result = 0;
    for (int shift = 0; shift <= 24; shift += 8)
      result |= Math.round(Mth.lerp(t, (a >>> shift) & 255, (b >>> shift) & 255)) << shift;
    return result;
  }

  public static int mix(int a, int b, float amount) {
    float t = Mth.clamp(amount, 0, 1);
    return Math.round(Mth.lerp(t, (a >>> 16) & 255, (b >>> 16) & 255)) << 16
        | Math.round(Mth.lerp(t, (a >>> 8) & 255, (b >>> 8) & 255)) << 8
        | Math.round(Mth.lerp(t, a & 255, b & 255));
  }

  private static double positiveModulo(double value, double divisor) {
    return value - Math.floor(value / divisor) * divisor;
  }
}
