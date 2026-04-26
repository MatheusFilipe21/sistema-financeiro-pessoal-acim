package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sfpacim.backend.models.Conta;

/**
 * Repositório para a entidade {@link Conta}.
 *
 * <p>
 * Define os métodos de acesso ao banco de dados para as contas bancárias.
 *
 * @author Matheus F. N. Pereira
 */
public interface ContaRepository extends JpaRepository<Conta, UUID> {

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
     * Busca todas as contas vinculadas a um ID de usuário (através de suas
     * pessoas).
     *
     * <p>
     * Utiliza {@link EntityGraph} para resolver o problema de consultas N+1,
     * forçando o Hibernate a trazer os dados da Pessoa em um único JOIN no
     * banco de dados, otimizando a listagem geral.
     *
     * @param usuarioId O ID do usuário dono das contas.
     * @return Lista de contas com a entidade Pessoa já carregada na memória.
     */
    @EntityGraph(attributePaths = "pessoa")
    List<Conta> findByPessoaUsuarioId(UUID usuarioId);

    /**
     * Busca todas as contas vinculadas a uma pessoa específica, garantindo a
     * titularidade do usuário.
     *
     * @param usuarioId O ID do usuário dono das contas.
     * @param pessoaId  O ID da pessoa titular.
     * @return Lista de contas da pessoa.
     */
    List<Conta> findByPessoaUsuarioIdAndPessoaId(UUID usuarioId, UUID pessoaId);

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
}
