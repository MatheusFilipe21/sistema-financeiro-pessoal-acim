package br.com.sfpacim.backend.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.repositories.UsuarioRepository;

/**
 * Serviço (Boilerplate do Spring Security) responsável por carregar
 * os dados de um usuário (UserDetails) a partir do banco de dados
 * durante o processo de autenticação.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param usuarioRepository O repositório para acesso aos dados do usuário.
     */
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Localiza um usuário com base no seu e-mail (que é o 'username').
     * Este método é chamado automaticamente pelo AuthenticationManager do Spring.
     *
     * @param email O e-mail (username) fornecido na tentativa de login.
     * @return Um objeto UserDetails ({@link Usuario})
     * @throws UsernameNotFoundException Se o e-mail não for encontrado no banco.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }
}
