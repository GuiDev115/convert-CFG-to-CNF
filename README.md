# convert-CFG-to-CNF

Convertedor de uma Gramática Livre de Contexto para Gramática Forma Normal de Chomsky

## Descrição

Este projeto tem como objetivo converter uma Gramática Livre de Contexto (GLC) em uma Forma Normal de Chomsky (FNC). A FNC é uma forma especial de gramática que é útil em várias aplicações de teoria da computação, incluindo análise sintática e algoritmos de reconhecimento de linguagem.

## Estrutura do Projeto

O projeto é composto pelos seguintes arquivos:

- `Main.java`: O arquivo principal que contém a lógica para a conversão da GLC para FNC.
- `glc1.txt`: Arquivo de entrada contendo a gramática livre de contexto.
- `glc1_fnc.txt`: Arquivo de saída onde a gramática em forma normal de Chomsky será salva.
- `.gitignore`: Arquivo para especificar quais arquivos ou diretórios devem ser ignorados pelo Git.
- `README.md`: Este arquivo, contendo informações sobre o projeto.

## Como Executar

1. Certifique-se de ter o Java Development Kit (JDK) instalado em sua máquina.

2. Compile o arquivo `Main.java`:
    ```sh
    javac Main.java
    ```

3. Execute o programa com os arquivos de entrada e saída desejados:
    ```sh
    java Main glc1.txt glc1_fnc.txt
    ```

## Exemplo de Uso

Suponha que `glc1.txt` contenha a seguinte gramática:

GitHub Copilot
Claro! Vou adicionar mais detalhes ao README para torná-lo mais informativo.

S -> AB | BC A -> BA | a B -> CC | b C -> AB | a


Após a execução do programa, o arquivo `glc1_fnc.txt` conterá a gramática convertida para a Forma Normal de Chomsky.    

## Contato

Para mais informações, entre em contato conosco. Se você deseja contribuir para o projeto, sinta-se à vontade para enviar uma issue ou um pull request.

## Licença

Este projeto está licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
