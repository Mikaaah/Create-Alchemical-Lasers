package nl.mikaaah.creationaugmenter.compat;

import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import nl.mikaaah.creationaugmenter.CreationAugmenter;
import nl.mikaaah.creationaugmenter.recipe.CreationAugmentingRecipe;

@EmiEntrypoint
public class CreationEmiPlugin implements EmiPlugin {
  public static final EmiRecipeCategory CATEGORY =
      new EmiRecipeCategory(
          CreationAugmenter.id("creation_augmenting"),
          EmiStack.of(CreationAugmenter.BLOCK_ITEM.get()));

  @Override
  public void register(EmiRegistry registry) {
    registry.addCategory(CATEGORY);
    registry.addWorkstation(CATEGORY, EmiStack.of(CreationAugmenter.BLOCK_ITEM.get()));
    for (var holder :
        registry.getRecipeManager().getAllRecipesFor(CreationAugmenter.AUGMENTATION.get()))
      registry.addRecipe(new Display(holder.id(), holder.value()));
  }

  private static final class Display extends BasicEmiRecipe {
    final CreationAugmentingRecipe recipe;

    Display(ResourceLocation id, CreationAugmentingRecipe recipe) {
      super(CATEGORY, id, 164, 78);
      this.recipe = recipe;
      var catalyst = EmiStack.of(CreationAugmenter.LENSES.get(recipe.lens()).toStack());
      inputs = List.of(EmiIngredient.of(recipe.ingredient()));
      catalysts = List.of(catalyst);
      outputs = List.of(EmiStack.of(recipe.result()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
      widgets.addSlot(inputs.get(0), 10, 26);
      widgets.addSlot(catalysts.get(0), 57, 4);
      widgets.addTexture(EmiTexture.EMPTY_ARROW, 56, 29);
      widgets.addSlot(outputs.get(0), 104, 26).recipeContext(this);

      widgets.addText(
          Component.translatable("recipe.creationaugmenter.duration", recipe.processingTime()),
          3,
          57,
          0xaaaaaa,
          false);
      widgets.addText(
          Component.translatable("recipe.creationaugmenter.minimum", recipe.minRpm()),
          3,
          68,
          0xaaaaaa,
          false);
    }
  }
}
