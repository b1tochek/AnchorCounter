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

    private TextFieldWidget colorField;

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
        int y = 50;
        int buttonWidth = 200;

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
        y += 35;

        colorField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, 20, Text.literal("Color"));
        colorField.setText(config.nametagColor);
        colorField.setChangedListener(text -> config.nametagColor = text);
        this.addDrawableChild(colorField);
        y += 40;

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

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFF55FF);

        context.drawTextWithShadow(this.textRenderer, "Nametag Color (hex):", this.width / 2 - 100, 135, 0xAAAAAA);

        int previewColor = AnchorConfig.parseColor(config.nametagColor);
        context.drawTextWithShadow(this.textRenderer, "Preview: ", this.width / 2 - 50, this.height - 50, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "-5", this.width / 2 + 10, this.height - 50, previewColor);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}