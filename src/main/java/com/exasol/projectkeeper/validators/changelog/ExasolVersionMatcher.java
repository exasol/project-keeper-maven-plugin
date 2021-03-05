package com.exasol.projectkeeper.validators.changelog;

import java.util.regex.Pattern;

/**
 * This class matches version numbers in the format used for Exasol open-source projects (major.minor.fix).
 */
public class ExasolVersionMatcher {
    private static final Pattern EXASOL_VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    /**
     * Match version numbers in the format used for Exasol open-source projects (major.minor.fix).
     * 
     * @param tag string to match
     * @return {@code true} if string matches the format
     */
    public boolean isExasolVersion(final String tag) {
        return EXASOL_VERSION_PATTERN.matcher(tag).matches();
    }
}
