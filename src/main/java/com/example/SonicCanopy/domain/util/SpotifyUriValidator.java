package com.example.SonicCanopy.domain.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpotifyUriValidator {

    private static final Pattern URI_PATTERN =
            Pattern.compile("^spotify:(track|album|artist|playlist):([a-zA-Z0-9]+)$");

    private SpotifyUriValidator() {} // prevent instantiation

    public static boolean isValid(String uri) {
        return uri != null && !uri.isBlank() && URI_PATTERN.matcher(uri).matches();
    }

    public static String extractId(String uri) {
        Matcher m = URI_PATTERN.matcher(uri);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid Spotify URI: " + uri);
        }
        return m.group(2);
    }

    public static String extractType(String uri) {
        Matcher m = URI_PATTERN.matcher(uri);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid Spotify URI: " + uri);
        }
        return m.group(1);
    }
}
