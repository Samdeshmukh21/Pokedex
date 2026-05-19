package com.pokedex.controller;

import com.pokedex.dto.PokemonData;
import com.pokedex.dto.PokemonResponse;
import com.pokedex.dto.SearchResult;
import com.pokedex.exception.PokeApiException;
import com.pokedex.service.PokeApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Pokemon API endpoints.
 * Mirrors routes/pokemon.js — same paths, same response format.
 */
@RestController
@RequestMapping("/api")
public class PokemonController {

    private final PokeApiService pokeApiService;

    public PokemonController(PokeApiService pokeApiService) {
        this.pokeApiService = pokeApiService;
    }

    /**
     * GET /api/pokemon/:name — Get full Pokemon details.
     */
    @GetMapping("/pokemon/{name}")
    public ResponseEntity<PokemonResponse<PokemonData>> getPokemon(@PathVariable String name) {
        PokemonData data = pokeApiService.getPokemon(name);
        return ResponseEntity.ok(PokemonResponse.ok(data));
    }

    /**
     * GET /api/pokemon?q=<query> — Search Pokemon by name.
     */
    @GetMapping("/pokemon")
    public ResponseEntity<PokemonResponse<List<SearchResult>>> searchPokemon(
            @RequestParam(name = "q", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new PokeApiException("Query parameter \"q\" is required", 400);
        }
        List<SearchResult> results = pokeApiService.searchPokemon(query);
        return ResponseEntity.ok(PokemonResponse.ok(results));
    }
}
