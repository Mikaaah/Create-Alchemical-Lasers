package nl.mikaaah.alchemicallaser.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
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
    // Bound both vanilla chamber particles and the directly tinted spark by distance/settings.
    int divisor = distance > 24 * 24 ? 4 : distance > 12 * 12 ? 2 : 1;
    int detail = mc.options.particles().get().getId(); // ALL=0, DECREASED=1, MINIMAL=2
    divisor *= 1 << detail;
    long tick = level.getGameTime();
    if (Math.floorMod(tick + be.getBlockPos().asLong(), divisor) != 0) return;
    VisualLens lens = VisualLens.of(be.lens());
    float t = (tick % 24000);
    // The cage stays in its approved position when the lower emitter is lengthened.
    Vec3 center = Vec3.atLowerCornerOf(be.getBlockPos()).add(.5, -.21, .5);
    Vector3f lensColor = color(be.beamColor());
    // Four distinct arcs curve from the gearbox's bottom into the suspended amethyst core.
    int arc = (int) (tick & 3);
    float angle = arc * Mth.HALF_PI + t * .16f;
    float along = positiveModulo(t * .17f, 1);
    float radius = .25f * (1 - along);
    double x = center.x + Mth.cos(angle + along * 1.6f) * radius;
    double y = center.y + .21f * (1 - along) - .08f * along;
    double z = center.z + Mth.sin(angle + along * 1.6f) * radius;
    level.addParticle(
        new DustColorTransitionOptions(new Vector3f(.70f, .35f, 1), lensColor, .30f),
        x,
        y,
        z,
        0,
        -.008,
        0);
    if (Math.floorMod(tick + be.getBlockPos().asLong(), 8L * divisor) == 0)
      level.addParticle(
          ParticleTypes.END_ROD,
          center.x,
          center.y,
          center.z,
          (level.random.nextDouble() - .5) * .015,
          .005,
          (level.random.nextDouble() - .5) * .015);
    // A sparse, short-lived spark keeps the active lens visible without covering the beam.
    if (Math.floorMod(tick + be.getBlockPos().asLong(), 4L * divisor) != 0) return;
    Vec3 impact = MachineAnimation.impact(be).add(0, .025, 0);
    double angleAtImpact = level.random.nextDouble() * Math.PI * 2;
    double speed = .012 + level.random.nextDouble() * .012;
    var particle =
        mc.particleEngine.createParticle(
            LaserParticles.type(lens, Kind.SPARK),
            impact.x,
            impact.y,
            impact.z,
            Math.cos(angleAtImpact) * speed,
            .01 + level.random.nextDouble() * .01,
            Math.sin(angleAtImpact) * speed);
    if (particle != null) {
      // Recipe-specific beam tints override the default lens palette here too.
      particle.setColor(lensColor.x, lensColor.y, lensColor.z);
    }
  }

  private static Vector3f color(int rgb) {
    return new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
  }

  private static float positiveModulo(float n, float d) {
    return n - d * Mth.floor(n / d);
  }
}
