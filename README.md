# IntuitiveCare - Teste Técnico

Este projeto implementa os três testes técnicos solicitados: Web Scraping, Transformação de Dados e Banco de Dados.

## Estrutura do Projeto

```
IntuitiveCare/
├── src/main/java/com/kleberrhuan/intuitivecare/
│   ├── cli/                  # Interface de linha de comando
│   ├── config/               # Configurações da aplicação
│   ├── exception/            # Classes de exceção personalizadas
│   ├── model/                # Modelos de dados
│   ├── service/              # Serviços da aplicação
│   ├── util/                 # Classes utilitárias
│   │   ├── helpers/          # Classes auxiliares
│   │   └── interfaces/       # Interfaces
│   └── Main.java             # Ponto de entrada da aplicação
├── src/main/resources/
│   └── logback.xml           # Configuração de logs
├── src/test/java/            # Testes unitários
│   └── com/kleberrhuan/intuitivecare/
│       ├── cli/              # Testes da CLI
│       ├── service/          # Testes de serviços 
│       └── util/             # Testes de utilitários
├── sql/                      # Scripts SQL para o teste 3
│   ├── 3.1/                  # Scripts para importação de dados
│   ├── 3.2/                  # Scripts para análise de dados
│   └── schema/               # Scripts para criação de tabelas
└── output/                   # Diretório onde os arquivos gerados são salvos
```

## Requisitos

- Java 21 ou superior
- Maven 3.8.0 ou superior
- MySQL 8.0 ou PostgreSQL 10.0 ou superior (para o teste 3)

## Como Executar

### Compilação

```bash
mvn clean package
```

### Execução

```bash
java -jar target/intuitivecare-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Ou executar diretamente pela IDE, executando a classe `com.kleberrhuan.intuitivecare.Main`.

### Testes

```bash
mvn test
```

## Descrição dos Testes

### 1. Teste de Web Scraping

Este teste acessa o site da ANS, baixa os Anexos I e II em formato PDF e os compacta em um único arquivo ZIP.

Funcionalidades implementadas:
- Acesso ao site da ANS usando JSoup
- Download dos PDFs
- Compactação em um arquivo ZIP

Para executar apenas este teste, selecione a opção 1 no menu principal.

### 2. Teste de Transformação de Dados

Este teste extrai dados da tabela do PDF do Anexo I, salva em formato CSV e compacta o arquivo.

Funcionalidades implementadas:
- Extração de dados tabulares do PDF usando Apache PDFBox
- Conversão para formato CSV
- Substituição de abreviações (OD → Seg. Odontológica, AMB → Seg. Ambulatorial)
- Compactação do CSV

Para executar apenas este teste, selecione a opção 2 no menu principal.

### 3. Teste de Banco de Dados

Este teste consiste em scripts SQL para importação e análise de dados das demonstrações contábeis da ANS.

Os scripts estão localizados na pasta `sql/`:
- `sql/3.1/`: Scripts para baixar e importar os arquivos CSV
- `sql/schema/`: Scripts para criar as tabelas necessárias
- `sql/3.2/`: Scripts para análise dos dados

Para executar análises relacionadas ao banco de dados, selecione a opção 3 no menu principal.

### 4. Download de Demonstrações Contábeis

Esta opção permite baixar automaticamente os arquivos de demonstrações contábeis da ANS (Agência Nacional de Saúde Suplementar) de múltiplos anos.

Funcionalidades implementadas:
- Download automático de demonstrações contábeis da ANS
- Seleção do número de anos para download
- Organização dos arquivos por ano para facilitar análises

Para utilizar esta funcionalidade, selecione a opção 4 no menu principal e informe quantos anos de dados você deseja baixar.

## Menu Principal da Aplicação

Ao executar o programa, você verá o seguinte menu:

```
----- IntuitiveCare CLI -----
1. Executar Web Scraping
2. Executar Transformação de Dados
3. Executar Fluxo Completo
4. Download de Demonstrações Contábeis
0. Sair
Escolha uma opção:
```

## Queries Analíticas

As queries solicitadas para análise dos dados estão localizadas em `sql/3.2/` e respondem às seguintes perguntas:

1. Quais as 10 operadoras com maiores despesas em "EVENTOS/ SINISTROS CONHECIDOS OU AVISADOS DE ASSISTÊNCIA A SAÚDE MEDICO HOSPITALAR" no último trimestre?

2. Quais as 10 operadoras com maiores despesas nessa categoria no último ano?

## Testes Unitários

O projeto inclui testes unitários para os principais componentes:

- **ZipManagerTest**: Testa a compactação e extração de arquivos ZIP
- **HttpDownloaderTest**: Testa o download de arquivos via HTTP
- **PdfProcessingServiceTest**: Testa o processamento de PDFs e extração de dados
- **ScrapperServiceTest**: Testa o web scraping e análise de páginas HTML
- **CliManagerTest**: Testa a interface de linha de comando

Os testes utilizam JUnit 5 e Mockito para simular comportamentos e isolar componentes.

## Logs e Monitoramento

A aplicação utiliza Logback para registro de logs, com configurações coloridas e rotação de arquivos:
- Logs da console com cores para facilitar a identificação de erros
- Logs em arquivo em `logs/intuitive-care.log` com rotação automática

## Contribuição

Este projeto foi desenvolvido como parte de um teste técnico. Para contribuir, por favor, faça um fork do repositório e crie um pull request com suas alterações. 