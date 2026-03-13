package br.com.sfpacim.backend.models;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testes unitários para a entidade {@link Conta}.
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
}
