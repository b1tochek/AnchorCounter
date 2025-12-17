package b1tochek.anchorcounter;

import b1tochek.anchorcounter.command.AnchorCommands;
import b1tochek.anchorcounter.config.AnchorConfig;
import b1tochek.anchorcounter.event.AnchorEventHandler;
import b1tochek.anchorcounter.render.AnchorHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AnchorCounterMod implements ClientModInitializer {

    public static final String MOD_ID = "anchorcounter";
    public static AnchorTracker tracker;

    public static KeyBinding toggleKey;
    public static KeyBinding resetKey;
    public static KeyBinding reloadConfigKey;
    public static KeyBinding showExplodedsKey;

    @Override
    public void onInitializeClient() {
        AnchorConfig.load();

        tracker = new AnchorTracker();

        registerKeybindings();

        AnchorEventHandler.register();

        AnchorHudRenderer.register();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AnchorCommands.register(dispatcher);
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void registerKeybindings() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorcounter.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "category.anchorcounter.keys"
        ));

        resetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorcounter.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                "category.anchorcounter.keys"
        ));

        reloadConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorcounter.reload",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL,
                "category.anchorcounter.keys"
        ));

        showExplodedsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorcounter.showexplodeds",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_MINUS,
                "category.anchorcounter.keys"
        ));
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null) return;

        while (toggleKey.wasPressed()) {
            AnchorConfig.get().enabled = !AnchorConfig.get().enabled;
            AnchorConfig.save();
            sendMessage(client, AnchorConfig.get().enabled ?
                    "§aAnchorCounter enabled" : "§cAnchorCounter disabled");
        }

        while (resetKey.wasPressed()) {
            int total = tracker.getTotalExploded();
            tracker.resetAll();
            AnchorEventHandler.reset();
            sendMessage(client, "§eAnchors reset! §7(was: " + total + " exploded)");
        }

        while (reloadConfigKey.wasPressed()) {
            AnchorConfig.load();
            sendMessage(client, "§bConfig reloaded!");
        }

        while (showExplodedsKey.wasPressed()) {
            AnchorCommands.showExplodeds(client);
        }
    }

    public static void sendMessage(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§d[AnchorCounter] §r" + message), false);
        }
    }

    public static void sendActionBar(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§d[AnchorCounter] §r" + message), true);
        }
    }
}