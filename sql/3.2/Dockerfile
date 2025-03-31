FROM postgres:15

# Instalar curl para baixar arquivos
RUN apt-get update && apt-get install -y curl

# Criar diretórios necessários
RUN mkdir -p /docker-entrypoint-initdb.d/
RUN mkdir -p /tmp/data/

# Copiar scripts SQL para inicialização do banco
COPY ./sql/3.2/*.sql /docker-entrypoint-initdb.d/

# Ordenar a execução dos scripts
RUN cd /docker-entrypoint-initdb.d/ && \
  mv 01_schema.sql 01_schema.sql && \
  mv 02_import_data.sql 02_import_data.sql

# Variáveis de ambiente PostgreSQL
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=intuitive_care

# Expor a porta do PostgreSQL
EXPOSE 5432

# Configurações para melhorar a performance
RUN echo "shared_buffers = 256MB" >> /usr/share/postgresql/postgresql.conf.sample
RUN echo "work_mem = 16MB" >> /usr/share/postgresql/postgresql.conf.sample
RUN echo "maintenance_work_mem = 64MB" >> /usr/share/postgresql/postgresql.conf.sample 