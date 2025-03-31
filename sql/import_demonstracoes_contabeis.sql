CREATE SCHEMA IF NOT EXISTS accounting;
CREATE SCHEMA IF NOT EXISTS config;

-- Tabela criada com Base no dicionario de dados presente no link https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/
CREATE TABLE IF NOT EXISTS accounting.ans_demonstracoes (
                                                            id bigserial PRIMARY KEY,
                                                            data DATE, -- Data do início do trimestre dos dados (AAAA-MM-DD)
                                                            reg_ans INTEGER NOT NULL, -- Registro da operadora (Número, 8 dígitos)
                                                            cd_conta_contabil INTEGER, -- Código da conta contábil (Número, 8 dígitos)
                                                            descricao VARCHAR(150), -- Descrição da conta contábil (Texto, 150 caracteres)
                                                            vl_saldo_inicial NUMERIC(8,2), -- Valor do saldo inicial (Número, 8 dígitos)
                                                            vl_saldo_final NUMERIC(8,2)  -- Valor do saldo final (Número, 8 dígitos)
);

-- Tabela de Referencia para UF's estaduais
CREATE TABLE IF NOT EXISTS config.states (
                                             state_uf CHAR(2) PRIMARY KEY NOT NULL
);

-- Tabela criada com Base no dicionario de dados presente no link https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/
CREATE TABLE IF NOT EXISTS config.empresa_ans (
                                                  id bigserial PRIMARY KEY,
                                                  registro_operadora       varchar(6)    NOT NULL,  -- Registro da operadora (6 dígitos)
                                                  cnpj                     varchar(14)   NOT NULL,  -- CNPJ da Operadora
                                                  razao_social             varchar(140)  NOT NULL,  -- Razão Social da Operadora
                                                  nome_fantasia            varchar(140),            -- Nome Fantasia da Operadora
                                                  modalidade               varchar(2)    NOT NULL,  -- Modalidade (classificação conforme estatuto jurídico)
                                                  logradouro               varchar(40)   NOT NULL,  -- Endereço da Sede da Operadora
                                                  numero                   varchar(20)   NOT NULL,  -- Número do endereço
                                                  complemento              varchar(40),             -- Complemento do endereço
                                                  bairro                   varchar(30),             -- Bairro
                                                  cidade                   varchar(30),             -- Cidade
                                                  uf                       char(2)       NOT NULL,  -- Estado (UF)
                                                  cep                      char(8),                 -- CEP
                                                  ddd                      varchar(4)    NOT NULL,  -- Código de DDD da Operadora
                                                  telefone                 varchar(20)   NOT NULL,  -- Telefone da Operadora
                                                  fax                      varchar(20),             -- Fax da Operadora
                                                  endereco_eletronico      varchar(255),            -- E-mail da Operadora
                                                  representante            varchar(50),             -- Representante legal da Operadora
                                                  cargo_representante      varchar(40),             -- Cargo do representante legal
                                                  regiao_de_comercializacao smallint,             -- Região de Comercialização (número de 1 dígito)
                                                  data_registro_ans        date        NOT NULL,  -- Data do registro na ANS (formato AAAA-MM-DD)

                                                  CONSTRAINT empresa_ans_uf_fk FOREIGN KEY (uf) REFERENCES config.states (state_uf)
);

-- Procedure criada para importar csv's dinamicamente utilizando um diretorio como base
CREATE OR REPLACE PROCEDURE config.load_ans_demonstracoes_csv_from_dir(p_dir TEXT)
    LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;
    file_path TEXT;
