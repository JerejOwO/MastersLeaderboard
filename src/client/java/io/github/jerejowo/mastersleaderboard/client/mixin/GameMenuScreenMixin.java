package io.github.jerejowo.mastersleaderboard.client.mixin;

import io.github.jerejowo.mastersleaderboard.client.leaderboard.Leaderboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Unique
    Leaderboard leaderboard = null;
    @Unique
    int page = 0;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.leaderboard = new Leaderboard(this.textRenderer, this.client, this.width, this.height, this.page);

        this.addDrawableChild(leaderboard.getDisableButton());

        this.addDrawableChild(leaderboard.getFlipButton());

        this.addDrawableChild(leaderboard.getBackButton());

        this.addDrawableChild(leaderboard.getNextButton());

        this.addDrawableChild(leaderboard.getRefreshButton());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.leaderboard.draw(context);
    }
}
