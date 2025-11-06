package io.github.jerejowo.mastersleaderboard.client.leaderboard;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.button.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Leaderboard {

    static final int ROWS = 10;

    private static final int PANEL_BG = 0x88000000;
    private static final int MAIN_TEXT_COLOR = 0xFFFFFF;
    private static final int SEC_TEXT_COLOR = 0xAAAAAA;
    private static final int BORDER_COLOR = 0xFFFFFFFF;
    private static final int ROW_1_COLOR = 0x55000044;
    private static final int ROW_2_COLOR = 0x55000088;
    private static final int ROW_USER_COLOR = 0x55005500;
    private static final int FIRST_POS_COLOR = 0xFFEFBF04;
    private static final int SECOND_POS_COLOR = 0xFFC0C0C0;
    private static final int THIRD_POS_COLOR = 0xFFCE8946;
    private static final int INVALID_POS_COLOR = 0x55FFFFFF;

    static boolean enabled = true;
    static boolean flipped = false;
    int width;
    int height;
    int page;
    boolean loading = true;
    LeaderboardFetcher fetcher;
    TextRenderer textRenderer;
    MinecraftClient client;
    LeaderboardPlayer[] currentPlayers = new LeaderboardPlayer[0];
    static Set<String> loadedAvatars = new HashSet<>();
    Set<Button> buttons;

    public Leaderboard(TextRenderer textRenderer, MinecraftClient client, int width, int height, int page) {
        this.fetcher = new LeaderboardFetcher();
        this.textRenderer = textRenderer;
        this.client = client;
        this.width = width;
        this.height = height;
        this.page = page;
        this.buttons = Set.of(
                new DisableButton(this),
                new FlipButton(this),
                new BackButton(this),
                new NextButton(this),
                new RefreshButton(this)
                );

        this.refresh();
    }

    public void draw(DrawContext context) {
        if (!enabled) { return; }

        int panelX = this.getPanelX();
        int panelY = this.getPanelY();
        int panelWidth = this.getPanelWidth();
        int panelHeight = this.getPanelHeight();

        int leaderboardX = this.getLeaderboardX();
        int leaderboardY = this.getLeaderboardY();
        int leftColumnX = this.getLeftColumnX();
        int middleLeftColumnX = this.getMiddleLeftColumnX();
        int middleRightColumnX = this.getMiddleRightColumnX();
        int rightColumnX = this.getRightColumnX();
        int topColumnY = this.getTopColumnY();
        int userInfoY = this.getUserInfoY();

        this.drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        this.drawLeaderboardTable(context, leaderboardX, leaderboardY, leftColumnX, middleLeftColumnX, middleRightColumnX, rightColumnX, topColumnY);
        this.drawLeaderboardInfo(context, leaderboardY, leftColumnX, middleLeftColumnX, middleRightColumnX, userInfoY);
    }

    public void drawPanel(DrawContext context, int panelX, int panelY, int panelWidth, int panelHeight) {
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BG);
    }

    private void drawLeaderboardTable(DrawContext context, int leaderboardX, int leaderboardY, int leftColumnX, int middleLeftColumnX, int middleRightColumnX, int rightColumnX, int topColumnY) {

        context.drawText(this.textRenderer, "Leaderboard", leftColumnX, leaderboardY - 15, MAIN_TEXT_COLOR, false);

        context.drawText(this.textRenderer, "#", leftColumnX + 8, topColumnY, SEC_TEXT_COLOR, false);
        context.drawText(this.textRenderer, "Player", middleLeftColumnX + 4, topColumnY, SEC_TEXT_COLOR, false);
        context.drawText(this.textRenderer, "Points", middleRightColumnX - 2, topColumnY, SEC_TEXT_COLOR, false);

        topColumnY += 12;

        int rowY = topColumnY;
        for (int i = 0; i < ROWS; i++) {
            int bgColor = (i % 2 == 0) ? ROW_1_COLOR : ROW_2_COLOR;
            context.fill(leaderboardX - 5, rowY, rightColumnX, rowY + 20, bgColor);
            rowY += 20;
        }
        rowY += 2;
        context.fill(leaderboardX - 5, rowY, rightColumnX, rowY + 20, ROW_USER_COLOR);

        int bottomColumnY = this.getBottomColumnY();
        int lineColor = BORDER_COLOR;
        context.fill(leftColumnX, topColumnY - 2, rightColumnX + 2, topColumnY, lineColor);
        context.fill(leftColumnX, topColumnY, leftColumnX + 2, bottomColumnY, lineColor);
        context.fill(middleLeftColumnX, topColumnY, middleLeftColumnX + 2, bottomColumnY, lineColor);
        context.fill(middleRightColumnX, topColumnY, middleRightColumnX + 2, bottomColumnY, lineColor);
        context.fill(rightColumnX, topColumnY, rightColumnX + 2, bottomColumnY, lineColor);
        context.fill(leftColumnX, bottomColumnY - 22, rightColumnX + 2, bottomColumnY - 20, lineColor);
        context.fill(leftColumnX, bottomColumnY, rightColumnX + 2, bottomColumnY + 2, lineColor);

    }

    public void drawLeaderboardInfo(DrawContext context, int leaderboardY, int leftColumnX, int middleLeftColumnX, int middleRightColumnX, int userInfoY) {

        if (this.loading || this.currentPlayers == null || this.currentPlayers.length == 0) {
            context.drawText(this.textRenderer, "(Loading...)", leftColumnX + this.textRenderer.getWidth("Leaderboard") + 4, leaderboardY - 15, SEC_TEXT_COLOR, false);
            return;
        }

        leaderboardY += 18;
        for (int i = 0; i < Math.min(ROWS, this.currentPlayers.length - this.page * ROWS) ; i++) {

            LeaderboardPlayer player = this.currentPlayers[i + ROWS * this.page];

            String name = this.textRenderer.trimToWidth(player.username, middleRightColumnX - middleLeftColumnX - 15);

            if (name.length() < player.username.length()) {
                name = name.substring(0, name.length() - 1) + "...";
            }

            this.drawPlayer(context, leaderboardY, leftColumnX, middleLeftColumnX, middleRightColumnX, player, name);

            context.drawText(this.textRenderer, name, middleLeftColumnX + 14, leaderboardY, MAIN_TEXT_COLOR, false);

            leaderboardY += 20;
        }

        LeaderboardPlayer user = this.getUser();

        String name = this.textRenderer.trimToWidth(user.username + " (You)", middleRightColumnX - middleLeftColumnX - 15);

        if (name.length() < (user.username).length() + 5) {
            name = name.substring(0, name.length() - 2) + "...";
        }

        this.drawPlayer(context, userInfoY, leftColumnX, middleLeftColumnX, middleRightColumnX, user, name);
    }

    private void drawPlayer(DrawContext context, int leaderboardY, int leftColumnX, int middleLeftColumnX, int middleRightColumnX, LeaderboardPlayer player, String name) {
        context.drawTexture(
                Identifier.of("mastersleaderboard", loadedAvatars.contains(player.username) ? ("dynamic/" + player.username.toLowerCase()) : ("textures/gui/default.png")),
                middleLeftColumnX + 4, leaderboardY,
                0, 0,
                8, 8,
                8, 8
        );

        int posColor = player.position <= 3 ?
                (player.position == 1 ? FIRST_POS_COLOR
                : (player.position == 2 ? SECOND_POS_COLOR
                : (player.position > 0 ? THIRD_POS_COLOR : INVALID_POS_COLOR)))
                : MAIN_TEXT_COLOR;
        context.drawText(this.textRenderer,
                String.valueOf(player.position),
                player.position >= 100 ? leftColumnX + 4 : (player.position >= 10 ? leftColumnX + 6 : leftColumnX + 9),
                leaderboardY, posColor, false);

        context.drawText(this.textRenderer,
                name,
                middleLeftColumnX + 14,
                leaderboardY, MAIN_TEXT_COLOR, false);

        context.drawText(this.textRenderer,
                String.valueOf(player.total_points),
                player.total_points >= 10 ? middleRightColumnX + 6 : middleRightColumnX + 9,
                leaderboardY, MAIN_TEXT_COLOR, false);
    }

    public LeaderboardPlayer getUser() {
        return Arrays.stream(this.currentPlayers)
                .filter(p -> Objects.equals(p.username, MinecraftClient.getInstance().getSession().getUsername()))
                .findFirst()
                .orElse(LeaderboardPlayer.getDefaultPlayer());
    }

    public void toggleEnabled() {
        enabled = !enabled;
        this.getButtons().forEach(Button::toggleVisibility);
    }

    public void flip() {
        flipped = !flipped;
        this.getButtons().forEach(Button::adjustPosition);

    }

    public void previousPage() {
        if (this.page > 0) {
            this.page = this.page - 1;
            this.fetcher.fetchTexturesAsync(this);
        }
    }

    public void nextPage() {
        if (page < (currentPlayers.length - 1) / ROWS) {
            page = page + 1;
            this.fetcher.fetchTexturesAsync(this);
        }
    }

    public void refresh() {
        this.fetcher.fetchPlayersAsync(this);
    }

    public static boolean isEnabled() { return enabled; }

    public Set<Button> getButtons() { return this.buttons; }

    public int getDisableButtonX() { return this.getPanelX(); }

    public int getFlipButtonX() { return this.getPanelX() + this.getPanelWidth() - this.getFlipButtonWidth(); }

    public int getRefreshButtonX() { return this.getBackButtonX() + this.getButtonWidth() + 2; }

    public int getBackButtonX() { return this.getLeftColumnX(); }

    public int getNextButtonX() { return this.getMiddleRightColumnX() + 2; }

    public int getDisableButtonY() { return this.getPanelY(); }

    public int getFlipButtonY() { return this.getPanelY(); }

    public int getBottomButtonY() { return this.getBottomColumnY() + 4; }

    public int getDisableButtonWidth() { return Math.min(100, this.getPanelWidth() / 2) ; }

    public int getFlipButtonWidth() { return Math.min(50, this.getPanelWidth() / 2); }

    public int getRefreshButtonWidth() { return this.getNextButtonX() - this.getBackButtonX() - this.getButtonWidth() - 4; }

    public int getButtonWidth() { return 22; }

    public int getButtonHeight() { return 20; }

    private int getPanelX() { return flipped ? this.width - this.getPanelWidth() : 0; }

    private int getPanelY() { return 0; }

    private int getPanelWidth() { return (int) (this.width * 0.33f); }

    private int getPanelHeight() {  return this.height; }

    private int getLeaderboardX() { return this.getPanelX() + 20; }

    private int getLeaderboardY() { return this.getPanelY() + 40; }

    private int getLeftColumnX() { return this.getLeaderboardX() - 7; }

    private int getMiddleLeftColumnX() { return this.getLeftColumnX() + 22; }

    private int getMiddleRightColumnX() { return this.getRightColumnX() - 22; }

    private int getRightColumnX() { return this.getPanelX() + this.getPanelWidth() - 17; }

    private int getTopColumnY() { return this.getLeaderboardY(); }

    private int getBottomColumnY() { return this.getPanelY() + ROWS * 20 + 74; }

    private int getUserInfoY() { return ROWS * 20 + 60; }
}
