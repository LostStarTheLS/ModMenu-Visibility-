package net.irisshaders.iris.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.tripleswapping.gui.TripleSwappingGuiFactory;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TripleSwappingGuiFactory::createConfigScreen;
    }
}
