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
 * @param email O e-mail único do usuário.
 */
@Schema(description = "${usuario.descricao.schema.leitura}")
public record UsuarioDTO(

        @Schema(description = "${usuario.descricao.id}", example = "${usuario.exemplo.id}") //
        UUID id,

        @Schema(description = "${usuario.descricao.nome}", example = "${usuario.exemplo.nome}") //
        String nome,

        @Schema(description = "${usuario.descricao.email.leitura}", example = "${usuario.exemplo.email}") //
        String email) {

    /**
     * Construtor customizado para mapear a entidade {@link Usuario}
     * para este DTO.
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
