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