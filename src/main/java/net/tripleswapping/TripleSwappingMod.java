package net.tripleswapping;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleSwappingMod implements ModInitializer {
    public static final String MOD_ID = "triple_swapping";
    public static final String MOD_NAME = "Triple Swapping";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] Initialized common components.", MOD_NAME);
    }
}
