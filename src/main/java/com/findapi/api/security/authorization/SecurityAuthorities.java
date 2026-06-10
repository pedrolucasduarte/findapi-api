package com.findapi.api.security.authorization;

public final class SecurityAuthorities {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_PROVIDER = "ROLE_PROVIDER";
    public static final String ROLE_REVIEWER = "ROLE_REVIEWER";

    public static final String API_READ = "API_READ";
    public static final String API_WRITE = "API_WRITE";
    public static final String API_DELETE = "API_DELETE";
    public static final String CATEGORY_READ = "CATEGORY_READ";
    public static final String CATEGORY_WRITE = "CATEGORY_WRITE";
    public static final String REVIEW_READ = "REVIEW_READ";
    public static final String REVIEW_WRITE = "REVIEW_WRITE";
    public static final String COLLECTION_READ = "COLLECTION_READ";
    public static final String COLLECTION_WRITE = "COLLECTION_WRITE";

    private SecurityAuthorities() {
    }
}
