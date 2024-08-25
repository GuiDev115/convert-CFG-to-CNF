import java.io.*;
import java.nio.file.*;
import java.util.*;

class FNC {

    // Método para copiar o arquivo
    public static void copiarArquivo(String nomeArquivoEntrada, String nomeArquivoSaida) throws IOException { // Copia o arquivo de entrada para um novo arquivo
        Path caminhoEntrada = Paths.get(nomeArquivoEntrada);
        Path caminhoSaida = Paths.get(nomeArquivoSaida);
        Files.copy(caminhoEntrada, caminhoSaida, StandardCopyOption.REPLACE_EXISTING);
    }

    // Método para criar combinações
    public static void gerarCombinacoes(List<String> conjunto, String prefixo, List<String> resultado) { // Gera todas as combinações possíveis
        
        if (!conjunto.isEmpty()) { // Se o conjunto não estiver vazio
            for (int i = 0; i < conjunto.size(); i++) { // Para cada elemento no conjunto 
                String novoPrefixo = prefixo + conjunto.get(i); // Adiciona o elemento ao prefixo
                resultado.add(novoPrefixo); // Adiciona o prefixo ao resultado
                List<String> restante = new ArrayList<>(conjunto.subList(i + 1, conjunto.size()));
                gerarCombinacoes(restante, novoPrefixo, resultado);
            }
        }
    }

    // Método para criar todas as combinações possíveis
    public static void gerarCombinacoes(List<String> conjunto, String prefixo, List<String> resultado, int maxTamanho) { // Gera todas as combinações possíveis com um tamanho máximo
        if (prefixo.length() <= maxTamanho) {
            if (!prefixo.isEmpty()) { // Se o prefixo não estiver vazio
                resultado.add(prefixo);
            }
            for (String elemento : conjunto) {
                gerarCombinacoes(conjunto, prefixo + elemento, resultado, maxTamanho); // Adiciona o elemento ao prefixo
            }
        }
    }

