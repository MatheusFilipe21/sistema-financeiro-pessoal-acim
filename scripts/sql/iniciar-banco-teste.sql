-- Cria o banco de testes apenas se ele não existir
SELECT 'CREATE DATABASE sfpacim_db_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sfpacim_db_test')\gexec
