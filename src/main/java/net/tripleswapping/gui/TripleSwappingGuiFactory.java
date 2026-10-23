package net.tripleswapping.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.tripleswapping.TripleSwappingMod;

public class TripleSwappingGuiFactory {

    public static Screen createConfigScreen(Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("cloth-config")
                || FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            try {
                return ClothConfigScreenHelper.createClothConfigScreen(parent);
            } catch (Throwable t) {
                TripleSwappingMod.LOGGER.warn("Cloth Config GUI failed to instantiate, falling back to vanilla screen.", t);
            }
        }
        return new TripleSwappingConfigScreen(parent);
    }
}
