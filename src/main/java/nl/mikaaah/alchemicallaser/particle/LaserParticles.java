package nl.mikaaah.alchemicallaser.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.mikaaah.alchemicallaser.client.VisualLens;

/**
 * SimpleParticleType implements ParticleOptions with the standard codec and network stream codec.
 */
public final class LaserParticles {
  public enum Kind {
    MOTE,
    SPARK,
    EMBER,
    SMOKE
  }

  public record Entry(
      VisualLens lens, Kind kind, DeferredHolder<ParticleType<?>, SimpleParticleType> type) {}

  private static final DeferredRegister<ParticleType<?>> TYPES =
      DeferredRegister.create(Registries.PARTICLE_TYPE, "alchemicallaser");
  private static final List<Entry> ENTRIES = new ArrayList<>();

  static {
    for (VisualLens lens : VisualLens.values())
      for (Kind kind : Kind.values()) {
        String key = lens.id() + "_" + kind.name().toLowerCase(Locale.ROOT);
        ENTRIES.add(
            new Entry(lens, kind, TYPES.register(key, () -> new SimpleParticleType(false))));
      }
  }

  private LaserParticles() {}

  public static void register(IEventBus bus) {
    TYPES.register(bus);
  }

  public static List<Entry> entries() {
    return List.copyOf(ENTRIES);
  }

  public static SimpleParticleType type(VisualLens lens, Kind kind) {
    return ENTRIES.get(lens.ordinal() * Kind.values().length + kind.ordinal()).type().get();
  }
}
