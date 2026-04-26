package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sfpacim.backend.models.Pessoa;

/**
 * Repositório para a entidade {@link Pessoa}.
 * 
 * <p>
 * Define os métodos de acesso ao banco de dados para as pessoas.
 *
 * @author Matheus F. N. Pereira
 */
public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {

    /**
     * Busca uma pessoa garantindo primeiro a titularidade do usuário e depois o ID
     * da pessoa.
     * 
     * @param usuarioId O ID do usuário proprietário.
     * @param id        O ID da pessoa.
     * @return Um {@link Optional} contendo a pessoa se encontrada e pertencer ao
     *         usuário.
     */
    Optional<Pessoa> findByUsuarioIdAndId(UUID usuarioId, UUID id);

    /**
     * Busca todas as pessoas de um usuário específico utilizando apenas o ID.
     *
     * @param usuarioId O ID do usuário dono dos registros.
     * @return Lista de pessoas vinculadas ao ID informado.
     */
    List<Pessoa> findByUsuarioId(UUID usuarioId);
}
