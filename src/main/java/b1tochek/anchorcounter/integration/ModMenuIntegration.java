package b1tochek.anchorcounter.integration;

import b1tochek.anchorcounter.gui.AnchorConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AnchorConfigScreen(parent);
    }
}