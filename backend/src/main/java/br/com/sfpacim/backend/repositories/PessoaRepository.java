package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;

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
     * Busca todas as pessoas de um usuário específico.
     *
     * @param usuario O usuário dos registros.
     * @return Lista de pessoas do usuário.
     */
    List<Pessoa> findByUsuario(Usuario usuario);
}
