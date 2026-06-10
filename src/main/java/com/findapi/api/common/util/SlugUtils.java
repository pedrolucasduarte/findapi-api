package com.findapi.api.common.util;

import java.util.Locale;

public final class SlugUtils {
    private SlugUtils() {
    }

    public static String normalize(String slug) {
        return slug == null ? null : slug.trim().toLowerCase(Locale.ROOT);
    }
}