BEGIN
    -- Remove a tabela temporária se ela já existir para evitar conflito
    DROP TABLE IF EXISTS staging_ans_demonstracoes;

    -- Cria uma tabela temp para armazenar os dados
    CREATE TEMP TABLE staging_ans_demonstracoes (
                                                    data TEXT,
                                                    reg_ans TEXT,
                                                    cd_conta_contabil TEXT,
                                                    descricao TEXT,
                                                    vl_saldo_inicial TEXT,
                                                    vl_saldo_final TEXT
    ) ON COMMIT DROP;

    -- Loop sobre cada arquivo CSV no diretório
    FOR rec IN SELECT pg_ls_dir(p_dir) AS filename
        LOOP
            IF rec.filename LIKE '%.csv' THEN
                file_path := p_dir || '/' || rec.filename;
                RAISE NOTICE 'Loading file: %', file_path;

                BEGIN
                    -- Carrega os dados do arquivo para a tabela temporária
                    EXECUTE format(
                            'COPY staging_ans_demonstracoes (data, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final)
                             FROM %L WITH (FORMAT csv, HEADER TRUE, DELIMITER '';'')',
                            file_path
                            );

                    -- Insere os dados processados na tabela definitiva
                    EXECUTE '
                    INSERT INTO accounting.ans_demonstracoes (data, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final)
                    SELECT
                        data::date,
                        reg_ans::integer,
                        cd_conta_contabil::integer,
                        descricao,
                        REPLACE(vl_saldo_inicial, '','', ''.'')::numeric,
                        REPLACE(vl_saldo_final, '','', ''.'')::numeric
                    FROM staging_ans_demonstracoes
                ';
                EXCEPTION WHEN OTHERS THEN
                    RAISE NOTICE 'Error processing file %: %', file_path, SQLERRM;
                END;

                -- Limpa a tabela temporária para o próximo arquivo
                TRUNCATE staging_ans_demonstracoes;
            END IF;
        END LOOP;
END;
$$;

-- Chamada de Metodos para importar os csv's ( Dentro de transacao para assegurar operacoa atomica)
BEGIN;
CALL config.load_ans_demonstracoes_csv_from_dir('/private/tmp/2023');
CALL config.load_ans_demonstracoes_csv_from_dir('/private/tmp/2024');
COMMIT;

INSERT INTO config.states (state_uf) VALUES
                                         ('AC'),
                                         ('AL'),
                                         ('AP'),
                                         ('AM'),
                                         ('BA'),
                                         ('CE'),
                                         ('DF'),
                                         ('ES'),
                                         ('GO'),
                                         ('MA'),
                                         ('MT'),
                                         ('MS'),
                                         ('MG'),
                                         ('PA'),
                                         ('PB'),
                                         ('PR'),
                                         ('PE'),
                                         ('PI'),
                                         ('RJ'),
                                         ('RN'),
                                         ('RS'),
                                         ('RO'),
                                         ('RR'),
                                         ('SC'),
                                         ('SP'),
                                         ('SE'),
                                         ('TO');

-- Tabela temp para armazenamento de INFO
CREATE TEMPORARY TABLE temp_empresa_ans (
                                            registro_ans              TEXT,
                                            cnpj                      TEXT,
                                            razao_social              TEXT,
                                            nome_fantasia             TEXT,
                                            modalidade                TEXT,
                                            logradouro                TEXT,
                                            numero                    TEXT,
                                            complemento               TEXT,
                                            bairro                    TEXT,
                                            cidade                    TEXT,
                                            uf                        TEXT,
                                            cep                       TEXT,
                                            ddd                       TEXT,
                                            telefone                  TEXT,
                                            fax                       TEXT,
                                            endereco_eletronico       TEXT,
                                            representante             TEXT,
                                            cargo_representante       TEXT,
                                            regiao_de_comercializacao TEXT,
                                            data_registro_ans         TEXT
);

-- Copia dados do arquivo csv para a tabela
COPY temp_empresa_ans FROM '/private/tmp/Relatorio_cadop.csv'
    WITH (FORMAT csv, HEADER true, DELIMITER ',');

INSERT INTO config.empresa_ans (
    registro_operadora,
    cnpj,
    razao_social,
    nome_fantasia,
    modalidade,
    logradouro,
    numero,
    complemento,
    bairro,
    cidade,
    uf,
    cep,
    ddd,
    telefone,
    fax,
    endereco_eletronico,
    representante,
    cargo_representante,
    regiao_de_comercializacao,
    data_registro_ans
)
SELECT
    registro_ans,
    cnpj,
    razao_social,
    nome_fantasia,
    modalidade,
    logradouro,
    numero,
    complemento,
    bairro,
    cidade,
    uf,
    cep,
    ddd,
    telefone,
    fax,
    endereco_eletronico,
    representante,
    cargo_representante,
    CAST(regiao_de_comercializacao AS smallint),
    CAST(data_registro_ans AS DATE)
FROM temp_empresa_ans;

DROP TABLE temp_empresa_ans;