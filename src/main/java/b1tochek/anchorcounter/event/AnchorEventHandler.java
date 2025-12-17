package b1tochek.anchorcounter.event;

import b1tochek.anchorcounter.AnchorCounterMod;
import b1tochek.anchorcounter.config.AnchorConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AnchorEventHandler {

    private static final Map<BlockPos, UUID> anchorOwners = new HashMap<>();
    private static final Set<BlockPos> knownAnchors = new HashSet<>();
    private static final Map<UUID, String> playerNames = new HashMap<>();
    private static final Map<BlockPos, Integer> anchorCharges = new HashMap<>();

    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world == null) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            tickCounter++;

            checkRemovedAnchors(world);

            if (tickCounter % 20 == 0) {
                scanForAnchorsOptimized(world, client);
            }
        });
    }

    private static void scanForAnchorsOptimized(ClientWorld world, MinecraftClient client) {
        int range = AnchorConfig.get().scanRadius;

        List<AbstractClientPlayerEntity> players = world.getPlayers();

        for (AbstractClientPlayerEntity player : players) {
            if (player.squaredDistanceTo(client.player) > range * range) continue;

            BlockPos pPos = player.getBlockPos();

            for (int x = -5; x <= 5; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -5; z <= 5; z++) {
                        BlockPos checkPos = pPos.add(x, y, z);
                        checkBlock(world, checkPos, player);
                    }
                }
            }
        }
    }

    private static void checkBlock(ClientWorld world, BlockPos checkPos, AbstractClientPlayerEntity nearPlayer) {
        BlockState state = world.getBlockState(checkPos);

        if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
            BlockPos immutablePos = checkPos.toImmutable();
            int charges = state.get(RespawnAnchorBlock.CHARGES);

            if (!knownAnchors.contains(immutablePos)) {
                knownAnchors.add(immutablePos);
                anchorCharges.put(immutablePos, charges);

                UUID ownerUuid = nearPlayer.getUuid();
                String ownerName = nearPlayer.getName().getString();

                anchorOwners.put(immutablePos, ownerUuid);
                playerNames.put(ownerUuid, ownerName);

                AnchorCounterMod.tracker.onAnchorPlaced(ownerUuid, immutablePos, ownerName);
            } else {
                anchorCharges.put(immutablePos, charges);
            }
        }
    }

    private static void checkRemovedAnchors(ClientWorld world) {
        if (knownAnchors.isEmpty()) return;

        Iterator<BlockPos> iterator = knownAnchors.iterator();

        int checked = 0;

        while (iterator.hasNext() && checked < 10) {
            BlockPos pos = iterator.next();
            checked++;

            if (!world.isChunkLoaded(pos)) continue;

            BlockState state = world.getBlockState(pos);

            if (!state.isOf(Blocks.RESPAWN_ANCHOR)) {
                iterator.remove();
                anchorCharges.remove(pos);

                UUID owner = anchorOwners.remove(pos);

                AbstractClientPlayerEntity exploder = findNearestPlayer(world, pos);

                if (exploder != null) {
                    UUID exploderUuid = exploder.getUuid();
                    String exploderName = exploder.getName().getString();

                    playerNames.put(exploderUuid, exploderName);
                    AnchorCounterMod.tracker.onAnchorExploded(exploderUuid, exploderName);
                } else if (owner != null) {
                    String ownerName = playerNames.getOrDefault(owner, "Unknown");
                    AnchorCounterMod.tracker.onAnchorExploded(owner, ownerName);
                }
            }
        }
    }

    private static AbstractClientPlayerEntity findNearestPlayer(ClientWorld world, BlockPos pos) {
        double maxDistance = 8.0;
        AbstractClientPlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (AbstractClientPlayerEntity player : world.getPlayers()) {
            double dist = player.getPos().squaredDistanceTo(pos.toCenterPos());
            if (dist < maxDistance * maxDistance && dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    public static void reset() {
        knownAnchors.clear();
        anchorOwners.clear();
        anchorCharges.clear();
    }

    public static String getPlayerName(UUID uuid) {
        return playerNames.get(uuid);
    }
}