# Base image com Java 25 pré-instalado
FROM mcr.microsoft.com/devcontainers/java:25

# Define que não haverá interação via terminal durante o build
ENV DEBIAN_FRONTEND=noninteractive

# Remove o Yarn legado que costuma dar erro de chave GPG expirada
RUN rm -f /etc/apt/sources.list.d/yarn.list

# Instalação de Ferramentas de Sistema:
# maven: Build Java | git-flow: Metodologia Git | vim/tree/lsof: Utilitários de terminal
# gnupg/ca-certificates: Necessários para segurança e chaves de repositórios
RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    git-flow \
    vim \
    tree \
    lsof \
    wget \
    gnupg \
    ca-certificates \
    && apt-get clean

# Instalação do Google Chrome (Necessário para testes Selenium/E2E):
# 1. Baixa a chave de assinatura do Google
# 2. Adiciona o repositório oficial do Chrome
# 3. Instala o navegador
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome-keyring.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*
