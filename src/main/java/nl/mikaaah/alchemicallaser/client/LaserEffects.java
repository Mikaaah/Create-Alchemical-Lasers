package nl.mikaaah.alchemicallaser.client;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import nl.mikaaah.alchemicallaser.*;
import org.joml.Vector3f;

public final class LaserEffects {
  private static final Map<LaserBlockEntity, Boolean> FIRING = new WeakHashMap<>();
  private static final Map<LaserBlockEntity, Boolean> CHARGING = new WeakHashMap<>();
  private static final Map<LaserBlockEntity, WeakReference<ChargeSound>> CHARGE_SOUNDS =
      new WeakHashMap<>();

  public static void tick(LaserBlockEntity be) {
    MachineAnimation.tick(be);
    OpticalEffects.tick(be);
    var level = be.getLevel();
    var pos = be.getBlockPos();
    var mc = Minecraft.getInstance();
    boolean active = be.isBeamActive();
    boolean previouslyActive = Boolean.TRUE.equals(FIRING.put(be, active));
    if (previouslyActive && !active) discharge(be);
    boolean charging = be.phase == LaserBlockEntity.Phase.CHARGING;
    boolean previouslyCharging = Boolean.TRUE.equals(CHARGING.put(be, charging));
    if (mc.player == null
        || mc.player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 4096)
      return;
    if (!be.isVirtual() && charging && !previouslyCharging) {
      WeakReference<ChargeSound> previousRef = CHARGE_SOUNDS.remove(be);
      ChargeSound previous = previousRef == null ? null : previousRef.get();
      if (previous != null) previous.finish();
      var chargeSound = new ChargeSound(be);
      CHARGE_SOUNDS.put(be, new WeakReference<>(chargeSound));
      mc.getSoundManager().play(chargeSound);
    }
    if (!be.isVirtual()
        && be.isBeamActive()
        && !(be.getSoundInstance() instanceof BeamSound sound && !sound.isStopped())) {
      var sound = new BeamSound(be);
      be.setSoundInstance(sound);
      mc.getSoundManager().play(sound);
    }
    if (!charging || level.getGameTime() % 3 != 0) return;
    int rgb = be.beamColor();
    var dust =
        new DustParticleOptions(
            new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f),
            .65f);
    double x = pos.getX() + .5, y = pos.getY(), z = pos.getZ() + .5;
    y -= .70;
    for (int i = 0; i < 2; i++) {
      double a = level.random.nextDouble() * Math.PI * 2,
          dx = Math.cos(a) * .35,
          dz = Math.sin(a) * .35;
      level.addParticle(ParticleTypes.ENCHANT, x + dx, y + .15, z + dz, -dx * .8, 0, -dz * .8);
      level.addParticle(dust, x + dx * .5, y + .18, z + dz * .5, 0, .01, 0);
    }
  }

  public static void complete(LaserBlockEntity be) {
    var level = be.getLevel();
    int rgb = be.beamColor();
    var dust =
        new DustParticleOptions(
            new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f),
            .35f);
    var impact = MachineAnimation.impact(be);
    for (int i = 0; i < 4; i++) {
      double a = i * Math.PI / 2, dx = Math.cos(a), dz = Math.sin(a);
      level.addParticle(
          dust,
          impact.x + dx * .07,
          impact.y + .025,
          impact.z + dz * .07,
          dx * .015,
          .02,
          dz * .015);
    }
  }

  /** Tiny cooling plumes at both ends, once on the firing-to-stopped edge. */
  private static void discharge(LaserBlockEntity be) {
    var level = be.getLevel();
    var mc = Minecraft.getInstance();
    if (level == null || mc.player == null) return;
    var muzzle = MachineAnimation.muzzle(be, 1);
    if (mc.player.distanceToSqr(muzzle.x, muzzle.y, muzzle.z) > 48 * 48) return;
    int count = Math.max(0, 2 - mc.options.particles().get().getId());
    for (var point :
        new net.minecraft.world.phys.Vec3[] {
          muzzle.add(0, -.01, 0), MachineAnimation.impact(be).add(0, .025, 0)
        })
      for (int i = 0; i < count; i++) {
        double dx = (level.random.nextDouble() - .5) * .002;
        double dz = (level.random.nextDouble() - .5) * .002;
        double dy = .02 + level.random.nextDouble() * .02;
        var particle =
            mc.particleEngine.createParticle(
                ParticleTypes.SMOKE,
                point.x + (level.random.nextDouble() - .5) * .04,
                point.y,
                point.z + (level.random.nextDouble() - .5) * .04,
                dx,
                dy,
                dz);
        if (particle != null) {
          particle.scale(.28f);
          particle.setLifetime(10 + level.random.nextInt(6));
          // Vanilla smoke randomizes its initial velocity; restore the requested gentle rise.
          particle.setParticleSpeed(dx, dy, dz);
        }
      }
  }

  /** A stoppable vanilla-anchor charge; the long reverb cannot pile up between cycles. */
  private static final class ChargeSound extends AbstractTickableSoundInstance {
    private final LaserBlockEntity be;
    private int releaseTicks;

    ChargeSound(LaserBlockEntity be) {
      super(AlchemicalLaser.AUDIO.get("charge").get(), SoundSource.BLOCKS, RandomSource.create());
      this.be = be;
      looping = false;
      delay = 0;
      relative = false;
      attenuation = SoundInstance.Attenuation.LINEAR;
      x = be.getBlockPos().getX() + .5;
      y = be.getBlockPos().getY() - .5;
      z = be.getBlockPos().getZ() + .5;
      volume = .45f * LaserConfig.SOUND_VOLUME.get().floatValue();
      pitch = be.lensPitch();
    }

    @Override
    public void tick() {
      var mc = Minecraft.getInstance();
      if (be.isRemoved()
          || be.getLevel() == null
          || be.getLevel() != mc.level
          || mc.player == null
          || !be.getLevel().hasChunkAt(be.getBlockPos())
          || mc.player.distanceToSqr(x, y, z) > 4096) {
        finish();
        return;
      }
      if (be.phase != LaserBlockEntity.Phase.CHARGING) {
        if (++releaseTicks >= 2) {
          finish();
          return;
        }
        volume = .12f * LaserConfig.SOUND_VOLUME.get().floatValue();
      } else {
        releaseTicks = 0;
        volume = .45f * LaserConfig.SOUND_VOLUME.get().floatValue();
      }
      pitch = be.lensPitch();
    }

    private void finish() {
      stop();
      WeakReference<ChargeSound> reference = CHARGE_SOUNDS.get(be);
      if (reference != null && reference.get() == this) CHARGE_SOUNDS.remove(be);
    }

    @Override
    public boolean canStartSilent() {
      return true;
    }
  }

  private static final class BeamSound extends AbstractTickableSoundInstance {
    private final LaserBlockEntity be;
    private float envelope;

    BeamSound(LaserBlockEntity be) {
      super(AlchemicalLaser.AUDIO.get("beam").get(), SoundSource.BLOCKS, RandomSource.create());
      this.be = be;
      looping = true;
      delay = 0;
      relative = false;
      attenuation = SoundInstance.Attenuation.LINEAR;
      x = be.getBlockPos().getX() + .5;
      y = be.getBlockPos().getY() + .4;
      z = be.getBlockPos().getZ() + .5;
      volume = 0;
      pitch = beamPitch();
    }

    @Override
    public void tick() {
      var mc = Minecraft.getInstance();
      if (be.isRemoved()
          || be.getLevel() == null
          || be.getLevel() != mc.level
          || mc.player == null
          || !be.getLevel().hasChunkAt(be.getBlockPos())
          || mc.player.distanceToSqr(x, y, z) > 4096) {
        finish();
        return;
      }
      envelope = be.isBeamActive() ? Math.min(1, envelope + .25f) : Math.max(0, envelope - .25f);
      if (envelope == 0 && !be.isBeamActive()) {
        finish();
        return;
      }
      volume = .30f * envelope * LaserConfig.SOUND_VOLUME.get().floatValue();
      pitch = beamPitch();
    }

    private float beamPitch() {
      float lens = Math.clamp(be.lensPitch(), .8f, 1.15f);
      return lens * (.88f + Math.min(Math.abs(be.getSpeed()), 256) / 2048f);
    }

    private void finish() {
      stop();
      if (be.getSoundInstance() == this) be.setSoundInstance(null);
    }

    @Override
    public boolean canStartSilent() {
      return true;
    }
  }
}
