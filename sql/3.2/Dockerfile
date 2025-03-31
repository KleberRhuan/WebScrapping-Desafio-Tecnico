FROM postgres:17-alpine

LABEL maintainer="Kleber Rhuan - Intuitive Care(Teste Tecnico)"
LABEL description="Banco de dados PostgreSQL com configurações otimizadas"

# Instalar dependências usando o gerenciador de pacotes Alpine
RUN apk add --no-cache curl

# Configurações otimizadas para PostgreSQL
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=intuitive_care
# Otimizações de performance
ENV POSTGRES_SHARED_BUFFERS=256MB
ENV POSTGRES_WORK_MEM=16MB
ENV POSTGRES_MAINTENANCE_WORK_MEM=64MB
# Configurações adicionais para melhor performance
ENV POSTGRES_EFFECTIVE_CACHE_SIZE=768MB
ENV POSTGRES_MAX_CONNECTIONS=100

# Criar diretório para dados temporários
RUN mkdir -p /tmp/data/

# Copiar scripts SQL para inicialização do banco
# Os scripts serão executados em ordem alfabética
COPY ./sql/3.2/*.sql /docker-entrypoint-initdb.d/

# Script de inicialização personalizado para configurações adicionais
COPY <<EOF /docker-entrypoint-initdb.d/00_configure.sh
#!/bin/sh
set -e

# Espera o PostgreSQL iniciar completamente
until pg_isready -U postgres; do
  echo "Aguardando PostgreSQL iniciar..."
  sleep 1
done

# Executa otimizações adicionais se necessário
echo "Configurando parâmetros otimizados do PostgreSQL..."
psql -v ON_ERROR_STOP=1 --username "\$POSTGRES_USER" --dbname "\$POSTGRES_DB" <<-EOSQL
  ALTER SYSTEM SET synchronous_commit = 'off';
  ALTER SYSTEM SET random_page_cost = 1.1;
  ALTER SYSTEM SET effective_io_concurrency = 200;
  SELECT pg_reload_conf();
EOSQL

echo "Configuração do PostgreSQL concluída!"
EOF

# Dar permissão de execução ao script
RUN chmod +x /docker-entrypoint-initdb.d/00_configure.sh

# Expor a porta padrão do PostgreSQL
EXPOSE 5432

# Informações de saúde
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD pg_isready -U postgres || exit 1
