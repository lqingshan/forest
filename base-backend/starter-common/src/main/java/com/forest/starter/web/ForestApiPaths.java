package com.forest.starter.web;

/**
 * Project-wide top-level API path prefixes.
 *
 * <p>The constants intentionally stop at the endpoint group level. Domain modules
 * own their business resource paths, for example {@code ForestApiPaths.ADMIN + "/organization"}.</p>
 */
public final class ForestApiPaths {
    public static final String API = "/api";
    public static final String AUTH = API + "/auth";
    public static final String CLIENT = API + "/client";
    public static final String ADMIN = API + "/admin";
    public static final String PLATFORM = API + "/platform";
    public static final String OPEN = API + "/open";

    private ForestApiPaths() {
    }
}
