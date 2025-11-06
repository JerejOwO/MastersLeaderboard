package io.github.jerejowo.mastersleaderboard.client.leaderboard.button;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;

public class BackButton extends Button {

    public BackButton(Leaderboard leaderboard) {
        super(leaderboard.getBackButtonX(), leaderboard.getBottomButtonY(), leaderboard.getButtonWidth(), leaderboard.getButtonHeight(), "<-", leaderboard);
    }

    @Override
    public void onPress() {
        super.onPress();

        this.leaderboard.previousPage();
    }

    public void adjustPosition() {
        this.setX(leaderboard.getBackButtonX());
    }
}
