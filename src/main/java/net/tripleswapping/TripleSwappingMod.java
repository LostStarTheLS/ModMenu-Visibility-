package net.tripleswapping;

import net.fabricmc.api.ModInitializer;
import net.tripleswapping.config.TripleSwappingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleSwappingMod implements ModInitializer {
    public static final String MOD_ID = "iris";
    public static final String ASSET_NAMESPACE = "triple_swapping";
    public static final Logger LOGGER = LoggerFactory.getLogger("Iris");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Iris for Minecraft 1.21.11...");
        TripleSwappingConfig.load();
    }
}
