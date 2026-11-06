package nl.mikaaah.alchemicallaser.compat;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;
import nl.mikaaah.alchemicallaser.recipe.LaserRecipe;

/** Loaded only by JEI on the client. */
public class LaserAssemblySubCategory extends SequencedAssemblySubCategory {
  public LaserAssemblySubCategory() {
    super(32);
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder, SequencedRecipe<?> step, IFocusGroup focuses, int x) {
    LaserRecipe r = (LaserRecipe) step.getRecipe();
    builder
        .addSlot(
            r.consume() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST, x + 8, 15)
        .addItemStack(new ItemStack(BuiltInRegistries.ITEM.get(r.lens())))
        .addRichTooltipCallback(
            (slot, tooltip) ->
                tooltip.add(
                    Component.translatable(
                        r.consume()
                            ? "recipe.alchemicallaser.consumed"
                            : "recipe.alchemicallaser.reusable")));
  }

  @Override
  public void draw(
      SequencedRecipe<?> step, GuiGraphics graphics, double mouseX, double mouseY, int index) {
    LaserRecipe r = (LaserRecipe) step.getRecipe();
    graphics.renderItem(AlchemicalLaser.EMITTER_ITEM.toStack(), 8, 38);
    int color = 0xff000000 | r.beamColor(new ItemStack(BuiltInRegistries.ITEM.get(r.lens())));
    graphics.fill(15, 55, 18, 72, color);
  }
}
