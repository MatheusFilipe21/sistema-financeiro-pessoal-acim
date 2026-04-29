-- Cria o banco de testes apenas se ele não existir
SELECT 'CREATE DATABASE sfpacim_db_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sfpacim_db_test')\gexec

-- Conecta no banco de testes para aplicar as configurações locais
\c sfpacim_db_test

-- Executa o schema oficial do projeto
\i /docker-entrypoint-initdb.d/schema.sql
