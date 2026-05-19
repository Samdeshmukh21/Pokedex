package com.pokedex.dto;

import java.util.List;

public record PokemonData(
    int id,
    String name,
    int height,
    int weight,
    List<String> types,
    List<AbilityEntry> abilities,
    List<StatEntry> stats,
    String sprite,
    String genus,
    String flavorText
) {}
