package br.com.sfpacim.backend.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Categoria;

/**
 * Testes unitários para a classe utilitária {@link MetodosUteis}.
 *
 * <p>
 * Cobre a formatação dinâmica de mensagens de erro, regras de negócio e
 * predicados dinâmicos da JPA.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class MetodosUteisTest {

    @Mock
    private MessageSource messageSource;

    /**
     * Testa o bloqueio de instanciação no construtor da classe.
     */
    @Test
    @DisplayName("Construtor: Deve ser privado para impedir instanciação")
    void testeConstrutorPrivado() throws Exception {
        Constructor<MetodosUteis> constructor = MetodosUteis.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    /**
     * Testa o comportamento de segurança ao receber chave nula no método
     * {@link MetodosUteis#obterMensagem(MessageSource, String, Object...)}.
     */
    @Test
    @DisplayName("obterMensagem: Quando chave nula, deve retornar nulo")
    void testeObterMensagem_QuandoChaveNula_DeveRetornarNulo() {
        assertNull(MetodosUteis.obterMensagem(messageSource, null));
    }

    /**
     * Testa o sucesso na extração da mensagem do properties no método
     * {@link MetodosUteis#obterMensagem(MessageSource, String, Object...)}.
     */
    @Test
    @DisplayName("obterMensagem: Quando sucesso, deve retornar string formatada")
    void testeObterMensagem_QuandoSucesso_DeveRetornarMensagem() {
        when(messageSource.getMessage(eq("chave.teste"), any(), any())).thenReturn("Mensagem de Teste");
        assertEquals("Mensagem de Teste", MetodosUteis.obterMensagem(messageSource, "chave.teste"));
    }

    /**
     * Testa o fallback quando a chave não existe no properties no método
     * {@link MetodosUteis#obterMensagem(MessageSource, String, Object...)}.
     */
    @Test
    @DisplayName("obterMensagem: Quando exceção, deve retornar a própria chave (Fallback)")
    void testeObterMensagem_QuandoExcecao_DeveRetornarChave() {
        when(messageSource.getMessage(anyString(), any(), any())).thenThrow(NoSuchMessageException.class);
        assertEquals("chave.inexistente", MetodosUteis.obterMensagem(messageSource, "chave.inexistente"));
    }

    /**
     * Testa a montagem da exceção de unicidade com injeção de parâmetros no método
     * {@link MetodosUteis#gerarExcecaoUnicidade(MessageSource, Class, String, Object...)}.
     */
    @Test
    @DisplayName("gerarExcecaoUnicidade: Deve montar mensagem traduzindo a entidade e os argumentos")
    void testeGerarExcecaoUnicidade() {
        when(messageSource.getMessage(eq("categoria.nome.singular"), any(), any())).thenReturn("Categoria");
        when(messageSource.getMessage(eq("erro.duplicado"), any(), any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArgument(1);
            return String.format("%s duplicada: %s", args[0], args[1]);
        });

        ViolacaoDadosException excecao = MetodosUteis.gerarExcecaoUnicidade(
                messageSource, Categoria.class, "erro.duplicado", "Streaming");

        assertEquals("Categoria duplicada: Streaming", excecao.getMessage());
    }

    /**
     * Testa o disparo da exceção de duplicidade no método
     * {@link MetodosUteis#validarUnicidade(MessageSource, boolean, Class, String, Object...)}.
     */
    @Test
    @DisplayName("validarUnicidade: Quando duplicado, deve lançar exceção")
    void testeValidarUnicidade_QuandoDuplicado_DeveLancarExcecao() {
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Erro genérico");

        assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarUnicidade(messageSource, true, Categoria.class, "chave"));
    }

    /**
     * Testa o fluxo limpo quando não há duplicidade no método
     * {@link MetodosUteis#validarUnicidade(MessageSource, boolean, Class, String, Object...)}.
     */
    @Test
    @DisplayName("validarUnicidade: Quando não duplicado, deve passar silenciosamente")
    void testeValidarUnicidade_QuandoNaoDuplicado_NaoFazNada() {
        assertDoesNotThrow(() -> MetodosUteis.validarUnicidade(messageSource, false, Categoria.class, "chave"));
    }

    /**
     * Testa as ramificações de early-return ao não identificar dependências no
     * método
     * {@link MetodosUteis#validarDependenciasExclusao(MessageSource, String, List)}.
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Quando lista nula ou vazia, não faz nada")
    void testeValidarDependenciasExclusao_QuandoVazia_NaoFazNada() {
        assertDoesNotThrow(() -> MetodosUteis.validarDependenciasExclusao(messageSource, "Alvo", null));
        assertDoesNotThrow(() -> MetodosUteis.validarDependenciasExclusao(messageSource, "Alvo", List.of()));
    }

    /**
     * Testa a formatação gramatical para uma única dependência no método
     * {@link MetodosUteis#validarDependenciasExclusao(MessageSource, String, List)}.
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Com 1 item, formata sem vírgulas ou conjunções")
    void testeDependenciasExclusao_UmItem() {
        when(messageSource.getMessage(eq("erro.exclusao.dependencias"), any(), any())).thenAnswer(inv -> {
            Object[] args = inv.getArgument(1);
            return args[0] + " tem vínculos: " + args[1];
        });

        ViolacaoDadosException ex = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarDependenciasExclusao(messageSource, "João", List.of("Contas")));
        assertEquals("João tem vínculos: Contas", ex.getMessage());
    }

    /**
     * Testa a formatação gramatical ligando duas dependências no método
     * {@link MetodosUteis#validarDependenciasExclusao(MessageSource, String, List)}.
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Com 2 itens, formata ligando com 'e'")
    void testeDependenciasExclusao_DoisItens() {
        when(messageSource.getMessage(eq("erro.exclusao.dependencias"), any(), any())).thenAnswer(inv -> {
            Object[] args = inv.getArgument(1);
            return args[0] + " tem vínculos: " + args[1];
        });

        ViolacaoDadosException ex = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarDependenciasExclusao(messageSource, "João", List.of("Contas", "Transações")));
        assertEquals("João tem vínculos: Contas e Transações", ex.getMessage());
    }

    /**
     * Testa a formatação gramatical complexa para três ou mais dependências no
     * método
     * {@link MetodosUteis#validarDependenciasExclusao(MessageSource, String, List)}.
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Com 3 itens, formata lista gramaticalmente com vírgula e 'e'")
    void testeDependenciasExclusao_TresItens() {
        when(messageSource.getMessage(eq("erro.exclusao.dependencias"), any(), any())).thenAnswer(inv -> {
            Object[] args = inv.getArgument(1);
            return args[0] + " tem vínculos: " + args[1];
        });

        ViolacaoDadosException ex = assertThrows(ViolacaoDadosException.class, () -> MetodosUteis
                .validarDependenciasExclusao(messageSource, "João", List.of("Contas", "Cartões", "Transações")));
        assertEquals("João tem vínculos: Contas, Cartões e Transações", ex.getMessage());
    }

    /**
     * Testa a montagem do predicado de busca textual via JPA Criteria no método
     * {@link MetodosUteis#adicionarFiltroTextual(List, CriteriaBuilder, Expression, String)}.
     */
    @Test
    @DisplayName("adicionarFiltroTextual: Deve aplicar LIKE quando termo for válido")
    @SuppressWarnings("unchecked")
    void testeAdicionarFiltroTextual() {
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Expression<String> campo = mock(Expression.class);
        Expression<String> funcExp = mock(Expression.class);
        Predicate likePredicate = mock(Predicate.class);

        when(cb.lower(campo)).thenReturn(funcExp);
        when(cb.function(anyString(), eq(String.class), any())).thenReturn(funcExp);
        when(cb.literal(anyString())).thenReturn(funcExp);
        when(cb.like(any(Expression.class), any(Expression.class))).thenReturn(likePredicate);

        MetodosUteis.adicionarFiltroTextual(predicates, cb, campo, " Busca ");

        assertEquals(1, predicates.size());
        verify(cb).literal("%busca%");
    }

    /**
     * Testa o descarte seguro de predicados com termos vazios no método
     * {@link MetodosUteis#adicionarFiltroTextual(List, CriteriaBuilder, Expression, String)}.
     */
    @Test
    @DisplayName("adicionarFiltroTextual: Não deve adicionar se o termo for vazio")
    @SuppressWarnings("unchecked")
    void testeAdicionarFiltroTextual_QuandoVazio_Ignora() {
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Expression<String> campo = mock(Expression.class);

        MetodosUteis.adicionarFiltroTextual(predicates, cb, campo, "   ");
        assertTrue(predicates.isEmpty());
    }

    /**
     * Testa a montagem de predicado IN em listas populadas no método
     * {@link MetodosUteis#adicionarFiltroIn(List, Path, List)}.
     */
    @Test
    @DisplayName("adicionarFiltroIn: Deve adicionar predicado quando lista possuir itens")
    @SuppressWarnings("unchecked")
    void testeAdicionarFiltroIn_QuandoTemValores() {
        List<Predicate> predicates = new ArrayList<>();
        Path<String> campo = mock(Path.class);
        Predicate inPredicate = mock(Predicate.class);

        when(campo.in(any(List.class))).thenReturn(inPredicate);

        MetodosUteis.adicionarFiltroIn(predicates, campo, List.of("A", "B"));
        assertEquals(1, predicates.size());
    }

    /**
     * Testa a omissão segura do predicado IN ao receber listas nulas ou vazias no
     * método
     * {@link MetodosUteis#adicionarFiltroIn(List, Path, List)}.
     */
    @Test
    @DisplayName("adicionarFiltroIn: Não deve adicionar quando nulo ou vazio")
    @SuppressWarnings("unchecked")
    void testeAdicionarFiltroIn_QuandoVazio() {
        List<Predicate> predicates = new ArrayList<>();
        Path<String> campo = mock(Path.class);

        MetodosUteis.adicionarFiltroIn(predicates, campo, null);
        MetodosUteis.adicionarFiltroIn(predicates, campo, List.of());
        assertTrue(predicates.isEmpty());
    }

    /**
     * Testa a montagem e o comportamento seguro dos comparadores lógicos nos
     * métodos
     * {@link MetodosUteis#adicionarFiltroMaiorOuIgual} e
     * {@link MetodosUteis#adicionarFiltroMenorOuIgual}.
     */
    @Test
    @DisplayName("adicionarFiltroMatematico: MaiorOuIgual e MenorOuIgual devem respeitar valores não nulos")
    @SuppressWarnings("unchecked")
    void testeAdicionarFiltroMatematico() {
        List<Predicate> predicates = new ArrayList<>();
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<BigDecimal> campo = mock(Path.class);
        Predicate mathPredicate = mock(Predicate.class);

        when(cb.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(mathPredicate);
        when(cb.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(mathPredicate);

        MetodosUteis.adicionarFiltroMaiorOuIgual(predicates, cb, campo, BigDecimal.TEN);
        MetodosUteis.adicionarFiltroMenorOuIgual(predicates, cb, campo, BigDecimal.ONE);

        MetodosUteis.adicionarFiltroMaiorOuIgual(predicates, cb, campo, null);
        MetodosUteis.adicionarFiltroMenorOuIgual(predicates, cb, campo, null);

        assertEquals(2, predicates.size());
    }
}
