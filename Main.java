import java.io.*;
import java.nio.file.*;
import java.util.*;

class FNC {

    // Método para copiar o arquivo
    public static void copiarArquivo(String nomeArquivoEntrada, String nomeArquivoSaida) throws IOException {
        Path caminhoEntrada = Paths.get(nomeArquivoEntrada);
        Path caminhoSaida = Paths.get(nomeArquivoSaida);
        Files.copy(caminhoEntrada, caminhoSaida, StandardCopyOption.REPLACE_EXISTING);
    }

    // Método para criar combinações
    public static void gerarCombinacoes(List<String> conjunto, String prefixo, List<String> resultado) {
        
        if (!conjunto.isEmpty()) {
            for (int i = 0; i < conjunto.size(); i++) {
                String novoPrefixo = prefixo + conjunto.get(i);
                resultado.add(novoPrefixo);
                List<String> restante = new ArrayList<>(conjunto.subList(i + 1, conjunto.size()));
                gerarCombinacoes(restante, novoPrefixo, resultado);
            }
        }
    }

    // Método para criar todas as combinações possíveis
    public static void gerarCombinacoes(List<String> conjunto, String prefixo, List<String> resultado, int maxTamanho) {
        if (prefixo.length() <= maxTamanho) {
            if (!prefixo.isEmpty()) {
                resultado.add(prefixo);
            }
            for (String elemento : conjunto) {
                gerarCombinacoes(conjunto, prefixo + elemento, resultado, maxTamanho);
            }
        }
    }

