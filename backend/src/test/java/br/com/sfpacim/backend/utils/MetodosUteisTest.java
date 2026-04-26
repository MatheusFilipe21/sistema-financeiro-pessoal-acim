package br.com.sfpacim.backend.utils;

import java.text.Collator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para a classe utilitária {@link MetodosUteis}.
 *
 * <p>
 * Cobre a normalização de strings, formatação dinâmica de mensagens de erro
 * e regras de collation, garantindo 100% de cobertura de código e ramificações.
 *
 * @author Matheus F. N. Pereira
 */
class MetodosUteisTest {

    /**
     * Testa as regras de ordenação e comparação do método
     * {@link MetodosUteis#collator()}.
     *
     * <p>
     * Garante que a instância de Collator está configurada com força PRIMÁRIA
     * (PRIMARY),
     * ignorando diferenças de acentuação e de letras maiúsculas/minúsculas.
     */
    @Test
    @DisplayName("collator: Deve configurar comparador ignorando acentos e case")
    void testeCollator() {
        Collator collator = MetodosUteis.collator();

        assertEquals(0, collator.compare("João", "joao"), "Deve ignorar acentuação e maiúsculas");
        assertEquals(0, collator.compare("MACA", "Maçã"), "Deve ignorar acentuação e maiúsculas");
        assertTrue(collator.compare("Ana", "Zélia") < 0, "Deve ordenar alfabeticamente ('A' vem antes de 'Z')");
    }

    /**
     * Testa a limpeza de formatação do método
     * {@link MetodosUteis#normalizarParaBusca(String)}.
     *
     * <p>
     * Cobre o cenário ideal, onde espaços sobressalentes são removidos (trim),
     * letras maiúsculas são rebaixadas e a acentuação é decomposta e expurgada via
     * Regex.
     */
    @Test
    @DisplayName("normalizarParaBusca: Deve remover acentos, espaços e converter para minúsculas")
    void testeNormalizarParaBusca_Sucesso() {
        String resultado = MetodosUteis.normalizarParaBusca("  São Paulo  ");
        assertEquals("sao paulo", resultado);

        resultado = MetodosUteis.normalizarParaBusca("ÁéîõÜç");
        assertEquals("aeiouc", resultado);
    }

    /**
     * Testa as ramificações de segurança do método
     * {@link MetodosUteis#normalizarParaBusca(String)}.
     *
     * <p>
     * Garante que entradas nulas ou compostas apenas por espaços em branco não
     * quebrem
     * a lógica de validação de unicidade.
     */
    @Test
    @DisplayName("normalizarParaBusca: Quando string for nula ou vazia, deve retornar string vazia")
    void testeNormalizarParaBusca_NuloOuVazio() {
        assertEquals("", MetodosUteis.normalizarParaBusca(null));
        assertEquals("", MetodosUteis.normalizarParaBusca("   "));
    }

    /**
     * Testa a ramificação verdadeira do método simples de validação.
     *
     * <p>
     * Garante o lançamento da {@link ViolacaoDadosException} com a formatação
     * padrão para entidades de nível raiz (ex: Pessoas e Categorias).
     */
    @Test
    @DisplayName("validarUnicidade (Simples): Quando duplicado, deve lançar exceção padronizada")
    void testeValidarUnicidadeSimples_QuandoDuplicado_DeveLancarExcecao() {
        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarUnicidade(true, "Pessoa", "Matheus"));

        assertEquals("Já existe uma Pessoa cadastrada com o nome 'Matheus'.", excecao.getMessage());
    }

    /**
     * Testa a ramificação falsa do método simples de validação.
     *
     * <p>
     * Garante que o fluxo do sistema continua normalmente quando a flag indica
     * inexistência de conflitos.
     */
    @Test
    @DisplayName("validarUnicidade (Simples): Quando NÃO duplicado, não deve fazer nada")
    void testeValidarUnicidadeSimples_QuandoNaoDuplicado() {
        assertDoesNotThrow(() -> MetodosUteis.validarUnicidade(false, "Pessoa", "Matheus"));
    }

    /**
     * Testa a ramificação verdadeira do método contextual de validação.
     *
     * <p>
     * Garante o lançamento da {@link ViolacaoDadosException} formatada para
     * entidades
     * que pertencem à outra (ex: Contas vinculadas a Pessoas).
     */
    @Test
    @DisplayName("validarUnicidade (Contextual): Quando duplicado, deve lançar exceção com dono")
    void testeValidarUnicidadeContextual_QuandoDuplicado_DeveLancarExcecao() {
        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarUnicidade(true, "Conta", "Nubank", "Matheus"));

        assertEquals("Já existe uma Conta 'Nubank' cadastrada para Matheus.", excecao.getMessage());
    }

    /**
     * Testa a ramificação falsa do método contextual de validação.
     */
    @Test
    @DisplayName("validarUnicidade (Contextual): Quando NÃO duplicado, não deve fazer nada")
    void testeValidarUnicidadeContextual_QuandoNaoDuplicado() {
        assertDoesNotThrow(() -> MetodosUteis.validarUnicidade(false, "Conta", "Nubank", "Matheus"));
    }

    /**
     * Testa a blindagem do gerador de mensagens dinâmicas contra listas vazias.
     *
     * <p>
     * Cobre o IF de interrupção precoce no método
     * {@link MetodosUteis#validarDependenciasExclusao(String, List)}.
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Quando nulo ou vazio, não deve lançar exceção")
    void testeDependenciasExclusao_QuandoVazioOuNulo() {
        List<String> listaVazia = List.of();

        assertDoesNotThrow(() -> MetodosUteis.validarDependenciasExclusao("Alvo", null));
        assertDoesNotThrow(() -> MetodosUteis.validarDependenciasExclusao("Alvo", listaVazia));
    }

    /**
     * Testa a formatação gramatical para uma única dependência.
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Com 1 item, formata sem vírgulas ou conjunções")
    void testeDependenciasExclusao_UmItem() {
        List<String> dependencias = List.of("Contas");

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarDependenciasExclusao("João", dependencias));

        assertEquals(
                "Não é possível excluir 'João' pois existem Contas vinculadas. Remova os registros vinculados primeiro.",
                excecao.getMessage());
    }

    /**
     * Testa a formatação gramatical conectando duas dependências.
     *
     * <p>
     * Cobre o uso da conjunção "e" sem uso de vírgulas (Ex: A e B).
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Com 2 itens, formata ligando com 'e'")
    void testeDependenciasExclusao_DoisItens() {
        List<String> dependencias = List.of("Contas", "Transações");

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarDependenciasExclusao("João", dependencias));

        assertEquals(
                "Não é possível excluir 'João' pois existem Contas e Transações vinculadas. Remova os registros vinculados primeiro.",
                excecao.getMessage());
    }

    /**
     * Testa a formatação gramatical complexa para múltiplas dependências.
     *
     * <p>
     * Cobre o uso do particionamento de lista (subList) para aplicar vírgulas e
     * finalizar a enumeração com a conjunção "e" (Ex: A, B e C).
     */
    @Test
    @DisplayName("validarDependenciasExclusao: Com 3 itens, formata lista gramaticalmente com vírgula e 'e'")
    void testeDependenciasExclusao_TresItens() {
        List<String> dependencias = List.of("Contas", "Cartões", "Transações");

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> MetodosUteis.validarDependenciasExclusao("João", dependencias));

        assertEquals(
                "Não é possível excluir 'João' pois existem Contas, Cartões e Transações vinculadas. Remova os registros vinculados primeiro.",
                excecao.getMessage());
    }
}
