package br.com.sfpacim.backend.dtos.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para recebimento de dados de criação e
 * atualização de transações.
 *
 * <p>
 * Contém as validações de integridade dos dados de entrada (Bean Validation).
 *
 * @author Matheus F. N. Pereira
 */
@Schema(description = "DTO utilizado para cadastrar ou atualizar uma transação.")
public record CriarAtualizarTransacaoDTO(

        @Schema(description = "Descrição da transação.", example = "Conta de Luz") //
        @NotBlank(message = "A descrição é obrigatória.") //
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.") String descricao,

        @Schema(description = "Valor da transação (deve ser positivo).", example = "150.90") //
        @NotNull(message = "O valor é obrigatório.") //
        @Positive(message = "O valor deve ser maior que zero.") //
        BigDecimal valor,

        @Schema(description = "Data de competência.", example = "2026-02-14") //
        @NotNull(message = "A data de competência é obrigatória.") //
        LocalDate dataCompetencia,

        @Schema(description = "Data de vencimento.", example = "2026-02-20") //
        @NotNull(message = "A data de vencimento é obrigatória.") //
        LocalDate dataVencimento,

        @Schema(description = "Data de pagamento (Opcional).", example = "2026-02-18") //
        LocalDate dataPagamento,

        @Schema(description = "Tipo da transação.", example = "DESPESA") //
        @NotNull(message = "O tipo é obrigatório.") //
        TipoTransacao tipo,

        @Schema(description = "Status da transação.", example = "PENDENTE") //
        @NotNull(message = "O status é obrigatório.") //
        StatusTransacao status,

        @Schema(description = "Observações adicionais.", example = "Referente ao consumo de Janeiro.") //
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.") //
        String observacao,

        @Schema(description = "ID da categoria vinculada.", example = "c8f2a1b3-4d5e-6f7g-8h9i-0j1k2l3m4n5o") //
        @NotNull(message = "A categoria é obrigatória.") //
        UUID categoriaId,

        @Schema(description = "ID da conta vinculada.", example = "b2c3d4e5-f6a7-8901-2345-67890abcdef1") //
        @NotNull(message = "A conta é obrigatória.") //
        UUID contaId,

        @Schema(description = "ID da pessoa vinculada (Opcional).", example = "a1b2c3d4-e5f6-7890-1234-56789abcdef0") //
        UUID pessoaId) {
}
