package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sfpacim.backend.models.Categoria;

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
     * Busca uma categoria específica garantindo que ela pertence ao usuário
     * autenticado ou é uma categoria padrão do sistema.
     *
     * @param id        O ID da categoria.
     * @param usuarioId O ID do usuário dono da categoria.
     * @return Um {@link Optional} contendo a categoria se encontrada.
     */
    @Query("SELECT c FROM Categoria c WHERE (c.usuario.id = :usuarioId OR c.usuario IS NULL) AND c.id = :id")
    Optional<Categoria> findByUsuarioIdOrSistemaAndId(@Param("usuarioId") UUID usuarioId, @Param("id") UUID id);

    /**
     * Busca todas as categorias visíveis para o usuário.
     *
     * <p>
     * Retorna tanto as categorias criadas pelo próprio usuário quanto as
     * categorias globais do sistema (onde usuário é nulo), ordenadas por nome.
     * Utilizado para listagem e validação de duplicidade em memória.
     *
     * @param usuarioId O ID do usuário autenticado.
     * @return Lista de categorias (Pessoais + Sistema).
     */
    List<Categoria> findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(UUID usuarioId);
}
