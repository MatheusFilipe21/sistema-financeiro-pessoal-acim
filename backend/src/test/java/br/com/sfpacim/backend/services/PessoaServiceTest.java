package br.com.sfpacim.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

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
import br.com.sfpacim.backend.repositories.ContaRepository;
import br.com.sfpacim.backend.repositories.PessoaRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ContaRepository contaRepository;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

    @InjectMocks
    private PessoaService pessoaService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String NOME_NOVO = "Ilka Fernanda Berenguer Paz";
    private static final boolean TITULAR = true;
    private static final boolean TITULAR_NOVO = false;

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

        criarAtualizarPessoaDTO = new CriarAtualizarPessoaDTO(NOME, TITULAR);
    }

    /**
     * Testa o método {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     * Valida o cenário de sucesso.
     *
     * <p>
     * Verifica se o serviço recupera o usuário, converte para entidade
     * e salva no repositório.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve vincular ao usuário e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarPessoa() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.cadastrar(criarAtualizarPessoaDTO);

        assertNotNull(resultado, "O DTO retornado não deve ser nulo");
        assertEquals(NOME, resultado.nome(), "O nome deve ser preservado");

        verify(contextoUsuarioService).getUsuarioAutenticado();
        verify(pessoaRepository).saveAndFlush(any(Pessoa.class));
    }

    /**
     * Testa o método {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     * Valida o cenário de falha por nome duplicado para o usuário.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado, deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicado_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.saveAndFlush(any(Pessoa.class)))
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
    @Test
    @DisplayName("atualizar: Quando pessoa existe e pertence ao usuário, deve atualizar")
    void testeAtualizar_QuandoValido_DeveAtualizarNome() {
        CriarAtualizarPessoaDTO criarAtualizarPessoaDTONovo = new CriarAtualizarPessoaDTO(NOME_NOVO, TITULAR_NOVO);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.atualizar(pessoa.getId(), criarAtualizarPessoaDTONovo);

        assertEquals(NOME_NOVO, resultado.nome(), "O nome retornado deve ser o novo");
        assertEquals(NOME_NOVO, pessoa.getNome(), "A entidade deve ter sido alterada");

        verify(pessoaRepository).saveAndFlush(pessoa);
    }

    /**
     * Testa a segurança do método {@link PessoaService#atualizar(UUID, PessoaDTO)}.
     * Tenta atualizar um registro que pertence a OUTRO usuário.
     */
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
        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o método {@link PessoaService#atualizar} quando o campo titular é nulo.
     * Deve manter o valor original da entidade.
     */
    @Test
    @DisplayName("atualizar: Quando titular é nulo no DTO, deve manter valor original")
    void testeAtualizar_QuandoTitularNulo_NaoDeveAlterarTitular() {
        CriarAtualizarPessoaDTO dtoTitularNulo = new CriarAtualizarPessoaDTO(NOME_NOVO, null);
        pessoa.setTitular(true);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PessoaDTO resultado = pessoaService.atualizar(pessoa.getId(), dtoTitularNulo);

        assertEquals(NOME_NOVO, resultado.nome());
        assertTrue(resultado.titular(), "O valor original (true) deveria ter sido mantido");

        verify(pessoaRepository).saveAndFlush(pessoa);
    }

    /**
     * Testa o método {@link PessoaService#excluir(UUID)}.
     * Valida o cenário de sucesso.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve remover o registro")
    void testeExcluir_QuandoValido_DeveDeletar() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        pessoaService.excluir(pessoa.getId());

        verify(pessoaRepository).delete(pessoa);
    }

    /**
     * Testa a validação de unicidade (Collator) no cadastro.
     * Deve lançar exceção se já existir nome igual (ignorando case/acento).
     */
    @Test
    @DisplayName("cadastrar: Quando nome existe (Case Insensitive), deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicadoLogicaService_DeveLancarExcecao() {
        Pessoa pessoaExistente = new Pessoa("JOÃO", usuario);
        pessoaExistente.setId(UUID.randomUUID());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);

        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(pessoaExistente));

        CriarAtualizarPessoaDTO dtoDuplicado = new CriarAtualizarPessoaDTO("João", true);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.cadastrar(dtoDuplicado));

        assertEquals("Já existe uma pessoa cadastrada com o nome 'João'.", excecao.getMessage());

        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa a validação de unicidade na atualização.
     * Deve permitir atualizar se o nome conflitante for do PRÓPRIO registro.
     */
    @Test
    @DisplayName("atualizar: Quando nome é igual ao próprio registro, deve permitir atualização")
    void testeAtualizar_QuandoNomeIgualAoProprio_DevePermitir() {

        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(pessoa));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenReturn(pessoa);

        CriarAtualizarPessoaDTO dtoMesmoNome = new CriarAtualizarPessoaDTO(NOME, false);

        assertDoesNotThrow(() -> pessoaService.atualizar(pessoa.getId(), dtoMesmoNome));

        verify(pessoaRepository).saveAndFlush(pessoa);
    }

    /**
     * Testa a validação de unicidade na atualização (Erro).
     * Deve bloquear se o nome pertencer a OUTRO registro.
     */
    @Test
    @DisplayName("atualizar: Quando nome pertence a outra pessoa, deve lançar exceção")
    void testeAtualizar_QuandoNomeDuplicadoOutroId_DeveLancarExcecao() {

        Pessoa outraPessoa = new Pessoa("Outra Pessoa", usuario);
        outraPessoa.setId(UUID.randomUUID());

        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(outraPessoa));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        CriarAtualizarPessoaDTO dtoConflito = new CriarAtualizarPessoaDTO("Outra Pessoa", false);

        UUID idParaAtualizar = pessoa.getId();

        assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.atualizar(idParaAtualizar, dtoConflito));

        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa a ordenação da listagem.
     * Deve retornar alfabeticamente (A-Z) independente da ordem do banco.
     */
    @Test
    @DisplayName("listar: Deve retornar lista ordenada alfabeticamente por nome")
    void testeListar_DeveRetornarOrdenadoPorNome() {
        Pessoa p1 = new Pessoa("Zélia", usuario);
        Pessoa p2 = new Pessoa("Ana", usuario);
        Pessoa p3 = new Pessoa("Carlos", usuario);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(p1, p2, p3));

        List<PessoaDTO> resultado = pessoaService.listar();

        assertEquals(3, resultado.size());

        assertEquals("Ana", resultado.get(0).nome());
        assertEquals("Carlos", resultado.get(1).nome());
        assertEquals("Zélia", resultado.get(2).nome());
    }

    /**
     * Testa o bloqueio de exclusão quando a pessoa possui vínculos.
     * Deve lançar ViolacaoDadosException se existirem contas vinculadas.
     */
    @Test
    @DisplayName("excluir: Quando existem contas vinculadas, deve lançar ViolacaoDadosException")
    void testeExcluir_QuandoPossuiContas_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoa.getId())).thenReturn(Optional.of(pessoa));

        when(contaRepository.findByPessoa(pessoa)).thenReturn(List.of(mock(br.com.sfpacim.backend.models.Conta.class)));

        UUID idPessoa = pessoa.getId();

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.excluir(idPessoa));

        assertTrue(excecao.getMessage().contains("existem Contas vinculadas"),
                "A mensagem deve informar sobre o vínculo com contas");

        verify(pessoaRepository, never()).delete(any());
    }
}
