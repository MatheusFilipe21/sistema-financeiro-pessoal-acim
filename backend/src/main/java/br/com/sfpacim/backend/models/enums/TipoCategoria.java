package br.com.sfpacim.backend.models.enums;

import lombok.Getter;

/**
 * Enum que define a natureza financeira de uma categoria.
 *
 * <p>
 * Utilizado para filtrar categorias em lançamentos e para regras de negócio
 * relacionadas a limites de gastos.
 *
 * @author Matheus F. N. Pereira
 */
@Getter
public enum TipoCategoria {

    RECEITA("Receita"),
    DESPESA("Despesa"),
    AMBOS("Ambos");

    /**
     * Nomenclatura descritiva do tipo de categoria.
     */
    private final String descricao;

    /**
     * Construtor do enum.
     * 
     * @param descricao O nome amigável do tipo para exibição.
     */
    TipoCategoria(String descricao) {
        this.descricao = descricao;
    }
}
