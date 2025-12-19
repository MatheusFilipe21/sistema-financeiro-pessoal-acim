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
 * Este record é usado exclusivamente como corpo da requisição (@RequestBody)
 * nos endpoints POST e PUT. Contém as validações necessárias para garantir
 * a integridade dos dados antes de chegarem à camada de serviço.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome         Nome da conta (Obrigatório).
 * @param instituicao  Instituição financeira (Enum, Obrigatório).
 * @param saldoInicial Saldo inicial da conta (Obrigatório).
 * @param pessoaId     ID da pessoa titular da conta (Obrigatório).
 */
@Schema(description = "DTO utilizado para cadastrar ou atualizar uma conta bancária.")
public record CriarAtualizarContaDTO(

        @Schema(description = "Nome identificador da conta.", example = "Investimentos Mercado Pago") //
        @NotBlank(message = "O nome é obrigatório.") //
        String nome,

        @Schema(description = "Instituição financeira vinculada.", example = "MERCADO_PAGO") //
        @NotNull(message = "A instituição financeira é obrigatória.") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "Saldo inicial da conta.", example = "1500.50") //
        @NotNull(message = "O saldo inicial é obrigatório.") //
        BigDecimal saldoInicial,

        @Schema(description = "ID da pessoa titular da conta.", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479") //
        @NotNull(message = "A pessoa titular é obrigatória.") //
        UUID pessoaId) {
}
