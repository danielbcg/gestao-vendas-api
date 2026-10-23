# 💎 Gestão Fácil de Vendas — Loja da Maria

Aplicação funcional de ponta a ponta desenvolvida especialmente para ajudar a **Maria**, proprietária de uma loja de acessórios, a organizar suas vendas que antes eram registradas à mão em uma planilha Excel desorganizada.

A aplicação importa planilhas Excel manuais, padroniza todos os dados através de regras determinísticas, aponta exatamente o que precisa de conferência, permite registrar novas vendas por um formulário prático com opções prontas, lista e filtra as vendas em tempo real com cálculo automático de totais, permite exportar os dados limpos de volta para o Excel e conta com suporte de Inteligência Artificial (Google Gemini) para sugerir correções de forma segura e controlada.

---

## 👩‍💼 Guia de Uso da Maria (Passo a Passo Simples)

Olá, Maria! Preparamos este sistema para ser o mais simples e direto possível, sem termos difíceis:

### 1. Como abrir o sistema
- Abra seu navegador de internet e acesse: [http://localhost:5173](http://localhost:5173)

### 2. Importando sua planilha antiga (Aba "📥 Importar Planilha")
- Clique na aba **"Importar Planilha"** no topo da tela.
- Clique em **"Escolher arquivo"** e selecione a sua planilha (por exemplo, `vendas_maria.xlsx`).
- Clique no botão **"Importar planilha"**.
- O sistema vai ler todas as linhas e mostrar 3 cartões coloridos:
  - **Vendas importadas**: tudo o que o sistema conseguiu entender e arrumar sozinho (datas, valores, produtos).
  - **Precisam de conferência**: linhas onde faltou alguma informação ou que estavam confusas (por exemplo, anotações como "depois paga", datas como "segunda", ou valores com frete somado).
  - **Linhas ignoradas**: linhas que estavam totalmente vazias ou eram vendas repetidas idênticas.
- Para cada item que precisa de conferência, basta clicar no botão **"Conferir"**:
  - Se a Inteligência Artificial estiver ativada, você verá o botão **"✨ Pedir sugestão da Inteligência Artificial"**.
  - O sistema mostra: *Na planilha estava X*, *Sugestão Y*, *Por quê*.
  - Você pode clicar em **"Usar essa sugestão"** ou **"Deixar como está"** e preencher do seu jeito.
  - Ao clicar em **"Salvar e Confirmar"**, a venda é corrigida e sai da lista de pendências!

### 3. Registrando uma venda no dia a dia (Aba "➕ Nova Venda")
- Clique na aba **"Nova Venda"**.
- A data de hoje e o tipo "Venda" já vêm preenchidos para adiantar seu lado.
- No campo **Produto**, você pode escolher um acessório que já vendeu antes na lista, ou escolher **"➕ Cadastrar novo produto..."** e digitar o nome na hora.
- Digite o **Valor**.
- Escolha **Como Pagou** (Pix, Dinheiro, Cartão, Fiado, etc.). Se for Cartão, o sistema abre as opções de Bandeira e Parcelas.
- Escolha a **Entrega** (Loja, Motoboy, Correios, Ela veio buscar, etc.). Se for Correios ou Sedex, o sistema pede o código de rastreio para você não esquecer.
- Clique em **"Salvar venda"**. Pronto! Formulário limpo e venda registrada.

### 4. Consultando suas vendas e exportando (Aba "📋 Vendas Registradas")
- Veja no topo o **Total de Vendas**, **Total de Devoluções** e o **Total Líquido**.
- Use os filtros para encontrar vendas por período de datas, por cliente, por forma de pagamento ou por produto.
- Clique no botão verde **"📊 Exportar para Excel Padronizado"** a qualquer momento para baixar uma planilha limpa, organizada e pronta para o seu controle.

---

## 💻 Guia Técnico para o Desenvolvedor

### 1. Estrutura do Projeto
```text
gestao-vendas-api/
├── .gitignore                      # Regras do Git (ignora node_modules, dist, .env, etc.)
├── README.md                       # Documentação completa
├── data/
│   ├── vendas_maria.xlsx           # Planilha de exemplo original da Maria
│   └── vendasdb.mv.db              # Banco de dados H2 persistido em arquivo local
├── backend/                        # API REST em Spring Boot (Java 25, Spring Boot 4.1.2)
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/
│       ├── main/
│       │   ├── java/com/daniel/gestaovendasfacil/
│       │   │   ├── config/         # CORS e Serialização JSON
│       │   │   ├── controller/     # Endpoints REST (/api/vendas, /api/importacao, etc.)
│       │   │   ├── dto/            # Data Transfer Objects com Bean Validation
│       │   │   ├── model/          # Entidades JPA (Venda, Produto) e Enums
│       │   │   ├── repository/     # Repositories Spring Data com Specifications
│       │   │   └── service/        # Regras de negócio, Importação POI e GeminiService
│       │   │       └── normalizer/ # Normalizadores determinísticos testáveis
│       │   └── resources/
│       │       ├── application.properties
│       │       └── produtos-sinonimos.json
│       └── test/                   # Testes unitários e de integração JUnit 5
└── frontend/                       # Aplicação React SPA criada com Vite
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx                 # Componente raiz com navegação por abas
        ├── index.css               # Estilos modernos sem dependências pesadas
        ├── main.jsx
        └── components/
            ├── ImportarPlanilha.jsx
            ├── NovaVenda.jsx
            ├── VendasRegistradas.jsx
            └── ModalEditarVenda.jsx
```

### 2. Pré-requisitos
- **Java 25** instalado e configurado no `PATH`.
- **Node.js** (v18+) e **npm** instalados.

### 3. Como Rodar a Aplicação

#### Rodando o Backend (Spring Boot na porta 8080)
No terminal (PowerShell), entre na pasta `backend/` e execute:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
*(No Linux/macOS, use `./mvnw spring-boot:run`)*.

#### Rodando o Frontend (Vite na porta 5173)
Em outro terminal (PowerShell), entre na pasta `frontend/` e execute:
```powershell
cd frontend
npm install
npm run dev
```
Acesse a aplicação no navegador em: `http://localhost:5173`.

---

## 🔑 Configuração da Variável de Ambiente `GEMINI_API_KEY` (Windows PowerShell)

Para habilitar a integração com o Google Gemini (`gemini-flash-latest`), defina a variável no terminal antes de iniciar o backend:

### No Windows PowerShell (Sessão atual):
```powershell
$env:GEMINI_API_KEY="sua_chave_aqui"
cd backend
.\mvnw.cmd spring-boot:run
```

### No Windows PowerShell (Permanente para o usuário):
```powershell
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'sua_chave_aqui', 'User')
```

> **Nota de Segurança:** A chave da API é consumida exclusivamente via `${GEMINI_API_KEY:}` do ambiente, nunca foi exposta no código-fonte, nos arquivos de configuração ou em logs do sistema. Se a chave não for fornecida, o sistema opera normalmente em modo determinístico, informando que a IA não está configurada.

---

## 🤖 Como a IA foi Utilizada: Equilíbrio e Segurança

A integração com Inteligência Artificial foi desenhada seguindo princípios estritos de **Governança de Dados** e **Segurança**:

1. **A IA nunca salva nem altera dados sozinha:** Toda a padronização e ingestão da planilha é 100% determinística (executada por regras Java em `DateNormalizer`, `MoneyNormalizer`, `PaymentNormalizer`, `DeliveryNormalizer`, `ClientNormalizer` e `ProductNormalizer`).
2. **Onde a IA atua:** Exclusivamente no botão "Pedir sugestão da Inteligência Artificial" para auxiliar a Maria quando ela estiver em dúvida sobre uma linha marcada com pendência (`REVISAR`).
3. **Controle e Validação no Código:** A API chama o endpoint do Google Gemini (`gemini-flash-latest`) solicitando estritamente formato JSON (`campo`, `valorSugerido`, `explicacao`). Antes de entregar a sugestão para a tela:
   - O campo é validado contra a lista permitida (`data`, `valor`, `forma_pagamento`, `forma_entrega`, `cliente`, `produto`).
   - Datas são validadas para garantir formato de data real.
   - Valores são validados para garantir número decimal válido.
   - Formas de pagamento e entrega são checadas estritamente contra os Enums do sistema.
   - Qualquer resposta inválida é descartada imediatamente, exibindo mensagem amigável sem quebrar o fluxo.
   - Timeout de 15 segundos e retry automático único com intervalo de 3s para status 503/429.
4. **Decisão Humana Soberana (Human-in-the-Loop):** A Maria visualiza o valor que estava na planilha original, o valor sugerido e o porquê. Ela decide ativamente se clica em "Usar essa sugestão" ou "Deixar como está". O campo `sugestao_ia_aceita` armazena se a recomendação foi aceita para fins de rastreabilidade.

---

## 📦 Dependências Adicionadas

Conforme regras do projeto, mantiveram-se as dependências originais e foram adicionados apenas módulos essenciais para o ecossistema JSON com Java 25:
- `com.fasterxml.jackson.core:jackson-databind`: Para serialização e deserialização robusta de JSON na API REST e comunicação com o Gemini.
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310`: Para mapeamento nativo de tipos de data (`LocalDate`) do Java 8+ em JSON.

---

## 🧪 Testes Automatizados Executados (JUnit 5)

Foi implementada uma suíte abrangente de testes automatizados executada via Maven:

```powershell
cd backend
.\mvnw.cmd test
```

### Resultados dos Testes:
```text
[INFO] Running com.daniel.gestaovendasfacil.GeminiServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.daniel.gestaovendasfacil.GeminiServiceTest
[INFO] Running com.daniel.gestaovendasfacil.GestaoVendasFacilApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in com.daniel.gestaovendasfacil.GestaoVendasFacilApplicationTests
[INFO] Running com.daniel.gestaovendasfacil.ImportacaoIntegracaoTest
=== RELATÓRIO DE IMPORTAÇÃO ===
Total de linhas lidas: 71
Importadas automaticamente: 38
Importadas com REVISAR: 32
Ignoradas (duplicadas/vazias): 1
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in com.daniel.gestaovendasfacil.ImportacaoIntegracaoTest
[INFO] Running com.daniel.gestaovendasfacil.NormalizadoresTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in com.daniel.gestaovendasfacil.NormalizadoresTest
[INFO] Running com.daniel.gestaovendasfacil.ValidacaoVendaTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.daniel.gestaovendasfacil.ValidacaoVendaTest
[INFO] 
[INFO] Results:
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **NormalizadoresTest**: Casos reais cobertos: `"8/16/26"`, `"hj"`, `"ontem"`, `"R$ 35"`, `"28,00"`, `"35+12 frete"` (soma e revisão de frete), `"-40"` (devolução), `"credito 3x visa"`, `"maquinha"`, `"depois paga"`, `"ela veio buscar"`, telefone na coluna cliente, código de rastreio na coluna cliente com extração de `"era a Juliana"`, e mapa de sinônimos `"brinc argola"` -> `"brinco argola"`.
- **ImportacaoIntegracaoTest**: Importa a planilha real `vendas_maria.xlsx` de ponta a ponta, confirmando as contagens de importadas automáticas, revisões e duplicadas ignoradas.
- **ValidacaoVendaTest**: Valida que venda a crédito sem parcelas assume 1 parcela por padrão e que entrega via Correios/Sedex sem rastreio é rejeitada pela API com mensagem amigável.
- **GeminiServiceTest**: Valida via mock que respostas estruturadas válidas da IA são aceitas e que formatos inválidos ou campos estranhos são descartados com segurança.

---

## 🚀 Próximos Passos (O que ficou de fora por escopo)
1. **Autenticação e Perfis de Acesso (Login/JWT):** No momento o acesso é direto para facilitar o uso pela Maria em máquina local; um sistema multi-usuário com controle de permissões poderá ser incluído.
2. **Chat Conversacional Livre com IA:** A IA foi intencionalmente focada em sugestões pontuais de correção com validação estrita de esquema; um assistente estilo chat para perguntas e respostas gerais sobre o faturamento pode ser adicionado.
3. **Edição Direta do Arquivo Excel Original:** A aplicação preserva o arquivo Excel original intacto como histórico de entrada e gera novas planilhas padronizadas e limpas via `/api/exportacao`.
4. **Migração do Banco H2 para MySQL/PostgreSQL em Produção:** O projeto utiliza H2 persistido em arquivo local (`data/vendasdb.mv.db`) para rodar instantaneamente sem necessidade de instalar servidores de banco adicionais, com o driver MySQL já mapeado no POM para fácil ativação futura.
