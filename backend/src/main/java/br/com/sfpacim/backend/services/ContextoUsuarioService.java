package br.com.sfpacim.backend.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;

/**
 * Serviço utilitário para recuperação do usuário autenticado.
 *
 * <p>
 * Centraliza a lógica de acesso ao Contexto de Segurança do Spring e a
 * recuperação da entidade {@link Usuario} no banco de dados.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class ContextoUsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * @param usuarioRepository O repositório para buscar os dados do usuário.
     */
    public ContextoUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Recupera a entidade {@link Usuario} do usuário atualmente autenticado.
     *
     * <p>
     * Utiliza o e-mail extraído do token JWT (via SecurityContext) para
     * buscar os dados atualizados no banco.
     * 
     * @return A entidade {@link Usuario} logada.
     * 
     * @throws UsernameNotFoundException Caso o usuário do token não exista mais no
     *                                   banco.
     */
    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Não há usuário autenticado no contexto de segurança.");
        }

        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Usuário autenticado não encontrado na base de dados."));
    }
}
