package net.irisshaders.iris;

import net.fabricmc.api.ClientModInitializer;
import net.tripleswapping.TripleSwappingClient;

public class IrisClient implements ClientModInitializer {
    private final TripleSwappingClient delegate = new TripleSwappingClient();

    @Override
    public void onInitializeClient() {
        delegate.onInitializeClient();
    }
}
