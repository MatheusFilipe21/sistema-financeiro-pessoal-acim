package br.com.sfpacim.backend.utils;

import java.text.Collator;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;

/**
 * Classe utilitária contendo métodos estáticos e puros para apoio
 * às regras de negócio e formatação de dados do sistema.
 */
public class MetodosUteis {

    /**
     * Padrão para extrair diacríticos de uma String.
     */
    private static final Pattern DIACRITICOS = Pattern.compile("\\p{M}");

    /**
     * Construtor privado para evitar instanciação da classe utilitária.
     */
    private MetodosUteis() {

    }

    /**
     * Retorna uma instância de {@link Collator} configurada para o Brasil.
     *
     * <p>
     * Ignora acentuação e diferença entre maiúsculas e minúsculas.
     * Ideal para ordenação e comparação de Strings de forma natural.
     *
     * @return O {@link Collator} devidamente configurado.
     * 
     * @author Matheus F. N. Pereira
     */
    public static Collator collator() {
        Locale locale = Locale.of("pt", "BR");
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        return collator;
    }

    /**
     * Valida a regra de unicidade de um registro. Lança exceção padronizada se
     * houver duplicidade.
     *
     * <p>
     * Exemplo de saída: "Já existe uma pessoa cadastrada com o nome 'Matheus'."
     *
     * @param existeDuplicado Booleano indicando se a duplicidade foi detectada no
     *                        banco.
     * @param nomeEntidade    O tipo do registro no feminino (ex: "pessoa",
     *                        "categoria", "conta").
     * @param valorDuplicado  O nome que causou o conflito.
     * 
     * @throws ViolacaoDadosException Se o parâmetro existeDuplicado for verdadeiro.
     * 
     * @author Matheus F. N. Pereira
     */
    public static void validarUnicidade(boolean existeDuplicado, String nomeEntidade, String valorDuplicado) {
        if (existeDuplicado) {
            throw new ViolacaoDadosException(
                    String.format("Já existe uma %s cadastrada com o nome '%s'.", nomeEntidade, valorDuplicado));
        }
    }

    /**
     * Valida a regra de unicidade incluindo um contexto adicional de pertencimento.
     *
     * <p>
     * Exemplo de saída: "Já existe uma conta 'Nubank' cadastrada para Matheus."
     *
     * @param existeDuplicado   Booleano indicando duplicidade.
     * @param nomeEntidade      O tipo do registro (ex: "conta").
     * @param valorDuplicado    O nome que causou o conflito.
     * @param contextoAdicional Complemento para a mensagem indicando a posse.
     * 
     * @throws ViolacaoDadosException Se o parâmetro existeDuplicado for verdadeiro.
     * 
     * @author Matheus F. N. Pereira
     */
    public static void validarUnicidade(boolean existeDuplicado, String nomeEntidade, String valorDuplicado,
            String contextoAdicional) {
        if (existeDuplicado) {
            throw new ViolacaoDadosException(
                    String.format("Já existe uma %s '%s' cadastrada para %s.", nomeEntidade, valorDuplicado,
                            contextoAdicional));
        }
    }

    /**
     * Valida se existem dependências ativas impedindo uma exclusão.
     *
     * <p>
     * Caso a lista de dependências não esteja vazia, a execução é interrompida
     * e uma exceção de regra de negócio é lançada com uma mensagem formatada
     * dinamicamente baseada nos itens da lista.
     *
     * @param nomeAlvo     O nome da entidade que está sendo validada (ex:
     *                     "Matheus").
     * @param dependencias A lista contendo os nomes das dependências encontradas.
     * 
     * @throws ViolacaoDadosException Se a lista de dependências não estiver vazia.
     * 
     * @author Matheus F. N. Pereira
     */
    public static void validarDependenciasExclusao(String nomeAlvo, List<String> dependencias) {
        if (dependencias == null || dependencias.isEmpty()) {
            return;
        }

        String mensagemErro = gerarMensagemDependenciasExclusao(nomeAlvo, dependencias);
        throw new ViolacaoDadosException(mensagemErro);
    }

    /**
     * Gera a mensagem de erro dinâmica gramaticalmente correta.
     *
     * <p>
     * Concatena os elementos da lista utilizando vírgulas e a conjunção "e"
     * para o último elemento (ex: "Contas, Cartões e Transações").
     *
     * @param nomeAlvo     O nome da entidade alvo da exclusão.
     * @param dependencias A lista de dependências ativas.
     * 
     * @return A mensagem de erro formatada para exibição ao usuário.
     * 
     * @author Matheus F. N. Pereira
     */
    private static String gerarMensagemDependenciasExclusao(String nomeAlvo, List<String> dependencias) {
        int ultimoIndice = dependencias.size() - 1;

        String textoDependencias = ultimoIndice == 0
                ? dependencias.get(0)
                : String.join(", ", dependencias.subList(0, ultimoIndice)) + " e " + dependencias.get(ultimoIndice);

        return String.format(
                "Não é possível excluir '%s' pois existem %s vinculadas. Remova os registros vinculados primeiro.",
                nomeAlvo, textoDependencias);
    }

    /**
     * Normaliza uma string para fins de comparação e busca.
     *
     * <p>
     * O processo envolve:
     * 1. Remover espaços em branco nas extremidades.
     * 2. Converter para caixa baixa (lowercase).
     * 3. Decompor caracteres acentuados (NFD) e remover os diacríticos.
     * Exemplo: " São Paulo " vira "sao paulo".
     * 
     * @param texto O texto original.
     * 
     * @return O texto normalizado ou string vazia se nulo.
     * 
     * @author Matheus F. N. Pereira
     */
    public static String normalizarParaBusca(String texto) {
        if (texto == null) {
            return "";
        }

        String temp = texto.trim().toLowerCase();

        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);

        return DIACRITICOS.matcher(temp).replaceAll("");
    }
}
