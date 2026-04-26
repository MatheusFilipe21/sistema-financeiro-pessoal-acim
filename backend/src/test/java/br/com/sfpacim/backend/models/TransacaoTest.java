package br.com.sfpacim.backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para a entidade {@link Transacao}.
 *
 * <p>
 * Garante a integridade das validações de estado da transação (pendente vs
 * pago) e o correto funcionamento da sua construção.
 *
 * @author Matheus F. N. Pereira
 */
class TransacaoTest {

    private static final String DESCRICAO = "Desenvolvimento de Software";
    private static final BigDecimal VALOR = new BigDecimal("150.00");
    private static final LocalDate DATA_ATUAL = LocalDate.now();
    private static final TipoTransacao TIPO = TipoTransacao.RECEITA;
    private static final StatusTransacao STATUS = StatusTransacao.PENDENTE;

    private Transacao transacao;

    /**
     * Inicializa a entidade básica antes de cada teste utilizando o Builder.
     */
    @BeforeEach
    void setUp() {
        transacao = Transacao.builder()
                .descricao(DESCRICAO)
                .valor(VALOR)
                .dataCompetencia(DATA_ATUAL)
                .dataVencimento(DATA_ATUAL)
                .tipo(TIPO)
                .status(STATUS)
                .build();
    }

    /**
     * Testa o método de verificação de pagamento quando o status reflete a
     * quitação do lançamento.
     */
    @Test
    @DisplayName("isPago quando status for PAGO, deve retornar verdadeiro")
    void testIsPago_QuandoStatusForPago_DeveRetornarVerdadeiro() {
        transacao.setStatus(StatusTransacao.PAGO);

        assertTrue(transacao.isPago(), "Deveria retornar verdadeiro pois o status é PAGO");
    }

    /**
     * Testa o método de verificação de pagamento quando o status reflete um
     * lançamento ainda em aberto.
     */
    @Test
    @DisplayName("isPago quando status for PENDENTE, deve retornar falso")
    void testIsPago_QuandoStatusForPendente_DeveRetornarFalso() {
        transacao.setStatus(StatusTransacao.PENDENTE);

        assertFalse(transacao.isPago(), "Deveria retornar falso pois o status é PENDENTE");
    }

    /**
     * Testa o método de verificação de pagamento para o cenário atípico de
     * ausência de status, garantindo que não ocorra NullPointerException.
     */
    @Test
    @DisplayName("isPago quando status for nulo, deve retornar falso e não lançar exceção")
    void testIsPago_QuandoStatusForNulo_DeveRetornarFalso() {
        transacao.setStatus(null);

        assertFalse(transacao.isPago(), "Deveria retornar falso em vez de lançar NullPointerException");
    }
}
