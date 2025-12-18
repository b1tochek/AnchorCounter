package b1tochek.anchorcounter.event;

import b1tochek.anchorcounter.AnchorCounterMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AnchorEventHandler {

    private static final Set<BlockPos> processedAnchors = new HashSet<>();
    private static final Map<UUID, String> playerNames = new HashMap<>();
    private static final Map<UUID, Boolean> wasSwinging = new HashMap<>();

    private static final int VIEW_RADIUS = 100;
    private static final int SCAN_RADIUS = 8;

    public static void register() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world == null) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            checkRemovedAnchors(world);

            scanForAnchors(world, client);

            updateSwingingState(world);
        });
    }

    private static void updateSwingingState(ClientWorld world) {
        for (AbstractClientPlayerEntity player : world.getPlayers()) {
            wasSwinging.put(player.getUuid(), player.handSwinging);
        }
    }

    private static void scanForAnchors(ClientWorld world, MinecraftClient client) {
        for (AbstractClientPlayerEntity player : world.getPlayers()) {
            double distToPlayer = client.player.getPos().distanceTo(player.getPos());
            if (distToPlayer > VIEW_RADIUS) continue;

            boolean isSwinging = player.handSwinging;
            boolean wasPlayerSwinging = wasSwinging.getOrDefault(player.getUuid(), false);

            if (!isSwinging && !wasPlayerSwinging) continue;

            BlockPos playerPos = player.getBlockPos();

            for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
                for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                    for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                        BlockPos checkPos = playerPos.add(x, y, z);

                        double distToBlock = player.getPos().distanceTo(checkPos.toCenterPos());
                        if (distToBlock > SCAN_RADIUS) continue;

                        BlockState state = world.getBlockState(checkPos);

                        if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                            BlockPos immutablePos = checkPos.toImmutable();

                            if (!processedAnchors.contains(immutablePos)) {
                                if (distToBlock < 5.0) {
                                    processedAnchors.add(immutablePos);

                                    UUID ownerUuid = player.getUuid();
                                    String ownerName = player.getName().getString();

                                    playerNames.put(ownerUuid, ownerName);
                                    AnchorCounterMod.tracker.onAnchorPlaced(ownerUuid, immutablePos, ownerName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void checkRemovedAnchors(ClientWorld world) {
        if (processedAnchors.isEmpty()) return;

        Iterator<BlockPos> iterator = processedAnchors.iterator();

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            if (!world.isChunkLoaded(pos)) continue;

            BlockState state = world.getBlockState(pos);

            if (!state.isOf(Blocks.RESPAWN_ANCHOR)) {
                iterator.remove();
            }
        }
    }

    public static void reset() {
        processedAnchors.clear();
        wasSwinging.clear();
    }

    public static String getPlayerName(UUID uuid) {
        return playerNames.get(uuid);
    }
}