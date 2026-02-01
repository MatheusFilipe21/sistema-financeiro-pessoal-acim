package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;

/**
 * Repositório para a entidade {@link Categoria}.
 *
 * <p>
 * Define os métodos de acesso ao banco de dados para as categorias de
 * transação.
 * Suporta a busca híbrida (Categorias do Usuário + Categorias do Sistema).
 *
 * @author Matheus F. N. Pereira
 */
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    /**
     * Busca todas as categorias visíveis para o usuário.
     *
     * <p>
     * Retorna tanto as categorias criadas pelo próprio usuário quanto as
     * categorias globais do sistema (onde usuário é nulo), ordenadas por nome.
     *
     * @param usuario O usuário autenticado.
     * @return Lista de categorias (Pessoais + Sistema).
     */
    List<Categoria> findByUsuarioOrUsuarioIsNullOrderByNomeAsc(Usuario usuario);

    /**
     * Verifica duplicidade de nome na criação.
     *
     * <p>
     * Valida se já existe uma categoria com o mesmo nome (ignorando case)
     * vinculada ao usuário OU ao sistema.
     *
     * @param nome    O nome a ser verificado.
     * @param usuario O usuário dono do escopo.
     * @return {@code true} se houver conflito.
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Categoria c
            WHERE (c.usuario = :usuario OR c.usuario IS NULL)
            AND LOWER(c.nome) = LOWER(:nome)
            """)
    boolean existsByNomeAndUsuarioConflitoCadastro(@Param("nome") String nome, @Param("usuario") Usuario usuario);

    /**
     * Verifica duplicidade de nome na atualização.
     *
     * <p>
     * Similar à validação de cadastro, mas ignora o registro que está sendo
     * editado (pelo ID) para evitar falso positivo.
     *
     * @param nome    O nome a ser verificado.
     * @param usuario O usuário dono do escopo.
     * @param id      O ID da categoria em edição.
     * @return {@code true} se houver conflito.
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Categoria c
            WHERE (c.usuario = :usuario OR c.usuario IS NULL)
            AND LOWER(c.nome) = LOWER(:nome)
            AND c.id != :id
            """)
    boolean existsByNomeAndUsuarioConflito(@Param("nome") String nome, @Param("usuario") Usuario usuario,
            @Param("id") UUID id);
}
