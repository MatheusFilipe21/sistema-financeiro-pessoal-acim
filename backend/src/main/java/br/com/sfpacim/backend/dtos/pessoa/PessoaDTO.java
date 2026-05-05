package br.com.sfpacim.backend.dtos.pessoa;

import java.util.UUID;

import br.com.sfpacim.backend.models.Pessoa;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados públicos de uma
 * {@link Pessoa}.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id      O identificador único (UUID) da pessoa.
 * @param nome    O nome da pessoa.
 * @param titular Indica se esta pessoa é titular de contas e cartões.
 */
@Schema(description = "${pessoa.descricao.schema.leitura}")
public record PessoaDTO(

        @Schema(description = "${pessoa.descricao.id}", example = "${pessoa.exemplo.id}") //
        UUID id,

        @Schema(description = "${pessoa.descricao.nome}", example = "${pessoa.exemplo.nome}") //
        String nome,

        @Schema(description = "${pessoa.descricao.titular}", example = "${pessoa.exemplo.titular}") //
        boolean titular) {

    /**
     * Construtor customizado para mapear/converter a entidade {@link Pessoa}
     * para este DTO.
     *
     * @param pessoa A entidade JPA Pessoa a ser convertida.
     */
    public PessoaDTO(Pessoa pessoa) {
        this(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.isTitular());
    }
}
