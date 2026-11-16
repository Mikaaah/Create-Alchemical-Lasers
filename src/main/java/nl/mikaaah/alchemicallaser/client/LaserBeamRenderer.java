package nl.mikaaah.alchemicallaser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nl.mikaaah.alchemicallaser.LaserBlockEntity;

/**
 * All geometry is local to the downward muzzle. No frame-rate-dependent state or particle spawning.
 */
public final class LaserBeamRenderer {
  private static final int FULL_BRIGHT = 0xF000F0;
  private static final int AMETHYST = 0xAC70FF;

  private LaserBeamRenderer() {}

  /**
   * Pose is the block origin; this method owns its muzzle translation. Time is measured in client
   * ticks.
   */
  public static void render(
      LaserBlockEntity be, float partial, PoseStack stack, MultiBufferSource buffers) {
    if (be.getLevel() == null || !be.isBeamActive() || VisualLens.of(be.lens()) == null) return;
    float length =
        boundedLength(
            MachineAnimation.BEAM_LENGTH + MachineAnimation.state(be).nozzleOffset(partial) / 16f);
    float power = Mth.clamp(MachineAnimation.state(be).charge(partial), 0, 1);
    if (length < .001f || power <= .001f) return;
    // Bound the phase: preserve sub-tick interpolation after worlds have run for years.
    float time = (be.getLevel().getGameTime() % 24000) + partial;
    stack.pushPose();
    Vec3 muzzle = MachineAnimation.muzzle(be, partial);
    stack.translate(
        muzzle.x - be.getBlockPos().getX(),
        muzzle.y - be.getBlockPos().getY(),
        muzzle.z - be.getBlockPos().getZ());
    renderLayers(stack, buffers, time, length, be.beamColor(), power, true);
    stack.popPose();
  }

  /**
   * Public geometry entry point for a preview or another -Y machine. Diameter, not radius, is
   * .08-.14 blocks.
   */
  public static void renderLayers(
      PoseStack stack,
      MultiBufferSource buffers,
      float time,
      float length,
      int color,
      float power,
      boolean impact) {
    length = boundedLength(length);
    if (length < .001f || !Float.isFinite(time)) return;
    power = Mth.clamp(power, 0, 1);
    PoseStack.Pose pose = stack.last();
    float diameter = .11f + .03f * Mth.sin(time * 25f);
    float jitter = .004f * Mth.sin(time * 17.3f);
    // Each RenderType is acquired only once; no retained consumer is reused after switching
    // buffers.
    VertexConsumer core = buffers.getBuffer(LaserRenderTypes.CORE);
    prism(pose, core, length, diameter * .5f, 0xF3FBFF, .92f * power, time * .9f, jitter);
    VertexConsumer helix = buffers.getBuffer(LaserRenderTypes.HELIX);
    ribbon(pose, helix, length, time, 1, color, power);
    ribbon(pose, helix, length, time, -1, AMETHYST, power);
    VertexConsumer aura = buffers.getBuffer(LaserRenderTypes.AURA);
    crossedAura(pose, aura, length, .28f, color, .14f * power, time);
    crossedAura(pose, aura, length, .40f, mix(color, AMETHYST, .35f), .055f * power, -time * .7f);
    // A pulse is born at the muzzle every four ticks and travels 0.42 blocks/tick downward.
    float spacing = 1.68f;
    float phase = positiveModulo(time * .42f, spacing);
    for (float y = phase; y < length; y += spacing) {
      float envelope = Math.min(1, y / .18f) * Math.min(1, (length - y) / .18f);
      ring(pose, aura, -y, .16f, .035f, color, .42f * power * envelope, time * .12f);
    }
    if (impact) {
      // Two expanding flat octagonal shockwaves, tucked just above the server raycast surface.
      for (int i = 0; i < 2; i++) {
        float age = positiveModulo(time + i * 6, 12) / 12f;
        ring(
            pose,
            aura,
            -length + .014f,
            .12f + age * .85f,
            .035f + age * .018f,
            color,
            (1 - age) * .6f * power,
            0);
      }
      ring(
          pose,
          aura,
          -length + .018f,
          .10f + .025f * Mth.sin(time * 2),
          .055f,
          0xF4E5FF,
          .7f * power,
          0);
    }
  }

  public static float boundedLength(float value) {
    return Float.isFinite(value) ? Mth.clamp(value, 0, 32) : 0;
  }

  public static AABB boundingBox(LaserBlockEntity be) {
    Vec3 a = MachineAnimation.muzzle(be, 0),
        b = a.add(0, -boundedLength(MachineAnimation.BEAM_LENGTH), 0);
    return new AABB(a, b).inflate(1.1).minmax(new AABB(be.getBlockPos()).inflate(.25));
  }

