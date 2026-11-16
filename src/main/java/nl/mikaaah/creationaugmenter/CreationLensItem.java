package nl.mikaaah.creationaugmenter;

import net.minecraft.world.item.Item;

/** A completed augmentation always spends one use, regardless of equipment enchantments. */
public final class CreationLensItem extends Item {
  private final CreationLens kind;

  public CreationLensItem(CreationLens kind) {
    super(new Item.Properties().durability(kind.uses()).fireResistant());
    this.kind = kind;
  }

  @Override
  public void appendHoverText(
      net.minecraft.world.item.ItemStack stack,
      TooltipContext context,
      java.util.List<net.minecraft.network.chat.Component> tooltip,
      net.minecraft.world.item.TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.add(
        net.minecraft.network.chat.Component.translatable(
            "tooltip.creationaugmenter.uses",
            stack.getMaxDamage() - stack.getDamageValue(),
            stack.getMaxDamage()));
  }

  public CreationLens kind() {
    return kind;
  }
}
