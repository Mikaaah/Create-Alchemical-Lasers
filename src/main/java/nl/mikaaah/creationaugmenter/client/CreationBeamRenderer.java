package nl.mikaaah.creationaugmenter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nl.mikaaah.creationaugmenter.CreationAugmenterBlockEntity;

/** Three bounded, depth-tested energy layers emitted strictly downward from the moving lens. */
public final class CreationBeamRenderer {
  private static final int FULL_BRIGHT = 0xF000F0;
  private static final int CREATIVE_MAGENTA = 0xCE59E7;
  private static final int CREATIVE_VIOLET = 0x8663DA;

  private CreationBeamRenderer() {}

  public static void render(
      CreationAugmenterBlockEntity be, float partial, PoseStack stack, MultiBufferSource buffers) {
    if (be.getLevel() == null || !be.isBeamActive() || be.lensKind() == null) return;
    Vec3 muzzle = CreationAnimation.muzzle(be, partial);
    float length = boundedLength((float) (muzzle.y - be.getTargetPosition().y));
    float power = Mth.clamp(CreationAnimation.state(be).charge(partial), 0, 1);
    if (length < .001f || power < .001f) return;
    long ticks = be.getLevel().getGameTime();
    float time = (ticks % 24000) + partial;
    int spectrum = CoreSpectrum.color(ticks, partial);
    stack.pushPose();
    stack.translate(
        muzzle.x - be.getBlockPos().getX(),
        muzzle.y - be.getBlockPos().getY(),
        muzzle.z - be.getBlockPos().getZ());
    renderLayers(
        stack,
        buffers,
        time,
        length,
        spectrum,
        be.lensKind().color(),
        be.lensKind().secondaryColor(),
        power);
    stack.popPose();
  }

  /** Public entry point for deterministic art previews, sharing every runtime beam formula. */
  public static void renderLayers(
      PoseStack stack,
      MultiBufferSource buffers,
      float time,
      float length,
      int spectrum,
      int lensColor,
      int secondaryColor,
      float power) {
    length = boundedLength(length);
    if (length < .001f || !Float.isFinite(time)) return;
    power = Mth.clamp(power, 0, 1);
    PoseStack.Pose pose = stack.last();

    // Thin white/iridescent prismatic core: a four-sided voxel prism, not a cylinder.
    VertexConsumer core = buffers.getBuffer(CreationRenderTypes.CORE);
    float diameter = .065f + .012f * Mth.sin(time * 1.7f);
    prism(
        pose,
        core,
        length,
        diameter * .5f,
        CoreSpectrum.mix(0xFFFFFF, spectrum, .32f),
        (.82f + .12f * Mth.sin(time * 2.3f)) * power,
        time * .8f,
        .0015f * Mth.sin(time * 25));

    VertexConsumer helix = buffers.getBuffer(CreationRenderTypes.HELIX);
    ribbon(
        pose,
        helix,
        length,
        time,
        1,
        CoreSpectrum.mix(lensColor, CREATIVE_MAGENTA, .24f),
        spectrum,
        power);
    ribbon(
        pose,
        helix,
        length,
        time,
        -1,
        CoreSpectrum.mix(secondaryColor, CREATIVE_VIOLET, .30f),
        spectrum,
        power);

    VertexConsumer aura = buffers.getBuffer(CreationRenderTypes.AURA);
    int containment = CoreSpectrum.mix(lensColor, spectrum, .30f);
    crossedAura(pose, aura, length, .23f, containment, .10f * power, time);
    crossedAura(pose, aura, length, .31f, CREATIVE_VIOLET, .035f * power, -time * .7f);

    // Infusion bands launch every five ticks and travel toward the target item.
    float spacing = .55f;
    float phase = positiveModulo(time * .11f, spacing);
    for (float distance = phase; distance < length; distance += spacing) {
      float envelope = Math.min(1, distance / .09f) * Math.min(1, (length - distance) / .12f);
      squareRing(pose, aura, -distance, .16f, .015f, containment, .38f * power * envelope, 0);
    }
    // A pair of crisp square seals wraps the item without covering the item renderer.
    for (int ring = 0; ring < 2; ring++) {
      float age = positiveModulo(time + ring * 8, 16) / 16;
      squareRing(
          pose,
          aura,
          -length + .015f + (1 - age) * .10f,
          .18f + age * .30f,
          .018f,
          CoreSpectrum.mix(containment, secondaryColor, age),
          (1 - age) * .40f * power,
          ring == 0 ? 0 : Mth.PI / 4);
    }
  }

  public static float boundedLength(float length) {
    return Float.isFinite(length) ? Mth.clamp(length, 0, 8) : 0;
  }

  public static AABB boundingBox(CreationAugmenterBlockEntity be) {
    Vec3 source = CreationAnimation.muzzle(be, 0);
    Vec3 target = be.getTargetPosition();
    return new AABB(source, new Vec3(source.x, target.y, source.z))
        .inflate(.75)
        .minmax(new AABB(be.getBlockPos()).expandTowards(0, -1.2, 0).inflate(.3));
  }

  private static void prism(
      PoseStack.Pose pose,
      VertexConsumer out,
      float length,
      float radius,
      int color,
      float alpha,
      float scroll,
      float jitter) {
    float[][] corners = {
      {-radius, -radius}, {radius, -radius}, {radius, radius}, {-radius, radius}
    };
    for (int i = 0; i < 4; i++) {
      int j = (i + 1) % 4;
      float nx = (corners[i][0] + corners[j][0]) / (2 * radius);
      float nz = (corners[i][1] + corners[j][1]) / (2 * radius);
      vertex(
          pose, out, corners[i][0] + jitter, 0, corners[i][1], 0, -scroll, nx, 0, nz, color, alpha);
      vertex(
          pose, out, corners[j][0] + jitter, 0, corners[j][1], 1, -scroll, nx, 0, nz, color, alpha);
      vertex(
          pose,
          out,
          corners[j][0] - jitter,
          -length,
          corners[j][1],
          1,
          length * 5 - scroll,
          nx,
          0,
          nz,
          color,
          alpha);
      vertex(
          pose,
          out,
          corners[i][0] - jitter,
          -length,
          corners[i][1],
          0,
          length * 5 - scroll,
          nx,
          0,
          nz,
          color,
          alpha);
    }
  }

