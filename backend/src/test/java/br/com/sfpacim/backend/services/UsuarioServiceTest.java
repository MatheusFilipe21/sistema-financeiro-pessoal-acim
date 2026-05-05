package br.com.sfpacim.backend.services;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.sfpacim.backend.dtos.usuario.DadosCadastroUsuarioDTO;
import br.com.sfpacim.backend.dtos.usuario.UsuarioDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import br.com.sfpacim.backend.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe {@link UsuarioService}.
 * 
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas
 * (como o UsuarioRepository e PasswordEncoder).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UsuarioService usuarioService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA = "Ab123456";
    private static final String SENHA_HASH = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    /**
     * Configura o cenário comum e padroniza as respostas do MessageSource.
     */
    @BeforeEach
    void setUp() {
        Answer<String> answerMensagemDinamica = invocation -> {
            String codigo = invocation.getArgument(0);
            if ("erro.usuario.email.duplicado".equals(codigo)) {
                Object[] args = invocation.getArgument(1);
                String emailArg = (args != null && args.length > 0) ? args[0].toString() : "";
                return String.format("O e-mail: %s já está cadastrado.", emailArg);
            }
            return "Mensagem Mockada";
        };

        Mockito.lenient()
                .when(messageSource.getMessage(anyString(), any(Object[].class), any()))
                .thenAnswer(answerMensagemDinamica);
    }

    /**
     * Testa o método {@link UsuarioService#registrar(DadosCadastroUsuarioDTO)}.
     * Valida o cenário de sucesso.
     * 
     * <p>
     * Verifica se o serviço chama o PasswordEncoder, persiste o usuário e
     * cria automaticamente a pessoa titular vinculada.
     */
    @Test
    @DisplayName("registrar: Quando dados válidos, deve hashear a senha, salvar usuário e criar pessoa titular")
    void testeRegistrar_QuandoDadosValidos_DeveSalvarUsuarioEPessoa() {
        DadosCadastroUsuarioDTO dadosCadastro = new DadosCadastroUsuarioDTO(NOME, EMAIL, SENHA);
        Usuario usuarioSalvo = new Usuario(NOME, EMAIL, SENHA_HASH);
        usuarioSalvo.setId(UUID.randomUUID());

        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_HASH);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        UsuarioDTO resultadoDTO = usuarioService.registrar(dadosCadastro);

        assertNotNull(resultadoDTO);
        assertEquals(NOME, resultadoDTO.nome());
        assertEquals(EMAIL, resultadoDTO.email());

        verify(usuarioRepository).existsByEmail(EMAIL);
        verify(passwordEncoder, times(1)).encode(SENHA);
        verify(usuarioRepository).save(any(Usuario.class));
        verify(pessoaRepository).save(argThat(pessoa -> pessoa.getNome().equals(NOME) && pessoa.isTitular()));
    }

    /**
     * Testa o método {@link UsuarioService#registrar(DadosCadastroUsuarioDTO)}.
     * Valida o cenário de falha por e-mail duplicado (Verificação Proativa).
     * 
     * <p>
     * Aplica a correção java:S5778 isolando a chamada que lança exceção.
     */
    @Test
    @DisplayName("registrar: Quando e-mail já existe, deve lançar ViolacaoDadosException")
    void testeRegistrar_QuandoEmailDuplicado_DeveLancarExcecao() {
        DadosCadastroUsuarioDTO dadosCadastro = new DadosCadastroUsuarioDTO(NOME, EMAIL, SENHA);
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(true);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> usuarioService.registrar(dadosCadastro));

        assertEquals(String.format("O e-mail: %s já está cadastrado.", EMAIL), excecao.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(pessoaRepository, never()).save(any());
    }

    /**
     * Testa o método {@link UsuarioService#atualizarSenha(Usuario, String)}.
     * Valida o fluxo de alteração de senha.
     */
    @Test
    @DisplayName("atualizarSenha: Deve gerar novo hash e salvar as alterações")
    void testeAtualizarSenha_DeveCodificarESalvar() {
        String novaSenha = "NovaSenha123";
        String novoHash = "$2a$10$HASH_NOVO";
        Usuario usuarioMock = new Usuario(NOME, EMAIL, SENHA_HASH);
        usuarioMock.setId(UUID.randomUUID());

        when(passwordEncoder.encode(novaSenha)).thenReturn(novoHash);

        usuarioService.atualizarSenha(usuarioMock, novaSenha);

        assertEquals(novoHash, usuarioMock.getSenha());
        verify(passwordEncoder).encode(novaSenha);
        verify(usuarioRepository).save(usuarioMock);
    }
}
