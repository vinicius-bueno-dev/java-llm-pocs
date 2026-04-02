import type { ReactNode } from 'react'

interface TableProps {
  headers: string[]
  rows: ReactNode[][]
}

export function Table({ headers, rows }: TableProps) {
  return (
    <table className="styled-table">
      <thead>
        <tr>
          {headers.map((h, i) => (
            <th key={i}>{h}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, i) => (
          <tr key={i}>
            {row.map((cell, j) => (
              <td key={j}>{cell}</td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  )
}
