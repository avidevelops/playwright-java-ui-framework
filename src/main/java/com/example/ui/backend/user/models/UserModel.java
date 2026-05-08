package com.example.ui.backend.user.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the authenticated user returned by {@code GET /api/user}.
 * <p>
 * Matches the Conduit API shape:
 * <pre>{@code
 * {
 *   "user": {
 *     "email": "...",
 *     "token": "...",
 *     "username": "...",
 *     "bio": "...",
 *     "image": "..."
 *   }
 * }
 * }</pre>
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserModel(
        String email,
        String token,
        String username,
        String bio,
        String image
) {}
