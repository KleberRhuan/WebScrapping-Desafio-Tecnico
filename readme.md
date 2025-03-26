# TESTE TÉCNICO - INTUITIVECARE

Este projeto foi desenvolvido em **Java** utilizando Maven e tem como objetivo realizar um teste de Web Scraping. Nele, o sistema realiza as seguintes operações:

1. **Download de Arquivos:**
    - Acessa o [site](https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos): 
    - para identificar e baixar os anexos **Anexo I** e **Anexo II** em formato PDF.
    - Os arquivos são baixados para um diretório especificado pelo usuário.

2. **Criação de Arquivo ZIP:**
    - Após o download, é possível compactar todos os arquivos baixados em um único arquivo compactado (por exemplo, em formato **ZIP**).

## Tecnologias e Dependências

O projeto utiliza as seguintes bibliotecas:

- **Jsoup:** Para conexão e parsing da página HTML.  
  Versão: `1.19.1`
- **Jakarta Validation API:** Para validação dos dados.  
  Versão: `3.1.1`
- **Lombok:** Para reduzir o boilerplate de código.  
  Versão: `1.18.36`
- **JUnit Jupiter:** Para testes unitários.  
  Versão: `5.12.1`
- **Mockito:** Para criação de mocks nos testes.  
  Versão: `5.16.1`

As dependências estão configuradas no arquivo `pom.xml`.

## Estrutura do Projeto

- **com.kleberrhuan.intuitivecare:**  
  Contém a classe principal `CliManager` que gerencia as operações do sistema via interface de linha de comando.

- **com.kleberrhuan.intuitivecare.service:**  
  Contém o serviço de scraping (`ScrapperService`) que realiza o download dos anexos do site.

- **com.kleberrhuan.intuitivecare.util:**  
  Contém utilitários como:
    - `ScannerHelper`: Facilita a leitura de dados do usuário.
    - `ZipArchiver`: Realiza a compactação de arquivos.
    - `DirectoryHelper`: Auxilia na criação e validação de diretórios.
    - `HttpDownloader`: Gerencia o download dos arquivos utilizando um pool de threads.

- **com.kleberrhuan.intuitivecare.model:**  
  Contém os modelos de dados usados no sistema (por exemplo, `FileModel`, `FilelinkModel`, `ScrappingRequest` e o enum `FileType`).

## Instruções de Execução

1. **Pré-Requisitos:**
    - Java 21 (ou superior)
    - Maven instalado na máquina

2. **Compilação e Execução:**

    - **Compilar o Projeto:**  
      Na raiz do projeto, execute:
      ```bash
      mvn clean install
      ```

    - **Executar o Programa:**  
      Execute a classe principal `CliManager`:
      ```bash
      mvn exec:java -Dexec.mainClass="com.kleberrhuan.intuitivecare.CliManager"
      ```
      Ou execute a partir da sua IDE (por exemplo, IntelliJ IDEA).

3. **Utilização:**
    - Ao iniciar o programa, será exibido um menu com as opções:
        1. **Download de arquivos:**  
           Solicita ao usuário o diretório para download e executa o scraping para baixar os PDFs dos anexos.
        2. **Criação do arquivo ZIP:**  
           Solicita ao usuário o diretório contendo os arquivos a serem compactados, o diretório de saída e o nome do arquivo ZIP.
        3. **Sair:**  
           Encerra a aplicação.

## Testes

O projeto contém testes unitários para validar as operações de scraping, download e compactação dos arquivos. Os testes utilizam JUnit e Mockito para simular o comportamento dos componentes (por exemplo, mockar a conexão com o site via Jsoup).

Para executar os testes, utilize:
```bash
mvn test
```

## Observações

- **Conectividade:**  
  Certifique-se de que a máquina onde o programa será executado possui acesso à internet para que o scraping funcione corretamente.

- **Manutenção:**  
  Caso o layout ou a estrutura do site seja alterado, os seletores e a lógica de extração dos links poderão necessitar de ajustes.

- **Extensibilidade:**  
  O código pode ser adaptado para suportar outros tipos de arquivos ou métodos de compactação, conforme a necessidade.

---

Este projeto foi desenvolvido como parte dos **TESTE TÉCNICO - INTUITIVECARE**.  
