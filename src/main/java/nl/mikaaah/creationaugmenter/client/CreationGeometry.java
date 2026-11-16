package nl.mikaaah.creationaugmenter.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.slf4j.Logger;

/**
 * Resource-reloadable cuboid geometry. Coordinates and pivots are Blockbench pixels, Y-up; face UV
 * rectangles are texture pixels, with explicit per-face atlas ids. No baked Minecraft block
 * geometry or borrowed machine model is involved.
 */
public final class CreationGeometry {
  private static final Gson GSON = new Gson();
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Map<ResourceLocation, CreationGeometry> CACHE = new LinkedHashMap<>();
  private static final CreationGeometry EMPTY = new CreationGeometry(Map.of(), Map.of(), List.of());
  private final Map<String, Bone> bones;
  private final Map<ResourceLocation, Texture> textures;
  private final List<Cube> cubes;

  private CreationGeometry(
      Map<String, Bone> bones, Map<ResourceLocation, Texture> textures, List<Cube> cubes) {
    this.bones = bones;
    this.textures = textures;
    this.cubes = cubes;
  }

  public static void clear() {
    CACHE.clear();
  }

  public static CreationGeometry get(ResourceLocation resource) {
    return CACHE.computeIfAbsent(resource, CreationGeometry::load);
  }

  private static CreationGeometry load(ResourceLocation resource) {
    try (var reader =
        new InputStreamReader(
            Minecraft.getInstance().getResourceManager().getResourceOrThrow(resource).open(),
            StandardCharsets.UTF_8)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      Map<String, Bone> bones = new LinkedHashMap<>();
      for (JsonElement element : root.getAsJsonArray("bones")) {
        JsonObject b = element.getAsJsonObject();
        String name = b.get("name").getAsString();
        String parent =
            b.has("parent") && !b.get("parent").isJsonNull() ? b.get("parent").getAsString() : "";
        bones.put(name, new Bone(name, parent, vec(b, "origin"), vec(b, "rotation")));
      }
      Map<ResourceLocation, Texture> textures = new LinkedHashMap<>();
      for (var entry : root.getAsJsonObject("textures").entrySet()) {
        ResourceLocation id = ResourceLocation.parse(entry.getKey());
        int width = 16, height = 16;
        JsonElement data = entry.getValue();
        if (data.isJsonArray()) {
          width = data.getAsJsonArray().get(0).getAsInt();
          height = data.getAsJsonArray().get(1).getAsInt();
        } else if (data.isJsonObject()) {
          width = data.getAsJsonObject().get("width").getAsInt();
          height = data.getAsJsonObject().get("height").getAsInt();
        }
        ResourceLocation mask = id.withPath(id.getPath().replace(".png", "_e.png"));
        boolean hasMask =
            Minecraft.getInstance().getResourceManager().getResource(mask).isPresent();
        textures.put(id, new Texture(width, height, hasMask ? mask : null));
      }
      List<Cube> cubes = new ArrayList<>();
      for (JsonElement element : root.getAsJsonArray("cubes")) {
        JsonObject c = element.getAsJsonObject();
        String group = c.has("group") ? c.get("group").getAsString() : "gearbox";
        Map<String, Face> faces = new LinkedHashMap<>();
        for (var entry : c.getAsJsonObject("faces").entrySet()) {
          JsonObject f = entry.getValue().getAsJsonObject();
          ResourceLocation texture = ResourceLocation.parse(f.get("texture").getAsString());
          String material = f.has("material") ? f.get("material").getAsString() : "";
          boolean glass =
              bool(f, "translucent")
                  || material.contains("tinted_glass")
                  || material.contains("blast_glass");
          JsonArray uv = f.getAsJsonArray("uv");
          // Aero assets stay in their owner mod. Prefer them only when the
          // resource pack stack supplies them, otherwise use our original atlas.
          if (f.has("preferred_texture")) {
            ResourceLocation preferred =
                ResourceLocation.parse(f.get("preferred_texture").getAsString());
            if (Minecraft.getInstance().getResourceManager().getResource(preferred).isPresent()) {
              texture = preferred;
              uv = f.getAsJsonArray("preferred_uv");
              JsonArray size = f.getAsJsonArray("preferred_size");
              int width = size == null ? 16 : size.get(0).getAsInt();
              int height = size == null ? 16 : size.get(1).getAsInt();
              ResourceLocation mask =
                  preferred.withPath(preferred.getPath().replace(".png", "_e.png"));
              boolean hasMask =
                  Minecraft.getInstance().getResourceManager().getResource(mask).isPresent();
              textures.putIfAbsent(preferred, new Texture(width, height, hasMask ? mask : null));
            }
          }
          faces.put(
              entry.getKey(),
              new Face(
                  texture,
                  new float[] {
                    uv.get(0).getAsFloat(),
                    uv.get(1).getAsFloat(),
                    uv.get(2).getAsFloat(),
                    uv.get(3).getAsFloat()
                  },
                  glass,
                  f.has("rotation") ? f.get("rotation").getAsInt() : 0,
                  f.has("alpha") ? f.get("alpha").getAsFloat() : 1f,
                  bool(f, "emissive"),
                  bool(f, "animated_core"),
                  material.contains("transmutation/faceted_crystal")));
        }
        cubes.add(
            new Cube(
                group, vec(c, "from"), vec(c, "to"), vec(c, "origin"), vec(c, "rotation"), faces));
      }
      return new CreationGeometry(bones, textures, cubes);
    } catch (Exception error) {
      LOGGER.error("Could not load Creation Augmenter geometry {}", resource, error);
      return EMPTY;
    }
  }

