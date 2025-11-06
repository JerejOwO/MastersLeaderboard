package io.github.jerejowo.mastersleaderboard.client.leaderboard.button;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;
import net.minecraft.text.Text;

public class DisableButton extends Button {

    public DisableButton(Leaderboard leaderboard) {
        super(leaderboard.getDisableButtonX(), leaderboard.getDisableButtonY(), leaderboard.getDisableButtonWidth(), leaderboard.getButtonWidth(), getName(), leaderboard);

    }

    private static String getName() {
        return "Leaderboard: " + (Leaderboard.isEnabled() ? "On" : "Off");
    }

    @Override
    public void onPress() {
        super.onPress();

        leaderboard.toggleEnabled();
        this.setMessage(Text.of(getName()));
    }

    @Override
    public void adjustPosition() {
        this.setX(leaderboard.getDisableButtonX());
    }

    @Override
    protected void setVisibility(boolean enabled) {}

    @Override
    public void toggleVisibility() {}
}
