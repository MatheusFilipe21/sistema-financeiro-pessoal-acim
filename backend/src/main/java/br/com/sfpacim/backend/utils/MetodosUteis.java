package br.com.sfpacim.backend.utils;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;

/**
 * Classe utilitária contendo métodos estáticos e puros para apoio
 * às regras de negócio e formatação de dados do sistema.
 */
public class MetodosUteis {

    /**
     * Construtor privado para evitar instanciação da classe utilitária.
     */
    private MetodosUteis() {

    }

    /**
     * Busca a mensagem no arquivo properties injetando parâmetros dinâmicos.
     * Substitui o uso de String.format() para manter compatibilidade com i18n.
     *
     * @param messageSource A instância do MessageSource.
     * @param chave         A chave da mensagem no arquivo properties.
     * @param args          Argumentos varargs para substituir {0}, {1}, etc.
     * @return O texto traduzido e formatado, ou a chave como fallback.
     * 
     * @author Matheus F. N. Pereira
     */
    public static String obterMensagem(MessageSource messageSource, String chave, Object... args) {
        if (chave == null) {
            return null;
        }

        try {
            return messageSource.getMessage(chave, args, LocaleContextHolder.getLocale());
        } catch (Exception _) {
            return chave;
        }
    }

    /**
     * Busca a mensagem no arquivo properties com base no idioma atual da
     * requisição.
     *
     * @param messageSource A instância do MessageSource.
     * @param chave         A chave da mensagem no arquivo properties.
     * @return O texto traduzido ou a própria chave como fallback caso não seja
     *         encontrada.
     * 
     * @author Matheus F. N. Pereira
     */
    public static String obterMensagem(MessageSource messageSource, String chave) {
        return obterMensagem(messageSource, chave, (Object[]) null);
    }

    /**
     * Constrói a exceção de violação de dados com a mensagem traduzida e formatada.
     * Utilizado para ser lançado (throw) explicitamente em blocos catch.
     *
     * @param messageSource      A instância do MessageSource.
     * @param classeEntidade     A classe da entidade para extração do nome.
     * @param chaveErro          A chave da mensagem de erro principal.
     * @param argsComplementares Argumentos dinâmicos para substituir {1}, {2}, etc.
     * @return Uma instância de {@link ViolacaoDadosException} pronta para ser
     *         lançada.
     * 
     * @author Matheus F. N. Pereira
     */
    public static ViolacaoDadosException gerarExcecaoUnicidade(MessageSource messageSource, Class<?> classeEntidade,
            String chaveErro, Object... argsComplementares) {

        String prefixo = classeEntidade.getSimpleName().toLowerCase();
        String chaveNomeEntidade = prefixo + ".nome.singular";
        String nomeEntidadeTraduzida = obterMensagem(messageSource, chaveNomeEntidade);

        Object[] argumentosFinais = new Object[argsComplementares.length + 1];
        argumentosFinais[0] = nomeEntidadeTraduzida;

        if (argsComplementares.length > 0) {
            System.arraycopy(argsComplementares, 0, argumentosFinais, 1, argsComplementares.length);
        }

        String mensagem = obterMensagem(messageSource, chaveErro, argumentosFinais);

        return new ViolacaoDadosException(mensagem);
    }

    /**
     * Valida a regra de unicidade proativamente. Se duplicado, lança a exceção.
     * 
     * @param messageSource      A instância do MessageSource.
     * @param existeDuplicado    O resultado da validação no banco.
     * @param classeEntidade     A classe da entidade alvo.
     * @param chaveErro          A chave da mensagem de erro principal.
     * @param argsComplementares Argumentos dinâmicos complementares.
     * 
     * @author Matheus F. N. Pereira
     */
    public static void validarUnicidade(MessageSource messageSource, boolean existeDuplicado, Class<?> classeEntidade,
            String chaveErro, Object... argsComplementares) {

        if (existeDuplicado) {
            throw gerarExcecaoUnicidade(messageSource, classeEntidade, chaveErro, argsComplementares);
        }
    }

    /**
     * Valida se existem dependências ativas impedindo uma exclusão.
     *
     * @param messageSource A instância do MessageSource para buscar a tradução.
     * @param nomeAlvo      O nome da entidade que está sendo validada (ex:
     *                      "Matheus").
     * @param dependencias  A lista contendo os nomes das dependências encontradas.
     * 
     * @throws ViolacaoDadosException Se a lista de dependências não estiver vazia.
     * 
     * @author Matheus F. N. Pereira
     */
    public static void validarDependenciasExclusao(MessageSource messageSource, String nomeAlvo,
            List<String> dependencias) {
        if (dependencias == null || dependencias.isEmpty()) {
            return;
        }

        String mensagemErro = gerarMensagemDependenciasExclusao(messageSource, nomeAlvo, dependencias);
        throw new ViolacaoDadosException(mensagemErro);
    }