  public void render(
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay,
      BoneAnimator animator,
      CreationGeometry inheritedBones,
      boolean skipShaft) {
    Map<String, Bone> hierarchy = new LinkedHashMap<>();
    if (inheritedBones != null) hierarchy.putAll(inheritedBones.bones);
    hierarchy.putAll(bones);
    // Render opaque shells before blast shields. Minecraft sorts translucent buffers
    // after opaque geometry; the particle/beam pass remains independently additive.
    for (int pass = 0; pass < 3; pass++) {
      for (Cube cube : cubes) {
        if (skipShaft && cube.group.equals("side_shaft")) continue;
        pose.pushPose();
        applyBone(cube.group, hierarchy, pose, animator, 0);
        rotateAt(pose, cube.origin, cube.rotation);
        for (var entry : cube.faces.entrySet()) {
          Face face = entry.getValue();
          Texture texture = textures.get(face.texture);
          if (face.animatedCore) {
            face =
                new Face(
                    CoreSpectrum.texture(),
                    face.uv,
                    face.translucent,
                    face.rotation,
                    face.alpha,
                    face.emissive,
                    true,
                    face.iridescent);
            texture = new Texture(16, 16, CoreSpectrum.emissiveTexture());
          }
          if (texture == null) continue;
          boolean emissive = pass == 2;
          if (emissive && (texture.emissive == null || !face.emissive)) continue;
          if (!emissive && face.translucent != (pass == 1)) continue;
          RenderType type =
              emissive
                  ? RenderType.entityTranslucentEmissive(texture.emissive)
                  : face.translucent
                      ? RenderType.entityTranslucent(face.texture)
                      : RenderType.entityCutoutNoCull(face.texture);
          renderFace(
              cube,
              entry.getKey(),
              face,
              texture,
              pose.last(),
              buffers.getBuffer(type),
              emissive ? LightTexture.FULL_BRIGHT : light,
              overlay,
              emissive ? animator.emissiveAlpha() : face.alpha);
        }
        pose.popPose();
      }
    }
  }

