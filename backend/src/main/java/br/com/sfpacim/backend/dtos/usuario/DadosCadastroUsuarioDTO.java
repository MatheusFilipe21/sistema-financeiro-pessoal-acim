package br.com.sfpacim.backend.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO (Data Transfer Object) para encapsular os dados de entrada
 * do cadastro de um usuário.
 * 
 * <p>
 * Este record aplica as validações de negócio usando o Spring Validation.
 *
 * @author Matheus F. N. Pereira
 * 
 * @param nome  O nome do usuário.
 * @param email O e-mail único do usuário (será usado para login).
 * @param senha A senha de acesso (mínimo 8 caracteres, 1 maiúscula, 1
 *              minúscula, 1 número).
 */
@Schema(description = "${usuario.descricao.schema.cadastro}")
public record DadosCadastroUsuarioDTO(

        @Schema(description = "${usuario.descricao.nome}", example = "${usuario.exemplo.nome}") //
        @NotBlank(message = "{geral.validacao.nome.obrigatorio}") //
        String nome,

        @Schema(description = "${usuario.descricao.email.cadastro}", example = "${usuario.exemplo.email}") //
        @NotBlank(message = "{usuario.validacao.email.obrigatorio}") //
        @Email(message = "{usuario.validacao.email.invalido}") //
        String email,

        @Schema(description = "${usuario.descricao.senha}", example = "${usuario.exemplo.senha}") //
        @NotBlank(message = "{usuario.validacao.senha.obrigatoria}") //
        @Pattern(regexp = "^(?=.*[\\d])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "{usuario.validacao.senha.padrao}") //
        String senha) {
}
