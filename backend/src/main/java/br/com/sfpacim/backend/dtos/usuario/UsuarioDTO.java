package br.com.sfpacim.backend.dtos.usuario;

import java.util.UUID;

import br.com.sfpacim.backend.models.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados públicos de um
 * {@link Usuario}.
 * 
 * <p>
 * Este record é usado como a resposta JSON padrão para endpoints
 * que retornam informações do usuário (ex: cadastro, busca por id).
 * Ele omite dados sensíveis como a senha.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id    O identificador único (UUID) do usuário.
 * @param nome  O nome do usuário.
 * @param email O e-mail (único) do usuário.
 */
@Schema(description = "DTO para representar os dados públicos de um usuário no sistema.")
public record UsuarioDTO(

        @Schema(description = "Identificador único (UUID) do usuário.", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479") //
        UUID id,

        @Schema(description = "Nome do usuário.", example = "Matheus Filipe do Nascimento Pereira") //
        String nome,

        @Schema(description = "E-mail único do usuário (usado para login).", example = "matheus@email.com") //
        String email) {

    /**
     * Construtor customizado para mapear/converter a entidade {@link Usuario}
     * (vinda do banco) para este DTO (que será enviado como JSON).
     *
     * @param usuario A entidade JPA Usuario a ser convertida.
     */
    public UsuarioDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail());
    }
}
