package br.com.sfpacim.backend.repositories.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import br.com.sfpacim.backend.dtos.categoria.FiltroCategoriaDTO;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.utils.MetodosUteis;

import jakarta.persistence.criteria.Predicate;

/**
 * Encapsula as regras de filtragem dinâmica para a entidade Categoria.
 * 
 * @author Matheus F. N. Pereira
 */
public class CategoriaSpec {

    /**
     * Construtor privado para evitar instanciação da classe.
     */
    private CategoriaSpec() {

    }

    /**
     * Constrói predicados dinâmicos baseados nos parâmetros fornecidos via DTO.
     *
     * @param usuarioId ID do usuário proprietário (Obrigatório para segurança).
     * @param filtro    Objeto {@link FiltroCategoriaDTO} contendo os filtros
     *                  opcionais.
     * @return Specification configurada e pronta para o Criteria API.
     */
    public static Specification<Categoria> comFiltros(UUID usuarioId, FiltroCategoriaDTO filtro) {
        return (root, query, cb) -> {
            Predicate doUsuario = cb.equal(root.get("usuario").get("id"), usuarioId);
            Predicate doSistema = cb.isNull(root.get("usuario"));

            if (filtro == null) {
                return cb.or(doUsuario, doSistema);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (Boolean.TRUE.equals(filtro.sistema())) {
                predicates.add(doSistema);
            } else if (Boolean.FALSE.equals(filtro.sistema())) {
                predicates.add(doUsuario);
            } else {
                predicates.add(cb.or(doUsuario, doSistema));
            }

            MetodosUteis.adicionarFiltroTextual(predicates, cb, root.get("nome"), filtro.nome());

            if (filtro.tipo() != null) {
                Predicate tipoExato = cb.equal(root.get("tipo"), filtro.tipo());
                Predicate tipoAmbos = cb.equal(root.get("tipo"), TipoCategoria.AMBOS);
                predicates.add(cb.or(tipoExato, tipoAmbos));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
