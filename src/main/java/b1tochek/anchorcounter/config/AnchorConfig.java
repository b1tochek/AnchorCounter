package b1tochek.anchorcounter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnchorConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("anchorcounter.json");

    private static AnchorConfig INSTANCE;

    public boolean enabled = true;
    public boolean showSelf = true;
    public boolean showOthers = true;

    public String placedText = "placed";
    public String explodedText = "exploded";
    public String anchorSymbol = "⚓";

    public String placedColor = "#55FFFF";
    public String explodedColor = "#FF5555";
    public String symbolColor = "#FF55FF";
    public String textColor = "#FFFFFF";
    public String numberColor = "#FFFF55";

    public String displayFormat = "{symbol} {placed_text} {placed} | {exploded_text} {exploded}";

    public int hudX = 10;
    public int hudY = 10;

    public double maxRenderDistance = 50.0;
    public int scanRadius = 32;

    public float nametagOffsetY = 0.3f;
    public float nametagScale = 0.025f;

    public static AnchorConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, AnchorConfig.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (INSTANCE == null) {
            INSTANCE = new AnchorConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(get()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int parseColor(String hex) {
        try {
            String clean = hex.replace("#", "");
            return Integer.parseInt(clean, 16) | 0xFF000000;
        } catch (Exception e) {
            return 0xFFFFFFFF;
        }
    }

    public String formatDisplay(int placed, int exploded) {
        return displayFormat
                .replace("{symbol}", anchorSymbol)
                .replace("{placed_text}", placedText)
                .replace("{placed}", String.valueOf(placed))
                .replace("{exploded_text}", explodedText)
                .replace("{exploded}", String.valueOf(exploded));
    }
}