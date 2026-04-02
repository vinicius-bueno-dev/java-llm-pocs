export function Footer() {
  return (
    <footer className="app-footer">
      <div className="app-footer__inner">
        <div className="app-footer__left">
          <span className="app-footer__brand">java-llm<span>-pocs</span></span>
          <p className="app-footer__description">
            POCs para estudo de Claude Code, LocalStack e Terraform com Java 21.
          </p>
        </div>
        <div className="app-footer__links">
          <div className="app-footer__col">
            <h4 className="app-footer__col-title">Recursos</h4>
            <a href="https://github.com/vinicius-bueno-dev/java-llm-pocs" target="_blank" rel="noopener noreferrer">
              GitHub
            </a>
            <a href="https://docs.localstack.cloud/" target="_blank" rel="noopener noreferrer">
              LocalStack Docs
            </a>
            <a href="https://developer.hashicorp.com/terraform/docs" target="_blank" rel="noopener noreferrer">
              Terraform Docs
            </a>
          </div>
          <div className="app-footer__col">
            <h4 className="app-footer__col-title">Stack</h4>
            <span>Java 21 + Spring Boot</span>
            <span>AWS SDK v2</span>
            <span>React + Vite</span>
          </div>
        </div>
      </div>
      <div className="app-footer__bottom">
        <span>Feito com React + Vite + TypeScript</span>
      </div>
    </footer>
  )
}
