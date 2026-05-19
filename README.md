# Pokédex - Pokemon Search Engine

A full-stack Pokemon search engine built with Java/Spring Boot and React. Fetches data from the [PokeAPI](https://pokeapi.co/), caches responses for performance, and displays rich Pokemon information in a modern UI.

## Features

- **Search** by Pokemon name with autocomplete suggestions
- **Rich display** - stats, abilities, types, measurements (metric & imperial), flavor text
- **Featured Pokemon** grid showing 7 Pokemon on load (click to view details)
- **Server-side caching** - LRU cache with TTL (200 entries, 10min expiry)
- **Responsive** - works on desktop and mobile
- **Error handling** - 404, network errors, invalid input

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21+, Spring Boot 3.4 |
| Frontend | React 18, Vite |
| Caching | In-memory LRU cache (LinkedHashMap) |
| API | PokeAPI v2 |

## Getting Started

### Prerequisites

- Java 21+ (Maven is bundled via Maven Wrapper)
- Node.js 18+ (for the React frontend)

### Install & Run

```sh
cd pokedex

# Install frontend dependencies
npm install --prefix client

# Build frontend
npm run build --prefix client

# Start Java server (serves frontend + API)
cd server && ./mvnw spring-boot:run
```

Open http://localhost:3001

### Development (hot reload)

```sh
npm install --prefix client

# Start both servers concurrently:
npm run dev

# Or manually:
# Terminal 1 — Java server:
cd server && ./mvnw spring-boot:run

# Terminal 2 — Vite dev server:
npm run dev --prefix client
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pokemon/:name` | Get full Pokemon details |
| GET | `/api/pokemon?q=<query>` | Search Pokemon by name |

### Response Format

```json
{
  "success": true,
  "data": {
    "id": 25,
    "name": "pikachu",
    "types": ["electric"],
    "stats": [
      { "name": "hp", "value": 35 },
      { "name": "attack", "value": 55 }
    ],
    "abilities": [
      { "name": "static", "isHidden": false }
    ],
    "height": 4,
    "weight": 60,
    "sprite": "https://...",
    "genus": "Mouse Pokémon",
    "flavorText": "..."
  }
}
```

## Project Structure

```
pokedex/
├── server/                           Java Spring Boot backend
│   ├── mvnw                          Maven Wrapper script
│   ├── pom.xml                       Maven build file
│   └── src/main/java/com/pokedex/
│       ├── PokedexApplication.java   Spring Boot entry point
│       ├── config/WebConfig.java     CORS & static files
│       ├── controller/PokemonController.java  REST API
│       ├── service/PokeApiService.java        PokeAPI client
│       ├── cache/LRUCache.java       LRU cache implementation
│       ├── dto/                      Data transfer objects
│       └── exception/                Error handling
├── client/                           React frontend
│   └── src/
│       ├── App.jsx                   Main app component
│       ├── components/
│       │   ├── SearchBar.jsx         Autocomplete search
│       │   └── PokemonCard.jsx       Detail view
│       └── styles/App.css            All styles
└── README.md
```

## Caching

The server uses an in-memory LRU (Least Recently Used) cache:
- **Max entries**: 200
- **TTL**: 10 minutes
- Evicts oldest entries when full
- Separate cache for search results and Pokemon details
- Thread-safe via synchronized access