  private static void applyBone(
      String name, Map<String, Bone> bones, PoseStack pose, BoneAnimator animator, int depth) {
    if (depth > 32 || name == null || name.isBlank()) return;
    Bone bone = bones.get(name);
    if (bone == null) return;
    applyBone(bone.parent, bones, pose, animator, depth + 1);
    rotateAt(pose, bone.origin, bone.rotation);
    animator.apply(name, bone.origin, pose);
  }

  private static void rotateAt(PoseStack pose, float[] origin, float[] rotation) {
    if (rotation[0] == 0 && rotation[1] == 0 && rotation[2] == 0) return;
    pose.translate(origin[0], origin[1], origin[2]);
    pose.mulPose(
        new Quaternionf()
            .rotationXYZ(
                (float) Math.toRadians(rotation[0]),
                (float) Math.toRadians(rotation[1]),
                (float) Math.toRadians(rotation[2])));
    pose.translate(-origin[0], -origin[1], -origin[2]);
  }

  private static void renderFace(
      Cube cube,
      String direction,
      Face face,
      Texture texture,
      PoseStack.Pose pose,
      VertexConsumer out,
      int light,
      int overlay,
      float alpha) {
    float x0 = cube.from[0], y0 = cube.from[1], z0 = cube.from[2];
    float x1 = cube.to[0], y1 = cube.to[1], z1 = cube.to[2];
    float[][] p;
    float nx = 0, ny = 0, nz = 0;
    switch (direction) {
      case "north" -> {
        p = new float[][] {{x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}};
        nz = -1;
      }
      case "south" -> {
        p = new float[][] {{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
        nz = 1;
      }
      case "east" -> {
        p = new float[][] {{x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}};
        nx = 1;
      }
      case "west" -> {
        p = new float[][] {{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}};
        nx = -1;
      }
      case "up" -> {
        p = new float[][] {{x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}};
        ny = 1;
      }
      case "down" -> {
        p = new float[][] {{x0, y0, z1}, {x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}};
        ny = -1;
      }
      default -> {
        return;
      }
    }
    float u0 = face.uv[0] / texture.width, v0 = face.uv[1] / texture.height;
    float u1 = face.uv[2] / texture.width, v1 = face.uv[3] / texture.height;
    float[][] uv = {{u0, v1}, {u1, v1}, {u1, v0}, {u0, v0}};
    int rotation = Math.floorMod(face.rotation / 90, 4);
    int a = Math.clamp((int) (alpha * 255), 0, 255);
    int tint =
        face.iridescent ? CoreSpectrum.mix(0xFFFFFF, CoreSpectrum.currentColor(), .40f) : 0xFFFFFF;
    for (int i = 0; i < 4; i++) {
      float[] tex = uv[(i + rotation) % 4];
      out.addVertex(pose.pose(), p[i][0], p[i][1], p[i][2])
          .setColor((tint >> 16) & 255, (tint >> 8) & 255, tint & 255, a)
          .setUv(tex[0], tex[1])
          .setOverlay(overlay)
          .setLight(light)
          .setNormal(pose, nx, ny, nz);
    }
  }

  private static boolean bool(JsonObject obj, String key) {
    return obj.has(key) && obj.get(key).getAsBoolean();
  }

  private static float[] vec(JsonObject obj, String key) {
    if (!obj.has(key)) return new float[] {0, 0, 0};
    JsonArray a = obj.getAsJsonArray(key);
    return new float[] {a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()};
  }

  public interface BoneAnimator {
    void apply(String name, float[] pivot, PoseStack pose);

    default float emissiveAlpha() {
      return 1f;
    }
  }

  private record Bone(String name, String parent, float[] origin, float[] rotation) {}

  private record Texture(int width, int height, ResourceLocation emissive) {}

  private record Face(
      ResourceLocation texture,
      float[] uv,
      boolean translucent,
      int rotation,
      float alpha,
      boolean emissive,
      boolean animatedCore,
      boolean iridescent) {}

  private record Cube(
      String group,
      float[] from,
      float[] to,
      float[] origin,
      float[] rotation,
      Map<String, Face> faces) {}
}
