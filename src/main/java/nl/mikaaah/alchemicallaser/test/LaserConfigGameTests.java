package nl.mikaaah.alchemicallaser.test;

import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;
import nl.mikaaah.alchemicallaser.*;

/** Separate run: disable both defaults in the common config before runConfigTestServer. */
@GameTestHolder("alchemicallaser_config")
@PrefixGameTestTemplate(false)
public class LaserConfigGameTests {
    @GameTest(template="empty",timeoutTicks=50)
    public static void defaultSwitchesPreserveCustomRecipes(GameTestHelper h) {
        h.assertTrue(!DefaultRecipeConfig.PROCESSING.get() && !DefaultRecipeConfig.CRAFTING.get(), "Set both default recipe switches false before running this test");
        var manager=h.getLevel().getRecipeManager();
        for (String id : new String[]{"sand_to_glass","growth_wheat","laser_emitter","power_lens","eclipse_lens"})
            h.assertTrue(manager.byKey(AlchemicalLaser.id(id)).isEmpty(), "Disabled bundled recipe loaded: "+id);
        h.assertTrue(manager.byKey(ResourceLocation.parse("tnk:kjs_consumable")).isPresent(), "Config removed custom KubeJS recipe");
        h.assertTrue(manager.byKey(AlchemicalLaser.id("custom_same_namespace")).isPresent(), "Config incorrectly filtered custom recipes by namespace");
        h.succeed();
    }
}
