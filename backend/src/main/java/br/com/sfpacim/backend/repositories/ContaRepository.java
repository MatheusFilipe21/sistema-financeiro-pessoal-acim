package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;

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
     * Busca todas as contas vinculadas a uma pessoa específica.
     *
     * @param pessoa A pessoa titular das contas.
     * @return Lista de contas daquela pessoa.
     */
    List<Conta> findByPessoa(Pessoa pessoa);
}
