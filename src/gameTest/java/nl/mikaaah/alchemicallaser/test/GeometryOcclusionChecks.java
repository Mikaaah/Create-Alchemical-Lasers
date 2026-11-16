package nl.mikaaah.alchemicallaser.test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.mikaaah.alchemicallaser.client.GeometryOcclusion;
import nl.mikaaah.alchemicallaser.client.GeometryOcclusion.Box;
import nl.mikaaah.alchemicallaser.client.GeometryOcclusion.Region;

/**
 * Standalone, dependency-free regression checks; execute main with assertions always enabled here.
 */
public final class GeometryOcclusionChecks {
  private static final Set<String> OPAQUE = Set.of("north", "south", "east", "west", "up", "down");

  public static void main(String[] arguments) {
    partialCoplanarOverlayRetainsExposedArea();
    oppositeInternalJoinRemovesBothFaces();
    differentAnimatedBonesNeverOcclude();
    rotatedPoseRequiresMatchingPivot();
    translucentBoxDoesNotHideMetal();
    cutoutFacePreventsSolidOcclusion();
    clippedRegionsPreserveAllSixUvDirections();
    System.out.println("GeometryOcclusion: 7 checks passed");
  }

  private static void partialCoplanarOverlayRetainsExposedArea() {
    var result =
        GeometryOcclusion.build(
            List.of(
                box("frame", new float[] {0, 0, 0}, new float[] {4, 4, 1}),
                box("frame", new float[] {1, 1, 0}, new float[] {3, 3, .5f})));
    close(area(result.get(0).get("north")), .75f, "Partial overlay removed exposed metal");
    close(area(result.get(1).get("north")), 1, "Later coplanar detail lost priority");
    check(result.get(0).get("north").size() == 4, "Central cut must leave four disjoint strips");
    noOverlaps(result.get(0).get("north"));
  }

  private static void oppositeInternalJoinRemovesBothFaces() {
    var result =
        GeometryOcclusion.build(
            List.of(
                box("frame", new float[] {0, 0, 0}, new float[] {2, 2, 2}),
                box("frame", new float[] {2, 0, 0}, new float[] {4, 2, 2})));
    check(result.get(0).get("east").isEmpty(), "First shared face remains");
    check(result.get(1).get("west").isEmpty(), "Opposing shared face remains");
    close(area(result.get(0).get("west")), 1, "External face was clipped");
    close(area(result.get(1).get("east")), 1, "External opposing face was clipped");
  }

  private static void differentAnimatedBonesNeverOcclude() {
    var result =
        GeometryOcclusion.build(
            List.of(
                box("nozzle", new float[] {0, 0, 0}, new float[] {2, 2, 2}),
                box("claw", new float[] {0, 0, 0}, new float[] {2, 2, 2})));
    for (Map<String, List<Region>> cube : result)
      for (List<Region> regions : cube.values())
        check(regions == GeometryOcclusion.FULL, "A separately animated bone hid a face");
  }

  private static void rotatedPoseRequiresMatchingPivot() {
    float[] rotation = {0, 45, 0}, pivot = {1, 0, 1};
    Box first =
        new Box("frame", new float[] {0, 0, 0}, new float[] {2, 2, 2}, pivot, rotation, OPAQUE);
    Box matching =
        new Box("frame", new float[] {2, 0, 0}, new float[] {4, 2, 2}, pivot, rotation, OPAQUE);
    var shared = GeometryOcclusion.build(List.of(first, matching));
    check(shared.get(0).get("east").isEmpty(), "Common rotated pose did not clip its join");
    Box separate =
        new Box(
            "frame",
            new float[] {2, 0, 0},
            new float[] {4, 2, 2},
            new float[] {2, 0, 1},
            rotation,
            OPAQUE);
    var distinct = GeometryOcclusion.build(List.of(first, separate));
    close(area(distinct.get(0).get("east")), 1, "Different rotation pivots were conflated");
  }

