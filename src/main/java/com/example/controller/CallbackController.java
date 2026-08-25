package com.example.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spotify.SpotifyService;

@RestController
public class CallbackController {

    private final SpotifyService spotifyService;

    public CallbackController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/callback")
    public CompletableFuture<String> callback(
            @RequestParam("code") String code) {

        System.out.println("收到 Spotify authorization code");

        return spotifyService.authorize(code)
                .thenApply(credentials -> {
                    System.out.println("Spotify Access Token 取得成功");

                    return "Spotify 授權成功！可以回 Discord 使用 Spotify 功能了。";
                });
    }
}
