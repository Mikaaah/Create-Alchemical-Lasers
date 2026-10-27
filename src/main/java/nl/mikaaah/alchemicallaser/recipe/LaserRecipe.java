package nl.mikaaah.alchemicallaser.recipe;

import java.util.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import nl.mikaaah.alchemicallaser.AlchemicalLaser;

public class LaserRecipe extends com.simibubi.create.content.processing.recipe.ProcessingRecipe<SingleRecipeInput, LaserRecipe.Params>
        implements com.simibubi.create.content.processing.sequenced.IAssemblyRecipe {
    public static class Params extends com.simibubi.create.content.processing.recipe.ProcessingRecipeParams {}
    private static final com.simibubi.create.foundation.recipe.IRecipeTypeInfo INFO = new com.simibubi.create.foundation.recipe.IRecipeTypeInfo() {
        public ResourceLocation getId() { return AlchemicalLaser.id("laser_processing"); }
        @SuppressWarnings("unchecked") public <T extends RecipeSerializer<?>> T getSerializer() { return (T) AlchemicalLaser.LASER_SERIALIZER.get(); }
        @SuppressWarnings("unchecked") public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() { return (RecipeType<R>) AlchemicalLaser.LASER_RECIPE.get(); }
    };
    private final int inputCount, duration, minimumRpm;
    private final ResourceLocation lens;
    private final ItemStack result;
    private final Optional<ItemStack> secondary;
    private final float secondaryChance;
    private final boolean copyComponents, consume;
    private final Optional<String> color;
    private java.util.function.Supplier<ItemStack> assemblyResult;
    public LaserRecipe(Ingredient ingredient, int inputCount, ResourceLocation lens, ItemStack result,
            int duration, int minimumRpm, Optional<ItemStack> secondary, float secondaryChance, boolean copyComponents) {
        this(ingredient, inputCount, lens, result, duration, minimumRpm, secondary, secondaryChance, copyComponents, Optional.empty(), false);
    }
    public LaserRecipe(Ingredient ingredient, int inputCount, ResourceLocation lens, ItemStack result,
            int duration, int minimumRpm, Optional<ItemStack> secondary, float secondaryChance, boolean copyComponents,
            Optional<String> color, boolean consume) {
        super(INFO, new Params());
        this.ingredients.add(ingredient);
        this.results.add(new com.simibubi.create.content.processing.recipe.ProcessingOutput(result, 1));
        secondary.ifPresent(s -> this.results.add(new com.simibubi.create.content.processing.recipe.ProcessingOutput(s, secondaryChance)));
        this.processingDuration = duration;
        this.inputCount = inputCount; this.lens = lens; this.result = result; this.duration = duration;
        this.minimumRpm = minimumRpm; this.secondary = secondary; this.secondaryChance = secondaryChance;
        this.copyComponents = copyComponents; this.color = color; this.consume = consume;
    }
    public Ingredient ingredient() { return ingredients.getFirst(); }
    public int inputCount() { return inputCount; }
    public ResourceLocation lens() { return lens; }
    public ItemStack result() { return result; }
    public int duration() { return duration; }
    public int minimumRpm() { return minimumRpm; }
    public Optional<ItemStack> secondary() { return secondary; }
    public float secondaryChance() { return secondaryChance; }
    public boolean copyComponents() { return copyComponents; }
    public Optional<String> color() { return color; }
    public boolean consume() { return consume; }
    public int beamColor(ItemStack installed) { return color.map(s -> Integer.parseInt(s.replace("#", ""), 16)).orElseGet(() -> AlchemicalLaser.lensColor(installed)); }
    @Override protected int getMaxInputCount() { return 1; }
    @Override protected int getMaxOutputCount() { return 2; }
    @Override protected boolean canSpecifyDuration() { return true; }
    @Override public boolean supportsAssembly() { return inputCount == 1 && secondary.isEmpty(); }
    @Override public net.minecraft.network.chat.Component getDescriptionForAssembly() {
        return net.minecraft.network.chat.Component.literal("Laser processing with ").append(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(lens)).getHoverName());
    }
    @Override public void addRequiredMachines(Set<net.minecraft.world.level.ItemLike> list) { list.add(AlchemicalLaser.EMITTER_ITEM.get()); }
    @Override public void addAssemblyIngredients(List<Ingredient> list) { list.add(Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(lens))); }
    @Override public java.util.function.Supplier<java.util.function.Supplier<com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> nl.mikaaah.alchemicallaser.compat.LaserAssemblySubCategory::new;
    }
    @Override public void enforceNextResult(java.util.function.Supplier<ItemStack> supplier) { assemblyResult = supplier; }
    public LaserRecipe assemblyCopy() {
        LaserRecipe copy = new LaserRecipe(ingredient(), inputCount, lens, result, duration, minimumRpm, secondary, secondaryChance, copyComponents, color, consume);
        copy.assemblyResult = assemblyResult; return copy;
    }
    private static final Codec<String> COLOR = Codec.STRING.validate(s -> s.matches("#?[0-9a-fA-F]{6}")
        ? com.mojang.serialization.DataResult.success(s) : com.mojang.serialization.DataResult.error(() -> "Laser color must contain six hexadecimal digits, optionally prefixed with #"));
    public static final MapCodec<LaserRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(LaserRecipe::ingredient),
        Codec.intRange(1, 64).optionalFieldOf("input_count", 1).forGetter(LaserRecipe::inputCount),
        ResourceLocation.CODEC.fieldOf("lens").forGetter(LaserRecipe::lens),
        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(LaserRecipe::result),
        Codec.intRange(1, 72000).optionalFieldOf("duration", 100).forGetter(LaserRecipe::duration),
        Codec.intRange(1, 4096).optionalFieldOf("minimum_rpm", 16).forGetter(LaserRecipe::minimumRpm),
        ItemStack.STRICT_CODEC.optionalFieldOf("secondary").forGetter(LaserRecipe::secondary),
        Codec.floatRange(0, 1).optionalFieldOf("secondary_chance", 1f).forGetter(LaserRecipe::secondaryChance),
        Codec.BOOL.optionalFieldOf("copy_components", false).forGetter(LaserRecipe::copyComponents),
        COLOR.optionalFieldOf("color").forGetter(LaserRecipe::color),
        Codec.BOOL.optionalFieldOf("consume", false).forGetter(LaserRecipe::consume)
    ).apply(i, LaserRecipe::new));
    public boolean matchesInput(ItemStack input, ItemStack installedLens) {
        return input.getCount() >= inputCount && ingredient().test(input)
            && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(installedLens.getItem()).equals(lens);
    }
    @Override public boolean matches(SingleRecipeInput input, Level level) {
        return input.item().getCount() >= inputCount && ingredient().test(input.item());
    }
    public ItemStack makeResult(ItemStack input) {
        if (assemblyResult != null) return assemblyResult.get();
        ItemStack out = result.copy();
        if (copyComponents) {
            out.applyComponents(input.getComponents());
            out.applyComponents(result.getComponentsPatch());
        }
        return out;
    }
    public List<ItemStack> roll(ItemStack input, RandomSource random) {
        List<ItemStack> outputs = new ArrayList<>(); outputs.add(makeResult(input));
        if (assemblyResult == null && secondary.isPresent() && random.nextFloat() < secondaryChance) outputs.add(secondary.get().copy());
        return outputs;
    }
    public record Batch(int consumed, List<ItemStack> outputs) {}
    public Batch rollBatch(ItemStack input, RandomSource random) {
        return rollBatch(input, random, Integer.MAX_VALUE);
    }
    public Batch rollBatch(ItemStack input, RandomSource random, int maxOperations) {
        int operations = Math.min(input.getCount() / inputCount, maxOperations);
        List<ItemStack> outputs = new ArrayList<>();
        for (int operation = 0; operation < operations; operation++) {
            for (ItemStack rolled : roll(input, random)) {
                for (ItemStack existing : outputs) {
                    if (rolled.isEmpty()) break;
                    if (!ItemStack.isSameItemSameComponents(existing, rolled)) continue;
                    int moved = Math.min(rolled.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(moved); rolled.shrink(moved);
                }
                while (!rolled.isEmpty()) outputs.add(rolled.split(Math.min(rolled.getCount(), rolled.getMaxStackSize())));
            }
        }
        return new Batch(operations * inputCount, outputs);
    }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return makeResult(input.item()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public RecipeSerializer<?> getSerializer() { return AlchemicalLaser.LASER_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return AlchemicalLaser.LASER_RECIPE.get(); }

    @Override public boolean isSpecial() { return true; }
    public static final class Serializer implements RecipeSerializer<LaserRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, LaserRecipe> STREAM = StreamCodec.of(
            (buf, r) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.ingredient());
                buf.writeVarInt(r.inputCount); buf.writeResourceLocation(r.lens);
                ItemStack.STREAM_CODEC.encode(buf, r.result); buf.writeVarInt(r.duration); buf.writeVarInt(r.minimumRpm);
                buf.writeBoolean(r.secondary.isPresent()); r.secondary.ifPresent(s -> ItemStack.STREAM_CODEC.encode(buf, s));
                buf.writeFloat(r.secondaryChance); buf.writeBoolean(r.copyComponents);
                buf.writeOptional(r.color, (b, c) -> b.writeUtf(c)); buf.writeBoolean(r.consume);
            }, buf -> new LaserRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), buf.readVarInt(),
                buf.readResourceLocation(), ItemStack.STREAM_CODEC.decode(buf), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean() ? Optional.of(ItemStack.STREAM_CODEC.decode(buf)) : Optional.empty(), buf.readFloat(), buf.readBoolean(), buf.readOptional(b -> b.readUtf()), buf.readBoolean()));
        @Override public MapCodec<LaserRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, LaserRecipe> streamCodec() { return STREAM; }
    }
}
