package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sfpacim.backend.dtos.pessoa.SelecaoPessoaDTO;
import br.com.sfpacim.backend.models.Pessoa;

/**
 * Repositório para a entidade {@link Pessoa}.
 * 
 * <p>
 * Define os métodos de acesso ao banco de dados para as pessoas.
 *
 * @author Matheus F. N. Pereira
 */
public interface PessoaRepository extends JpaRepository<Pessoa, UUID>, JpaSpecificationExecutor<Pessoa> {

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
     * Valida a existência de um nome duplicado para o mesmo usuário, ignorando
     * acentos e caixa.
     *
     * <p>
     * Utiliza a função {@code unaccent} do PostgreSQL para garantir a integridade
     * semântica, impedindo que variações ortográficas (ex: "João" e "Joao") sejam
     * cadastradas para o mesmo usuário.
     * 
     * @param usuarioId O ID do usuário proprietário do registro.
     * @param nome      O nome a ser verificado.
     * @param id        O ID do registro atual para desconsiderá-lo em caso de
     *                  atualização.
     *                  Pode ser nulo para novos cadastros.
     * @return {@code true} se já existir uma pessoa com nome equivalente;
     *         {@code false} caso contrário.
     */
    @Query("""
                SELECT COUNT(p) > 0
                FROM Pessoa p
                WHERE p.usuario.id = :usuarioId
                AND (:id IS NULL OR p.id <> :id)
                AND unaccent(LOWER(p.nome)) = unaccent(LOWER(:nome))
            """)
    boolean existeNomeDuplicado(@Param("usuarioId") UUID usuarioId, @Param("nome") String nome, @Param("id") UUID id);

    /**
     * Busca a lista de pessoas do usuário formatada para componentes de seleção.
     *
     * <p>
     * Utiliza projeção direta via JPQL para instanciar o DTO sem sobrecarregar o
     * contexto de persistência do Hibernate, extraindo apenas o ID e o Nome.
     * Isso otimiza o uso de memória e rede. O filtro de titularidade é dinâmico e
     * opcional.
     *
     * @param usuarioId O identificador do usuário autenticado.
     * @param titular   {@code true} para buscar apenas titulares, {@code false}
     *                  para não-titulares, ou {@code null} para ignorar este
     *                  filtro.
     * @return Uma lista de {@link SelecaoPessoaDTO} ordenada alfabeticamente.
     */
    @Query("""
            SELECT p.id AS id,
                   p.nome AS nome
            FROM Pessoa p
            WHERE p.usuario.id = :usuarioId
            AND (:titular IS NULL OR p.titular = :titular)
            ORDER BY p.nome ASC
            """)
    List<SelecaoPessoaDTO> buscarOpcoesParaSelecao(@Param("usuarioId") UUID usuarioId,
            @Param("titular") Boolean titular);
}
