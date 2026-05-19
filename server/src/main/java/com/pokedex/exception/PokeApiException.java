package com.pokedex.exception;

/**
 * Custom exception for PokeAPI errors, carrying an HTTP status code.
 * Mirrors PokeAPIError from the JavaScript codebase.
 */
public class PokeApiException extends RuntimeException {

    private final int status;

    public PokeApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