  private static void ribbon(
      PoseStack.Pose pose,
      VertexConsumer out,
      float length,
      float time,
      int direction,
      int color,
      int spectrum,
      float power) {
    int segments = Mth.clamp(Mth.ceil(length * 16), 1, 128);
    float radius = .11f + .006f * Mth.sin(time * .5f), halfWidth = .025f;
    for (int i = 0; i < segments; i++) {
      float y0 = length * i / segments, y1 = length * (i + 1) / segments;
      float a0 = direction * (time * .25f + y0 * 5.4f);
      float a1 = direction * (time * .25f + y1 * 5.4f);
      if (direction < 0) {
        a0 += Mth.PI;
        a1 += Mth.PI;
      }
      float c0 = Mth.cos(a0), s0 = Mth.sin(a0), c1 = Mth.cos(a1), s1 = Mth.sin(a1);
      float v0 = y0 * 4 - time * .65f, v1 = y1 * 4 - time * .65f;
      int tint = CoreSpectrum.mix(color, spectrum, .12f + .10f * Mth.sin(time * .3f + y0));
      vertex(
          pose,
          out,
          c0 * radius - s0 * halfWidth,
          -y0,
          s0 * radius + c0 * halfWidth,
          0,
          v0,
          c0,
          0,
          s0,
          tint,
          .62f * power);
      vertex(
          pose,
          out,
          c0 * radius + s0 * halfWidth,
          -y0,
          s0 * radius - c0 * halfWidth,
          1,
          v0,
          c0,
          0,
          s0,
          tint,
          .62f * power);
      vertex(
          pose,
          out,
          c1 * radius + s1 * halfWidth,
          -y1,
          s1 * radius - c1 * halfWidth,
          1,
          v1,
          c1,
          0,
          s1,
          tint,
          .62f * power);
      vertex(
          pose,
          out,
          c1 * radius - s1 * halfWidth,
          -y1,
          s1 * radius + c1 * halfWidth,
          0,
          v1,
          c1,
          0,
          s1,
          tint,
          .62f * power);
    }
  }

  private static void crossedAura(
      PoseStack.Pose pose,
      VertexConsumer out,
      float length,
      float radius,
      int color,
      float alpha,
      float time) {
    for (int i = 0; i < 4; i++) {
      float a = i * Mth.PI / 4, dx = Mth.cos(a) * radius, dz = Mth.sin(a) * radius;
      float nx = -Mth.sin(a), nz = Mth.cos(a);
      vertex(pose, out, -dx, 0, -dz, 0, -time * .20f, nx, 0, nz, color, alpha);
      vertex(pose, out, dx, 0, dz, 1, -time * .20f, nx, 0, nz, color, alpha);
      vertex(pose, out, dx, -length, dz, 1, length * 2 - time * .20f, nx, 0, nz, color, alpha);
      vertex(pose, out, -dx, -length, -dz, 0, length * 2 - time * .20f, nx, 0, nz, color, alpha);
    }
  }

  private static void squareRing(
      PoseStack.Pose pose,
      VertexConsumer out,
      float y,
      float radius,
      float halfWidth,
      int color,
      float alpha,
      float rotation) {
    float sin = Mth.sin(rotation), cos = Mth.cos(rotation);
    float[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
    for (int i = 0; i < 4; i++) {
      int next = (i + 1) % 4;
      float x0 = corners[i][0] * cos - corners[i][1] * sin;
      float z0 = corners[i][0] * sin + corners[i][1] * cos;
      float x1 = corners[next][0] * cos - corners[next][1] * sin;
      float z1 = corners[next][0] * sin + corners[next][1] * cos;
      vertex(
          pose,
          out,
          x0 * (radius - halfWidth),
          y,
          z0 * (radius - halfWidth),
          0,
          0,
          0,
          1,
          0,
          color,
          alpha);
      vertex(
          pose,
          out,
          x0 * (radius + halfWidth),
          y,
          z0 * (radius + halfWidth),
          1,
          0,
          0,
          1,
          0,
          color,
          alpha);
      vertex(
          pose,
          out,
          x1 * (radius + halfWidth),
          y,
          z1 * (radius + halfWidth),
          1,
          1,
          0,
          1,
          0,
          color,
          alpha);
      vertex(
          pose,
          out,
          x1 * (radius - halfWidth),
          y,
          z1 * (radius - halfWidth),
          0,
          1,
          0,
          1,
          0,
          color,
          alpha);
    }
  }

  private static float positiveModulo(float number, float divisor) {
    return number - divisor * Mth.floor(number / divisor);
  }

  private static void vertex(
      PoseStack.Pose pose,
      VertexConsumer out,
      float x,
      float y,
      float z,
      float u,
      float v,
      float nx,
      float ny,
      float nz,
      int color,
      float alpha) {
    out.addVertex(pose, x, y, z)
        .setColor(
            (color >>> 16) & 255,
            (color >>> 8) & 255,
            color & 255,
            Math.round(255 * Mth.clamp(alpha, 0, 1)))
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(FULL_BRIGHT)
        .setNormal(pose, nx, ny, nz);
  }
}
