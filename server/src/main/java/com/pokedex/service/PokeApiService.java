package com.pokedex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedex.cache.LRUCache;
import com.pokedex.dto.*;
import com.pokedex.exception.PokeApiException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service layer for fetching Pokemon data from PokeAPI.
 * Port of services/pokeapi.js — includes retry logic, input sanitization,
 * parallel fetching, and LRU caching.
 */
@Service
public class PokeApiService {

    private static final String BASE_URL = "https://pokeapi.co/api/v2";
    private static final int MAX_RETRIES = 2;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LRUCache<PokemonData> pokemonCache;
    private final LRUCache<List<SearchResult>> searchCache;

    public PokeApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.pokemonCache = new LRUCache<>(200, 10 * 60 * 1000L);
        this.searchCache = new LRUCache<>(200, 10 * 60 * 1000L);
    }

    /**
     * Sanitize a Pokemon name — lowercase, trim, remove non-alphanumeric/hyphen chars.
     * Mirrors sanitizeName() from pokeapi.js.
     */
    private String sanitizeName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("[^a-z0-9-]", "");
    }

    /**
     * Fetch a URL with retry logic (up to MAX_RETRIES attempts with backoff).
     * Mirrors fetchWithRetry() from pokeapi.js.
     */
    private JsonNode fetchWithRetry(String url) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readTree(response.body());
                }

                if (response.statusCode() == 404) {
                    throw new PokeApiException("Pokemon not found", 404);
                }

                if (attempt == MAX_RETRIES) {
                    throw new PokeApiException("Failed to fetch from PokeAPI",
                            response.statusCode());
                }

                // Backoff before retry
                Thread.sleep(1000L * (attempt + 1));

            } catch (PokeApiException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PokeApiException("Request interrupted", 500);
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    throw new PokeApiException("Failed to fetch from PokeAPI", 500);
                }
                try {
                    Thread.sleep(1000L * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PokeApiException("Request interrupted", 500);
                }
            }
        }
        throw new PokeApiException("Failed to fetch from PokeAPI", 500);
    }

    /**
     * Fetch a URL with retry, returning null instead of throwing on failure.
     * Used for optional data like species info.
     */
    private JsonNode fetchWithRetryOrNull(String url) {
        try {
            return fetchWithRetry(url);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get full Pokemon details by name.
     * Mirrors getPokemon() from pokeapi.js.
     */
    public PokemonData getPokemon(String name) {
        String sanitized = sanitizeName(name);
        if (sanitized.isEmpty()) {
            throw new PokeApiException("Invalid pokemon name", 400);
        }

        // Check cache first
        PokemonData cached = pokemonCache.get(sanitized);
        if (cached != null) {
            return cached;
        }

        // Fetch pokemon + species in parallel (mirrors Promise.all from JS)
        CompletableFuture<JsonNode> pokemonFuture = CompletableFuture.supplyAsync(
                () -> fetchWithRetry(BASE_URL + "/pokemon/" + sanitized));
        CompletableFuture<JsonNode> speciesFuture = CompletableFuture.supplyAsync(
                () -> fetchWithRetryOrNull(BASE_URL + "/pokemon-species/" + sanitized));

        JsonNode pokemon = pokemonFuture.join();
        JsonNode species = speciesFuture.join();

        // Extract types
        List<String> types = new ArrayList<>();
        for (JsonNode typeNode : pokemon.get("types")) {
            types.add(typeNode.get("type").get("name").asText());
        }

        // Extract abilities
        List<AbilityEntry> abilities = new ArrayList<>();
        for (JsonNode abilityNode : pokemon.get("abilities")) {
            abilities.add(new AbilityEntry(
                    abilityNode.get("ability").get("name").asText(),
                    abilityNode.get("is_hidden").asBoolean()
            ));
        }

        // Extract stats
        List<StatEntry> stats = new ArrayList<>();
        for (JsonNode statNode : pokemon.get("stats")) {
            stats.add(new StatEntry(
                    statNode.get("stat").get("name").asText(),
                    statNode.get("base_stat").asInt()
            ));
        }

        // Extract sprite — prefer official artwork, fallback to front_default
        String sprite = null;
        JsonNode sprites = pokemon.get("sprites");
        JsonNode other = sprites.get("other");
        if (other != null) {
            JsonNode officialArtwork = other.get("official-artwork");
            if (officialArtwork != null) {
                JsonNode frontDefault = officialArtwork.get("front_default");
                if (frontDefault != null && !frontDefault.isNull()) {
                    sprite = frontDefault.asText();
                }
            }
        }
        if (sprite == null) {
            JsonNode frontDefault = sprites.get("front_default");
            if (frontDefault != null && !frontDefault.isNull()) {
                sprite = frontDefault.asText();
            }
        }

        // Extract genus (English) from species data
        String genus = null;
        if (species != null && species.has("genera")) {
            for (JsonNode genusNode : species.get("genera")) {
                if ("en".equals(genusNode.get("language").get("name").asText())) {
                    genus = genusNode.get("genus").asText();
                    break;
                }
            }
        }

        // Extract flavor text (English) from species data
        String flavorText = null;
        if (species != null && species.has("flavor_text_entries")) {
            for (JsonNode entry : species.get("flavor_text_entries")) {
                if ("en".equals(entry.get("language").get("name").asText())) {
                    flavorText = entry.get("flavor_text").asText()
                            .replaceAll("[\\n\\f]", " ");
                    break;
                }
            }
        }

        PokemonData result = new PokemonData(
                pokemon.get("id").asInt(),
                pokemon.get("name").asText(),
                pokemon.get("height").asInt(),
                pokemon.get("weight").asInt(),
                types, abilities, stats,
                sprite, genus, flavorText
        );

        pokemonCache.set(sanitized, result);
        return result;
    }

    /**
     * Search Pokemon by partial name match.
     * Mirrors searchPokemon() from pokeapi.js.
     */
    public List<SearchResult> searchPokemon(String query) {
        String sanitized = sanitizeName(query);
        if (sanitized.isEmpty()) {
            throw new PokeApiException("Invalid search query", 400);
        }

        String cacheKey = "search:" + sanitized;
        List<SearchResult> cached = searchCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        JsonNode list = fetchWithRetry(BASE_URL + "/pokemon?limit=1000");
        JsonNode results = list.get("results");

        List<SearchResult> matches = new ArrayList<>();
        for (JsonNode pokemon : results) {
            String pokemonName = pokemon.get("name").asText();
            if (pokemonName.contains(sanitized)) {
                matches.add(new SearchResult(pokemonName));
                if (matches.size() >= 20) break;
            }
        }

        searchCache.set(cacheKey, matches);
        return matches;
    }
}
