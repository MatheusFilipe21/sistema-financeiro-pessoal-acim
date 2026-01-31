package br.com.sfpacim.backend.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO (Data Transfer Object) que representa os dados recebidos
 * no JSON da requisição de cadastro de usuário.
 * 
 * <p>
 * Este record aplica as validações de negócio (RF02, RF03, RF04)
 * usando o Spring Validation.
 *
 * @author Matheus F. N. Pereira
 */
@Schema(description = "Dados de entrada para o cadastro de um novo usuário.")
public record DadosCadastroUsuarioDTO(

        /**
         * RF04: O nome é obrigatório.
         */
        @Schema(description = "Nome do usuário.", example = "Matheus Filipe do Nascimento Pereira") //
        @NotBlank(message = "O nome é obrigatório.") //
        String nome,

        /**
         * RF02: O e-mail deve ter um formato válido.
         * RF04: O e-mail é obrigatório.
         */
        @Schema(description = "E-mail único (será usado para login).", example = "matheusfnpereira@gmail.com") //
        @NotBlank(message = "O e-mail é obrigatório.") //
        @Email(message = "O formato do e-mail é inválido.") //
        String email,

        /**
         * RF03: A senha deve ser complexa.
         * RF04: A senha é obrigatória.
         * Regra: Mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número.
         */
        @Schema(description = "Senha de acesso (mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número).", example = "Ab123456") //
        @NotBlank(message = "A senha é obrigatória.") //
        @Pattern(regexp = "^(?=.*[\\d])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "A senha deve ter no mínimo 8 caracteres, contendo ao menos uma letra maiúscula, uma minúscula e um número.") //
        String senha) {
}
