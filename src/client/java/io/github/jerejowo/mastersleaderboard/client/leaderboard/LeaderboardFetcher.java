package io.github.jerejowo.mastersleaderboard.client.leaderboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LeaderboardFetcher {

    private static final String API = "https://api.sclab.live/api/matches/leaderboard?page=1&limit=300";

    @Unique
    void fetchPlayersAsync(Leaderboard leaderboard) {
        leaderboard.loading = true;

        new Thread(() -> {
            try {
                leaderboard.currentPlayers = this.getApiResponse();
            } catch (IOException | InterruptedException ignored) {}

            leaderboard.loading = false;

            this.fetchTexturesAsync(leaderboard);
            this.fetchPlayerTexture(leaderboard.getUser());
        }, "playersFetcher").start();
    }

    @Unique
    void fetchTexturesAsync(Leaderboard leaderboard) {
        new Thread(() -> {
            for (int i = 0 ; i < Math.min(10, leaderboard.currentPlayers.length - leaderboard.page * 10) ; i++) {
                LeaderboardPlayer player = leaderboard.currentPlayers[i + leaderboard.page * 10];

                if (Leaderboard.loadedAvatars.contains(player.username)) {
                    continue;
                }

                this.fetchPlayerTexture(player);
            }
        }, "textureFetcher").start();
    }

    private void fetchPlayerTexture(LeaderboardPlayer player) {
        try {
            URL url = new URI(player.avatar_url.substring(0, player.avatar_url.length() - 2) + "8").toURL();

            InputStream inputStream = url.openStream();

            NativeImage image = NativeImage.read(inputStream);

            Identifier id = Identifier.of("mastersleaderboard", "dynamic/" + player.username.toLowerCase());

            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(image))
            );

            Leaderboard.loadedAvatars.add(player.username);
        } catch (Exception ignored) {
        }
    }

    @Unique
    private LeaderboardPlayer[] getApiResponse() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();

        return new Gson().fromJson(
                root.getAsJsonArray("players"),
                LeaderboardPlayer[].class
        );
    }
}
