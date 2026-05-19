package com.pokedex.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wraps all API responses: { success: true/false, data: ..., error: ... }
 * Mirrors the exact JSON format the React frontend expects.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PokemonResponse<T>(
    boolean success,
    T data,
    String error
) {
    public static <T> PokemonResponse<T> ok(T data) {
        return new PokemonResponse<>(true, data, null);
    }

    public static <T> PokemonResponse<T> fail(String error) {
        return new PokemonResponse<>(false, null, error);
    }
}
