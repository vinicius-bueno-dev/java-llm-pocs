export function TerraformFlow() {
  const steps = [
    { label: 'Developer', icon: 'user', x: 0 },
    { label: 'Terraform\nCLI', icon: 'terminal', x: 130 },
    { label: 'AWS\nProvider', icon: 'cloud', x: 260 },
    { label: 'LocalStack\n(Docker)', icon: 'container', x: 390 },
    { label: 'Spring Boot\nApp', icon: 'app', x: 520 },
  ]

  return (
    <div className="diagram" style={{ margin: 'var(--space-6) 0', overflowX: 'auto' }}>
      <svg
        viewBox="0 0 620 100"
        fill="none"
        style={{ width: '100%', maxWidth: 620, display: 'block', margin: '0 auto' }}
      >
        <defs>
          <marker id="flow-arrow" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
            <polygon points="0 0, 8 3, 0 6" fill="var(--color-primary)" />
          </marker>
        </defs>

        {steps.map((step, i) => (
          <g key={step.label}>
            {/* Box */}
            <rect
              x={step.x}
              y="20"
              width="100"
              height="60"
              rx="10"
              fill={i === 3 ? 'var(--color-primary-container)' : 'var(--color-surface-variant)'}
              stroke={i === 3 ? 'var(--color-primary)' : 'var(--color-outline)'}
              strokeWidth="1.5"
            />
            {/* Label */}
            {step.label.split('\n').map((line, li) => (
              <text
                key={li}
                x={step.x + 50}
                y={step.label.includes('\n') ? 46 + li * 16 : 54}
                textAnchor="middle"
                fill={i === 3 ? 'var(--color-primary)' : 'var(--color-on-surface)'}
                fontFamily="var(--font-body)"
                fontSize="12"
                fontWeight="500"
              >
                {line}
              </text>
            ))}
            {/* Arrow */}
            {i < steps.length - 1 && (
              <line
                x1={step.x + 100}
                y1="50"
                x2={steps[i + 1].x}
                y2="50"
                stroke="var(--color-primary)"
                strokeWidth="1.5"
                markerEnd="url(#flow-arrow)"
              />
            )}
          </g>
        ))}

        {/* Protocol labels */}
        <text x="165" y="16" textAnchor="middle"
          fill="var(--color-on-surface-variant)" fontFamily="var(--font-mono)" fontSize="9">
          HCL
        </text>
        <text x="295" y="16" textAnchor="middle"
          fill="var(--color-on-surface-variant)" fontFamily="var(--font-mono)" fontSize="9">
          API calls
        </text>
        <text x="425" y="16" textAnchor="middle"
          fill="var(--color-on-surface-variant)" fontFamily="var(--font-mono)" fontSize="9">
          :4566
        </text>
        <text x="555" y="16" textAnchor="middle"
          fill="var(--color-on-surface-variant)" fontFamily="var(--font-mono)" fontSize="9">
          SDK v2
        </text>
      </svg>
    </div>
  )
}
