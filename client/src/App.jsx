import { useState, useCallback } from 'react';
import SearchBar from './components/SearchBar';
import PokemonCard from './components/PokemonCard';

export default function App() {
  const [pokemon, setPokemon] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchPokemon = useCallback(async (name) => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/pokemon/${encodeURIComponent(name)}`);
      const json = await res.json();
      if (json.success) {
        setPokemon(json);
      } else {
        setError(json.error || 'Failed to fetch pokemon');
        setPokemon(null);
      }
    } catch {
      setError('Network error. Is the server running?');
      setPokemon(null);
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <div className="app">
      <header className="header">
        <h1><span>Poké</span>dex</h1>
        <p>Search and discover Pokemon</p>
      </header>

      <div className="search-section">
        <SearchBar onSelect={fetchPokemon} />
      </div>

      {loading && (
        <div className="spinner">
          <div className="spinner-ring" />
          <span className="spinner-text">Searching...</span>
        </div>
      )}

      {error && (
        <div className="error-banner">
          <div className="error-icon">&#9888;</div>
          <p>{error}</p>
        </div>
      )}

      {!loading && pokemon && <PokemonCard pokemon={pokemon} onBack={() => setPokemon(null)} />}

      {!loading && !error && !pokemon && (
        <div className="hero">
          <div className="hero-logo">
            <svg viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="100" cy="100" r="95" stroke="#ef5350" strokeWidth="6"/>
              <circle cx="100" cy="100" r="90" fill="#1a1a2e"/>
              <rect x="5" y="95" width="190" height="10" fill="#ef5350"/>
              <circle cx="100" cy="100" r="35" fill="white" stroke="#ef5350" strokeWidth="4"/>
              <circle cx="100" cy="100" r="18" fill="#ef5350"/>
              <circle cx="100" cy="50" r="12" fill="#ef5350"/>
              <circle cx="150" cy="100" r="12" fill="#ef5350"/>
              <circle cx="100" cy="150" r="12" fill="#ef5350"/>
              <circle cx="50" cy="100" r="12" fill="#ef5350"/>
            </svg>
          </div>
          <h2 className="hero-title">Discover Pokemon</h2>
          <p className="hero-sub">Type a name above to explore stats, abilities, and more</p>
          <div className="hero-hints">
            <span onClick={() => fetchPokemon('pikachu')}>pikachu</span>
            <span onClick={() => fetchPokemon('charizard')}>charizard</span>
            <span onClick={() => fetchPokemon('mewtwo')}>mewtwo</span>
            <span onClick={() => fetchPokemon('gengar')}>gengar</span>
            <span onClick={() => fetchPokemon('eevee')}>eevee</span>
          </div>
        </div>
      )}

      <footer className="footer">
        Data sourced from <a href="https://pokeapi.co/" target="_blank" rel="noopener noreferrer">PokeAPI</a>
      </footer>
    </div>
  );
}
