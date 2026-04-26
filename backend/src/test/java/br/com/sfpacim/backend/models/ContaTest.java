package br.com.sfpacim.backend.models;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes unitários para a entidade {@link Conta}.
 *
 * <p>
 * Garante a integridade das regras de negócio encapsuladas no modelo,
 * como inicialização segura de saldos e recálculos automáticos de débitos
 * e créditos.
 *
 * @author Matheus F. N. Pereira
 */
class ContaTest {

    private static final String NOME_CONTA = "Investimentos Mercado Pago";
    private static final InstituicaoFinanceira INSTITUICAO = InstituicaoFinanceira.MERCADO_PAGO;
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1500.50");
    private static final Usuario USUARIO = new Usuario("Matheus Filipe do Nascimento Pereira",
            "matheusfnpereira@gmail.com", "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");
    private static final Pessoa PESSOA = new Pessoa(USUARIO.getNome(), USUARIO);

    private Conta conta;

    /**
     * Configura um objeto {@link Conta} padrão antes de cada teste.
     * 
     * <p>
     * Garante que as dependências (Usuario e Pessoa) tenham IDs para simular
     * objetos persistidos, embora não seja estritamente necessário para teste
     * de unidade pura, ajuda na consistência.
     */
    @BeforeEach
    void setUp() {
        USUARIO.setId(UUID.randomUUID());
        PESSOA.setId(UUID.randomUUID());

        conta = new Conta(NOME_CONTA, INSTITUICAO, SALDO_INICIAL, PESSOA);
    }

    /**
     * Testa o construtor customizado da classe
     * {@link Conta#Conta(String, InstituicaoFinanceira, BigDecimal, Pessoa)}.
     *
     * <p>
     * Verifica se os atributos são inicializados corretamente e, principalmente,
     * se o saldo atual é inicializado com o mesmo valor do saldo inicial.
     */
    @Test
    @DisplayName("Construtor customizado deve inicializar atributos e replicar saldo inicial no atual")
    void testConstrutorCustomizado_QuandoChamado_DeveInicializarAtributos() {
        assertNull(conta.getId(), "O id da Conta deveria ser nulo antes da persistência");
        assertEquals(NOME_CONTA, conta.getNome(), "O nome da Conta deveria ser o esperado");
        assertEquals(INSTITUICAO, conta.getInstituicao(), "A instituição deveria ser a esperada");
        assertEquals(PESSOA, conta.getPessoa(), "A pessoa titular deveria ser a esperada");
        assertEquals(SALDO_INICIAL, conta.getSaldoInicial(), "O saldo inicial deveria ser o esperado");
        assertEquals(SALDO_INICIAL, conta.getSaldoAtual(),
                "O saldo atual deve ser inicializado com o mesmo valor do saldo inicial");
    }

