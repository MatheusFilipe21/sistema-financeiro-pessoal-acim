package br.com.sfpacim.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.sfpacim.backend.dtos.usuario.DadosCadastroUsuarioDTO;
import br.com.sfpacim.backend.dtos.usuario.UsuarioDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import br.com.sfpacim.backend.repositories.UsuarioRepository;

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

    @InjectMocks
    private UsuarioService usuarioService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA = "Ab123456";
    private static final String SENHA_HASH = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    /**
     * Testa o método {@link UsuarioService#registrar(DadosCadastroUsuarioDTO)}.
     * Valida o cenário de sucesso.
     * 
     * <p>
     * Verifica se o serviço chama o PasswordEncoder e o Repository.save()
     * corretamente e retorna o DTO esperado.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("registrar: Quando dados válidos forem fornecidos, deve hashear a senha e salvar o usuário")
    void testeRegistrar_QuandoDadosValidos_DeveSalvarUsuario() {
        DadosCadastroUsuarioDTO dadosCadastro = new DadosCadastroUsuarioDTO(NOME, EMAIL, SENHA);

        Usuario usuarioSalvo = new Usuario(UUID.randomUUID(), NOME, EMAIL, SENHA_HASH);

        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_HASH);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        UsuarioDTO resultadoDTO = usuarioService.registrar(dadosCadastro);

        assertNotNull(resultadoDTO, "O DTO retornado não deve ser nulo");
        assertEquals(NOME, resultadoDTO.nome(), "O nome no DTO de resposta deve ser o mesmo da entrada");
        assertEquals(EMAIL, resultadoDTO.email(), "O e-mail no DTO de resposta deve ser o mesmo da entrada");

        verify(passwordEncoder, times(1)).encode(SENHA);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));

        verify(pessoaRepository).save(argThat(pessoa -> pessoa.getNome().equals(NOME) &&
                pessoa.isTitular() == true));
    }

    /**
     * Testa o método {@link UsuarioService#registrar(DadosCadastroUsuarioDTO)}.
     * Valida o cenário de falha por e-mail duplicado (RF04).
     * 
     * <p>
     * Verifica se o serviço captura a DataIntegrityViolationException (lançada
     * pelo mock do repositório) e a relança como ViolacaoDadosException.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("registrar: Quando e-mail duplicado, deve lançar ViolacaoDadosException")
    void testeRegistrar_QuandoEmailDuplicado_DeveLancarViolacaoDadosException() {
        DadosCadastroUsuarioDTO dadosCadastro = new DadosCadastroUsuarioDTO(NOME, EMAIL, SENHA);

        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_HASH);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("E-mail duplicado"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class, () -> {
            usuarioService.registrar(dadosCadastro);
        });

        assertEquals(excecao.getMessage(), String.format("O e-mail: %s já está cadastrado.", EMAIL));
        verify(pessoaRepository, never()).save(any());
    }

    /**
     * Testa o método {@link UsuarioService#atualizarSenha(Usuario, String)}.
     * Valida o fluxo de alteração de senha (RF17).
     *
     * <p>
     * Verifica se a nova senha é encriptada (RF06) e se a entidade é salva
     * no repositório com o novo hash.
     */
    @Test
    @DisplayName("atualizarSenha: Deve gerar novo hash e salvar as alterações")
    void testeAtualizarSenha_DeveCodificarESalvar() {
        String novaSenha = "NovaSenha123";
        String novoHash = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgG";
        Usuario usuarioMock = new Usuario(UUID.randomUUID(), NOME, EMAIL, SENHA_HASH);

        when(passwordEncoder.encode(novaSenha)).thenReturn(novoHash);

        usuarioService.atualizarSenha(usuarioMock, novaSenha);

        assertEquals(novoHash, usuarioMock.getSenha(), "A senha do objeto deve ser atualizada para o novo hash");

        verify(passwordEncoder).encode(novaSenha);
        verify(usuarioRepository).save(usuarioMock);
    }
}
