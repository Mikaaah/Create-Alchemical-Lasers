package nl.mikaaah.creationaugmenter.client;

import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import nl.mikaaah.creationaugmenter.*;
import org.joml.Vector3f;

/** Client tick lifecycle; rendering only discovers machines and never emits particles. */
public final class CreationEffects {
  private static final Map<CreationAugmenterBlockEntity, Entry> TRACKED = new WeakHashMap<>();
  private static ClientLevel previousLevel;
  private static long previousTick = Long.MIN_VALUE;

  private CreationEffects() {}

  public static void track(CreationAugmenterBlockEntity be) {
    if (be.getLevel() != Minecraft.getInstance().level) return;
    TRACKED.computeIfAbsent(be, key -> new Entry(be.isBeamActive()));
    CreationAnimation.tick(be);
  }

  public static void clear() {
    for (Entry entry : TRACKED.values()) {
      var sound = entry.sound.get();
      if (sound != null) sound.finish();
    }
    TRACKED.clear();
    previousTick = Long.MIN_VALUE;
  }

  public static void tick(ClientTickEvent.Post event) {
    var mc = Minecraft.getInstance();
    var level = mc.level;
    if (level != previousLevel) {
      clear();
      CreationAnimation.clear();
      previousLevel = level;
    }
    if (level == null || mc.player == null || previousTick == level.getGameTime()) return;
    previousTick = level.getGameTime();
    var iterator = TRACKED.entrySet().iterator();
    while (iterator.hasNext()) {
      var tracked = iterator.next();
      var be = tracked.getKey();
      var entry = tracked.getValue();
      var pos = be.getBlockPos();
      if (be.isRemoved()
          || be.getLevel() != level
          || !level.hasChunkAt(pos)
          || mc.player.distanceToSqr(pos.getX() + .5, pos.getY(), pos.getZ() + .5) > 4096) {
        var sound = entry.sound.get();
        if (sound != null) sound.finish();
        iterator.remove();
        continue;
      }
      CreationAnimation.tick(be);
      boolean active = be.isBeamActive();
      if (entry.firing && !active) discharge(be);
      entry.firing = active;
      var sound = entry.sound.get();
      if (!be.isVirtual() && active && (sound == null || sound.isStopped())) {
        sound = new BeamSound(be);
        entry.sound = new WeakReference<>(sound);
        mc.getSoundManager().play(sound);
      }
      particles(be, level);
    }
  }

  private static void particles(CreationAugmenterBlockEntity be, ClientLevel level) {
    if (be.lensKind() == null
        || (!be.isBeamActive() && be.getPhase() != CreationAugmenterBlockEntity.Phase.CHARGING))
      return;
    var mc = Minecraft.getInstance();
    Vec3 core = Vec3.atLowerCornerOf(be.getBlockPos()).add(.5, -3.5 / 16, .5);
    double distance = mc.player.distanceToSqr(core);
    if (distance > 48 * 48) return;
    int divisor =
        (distance > 24 * 24 ? 4 : distance > 12 * 12 ? 2 : 1)
            << mc.options.particles().get().getId();
    long tick = level.getGameTime();
    long clock = tick + be.getBlockPos().asLong();
    int coreColor = CoreSpectrum.color(tick, 0);
    Vector3f from = color(coreColor), to = color(be.lensKind().color());
    if (Math.floorMod(clock, 4L * divisor) == 0) {
      double a = (tick % 400) * .24;
      double dx = Math.cos(a) * .19, dz = Math.sin(a) * .19;
      level.addParticle(
          new DustColorTransitionOptions(from, to, .25f),
          core.x + dx,
          core.y + .10,
          core.z + dz,
          -dx * .025,
          -.005,
          -dz * .025);
    }
    if (Math.floorMod(clock, 8L * divisor) == 0) {
      double a = (tick % 400) * .31;
      double dx = Math.cos(a) * .25, dz = Math.sin(a) * .25;
      level.addParticle(
          ParticleTypes.ENCHANT,
          core.x + dx,
          core.y + .12,
          core.z + dz,
          -dx * .55,
          -.03,
          -dz * .55);
    }
    if (!be.isBeamActive()) return;
    Vec3 impact = be.getTargetPosition().add(0, .025, 0);
    if (Math.floorMod(clock, 4L * divisor) == 0) {
      double a = level.random.nextDouble() * Math.PI * 2;
      double dx = Math.cos(a) * .12, dz = Math.sin(a) * .12;
      level.addParticle(
          new DustColorTransitionOptions(from, to, .30f),
          impact.x + dx,
          impact.y,
          impact.z + dz,
          dx * .04,
          .015 + level.random.nextDouble() * .015,
          dz * .04);
    }
    if (Math.floorMod(clock, 12L * divisor) == 0)
      level.addParticle(
          ParticleTypes.ENCHANT,
          impact.x,
          impact.y + .08,
          impact.z,
          (level.random.nextDouble() - .5) * .15,
          .08,
          (level.random.nextDouble() - .5) * .15);
  }

  /** Four wisps at most: two at the animated muzzle, two at the equipment. */
  private static void discharge(CreationAugmenterBlockEntity be) {
    var mc = Minecraft.getInstance();
    var level = be.getLevel();
    if (level == null
        || mc.player == null
        || mc.player.distanceToSqr(Vec3.atCenterOf(be.getBlockPos())) > 48 * 48) return;
    int count = Math.max(0, 2 - mc.options.particles().get().getId());
    for (Vec3 point :
        new Vec3[] {CreationAnimation.muzzle(be, 0), be.getTargetPosition().add(0, .025, 0)})
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
          particle.setParticleSpeed(dx, dy, dz);
        }
      }
  }

  private static Vector3f color(int rgb) {
    return new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
  }

  private static final class Entry {
    boolean firing;
    WeakReference<BeamSound> sound = new WeakReference<>(null);

    Entry(boolean firing) {
      this.firing = firing;
    }
  }

  private static final class BeamSound extends AbstractTickableSoundInstance {
    private final CreationAugmenterBlockEntity be;
    private float envelope;

    BeamSound(CreationAugmenterBlockEntity be) {
      super(CreationAugmenter.AUDIO.get("beam").get(), SoundSource.BLOCKS, RandomSource.create());
      this.be = be;
      looping = true;
      delay = 0;
      relative = false;
      attenuation = SoundInstance.Attenuation.LINEAR;
      x = be.getBlockPos().getX() + .5;
      y = be.getBlockPos().getY() - 15.0 / 16;
      z = be.getBlockPos().getZ() + .5;
      volume = .001f;
      pitch = .95f;
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
      envelope = Mth.clamp(envelope + (be.isBeamActive() ? .25f : -.25f), 0, 1);
      volume = .28f * envelope;
      pitch = .90f + Math.min(Math.abs(be.getSpeed()), 256) / 2048f;
      y = CreationAnimation.muzzle(be, 0).y;
      if (!be.isBeamActive() && envelope <= 0) finish();
    }

    private void finish() {
      stop();
    }

    @Override
    public boolean canStartSilent() {
      return true;
    }
  }
}
