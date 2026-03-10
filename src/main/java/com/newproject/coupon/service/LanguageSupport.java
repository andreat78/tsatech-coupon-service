package com.newproject.coupon.service;

import java.util.List;
import java.util.Locale;

public final class LanguageSupport {
    public static final String DEFAULT_LANGUAGE = "it";
    public static final List<String> SUPPORTED_LANGUAGES = List.of("it", "en", "fr", "de", "es");

    private LanguageSupport() {}

    public static String resolveLanguage(String explicitLanguage, String acceptLanguageHeader) {
        String normalized = normalizeLanguage(explicitLanguage);
        if (normalized != null) {
            return normalized;
        }

        if (acceptLanguageHeader != null && !acceptLanguageHeader.isBlank()) {
            String[] entries = acceptLanguageHeader.split(",");
            for (String entry : entries) {
                String candidate = normalizeLanguage(entry);
                if (candidate != null) {
                    return candidate;
                }
            }
        }

        return DEFAULT_LANGUAGE;
    }

    public static String normalizeLanguage(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return null;
        }

        int separator = normalized.indexOf(';');
        if (separator >= 0) {
            normalized = normalized.substring(0, separator);
        }

        separator = normalized.indexOf('-');
        if (separator >= 0) {
            normalized = normalized.substring(0, separator);
        }

        separator = normalized.indexOf('_');
        if (separator >= 0) {
            normalized = normalized.substring(0, separator);
        }

        return SUPPORTED_LANGUAGES.contains(normalized) ? normalized : null;
    }
}
