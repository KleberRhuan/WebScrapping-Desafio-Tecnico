-- Script para criação de schemas e tabelas
create schema if not exists config;
create schema if not exists cadop;
CREATE COLLATION IF NOT EXISTS pt_br_ci_ai (
    PROVIDER = icu,
    DETERMINISTIC = FALSE,
    LOCALE = 'pt-u-ks-level1'
    );

-- Tabela criada com Base no dicionario de dados presente no link https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/
CREATE TABLE IF NOT EXISTS cadop.cadastro_operadoras (
     id bigserial PRIMARY KEY,
     registro_operadora       varchar(6),  -- Registro da operadora (6 dígitos)
     cnpj                     varchar(14),  -- CNPJ da Operadora
     razao_social             varchar(140) collate "pt_br_ci_ai" ,  -- Razão Social da Operadora
     nome_fantasia            varchar(140) collate "pt_br_ci_ai" ,  -- Nome Fantasia da Operadora
     modalidade               varchar(100) collate "pt_br_ci_ai" ,  -- Modalidade (classificação conforme estatuto jurídico)
     logradouro               varchar(40) collate "pt_br_ci_ai" ,  -- Endereço da Sede da Operadora
     numero                   varchar(20) collate "pt_br_ci_ai" ,  -- Número do endereço
     complemento              varchar(40) collate "pt_br_ci_ai" ,  -- Complemento do endereço
     bairro                   varchar(30) collate "pt_br_ci_ai" ,  -- Bairro
     cidade                   varchar(30) collate "pt_br_ci_ai" ,  -- Cidade
     uf                       char(2),  -- Estado (UF)
     cep                      char(8),  -- CEP
     ddd                      varchar(4),  -- Código de DDD da Operadora
     telefone                 varchar(20),  -- Telefone da Operadora
     fax                      varchar(20) collate "pt_br_ci_ai" ,  -- Fax da Operadora
     endereco_eletronico      varchar(255) collate "pt_br_ci_ai" ,  -- E-mail da Operadora
     representante            varchar(50) collate "pt_br_ci_ai" ,  -- Representante legal da Operadora
     cargo_representante      varchar(40) collate "pt_br_ci_ai" ,  -- Cargo do representante legal
     regiao_de_comercializacao smallint,  -- Região de Comercialização (número de 1 dígito)
     data_registro_ans        date,  -- Data do registro na ANS (formato AAAA-MM-DD)

     CONSTRAINT cadastro_operadoras_check_uf CHECK (uf IN (
           'AC','AL','AP','AM','BA','CE','DF','ES','GO',
           'MA','MT','MS','MG','PA','PB','PR','PE','PI',
           'RJ','RN','RS','RO','RR','SC','SP','SE','TO'
     ))
);

-- Índices para otimizar consultas analíticas
CREATE INDEX IF NOT EXISTS idx_cadastro_operadoras_registro ON cadop.cadastro_operadoras(registro_operadora);
CREATE INDEX IF NOT EXISTS idx_cadastro_operadoras_razao_social ON cadop.cadastro_operadoras(razao_social); 