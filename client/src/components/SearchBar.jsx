import { useState, useEffect, useRef, useCallback } from 'react';

export default function SearchBar({ onSelect }) {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [activeIdx, setActiveIdx] = useState(-1);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef(null);
  const timerRef = useRef(null);

  const fetchSuggestions = useCallback(async (q) => {
    if (q.length < 1) { setSuggestions([]); return; }
    setLoading(true);
    try {
      const res = await fetch(`/api/pokemon?q=${encodeURIComponent(q)}`);
      const json = await res.json();
      if (json.success) setSuggestions(json.data);
      else setSuggestions([]);
    } catch {
      setSuggestions([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => fetchSuggestions(query), 200);
    return () => { if (timerRef.current) clearTimeout(timerRef.current); };
  }, [query, fetchSuggestions]);

  const handleKeyDown = (e) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIdx(i => Math.min(i + 1, suggestions.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIdx(i => Math.max(i - 1, 0));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (activeIdx >= 0 && suggestions[activeIdx]) {
        handleSelect(suggestions[activeIdx].name);
      } else if (query.trim()) {
        handleSelect(query.trim());
      }
    } else if (e.key === 'Escape') {
      setSuggestions([]);
    }
  };

  const handleSelect = (name) => {
    setQuery(name);
    setSuggestions([]);
    setActiveIdx(-1);
    onSelect(name);
    inputRef.current?.blur();
  };

  return (
    <div className="search-wrapper">
      <input
        ref={inputRef}
        className="search-input"
        type="text"
        placeholder="Search Pokemon... e.g. pikachu"
        value={query}
        onChange={e => { setQuery(e.target.value); setActiveIdx(-1); }}
        onKeyDown={handleKeyDown}
        onFocus={() => { if (suggestions.length) setSuggestions([...suggestions]); }}
      />
      {suggestions.length > 0 && (
        <ul className="suggestions">
          {suggestions.map((p, i) => (
            <li
              key={p.name}
              className={i === activeIdx ? 'active' : ''}
              onMouseDown={() => handleSelect(p.name)}
              onMouseEnter={() => setActiveIdx(i)}
            >
              {p.name.replace(/-/g, ' ')}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
