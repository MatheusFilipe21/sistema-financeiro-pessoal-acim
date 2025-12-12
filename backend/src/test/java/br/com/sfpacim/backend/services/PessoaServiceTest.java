package br.com.sfpacim.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Testes unitários para a classe {@link PessoaService}.
 *
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas
 * (PessoaRepository e ContextoUsuarioService).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

    @InjectMocks
    private PessoaService pessoaService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String NOME_NOVO = "Ilka Fernanda Berenguer Paz";

    private Usuario usuario;
    private Pessoa pessoa;
    private CriarAtualizarPessoaDTO criarAtualizarPessoaDTO;

    /**
     * Configura o cenário comum antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Matheus Filipe do Nascimento Pereira",
                "matheusfnpereira@gmail.com", "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");
        usuario.setId(UUID.randomUUID());

        pessoa = new Pessoa(NOME, usuario);
        pessoa.setId(UUID.randomUUID());

        criarAtualizarPessoaDTO = new CriarAtualizarPessoaDTO(NOME);
    }

    /**
     * Testa o método {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     * Valida o cenário de sucesso.
     *
     * <p>
     * Verifica se o serviço recupera o usuário, converte para entidade
     * e salva no repositório.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve vincular ao usuário e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarPessoa() {

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.cadastrar(criarAtualizarPessoaDTO);

        assertNotNull(resultado, "O DTO retornado não deve ser nulo");
        assertEquals(NOME, resultado.nome(), "O nome deve ser preservado");

        verify(contextoUsuarioService).getUsuarioAutenticado();
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    /**
     * Testa o método {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     * Valida o cenário de falha por nome duplicado para o usuário (RF - Unicidade).
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("cadastrar: Quando nome duplicado, deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicado_DeveLancarExcecao() {

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.save(any(Pessoa.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint Violation"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.cadastrar(criarAtualizarPessoaDTO));

        assertEquals(String.format("Já existe uma pessoa cadastrada com o nome '%s'.", NOME),
                excecao.getMessage());
    }

    /**
     * Testa o método {@link PessoaService#listar()}.
     * Valida se apenas os registros do usuário autenticado são retornados.
     */
    @Test
    @DisplayName("listar: Deve retornar apenas pessoas do usuário autenticado")
    void testeListar_DeveRetornarRegistrosDoUsuario() {

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(pessoa));

        List<PessoaDTO> resultado = pessoaService.listar();

        assertFalse(resultado.isEmpty(), "A lista não deve estar vazia");
        assertEquals(1, resultado.size());
        assertEquals(NOME, resultado.get(0).nome());

        verify(pessoaRepository).findByUsuario(usuario);
    }

    /**
     * Testa o método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     * Valida o cenário de sucesso.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("atualizar: Quando pessoa existe e pertence ao usuário, deve atualizar")
    void testeAtualizar_QuandoValido_DeveAtualizarNome() {
        CriarAtualizarPessoaDTO criarAtualizarPessoaDTONovo = new CriarAtualizarPessoaDTO(NOME_NOVO);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.atualizar(pessoa.getId(), criarAtualizarPessoaDTONovo);

        assertEquals(NOME_NOVO, resultado.nome(), "O nome retornado deve ser o novo");
        assertEquals(NOME_NOVO, pessoa.getNome(), "A entidade deve ter sido alterada");

        verify(pessoaRepository).save(pessoa);
    }

    /**
     * Testa a segurança do método {@link PessoaService#atualizar(UUID, PessoaDTO)}.
     * Tenta atualizar um registro que pertence a OUTRO usuário.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("atualizar: Quando pessoa pertence a outro usuário, deve lançar EntityNotFoundException")
    void testeAtualizar_QuandoPertenceOutroUsuario_DeveLancarExcecao() {

        Usuario outroUsuario = new Usuario("Outro", "outro@email.com", "123");
        outroUsuario.setId(UUID.randomUUID());

        Pessoa pessoaDeOutro = new Pessoa("Intruso", outroUsuario);
        pessoaDeOutro.setId(UUID.randomUUID());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoaDeOutro.getId())).thenReturn(Optional.of(pessoaDeOutro));

        UUID idPessoaOutro = pessoaDeOutro.getId();

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> pessoaService.atualizar(idPessoaOutro, criarAtualizarPessoaDTO));

        assertTrue(excecao.getMessage().contains("acesso negado"),
                "A mensagem deve indicar acesso negado ou não encontrado");
        verify(pessoaRepository, never()).save(any());
    }

    /**
     * Testa o método {@link PessoaService#excluir(UUID)}.
     * Valida o cenário de sucesso.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("excluir: Quando válido, deve remover o registro")
    void testeExcluir_QuandoValido_DeveDeletar() {

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        pessoaService.excluir(pessoa.getId());

        verify(pessoaRepository).delete(pessoa);
    }
}
