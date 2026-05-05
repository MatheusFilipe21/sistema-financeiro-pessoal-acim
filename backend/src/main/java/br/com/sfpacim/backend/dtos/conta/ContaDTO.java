package br.com.sfpacim.backend.dtos.conta;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados públicos de uma
 * {@link Conta}.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id           O identificador único da conta.
 * @param nome         O nome da conta.
 * @param instituicao  A instituição financeira.
 * @param saldoInicial O saldo inicial cadastrado.
 * @param saldoAtual   O saldo atualizado (calculado).
 * @param pessoaId     O identificador único da pessoa titular da conta.
 */
@Schema(description = "${conta.descricao.schema.leitura}")
public record ContaDTO(

        @Schema(description = "${conta.descricao.id}", example = "${conta.exemplo.id}") //
        UUID id,

        @Schema(description = "${conta.descricao.nome}", example = "${conta.exemplo.nome}") //
        String nome,

        @Schema(description = "${conta.descricao.instituicao}", example = "${conta.exemplo.instituicao}") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "${conta.descricao.saldo-inicial}", example = "${conta.exemplo.saldo-inicial}") //
        BigDecimal saldoInicial,

        @Schema(description = "${conta.descricao.saldo-atual}", example = "${conta.exemplo.saldo-atual}") //
        BigDecimal saldoAtual,

        @Schema(description = "${conta.descricao.pessoa-id}", example = "${pessoa.exemplo.id}") //
        UUID pessoaId) {

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
                conta.getPessoa().getId());
    }
}
