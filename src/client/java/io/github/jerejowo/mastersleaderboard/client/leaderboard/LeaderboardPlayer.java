package io.github.jerejowo.mastersleaderboard.client.leaderboard;

public class LeaderboardPlayer {
    public final static String DEFAULT_USER_NAME = "Not Registered";

    public String uuid;
    public String username;
    public int total_points;
    public String team_name;
    public int position;
    public String avatar_url;

    static LeaderboardPlayer getDefaultPlayer() {
        LeaderboardPlayer leaderboardPlayer = new LeaderboardPlayer();

        leaderboardPlayer.position = 0;
        leaderboardPlayer.total_points = 0;
        leaderboardPlayer.username = DEFAULT_USER_NAME;

        return leaderboardPlayer;
    }
}
