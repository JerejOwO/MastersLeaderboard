package io.github.jerejowo.mastersleaderboard.client.leaderboard.button;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;

public class NextButton extends Button {

    public NextButton(Leaderboard leaderboard) {
        super(leaderboard.getNextButtonX(), leaderboard.getBottomButtonY(), leaderboard.getButtonWidth(), leaderboard.getButtonHeight(), "->", leaderboard);
    }

    @Override
    public void onPress() {
        super.onPress();

        leaderboard.nextPage();
    }

    @Override
    public void adjustPosition() {
        this.setX(leaderboard.getNextButtonX());
    }
}
