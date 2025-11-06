package io.github.jerejowo.mastersleaderboard.client.leaderboard.button;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;

public class RefreshButton extends Button {

    public RefreshButton(Leaderboard leaderboard) {
        super(leaderboard.getRefreshButtonX(), leaderboard.getBottomButtonY(), leaderboard.getRefreshButtonWidth(), leaderboard.getButtonHeight(), "Refresh", leaderboard);
    }

    @Override
    public void onPress() {
        super.onPress();

        leaderboard.refresh();
    }

    @Override
    public void adjustPosition() {
        this.setX(leaderboard.getRefreshButtonX());
    }
}
