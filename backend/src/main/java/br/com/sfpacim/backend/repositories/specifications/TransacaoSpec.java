package br.com.sfpacim.backend.repositories.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import br.com.sfpacim.backend.dtos.transacao.FiltroTransacaoDTO;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.utils.MetodosUteis;

/**
 * Encapsula as regras de filtragem dinâmica para a entidade Transação.
 * 
 * @author Matheus F. N. Pereira
 */
public class TransacaoSpec {

    /**
     * Construtor privado para evitar instanciação da classe.
     */
    private TransacaoSpec() {

    }

    /**
     * Constrói a especificação (Specification) combinando todos os filtros
     * dinâmicos fornecidos pelo usuário.
     *
     * <p>
     * Este método garante nativamente o Defense in Depth, injetando a restrição
     * de {@code usuarioId} na raiz de qualquer consulta gerada.
     *
     * @param usuarioId O ID do usuário logado (obrigatório para isolamento).
     * @param filtro    O DTO contendo os parâmetros de busca opcionais.
     * @return Uma {@link Specification} configurada para a entidade
     *         {@link Transacao}.
     */
    public static Specification<Transacao> comFiltros(UUID usuarioId, FiltroTransacaoDTO filtro) {
        return (root, query, cb) -> {
            if (Transacao.class.equals(query.getResultType())) {
                root.fetch("categoria", JoinType.INNER);
                root.fetch("conta", JoinType.INNER);
                root.fetch("pessoa", JoinType.LEFT);
            }

            Predicate doUsuario = cb.equal(root.get("usuario").get("id"), usuarioId);

            if (filtro == null) {
                return doUsuario;
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(doUsuario);

            MetodosUteis.adicionarFiltroTextual(predicates, cb, root.get("descricao"), filtro.descricao());

            MetodosUteis.adicionarFiltroMaiorOuIgual(predicates, cb, root.get("valor"), filtro.valorMin());
            MetodosUteis.adicionarFiltroMenorOuIgual(predicates, cb, root.get("valor"), filtro.valorMax());

            MetodosUteis.adicionarFiltroMaiorOuIgual(predicates, cb, root.get("dataVencimento"),
                    filtro.dataVencimentoInicio());
            MetodosUteis.adicionarFiltroMenorOuIgual(predicates, cb, root.get("dataVencimento"),
                    filtro.dataVencimentoFim());

            MetodosUteis.adicionarFiltroIn(predicates, root.get("tipo"), filtro.tipos());
            MetodosUteis.adicionarFiltroIn(predicates, root.get("status"), filtro.statusList());
            MetodosUteis.adicionarFiltroIn(predicates, root.get("categoria").get("id"), filtro.categorias());
            MetodosUteis.adicionarFiltroIn(predicates, root.get("conta").get("id"), filtro.contas());
            MetodosUteis.adicionarFiltroIn(predicates, root.get("pessoa").get("id"), filtro.pessoas());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
