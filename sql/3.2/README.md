# Instruções para Importação de Dados CADOP

Este diretório contém os scripts necessários para criar a estrutura de banco de dados e importar os dados do CADOP (Cadastro de Operadoras) da ANS (Agência Nacional de Saúde Suplementar).

## Estrutura de Arquivos

- `01_schema.sql`: Script que cria os schemas e tabelas necessárias
- `02_import_data.sql`: Script que importa os dados do CADOP
- `Dockerfile`: Configuração do container Docker do PostgreSQL
- `docker-compose.yml`: Arquivo de configuração para executar o ambiente completo

## Como Executar

### Usando Docker Compose (Recomendado)

1. Certifique-se de ter o Docker e o Docker Compose instalados no seu sistema
2. Navegue até este diretório: `cd sql/3.2`
3. Execute o ambiente:

```bash
docker-compose up -d
```

4. Acesse o banco de dados:
   - Host: localhost
   - Porta: 5432
   - Usuário: postgres
   - Senha: postgres
   - Banco de Dados: intuitive_care

5. Para acessar o pgAdmin (interface gráfica):
   - Abra o navegador e acesse: http://localhost:5050
   - Email: admin@example.com
   - Senha: admin
   - Adicione um novo servidor apontando para o host "postgres"

### Executando Localmente (Sem Docker)

1. Certifique-se de ter o PostgreSQL instalado localmente
2. Execute os scripts na ordem:

```bash
psql -U seu_usuario -d seu_banco -f 01_schema.sql
psql -U seu_usuario -d seu_banco -f 02_import_data.sql
```

## Notas

- A importação irá tentar baixar os dados diretamente do site da ANS
- Caso a conexão com a internet falhe, ele tentará usar um arquivo local
- Certifique-se de que o PostgreSQL tem permissão para executar o comando curl 