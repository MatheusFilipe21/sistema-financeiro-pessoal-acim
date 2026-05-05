package br.com.sfpacim.backend.services;

import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;
import br.com.sfpacim.backend.utils.MetodosUteis;

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

    private final MessageSource messageSource;
    private final UsuarioRepository usuarioRepository;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * @param messageSource     A instância do MessageSource.
     * @param usuarioRepository O repositório para buscar os dados do usuário.
     */
    public ContextoUsuarioService(MessageSource messageSource, UsuarioRepository usuarioRepository) {
        this.messageSource = messageSource;
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
     *                                   banco ou o contexto de segurança esteja
     *                                   vazio.
     */
    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException(
                    MetodosUteis.obterMensagem(messageSource, "erro.seguranca.contexto.vazio"));
        }

        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        MetodosUteis.obterMensagem(messageSource, "erro.seguranca.usuario.nao-encontrado")));
    }
}
