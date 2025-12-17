package b1tochek.anchorcounter.gui;

import b1tochek.anchorcounter.config.AnchorConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AnchorConfigScreen extends Screen {

    private final Screen parent;
    private final AnchorConfig config;

    private TextFieldWidget placedTextField;
    private TextFieldWidget explodedTextField;
    private TextFieldWidget formatTextField;
    private TextFieldWidget symbolTextField;
    private TextFieldWidget hudXField;
    private TextFieldWidget hudYField;

    private ButtonWidget enabledButton;
    private ButtonWidget showSelfButton;
    private ButtonWidget showOthersButton;

    public AnchorConfigScreen(Screen parent) {
        super(Text.literal("AnchorCounter Settings"));
        this.parent = parent;
        this.config = AnchorConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        int buttonWidth = 200;
        int fieldWidth = 200;

        enabledButton = ButtonWidget.builder(
                        Text.literal("Enabled: " + (config.enabled ? "§aON" : "§cOFF")),
                        button -> {
                            config.enabled = !config.enabled;
                            updateButtonText();
                        })
                .dimensions(centerX - buttonWidth/2, y, buttonWidth, 20)
                .build();
        this.addDrawableChild(enabledButton);
        y += 24;

        showSelfButton = ButtonWidget.builder(
                        Text.literal("Show Self: " + (config.showSelf ? "§aON" : "§cOFF")),
                        button -> {
                            config.showSelf = !config.showSelf;
                            updateButtonText();
                        })
                .dimensions(centerX - buttonWidth/2, y, buttonWidth, 20)
                .build();
        this.addDrawableChild(showSelfButton);
        y += 24;

        showOthersButton = ButtonWidget.builder(
                        Text.literal("Show Others: " + (config.showOthers ? "§aON" : "§cOFF")),
                        button -> {
                            config.showOthers = !config.showOthers;
                            updateButtonText();
                        })
                .dimensions(centerX - buttonWidth/2, y, buttonWidth, 20)
                .build();
        this.addDrawableChild(showOthersButton);
        y += 30;

        symbolTextField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, y, fieldWidth, 18, Text.literal("Symbol"));
        symbolTextField.setText(config.anchorSymbol);
        symbolTextField.setChangedListener(text -> config.anchorSymbol = text);
        this.addDrawableChild(symbolTextField);
        y += 22;

        placedTextField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, y, fieldWidth, 18, Text.literal("Placed Text"));
        placedTextField.setText(config.placedText);
        placedTextField.setChangedListener(text -> config.placedText = text);
        this.addDrawableChild(placedTextField);
        y += 22;

        explodedTextField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, y, fieldWidth, 18, Text.literal("Exploded Text"));
        explodedTextField.setText(config.explodedText);
        explodedTextField.setChangedListener(text -> config.explodedText = text);
        this.addDrawableChild(explodedTextField);
        y += 22;

        formatTextField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, y, fieldWidth, 18, Text.literal("Format"));
        formatTextField.setMaxLength(100);
        formatTextField.setText(config.displayFormat);
        formatTextField.setChangedListener(text -> config.displayFormat = text);
        this.addDrawableChild(formatTextField);
        y += 26;

        hudXField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, y, 95, 18, Text.literal("HUD X"));
        hudXField.setText(String.valueOf(config.hudX));
        hudXField.setChangedListener(text -> {
            try {
                config.hudX = Integer.parseInt(text);
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(hudXField);

        hudYField = new TextFieldWidget(this.textRenderer, centerX + 5, y, 95, 18, Text.literal("HUD Y"));
        hudYField.setText(String.valueOf(config.hudY));
        hudYField.setChangedListener(text -> {
            try {
                config.hudY = Integer.parseInt(text);
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(hudYField);
        y += 30;

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§aSave & Close"),
                        button -> {
                            AnchorConfig.save();
                            this.client.setScreen(parent);
                        })
                .dimensions(centerX - 102, y, 100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§cCancel"),
                        button -> {
                            AnchorConfig.load();
                            this.client.setScreen(parent);
                        })
                .dimensions(centerX + 2, y, 100, 20)
                .build());
    }

    private void updateButtonText() {
        enabledButton.setMessage(Text.literal("Enabled: " + (config.enabled ? "§aON" : "§cOFF")));
        showSelfButton.setMessage(Text.literal("Show Self: " + (config.showSelf ? "§aON" : "§cOFF")));
        showOthersButton.setMessage(Text.literal("Show Others: " + (config.showOthers ? "§aON" : "§cOFF")));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFF55FF);

        String preview = config.formatDisplay(5, 3);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Preview: §f" + preview, this.width / 2, this.height - 30, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}