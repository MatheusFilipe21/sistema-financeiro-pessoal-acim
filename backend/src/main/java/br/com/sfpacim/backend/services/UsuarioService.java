package br.com.sfpacim.backend.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.usuario.DadosCadastroUsuarioDTO;
import br.com.sfpacim.backend.dtos.usuario.UsuarioDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import br.com.sfpacim.backend.repositories.UsuarioRepository;

/**
 * Serviço responsável pela lógica de negócio relacionada ao {@link Usuario}.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Construtor para Injeção de Dependências.
     *
     * <p>
     * O Spring injeta automaticamente as instâncias necessárias quando esta
     * classe é criada.
     *
     * @param usuarioRepository O repositório para acesso aos dados do usuário.
     * @param pessoaRepository  O repositório para persistência da pessoa titular
     *                          vinculada.
     * @param passwordEncoder   O bean para codificação de senhas (BCrypt).
     */
    public UsuarioService(UsuarioRepository usuarioRepository, PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Processa o registro de um novo usuário no sistema.
     *
     * <p>
     * Este método orquestra a conversão do DTO, a persistência e a criação
     * automática da pessoa titular vinculada a este usuário.
     *
     * @param dados Os dados de cadastro (DTO) já validados pelo controller.
     * @return O {@link UsuarioDTO} contendo os dados públicos do usuário
     *         recém-criado.
     * @throws ViolacaoDadosException Se o e-mail já existir no banco.
     */
    @Transactional
    public UsuarioDTO registrar(DadosCadastroUsuarioDTO dados) throws ViolacaoDadosException {
        Usuario usuario = this.salvarEntidade(paraEntidade(dados));

        criarPessoaTitular(usuario);

        return paraDTO(usuario);
    }

    /**
     * Atualiza a senha do usuário.
     *
     * <p>
     * Responsável por gerar o hash da nova senha e persistir a alteração.
     *
     * @param usuario   A entidade do usuário já carregada do banco.
     * @param novaSenha A nova senha vinda do DTO.
     */
    public void atualizarSenha(Usuario usuario, String novaSenha) {
        usuario.setSenha(passwordEncoder.encode(novaSenha));

        this.salvarEntidade(usuario);
    }

    /**
     * Converte a entidade {@link Usuario} (persistida) para um
     * {@link UsuarioDTO} (objeto de resposta).
     * <p>
     * Este método garante que dados sensíveis (como a senha)
     * não sejam expostos na API.
     *
     * @param usuario A entidade {@link Usuario} vinda do banco.
     * @return O {@link UsuarioDTO} correspondente.
     */
    private UsuarioDTO paraDTO(Usuario usuario) {
        return new UsuarioDTO(usuario);
    }

    /**
     * Converte um {@link DadosCadastroUsuarioDTO} (DTO) para a entidade
     * {@link Usuario} aplicando o hash na senha durante a conversão.
     *
     * @param dto {@link DadosCadastroUsuarioDTO} com os dados do novo usuário.
     * @return A entidade {@link Usuario} pronta para ser persistida.
     */
    private Usuario paraEntidade(DadosCadastroUsuarioDTO dto) {
        return new Usuario(dto.nome(), dto.email(), passwordEncoder.encode(dto.senha()));
    }

    /**
     * Tenta salvar uma entidade {@link Usuario} no repositório.
     * 
     * <p>
     * Este método encapsula o save() e trata a exceção de violação
     * de integridade (e-mail duplicado), lançando uma
     * exceção de negócio mais clara (ViolacaoDadosException).
     *
     * @param usuario Entidade {@link Usuario} a ser salva.
     * @return O usuário persistido.
     * @throws ViolacaoDadosException Caso o e-mail (unique=true) já esteja
     *                                cadastrado.
     */
    private Usuario salvarEntidade(Usuario usuario) throws ViolacaoDadosException {
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException _) {
            throw new ViolacaoDadosException(
                    String.format("O e-mail: %s já está cadastrado.", usuario.getEmail()));
        }
    }

    /**
     * Cria e salva a Pessoa vinculada ao usuário recém-criado.
     * 
     * <p>
     * Esta pessoa será marcada como "Titular", permitindo que contas e cartões
     * sejam criados para ela imediatamente.
     *
     * @param usuario O usuário recém-cadastrado que será dono do registro.
     */
    private void criarPessoaTitular(Usuario usuario) {
        Pessoa titular = new Pessoa(usuario.getNome(), usuario);
        titular.setTitular(true);

        pessoaRepository.saveAndFlush(titular);
    }
}
