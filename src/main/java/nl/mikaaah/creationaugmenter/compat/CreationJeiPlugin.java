package nl.mikaaah.creationaugmenter.compat;

import java.util.Arrays;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nl.mikaaah.creationaugmenter.CreationAugmenter;
import nl.mikaaah.creationaugmenter.recipe.CreationAugmentingRecipe;

@JeiPlugin
public class CreationJeiPlugin implements IModPlugin {
  public static final RecipeType<CreationAugmentingRecipe> TYPE =
      new RecipeType<>(CreationAugmenter.id("creation_augmenting"), CreationAugmentingRecipe.class);

  @Override
  public ResourceLocation getPluginUid() {
    return CreationAugmenter.id("jei");
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    if (Minecraft.getInstance().level != null)
      registration.addRecipes(
          TYPE,
          Minecraft.getInstance()
              .level
              .getRecipeManager()
              .getAllRecipesFor(CreationAugmenter.AUGMENTATION.get())
              .stream()
              .map(r -> r.value())
              .toList());
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(CreationAugmenter.BLOCK_ITEM.toStack(), TYPE);
  }

  private static class Category implements IRecipeCategory<CreationAugmentingRecipe> {
    final IDrawable icon;

    Category(IGuiHelper gui) {
      icon =
          gui.createDrawableIngredient(
              VanillaTypes.ITEM_STACK, CreationAugmenter.BLOCK_ITEM.toStack());
    }

    @Override
    public RecipeType<CreationAugmentingRecipe> getRecipeType() {
      return TYPE;
    }

    @Override
    public Component getTitle() {
      return Component.translatable("recipe.creationaugmenter.creation_augmenting");
    }

    @Override
    public int getWidth() {
      return 164;
    }

    @Override
    public int getHeight() {
      return 78;
    }

    @Override
    public IDrawable getIcon() {
      return icon;
    }

    @Override
    public void setRecipe(
        IRecipeLayoutBuilder builder, CreationAugmentingRecipe recipe, IFocusGroup focuses) {
      List<ItemStack> inputs =
          Arrays.stream(recipe.ingredient().getItems()).map(s -> s.copyWithCount(1)).toList();
      builder.addSlot(RecipeIngredientRole.INPUT, 10, 26).addItemStacks(inputs);
      builder
          .addSlot(RecipeIngredientRole.CATALYST, 57, 4)
          .addItemStack(CreationAugmenter.LENSES.get(recipe.lens()).toStack())
          .addRichTooltipCallback(
              (view, tooltip) ->
                  tooltip.add(Component.translatable("recipe.creationaugmenter.lens_use")));
      builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 26).addItemStack(recipe.result());
    }

    @Override
    public void draw(
        CreationAugmentingRecipe recipe,
        IRecipeSlotsView slots,
        GuiGraphics graphics,
        double mouseX,
        double mouseY) {
      var font = Minecraft.getInstance().font;
      graphics.drawString(font, "→", 62, 31, 0x725335, false);
      graphics.drawString(
          font,
          Component.translatable("recipe.creationaugmenter.duration", recipe.processingTime()),
          3,
          57,
          0x555555,
          false);
      graphics.drawString(
          font,
          Component.translatable("recipe.creationaugmenter.minimum", recipe.minRpm()),
          3,
          68,
          0x555555,
          false);
    }
  }
}
