package com.mb3364.twitch.api.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mb3364.http.AsyncHttpClient;
import com.mb3364.http.HttpResponse;
import com.mb3364.http.HttpResponseHandler;
import com.mb3364.twitch.api.handlers.BaseFailureHandler;
import com.mb3364.twitch.api.models.Error;

import java.io.IOException;

/**
 * AbstractResource is the abstract base class of a Twitch resource.
 * A resource provides the functionality to access the REST endpoints of the Twitch API.
 *
 * @author Matthew Bell
 */
public abstract class AbstractResource {

    protected static final ObjectMapper objectMapper = new ObjectMapper(); // can reuse
    protected static final AsyncHttpClient http = new AsyncHttpClient(); // can reuse
    private final String baseUrl; // Base url for twitch rest api

    /**
     * Construct a resource using the Twitch API base URL and specified API version.
     *
     * @param baseUrl    the base URL of the Twitch API
     * @param apiVersion the requested version of the Twitch API
     */
    protected AbstractResource(String baseUrl, int apiVersion) {
        this.baseUrl = baseUrl;
        http.setHeader("ACCEPT", "application/vnd.twitchtv.v" + Integer.toString(apiVersion) + "+json"); // Specify API version
        configureObjectMapper();
    }

    /**
     * Configure the Jackson JSON {@link ObjectMapper} to properly parse the API responses.
     */
    private void configureObjectMapper() {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    /**
     * Sets the authentication access token to be included in the HTTP headers of each
     * API request.
     *
     * @param accessToken the user's authentication access token
     */
    public void setAuthAccessToken(String accessToken) {
        if (accessToken != null && accessToken.length() > 0) {
            http.setHeader("Authorization", String.format("OAuth %s", accessToken));
        } else {
            http.removeHeader("Authorization");
        }
    }

    /**
     * Sets the application's client ID to be included in the HTTP headers of each API request.
     *
     * @param clientId the application's client ID
     */
    public void setClientId(String clientId) {
        if (clientId != null && clientId.length() > 0) {
            http.setHeader("Client-ID", clientId);
        } else {
            http.removeHeader("Client-ID");
        }
    }

    /**
     * Handles an error response from the Twitch API by parsing it and sending a callback to the
     * specified handler.
     *
     * @param response HttpResponse, the response from the Http request
     * @param handler the handler for the callback
     */
    public static void handleTwitchErrorResponse(HttpResponse response, BaseFailureHandler handler) {
        try {
            com.mb3364.twitch.api.models.Error error = objectMapper.readValue(response.getContent(), com.mb3364.twitch.api.models.Error.class);
            handler.onFailure(response.getStatusCode(), response.getStatusMessage(), error.getMessage());
        } catch (IOException e) {
            handler.onFailure(e);
        }
    }

    /**
     * Get the base URL to the Twitch API. Intended to be called by subclasses when generating
     * their resource URL endpoint.
     *
     * @return the base URL to the Twitch API
     */
    protected String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Handles HTTP response's from the Twitch API.
     * <p>Since all Http failure logic is the same, we handle it all in one place: here.</p>
     */
    protected static abstract class TwitchHttpResponseHandler extends HttpResponseHandler {

        private BaseFailureHandler apiHandler;

        public TwitchHttpResponseHandler(BaseFailureHandler apiHandler) {
            this.apiHandler = apiHandler;
        }

        @Override
        public abstract void onSuccess(HttpResponse response);

        @Override
        public void onFailure(HttpResponse response) {
            try {
                Error error = objectMapper.readValue(response.getContent(), Error.class);
                apiHandler.onFailure(response.getStatusCode(), response.getStatusMessage(), error.getMessage());
            } catch (IOException e) {
                apiHandler.onFailure(e);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            apiHandler.onFailure(throwable);
        }
    }
}
