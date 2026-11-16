package nl.mikaaah.creationaugmenter.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import nl.mikaaah.creationaugmenter.*;

/** One input item and one lens use per committed operation. */
public record CreationAugmentingRecipe(
    CreationLens lens,
    Ingredient ingredient,
    ItemStack result,
    boolean copyComponents,
    int processingTime,
    int minRpm)
    implements Recipe<SingleRecipeInput> {
  private static final Codec<CreationLens> LENS =
      Codec.STRING.comapFlatMap(
          value -> {
            String key =
                value.startsWith(CreationAugmenter.ID + ":")
                    ? value.substring(value.indexOf(':') + 1)
                    : value;
            if (key.endsWith("_lens")) key = key.substring(0, key.length() - 5);
            for (CreationLens lens : CreationLens.values())
              if (lens.id().equals(key)) return DataResult.success(lens);
            return DataResult.error(() -> "Unknown Creation Lens: " + value);
          },
          CreationLens::id);
  public static final MapCodec<CreationAugmentingRecipe> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(
                      LENS.fieldOf("lens").forGetter(CreationAugmentingRecipe::lens),
                      Ingredient.CODEC_NONEMPTY
                          .fieldOf("ingredient")
                          .forGetter(CreationAugmentingRecipe::ingredient),
                      ItemStack.STRICT_CODEC
                          .fieldOf("result")
                          .forGetter(CreationAugmentingRecipe::result),
                      Codec.BOOL
                          .optionalFieldOf("copy_components", false)
                          .forGetter(CreationAugmentingRecipe::copyComponents),
                      Codec.intRange(1, 72000)
                          .optionalFieldOf("processing_time", 60)
                          .forGetter(CreationAugmentingRecipe::processingTime),
                      Codec.intRange(128, 65536)
                          .optionalFieldOf("min_rpm", 128)
                          .forGetter(CreationAugmentingRecipe::minRpm))
                  .apply(i, CreationAugmentingRecipe::new));

  public CreationAugmentingRecipe {
    if (lens == null
        || ingredient.isEmpty()
        || result.isEmpty()
        || processingTime < 1
        || processingTime > 72000
        || minRpm < 128
        || minRpm > 65536) throw new IllegalArgumentException("Invalid Creation Augmenter recipe");
    result = result.copy();
  }

  /** Compatibility constructor for source-level integrations using the original supplied format. */
  public CreationAugmentingRecipe(
      CreationLens lens, Ingredient ingredient, ItemStack result, boolean copyComponents) {
    this(lens, ingredient, result, copyComponents, 60, 128);
  }

  @Override
  public ItemStack result() {
    return result.copy();
  }

  public boolean matches(ItemStack input, CreationLens installed) {
    return installed == lens
        && !input.isEmpty()
        && ingredient.test(input)
        && !ItemStack.matches(input.copyWithCount(1), makeResult(input));
  }

  public ItemStack makeResult(ItemStack input) {
    ItemStack output = result.copy();
    if (copyComponents) {
      output.applyComponents(input.getComponentsPatch());
      output.applyComponents(result.getComponentsPatch());
    }
    return output;
  }

  @Override
  public boolean matches(SingleRecipeInput input, Level level) {
    return ingredient.test(input.item());
  }

  @Override
  public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
    return makeResult(input.item());
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return result.copy();
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return CreationAugmenter.AUGMENTATION_SERIALIZER.get();
  }

  @Override
  public RecipeType<?> getType() {
    return CreationAugmenter.AUGMENTATION.get();
  }

  @Override
  public boolean isSpecial() {
    return true;
  }

  public static final class Serializer implements RecipeSerializer<CreationAugmentingRecipe> {
    private static final StreamCodec<RegistryFriendlyByteBuf, CreationAugmentingRecipe> STREAM =
        StreamCodec.of(
            (buf, r) -> {
              buf.writeEnum(r.lens);
              Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.ingredient);
              ItemStack.STREAM_CODEC.encode(buf, r.result);
              buf.writeBoolean(r.copyComponents);
              buf.writeVarInt(r.processingTime);
              buf.writeVarInt(r.minRpm);
            },
            buf ->
                new CreationAugmentingRecipe(
                    buf.readEnum(CreationLens.class),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    ItemStack.STREAM_CODEC.decode(buf),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readVarInt()));

    @Override
    public MapCodec<CreationAugmentingRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CreationAugmentingRecipe> streamCodec() {
      return STREAM;
    }
  }
}
