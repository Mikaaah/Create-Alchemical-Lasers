package nl.mikaaah.alchemicallaser;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config is loaded before datapacks, unlike per-world server configs. */
public final class DefaultRecipeConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue PROCESSING, CRAFTING;

  static {
    var builder = new ModConfigSpec.Builder();
    builder.push("recipes");
    PROCESSING =
        builder
            .comment(
                "Load the bundled example laser processing recipes. Custom datapack and KubeJS"
                    + " recipes are unaffected. Restart after changing.")
            .define("enableDefaultProcessingRecipes", true);
    CRAFTING =
        builder
            .comment(
                "Load the bundled emitter and lens crafting recipes. Custom recipes are unaffected."
                    + " Restart after changing.")
            .define("enableDefaultCraftingRecipes", true);
    builder.pop();
    SPEC = builder.build();
  }

  private DefaultRecipeConfig() {}
}
