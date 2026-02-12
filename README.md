# 💰 Sistema Financeiro Pessoal ACIM (SFP-ACIM)

O SFP-ACIM é uma plataforma modular de gestão financeira pessoal desenvolvida para oferecer controle detalhado sobre receitas e despesas. Este projeto utiliza uma arquitetura moderna de monorepo.

---

## 🛠️ Stack Tecnológica

Nosso monorepo é construído com as seguintes tecnologias principais:

| Componente          | Tecnologia                 | Versão   | Propósito                                                |
| :------------------ | :------------------------- | :------- | :------------------------------------------------------- |
| **Backend**         | Java / Spring Boot         | 21 / 3.x | API de negócios (Controladores, Serviços, Persistência). |
| **Frontend**        | Angular                    | v20      | Interface de usuário web.                                |
| **Database**        | PostgreSQL                 | 18       | Armazenamento de dados.                                  |
| **Testes E2E**      | Python / Selenium / Behave | 3.12     | Validação de fluxo ponta-a-ponta (BDD).                  |
| **Containerização** | Docker Compose             |          | Ambiente de desenvolvimento local e CI/CD.               |

## 🏗️ Arquitetura e Padrões

O projeto segue um modelo de **Monorepo** com separação clara de responsabilidades:

1.  **Estrutura de Repositórios:** `backend/`, `frontend/`, `e2e/`.
2.  **Qualidade:** JaCoCo, Jasmine (Cobertura) e SonarQube (Análise Estática).
3.  **Testes:** Implementação de uma Pirâmide de Testes Completa.

---

## 🧪 Estratégia de Testes (Quality Gate)

A qualidade é aplicada em três camadas:

- **Unitário/Slice (Backend):** Usando JUnit e MockMvc para testar a lógica dos **Repositórios, Serviços e Controladores** de forma isolada e rápida.
- **Integração/BDD (API):** Usando **Cucumber e Rest Assured** para validar o comportamento dos **fluxos de negócio** e a comunicação entre as camadas da API.
- **E2E (Ponta-a-Ponta):** Usando **Selenium e Behave (Python)** para automatizar os cenários Gherkin no navegador (Chrome Headless), validando a comunicação completa entre o Frontend e o Backend.

---

## 🚀 Pipeline de CI/CD (GitHub Actions)

O projeto é validado por uma pipeline de Integração Contínua (CI) definida em `.github/workflows/pipeline.yml`.

O pipeline é disparado automaticamente em `push` (para `main`/`develop`) ou `pull_request` (para `develop`) e executa três jobs sequenciais para garantir a qualidade do monorepo:

1.  **Job 1: Backend (Java):**
    - Compila o Spring Boot.
    - Roda `mvn verify` (JUnit, BDD/Cucumber).
    - Gera e armazena os relatórios (JaCoCo, Surefire).

2.  **Job 2: Frontend (Angular):**
    - Instala o Chrome Headless.
    - Roda `npm ci` e `npm test` (Karma/Jasmine).
    - Gera e armazena os relatórios (LCOV, JUnit XML).

3.  **Job 3: E2E e Quality Gate (Docker + Sonar):**
    - Espera os Jobs 1 e 2 terminarem com sucesso.
    - Inicia a aplicação completa (Postgres, Backend, Frontend) usando `docker compose up --build`.
    - Roda os testes E2E (Behave/Selenium) contra a aplicação containerizada.
    - (Se o E2E passar) Envia uma análise combinada (Java + TS) para o SonarQube Cloud para validar o Quality Gate.

---

## 💻 Setup do Ambiente de Desenvolvimento (DevContainer)

O ambiente está 100% configurado para VS Code/Docker. Para começar, você precisa apenas do **Docker** e do **VS Code** com a extensão **Dev Containers**.

### 1. Pré-requisitos (Instalação do Docker)

Se ainda não possui o Docker instalado e configurado, expanda a seção correspondente ao seu sistema operacional abaixo:

<details>
<summary><strong>🪟 Clique aqui para instruções Windows (WSL2 + Ubuntu)</strong></summary>

Para garantir performance e compatibilidade, utilizaremos o Docker Desktop integrado a uma distribuição Linux dedicada (Ubuntu 24.04) rodando sobre o WSL2.

1.  **Habilitar WSL2:**
    Abra o PowerShell como Administrador e execute os comandos abaixo para instalar e garantir a versão 2:

    ```powershell
    wsl --install
    wsl --set-default-version 2
    ```

    _Reinicie o computador se solicitado._

2.  **Instalar a Distribuição Linux (Ubuntu):**
    - Abra a **Microsoft Store**.
    - Procure por **"Ubuntu 24.04 LTS"** (ou sua versão preferida) e instale.
    - Após instalar, **abra o terminal do Ubuntu** uma vez para finalizar a configuração criando seu usuário e senha UNIX.
    - **Importante:** Volte ao PowerShell e defina este Ubuntu como o padrão do sistema:
      ```powershell
      wsl --set-default Ubuntu-24.04
      ```

