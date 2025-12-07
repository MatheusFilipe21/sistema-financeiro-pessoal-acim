package br.com.sfpacim.backend.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.dtos.autenticacao.DadosAutenticacaoDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRecuperacaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRedefinicaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosTokenJWTDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;
import br.com.sfpacim.backend.services.interfaces.EmailService;

/**
 * Serviço responsável por orquestrar a lógica de autenticação.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class AutenticacaoService {

    /**
     * URL base do frontend (injetada via application.yml).
     */
    @Value("${app.frontend.url}")
    private String urlFrontend;

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final UsuarioService usuarioService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param authenticationManager O gerenciador de autenticação do Spring.
     * @param tokenService          O serviço para geração de tokens JWT.
     * @param usuarioRepository     Repositório para buscar usuários.
     * @param emailService          Serviço de envio de e-mails.
     * @param usuarioService        Serviço de domínio do usuário.
     */
    public AutenticacaoService(AuthenticationManager authenticationManager, TokenService tokenService,
            UsuarioRepository usuarioRepository, EmailService emailService, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.usuarioService = usuarioService;
    }

    /**
     * Orquestra a tentativa de login (RF10, RF11, RF12).
     *
     * @param dados Os dados de autenticação (email, senha).
     * @return O DTO contendo o Token JWT.
     */
    public DadosTokenJWTDTO login(DadosAutenticacaoDTO dados) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(),
                dados.senha());

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);

        Usuario usuario = (Usuario) authentication.getPrincipal();

        String tokenJWT = tokenService.gerarToken(usuario);

        return new DadosTokenJWTDTO(tokenJWT);
    }

    /**
     * Inicia o fluxo de recuperação de senha (RF14, RF16, RF17).
     * 
     * <p>
     * Verifica se o e-mail informado existe na base de dados.
     * <ul>
     * <li><b>Se existir:</b> Gera um token de recuperação seguro (assinado com a
     * senha atual),
     * monta o link de redefinição e envia por e-mail.</li>
     * <li><b>Se não existir:</b> Finaliza a execução silenciosamente (não lança
     * erro),
     * para evitar ataques aos usuários.</li>
     * </ul>
     *
     * @param dados O DTO contendo o e-mail solicitado.
     */
    public void solicitarRecuperacaoSenha(DadosRecuperacaoSenhaDTO dados) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(dados.email());

        if (usuario.isPresent()) {
            Usuario usuarioEncontrado = usuario.get();
            String token = tokenService.gerarTokenRecuperacao(usuarioEncontrado);

            String link = this.urlFrontend + "/redefinir-senha?token=" + token;

            String mensagemEmail = String.format("""
                    Olá, %s!

                    Recebemos uma solicitação para redefinir sua senha no Sistema Financeiro Pessoal ACIM.
                    Clique no link abaixo para criar uma nova senha:

                    %s

                    Este link é válido por 4 horas.
                    Se você não solicitou isso, pode ignorar este e-mail.
                    """, usuarioEncontrado.getNome(), link);

            emailService.enviar(usuarioEncontrado.getEmail(), "Recuperação de Senha - Sistema Financeiro Pessoal ACIM",
                    mensagemEmail);
        }
    }

    /**
     * Realiza a redefinição de senha (RF17).
     * 
     * @param dados O DTO com token e nova senha.
     */
    public void redefinirSenha(DadosRedefinicaoSenhaDTO dados) {
        String email = tokenService.obterEmailDoToken(dados.token());

        if (email == null) {
            throw criarErroToken();
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(this::criarErroToken);

        try {
            tokenService.validarTokenRecuperacao(dados.token(), usuario);
        } catch (Exception e) {
            throw criarErroToken();
        }

        usuarioService.atualizarSenha(usuario, dados.senha());
    }

    /**
     * Método auxiliar para instanciar a exceção de erro de token.
     *
     * <p>
     * Centraliza a criação da exceção para garantir que a mensagem de erro
     * seja idêntica em todos os cenários de falha.
     *
     * @return Uma nova instância de {@link RegraDeNegocioException}.
     */
    private RegraDeNegocioException criarErroToken() {
        return new RegraDeNegocioException("Token inválido ou expirado.");
    }
}
