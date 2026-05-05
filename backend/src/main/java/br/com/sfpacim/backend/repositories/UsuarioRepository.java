package br.com.sfpacim.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sfpacim.backend.models.Usuario;

/**
 * Repositório para a entidade {@link Usuario}.
 * 
 * <p>
 * Define os métodos de acesso ao banco de dados para os usuários.
 *
 * @author Matheus F. N. Pereira
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Verifica se um e-mail já está cadastrado na base de dados.
     * 
     * @param email O e-mail a ser verificado.
     * @return {@code true} se o e-mail existir; {@code false} caso contrário.
     */
    boolean existsByEmail(String email);

    /**
     * Busca um usuário pelo seu e-mail (Chave de Negócio).
     *
     * @param email O e-mail a ser buscado.
     * @return Um Optional contendo o Usuario, se encontrado.
     */
    Optional<Usuario> findByEmail(String email);
}
