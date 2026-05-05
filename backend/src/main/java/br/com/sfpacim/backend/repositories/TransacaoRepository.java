package br.com.sfpacim.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.sfpacim.backend.models.Transacao;

/**
 * Repositório para a entidade {@link Transacao}.
 *
 * <p>
 * Responsável pelo acesso aos dados de movimentações financeiras.
 * Implementa estratégias de paginação e filtragem por período de competência ou
 * vencimento, garantindo o isolamento de dados por usuário (Defense in Depth).
 *
 * @author Matheus F. N. Pereira
 */
public interface TransacaoRepository extends JpaRepository<Transacao, UUID>, JpaSpecificationExecutor<Transacao> {

    /**
     * Verifica se existem transações associadas a uma pessoa e um usuário
     * específico.
     * 
     * @param usuarioId O ID do usuário dono do registro.
     * @param pessoaId  O ID da pessoa vinculada.
     * @return {@code true} se existirem transações, {@code false} caso contrário.
     */
    boolean existsByUsuarioIdAndPessoaId(UUID usuarioId, UUID pessoaId);

    /**
     * Verifica se existem transações associadas a uma conta e um usuário
     * específico.
     * 
     * @param usuarioId O ID do usuário dono do registro.
     * @param contaId   O ID da conta vinculada.
     * @return {@code true} se existirem transações, {@code false} caso contrário.
     */
    boolean existsByUsuarioIdAndContaId(UUID usuarioId, UUID contaId);

    /**
     * Verifica se existem transações associadas a uma categoria personalizada.
     *
     * @param usuarioId   O ID do usuário dono do registro.
     * @param categoriaId O ID da categoria vinculada.
     * @return {@code true} se existirem transações, {@code false} caso contrário.
     */
    boolean existsByUsuarioIdAndCategoriaId(UUID usuarioId, UUID categoriaId);

    /**
     * Busca uma transação garantindo primeiro a titularidade do usuário e depois
     * o ID do lançamento.
     *
     * <p>
     * Utiliza {@link EntityGraph} para resolver o problema de consultas N+1,
     * forçando o Hibernate a trazer os dados das entidades em um único JOIN no
     * banco de dados, otimizando a listagem geral.
     *
     * @param usuarioId O ID do usuário dono da transação.
     * @param id        O ID da transação.
     * @return Um {@link Optional} contendo a transação com suas dependências
     *         pré-carregadas.
     */
    @EntityGraph(attributePaths = { "categoria", "conta", "pessoa" })
    Optional<Transacao> findByUsuarioIdAndId(UUID usuarioId, UUID id);
}
