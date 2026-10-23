package net.irisshaders.iris;

import net.fabricmc.api.ModInitializer;
import net.tripleswapping.TripleSwappingMod;

public class Iris implements ModInitializer {
    private final TripleSwappingMod delegate = new TripleSwappingMod();

    @Override
    public void onInitialize() {
        delegate.onInitialize();
    }
}
