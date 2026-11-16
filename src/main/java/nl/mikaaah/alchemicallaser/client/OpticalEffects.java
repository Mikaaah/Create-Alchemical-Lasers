package nl.mikaaah.alchemicallaser.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import nl.mikaaah.alchemicallaser.LaserBlockEntity;
import nl.mikaaah.alchemicallaser.particle.LaserParticles;
import nl.mikaaah.alchemicallaser.particle.LaserParticles.Kind;
import org.joml.Vector3f;

/**
 * Called once by the BE client tick. Rings are geometry; only short-lived motes use the particle
 * engine.
 */
public final class OpticalEffects {
  private OpticalEffects() {}

  public static void tick(LaserBlockEntity be) {
    if (!(be.getLevel() instanceof ClientLevel level)
        || !be.isBeamActive()
        || VisualLens.of(be.lens()) == null) return;
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return;
    Vec3 muzzle = MachineAnimation.muzzle(be, 1);
    double distance = mc.player.distanceToSqr(muzzle.x, muzzle.y, muzzle.z);
    if (distance > 48 * 48) return;
    // Engine honours each SimpleParticleType's distance/settings flag; this budget further limits
    // large factories.
    int divisor = distance > 24 * 24 ? 4 : distance > 12 * 12 ? 2 : 1;
    int detail = mc.options.particles().get().getId(); // ALL=0, DECREASED=1, MINIMAL=2
    divisor *= 1 << detail;
    long tick = level.getGameTime();
    if (Math.floorMod(tick + be.getBlockPos().asLong(), divisor) != 0) return;
    VisualLens lens = VisualLens.of(be.lens());
    float t = (tick % 24000);
    Vec3 center = MachineAnimation.muzzle(be, 0).add(0, .54, 0);
    Vector3f lensColor = color(be.beamColor());
    // Four distinct arcs curve from the gearbox's bottom into the suspended amethyst core.
    int arc = (int) (tick & 3);
    float angle = arc * Mth.HALF_PI + t * .16f;
    for (int j = 0; j < 2; j++) {
      float along = positiveModulo(t * .17f + j * .5f, 1);
      float radius = .25f * (1 - along);
      double x = center.x + Mth.cos(angle + along * 1.6f) * radius;
      double y = center.y + .21f * (1 - along) - .08f * along;
      double z = center.z + Mth.sin(angle + along * 1.6f) * radius;
      level.addParticle(
          new DustColorTransitionOptions(new Vector3f(.70f, .35f, 1), lensColor, .40f),
          x,
          y,
          z,
          0,
          -.008,
          0);
    }
    if (tick % 4 == 0)
      level.addParticle(
          ParticleTypes.END_ROD,
          center.x,
          center.y,
          center.z,
          (level.random.nextDouble() - .5) * .015,
          .005,
          (level.random.nextDouble() - .5) * .015);
    // Mote velocities encode their initial orbit radius; the provider turns them into a real
    // spiral.
    float a = t * .45f;
    double rx = Mth.cos(a) * .18, rz = Mth.sin(a) * .18;
    spawn(level, lens, Kind.MOTE, muzzle.add(rx, -.015, rz), rx, -.025, rz);
    if (tick % 3 == 0) {
      spawn(level, lens, Kind.SPARK, muzzle, rx * .20, -.045, rz * .20);
      level.addParticle(
          new DustParticleOptions(lensColor, .5f),
          muzzle.x + rx * .5,
          muzzle.y - .025,
          muzzle.z + rz * .5,
          rx * .05,
          -.03,
          rz * .05);
    }
    if (lens == VisualLens.IGNIS && tick % 5 == 0)
      spawn(level, lens, Kind.SMOKE, muzzle.add(rx, .02, rz), rx * .025, .025, rz * .025);
    float length = LaserBeamRenderer.boundedLength(MachineAnimation.BEAM_LENGTH);
    if (!true || length < .001f) return;
    Vec3 impact =
        Vec3.atLowerCornerOf(be.getBlockPos())
            .add(.5, MachineAnimation.MUZZLE_Y - length + .025, .5);
    double theta = level.random.nextDouble() * Math.PI * 2;
    double speed = .035 + level.random.nextDouble() * .065;
    // Full collision-based embers: hot globules, ice chips, violet runes or green sculk flecks per
    // lens.
    spawn(
        level,
        lens,
        Kind.EMBER,
        impact,
        Math.cos(theta) * speed,
        .05 + level.random.nextDouble() * .08,
        Math.sin(theta) * speed);
    if (tick % 3 == 0)
      spawn(
          level,
          lens,
          Kind.SMOKE,
          impact.add(
              (level.random.nextDouble() - .5) * .15, 0, (level.random.nextDouble() - .5) * .15),
          0,
          lens == VisualLens.GLACIES ? .018 : .035,
          0);
    if (tick % 4 == 0) {
      ParticleOptions secondary =
          switch (lens) {
            case IGNIS -> ParticleTypes.SMALL_FLAME;
            case GLACIES -> ParticleTypes.SNOWFLAKE;
            case AETHER -> new DustColorTransitionOptions(color(0xEB5BFF), color(0x7751FF), .6f);
            case VOID -> ParticleTypes.SCULK_SOUL;
            default -> ParticleTypes.ELECTRIC_SPARK;
          };
      level.addParticle(
          secondary,
          impact.x,
          impact.y + .02,
          impact.z,
          Math.cos(theta) * .03,
          .025,
          Math.sin(theta) * .03);
    }
    if (tick % 6 == 0) {
      // Crisp radial points reinforce the geometry shockwave without creating a haze cloud.
      for (int i = 0; i < 4; i++) {
        double s = i * Math.PI / 2 + t * .1;
        spawn(
            level,
            lens,
            Kind.SPARK,
            impact.add(Math.cos(s) * .12, .01, Math.sin(s) * .12),
            Math.cos(s) * .07,
            .008,
            Math.sin(s) * .07);
      }
    }
  }

  private static void spawn(
      ClientLevel level, VisualLens lens, Kind kind, Vec3 pos, double dx, double dy, double dz) {
    level.addParticle(LaserParticles.type(lens, kind), pos.x, pos.y, pos.z, dx, dy, dz);
  }

  private static Vector3f color(int rgb) {
    return new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
  }

  private static float positiveModulo(float n, float d) {
    return n - d * Mth.floor(n / d);
  }
}
