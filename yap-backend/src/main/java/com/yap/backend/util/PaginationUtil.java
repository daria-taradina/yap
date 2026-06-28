package com.yap.backend.util;

/**
 * Utility for pagination parameter validation.
 * Caps maximum page size at 50 to prevent abuse.
 */
public final class PaginationUtil {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtil() {}

    /**
     * Clamps the requested page size to the maximum allowed value.
     * Returns DEFAULT_PAGE_SIZE if size is null or non-positive.
     */
    public static int clampSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /**
     * Normalizes the page number. Returns 0 if null or negative.
     */
    public static int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }
}
