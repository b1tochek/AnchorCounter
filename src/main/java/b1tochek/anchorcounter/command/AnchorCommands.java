package b1tochek.anchorcounter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.AnchorTracker;
import b1tochek.anchorcounter.config.AnchorConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AnchorCommands {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("showexplodeds")
                        .executes(AnchorCommands::executeShowExplodeds)
        );

        dispatcher.register(
                ClientCommandManager.literal("resetanchors")
                        .executes(AnchorCommands::executeResetAll)
                        .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                .executes(AnchorCommands::executeResetPlayer))
        );

        dispatcher.register(
                ClientCommandManager.literal("anchorcounter")
                        .executes(AnchorCommands::executeHelp)
                        .then(ClientCommandManager.literal("toggle")
                                .executes(AnchorCommands::executeToggle))
                        .then(ClientCommandManager.literal("reload")
                                .executes(AnchorCommands::executeReload))
                        .then(ClientCommandManager.literal("stats")
                                .executes(AnchorCommands::executeShowExplodeds))
                        .then(ClientCommandManager.literal("reset")
                                .executes(AnchorCommands::executeResetAll)
                                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                        .executes(AnchorCommands::executeResetPlayer)))
                        .then(ClientCommandManager.literal("help")
                                .executes(AnchorCommands::executeHelp))
        );

        dispatcher.register(
                ClientCommandManager.literal("ac")
                        .redirect(dispatcher.getRoot().getChild("anchorcounter"))
        );
    }

    private static int executeShowExplodeds(CommandContext<FabricClientCommandSource> context) {
        showExplodeds(MinecraftClient.getInstance());
        return 1;
    }

    public static void showExplodeds(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        AnchorConfig config = AnchorConfig.get();
        Map<UUID, AnchorTracker.AnchorData> allData = AnchorCounterMod.tracker.getAllData();

        if (allData.isEmpty()) {
            client.player.sendMessage(Text.literal("No anchor data recorded yet.").formatted(Formatting.GRAY), false);
            return;
        }

        List<Map.Entry<UUID, AnchorTracker.AnchorData>> sorted = allData.entrySet()
                .stream()
                .sorted(Comparator.comparingInt((Map.Entry<UUID, AnchorTracker.AnchorData> e) ->
                        e.getValue().exploded).reversed())
                .toList();

        for (Map.Entry<UUID, AnchorTracker.AnchorData> entry : sorted) {
            UUID uuid = entry.getKey();
            AnchorTracker.AnchorData data = entry.getValue();

            String playerName = getPlayerName(client, uuid);

            MutableText line = Text.literal(playerName).formatted(Formatting.YELLOW, Formatting.BOLD)
                    .append(Text.literal(" ").formatted(Formatting.RESET))
                    .append(Text.literal(config.placedText.toUpperCase()).formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                    .append(Text.literal(" " + data.placed).formatted(Formatting.GREEN, Formatting.BOLD))
                    .append(Text.literal("; ").formatted(Formatting.GRAY))
                    .append(Text.literal(config.explodedText.toUpperCase()).formatted(Formatting.DARK_RED, Formatting.BOLD))
                    .append(Text.literal(" " + data.exploded).formatted(Formatting.RED, Formatting.BOLD));

            client.player.sendMessage(line, false);
        }
    }

    private static int executeResetAll(CommandContext<FabricClientCommandSource> context) {
        AnchorCounterMod.tracker.resetAll();
        AnchorCounterMod.sendMessage(MinecraftClient.getInstance(), "§eAll anchor stats reset!");
        return 1;
    }

    private static int executeResetPlayer(CommandContext<FabricClientCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) return 0;

        UUID targetUuid = null;
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player.getName().getString().equalsIgnoreCase(playerName)) {
                targetUuid = player.getUuid();
                break;
            }
        }

        if (targetUuid == null) {
            targetUuid = AnchorCounterMod.tracker.findUuidByName(playerName);
        }

        if (targetUuid != null) {
            AnchorCounterMod.tracker.resetPlayer(targetUuid);
            AnchorCounterMod.sendMessage(client, "§eReset stats for §b" + playerName);
        } else {
            AnchorCounterMod.sendMessage(client, "§cPlayer not found: " + playerName);
        }

        return 1;
    }

    private static int executeToggle(CommandContext<FabricClientCommandSource> context) {
        AnchorConfig.get().enabled = !AnchorConfig.get().enabled;
        AnchorConfig.save();

        AnchorCounterMod.sendMessage(MinecraftClient.getInstance(),
                AnchorConfig.get().enabled ? "§aEnabled" : "§cDisabled");

        return 1;
    }

    private static int executeReload(CommandContext<FabricClientCommandSource> context) {
        AnchorConfig.load();
        AnchorCounterMod.sendMessage(MinecraftClient.getInstance(), "§bConfig reloaded!");
        return 1;
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return 0;

        client.player.sendMessage(Text.literal("§d/showexplodeds §7- Show all anchor statistics"), false);
        client.player.sendMessage(Text.literal("§d/resetanchors §7- Reset all anchor stats"), false);
        client.player.sendMessage(Text.literal("§d/resetanchors <player> §7- Reset stats for player"), false);
        client.player.sendMessage(Text.literal("§d/anchorcounter toggle §7- Toggle display"), false);
        client.player.sendMessage(Text.literal("§d/anchorcounter reload §7- Reload config"), false);

        return 1;
    }

    private static String getPlayerName(MinecraftClient client, UUID uuid) {
        if (client.world != null) {
            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                if (player.getUuid().equals(uuid)) {
                    return player.getName().getString();
                }
            }
        }

        String savedName = AnchorCounterMod.tracker.getPlayerName(uuid);
        if (savedName != null) {
            return savedName;
        }

        return uuid.toString().substring(0, 8);
    }
}