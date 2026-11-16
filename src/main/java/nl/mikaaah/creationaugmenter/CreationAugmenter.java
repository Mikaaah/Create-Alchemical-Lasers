package nl.mikaaah.creationaugmenter;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.mikaaah.creationaugmenter.recipe.CreationAugmentingRecipe;

@Mod(CreationAugmenter.ID)
public final class CreationAugmenter {
  public static final String ID = "creationaugmenter";

  public static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(ID, path);
  }

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
  public static final DeferredRegister<BlockEntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
  public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
      DeferredRegister.create(Registries.RECIPE_TYPE, ID);
  public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, ID);
  public static final DeferredRegister<SoundEvent> SOUNDS =
      DeferredRegister.create(Registries.SOUND_EVENT, ID);
  public static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

  public static final DeferredBlock<CreationAugmenterBlock> BLOCK =
      BLOCKS.register(
          "creation_augmenter",
          () ->
              new CreationAugmenterBlock(
                  BlockBehaviour.Properties.of()
                      .strength(8.0F, 1200.0F)
                      .requiresCorrectToolForDrops()
                      .sound(SoundType.NETHERITE_BLOCK)
                      .noOcclusion()));
  public static final DeferredItem<BlockItem> BLOCK_ITEM = ITEMS.registerSimpleBlockItem(BLOCK);
  public static final Map<CreationLens, DeferredItem<Item>> LENSES =
      new EnumMap<>(CreationLens.class);
  public static final Supplier<BlockEntityType<CreationAugmenterBlockEntity>> BLOCK_ENTITY =
      ENTITIES.register(
          "creation_augmenter",
          () ->
              BlockEntityType.Builder.of(CreationAugmenterBlockEntity::new, BLOCK.get())
                  .build(null));
  public static final Supplier<RecipeType<CreationAugmentingRecipe>> AUGMENTATION =
      RECIPE_TYPES.register(
          "creation_augmenting",
          () ->
              new RecipeType<>() {
                @Override
                public String toString() {
                  return ID + ":creation_augmenting";
                }
              });
  public static final Supplier<RecipeSerializer<CreationAugmentingRecipe>> AUGMENTATION_SERIALIZER =
      SERIALIZERS.register("creation_augmenting", CreationAugmentingRecipe.Serializer::new);
  public static final Map<String, Supplier<SoundEvent>> AUDIO = new LinkedHashMap<>();

  static {
    for (CreationLens lens : CreationLens.values())
      LENSES.put(
          lens, ITEMS.registerItem(lens.id() + "_lens", properties -> new CreationLensItem(lens)));
    for (String name : new String[] {"fire", "beam"})
      AUDIO.put(name, SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name))));
    TABS.register(
        "main",
        () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.creationaugmenter"))
                .icon(BLOCK_ITEM::toStack)
                .displayItems(
                    (parameters, output) -> {
                      output.accept(BLOCK_ITEM.get());
                      LENSES.values().forEach(lens -> output.accept(lens.get()));
                    })
                .build());
  }

  public CreationAugmenter(IEventBus bus, ModContainer container) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    ENTITIES.register(bus);
    RECIPE_TYPES.register(bus);
    SERIALIZERS.register(bus);
    SOUNDS.register(bus);
    TABS.register(bus);
  }
}