    // Método para tratar símbolo inicial recursivo
    public static void etapa1(String nomeArquivo) throws IOException {

        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo));

        if (!linhas.isEmpty()) {

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
    public static void etapa2(String nomeArquivo) throws IOException {
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo));

        int linhaIndex = 0;
        List<String> conjuntoAnulavel = new ArrayList<>();
        List<String> linhasSemLambda = new ArrayList<>();

        if (!linhas.isEmpty()) {

            for (String linha : linhas) {

                String simbolo = linha.substring(0, 2).trim();
                String conteudoLinha = linha.substring(5).trim();

                if (linhaIndex == 0) {
                    linhaIndex++;
                    String novaLinha = simbolo + " -> " + String.join(" | ", conteudoLinha);
                    linhasSemLambda.add(novaLinha);
                    continue; // Pula a linha do símbolo inicial
                }

                char lambda = '.';

                if (conteudoLinha.indexOf(lambda) != -1) { 
                    conjuntoAnulavel.add(linha.substring(0, 2).trim());
                } 

                // Remove as partes específicas da linha
                String linhaSemPontoBarra = conteudoLinha
                    .replaceAll("\\. \\| ", "")
                    .replaceAll("\\| \\.", "")
                    .replaceAll("(?<!\\S)\\.(?!\\S)", "");

                
                // Adiciona a linha processada à lista
                if(!linhaSemPontoBarra.trim().isEmpty()){
                    String novaLinha = simbolo + " -> " + String.join(" | ", linhaSemPontoBarra);
                    linhasSemLambda.add(novaLinha);
                }
                
            }
            
            linhaIndex = 0;

            // Verifica o que mais deve ser adicionado no conjunto anulável
            boolean mudou = true; 

            while (mudou) {
                mudou = false;

                for (String linha : linhasSemLambda) {
                    
                    String conteudoLinha = linha.substring(5).trim();

                    String[] partes = conteudoLinha.split("\\|");

                    for (String parte : partes) {
                        String conteudoAtual = parte.trim();
                        for (String c : conjuntoAnulavel) {
                            conteudoAtual = conteudoAtual.replace(String.valueOf(c), "");
                        }
                        if (conteudoAtual.isEmpty() && !conjuntoAnulavel.contains(linha.substring(0, 2).trim())) {
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
                        parte = parte.trim(); 
                        if (!parte.isEmpty()) {
                            novasRegras.add(parte); 
                            for (String combinacao : combinacoes) {
                                String novaParte = parte;
                                for (char c : combinacao.toCharArray()) {
                                    // Enquanto o caractere estiver presente em novaParte
                                    while (novaParte.indexOf(c) != -1) {
                                        // Encontra o índice da primeira ocorrência do caractere
                                        int index = novaParte.indexOf(c);
                                        // Remove a primeira ocorrência do caractere
                                        novaParte = novaParte.substring(0, index) + novaParte.substring(index + 1);
                                        if (!novaParte.isEmpty() && !novaParte.equals(simbolo)) {
                                            novasRegras.add(novaParte);
                                        }
                                        // Só o símbolo inicial pode ter lambda
                                        if(novaParte.isEmpty() && linhaIndex == 0){
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
                        if (!parte.isEmpty()) {
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
                                        if (!novaParte.isEmpty() && !novaParte.equals(simbolo)) {
                                            novasRegras.add(novaParte);
                                        }
                                        
                                        // Só o símbolo inicial pode ter lambda
                                        if (novaParte.isEmpty() && linhaIndex == 0) {
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

    public static void etapa3(String nomeArquivo) throws IOException {
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
                    if (regra.length() == 1 && Character.isUpperCase(regra.charAt(0))) {
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
            for (Map.Entry<String, Set<String>> entry : regrasAtualizadas.entrySet()) {
                regrasMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        } while (houveMudanca);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();
                writer.write(simbolo + " -> " + String.join(" | ", regras));
                writer.newLine();
            }
        }
    }

    public static void etapa4(String nomeArquivo) throws IOException {
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
            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();

                // Verifica se alguma regra é composta apenas por terminais
                for (String regra : regras) {
                    boolean apenasTerminais = regra.chars().allMatch(Character::isLowerCase) ||
                                               regra.chars().allMatch(ch -> !Character.isUpperCase(ch) || variaveisTerminais.contains(String.valueOf((char) ch)));
                    if (apenasTerminais) {
                        if (variaveisTerminais.add(simbolo)) {
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
        variaveisNaoPresentes.removeAll(variaveisTerminais);

        // Remove linhas que contêm variáveis não presentes
        linhas.removeIf(linha -> variaveisNaoPresentes.contains(linha.substring(0, 2).trim()));

        // Remove regras que contêm variáveis não presentes
        for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
            ArrayList<String> regras = entry.getValue();
            regras.removeIf(regra -> variaveisNaoPresentes.stream().anyMatch(regra::contains));
        }

        // Escreve as regras restantes no arquivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (Map.Entry<String, ArrayList<String>> entry : regrasMap.entrySet()) {
                String simbolo = entry.getKey();
                ArrayList<String> regras = entry.getValue();
                if (!regras.isEmpty()) {
                    String conteudo = String.join(" | ", regras);
                    writer.write(simbolo + " -> " + conteudo);
                    writer.newLine();
                }
            }
        }
    }

    public static void etapa5(String nomeArquivoEntrada) throws IOException {
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivoEntrada)); // Lê as linhas do arquivo de entrada
        Set<String> reach = new HashSet<>(); // Cria um conjunto para armazenar as variáveis alcançáveis
        Set<String> prev = new HashSet<>(); // Cria um conjunto para armazenar as variáveis alcançáveis da iteração anterior

        // Inicializa REACH com o símbolo inicial 'S' ou 'S''
        reach.add("S'"); // Adiciona 'S'' como variável inicial
        reach.add("S");  // Adiciona 'S' como variável inicial

        // Calcula o conjunto REACH
        boolean mudou;
        do {
            Set<String> novo = new HashSet<>(reach);
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("saida.txt"))) {
            for (String linha : linhas) {
                String[] partes = linha.split(" -> ");
                String variavel = partes[0].trim();
                if (reach.contains(variavel)) {
                    writer.write(linha);
                    writer.newLine();
                }
            }
        }
    }
                  
    public static void etapa6_parte1(String nomeArquivoEntrada) throws IOException {
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
                            if (terminaisParaVariaveis.containsKey(String.valueOf(c))) {
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
    
                if (modificar) {
                    novaRegra.append(novoLadoDireito.toString());
                } else {
                    novaRegra.append(producao);
                }
    
                if (i < producoes.length - 1) {
                    novaRegra.append(" | ");
                }
            }
    
            novasRegras.add(novaRegra.toString());
        }
    
        // Adiciona as novas regras ao final do resultado
        novasRegras.addAll(novasRegrasTemporarias);
    
        Files.write(Paths.get(nomeArquivoEntrada), novasRegras);
    }                    

    public static void etapa6_parte2(String nomeArquivoEntrada) throws IOException {
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivoEntrada));
        List<String> novasRegras = new ArrayList<>(linhas);
        Map<String, String> substituicoes = new HashMap<>();
        int contadorVariaveis = 1;

        for (int i = 0; i < novasRegras.size(); i++) {
            String linha = novasRegras.get(i);
            String[] partes = linha.split("->");
            if (partes.length != 2) continue;

            String ladoEsquerdo = partes[0].trim();
            String[] producoes = partes[1].trim().split("\\|");

            StringBuilder novaRegra = new StringBuilder(ladoEsquerdo + " -> ");

            for (int j = 0; j < producoes.length; j++) {
                String producao = producoes[j].trim();
                StringBuilder novoLadoDireito = new StringBuilder();
                List<String> variaveis = new ArrayList<>();

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
                    while (variaveis.size() > 2) {
                        String chave = variaveis.get(variaveis.size() - 2) + variaveis.get(variaveis.size() - 1); // Concatena as duas últimas variáveis
                        String novaVariavel; // Nova variável para substituir as duas últimas
                        if (substituicoes.containsKey(chave)) { // Se a substituição já foi feita
                            novaVariavel = substituicoes.get(chave); // Pega a variável substituída
                        } else {
                            novaVariavel = "T" + contadorVariaveis++; // Cria uma nova variável
                            substituicoes.put(chave, novaVariavel);
                            novasRegras.add(novaVariavel + " -> " + chave);
                        }
                        variaveis.remove(variaveis.size() - 1);
                        variaveis.set(variaveis.size() - 1, novaVariavel);
                    }

                    for (String variavel : variaveis) {
                        novoLadoDireito.append(variavel);
                    }

                    novaRegra.append(novoLadoDireito.toString());
                } else {
                    novaRegra.append(producao);
                }

                if (j < producoes.length - 1) {
                    novaRegra.append(" | ");
                }
            }

            novasRegras.set(i, novaRegra.toString());
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
