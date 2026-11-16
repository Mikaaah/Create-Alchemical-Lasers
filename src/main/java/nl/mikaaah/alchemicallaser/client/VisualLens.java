package nl.mikaaah.alchemicallaser.client;

/** Shared stable IDs for items, model overlays, beam tints and particle styles. */
public enum VisualLens {
  QUARTZ("quartz", 0xA8F8FA),
  IGNIS("ignis", 0xFF641F),
  GLACIES("glacies", 0x65DCF4),
  AETHER("aether", 0xC05AF2),
  VOID("void", 0x26D8A8);

  public static VisualLens of(net.minecraft.world.item.ItemStack stack) {
    return switch (nl.mikaaah.alchemicallaser.AlchemicalLaser.lensName(stack)) {
      case "growth" -> VOID;
      case "splitting" -> GLACIES;
      case "power" -> IGNIS;
      case "eclipse" -> AETHER;
      default -> QUARTZ;
    };
  }

  private final String id;
  private final int color;

  VisualLens(String id, int color) {
    this.id = id;
    this.color = color;
  }

  public String id() {
    return id;
  }

  public int color() {
    return color;
  }

  public float red() {
    return ((color >> 16) & 255) / 255f;
  }

  public float green() {
    return ((color >> 8) & 255) / 255f;
  }

  public float blue() {
    return (color & 255) / 255f;
  }
}
