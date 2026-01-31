package br.com.sfpacim.backend.dtos.pessoa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para recebimento de dados de criação e
 * atualização de pessoas.
 *
 * <p>
 * Este record é usado exclusivamente como corpo da requisição (@RequestBody)
 * nos endpoints POST e PUT. Contém as validações necessárias para garantir
 * a integridade dos dados antes de chegarem à camada de serviço.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome O nome da pessoa (Obrigatório).
 */
@Schema(description = "DTO utilizado para cadastrar ou atualizar uma pessoa.")
public record CriarAtualizarPessoaDTO(

        @Schema(description = "Nome da pessoa.", example = "Matheus Filipe do Nascimento Pereira") //
        @NotBlank(message = "O nome é obrigatório.") //
        String nome,

        @Schema(description = "Define se a pessoa será titular de contas/cartões (Padrão: false).", example = "true") //
        Boolean titular) {
}
