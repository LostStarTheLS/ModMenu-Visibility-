package net.tripleswapping.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;

public class TripleSwappingGuiFactory {

    /**
     * Creates the modern Cloth Config screen if available in classpath / bundled via JiJ,
     * otherwise falls back to the native built-in screen.
     */
    public static Screen createScreen(Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("cloth-config") || hasClothConfigClass()) {
            try {
                return ClothConfigScreenHelper.createConfigScreen(parent);
            } catch (Throwable ignored) {
                // Fallback to built-in screen if any reflection/runtime issue arises
            }
        }
        return new TripleSwappingConfigScreen(parent);
    }

    private static boolean hasClothConfigClass() {
        try {
            Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
