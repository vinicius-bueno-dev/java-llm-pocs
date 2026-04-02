export function LocalStackArchitecture() {
  const services = [
    { name: 'S3', x: 30 },
    { name: 'SQS', x: 130 },
    { name: 'DynamoDB', x: 250 },
    { name: 'Lambda', x: 370 },
    { name: 'SNS', x: 460 },
    { name: 'IAM', x: 540 },
  ]

  return (
    <div className="diagram" style={{ margin: 'var(--space-6) 0', overflowX: 'auto' }}>
      <svg
        viewBox="0 0 620 220"
        fill="none"
        style={{ width: '100%', maxWidth: 620, display: 'block', margin: '0 auto' }}
      >
        {/* Gateway box */}
        <rect x="160" y="20" width="300" height="50" rx="12"
          fill="var(--color-primary-container)" stroke="var(--color-primary)" strokeWidth="1.5" />
        <text x="310" y="50" textAnchor="middle"
          fill="var(--color-primary)" fontFamily="var(--font-body)" fontSize="14" fontWeight="600">
          localhost:4566
        </text>

        {/* Connector lines */}
        {services.map((s) => (
          <line key={s.name}
            x1="310" y1="70"
            x2={s.x + 40} y2="130"
            stroke="var(--color-outline)" strokeWidth="1.5" strokeDasharray="4 3"
          />
        ))}

        {/* Service boxes */}
        {services.map((s) => (
          <g key={s.name} style={{ cursor: 'default' }}>
            <rect x={s.x} y="130" width="80" height="40" rx="8"
              fill="var(--color-surface-variant)" stroke="var(--color-outline)" strokeWidth="1" />
            <text x={s.x + 40} y="155" textAnchor="middle"
              fill="var(--color-on-surface)" fontFamily="var(--font-body)" fontSize="12" fontWeight="500">
              {s.name}
            </text>
          </g>
        ))}

        {/* Docker label */}
        <rect x="10" y="110" width="600" height="80" rx="12"
          fill="none" stroke="var(--color-outline-variant)" strokeWidth="1" strokeDasharray="6 4" />
        <text x="580" y="182" textAnchor="end"
          fill="var(--color-on-surface-variant)" fontFamily="var(--font-mono)" fontSize="10">
          Docker Container
        </text>

        {/* Arrow from top */}
        <line x1="310" y1="0" x2="310" y2="20"
          stroke="var(--color-primary)" strokeWidth="1.5" markerEnd="url(#arrowhead)" />
        <defs>
          <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
            <polygon points="0 0, 8 3, 0 6" fill="var(--color-primary)" />
          </marker>
        </defs>
        <text x="310" y="-4" textAnchor="middle"
          fill="var(--color-on-surface-variant)" fontFamily="var(--font-body)" fontSize="11">
          HTTP requests
        </text>
      </svg>
    </div>
  )
}