3.  **Instalar e Configurar Docker Desktop:**
    - Baixe e instale o [Docker Desktop](https://www.docker.com/products/docker-desktop/).
    - **Configuração Geral:** Nas configurações (_Settings_) -> _General_, certifique-se de que **"Use the WSL 2 based engine"** está marcado.
    - **Integração com a Distro:**
      1. Vá em _Settings_ -> _Resources_ -> _WSL Integration_.
      2. Marque a opção **"Enable integration with my default WSL distro"**.
      3. Clique em _Apply & Restart_.
         _(Como definimos o Ubuntu como padrão no passo anterior, ele será automaticamente integrado)._

4.  **Validação:**
    Abra o terminal do seu **Ubuntu 24.04** e digite `docker ps`. Se não der erro de conexão, o ambiente está pronto.

</details>

<details>
<summary><strong>🐧 Clique aqui para instruções Linux</strong></summary>

1.  **Atualizar pacotes:**
    ```bash
    sudo apt-get update
    sudo apt-get install ca-certificates curl gnupg
    ```
2.  **Instalar Docker Engine:**
    ```bash
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    ```
3.  **Configurar permissões (para rodar sem `sudo`):**
    ```bash
    sudo usermod -aG docker $USER
    newgrp docker
    ```

</details>

### 2. Inicialização e Clonagem

Dependendo do seu sistema operacional, o método para baixar (clonar) o projeto muda. **Siga rigorosamente os passos abaixo para evitar problemas de permissão e performance.**

<details>
<summary><strong>🪟 Windows (Fluxo via WSL2 - Obrigatório)</strong></summary>

No Windows, **NÃO** clone o projeto na sua Área de Trabalho ou Documentos. Você deve clonar dentro do sistema de arquivos do Linux.

1.  Abra o **VS Code** no Windows.
2.  Clique no botão verde/azul no canto inferior esquerdo (**Remote Window**) ou pressione `F1` e selecione:
    - `WSL: Connect to WSL using Distro...`
    - Escolha **Ubuntu-24.04**.
3.  Uma nova janela do VS Code abrirá conectada ao Linux. Abra o terminal integrado (`Ctrl + J`) e clone o projeto na sua pasta home:

    ```bash
    cd ~
    # Clona o repositório
    git clone https://github.com/MatheusFilipe21/sistema-financeiro-pessoal-acim

    # Entra na pasta e muda para a branch de desenvolvimento
    cd sistema-financeiro-pessoal-acim
    git checkout develop
    code .
    ```

</details>

<details>
<summary><strong>🐧 Linux (Fluxo Nativo)</strong></summary>

No Linux nativo, o processo é direto.

1.  Abra seu terminal e execute:

    ```bash
    # Clona o repositório
    git clone https://github.com/MatheusFilipe21/sistema-financeiro-pessoal-acim

    # Entra na pasta e muda para a branch de desenvolvimento
    cd sistema-financeiro-pessoal-acim
    git checkout develop

    # Abre o projeto no VS Code
    code .
    ```

</details>

---

### 3. Ativando o DevContainer

Assim que você abrir a pasta do projeto no VS Code (seguindo os passos acima):

1.  O editor detectará os arquivos de configuração `.devcontainer` e exibirá uma notificação no canto inferior direito:
    > **"Folder contains a Dev Container configuration file. Reopen to develop in a container."**
2.  Clique no botão **Reopen in Container**.
    - _Caso a notificação não apareça:_ Pressione `F1` e digite/selecione `Dev Containers: Reopen in Container`.
3.  Aguarde a construção do ambiente (pode demorar alguns minutos na primeira vez enquanto baixa as imagens do Java, Node, Chrome, etc).

### ⚠️ Otimização de Performance (Recomendado para máquinas com 8GB de RAM)

Se o seu computador apresentar lentidão ou travamentos ao rodar o projeto, siga os ajustes abaixo. Eles reduzem o consumo das ferramentas do VS Code e do Docker, garantindo que o navegador e o sistema operacional continuem fluidos.

#### 1. Limitar recursos do Docker (Apenas Windows)

O Docker Desktop pode reservar até 50% da sua RAM total, o que causa lentidão no Windows. Para limitar isso:

1. Pressione `Win + R`, digite `%USERPROFILE%` e dê Enter.
2. Crie (ou edite) um arquivo chamado `.wslconfig`.
3. Adicione o seguinte conteúdo:
   ```ini
   [wsl2]
   memory=4GB   # Limita o Linux a 4GB de RAM
   processors=4 # Limita o uso de CPU (evita 100% de uso no build)
   ```
4. Reinicie o Docker Desktop para aplicar.

### 4. Comandos Principais no VS Code (Run and Debug)

O fluxo de trabalho no VS Code é dividido em dois menus principais:

1.  **Menu "Run and Debug" (▶️):** Usado para iniciar **servidores** ou processos de **depuração** (debug).
2.  **Menu "Tasks" (Tarefas - `Ctrl+Shift+B`):** Usado para executar **scripts** que rodam e terminam (como builds ou testes).

| Tarefa                            | Descrição                                                                                       | Como Executar                                             |
| :-------------------------------- | :---------------------------------------------------------------------------------------------- | :-------------------------------------------------------- |
| **Backend (Iniciar)**             | Inicia a API Spring Boot (porta 8080).                                                          | Menu "Run and Debug" (▶️) -> **Spring Boot (Backend)**    |
| **Frontend (Iniciar)**            | Inicia o servidor Angular (porta 4200) com proxy para o backend.                                | Menu "Run and Debug" (▶️) -> **Angular (Frontend)**       |
| **Testes de Frontend**            | Roda os testes (Karma) em modo "watch" (observação) na porta 9876.                              | Menu "Run and Debug" (▶️) -> **Angular (Testes)**         |
| **Testes E2E (Debug)**            | Executa os testes Selenium/Python com o depurador anexado (permite breakpoints).                | Menu "Run and Debug" (▶️) -> **Selenium (E2E)**           |
| **Testes E2E (Tag @qa)**          | Executa apenas os cenários marcados com a tag `@qa`. Útil para desenvolvimento focado.          | Menu "Run and Debug" (▶️) -> **Selenium (E2E - Tag @qa)** |
| **Testes de Backend (Unitários)** | Roda/Depura testes unitários (JUnit) individualmente através da interface gráfica.              | **Aba "Testing" (🧪)** -> Selecionar e Rodar o teste.     |
| **Testes de Backend (Completo)**  | Roda `mvn clean verify`: testes JUnit, BDD (Cucumber), Rest Assured, e gera o relatório JaCoCo. | Menu `Terminal > Run Task...` -> **Spring Boot (Testes)** |

---

## 🐳 Ambiente de Produção Local (Docker Compose)

Para simular o ambiente de produção/deploy completo (com Nginx, JRE, etc.), você pode usar o `docker-compose.yml`.

**Este ambiente roda em portas distintas do ambiente de desenvolvimento (ex: 8081, 4201, 5433), garantindo que não haja conflito com as portas padrão (8080, 4200) usadas pelo DevContainer.**

### 1. Configuração de Segurança (.env)

Antes de iniciar, crie um arquivo chamado `.env` na raiz do projeto.
Abaixo estão todas as variáveis suportadas. **Apenas as credenciais de E-mail são obrigatórias**; as demais possuem valores padrão configurados no `docker-compose.yml` e podem permanecer comentadas para uso local.

```properties
# === E-MAIL (Obrigatório) ===
MAIL_USERNAME=SUBSTITUIR_PELO_E_MAIL_DO_GOOGLE
MAIL_PASSWORD=SUBSTITUIR_PELA_SENHA_DE_APP
# Se mantido comentado, o sistema usará: smtp.gmail.com / 587
# MAIL_HOST=smtp.gmail.com
# MAIL_PORT=587
# MAIL_AUTH=true
# MAIL_STARTTLS=true

# === BANCO DE DADOS (Opcional) ===
# Se mantido comentado, o sistema usará: sfpacim_db / user / password
# POSTGRES_DB=sfpacim_db
# POSTGRES_USER=admin_sfp
# POSTGRES_PASSWORD=senha_segura_sfp

# === SEGURANÇA JWT (Opcional) ===
# Se mantido comentado, o sistema usará um hash padrão de desenvolvimento.
# JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
# JWT_EXPIRATION=28800000

# === CONFIGURAÇÕES GERAIS (Opcional) ===
# APP_FRONTEND_URL=http://localhost:4201
# JPA_DDL_AUTO=update
# SHOW_SQL=false
```

> **🔑 Como obter a Senha de App (Google Workspace/Gmail):**
>
> 1. Acesse **Gerenciar sua Conta do Google** -> **Segurança**.
> 2. Garanta que a **"Verificação em duas etapas"** esteja ATIVADA.
> 3. Na barra de busca da conta, digite **"Senhas de app"**.
> 4. Crie uma nova senha com o nome "SFP-Local".
> 5. Copie a senha de 16 caracteres gerada e cole no campo `MAIL_PASSWORD` acima.

### 2. Execução

1.  No seu terminal (na raiz do projeto), execute:
    ```bash
    docker compose up --build
    ```
    > **Observação (VS Code):** A extensão **"Containers"**, que já está no DevContainer, oferece atalhos visuais para esta operação. Ela adiciona um **Painel Containers** na barra lateral esquerda, permitindo que você inicie/pare/inspecione os containers.
2.  Acesse a aplicação em: **`http://localhost:4201`** (a porta 80 do Nginx é mapeada para a 4201).
3.  Para parar todos os serviços, pressione `Ctrl+C` ou rode:
    ```bash
    docker compose down
    ```

---

**(C) 2025 Autoria Coletiva: Alexandre Orlando Gracio, Catherine Marie Cavalcanti Aussourd, Ilka Fernanda Berenguer Paz, Matheus Filipe do Nascimento Pereira.**
Todos os direitos reservados. Este código não possui licença de código aberto e os direitos de propriedade intelectual são retidos pelos coautores. **Proibida a reprodução, distribuição ou qualquer uso comercial do software sem permissão expressa e por escrito dos autores.**
