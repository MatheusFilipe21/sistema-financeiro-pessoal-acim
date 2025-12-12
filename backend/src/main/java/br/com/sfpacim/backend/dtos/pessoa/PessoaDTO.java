package br.com.sfpacim.backend.dtos.pessoa;

import java.util.UUID;

import br.com.sfpacim.backend.models.Pessoa;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados públicos de uma
 * {@link Pessoa}.
 *
 * <p>
 * Este record é usado como a resposta JSON padrão para endpoints
 * de listagem e detalhamento de pessoas.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id   O identificador único (UUID) da pessoa.
 * @param nome O nome da pessoa.
 */
@Schema(description = "DTO para representar os dados de uma pessoa cadastrada.")
public record PessoaDTO(

        @Schema(description = "Identificador único da pessoa.", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479") //
        UUID id,

        @Schema(description = "Nome da pessoa.", example = "Matheus Filipe do Nascimento Pereira") //
        String nome) {

    /**
     * Construtor customizado para mapear/converter a entidade {@link Pessoa}
     * (vinda do banco) para este DTO (que será enviado como JSON).
     *
     * @param pessoa A entidade JPA Pessoa a ser convertida.
     */
    public PessoaDTO(Pessoa pessoa) {
        this(
                pessoa.getId(),
                pessoa.getNome());
    }
}
