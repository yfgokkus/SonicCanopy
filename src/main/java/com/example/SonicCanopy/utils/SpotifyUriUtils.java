package com.example.SonicCanopy.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpotifyUriUtils {

    private static final Pattern URI_PATTERN =
            Pattern.compile("^spotify:(track|album|artist|playlist):([a-zA-Z0-9]+)$");

    private SpotifyUriUtils() {} // prevent instantiation

    public static boolean isValid(String uri) {
        return uri != null && URI_PATTERN.matcher(uri).matches();
    }

    public static String extractId(String uri) {
        Matcher m = URI_PATTERN.matcher(uri);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid Spotify URI: " + uri);
        }
        return m.group(2); // the ID part
    }

    public static String extractType(String uri) {
        Matcher m = URI_PATTERN.matcher(uri);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid Spotify URI: " + uri);
        }
        return m.group(1); // track, album, artist, playlist
    }
}
