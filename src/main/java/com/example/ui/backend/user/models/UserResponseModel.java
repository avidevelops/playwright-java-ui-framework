package com.example.ui.backend.user.models;

/**
 * Response wrapper for {@code GET /api/user}.
 *
 * <pre>{@code
 * { "user": { "email": "...", "username": "...", ... } }
 * }</pre>
 */
public record UserResponseModel(UserModel user) {}
