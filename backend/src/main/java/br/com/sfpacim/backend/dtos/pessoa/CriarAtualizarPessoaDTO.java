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
@Schema(description = "${pessoa.descricao.schema.criacao-atualizacao}")
public record CriarAtualizarPessoaDTO(

        @Schema(description = "${pessoa.descricao.nome}", example = "${pessoa.exemplo.nome}") //
        @NotBlank(message = "{geral.validacao.nome.obrigatorio}") //
        String nome,

        @Schema(description = "${pessoa.descricao.titular}", example = "${pessoa.exemplo.titular}") //
        Boolean titular) {
}