    // Método para tratar símbolo inicial recursivo
    public static void etapa1(String nomeArquivo) throws IOException { // Adiciona uma nova regra para o símbolo inicial

        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo)); // Lê as linhas do arquivo

        if (!linhas.isEmpty()) { // Se o arquivo não estiver vazio

            // Copia o símbolo inicial
            String primeiraLinha = linhas.get(0);
            String simboloInicial = primeiraLinha.substring(0, 2).trim();

            // Verifica presença do símbolo inicial
            for (String linha : linhas) {
 
                String conteudoLinha = linha.substring(5).trim();

                if (conteudoLinha.indexOf(simboloInicial) != -1) { 

                    // Símbolo inicial encontrado no conteúdo, criando S'
                    String novaLinha = simboloInicial + "' -> " + simboloInicial;
                    List<String> novasLinhas = new ArrayList<>();
                    novasLinhas.add(novaLinha);
                    novasLinhas.addAll(linhas);

                    // Escreve as novas linhas de volta no arquivo
                    Files.write(Paths.get(nomeArquivo), novasLinhas);

                }
            }
        }
    }

    // Método para tratar gramática para ser essencialmente não-contrátil
    public static void etapa2(String nomeArquivo) throws IOException { // Remove lambda e regras unitárias
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo));

        int linhaIndex = 0;
        List<String> conjuntoAnulavel = new ArrayList<>();
        List<String> linhasSemLambda = new ArrayList<>();

        if (!linhas.isEmpty()) { // Se o arquivo não estiver vazio

            for (String linha : linhas) {

                String simbolo = linha.substring(0, 2).trim();
                String conteudoLinha = linha.substring(5).trim();

                if (linhaIndex == 0) { // Se for a primeira linha
                    linhaIndex++;
                    String novaLinha = simbolo + " -> " + String.join(" | ", conteudoLinha);
                    linhasSemLambda.add(novaLinha);
                    continue; // Pula a linha do símbolo inicial
                }

                char lambda = '.';

                if (conteudoLinha.indexOf(lambda) != -1) { // Se a linha contém lambda
                    conjuntoAnulavel.add(linha.substring(0, 2).trim()); // Adiciona o símbolo ao conjunto anulável
                } 

                // Remove as partes específicas da linha
                String linhaSemPontoBarra = conteudoLinha // Remove os pontos e barras
                    .replaceAll("\\. \\| ", "")
                    .replaceAll("\\| \\.", "")
                    .replaceAll("(?<!\\S)\\.(?!\\S)", "");

                
                // Adiciona a linha processada à lista
                if(!linhaSemPontoBarra.trim().isEmpty()){ // Se a linha não estiver vazia
                    String novaLinha = simbolo + " -> " + String.join(" | ", linhaSemPontoBarra);
                    linhasSemLambda.add(novaLinha);
                }
                
            }
            
            linhaIndex = 0;

            // Verifica o que mais deve ser adicionado no conjunto anulável
            boolean mudou = true; 

            while (mudou) { // Enquanto houver mudanças
                mudou = false;

                for (String linha : linhasSemLambda) { 
                    
                    String conteudoLinha = linha.substring(5).trim();

                    String[] partes = conteudoLinha.split("\\|");

                    for (String parte : partes) { // Para cada parte da linha
                        String conteudoAtual = parte.trim();
                        for (String c : conjuntoAnulavel) {
                            conteudoAtual = conteudoAtual.replace(String.valueOf(c), "");
                        }
                        if (conteudoAtual.isEmpty() && !conjuntoAnulavel.contains(linha.substring(0, 2).trim())) { // Se a parte for vazia e o símbolo não estiver no conjunto anulável
                            conjuntoAnulavel.add(linha.substring(0, 2).trim());
                            mudou = true;
                        }
                    }
                }
            }
            
            // Gera as combinações possíveis do conjunto anulável 
            List<String> combinacoes = new ArrayList<>();
            gerarCombinacoes(conjuntoAnulavel, "", combinacoes);
            conjuntoAnulavel = combinacoes;

            // Modifica as regras para que sejam essencialmente não-contráteis
            List<String> novasLinhas = new ArrayList<>();

            for (String linha : linhasSemLambda) { 
                String simbolo = linha.substring(0, 2).trim();
                if (conjuntoAnulavel.contains(simbolo)) {
                    String conteudoLinha = linha.substring(5).trim();
                    String[] partes = conteudoLinha.split("\\|");
                    Set<String> novasRegras = new LinkedHashSet<>();

                    for (String parte : partes) {
                        parte = parte.trim();  // Remove espaços em branco
                        if (!parte.isEmpty()) { // Se a parte não estiver vazia
                            novasRegras.add(parte); 
                            for (String combinacao : combinacoes) { 
                                String novaParte = parte;
                                for (char c : combinacao.toCharArray()) {
                                    // Enquanto o caractere estiver presente em novaParte
                                    while (novaParte.indexOf(c) != -1) { // Enquanto o caractere estiver presente
                                        // Encontra o índice da primeira ocorrência do caractere
                                        int index = novaParte.indexOf(c);
                                        // Remove a primeira ocorrência do caractere
                                        novaParte = novaParte.substring(0, index) + novaParte.substring(index + 1);
                                        if (!novaParte.isEmpty() && !novaParte.equals(simbolo)) { // Se a nova parte não estiver vazia e não for igual ao símbolo
                                            novasRegras.add(novaParte);
                                        }
                                        // Só o símbolo inicial pode ter lambda
                                        if(novaParte.isEmpty() && linhaIndex == 0){ // Se a nova parte for vazia e for a primeira linha
                                            novasRegras.add(".");
                                            linhaIndex++;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (String parte : partes) {
                        parte = parte.trim(); 
                        if (!parte.isEmpty()) { // Se a parte não estiver vazia
                            novasRegras.add(parte); 
                            for (String combinacao : combinacoes) {
                                String novaParte = parte;
                                // Para cada caractere em combinacao
                                for (char c : combinacao.toCharArray()) {
                                    // Encontra todos os índices do caractere 'c' na string 'novaParte'
                                    // E remove o último índice encontrado primeiro
                                    while (novaParte.indexOf(c) != -1) {
                                        // Encontra o índice da última ocorrência do caractere
                                        int index = novaParte.lastIndexOf(c);
                                        // Remove a última ocorrência do caractere
                                        novaParte = novaParte.substring(0, index) + novaParte.substring(index + 1);
                                        
                                        // Adiciona a novaParte às novasRegras, se ela não for vazia e não for igual ao simbolo
                                        if (!novaParte.isEmpty() && !novaParte.equals(simbolo)) { // Se a nova parte não estiver vazia e não for igual ao símbolo
                                            novasRegras.add(novaParte);
                                        }
                                        
                                        // Só o símbolo inicial pode ter lambda
                                        if (novaParte.isEmpty() && linhaIndex == 0) { // Se a nova parte for vazia e for a primeira linha
                                            novasRegras.add(".");
                                            linhaIndex++;
                                        }
                                    }
                                }
                            }
                        }
                    }                    

                    String novaLinha = simbolo + " -> " + String.join(" | ", novasRegras);
                    novasLinhas.add(novaLinha);
                } 
                
                else {
                    novasLinhas.add(linha);
                }

            }

            linhaIndex = 0;
            Files.write(Paths.get(nomeArquivo), novasLinhas);

        }
    }

    public static void etapa3(String nomeArquivo) throws IOException { // Remove variáveis não-terminais de tamanho 1
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo));
        Map<String, ArrayList<String>> regrasMap = new LinkedHashMap<>();

        // Cria um mapa para armazenar as regras de cada símbolo
        for (String linha : linhas) {
            String simbolo = linha.substring(0, 2).trim();
            String conteudo = linha.substring(5).trim();
            String[] regrasSeparadas = conteudo.split("\\|");
            ArrayList<String> regras = new ArrayList<>();
            for (String regra : regrasSeparadas) {
                regras.add(regra.trim());
            }
            regrasMap.put(simbolo, regras);
        }

        // Atualiza as regras substituindo as variáveis não-terminais de tamanho 1
        boolean houveMudanca;
        do {
            houveMudanca = false;
            Map<String, Set<String>> regrasAtualizadas = new LinkedHashMap<>();

            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();
                Set<String> novasRegras = new LinkedHashSet<>();

                for (String regra : regras) {
                    if (regra.length() == 1 && Character.isUpperCase(regra.charAt(0))) { // Se a regra for uma variável não-terminal de tamanho 1
                        // É uma variável não-terminal
                        String simboloVar = regra;
                        if (regrasMap.containsKey(simboloVar)) {
                            novasRegras.addAll(regrasMap.get(simboloVar));
                            houveMudanca = true;
                        }
                    } else {
                        novasRegras.add(regra);
                    }
                }

                // Adiciona a regra atualizada ao mapa de regras
                regrasAtualizadas.put(simbolo, novasRegras);
            }

            // Atualiza o mapa de regras
            regrasMap = new LinkedHashMap<>();
            for (Map.Entry<String, Set<String>> entry : regrasAtualizadas.entrySet()) { // Para cada entrada no mapa de regras atualizadas
                regrasMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        } while (houveMudanca);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) { // Escreve as regras atualizadas no arquivo
            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();
                writer.write(simbolo + " -> " + String.join(" | ", regras));
                writer.newLine();
            }
        }
    }

    public static void etapa4(String nomeArquivo) throws IOException { // Remove variáveis que geram terminais
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo));
        Map<String, ArrayList<String>> regrasMap = new LinkedHashMap<>();

        // Cria um mapa para armazenar as regras de cada símbolo
        for (String linha : linhas) {
            String simbolo = linha.substring(0, 2).trim();
            String conteudo = linha.substring(5).trim();
            String[] regrasSeparadas = conteudo.split("\\|");
            ArrayList<String> regras = new ArrayList<>();
            for (String regra : regrasSeparadas) {
                regras.add(regra.trim());
            }
            regrasMap.put(simbolo, regras);
        }

        // Identifica variáveis que geram terminais
        Set<String> variaveisTerminais = new LinkedHashSet<>();
        boolean mudou;

        do {
            mudou = false;
            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) { // Para cada entrada no mapa de regras
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();

                // Verifica se alguma regra é composta apenas por terminais
                for (String regra : regras) {
                    boolean apenasTerminais = regra.chars().allMatch(Character::isLowerCase) ||
                                               regra.chars().allMatch(ch -> !Character.isUpperCase(ch) || variaveisTerminais.contains(String.valueOf((char) ch)));
                    if (apenasTerminais) { // Se a regra for composta apenas por terminais
                        if (variaveisTerminais.add(simbolo)) { // Adiciona o símbolo ao conjunto de variáveis terminais
                            mudou = true;
                        }
                        break;
                    }
                }
            }
        } while (mudou);

        // Gera combinações das variáveis terminais
        List<String> combinacoes = new ArrayList<>();
        gerarCombinacoes(new ArrayList<>(variaveisTerminais), "", combinacoes);

        // Identifica variáveis não presentes nas combinações
        Set<String> variaveisNaoPresentes = new LinkedHashSet<>(regrasMap.keySet());
        variaveisNaoPresentes.removeAll(variaveisTerminais); // Remove as variáveis terminais

        // Remove linhas que contêm variáveis não presentes
        linhas.removeIf(linha -> variaveisNaoPresentes.contains(linha.substring(0, 2).trim()));

        // Remove regras que contêm variáveis não presentes
        for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) { // Para cada entrada no mapa de regras
            ArrayList<String> regras = entry.getValue();
            regras.removeIf(regra -> variaveisNaoPresentes.stream().anyMatch(regra::contains));
        }

        // Escreve as regras restantes no arquivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) { // Escreve as regras atualizadas no arquivo
            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();
                if (!regras.isEmpty()) { // Se a lista de regras não estiver vazia
                    String conteudo = String.join(" | ", regras);
                    writer.write(simbolo + " -> " + conteudo);
                    writer.newLine(); 
                }
            }
        }
    }

    public static void etapa5(String nomeArquivoEntrada) throws IOException { // Remove variáveis inalcançáveis
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivoEntrada)); // Lê as linhas do arquivo de entrada
        Set<String> reach = new HashSet<>(); // Cria um conjunto para armazenar as variáveis alcançáveis
        Set<String> prev = new HashSet<>(); // Cria um conjunto para armazenar as variáveis alcançáveis da iteração anterior

        // Inicializa REACH com o símbolo inicial 'S' ou 'S''
        reach.add("S'"); // Adiciona 'S'' como variável inicial
        reach.add("S");  // Adiciona 'S' como variável inicial

        // Calcula o conjunto REACH
        boolean mudou;
        do {
            Set<String> novo = new HashSet<>(reach); // Cria um novo conjunto com as variáveis alcançáveis
            novo.removeAll(prev);
            prev = new HashSet<>(reach);

            // Para cada variável em REACH, adiciona as variáveis que ela produz
            for (String variavel : novo) {
                for (String linha : linhas) {
                    // Verifica se a linha começa com a variável
                    if (linha.startsWith(variavel + " -> ")) {
                        String[] partes = linha.split(" -> "); // Divide a linha em duas partes
                        if (partes.length > 1) { // Verifica se a linha contém uma produção
                            String producao = partes[1];
                            // Adiciona as variáveis da produção ao conjunto reach
                            for (char c : producao.toCharArray()) {
                                if (Character.isUpperCase(c)) {
                                    reach.add(String.valueOf(c));
                                }
                            }
                        }
                    }
                }
            }
            mudou = !reach.equals(prev);
        } while (mudou);

        // Escreve as regras alcançáveis no arquivo de saída
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivoEntrada))) { // Escreve as regras alcançáveis no arquivo de saída
            for (String linha : linhas) {
                String[] partes = linha.split(" -> ");
                String variavel = partes[0].trim();
                if (reach.contains(variavel)) { // Se a variável estiver no conjunto reach
                    writer.write(linha);
                    writer.newLine();
                }
            }
        }
    }
                  
    public static void etapa6_parte1(String nomeArquivoEntrada) throws IOException { // Remove variáveis não-terminais de tamanho 1
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivoEntrada));
        List<String> novasRegras = new ArrayList<>();
        List<String> novasRegrasTemporarias = new ArrayList<>();
        Map<String, String> terminaisParaVariaveis = new HashMap<>();
    
        for (String linha : linhas) { 
            String[] partes = linha.split("->");
            String ladoEsquerdo = partes[0].trim();
            String[] producoes = partes[1].trim().split("\\|");
    
            StringBuilder novaRegra = new StringBuilder(ladoEsquerdo + " -> ");
    
            for (int i = 0; i < producoes.length; i++) {
                String producao = producoes[i].trim();
                StringBuilder novoLadoDireito = new StringBuilder();
                boolean modificar = false;
    
                for (char c : producao.toCharArray()) {
                    if (Character.isLowerCase(c)) { // Se é um terminal
                        if (producao.length() > 1) { // Modificar apenas se não estiver sozinho
                            modificar = true;
                            String variavel;
                            if (terminaisParaVariaveis.containsKey(String.valueOf(c))) { // Se o terminal já foi mapeado
                                variavel = terminaisParaVariaveis.get(String.valueOf(c));
                            } else {
                                variavel = Character.toUpperCase(c) + "'";
                                terminaisParaVariaveis.put(String.valueOf(c), variavel);
                                novasRegrasTemporarias.add(variavel + " -> " + c);
                            }
                            novoLadoDireito.append(variavel);
                        } else {
                            novoLadoDireito.append(c);
                        }
                    } else {
                        novoLadoDireito.append(c);
                    }
                }
    
                if (modificar) { // Se a produção foi modificada
                    novaRegra.append(novoLadoDireito.toString());
                } else {
                    novaRegra.append(producao);
                }
    
                if (i < producoes.length - 1) { // Se não for a última produção
                    novaRegra.append(" | ");
                }
            }
    
            novasRegras.add(novaRegra.toString());
        }
    
        // Adiciona as novas regras ao final do resultado
        novasRegras.addAll(novasRegrasTemporarias);
    
        Files.write(Paths.get(nomeArquivoEntrada), novasRegras);
    }                    

    public static void etapa6_parte2(String nomeArquivoEntrada) throws IOException { // Remove variáveis não-terminais de tamanho maior que 2
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivoEntrada)); 
        List<String> novasRegras = new ArrayList<>(linhas);
        Map<String, String> substituicoes = new HashMap<>();
        int contadorVariaveis = 1;

        for (int i = 0; i < novasRegras.size(); i++) { // Para cada linha
            String linha = novasRegras.get(i); // Pega a linha
            String[] partes = linha.split("->"); // Divide a linha em duas partes
            if (partes.length != 2) continue; // Se a linha não contém uma produção, pula para a próxima linha

            String ladoEsquerdo = partes[0].trim(); // Pega o lado esquerdo
            String[] producoes = partes[1].trim().split("\\|"); // Divide o lado direito em produções

            StringBuilder novaRegra = new StringBuilder(ladoEsquerdo + " -> "); // Cria uma nova regra

            for (int j = 0; j < producoes.length; j++) { // Para cada produção
                String producao = producoes[j].trim(); // Pega a produção
                StringBuilder novoLadoDireito = new StringBuilder(); // Cria um novo lado direito
                List<String> variaveis = new ArrayList<>(); // Cria uma lista para armazenar as variáveis não-terminais

                for (int k = 0; k < producao.length(); k++) {
                    if (k + 1 < producao.length() && (producao.charAt(k + 1) == '\'' )) {  // Se é uma variável não-terminal com apóstrofo
                        variaveis.add(producao.substring(k, k + 2)); // Adiciona a variável não-terminal com apóstrofo
                        k++; // Pula o próximo caractere
                    } else if (Character.isUpperCase(producao.charAt(k))) { // Se é uma variável não-terminal sem apóstrofo
                        variaveis.add(String.valueOf(producao.charAt(k))); // Adiciona a variável não-terminal sem apóstrofo
                    } else {
                        novoLadoDireito.append(producao.charAt(k)); // Adiciona o caractere ao lado direito
                    } 
                }

                if (variaveis.size() > 2) {
                    while (variaveis.size() > 2) { //   Enquanto houver mais de duas variáveis
                        String chave = variaveis.get(variaveis.size() - 2) + variaveis.get(variaveis.size() - 1); // Concatena as duas últimas variáveis
                        String novaVariavel; // Nova variável para substituir as duas últimas
                        if (substituicoes.containsKey(chave)) { // Se a substituição já foi feita
                            novaVariavel = substituicoes.get(chave); // Pega a variável substituída
                        } else {
                            novaVariavel = "T" + contadorVariaveis++; // Cria uma nova variável
                            substituicoes.put(chave, novaVariavel); // Adiciona a substituição ao mapa
                            novasRegras.add(novaVariavel + " -> " + chave); // Adiciona a nova regra
                        }
                        variaveis.remove(variaveis.size() - 1);
                        variaveis.set(variaveis.size() - 1, novaVariavel); // Substitui as duas últimas variáveis pela nova variável
                    }

                    for (String variavel : variaveis) {
                        novoLadoDireito.append(variavel); // Adiciona a variável ao lado direito
                    }

                    novaRegra.append(novoLadoDireito.toString()); // Adiciona o lado direito à nova regra
                } else {
                    novaRegra.append(producao);
                }

                if (j < producoes.length - 1) {
                    novaRegra.append(" | ");
                }
            }

            novasRegras.set(i, novaRegra.toString()); // Substitui a regra original pela nova regra
        }

        Files.write(Paths.get(nomeArquivoEntrada), novasRegras);
    }
    
}

public class Main {
    public static void main(String[] args) {
        try {
            String nomeArquivoEntrada = args[0];
            String nomeArquivoSaida = args[1];

            // Copia o arquivo de entrada para um novo arquivo
            FNC.copiarArquivo(nomeArquivoEntrada, nomeArquivoSaida);

            // Começa as etapas de tranformação de gramática para FNC
            FNC.etapa1(nomeArquivoSaida);
            FNC.etapa2(nomeArquivoSaida);
            FNC.etapa3(nomeArquivoSaida);
            FNC.etapa4(nomeArquivoSaida);
            FNC.etapa5(nomeArquivoSaida);
            FNC.etapa6_parte1(nomeArquivoSaida);
            FNC.etapa6_parte2(nomeArquivoSaida);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
