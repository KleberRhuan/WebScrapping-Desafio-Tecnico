CREATE SCHEMA IF NOT EXISTS accounting;
CREATE SCHEMA IF NOT EXISTS config;

-- Tabela criada com Base no dicionario de dados presente no link https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/
CREATE TABLE IF NOT EXISTS accounting.demonstracoes_contabeis(
                                                                 id bigserial PRIMARY KEY,
                                                                 data DATE, -- Data do início do trimestre dos dados (AAAA-MM-DD)
                                                                 reg_ans INTEGER NOT NULL, -- Registro da operadora (Número, 8 dígitos)
                                                                 cd_conta_contabil INTEGER, -- Código da conta contábil (Número, 8 dígitos)
                                                                 descricao VARCHAR(150), -- Descrição da conta contábil (Texto, 150 caracteres)
                                                                 vl_saldo_inicial NUMERIC, -- Valor do saldo inicial (Sem Limite de Digitos para evitar overflow)
                                                                 vl_saldo_final NUMERIC  -- Valor do saldo final (Sem Limite de Digitos para evitar overflow)
);

-- Procedure criada para importar csv's dinamicamente utilizando um diretorio como base
CREATE OR REPLACE PROCEDURE config.load_demonstracoes_contabeis_csv_from_dir(p_dir TEXT)
    LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;
    file_path TEXT;
BEGIN
    -- Remove a tabela temporária se ela já existir para evitar conflito
    DROP TABLE IF EXISTS staging_demonstracoes_contabeis;

    -- Cria uma tabela temp para armazenar os dados
    CREATE TEMP TABLE staging_demonstracoes_contabeis (
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
                            'COPY staging_demonstracoes_contabeis (data, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final)
                             FROM %L WITH (FORMAT csv, HEADER TRUE, DELIMITER '';'')',
                            file_path
                            );

                    -- Insere os dados processados na tabela definitiva
                    EXECUTE '
                    INSERT INTO accounting.demonstracoes_contabeis (data, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final)
                    SELECT
                        data::date,
                        reg_ans::integer,
                        cd_conta_contabil::integer,
                        descricao,
                        REPLACE(vl_saldo_inicial, '','', ''.'')::numeric,
                        REPLACE(vl_saldo_final, '','', ''.'')::numeric
                    FROM staging_demonstracoes_contabeis
                ';
                EXCEPTION WHEN OTHERS THEN
                    RAISE NOTICE 'Error processing file %: %', file_path, SQLERRM;
                END;

                -- Limpa a tabela temporária para o próximo arquivo
                TRUNCATE staging_demonstracoes_contabeis;
            END IF;
        END LOOP;
END;
$$;

-- Chamada de Metodos para importar os csv's ( Dentro de transacao para assegurar operacoa atomica)
BEGIN;
CALL config.load_demonstracoes_contabeis_csv_from_dir('/private/tmp/2023');
CALL config.load_demonstracoes_contabeis_csv_from_dir('/private/tmp/2024');
COMMIT;