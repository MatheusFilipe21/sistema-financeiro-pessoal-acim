package br.com.sfpacim.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Classe de configuração principal do Spring Security.
 *
 * <p>
 * Define a política de autenticação, gerenciamento de sessão e
 * expõe os Beans necessários para a segurança da aplicação.
 *
 * @author Matheus F. N. Pereira
 */
@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    private final JWTFilter jwtFilter;

    /**
     * Construtor para Injeção de Dependências.
     * O Spring injeta automaticamente o JWTFilter quando esta classe de
     * configuração é criada.
     *
     * @param jwtFilter O filtro customizado para validação de tokens JWT.
     */
    public SegurancaConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Expõe o BCryptPasswordEncoder como um Bean gerenciado pelo Spring.
     * 
     * <p>
     * Isso permite que ele seja injetado em outros serviços (como o UsuarioService)
     * para fazer o hash de senhas (RF06).
     *
     * @return Uma instância do PasswordEncoder (BCrypt).
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expõe o AuthenticationManager (gerenciador de autenticação) do Spring
     * como um Bean.
     *
     * <p>
     * O AutenticacaoController irá injetar este Bean para processar
     * as tentativas de login (RF10).
     *
     * @param configuration A configuração de autenticação do Spring.
     * @return O AuthenticationManager configurado.
     * @throws Exception Exceção qualquer.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configura a cadeia de filtros de segurança (Security Filter Chain) do Spring.
     * 
     * <p>
     * Define quais endpoints são públicos (ex: /autenticacao/**, /swagger-ui/**) e
     * quais são privados (exigindo autenticação).
     *
     * @param http O construtor de segurança do Http.
     * @return O SecurityFilterChain construído.
     * @throws Exception Exceção qualquer.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita a proteção CSRF (Cross-Site Request Forgery),
                // pois a autenticação será stateless (via token JWT).
                .csrf(csrf -> csrf.disable())

                // Define a política de gerenciamento de sessão como STATELESS (sem estado).
                // A API não criará ou manterá sessões de usuário (padrão RESTful).
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configura as regras de autorização para os endpoints HTTP
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso público (não autenticado) ao endpoint de cadastro (RF07).
                        .requestMatchers("/autenticacao/cadastro").permitAll()

                        // Permite acesso público (não autenticado) ao endpoint de login (RF08).
                        .requestMatchers("/autenticacao/login").permitAll()

                        // Permite acesso público (não autenticado) à solicitação de recuperação de
                        // senha (RF14).
                        .requestMatchers("/autenticacao/esqueci-senha").permitAll()

                        // Permite acesso público (não autenticado) à documentação do Swagger.
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()

                        // Exige autenticação para todas as outras requisições.
                        .anyRequest().authenticated())

                // Adiciona o filtro JWT (JWTFilter) para rodar antes do filtro de autenticação
                // padrão do Spring.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
