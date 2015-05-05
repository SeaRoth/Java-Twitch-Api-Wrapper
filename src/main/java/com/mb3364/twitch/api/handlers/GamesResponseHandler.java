package com.mb3364.twitch.api.handlers;

import com.mb3364.twitch.api.models.Game;

import java.util.List;

public interface GamesResponseHandler extends BaseFailureHandler {
    void onSuccess(int total, List<Game> streams);

    @Override
    void onFailure(int statusCode, String statusMessage, String errorMessage);

    @Override
    void onFailure(Throwable throwable);
}
