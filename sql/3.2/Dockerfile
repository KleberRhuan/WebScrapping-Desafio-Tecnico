FROM postgres:17-alpine

LABEL maintainer="Kleber Rhuan - Intuitive Care(Teste Tecnico)"
LABEL description="Banco de dados PostgreSQL com configurações otimizadas"

# Instalar dependências usando o gerenciador de pacotes Alpine
RUN apk add --no-cache curl

# Configurações otimizadas para PostgreSQL
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=operators_db
ENV POSTGRES_SHARED_BUFFERS=256MB
ENV POSTGRES_WORK_MEM=16MB
ENV POSTGRES_MAINTENANCE_WORK_MEM=64MB
ENV POSTGRES_EFFECTIVE_CACHE_SIZE=768MB
ENV POSTGRES_MAX_CONNECTIONS=100

# Criar diretório para dados temporários
RUN mkdir -p /tmp/data/

# Copiar scripts SQL para inicialização do banco
COPY ./01_schema.sql ./02_import_data.sql /docker-entrypoint-initdb.d/

# Criar script de configuração linha por linha
RUN echo '#!/bin/sh' > /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'set -e' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'until pg_isready -U postgres; do' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '  echo "Aguardando PostgreSQL iniciar..."' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '  sleep 1' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'done' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'echo "Configurando parâmetros otimizados do PostgreSQL..."' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '  ALTER SYSTEM SET synchronous_commit = '\''off'\'';' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '  ALTER SYSTEM SET random_page_cost = 1.1;' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '  ALTER SYSTEM SET effective_io_concurrency = 200;' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '  SELECT pg_reload_conf();' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'EOSQL' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo '' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    echo 'echo "Configuração do PostgreSQL concluída!"' >> /docker-entrypoint-initdb.d/00_configure.sh && \
    chmod +x /docker-entrypoint-initdb.d/00_configure.sh

# Expor a porta padrão do PostgreSQL
EXPOSE 5432

# Configuração de verificação de integridade do container
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD pg_isready -U postgres || exit 1