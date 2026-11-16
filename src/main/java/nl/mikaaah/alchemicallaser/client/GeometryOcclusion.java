package nl.mikaaah.alchemicallaser.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Clips hidden cuboid faces in authoring space, without changing model positions or UV density.
 * Only boxes that share the complete animated pose can hide one another.
 */
public final class GeometryOcclusion {
  private static final float PLANE_EPSILON = 0.00001f;
  public static final List<Region> FULL = List.of(new Region(0, 0, 1, 1));

  private GeometryOcclusion() {}

  public record Box(
      String group,
      float[] from,
      float[] to,
      float[] origin,
      float[] rotation,
      Set<String> opaqueFaces) {
    public Box {
      group = Objects.requireNonNull(group, "group");
      from = vector(from, "from");
      to = vector(to, "to");
      origin = vector(origin, "origin");
      rotation = vector(rotation, "rotation");
      opaqueFaces = Set.copyOf(opaqueFaces);
      for (int axis = 0; axis < 3; axis++) {
        if (to[axis] <= from[axis])
          throw new IllegalArgumentException("Occlusion boxes must have positive volume");
      }
    }
  }

  /** Quad parameters along p0-to-p1 (s) and p0-to-p3 (t), each within [0, 1]. */
  public record Region(float s0, float t0, float s1, float t1) {
    public Region {
      if (!Float.isFinite(s0)
          || !Float.isFinite(t0)
          || !Float.isFinite(s1)
          || !Float.isFinite(t1)
          || s0 < 0
          || t0 < 0
          || s1 > 1
          || t1 > 1
          || s0 >= s1
          || t0 >= t1) throw new IllegalArgumentException("Invalid face region");
    }
  }

  /** One immutable face-to-region map per input box, preserving input order. */
  public static List<Map<String, List<Region>>> build(List<Box> boxes) {
    List<Box> inputs = List.copyOf(boxes);
    int count = inputs.size();
    boolean[] solid = new boolean[count];
    for (int index = 0; index < count; index++) {
      Set<String> faces = inputs.get(index).opaqueFaces;
      solid[index] = true;
      for (Face face : Face.values()) solid[index] &= faces.contains(face.id);
    }
    List<Map<String, List<Region>>> result = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      Box box = inputs.get(index);
      Map<String, List<Region>> faces = new LinkedHashMap<>();
      for (Face face : Face.values()) {
        List<Region> visible = FULL;
        if (box.opaqueFaces.contains(face.id)) {
          for (int otherIndex = 0; otherIndex < count && !visible.isEmpty(); otherIndex++) {
            if (otherIndex == index || !solid[otherIndex]) continue;
            Box other = inputs.get(otherIndex);
            if (!samePose(box, other) || !coversPlane(box, other, face, otherIndex > index))
              continue;
            Region overlap = projectedOverlap(box, other, face);
            if (overlap != null) visible = subtract(visible, overlap);
          }
        }
        faces.put(face.id, visible);
      }
      result.add(Map.copyOf(faces));
    }
    return List.copyOf(result);
  }

  private static boolean samePose(Box first, Box second) {
    if (!first.group.equals(second.group)) return false;
    boolean rotated = false;
    for (int axis = 0; axis < 3; axis++) {
      if (first.rotation[axis] != second.rotation[axis]) return false;
      rotated |= first.rotation[axis] != 0;
    }
    if (rotated) {
      for (int axis = 0; axis < 3; axis++)
        if (first.origin[axis] != second.origin[axis]) return false;
    }
    return true;
  }

  private static boolean coversPlane(Box box, Box other, Face face, boolean later) {
    float plane = face.positive ? box.to[face.axis] : box.from[face.axis];
    float low = other.from[face.axis], high = other.to[face.axis];
    if (plane < low - PLANE_EPSILON || plane > high + PLANE_EPSILON) return false;
    // A matching outer face is drawn by the later cube. A face inside another
    // solid, or the opposing face at an internal join, is always hidden.
    float outerPlane = face.positive ? high : low;
    if (Math.abs(plane - outerPlane) <= PLANE_EPSILON) return later;
    return true;
  }

  private static Region projectedOverlap(Box box, Box other, Face face) {
    float s0 = Math.max(box.from[face.sAxis], other.from[face.sAxis]);
    float s1 = Math.min(box.to[face.sAxis], other.to[face.sAxis]);
    float t0 = Math.max(box.from[face.tAxis], other.from[face.tAxis]);
    float t1 = Math.min(box.to[face.tAxis], other.to[face.tAxis]);
    if (s1 <= s0 || t1 <= t0) return null;
    float sSpan = box.to[face.sAxis] - box.from[face.sAxis];
    float tSpan = box.to[face.tAxis] - box.from[face.tAxis];
    float startS =
        face.sPositive ? (s0 - box.from[face.sAxis]) / sSpan : (box.to[face.sAxis] - s1) / sSpan;
    float endS =
        face.sPositive ? (s1 - box.from[face.sAxis]) / sSpan : (box.to[face.sAxis] - s0) / sSpan;
    float startT = (t0 - box.from[face.tAxis]) / tSpan;
    float endT = (t1 - box.from[face.tAxis]) / tSpan;
    return new Region(clamp(startS), clamp(startT), clamp(endS), clamp(endT));
  }

  private static List<Region> subtract(List<Region> source, Region cover) {
    List<Region> remaining = new ArrayList<>();
    boolean changed = false;
    for (Region region : source) {
      float s0 = Math.max(region.s0, cover.s0), s1 = Math.min(region.s1, cover.s1);
      float t0 = Math.max(region.t0, cover.t0), t1 = Math.min(region.t1, cover.t1);
      if (s1 <= s0 || t1 <= t0) {
        remaining.add(region);
        continue;
      }
      changed = true;
      // The outer strips span the original height. The two central strips
      // occupy only the cut's width, so no resulting rectangles overlap.
      add(remaining, region.s0, region.t0, s0, region.t1);
      add(remaining, s1, region.t0, region.s1, region.t1);
      add(remaining, s0, region.t0, s1, t0);
      add(remaining, s0, t1, s1, region.t1);
    }
    return changed ? List.copyOf(remaining) : source;
  }

  private static void add(List<Region> regions, float s0, float t0, float s1, float t1) {
    if (s1 > s0 && t1 > t0) regions.add(new Region(s0, t0, s1, t1));
  }

  private static float clamp(float value) {
    return Math.max(0, Math.min(1, value));
  }

  private static float[] vector(float[] value, String name) {
    if (value == null || value.length != 3)
      throw new IllegalArgumentException(name + " must contain three coordinates");
    float[] result = value.clone();
    for (float coordinate : result)
      if (!Float.isFinite(coordinate)) throw new IllegalArgumentException(name + " must be finite");
    return result;
  }

  private enum Face {
    NORTH("north", 2, false, 0, false, 1),
    SOUTH("south", 2, true, 0, true, 1),
    EAST("east", 0, true, 2, false, 1),
    WEST("west", 0, false, 2, true, 1),
    UP("up", 1, true, 2, true, 0),
    DOWN("down", 1, false, 2, false, 0);

    final String id;
    final int axis, sAxis, tAxis;
    final boolean positive, sPositive;

    Face(String id, int axis, boolean positive, int sAxis, boolean sPositive, int tAxis) {
      this.id = id;
      this.axis = axis;
      this.positive = positive;
      this.sAxis = sAxis;
      this.sPositive = sPositive;
      this.tAxis = tAxis;
    }
  }
}
