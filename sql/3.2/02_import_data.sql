-- Script para importação de dados do CADOP
-- Tabela temp para armazenamento temporário dos dados
CREATE TEMPORARY TABLE IF NOT EXISTS temp_cadastro_operadoras (
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

-- Abordagem 1: Usando curl para baixar (via psql \! comando de shell)
\! echo "Tentando baixar dados da ANS via curl..."
\! curl -s -o /tmp/Relatorio_cadop.csv https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv

-- Função flexível para importação de dados
DO $$
    DECLARE
        tmp_file TEXT := '/tmp/Relatorio_cadop.csv';
        local_file TEXT := '/tmp/Relatorio_cadop.csv';
        dados_importados BOOLEAN := FALSE;
    BEGIN
        -- Tenta importar o arquivo baixado pelo curl
        BEGIN
            RAISE NOTICE 'Tentando importar arquivo baixado...';
            EXECUTE format('COPY temp_cadastro_operadoras FROM %L
                      WITH (FORMAT csv, HEADER TRUE, DELIMITER '';'', ENCODING ''UTF8'')',
                           tmp_file);
            RAISE NOTICE 'Dados importados com sucesso do arquivo baixado: %', tmp_file;
            dados_importados := TRUE;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Erro ao importar arquivo baixado: %. Tentando arquivo local...', SQLERRM;
        END;

        -- Se não conseguiu importar o arquivo baixado, tenta do arquivo local
        IF NOT dados_importados THEN
            BEGIN
                EXECUTE format('COPY temp_cadastro_operadoras FROM %L
                          WITH (FORMAT csv, HEADER TRUE, DELIMITER '';'', ENCODING ''UTF8'')',
                               local_file);
                RAISE NOTICE 'Dados importados com sucesso do arquivo local: %', local_file;
                dados_importados := TRUE;
            EXCEPTION WHEN OTHERS THEN
                RAISE NOTICE 'Erro ao importar arquivo local: %', SQLERRM;
                RAISE EXCEPTION 'Falha ao importar dados do CADOP. Verifique se o arquivo está disponível.';
            END;
        END IF;

        -- Verifica se há registros com problemas antes de inserir
        RAISE NOTICE 'Validando dados importados...';
        PERFORM COUNT(*) FROM temp_cadastro_operadoras
        WHERE data_registro_ans IS NULL OR NOT data_registro_ans ~ '^\d{4}-\d{2}-\d{2}$';

        -- Insere na tabela definitiva
        RAISE NOTICE 'Inserindo dados na tabela permanente...';
        INSERT INTO cadop.cadastro_operadoras (
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
            TRIM(uf),
            cep,
            ddd,
            telefone,
            fax,
            endereco_eletronico,
            representante,
            cargo_representante,
            CASE
                WHEN regiao_de_comercializacao ~ '^\d+$' THEN CAST(regiao_de_comercializacao AS smallint)
                END,
            CASE
                WHEN data_registro_ans ~ '^\d{4}-\d{2}-\d{2}$' THEN CAST(data_registro_ans AS DATE)
                END
        FROM temp_cadastro_operadoras;

        -- Mostra quantos registros foram importados
        RAISE NOTICE 'Importação concluída. % registros importados.',
            (SELECT COUNT(*) FROM cadop.cadastro_operadoras);
    END $$;

-- Limpa a tabela temporária
DROP TABLE IF EXISTS temp_cadastro_operadoras;