    /**
     * Testa a restrição de nulidade no saldo inicial durante a criação.
     */
    @Test
    @DisplayName("Construtor customizado quando saldo inicial for nulo, deve lançar exceção")
    void testConstrutorCustomizado_QuandoSaldoInicialNulo_DeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Conta(NOME_CONTA, INSTITUICAO, null, PESSOA);
        });

        assertEquals("O saldo inicial não pode ser nulo. Use zero caso a conta esteja vazia.", exception.getMessage());
    }

    /**
     * Testa a regra de negócio de recálculo do saldo atual ao alterar o saldo
     * inicial.
     * 
     * <p>
     * Simula uma conta com movimentações prévias e verifica se a diferença do
     * novo saldo inicial é aplicada corretamente ao saldo atual, preservando o
     * histórico de transações.
     */
    @Test
    @DisplayName("alterarSaldoInicial quando receber valor válido, deve recalcular saldo atual preservando histórico")
    void testAlterarSaldoInicial_QuandoValorValido_DeveRecalcularSaldoAtual() {
        conta.creditar(new BigDecimal("100.00"));

        BigDecimal novoSaldoInicial = new BigDecimal("2000.00");
        conta.alterarSaldoInicial(novoSaldoInicial);

        BigDecimal saldoAtualEsperado = new BigDecimal("2100.00");

        assertEquals(novoSaldoInicial, conta.getSaldoInicial(), "O saldo inicial deveria ter sido atualizado");
        assertEquals(saldoAtualEsperado, conta.getSaldoAtual(),
                "O saldo atual deveria ter sido recalculado com a diferença");
    }

    /**
     * Testa a restrição de nulidade na alteração do saldo inicial.
     */
    @Test
    @DisplayName("alterarSaldoInicial quando valor for nulo, deve lançar exceção")
    void testAlterarSaldoInicial_QuandoValorNulo_DeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conta.alterarSaldoInicial(null);
        });

        assertEquals("O novo saldo inicial não pode ser nulo.", exception.getMessage());
    }

    /**
     * Testa a operação de adição de valores ao saldo da conta.
     */
    @Test
    @DisplayName("creditar quando valor for maior que zero, deve somar ao saldo atual")
    void testCreditar_QuandoValorValido_DeveSomaAoSaldoAtual() {
        BigDecimal valorCredito = new BigDecimal("250.00");
        conta.creditar(valorCredito);

        BigDecimal saldoAtualEsperado = new BigDecimal("1750.50");

        assertEquals(saldoAtualEsperado, conta.getSaldoAtual(), "O saldo atual deve refletir o crédito adicionado");
        assertEquals(SALDO_INICIAL, conta.getSaldoInicial(), "O saldo inicial não deve ser alterado por movimentações");
    }

    /**
     * Testa as restrições da operação de crédito para valores matematicamente
     * inválidos na regra de negócio.
     * 
     * @param valorString Valores simulados via ValueSource.
     */
    @ParameterizedTest
    @ValueSource(strings = { "0.00", "-50.00" })
    @DisplayName("creditar quando valor for zero ou negativo, deve lançar exceção")
    void testCreditar_QuandoValorZeroOuNegativo_DeveLancarExcecao(String valorString) {
        BigDecimal valorInvalido = new BigDecimal(valorString);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conta.creditar(valorInvalido);
        });

        assertEquals("O valor para crédito deve ser maior que zero.", exception.getMessage());
    }

    /**
     * Testa a restrição de nulidade na operação de crédito.
     */
    @Test
    @DisplayName("creditar quando valor for nulo, deve lançar exceção")
    void testCreditar_QuandoValorNulo_DeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conta.creditar(null);
        });

        assertEquals("O valor para crédito deve ser maior que zero.", exception.getMessage());
    }

    /**
     * Testa a operação de subtração de valores do saldo da conta.
     */
    @Test
    @DisplayName("debitar quando valor for maior que zero, deve subtrair do saldo atual")
    void testDebitar_QuandoValorValido_DeveSubtrairDoSaldoAtual() {
        BigDecimal valorDebito = new BigDecimal("500.50");
        conta.debitar(valorDebito);

        BigDecimal saldoAtualEsperado = new BigDecimal("1000.00");

        assertEquals(saldoAtualEsperado, conta.getSaldoAtual(), "O saldo atual deve refletir o débito subtraído");
        assertEquals(SALDO_INICIAL, conta.getSaldoInicial(), "O saldo inicial não deve ser alterado por movimentações");
    }

    /**
     * Testa as restrições da operação de débito para valores matematicamente
     * inválidos na regra de negócio.
     *
     * @param valorString Valores simulados via ValueSource.
     */
    @ParameterizedTest
    @ValueSource(strings = { "0.00", "-15.00" })
    @DisplayName("debitar quando valor for zero ou negativo, deve lançar exceção")
    void testDebitar_QuandoValorZeroOuNegativo_DeveLancarExcecao(String valorString) {
        BigDecimal valorInvalido = new BigDecimal(valorString);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conta.debitar(valorInvalido);
        });

        assertEquals("O valor para débito deve ser maior que zero.", exception.getMessage());
    }

    /**
     * Testa a restrição de nulidade na operação de débito.
     */
    @Test
    @DisplayName("debitar quando valor for nulo, deve lançar exceção")
    void testDebitar_QuandoValorNulo_DeveLancarExcecao() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conta.debitar(null);
        });

        assertEquals("O valor para débito deve ser maior que zero.", exception.getMessage());
    }
}
