package br.com.sfpacim.backend.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.sfpacim.backend.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de segurança que intercepta todas as requisições HTTP
 * para validar o Token JWT (se ele existir).
 *
 * @author Matheus F. N. Pereira
 */
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    /**
     * Construtor para Injeção de Dependências.
     * O Spring injeta automaticamente o TokenService e o UserDetailsService.
     *
     * @param tokenService       O serviço para validar os tokens JWT.
     * @param userDetailsService O serviço do Spring Security para buscar usuários
     *                           no banco.
     */
    public JWTFilter(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Lógica principal do filtro, executada em cada requisição.
     */
    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String tokenJWT = this.recuperarToken(request);

        if (tokenJWT != null) {
            String emailUsuario = tokenService.validarToken(tokenJWT);

            UserDetails usuario = userDetailsService.loadUserByUsername(emailUsuario);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    usuario,
                    null,
                    usuario.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Helper (auxiliar) para extrair o token do cabeçalho "Authorization".
     *
     * @param request A requisição HTTP.
     * @return O token (String pura) ou null se não existir.
     */
    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
