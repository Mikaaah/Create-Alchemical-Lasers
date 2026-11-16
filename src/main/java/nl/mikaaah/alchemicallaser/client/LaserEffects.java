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
    boolean completing = be.phase == LaserBlockEntity.Phase.COMPLETING;
    if (!be.isBeamActive() && !charging && !completing) return;
    if (be.isBeamActive()) return;
    if (level.getGameTime() % (completing ? 2 : 3) != 0) return;
    int rgb = be.beamColor();
    var dust =
        new DustParticleOptions(
            new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f),
            .65f);
    double x = pos.getX() + .5, y = pos.getY(), z = pos.getZ() + .5;
    if (charging) {
      y -= .70;
      for (int i = 0; i < 2; i++) {
        double a = level.random.nextDouble() * Math.PI * 2,
            dx = Math.cos(a) * .35,
            dz = Math.sin(a) * .35;
        level.addParticle(ParticleTypes.ENCHANT, x + dx, y + .15, z + dz, -dx * .8, 0, -dz * .8);
        level.addParticle(dust, x + dx * .5, y + .18, z + dz * .5, 0, .01, 0);
      }
    } else {
      for (int i = 0; i < (completing ? 5 : 2); i++)
        level.addParticle(
            dust,
            x + (level.random.nextDouble() - .5) * .2,
            y - 1.02,
            z + (level.random.nextDouble() - .5) * .2,
            0,
            .025,
            0);
      level.addParticle(
          ParticleTypes.ELECTRIC_SPARK,
          x,
          y - 1.02,
          z,
          (level.random.nextDouble() - .5) * .06,
          .04,
          (level.random.nextDouble() - .5) * .06);
    }
  }

  public static void complete(LaserBlockEntity be) {
    var level = be.getLevel();
    var p = be.getBlockPos();
    int rgb = be.beamColor();
    var dust =
        new DustParticleOptions(
            new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f),
            .9f);
    for (int i = 0; i < 8; i++) {
      double a = i * Math.PI / 4, dx = Math.cos(a), dz = Math.sin(a);
      level.addParticle(
          dust,
          p.getX() + .5 + dx * .12,
          p.getY() - 1.02,
          p.getZ() + .5 + dz * .12,
          dx * .04,
          .04,
          dz * .04);
    }
    level.addParticle(
        ParticleTypes.END_ROD, p.getX() + .5, p.getY() - 1, p.getZ() + .5, 0, .025, 0);
  }

  /** One local vent puff on the firing-to-stopped edge, never per render frame. */
  private static void discharge(LaserBlockEntity be) {
    var level = be.getLevel();
    var mc = Minecraft.getInstance();
    if (level == null || mc.player == null) return;
    var muzzle = MachineAnimation.muzzle(be, 1);
    if (mc.player.distanceToSqr(muzzle.x, muzzle.y, muzzle.z) > 48 * 48) return;
    for (int i = 0; i < 12; i++) {
      double angle = i * Math.PI / 6 + level.random.nextDouble() * .18;
      double radius = .14 + level.random.nextDouble() * .07;
      double dx = Math.cos(angle), dz = Math.sin(angle);
      level.addParticle(
          ParticleTypes.SMOKE,
          muzzle.x + dx * radius,
          muzzle.y + .20,
          muzzle.z + dz * radius,
          dx * .025,
          .018 + level.random.nextDouble() * .015,
          dz * .025);
    }
    for (int i = 0; i < 2; i++)
      level.addParticle(
          ParticleTypes.LARGE_SMOKE,
          muzzle.x + (level.random.nextDouble() - .5) * .12,
          muzzle.y + .17,
          muzzle.z + (level.random.nextDouble() - .5) * .12,
          0,
          .025,
          0);
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
