package nl.mikaaah.creationaugmenter;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/** A lens selects a process and two restrained beam accents. The core hue is independent. */
public enum CreationLens implements StringRepresentable {
  UNBREAKING("unbreaking", 10, 0xBCEFFF, 0x7DE4EE),
  ABSOLUTE_CONVERSION("absolute_conversion", 100, 0xDEB655, 0xB574FF),
  TRANSMUTATION("transmutation", 100, 0x47D5A0, 0xA998FF);

  public static final Codec<CreationLens> CODEC =
      StringRepresentable.fromEnum(CreationLens::values);
  private final String id;
  private final int color, secondaryColor, uses;

  CreationLens(String id, int uses, int color, int secondaryColor) {
    this.id = id;
    this.uses = uses;
    this.color = color;
    this.secondaryColor = secondaryColor;
  }

  public String id() {
    return id;
  }

  public int uses() {
    return uses;
  }

  public int color() {
    return color;
  }

  public int secondaryColor() {
    return secondaryColor;
  }

  @Override
  public String getSerializedName() {
    return id;
  }

  public static CreationLens of(ItemStack stack) {
    if (stack.isEmpty()) return null;
    for (var entry : CreationAugmenter.LENSES.entrySet())
      if (stack.is(entry.getValue().get())) return entry.getKey();
    return null;
  }
}
