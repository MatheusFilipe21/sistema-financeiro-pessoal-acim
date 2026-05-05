package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sfpacim.backend.dtos.categoria.SelecaoCategoriaDTO;
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
public interface CategoriaRepository extends JpaRepository<Categoria, UUID>, JpaSpecificationExecutor<Categoria> {

    /**
     * Valida a existência de uma categoria duplicada pelo nome, garantindo que o
     * usuário não recrie uma categoria que já existe no seu escopo pessoal ou no
     * escopo global do sistema.
     *
     * <p>
     * A restrição intencionalmente ignora o {@code TipoCategoria} (Receita,
     * Despesa, Ambos). Se uma categoria com o mesmo nome já existe, o usuário deve
     * atualizar a existente para o tipo "AMBOS" em vez de criar uma duplicata. A
     * comparação ignora acentos e caixa.
     * 
     * @param usuarioId O ID do usuário tentando criar/editar a categoria.
     * 
     * @param nome      O nome da categoria a ser verificado.
     * @param id        O ID da categoria atual para desconsiderá-la em caso de
     *                  atualização.
     *                  Pode ser nulo para novos cadastros.
     * @return {@code true} se já existir uma categoria com o mesmo nome no escopo
     *         aplicável; {@code false} caso contrário.
     */
    @Query("""
                SELECT COUNT(c) > 0
                FROM Categoria c
                WHERE (c.usuario.id = :usuarioId OR c.usuario IS NULL)
                AND (:id IS NULL OR c.id <> :id)
                AND unaccent(LOWER(c.nome)) = unaccent(LOWER(:nome))
            """)
    boolean existeCategoriaDuplicada(@Param("usuarioId") UUID usuarioId, @Param("nome") String nome,
            @Param("id") UUID id);

    /**
     * Busca uma categoria específica garantindo que ela pertence ao usuário
     * autenticado ou é uma categoria padrão do sistema.
     *
     * @param usuarioId O ID do usuário dono da categoria.
     * @param id        O ID da categoria.
     * @return Um {@link Optional} contendo a categoria se encontrada.
     */
    @Query("""
                SELECT c
                FROM Categoria c
                WHERE (c.usuario.id = :usuarioId OR c.usuario IS NULL)
                AND c.id = :id
            """)
    Optional<Categoria> buscarPorIdEUsuarioOuSistema(@Param("usuarioId") UUID usuarioId, @Param("id") UUID id);

    /**
     * Busca a lista de categorias formatada para componentes de seleção.
     *
     * <p>
     * Utiliza projeção direta via JPQL para instanciar o DTO sem sobrecarregar o
     * contexto de persistência do Hibernate, garantindo máxima performance.
     *
     * @param usuarioId O identificador do usuário autenticado.
     * @return Uma lista de {@link SelecaoCategoriaDTO} pronta para Dropdowns.
     */
    @Query("""
            SELECT c.id AS id,
                   c.nome AS nome,
                   c.tipo AS tipo,
                   c.icone AS icone,
                   c.cor AS cor
            FROM Categoria c
            WHERE (c.usuario.id = :usuarioId OR c.usuario IS NULL)
            ORDER BY c.nome ASC
            """)
    List<SelecaoCategoriaDTO> buscarOpcoesParaSelecao(@Param("usuarioId") UUID usuarioId);
}