  private static void translucentBoxDoesNotHideMetal() {
    Box metal = box("frame", new float[] {0, 0, 0}, new float[] {2, 2, 2});
    Box glass =
        new Box(
            "frame",
            new float[] {0, 0, 0},
            new float[] {2, 2, 2},
            new float[3],
            new float[3],
            Set.of());
    var result = GeometryOcclusion.build(List.of(metal, glass));
    for (Map<String, List<Region>> cube : result)
      for (List<Region> regions : cube.values())
        close(area(regions), 1, "Translucent geometry clipped an unrelated surface");
  }

  private static void cutoutFacePreventsSolidOcclusion() {
    Box metal = box("frame", new float[] {0, 0, 0}, new float[] {2, 2, 2});
    Box cutout =
        new Box(
            "frame",
            new float[] {0, 0, 0},
            new float[] {2, 2, 2},
            new float[3],
            new float[3],
            Set.of("south", "east", "west", "up", "down"));
    var result = GeometryOcclusion.build(List.of(metal, cutout));
    for (List<Region> regions : result.get(0).values())
      check(
          regions == GeometryOcclusion.FULL,
          "A cube with one texture cutout must not hide backing metal");
    check(
        result.get(1).get("north") == GeometryOcclusion.FULL,
        "Cutout face must keep its original textured quad");
  }

  private static void clippedRegionsPreserveAllSixUvDirections() {
    Box base = box("frame", new float[] {0, 0, 0}, new float[] {4, 4, 4});
    // Retain one quarter of each tested face. The cut is asymmetric so an
    // accidentally mirrored face parameter produces a different answer.
    List<Box> covers =
        List.of(
            box("frame", new float[] {0, 0, 0}, new float[] {3, 4, 1}),
            box("frame", new float[] {0, 0, 3}, new float[] {3, 4, 4}),
            box("frame", new float[] {3, 0, 0}, new float[] {4, 4, 3}),
            box("frame", new float[] {0, 0, 0}, new float[] {1, 4, 3}),
            box("frame", new float[] {0, 3, 0}, new float[] {4, 4, 3}),
            box("frame", new float[] {0, 0, 0}, new float[] {4, 1, 3}));
    String[] faces = {"north", "south", "east", "west", "up", "down"};
    boolean[] reversed = {true, false, true, false, false, true};
    for (int index = 0; index < faces.length; index++) {
      var regions =
          GeometryOcclusion.build(List.of(base, covers.get(index))).get(0).get(faces[index]);
      check(regions.size() == 1, "Asymmetric cut should leave one strip on " + faces[index]);
      Region remaining = regions.getFirst();
      close(remaining.s0(), reversed[index] ? 0 : .75f, "Wrong face s direction");
      close(remaining.s1(), reversed[index] ? .25f : 1, "Wrong mirrored face endpoint");
      close(remaining.t0(), 0, "Unexpected t clipping");
      close(remaining.t1(), 1, "Unexpected t endpoint");
      // The renderer bilinearly interpolates original UV corners with these
      // parameters. Keep both endpoints within an asymmetric source UV tile.
      for (float s : new float[] {remaining.s0(), remaining.s1()}) {
        float u = 37 + (53 - 37) * s;
        check(u >= 37 && u <= 53, "Clipped UV escaped its source tile");
      }
    }
  }

  private static Box box(String group, float[] from, float[] to) {
    return new Box(group, from, to, new float[3], new float[3], OPAQUE);
  }

  private static float area(List<Region> regions) {
    float result = 0;
    for (Region region : regions)
      result += (region.s1() - region.s0()) * (region.t1() - region.t0());
    return result;
  }

  private static void noOverlaps(List<Region> regions) {
    for (int first = 0; first < regions.size(); first++)
      for (int second = first + 1; second < regions.size(); second++) {
        Region a = regions.get(first), b = regions.get(second);
        check(
            Math.min(a.s1(), b.s1()) <= Math.max(a.s0(), b.s0())
                || Math.min(a.t1(), b.t1()) <= Math.max(a.t0(), b.t0()),
            "Clipped strips overlap");
      }
  }

  private static void close(float actual, float expected, String message) {
    check(Math.abs(actual - expected) < .00001f, message + ": " + actual + " != " + expected);
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
