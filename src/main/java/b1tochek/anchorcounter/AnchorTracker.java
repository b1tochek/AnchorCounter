package b1tochek.anchorcounter;

import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnchorTracker {

    public static class AnchorData {
        public int placed = 0;
        public int exploded = 0;
        public long lastUpdate = 0;
        public String playerName = null;

        public AnchorData() {
            this.lastUpdate = System.currentTimeMillis();
        }

        public boolean hasData() {
            return placed > 0 || exploded > 0;
        }
    }

    private final Map<UUID, AnchorData> playerData = new ConcurrentHashMap<>();
    private final Map<BlockPos, UUID> anchorOwners = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNames = new ConcurrentHashMap<>();

    public void onAnchorPlaced(UUID playerUuid, BlockPos pos, String playerName) {
        if (playerUuid == null) return;

        AnchorData data = playerData.computeIfAbsent(playerUuid, k -> new AnchorData());
        data.placed++;
        data.lastUpdate = System.currentTimeMillis();

        if (playerName != null && !playerName.isEmpty()) {
            data.playerName = playerName;
            playerNames.put(playerUuid, playerName);
        }

        if (pos != null) {
            anchorOwners.put(pos.toImmutable(), playerUuid);
        }
    }

    public void onAnchorPlaced(UUID playerUuid, BlockPos pos) {
        onAnchorPlaced(playerUuid, pos, null);
    }

    public void onAnchorExploded(BlockPos pos) {
        if (pos == null) return;

        UUID owner = anchorOwners.remove(pos.toImmutable());
        if (owner != null) {
            AnchorData data = playerData.get(owner);
            if (data != null) {
                data.exploded++;
                data.lastUpdate = System.currentTimeMillis();
            }
        }
    }

    public void onAnchorExploded(UUID playerUuid, String playerName) {
        if (playerUuid == null) return;

        AnchorData data = playerData.computeIfAbsent(playerUuid, k -> new AnchorData());
        data.exploded++;
        data.lastUpdate = System.currentTimeMillis();

        if (playerName != null && !playerName.isEmpty()) {
            data.playerName = playerName;
            playerNames.put(playerUuid, playerName);
        }
    }

    public void onAnchorExploded(UUID playerUuid) {
        onAnchorExploded(playerUuid, null);
    }

    public AnchorData getData(UUID uuid) {
        return playerData.getOrDefault(uuid, new AnchorData());
    }

    public boolean hasData(UUID uuid) {
        AnchorData data = playerData.get(uuid);
        return data != null && data.hasData();
    }

    public Map<UUID, AnchorData> getAllData() {
        return new HashMap<>(playerData);
    }

    public String getPlayerName(UUID uuid) {
        return playerNames.get(uuid);
    }

    public UUID findUuidByName(String name) {
        for (Map.Entry<UUID, String> entry : playerNames.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getTotalExploded() {
        return playerData.values().stream().mapToInt(d -> d.exploded).sum();
    }

    public int getTotalPlaced() {
        return playerData.values().stream().mapToInt(d -> d.placed).sum();
    }

    public void resetAll() {
        playerData.clear();
        anchorOwners.clear();
    }

    public void resetPlayer(UUID uuid) {
        playerData.remove(uuid);
        anchorOwners.entrySet().removeIf(e -> e.getValue().equals(uuid));
    }
}