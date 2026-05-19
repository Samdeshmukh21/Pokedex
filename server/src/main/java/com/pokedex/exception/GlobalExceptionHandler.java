package com.pokedex.exception;

import com.pokedex.dto.PokemonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.CompletionException;

/**
 * Centralized error handling — ensures all errors return the same
 * { success: false, error: "..." } JSON format the frontend expects.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PokeApiException.class)
    public ResponseEntity<PokemonResponse<Void>> handlePokeApiException(PokeApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(PokemonResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<PokemonResponse<Void>> handleCompletionException(CompletionException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof PokeApiException pokeEx) {
            return handlePokeApiException(pokeEx);
        }
        return handleGenericException(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PokemonResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(500)
                .body(PokemonResponse.fail("Internal server error"));
    }
}
