package br.com.sfpacim.backend.models;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade JPA que representa o usuário no banco de dados.
 * 
 * <p>
 * Esta classe também implementa UserDetails para integração com o Spring
 * Security.
 * 
 * @author Matheus F. N. Pereira
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    /**
     * Identificador único do usuário.
     * Gerado automaticamente utilizando o tipo UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Nome do usuário.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * E-mail único usado para login e comunicação (Chave de Negócio).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hash da senha do usuário (armazenado usando BCrypt).
     */
    @Column(nullable = false)
    private String senha;

    /**
     * Construtor para a criação de um novo usuário.
     *
     * @param nome          Nome do usuário.
     * @param email         E-mail único do usuário (usado para login).
     * @param senhaHasheada A senha já processada por um
     *                      PasswordEncoder (BCrypt).
     */
    public Usuario(String nome, String email, String senhaHasheada) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaHasheada;
    }

    /**
     * Retorna as "autoridades" (perfis ou papéis) concedidas ao usuário.
     * Por padrão, o perfil (role) é "ROLE_USUARIO".
     *
     * @return A coleção de perfis do usuário.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USUARIO"));
    }

    /**
     * Retorna a senha armazenada (hash) do usuário.
     * Utilizado pelo Spring Security para o processo de autenticação.
     *
     * @return A senha hasheada do usuário.
     */
    @Override
    public String getPassword() {
        return this.senha;
    }

    /**
     * Retorna o identificador único usado para autenticação.
     * Neste contexto, o "username" é o e-mail do usuário.
     *
     * @return O e-mail (username) do usuário.
     */
    @Override
    public String getUsername() {
        return this.email;
    }
}
