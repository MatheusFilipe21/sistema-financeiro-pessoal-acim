package br.com.sfpacim.backend.repositories.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import br.com.sfpacim.backend.dtos.conta.FiltroContaDTO;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.utils.MetodosUteis;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

/**
 * Encapsula as regras de filtragem dinâmica para a entidade Conta.
 * 
 * @author Matheus F. N. Pereira
 */
public class ContaSpec {

    /**
     * Constante para o campo "pessoa" na entidade Conta.
     */
    private static final String PESSOA = "pessoa";

    /**
     * Construtor privado para evitar instanciação da classe.
     */
    private ContaSpec() {

    }

    /**
     * Constrói predicados dinâmicos baseados nos parâmetros fornecidos via DTO.
     *
     * @param usuarioId ID do usuário proprietário (Obrigatório para segurança).
     * @param filtro    Objeto {@link FiltroContaDTO} contendo os filtros opcionais.
     * @return Specification configurada e pronta para o Criteria API.
     */
    public static Specification<Conta> comFiltros(UUID usuarioId, FiltroContaDTO filtro) {
        return (root, query, cb) -> {
            if (Conta.class.equals(query.getResultType())) {
                root.fetch(PESSOA, JoinType.INNER);
            }

            Predicate doUsuario = cb.equal(root.get(PESSOA).get("usuario").get("id"), usuarioId);

            if (filtro == null) {
                return doUsuario;
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(doUsuario);

            MetodosUteis.adicionarFiltroTextual(predicates, cb, root.get("nome"), filtro.nome());

            if (!CollectionUtils.isEmpty(filtro.instituicoes())) {
                predicates.add(root.get("instituicao").in(filtro.instituicoes()));
            }

            if (!CollectionUtils.isEmpty(filtro.pessoaIds())) {
                predicates.add(root.get(PESSOA).get("id").in(filtro.pessoaIds()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
