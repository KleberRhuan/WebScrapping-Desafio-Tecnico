-- Script otimizado para importação de demonstrações contábeis
CREATE SCHEMA IF NOT EXISTS accounting;
CREATE SCHEMA IF NOT EXISTS config;

-- Tabela otimizada baseada no dicionário de dados do link https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/
CREATE TABLE IF NOT EXISTS accounting.demonstracoes_contabeis(
                                                                 id bigserial PRIMARY KEY,
                                                                 data DATE, -- Data do início do trimestre dos dados (AAAA-MM-DD)
                                                                 reg_ans INTEGER, -- Registro da operadora (Número, 8 dígitos)
                                                                 cd_conta_contabil INTEGER, -- Código da conta contábil (Número, 8 dígitos)
                                                                 descricao VARCHAR(150), -- Descrição da conta contábil (Texto, 150 caracteres)
                                                                 vl_saldo_inicial NUMERIC, -- Valor do saldo inicial
                                                                 vl_saldo_final NUMERIC  -- Valor do saldo final
);

-- Índices para otimizar consultas analíticas
CREATE INDEX IF NOT EXISTS idx_dem_contab_data ON accounting.demonstracoes_contabeis(data);
CREATE INDEX IF NOT EXISTS idx_dem_contab_reg_ans ON accounting.demonstracoes_contabeis(reg_ans);
CREATE INDEX IF NOT EXISTS idx_dem_contab_descricao ON accounting.demonstracoes_contabeis(descricao);

-- Procedure para importar CSVs com melhor tratamento de erros e feedback
CREATE OR REPLACE PROCEDURE config.load_demonstracoes_contabeis_csv_from_dir(
    p_dir TEXT
)
    LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;
    file_path TEXT;
    file_count INT := 0;
    total_records INT := 0;
    start_time TIMESTAMPTZ;
    end_time TIMESTAMPTZ;
    current_batch INT := 0;
BEGIN
    start_time := clock_timestamp();
    RAISE NOTICE 'Iniciando importação de demonstrações contábeis do diretório %...', p_dir;

    -- Remove a tabela temporária se ela já existir para evitar conflito
    DROP TABLE IF EXISTS temp_demonstracoes_contabeis;

    -- Cria uma tabela temp para armazenar os dados
    CREATE TEMP TABLE temp_demonstracoes_contabeis (
                                                       data TEXT,
                                                       reg_ans TEXT,
                                                       cd_conta_contabil TEXT,
                                                       descricao TEXT,
                                                       vl_saldo_inicial TEXT,
                                                       vl_saldo_final TEXT
    ) ON COMMIT PRESERVE ROWS;

    -- Cria índice na tabela temporária para melhorar desempenho
    CREATE INDEX idx_temp_dem_contab ON temp_demonstracoes_contabeis(data, reg_ans);

    -- Verifica se o diretório existe
    PERFORM 1 FROM pg_ls_dir(p_dir) LIMIT 1;

    -- Loop sobre cada arquivo CSV no diretório
    FOR rec IN
        SELECT pg_ls_dir(p_dir) AS filename
        LOOP
            IF rec.filename LIKE '%.csv' THEN
                file_path := p_dir || '/' || rec.filename;
                file_count := file_count + 1;
                RAISE NOTICE '[%] Processando arquivo: %', file_count, file_path;

                BEGIN
                    -- Limpa a tabela temporária para o próximo arquivo
                    TRUNCATE temp_demonstracoes_contabeis;

                    -- Carrega os dados do arquivo para a tabela temporária
                    EXECUTE format(
                            'COPY temp_demonstracoes_contabeis (data, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final)
                             FROM %L WITH (FORMAT csv, HEADER TRUE, DELIMITER '';'', ENCODING ''UTF8'')',
                            file_path
                            );

                    -- Conta quantos registros foram carregados
                    EXECUTE 'SELECT COUNT(*) FROM temp_demonstracoes_contabeis' INTO current_batch;
                    RAISE NOTICE '  - Carregados % registros do arquivo', current_batch;

                    -- Verifica se há valores problemáticos antes de inserir
                    PERFORM COUNT(*) FROM temp_demonstracoes_contabeis
                    WHERE data IS NULL OR data !~ '^\d{4}-\d{2}-\d{2}$';

                    -- Insere os dados processados na tabela definitiva com validação mais robusta
                    EXECUTE '
                    INSERT INTO accounting.demonstracoes_contabeis (data, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final)
                    SELECT
                        CASE WHEN data ~ ''^\\d{4}-\\d{2}-\\d{2}$'' THEN data::date ELSE NULL END,
                        CASE WHEN reg_ans ~ ''^\\d+$'' THEN reg_ans::integer ELSE NULL END,
                        CASE WHEN cd_conta_contabil ~ ''^\\d+$'' THEN cd_conta_contabil::integer ELSE NULL END,
                        descricao,
                        CASE 
                            WHEN vl_saldo_inicial ~ ''^-?\\d+([,.]\\d+)?$'' 
                            THEN REPLACE(REPLACE(vl_saldo_inicial, ''.'', ''''), '','', ''.'')::numeric 
                            ELSE NULL 
                        END,
                        CASE 
                            WHEN vl_saldo_final ~ ''^-?\\d+([,.]\\d+)?$'' 
                            THEN REPLACE(REPLACE(vl_saldo_final, ''.'', ''''), '','', ''.'')::numeric 
                            ELSE NULL 
                        END
                    FROM temp_demonstracoes_contabeis
                    ';

                    total_records := total_records + current_batch;
                EXCEPTION WHEN OTHERS THEN
                    RAISE WARNING 'Erro ao processar arquivo %: %', file_path, SQLERRM;
                END;
            END IF;
        END LOOP;

    end_time := clock_timestamp();

    -- Limpa recursos
    DROP TABLE IF EXISTS temp_demonstracoes_contabeis;

    -- Análise final
    IF file_count = 0 THEN
        RAISE WARNING 'Nenhum arquivo CSV encontrado no diretório %', p_dir;
    ELSE
        RAISE NOTICE 'Importação concluída! Processados % arquivos, % registros em % segundos',
            file_count, total_records, EXTRACT(EPOCH FROM (end_time - start_time));
    END IF;
END;
$$;

-- Chamada para importar os arquivos (dentro de transação para assegurar operação atômica)
BEGIN;
CALL config.load_demonstracoes_contabeis_csv_from_dir('/tmp/2023');
CALL config.load_demonstracoes_contabeis_csv_from_dir('/tmp/2024');
COMMIT;