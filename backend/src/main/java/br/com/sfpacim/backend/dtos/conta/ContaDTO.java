package br.com.sfpacim.backend.dtos.conta;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados públicos de uma
 * {@link Conta}.
 *
 * <p>
 * Usado como resposta JSON padrão. Inclui os dados da pessoa titular
 * para facilitar a exibição no frontend.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id           O identificador único da conta.
 * @param nome         O nome da conta.
 * @param instituicao  A instituição financeira.
 * @param saldoInicial O saldo inicial cadastrado.
 * @param saldoAtual   O saldo atualizado (calculado).
 * @param pessoa       DTO com os dados do titular.
 */
@Schema(description = "DTO para representar os dados de uma conta cadastrada.")
public record ContaDTO(

        @Schema(description = "Identificador único da conta.", example = "a1b2c3d4-e5f6-7890-1234-56789abcdef0") //
        UUID id,

        @Schema(description = "Nome da conta.", example = "Investimentos Mercado Pago") //
        String nome,

        @Schema(description = "Instituição financeira.", example = "MERCADO_PAGO") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "Saldo inicial informado no cadastro.", example = "1000.00") //
        BigDecimal saldoInicial,

        @Schema(description = "Saldo atual calculado.", example = "1250.50") //
        BigDecimal saldoAtual,

        @Schema(description = "Dados da pessoa titular da conta.") //
        PessoaDTO pessoa) {

    /**
     * Construtor customizado para mapear/converter a entidade {@link Conta}
     * para este DTO.
     *
     * @param conta A entidade JPA Conta a ser convertida.
     */
    public ContaDTO(Conta conta) {
        this(
                conta.getId(),
                conta.getNome(),
                conta.getInstituicao(),
                conta.getSaldoInicial(),
                conta.getSaldoAtual(),
                new PessoaDTO(conta.getPessoa()));
    }
}
