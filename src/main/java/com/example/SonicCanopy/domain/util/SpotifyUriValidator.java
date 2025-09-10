package com.example.SonicCanopy.domain.util;

import com.example.SonicCanopy.domain.exception.spotify.InvalidSpotifyUriException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpotifyUriValidator {

    private static final Pattern URI_PATTERN =
            Pattern.compile("^spotify:(track|album|artist|playlist):([a-zA-Z0-9]+)$");

    private SpotifyUriValidator() {} // prevent instantiation

    private static void isValid(String uri) {
        if(uri == null || uri.isBlank() || !URI_PATTERN.matcher(uri).matches()){
            throw new InvalidSpotifyUriException("Invalid Spotify URI");
        }
    }

    public static String extractId(String uri) {
        isValid(uri);
        Matcher m = URI_PATTERN.matcher(uri);
        return m.group(2);
    }

    public static String extractType(String uri) {
        isValid(uri);
        Matcher m = URI_PATTERN.matcher(uri);
        return m.group(1);
    }
}
