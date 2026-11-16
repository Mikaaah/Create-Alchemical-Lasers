package nl.mikaaah.alchemicallaser.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import nl.mikaaah.alchemicallaser.*;
import org.joml.Quaternionf;

/** Evaluates the supplied authoring-space keyframes, with independent per-machine clocks. */
public final class MachineAnimation {
  public static final float MUZZLE_Y = -.75f;
  public static final float BEAM_LENGTH = .29f;
  private static final Map<LaserBlockEntity, MachineAnimation> STATES = new WeakHashMap<>();
  private static Map<String, Clip> clips;
  private ItemStack lens = ItemStack.EMPTY;
  private boolean initialized, powered, firing;
  private double powerStart = -1000, lensStart = -1000, fireStart = -1000;
  private double now, spinPhase, previousSpinPhase;
  private final Map<String, float[]> stoppedRotation = new HashMap<>();
  private float energy, previousEnergy;

  public static MachineAnimation state(LaserBlockEntity be) {
    return STATES.computeIfAbsent(be, ignored -> new MachineAnimation());
  }

  public static void clear() {
    clips = null;
    STATES.clear();
  }

  public static void tick(LaserBlockEntity be) {
    state(be).advance(be);
  }

  private void advance(LaserBlockEntity be) {
    now = be.getLevel().getGameTime();
    boolean hasPower = be.hasPower() && !be.lens().isEmpty();
    boolean active = be.isBeamActive();
    if (!initialized) {
      lens = be.lens().copyWithCount(1);
      initialized = true;
    }
    if (!ItemStack.isSameItemSameComponents(lens, be.lens())) {
      lensStart = now;
      lens = be.lens().copyWithCount(1);
    }
    if (hasPower != powered) {
      for (String bone : List.of("amethyst_core", "amethyst_shards", "brass_gimbal")) {
        float[] previous = rotation(bone, 0);
        for (int i = 0; i < 3; i++) previous[i] %= 360;
        stoppedRotation.put(bone, previous);
      }
      powerStart = now;
      powered = hasPower;
      spinPhase = 0;
    }
    if (active && !firing) fireStart = now;
    firing = active;
    previousEnergy = energy;
    energy = Mth.clamp(energy + (hasPower ? .1f : -.1f), 0, 1);
    previousSpinPhase = spinPhase;
    if (powered && now - powerStart >= 12)
      spinPhase += Math.min(Math.abs(be.getSpeed()) / 60.0, 4) / 20.0;
  }

  public float charge(float partial) {
    return Mth.lerp(partial, previousEnergy, energy);
  }

  private float[] rotation(String bone, float partial) {
    double age = (now + partial - powerStart) / 20;
    float[] base = stoppedRotation.getOrDefault(bone, new float[3]);
    float[] motion;
    if (powered) {
      motion =
          age < .6
              ? sample("spin_up", bone, "rotation", age, 0)
              : sample(
                  "spin", bone, "rotation", Mth.lerp(partial, previousSpinPhase, spinPhase), 0);
    } else {
      motion =
          powerStart < -999
              ? new float[3]
              : sample("spin_down", bone, "rotation", Math.min(age, .8), 0);
    }
    add(motion, base);
    return motion;
  }

  private String firingClip(float partial) {
    return now + partial - fireStart < 6.4 ? "firing" : "firing_loop";
  }

  private double firingTime(float partial) {
    double age = (now + partial - fireStart) / 20;
    return age < .32 ? age : age - .32;
  }

  private float[] position(String bone, float partial) {
    double age = (now + partial - powerStart) / 20;
    String spin = powered ? (age < .6 ? "spin_up" : "spin") : "idle";
    float[] result =
        sample(
            spin,
            bone,
            "position",
            age < .6 ? age : Mth.lerp(partial, previousSpinPhase, spinPhase),
            0);
    if (bone.startsWith("lens_clamps_") || bone.equals("lower_nozzle")) {
      float[] socket =
          sample(
              lens.isEmpty() ? "lens_remove" : "lens_insert",
              bone,
              "position",
              (now + partial - lensStart) / 20,
              0);
      add(result, socket);
    }
    if (firing) add(result, sample(firingClip(partial), bone, "position", firingTime(partial), 0));
    return result;
  }

  public float nozzleOffset(float partial) {
    return position("lower_nozzle", partial)[1];
  }

  public static Vec3 muzzle(LaserBlockEntity be, float partial) {
    return Vec3.atLowerCornerOf(be.getBlockPos())
        .add(.5, MUZZLE_Y + state(be).nozzleOffset(partial) / 16, .5);
  }

