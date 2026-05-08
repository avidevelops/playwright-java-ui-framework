package com.example.ui.backend.user.generators;

import com.example.ui.backend.user.models.UserModel;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fluent factory builder for {@link UserModel} test data.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 *   // Zero-config — produces a valid authenticated user
 *   UserModel user = UserGenerator.create().build();
 *
 *   // Override username only
 *   UserModel user = UserGenerator.create().withUsername("jane-doe").build();
 * }</pre>
 */
public class UserGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private String email;
    private String token;
    private String username;
    private String bio;
    private String image;

    public UserGenerator() {
        int seq = COUNTER.getAndIncrement();
        this.email    = "test-user-" + seq + "@example.com";
        this.token    = "mock-jwt-token-" + UUID.randomUUID().toString().substring(0, 8);
        this.username = "test-user-" + seq;
        this.bio      = "Bio for test user " + seq;
        this.image    = "https://api.dicebear.com/7.x/avataaars/svg?seed=user" + seq;
    }

    public static UserGenerator create() {
        return new UserGenerator();
    }

    public UserGenerator withEmail(String email)       { this.email = email;       return this; }
    public UserGenerator withToken(String token)       { this.token = token;       return this; }
    public UserGenerator withUsername(String username) { this.username = username; return this; }
    public UserGenerator withBio(String bio)           { this.bio = bio;           return this; }
    public UserGenerator withImage(String image)       { this.image = image;       return this; }

    public UserModel build() {
        return new UserModel(email, token, username, bio, image);
    }
}
