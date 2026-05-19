const STAT_LABELS = {
  hp: 'HP', attack: 'Atk', defense: 'Def',
  'special-attack': 'SpA', 'special-defense': 'SpD', speed: 'Spe'
};

function metersToFeetInches(m) {
  const totalInches = m * 39.3701;
  const feet = Math.floor(totalInches / 12);
  const inches = Math.round(totalInches % 12);
  return `${feet}'${inches}"`;
}

function kgToLbs(kg) {
  return (kg * 2.20462).toFixed(1);
}

export default function PokemonCard({ pokemon, onBack }) {
  if (!pokemon) return null;

  const { data } = pokemon;

  return (
    <>
      {onBack && (
        <button className="back-btn" onClick={onBack}>
          &larr; Back to all
        </button>
      )}
    <div className="pokemon-card">
      <div className="pokemon-header">
        <img
          className="pokemon-sprite"
          src={data.sprite}
          alt={data.name}
          loading="lazy"
        />
        <div className="pokemon-info">
          <div className="pokemon-id">#{String(data.id).padStart(4, '0')}</div>
          <h2>{data.name.replace(/-/g, ' ')}</h2>
          {data.genus && <div className="pokemon-genus">{data.genus}</div>}
          <div className="type-badges">
            {data.types.map(t => (
              <span key={t} className={`type-badge type-${t}`}>{t}</span>
            ))}
          </div>
        </div>
      </div>

      <div className="pokemon-body">
        <div className="section">
          <div className="section-title">Base Stats</div>
          {data.stats.map(s => (
            <div key={s.name} className="stat-bar">
              <span className="stat-name">{STAT_LABELS[s.name] || s.name}</span>
              <span className="stat-value">{s.value}</span>
              <div className="stat-track">
                <div className="stat-fill" style={{ width: `${Math.min(100, (s.value / 255) * 100)}%` }} />
              </div>
            </div>
          ))}
        </div>

        <div className="section">
          <div className="section-title">Abilities</div>
          {data.abilities.map(a => (
            <div key={a.name} className="ability-item">
              <span>{a.name.replace(/-/g, ' ')}</span>
              {a.isHidden && <span className="ability-hidden">Hidden</span>}
            </div>
          ))}
        </div>

        <div className="section">
          <div className="section-title">Measurements</div>
          <div className="measurements">
            <div className="measurement">
              <div className="measurement-value">{data.height / 10} m</div>
              <div className="measurement-label">
                Height ({metersToFeetInches(data.height / 10)})
              </div>
            </div>
            <div className="measurement">
              <div className="measurement-value">{data.weight / 10} kg</div>
              <div className="measurement-label">
                Weight ({kgToLbs(data.weight / 10)} lbs)
              </div>
            </div>
          </div>
        </div>

        {data.flavorText && (
          <div className="section full-width">
            <div className="section-title">Description</div>
            <p className="flavor-text">{data.flavorText}</p>
          </div>
        )}
      </div>
    </div>
    </>
  );
}
