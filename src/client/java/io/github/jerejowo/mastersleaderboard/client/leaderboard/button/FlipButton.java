package io.github.jerejowo.mastersleaderboard.client.leaderboard.button;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;

public class FlipButton extends Button {

    public FlipButton(Leaderboard leaderboard) {
        super(leaderboard.getFlipButtonX(), leaderboard.getFlipButtonY(), leaderboard.getFlipButtonWidth(), leaderboard.getButtonHeight(), "Flip", leaderboard);
    }

    @Override
    public void onPress() {
        super.onPress();

        leaderboard.flip();
    }

    @Override
    public void adjustPosition() {
        this.setX(leaderboard.getFlipButtonX());
    }
}
