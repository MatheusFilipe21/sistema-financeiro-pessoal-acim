package br.com.sfpacim.backend.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade JPA que representa uma pessoa no sistema.
 *
 * <p>
 * Uma pessoa é uma entidade de negócio utilizada para vincular contas bancárias
 * e cartões.
 * Pertence obrigatoriamente a um {@link Usuario} e não pode haver nomes
 * duplicados
 * para o mesmo usuário.
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
@Table(name = "pessoas", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "nome", "usuario_id" })
})
public class Pessoa {

    /**
     * Identificador único da pessoa.
     * Gerado automaticamente (UUID).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Nome da pessoa.
     * Obrigatório e deve ser único para o usuário.
     */
    @Column(nullable = false, length = 100)
    private String nome;

    /**
     * Usuário deste registro.
     * Define quem cadastrou e quem pode visualizar esta pessoa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    /**
     * Construtor para a criação de uma nova pessoa.
     *
     * @param nome    Nome da pessoa.
     * @param usuario Usuário do registro.
     */
    public Pessoa(String nome, Usuario usuario) {
        this.nome = nome;
        this.usuario = usuario;
    }
}
