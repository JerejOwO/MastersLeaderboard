package io.github.jerejowo.mastersleaderboard.client.leaderboard.button;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public abstract class Button extends ButtonWidget {

    protected Leaderboard leaderboard;

    protected Button(int x, int y, int width, int height, String message, Leaderboard leaderboard) {
        super(x, y, width, height, Text.of(message), button -> {}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);

        this.leaderboard = leaderboard;
        this.setVisibility(Leaderboard.isEnabled());
    }

    @Override
    public void onPress() {
        this.pressDownAnimation();
    }

    public void pressDownAnimation() {
        this.active = false;

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {}

            MinecraftClient.getInstance().execute(() -> {
                this.active = true;
                this.setFocused(false);
            });
        }).start();
    }

    protected void setVisibility(boolean enabled) {
        this.visible = enabled;
    }

    public void toggleVisibility() {
        this.visible = !this.visible;
    }

    public abstract void adjustPosition();
}