  private static void prism(
      PoseStack.Pose pose,
      VertexConsumer out,
      float length,
      float r,
      int rgb,
      float alpha,
      float scroll,
      float jitter) {
    float[][] p = {{-r, -r}, {r, -r}, {r, r}, {-r, r}};
    for (int i = 0; i < 4; i++) {
      int j = (i + 1) % 4;
      float nx = (p[i][0] + p[j][0]) / (2 * r), nz = (p[i][1] + p[j][1]) / (2 * r);
      vertex(pose, out, p[i][0] + jitter, 0, p[i][1], 0, -scroll, nx, 0, nz, rgb, alpha);
      vertex(pose, out, p[j][0] + jitter, 0, p[j][1], 1, -scroll, nx, 0, nz, rgb, alpha);
      vertex(
          pose,
          out,
          p[j][0] - jitter,
          -length,
          p[j][1],
          1,
          length * 4 - scroll,
          nx,
          0,
          nz,
          rgb,
          alpha);
      vertex(
          pose,
          out,
          p[i][0] - jitter,
          -length,
          p[i][1],
          0,
          length * 4 - scroll,
          nx,
          0,
          nz,
          rgb,
          alpha);
    }
  }

  private static void ribbon(
      PoseStack.Pose pose,
      VertexConsumer out,
      float length,
      float time,
      int direction,
      int rgb,
      float power) {
    int segments = Mth.clamp(Mth.ceil(length * 16), 1, 512);
    float radius = .115f + .01f * Mth.sin(time * .7f), halfWidth = .035f;
    for (int i = 0; i < segments; i++) {
      float y0 = length * i / segments, y1 = length * (i + 1) / segments;
      float a0 = direction * (time * .32f + y0 * 4.4f), a1 = direction * (time * .32f + y1 * 4.4f);
      if (direction < 0) {
        a0 += Mth.PI;
        a1 += Mth.PI;
      }
      float c0 = Mth.cos(a0), s0 = Mth.sin(a0), c1 = Mth.cos(a1), s1 = Mth.sin(a1);
      float v0 = y0 * 3 - time * .8f, v1 = y1 * 3 - time * .8f;
      int tint =
          mix(rgb, direction > 0 ? AMETHYST : 0xEEE2FF, .22f + .15f * Mth.sin(time * .4f + y0));
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
          .64f * power);
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
          .64f * power);
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
          .64f * power);
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
          .64f * power);
    }
  }

  private static void crossedAura(
      PoseStack.Pose pose,
      VertexConsumer out,
      float length,
      float radius,
      int rgb,
      float alpha,
      float time) {
    for (int i = 0; i < 4; i++) {
      float angle = i * Mth.PI / 4, dx = Mth.cos(angle) * radius, dz = Mth.sin(angle) * radius;
      vertex(
          pose, out, -dx, 0, -dz, 0, -time * .25f, -Mth.sin(angle), 0, Mth.cos(angle), rgb, alpha);
      vertex(pose, out, dx, 0, dz, 1, -time * .25f, -Mth.sin(angle), 0, Mth.cos(angle), rgb, alpha);
      vertex(
          pose,
          out,
          dx,
          -length,
          dz,
          1,
          length * 2 - time * .25f,
          -Mth.sin(angle),
          0,
          Mth.cos(angle),
          rgb,
          alpha);
      vertex(
          pose,
          out,
          -dx,
          -length,
          -dz,
          0,
          length * 2 - time * .25f,
          -Mth.sin(angle),
          0,
          Mth.cos(angle),
          rgb,
          alpha);
    }
  }

  private static void ring(
      PoseStack.Pose pose,
      VertexConsumer out,
      float y,
      float radius,
      float width,
      int rgb,
      float alpha,
      float rotation) {
    for (int i = 0; i < 8; i++) {
      float a = i * Mth.PI / 4 + rotation, b = (i + 1) * Mth.PI / 4 + rotation;
      float ca = Mth.cos(a), sa = Mth.sin(a), cb = Mth.cos(b), sb = Mth.sin(b);
      vertex(pose, out, ca * (radius - width), y, sa * (radius - width), 0, 0, 0, 1, 0, rgb, alpha);
      vertex(pose, out, ca * (radius + width), y, sa * (radius + width), 1, 0, 0, 1, 0, rgb, alpha);
      vertex(pose, out, cb * (radius + width), y, sb * (radius + width), 1, 1, 0, 1, 0, rgb, alpha);
      vertex(pose, out, cb * (radius - width), y, sb * (radius - width), 0, 1, 0, 1, 0, rgb, alpha);
    }
  }

  private static int mix(int a, int b, float amount) {
    amount = Mth.clamp(amount, 0, 1);
    int r = (int) Mth.lerp(amount, (a >> 16) & 255, (b >> 16) & 255);
    int g = (int) Mth.lerp(amount, (a >> 8) & 255, (b >> 8) & 255);
    int bl = (int) Mth.lerp(amount, a & 255, b & 255);
    return r << 16 | g << 8 | bl;
  }

  private static float positiveModulo(float n, float divisor) {
    return n - divisor * Mth.floor(n / divisor);
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
      int rgb,
      float alpha) {
    out.addVertex(pose, x, y, z)
        .setColor(
            (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, (int) (255 * Mth.clamp(alpha, 0, 1)))
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(FULL_BRIGHT)
        .setNormal(pose, nx, ny, nz);
  }
}