    /**
     * Gera a mensagem de erro dinâmica gramaticalmente correta.
     *
     * @param messageSource A instância do MessageSource para buscar a tradução.
     * @param nomeAlvo      O nome da entidade alvo da exclusão.
     * @param dependencias  A lista de dependências ativas.
     * @return A mensagem de erro formatada para exibição ao usuário.
     * 
     * @author Matheus F. N. Pereira
     */
    private static String gerarMensagemDependenciasExclusao(MessageSource messageSource, String nomeAlvo,
            List<String> dependencias) {
        int ultimoIndice = dependencias.size() - 1;

        String textoDependencias = ultimoIndice == 0
                ? dependencias.get(0)
                : String.join(", ", dependencias.subList(0, ultimoIndice)) + " e " + dependencias.get(ultimoIndice);

        return obterMensagem(messageSource, "erro.exclusao.dependencias", nomeAlvo, textoDependencias);
    }

    /**
     * Adiciona um predicado de busca textual parcial (LIKE) ignorando acentos e
     * caixa. Só adiciona o predicado se o termo de busca for válido.
     *
     * @param predicates  A lista de predicados atual.
     * @param cb          O CriteriaBuilder.
     * @param campoTabela A expressão do campo no banco (ex: root.get("descricao")).
     * @param termoBusca  A string digitada pelo usuário.
     * 
     * @author Matheus F. N. Pereira
     */
    public static void adicionarFiltroTextual(List<Predicate> predicates, CriteriaBuilder cb,
            Expression<String> campoTabela, String termoBusca) {
        if (StringUtils.hasText(termoBusca)) {
            Expression<String> campoNormalizado = cb.function("unaccent", String.class, cb.lower(campoTabela));

            Expression<String> termoNormalizado = cb.function("unaccent", String.class,
                    cb.literal("%" + termoBusca.toLowerCase().trim() + "%"));

            predicates.add(cb.like(campoNormalizado, termoNormalizado));
        }
    }

    /**
     * Adiciona um predicado IN para coleções, caso a lista não seja nula ou vazia.
     * Funciona tanto para atributos diretos (Enum) quanto relacionais (ID).
     *
     * @param predicates A lista de predicados atual.
     * @param campo      O caminho do campo no banco (ex: root.get("tipo") ou
     *                   root.get("categoria").get("id")).
     * @param valores    A lista de valores permitidos para o filtro.
     * @param <T>        O tipo do campo e dos valores da lista.
     * 
     * @author Matheus F. N. Pereira
     */
    public static <T> void adicionarFiltroIn(List<Predicate> predicates, Path<T> campo,
            List<T> valores) {
        if (!CollectionUtils.isEmpty(valores)) {
            predicates.add(campo.in(valores));
        }
    }

    /**
     * Adiciona um predicado Maior ou Igual (>=), caso o valor não seja nulo.
     * Ideal para datas iniciais e valores monetários mínimos.
     *
     * @param predicates A lista de predicados atual.
     * @param cb         O CriteriaBuilder.
     * @param campo      O caminho do campo no banco.
     * @param valor      O valor mínimo a ser comparado.
     * @param <Y>        O tipo do campo (deve ser Comparable, como LocalDate,
     *                   BigDecimal, etc).
     * 
     * @author Matheus F. N. Pereira
     */
    public static <Y extends Comparable<? super Y>> void adicionarFiltroMaiorOuIgual(
            List<Predicate> predicates, CriteriaBuilder cb, Path<Y> campo, Y valor) {
        if (valor != null) {
            predicates.add(cb.greaterThanOrEqualTo(campo, valor));
        }
    }

    /**
     * Adiciona um predicado Menor ou Igual (<=), caso o valor não seja nulo.
     * Ideal para datas finais e valores monetários máximos.
     *
     * @param predicates A lista de predicados atual.
     * @param cb         O CriteriaBuilder.
     * @param campo      O caminho do campo no banco.
     * @param valor      O valor máximo a ser comparado.
     * @param <Y>        O tipo do campo (deve ser Comparable, como LocalDate,
     *                   BigDecimal, etc).
     * 
     * @author Matheus F. N. Pereira
     */
    public static <Y extends Comparable<? super Y>> void adicionarFiltroMenorOuIgual(
            List<Predicate> predicates, CriteriaBuilder cb, Path<Y> campo, Y valor) {
        if (valor != null) {
            predicates.add(cb.lessThanOrEqualTo(campo, valor));
        }
    }
}
