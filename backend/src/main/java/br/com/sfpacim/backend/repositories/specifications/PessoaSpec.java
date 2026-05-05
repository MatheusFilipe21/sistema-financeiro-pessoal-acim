package br.com.sfpacim.backend.repositories.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import br.com.sfpacim.backend.dtos.pessoa.FiltroPessoaDTO;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.utils.MetodosUteis;

import jakarta.persistence.criteria.Predicate;

/**
 * Encapsula as regras de filtragem dinâmica para a entidade Pessoa.
 * 
 * @author Matheus F. N. Pereira
 */
public class PessoaSpec {

    /**
     * Construtor privado para evitar instanciação da classe.
     */
    private PessoaSpec() {

    }

    /**
     * Constrói predicados dinâmicos baseados nos parâmetros fornecidos via DTO.
     *
     * @param usuarioId ID do usuário proprietário (Obrigatório para segurança).
     * @param filtro    Objeto {@link FiltroPessoaDTO} contendo os filtros opcionais
     *                  (nome e titularidade).
     * @return Specification configurada e pronta para o Criteria API.
     */
    public static Specification<Pessoa> comFiltros(UUID usuarioId, FiltroPessoaDTO filtro) {
        return (root, query, cb) -> {
            Predicate doUsuario = cb.equal(root.get("usuario").get("id"), usuarioId);

            if (filtro == null) {
                return doUsuario;
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(doUsuario);

            MetodosUteis.adicionarFiltroTextual(predicates, cb, root.get("nome"), filtro.nome());

            if (filtro.titular() != null) {
                predicates.add(cb.equal(root.get("titular"), filtro.titular()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
