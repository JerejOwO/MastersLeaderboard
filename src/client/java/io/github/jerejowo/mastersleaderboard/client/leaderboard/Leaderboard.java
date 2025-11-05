package io.github.jerejowo.mastersleaderboard.client.leaderboard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Leaderboard {

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
    ButtonWidget disableButton;  // TODO: Cambiar por un set de buttons en Leaderboard y siempre que se haga algo para todos (GameMenuScreenMixin.init, new Leaderboard, disableButton, flipButton) hacer foreach
    ButtonWidget flipButton;
    ButtonWidget backButton;
    ButtonWidget nextButton;
    ButtonWidget refreshButton;

    public Leaderboard(TextRenderer textRenderer, MinecraftClient client, int width, int height, int page) {
        this.fetcher = new LeaderboardFetcher();
        this.textRenderer = textRenderer;
        this.client = client;
        this.width = width;
        this.height = height;
        this.page = page;
        this.disableButton = this.buildDisableButton();
        this.flipButton = this.buildFlipButton();
        this.backButton = this.buildBackButton();
        this.nextButton = this.buildNextButton();
        this.refreshButton = this.buildRefreshButton();

        this.fetcher.fetchPlayersAsync(this);
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
        int columnY = this.getColumnY();

        this.drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        this.drawLeaderboardTable(context, leaderboardX, leaderboardY, leftColumnX, middleLeftColumnX, middleRightColumnX, rightColumnX, columnY);
        this.drawLeaderboardInfo(context, leaderboardY, leftColumnX, middleLeftColumnX, middleRightColumnX);
    }

    public void drawPanel(DrawContext context, int panelX, int panelY, int panelWidth, int panelHeight) {
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BG);
    }

    private void drawLeaderboardTable(DrawContext context, int leaderboardX, int leaderboardY, int leftColumnX, int middleLeftColumnX, int middleRightColumnX, int rightColumnX, int columnY) {

        context.drawText(this.textRenderer, "Leaderboard", leftColumnX, leaderboardY - 15, MAIN_TEXT_COLOR, false);

        context.drawText(this.textRenderer, "#", leftColumnX + 8, columnY, SEC_TEXT_COLOR, false);
        context.drawText(this.textRenderer, "Player", middleLeftColumnX + 4, columnY, SEC_TEXT_COLOR, false);
        context.drawText(this.textRenderer, "Points", middleRightColumnX - 2, columnY, SEC_TEXT_COLOR, false);

        columnY += 12;

        int rowY = columnY;
        for (int i = 0; i < 10; i++) {
            int bgColor = (i % 2 == 0) ? ROW_1_COLOR : ROW_2_COLOR;
            context.fill(leaderboardX - 5, rowY, rightColumnX, rowY + 20, bgColor);
            rowY += 20;
        }
        rowY += 2;
        context.fill(leaderboardX - 5, rowY, rightColumnX, rowY + 20, ROW_USER_COLOR);

        int lineBottom = columnY + 222;
        int lineColor = BORDER_COLOR;
        context.fill(leftColumnX, columnY - 2, rightColumnX + 2, columnY, lineColor);
        context.fill(leftColumnX, columnY, leftColumnX + 2, lineBottom, lineColor);
        context.fill(middleLeftColumnX, columnY, middleLeftColumnX + 2, lineBottom, lineColor);
        context.fill(middleRightColumnX, columnY, middleRightColumnX + 2, lineBottom, lineColor);
        context.fill(rightColumnX, columnY, rightColumnX + 2, lineBottom, lineColor);
        context.fill(leftColumnX, lineBottom - 22, rightColumnX + 2, lineBottom - 20, lineColor);
        context.fill(leftColumnX, lineBottom, rightColumnX + 2, lineBottom + 2, lineColor);

    }

    public void drawLeaderboardInfo(DrawContext context, int leaderboardY, int leftColumnX, int middleLeftColumnX, int middleRightColumnX) {

        if (this.loading || this.currentPlayers == null || this.currentPlayers.length == 0) {
            context.drawText(this.textRenderer, "(Loading...)", leftColumnX + this.textRenderer.getWidth("Leaderboard") + 4, leaderboardY - 15, SEC_TEXT_COLOR, false);
            return;
        }

        leaderboardY += 18;
        for (int i = 0; i < Math.min(10, this.currentPlayers.length - this.page * 10) ; i++) {

            LeaderboardPlayer player = this.currentPlayers[i + 10 * this.page];

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

        this.drawPlayer(context, 260, leftColumnX, middleLeftColumnX, middleRightColumnX, user, name); // todo magic number
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

    public ButtonWidget buildDisableButton() {
        return ButtonWidget.builder(Text.of("Leaderboard: " + (enabled ? "On" : "Off")), button -> {
            enabled = !enabled;
            button.setMessage(Text.of("Leaderboard: " + (enabled ? "On" : "Off")));
            this.getFlipButton().visible = enabled;
            this.getBackButton().visible = enabled;
            this.getNextButton().visible = enabled;
            this.getRefreshButton().visible = enabled;

            buttonPressDown(button);
        }).dimensions(this.getPanelX(), this.getPanelY(), this.getDisableButtonWidth(), this.getButtonHeight()).build();
    }

    public ButtonWidget buildFlipButton() {
        return ButtonWidget.builder(Text.of("Flip"), button -> {
            flipped = !flipped;
            this.getDisableButton().setPosition(this.getPanelX(), this.getPanelY());
            button.setPosition(this.getFlipButtonX(), this.getPanelY());
            this.getBackButton().setPosition(this.getBackButtonX(), this.getBottomButtonY());
            this.getNextButton().setPosition(this.getNextButtonX(), this.getBottomButtonY());
            this.getRefreshButton().setPosition(this.getRefreshButtonX(), this.getBottomButtonY());

            this.buttonPressDown(button);
        }).dimensions(this.getFlipButtonX(), this.getPanelY(), this.getFlipButtonWidth(), this.getButtonHeight()).build();
    }

    public ButtonWidget buildBackButton() {
        ButtonWidget backButton = ButtonWidget.builder(Text.of("<-"), button -> {
            if (this.page > 0) {
                this.page = this.page - 1;
                this.fetcher.fetchTexturesAsync(this);
            }
            this.buttonPressDown(button);
        }).dimensions(this.getBackButtonX(), this.getBottomButtonY(), this.getButtonWidth(), this.getButtonHeight()).build();

        backButton.visible = enabled;

        return backButton;
    }

    public ButtonWidget buildNextButton() {
        ButtonWidget nextButton = ButtonWidget.builder(Text.of("->"), button -> {
            if (page < (currentPlayers.length - 1) / 10) {
                page = page + 1;
                this.fetcher.fetchTexturesAsync(this);
            }
            this.buttonPressDown(button);
        }).dimensions(this.getNextButtonX(), this.getBottomButtonY(), this.getButtonWidth(), this.getButtonHeight()).build();

        nextButton.visible = enabled;

        return nextButton;
    }

    public ButtonWidget buildRefreshButton() {
        ButtonWidget refreshButton = ButtonWidget.builder(Text.of("Refresh"), button -> {
            this.fetcher.fetchPlayersAsync(this);
            this.buttonPressDown(button);
        }).dimensions(getRefreshButtonX(), getBottomButtonY(), this.getRefreshButtonWidth(), this.getButtonHeight()).build();

        refreshButton.visible = enabled;

        return refreshButton;
    }

    private void buttonPressDown(ButtonWidget button) {
        button.active = false;

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {}

            MinecraftClient.getInstance().execute(() -> {
                button.active = true;
                button.setFocused(false);
            });
        }).start();
    }

    public ButtonWidget getDisableButton() { return this.disableButton; }

    public ButtonWidget getFlipButton() { return this.flipButton; }

    public ButtonWidget getBackButton() { return this.backButton; }

    public ButtonWidget getNextButton() { return this.nextButton; }

    public ButtonWidget getRefreshButton() { return this.refreshButton; }

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

    private int getColumnY() { return this.getLeaderboardY(); }

    private int getRefreshButtonX() { return this.getBackButtonX() + this.getButtonWidth() + 2; }

    private int getFlipButtonX() { return this.getPanelX() + this.getPanelWidth() - this.getFlipButtonWidth(); }

    private int getBackButtonX() { return this.getLeftColumnX(); }

    private int getNextButtonX() { return this.getMiddleRightColumnX() + 2; }

    private int getBottomButtonY() { return 278; }

    private int getDisableButtonWidth() { return Math.min(100, this.getPanelWidth() / 2) ; }

    private int getFlipButtonWidth() { return Math.min(50, this.getPanelWidth() / 2); }

    private int getRefreshButtonWidth() { return this.getNextButtonX() - this.getBackButtonX() - this.getButtonWidth() - 4; }

    private int getButtonWidth() { return 22; }

    private int getButtonHeight() { return 20; }
}
