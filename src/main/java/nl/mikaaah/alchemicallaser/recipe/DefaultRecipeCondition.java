package nl.mikaaah.alchemicallaser.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import nl.mikaaah.alchemicallaser.DefaultRecipeConfig;

/** Applied only to the bundled recipes; never filters other recipes by namespace or ID. */
public record DefaultRecipeCondition(boolean crafting) implements ICondition {
    public static final MapCodec<DefaultRecipeCondition> CODEC = Codec.BOOL.optionalFieldOf("crafting", false)
        .xmap(DefaultRecipeCondition::new, DefaultRecipeCondition::crafting);
    @Override public boolean test(IContext context) {
        return crafting ? DefaultRecipeConfig.CRAFTING.get() : DefaultRecipeConfig.PROCESSING.get();
    }
    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
}
