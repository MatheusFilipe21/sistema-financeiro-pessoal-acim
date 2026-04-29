# Base image oficial do PostgreSQL 18 (leve).
FROM postgres:18-alpine

# Instalação de utilitários de sistema:
RUN apk update && apk add --no-cache sed

# Configuração de Inicialização do Banco de Dados:
# 1. Copia o script mestre que cria o banco de teste e habilita extensões
# 2. Copia o schema oficial do backend para garantir paridade entre ambientes
# Os arquivos em /docker-entrypoint-initdb.d/ são executados automaticamente no boot.
COPY ./scripts/sql/iniciar-banco-teste.sql /docker-entrypoint-initdb.d/
COPY ./backend/src/main/resources/schema.sql /docker-entrypoint-initdb.d/

# Ajuste de Compatibilidade e Permissões:
# 1. Aplica permissão de execução nos scripts SQL
# 2. Remove caracteres residuais do Windows (\r) que costumam quebrar a execução no Linux
RUN chmod +x /docker-entrypoint-initdb.d/*.sql \
    && sed -i 's/\r$//' /docker-entrypoint-initdb.d/*.sql

# Limpeza de cache de pacotes para reduzir o tamanho final da imagem
RUN rm -rf /var/cache/apk/*
