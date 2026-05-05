package br.com.sfpacim.backend.services;

import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;
import br.com.sfpacim.backend.utils.MetodosUteis;

/**
 * Serviço (Boilerplate do Spring Security) responsável por carregar
 * os dados de um usuário (UserDetails) a partir do banco de dados
 * durante o processo de autenticação.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MessageSource messageSource;
    private final UsuarioRepository usuarioRepository;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param messageSource     A instância do MessageSource.
     * @param usuarioRepository O repositório para acesso aos dados do usuário.
     */
    public UserDetailsServiceImpl(MessageSource messageSource, UsuarioRepository usuarioRepository) {
        this.messageSource = messageSource;
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
                .orElseThrow(() -> new UsernameNotFoundException(
                        MetodosUteis.obterMensagem(messageSource, "erro.usuario.email.nao-encontrado", email)));
    }
}