  public MachineGeometry.BoneAnimator animator(float partial, float shaftDegrees) {
    return new MachineGeometry.BoneAnimator() {
      @Override
      public void apply(String name, float[] pivot, PoseStack pose) {
        float[] move = position(name, partial);
        float[] turn = rotation(name, partial);
        if (name.equals("bevel_gear_x")) turn = new float[] {shaftDegrees, 0, 0};
        if (name.equals("bevel_gear_y")) turn = new float[] {0, -shaftDegrees * 1.5f, 0};
        if (name.startsWith("lens_clamps_"))
          turn =
              sample(
                  lens.isEmpty() ? "lens_remove" : "lens_insert",
                  name,
                  "rotation",
                  (now + partial - lensStart) / 20,
                  0);
        if (firing)
          add(turn, sample(firingClip(partial), name, "rotation", firingTime(partial), 0));
        float[] scale =
            sample(
                firing ? firingClip(partial) : "idle",
                name,
                "scale",
                firing ? firingTime(partial) : (now + partial) / 20,
                1);
        pose.translate(move[0], move[1], move[2]);
        pose.translate(pivot[0], pivot[1], pivot[2]);
        pose.mulPose(
            new Quaternionf()
                .rotationXYZ(
                    turn[0] * Mth.DEG_TO_RAD, turn[1] * Mth.DEG_TO_RAD, turn[2] * Mth.DEG_TO_RAD));
        pose.scale(scale[0], scale[1], scale[2]);
        pose.translate(-pivot[0], -pivot[1], -pivot[2]);
      }

      @Override
      public float emissiveAlpha() {
        return .28f + .72f * charge(partial);
      }
    };
  }

  private static void add(float[] into, float[] value) {
    for (int i = 0; i < 3; i++) into[i] += value[i];
  }

  private static float[] sample(
      String clip, String bone, String channel, double seconds, float fallback) {
    if (clips == null) load();
    Clip c = clips.get(clip);
    if (c == null) return new float[] {fallback, fallback, fallback};
    var frames = c.channels.get(bone + "/" + channel);
    if (frames == null || frames.isEmpty()) return new float[] {fallback, fallback, fallback};
    double t = Math.max(0, seconds);
    if (c.loop && c.length > 0) t %= c.length;
    else t = Math.min(t, c.length);
    var lo = frames.floorEntry(t);
    var hi = frames.ceilingEntry(t);
    if (lo == null) return frames.firstEntry().getValue().clone();
    if (hi == null) return frames.lastEntry().getValue().clone();
    double blend =
        hi.getKey().equals(lo.getKey()) ? 0 : (t - lo.getKey()) / (hi.getKey() - lo.getKey());
    float[] result = new float[3];
    for (int i = 0; i < 3; i++)
      result[i] = (float) Mth.lerp(blend, lo.getValue()[i], hi.getValue()[i]);
    return result;
  }

  private static void load() {
    clips = new HashMap<>();
    try (var reader =
        new InputStreamReader(
            Minecraft.getInstance()
                .getResourceManager()
                .getResourceOrThrow(AlchemicalLaser.id("animations/machine.authoring.json"))
                .open(),
            StandardCharsets.UTF_8)) {
      JsonObject root =
          JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
      for (var entry : root.entrySet()) {
        JsonObject c = entry.getValue().getAsJsonObject();
        Map<String, NavigableMap<Double, float[]>> channels = new HashMap<>();
        for (var bone : c.getAsJsonObject("bones").entrySet())
          for (var channel : bone.getValue().getAsJsonObject().entrySet()) {
            NavigableMap<Double, float[]> frames = new TreeMap<>();
            for (var frame : channel.getValue().getAsJsonObject().entrySet()) {
              JsonArray v = frame.getValue().getAsJsonArray();
              frames.put(
                  Double.parseDouble(frame.getKey()),
                  new float[] {
                    v.get(0).getAsFloat(), v.get(1).getAsFloat(), v.get(2).getAsFloat()
                  });
            }
            channels.put(bone.getKey() + "/" + channel.getKey(), frames);
          }
        clips.put(
            entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1),
            new Clip(
                c.get("animation_length").getAsDouble(),
                c.get("loop").isJsonPrimitive()
                    && c.get("loop").getAsJsonPrimitive().isBoolean()
                    && c.get("loop").getAsBoolean(),
                channels));
      }
    } catch (Exception error) {
      LogUtils.getLogger().error("Could not load laser animation tracks", error);
    }
  }

  private record Clip(
      double length, boolean loop, Map<String, NavigableMap<Double, float[]>> channels) {}
}
