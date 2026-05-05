package br.com.sfpacim.backend.dtos.conta;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) para recebimento de dados de criação e
 * atualização de contas.
 *
 * <p>
 * Este record aplica as validações de negócio usando o Spring Validation.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome         Nome da conta (Obrigatório).
 * @param instituicao  Instituição financeira (Enum, Obrigatório).
 * @param saldoInicial Saldo inicial da conta (Obrigatório).
 * @param pessoaId     ID da pessoa titular da conta (Obrigatório).
 */
@Schema(description = "${conta.descricao.schema.criacao-atualizacao}")
public record CriarAtualizarContaDTO(

        @Schema(description = "${conta.descricao.nome}", example = "${conta.exemplo.nome}") //
        @NotBlank(message = "{geral.validacao.nome.obrigatorio}") //
        String nome,

        @Schema(description = "${conta.descricao.instituicao}", example = "${conta.exemplo.instituicao}") //
        @NotNull(message = "{conta.validacao.instituicao.obrigatoria}") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "${conta.descricao.saldo-inicial}", example = "${conta.exemplo.saldo-inicial.novo}") //
        @NotNull(message = "{conta.validacao.saldo-inicial.obrigatorio}") //
        BigDecimal saldoInicial,

        @Schema(description = "${conta.descricao.pessoa-id}", example = "${pessoa.exemplo.id}") //
        @NotNull(message = "{conta.validacao.pessoa-id.obrigatorio}") //
        UUID pessoaId) {
}
