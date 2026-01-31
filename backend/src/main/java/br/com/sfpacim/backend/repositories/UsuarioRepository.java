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
     * Busca um usuário pelo seu e-mail (Chave de Negócio).
     * 
     * <p>
     * O Spring Data JPA cria a consulta automaticamente
     * com base no nome do método (Query Methods).
     *
     * @param email O e-mail a ser buscado.
     * @return Um Optional contendo o Usuario, se encontrado.
     */
    Optional<Usuario> findByEmail(String email);
}