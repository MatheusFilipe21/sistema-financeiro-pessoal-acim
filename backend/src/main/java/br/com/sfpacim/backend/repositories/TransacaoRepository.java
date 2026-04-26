package br.com.sfpacim.backend.repositories;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Busca uma transação garantindo primeiro a titularidade do usuário e depois
     * o ID do lançamento.
     *
     * @param usuarioId O ID do usuário dono da transação.
     * @param id        O ID da transação.
     * @return Um {@link Optional} contendo a transação se encontrada.
     */
    Optional<Transacao> findByUsuarioIdAndId(UUID usuarioId, UUID id);

    /**
     * Busca transações de um usuário dentro de um intervalo de vencimento, com
     * paginação.
     *
     * <p>
     * Este é o método principal para alimentar as listas de "Contas a
     * Pagar/Receber" no dashboard.
     *
     * @param usuarioId O ID do usuário dono das transações.
     * @param inicio    Data de início do vencimento (inclusive).
     * @param fim       Data final do vencimento (inclusive).
     * @param pageable  Objeto contendo página, tamanho e ordenação.
     * @return Uma página de transações filtradas.
     */
    Page<Transacao> findByUsuarioIdAndDataVencimentoBetween(UUID usuarioId, LocalDate inicio, LocalDate fim,
            Pageable pageable);

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
}
