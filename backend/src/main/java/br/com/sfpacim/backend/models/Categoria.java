package br.com.sfpacim.backend.models;

import java.util.UUID;

import br.com.sfpacim.backend.models.enums.TipoCategoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade JPA que representa uma categoria de transação financeira.
 *
 * <p>
 * Suporta o conceito de categorias globais (Sistema) e categorias
 * personalizadas (Usuário). Se o campo {@code usuario} for nulo,
 * a categoria é considerada do sistema e visível para todos.
 *
 * @author Matheus F. N. Pereira
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "categorias")
public class Categoria {

    /**
     * Identificador único da categoria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Nome da categoria (ex: "Alimentação", "Salário").
     * Deve ser único dentro do escopo do usuário.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Tipo da transação compatível com esta categoria.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCategoria tipo;

    /**
     * Identificador do ícone (String referente ao Material Icons).
     * Ex: "restaurant", "home".
     */
    @Column(nullable = false)
    private String icone;

    /**
     * Cor de exibição em formato hexadecimal (ex: "#FF0000").
     */
    @Column(nullable = false, length = 7)
    private String cor;

    /**
     * Usuário proprietário da categoria.
     *
     * <p>
     * Se {@code null}, indica uma Categoria Global (Padrão do Sistema).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    /**
     * Construtor para cadastro de novas categorias.
     *
     * @param nome    Nome da categoria.
     * @param tipo    Tipo da categoria.
     * @param icone   Ícone visual.
     * @param cor     Cor hexadecimal.
     * @param usuario Usuário dono (ou null para sistema).
     */
    public Categoria(String nome, TipoCategoria tipo, String icone, String cor, Usuario usuario) {
        this.nome = nome;
        this.tipo = tipo;
        this.icone = icone;
        this.cor = cor;
        this.usuario = usuario;
    }

    /**
     * Verifica se a categoria pertence ao sistema (global).
     *
     * @return {@code true} se for global, {@code false} se pertencer a um usuário.
     */
    public boolean isDoSistema() {
        return this.usuario == null;
    }
}
