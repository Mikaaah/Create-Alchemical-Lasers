package nl.mikaaah.alchemicallaser.client;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import nl.mikaaah.alchemicallaser.particle.LaserParticles;
import nl.mikaaah.alchemicallaser.particle.LaserParticles.Kind;

/**
 * Four animated materials per lens. Embers use actual block collision and up to two damped bounces.
 */
public final class LaserParticle extends TextureSheetParticle {
  private final SpriteSet sprites;
  private final Kind kind;
  private final VisualLens lens;
  private final float initialSize;
  private int bounces;
  private final double orbitX, orbitZ;
  private double orbitAngle;
  private final double orbitRadius;

  private LaserParticle(
      ClientLevel level,
      double x,
      double y,
      double z,
      double dx,
      double dy,
      double dz,
      SpriteSet sprites,
      VisualLens lens,
      Kind kind) {
    super(level, x, y, z);
    this.sprites = sprites;
    this.lens = lens;
    this.kind = kind;
    this.xd = dx;
    this.yd = dy;
    this.zd = dz;
    this.hasPhysics = kind == Kind.EMBER;
    this.friction = kind == Kind.SMOKE ? .94f : .975f;
    this.gravity = kind == Kind.EMBER ? .55f : kind == Kind.SMOKE ? -.035f : 0;
    this.lifetime =
        switch (kind) {
          case MOTE -> 12 + random.nextInt(9);
          case SPARK -> 5 + random.nextInt(5);
          case EMBER -> 12 + random.nextInt(9);
          case SMOKE -> 20 + random.nextInt(10);
        };
    this.quadSize =
        switch (kind) {
          case MOTE -> .024f;
          case SPARK -> .025f;
          case EMBER -> .038f;
          case SMOKE -> .07f;
        };
    this.initialSize = quadSize;
    this.setSize(.025f, .025f);
    this.roll = this.oRoll = random.nextInt(4) * Mth.HALF_PI;
    int rgb = kind == Kind.SMOKE ? smokeColor(lens) : lens.color();
    if (kind == Kind.SPARK) rgb = brighten(rgb, .6f);
    this.setColor(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
    // Motes orbit their spawn centre without depending on mutable block state.
    this.orbitAngle = Math.atan2(dz, dx);
    this.orbitRadius = Math.hypot(dx, dz);
    this.orbitX = x - Math.cos(orbitAngle) * orbitRadius;
    this.orbitZ = z - Math.sin(orbitAngle) * orbitRadius;
    this.setSpriteFromAge(sprites);
  }

  public static void register(RegisterParticleProvidersEvent event) {
    for (var entry : LaserParticles.entries())
      event.registerSpriteSet(
          entry.type().get(),
          sprites ->
              (options, level, x, y, z, dx, dy, dz) ->
                  new LaserParticle(
                      level, x, y, z, dx, dy, dz, sprites, entry.lens(), entry.kind()));
  }

  @Override
  public void tick() {
    super.tick();
    if (kind == Kind.MOTE) {
      orbitAngle += (lens == VisualLens.VOID ? -.32 : .25);
      double radius = orbitRadius * (1 - (double) age / lifetime * .7);
      setPos(orbitX + Math.cos(orbitAngle) * radius, y, orbitZ + Math.sin(orbitAngle) * radius);
    }
    this.setSpriteFromAge(sprites);
    float t = Mth.clamp((float) age / lifetime, 0, 1);
    alpha = (kind == Kind.SMOKE ? .3f : .95f) * (1 - t) * (1 - t);
    quadSize = initialSize * (kind == Kind.SMOKE ? 1 + t * 2.5f : 1 - t * .4f);
    oRoll = roll;
    if (kind != Kind.SMOKE) roll += lens == VisualLens.GLACIES ? .03f : .1f;
  }

  @Override
  public void move(double dx, double dy, double dz) {
    if (kind != Kind.EMBER || !hasPhysics) {
      super.move(dx, dy, dz);
      return;
    }
    Vec3 request = new Vec3(dx, dy, dz);
    Vec3 allowed = Entity.collideBoundingBox(null, request, getBoundingBox(), level, List.of());
    setBoundingBox(getBoundingBox().move(allowed));
    setLocationFromBoundingbox();
    boolean ground = dy < 0 && allowed.y > dy + 1.0E-7;
    onGround = ground;
    if (Math.abs(allowed.x - dx) > 1.0E-7) xd = -xd * .4;
    if (Math.abs(allowed.z - dz) > 1.0E-7) zd = -zd * .4;
    if (ground) {
      if (bounces++ < 2 && Math.abs(dy) > .015) {
        yd = Math.abs(dy) * .43;
        onGround = false;
      } else {
        yd = 0;
        xd *= .65;
        zd *= .65;
      }
    }
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  @Override
  protected int getLightColor(float partial) {
    return kind == Kind.SMOKE ? super.getLightColor(partial) : 0xF000F0;
  }

  private static int brighten(int rgb, float amount) {
    return ((int) Mth.lerp(amount, (rgb >> 16) & 255, 255) << 16)
        | ((int) Mth.lerp(amount, (rgb >> 8) & 255, 255) << 8)
        | (int) Mth.lerp(amount, rgb & 255, 255);
  }

  private static int smokeColor(VisualLens lens) {
    return switch (lens) {
      case IGNIS -> 0x6B4C3A;
      case GLACIES -> 0xAAD4E0;
      case AETHER -> 0x694D83;
      case VOID -> 0x344F50;
      default -> 0x8296A0;
    };
  }
}
