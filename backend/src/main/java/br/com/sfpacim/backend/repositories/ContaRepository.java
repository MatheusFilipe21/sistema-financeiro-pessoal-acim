package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.sfpacim.backend.dtos.conta.SelecaoContaDTO;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;

/**
 * Repositório para a entidade {@link Conta}.
 *
 * <p>
 * Define os métodos de acesso ao banco de dados para as contas bancárias.
 *
 * @author Matheus F. N. Pereira
 */
public interface ContaRepository extends JpaRepository<Conta, UUID>, JpaSpecificationExecutor<Conta> {

    /**
     * Verifica se existe alguma conta vinculada a um ID de pessoa específico,
     * garantindo também a validação de propriedade do usuário.
     *
     * @param usuarioId O ID do usuário dono da conta.
     * @param pessoaId  O ID da pessoa a ser verificada.
     * @return {@code true} se existir pelo menos uma conta; {@code false} caso
     *         contrário.
     */
    boolean existsByPessoaUsuarioIdAndPessoaId(UUID usuarioId, UUID pessoaId);

    /**
     * Busca uma conta garantindo primeiro a titularidade do usuário e depois o ID
     * da conta.
     *
     * @param usuarioId O ID do usuário dono da conta.
     * @param id        O ID da conta.
     * @return Um {@link Optional} contendo a conta se encontrada e pertencer ao
     *         usuário.
     */
    Optional<Conta> findByPessoaUsuarioIdAndId(UUID usuarioId, UUID id);

    /**
     * Valida a existência de uma conta duplicada (mesmo nome e instituição) para a
     * mesma pessoa, ignorando acentos e caixa.
     *
     * <p>
     * A regra de unicidade de contas é composta pelo vínculo com a pessoa, a
     * instituição financeira e o nome da conta normalizado via {@code unaccent}.
     * 
     * @param usuarioId   O ID do usuário dono da conta.
     * @param pessoaId    O ID da pessoa titular da conta.
     * @param instituicao A instituição financeira da conta.
     * @param nome        O nome da conta a ser verificado.
     * @param id          O ID da conta atual para desconsiderá-la em caso de
     *                    atualização.
     *                    Pode ser nulo para novos cadastros.
     * @return {@code true} se já existir uma conta equivalente para a pessoa;
     *         {@code false} caso contrário.
     */
    @Query("""
                SELECT COUNT(c) > 0
                FROM Conta c
                WHERE c.pessoa.usuario.id = :usuarioId
                AND c.pessoa.id = :pessoaId
                AND c.instituicao = :instituicao
                AND (:id IS NULL OR c.id <> :id)
                AND unaccent(LOWER(c.nome)) = unaccent(LOWER(:nome))
            """)
    boolean existeContaDuplicada(@Param("usuarioId") UUID usuarioId, @Param("pessoaId") UUID pessoaId,
            @Param("instituicao") InstituicaoFinanceira instituicao, @Param("nome") String nome, @Param("id") UUID id);

    /**
     * Busca a lista de contas formatada para componentes de seleção.
     *
     * <p>
     * Utiliza projeção direta via JPQL para instanciar o DTO sem sobrecarregar o
     * contexto de persistência do Hibernate. O alias 'pessoaNome' garante o
     * mapeamento correto para o Record.
     *
     * @param usuarioId O identificador do usuário autenticado.
     * @return Uma lista de {@link SelecaoContaDTO} pronta para Dropdowns.
     */
    @Query("""
            SELECT c.id AS id,
                   c.nome AS nome,
                   c.instituicao AS instituicao,
                   c.pessoa.nome AS pessoaNome
            FROM Conta c
            WHERE c.pessoa.usuario.id = :usuarioId
            ORDER BY c.nome ASC
            """)
    List<SelecaoContaDTO> buscarOpcoesParaSelecao(@Param("usuarioId") UUID usuarioId);
}
