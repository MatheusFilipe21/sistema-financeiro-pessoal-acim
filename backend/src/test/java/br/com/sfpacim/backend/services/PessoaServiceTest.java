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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import br.com.sfpacim.backend.utils.MetodosUteis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe {@link PessoaService}.
 *
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas, validando
 * regras de negócio, formatação e o isolamento de dados por tenant.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ContaService contaService;

    @Mock
    private TransacaoService transacaoService;

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
     * Configura o cenário comum antes de cada teste com IDs simulados e
     * dados básicos instanciados.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Matheus Filipe do Nascimento Pereira",
                "matheusfnpereira@gmail.com", "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");
        usuario.setId(UUID.randomUUID());

        pessoa = new Pessoa(NOME, usuario);
        pessoa.setId(UUID.randomUUID());
        pessoa.setTitular(TITULAR);

        criarAtualizarPessoaDTO = new CriarAtualizarPessoaDTO(NOME, TITULAR);
    }

    /**
     * Testa o cenário de sucesso no método
     * {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     *
     * <p>
     * Garante que o DTO é convertido corretamente e que o repositório é
     * chamado para persistir os dados vinculados ao usuário logado.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve vincular ao usuário e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarPessoa() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.cadastrar(criarAtualizarPessoaDTO);

        assertNotNull(resultado, "O DTO retornado não deve ser nulo");
        assertEquals(NOME, resultado.nome(), "O nome deve ser preservado");
        verify(pessoaRepository).saveAndFlush(any(Pessoa.class));
    }

    /**
     * Testa a validação de unicidade de nome em memória no método
     * {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     *
     * <p>
     * Valida se a normalização de texto impede o cadastro de nomes iguais
     * com diferenças apenas em maiúsculas e minúsculas.
     */
    @Test
    @DisplayName("cadastrar: Quando nome existir na memória (Case Insensitive), deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicadoEmMemoria_DeveLancarExcecao() {
        Pessoa pessoaExistente = new Pessoa(NOME.toUpperCase(), usuario);
        pessoaExistente.setId(UUID.randomUUID());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(pessoaExistente));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.cadastrar(criarAtualizarPessoaDTO));

        assertEquals(String.format("Já existe uma Pessoa cadastrada com o nome '%s'.", NOME),
                excecao.getMessage());
        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa a ramificação do 'return null' dentro do catch.
     *
     * <p>
     * Utiliza MockedStatic para interceptar e emudecer o MetodosUteis. Como a
     * exceção
     * não será lançada, a execução atinge o 'return null'. O método 'paraDTO(null)'
     * lançará um NullPointerException, confirmando que passamos pela linha.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar erro, testa o catch cobrindo o return null (Mock Estático)")
    void testeCadastrar_ErroBanco_CobrindoReturnNull() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());

        when(pessoaRepository.saveAndFlush(any(Pessoa.class)))
                .thenThrow(new DataIntegrityViolationException("Erro de constraint"));

        try (MockedStatic<MetodosUteis> utilMock = mockStatic(MetodosUteis.class)) {
            assertThrows(NullPointerException.class, () -> pessoaService.cadastrar(criarAtualizarPessoaDTO));

            utilMock.verify(() -> MetodosUteis.validarUnicidade(true, Pessoa.class.getSimpleName(), NOME));
        }
    }

    /**
     * Testa o comportamento real do catch, garantindo que a exceção de integridade
     * é transformada na ViolacaoDadosException pela classe utilitária.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar erro de integridade, deve lançar ViolacaoDadosException")
    void testeCadastrar_ErroBanco_LancaExcecaoReal() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());

        when(pessoaRepository.saveAndFlush(any(Pessoa.class)))
                .thenThrow(new DataIntegrityViolationException("Erro de constraint"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.cadastrar(criarAtualizarPessoaDTO));

        assertTrue(excecao.getMessage().contains(NOME));
    }

    /**
     * Testa o método {@link PessoaService#listar()}.
     *
     * <p>
     * Verifica se o Collator está ordenando os nomes alfabeticamente
     * e respeitando apenas os registros do usuário logado.
     */
    @Test
    @DisplayName("listar: Deve retornar apenas pessoas do usuário autenticado ordenadas por nome")
    void testeListar_DeveRetornarRegistrosDoUsuarioOrdenados() {
        Pessoa p1 = new Pessoa("Zélia", usuario);
        Pessoa p2 = new Pessoa("Ana", usuario);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(p1, p2));

        List<PessoaDTO> resultado = pessoaService.listar();

        assertEquals(2, resultado.size());
        assertEquals("Ana", resultado.get(0).nome());
        assertEquals("Zélia", resultado.get(1).nome());
    }

    /**
     * Testa a atualização com sucesso no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     *
     * <p>
     * Verifica se os campos permitidos (nome e titularidade) são alterados
     * corretamente.
     */
    @Test
    @DisplayName("atualizar: Quando dados válidos, deve alterar nome e titular")
    void testeAtualizar_QuandoValido_DeveAtualizarDados() {
        CriarAtualizarPessoaDTO dtoNovo = new CriarAtualizarPessoaDTO(NOME_NOVO, TITULAR_NOVO);
        UUID pessoaId = pessoa.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(pessoa));
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.atualizar(pessoaId, dtoNovo);

        assertEquals(NOME_NOVO, resultado.nome());
        assertEquals(TITULAR_NOVO, resultado.titular());
    }

    /**
     * Testa a blindagem do serviço no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     *
     * <p>
     * Garante que uma tentativa de atualizar uma pessoa que não pertence
     * ao usuário lance a exceção apropriada.
     */
    @Test
    @DisplayName("atualizar: Quando ID não pertencer ao usuário, deve lançar EntityNotFoundException")
    void testeAtualizar_QuandoAcessoNegado_DeveLancarExcecao() {
        UUID pessoaId = pessoa.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> pessoaService.atualizar(pessoaId, criarAtualizarPessoaDTO));

        assertTrue(excecao.getMessage().contains("acesso negado"));
        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o comportamento de mesclagem parcial no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     *
     * <p>
     * Garante que ao receber um campo nulo (titular), o valor preexistente
     * na base de dados seja preservado em vez de sobrescrito por erro.
     */
    @Test
    @DisplayName("atualizar: Quando titular é nulo no DTO, deve manter valor original da entidade")
    void testeAtualizar_QuandoTitularNulo_NaoDeveAlterarTitularOriginal() {
        CriarAtualizarPessoaDTO dtoTitularNulo = new CriarAtualizarPessoaDTO(NOME_NOVO, null);
        UUID pessoaId = pessoa.getId();
        pessoa.setTitular(true);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(pessoa));
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PessoaDTO resultado = pessoaService.atualizar(pessoaId, dtoTitularNulo);

        assertEquals(NOME_NOVO, resultado.nome());
        assertTrue(resultado.titular());
    }

    /**
     * Testa a validação de unicidade na atualização no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     * 
     * <p>
     * Este teste cobre a ramificação de busca em memória onde um nome normalizado
     * coincide com o de outra pessoa já cadastrada, disparando a validação
     * antes mesmo da tentativa de persistência no banco de dados.
     */
    @Test
    @DisplayName("atualizar: Quando nome pertence a outra pessoa na memória, deve lançar ViolacaoDadosException")
    void testeAtualizar_QuandoNomeDuplicadoOutroId_DeveLancarExcecao() {
        UUID pessoaId = pessoa.getId();
        Pessoa outraPessoa = new Pessoa(NOME_NOVO, usuario);
        outraPessoa.setId(UUID.randomUUID());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(outraPessoa));

        CriarAtualizarPessoaDTO dtoConflito = new CriarAtualizarPessoaDTO(NOME_NOVO, false);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.atualizar(pessoaId, dtoConflito));

        assertEquals(String.format("Já existe uma Pessoa cadastrada com o nome '%s'.", NOME_NOVO),
                excecao.getMessage());
        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa a captura de exceção de integridade no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     * 
     * <p>
     * Este teste foca especificamente na cobertura do bloco {@code catch},
     * simulando um cenário onde a validação em memória passa, mas o banco de dados
     * rejeita a transação. Garante que o método utilize o {@link MetodosUteis} para
     * padronizar a exceção de retorno.
     */
    @Test
    @DisplayName("atualizar: Quando banco lançar exceção de integridade, deve tratar como ViolacaoDadosException")
    void testeAtualizar_QuandoConstraintViolation_DeveLancarExcecao() {
        UUID pessoaId = pessoa.getId();
        CriarAtualizarPessoaDTO dtoNovo = new CriarAtualizarPessoaDTO(NOME_NOVO, TITULAR_NOVO);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));

        when(pessoaRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());

        when(pessoaRepository.saveAndFlush(any(Pessoa.class)))
                .thenThrow(new DataIntegrityViolationException("Simulação de erro de banco"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.atualizar(pessoaId, dtoNovo));

        assertNotNull(excecao);
        verify(pessoaRepository).saveAndFlush(any(Pessoa.class));
    }

    /**
     * Testa a exclusão favorável no método {@link PessoaService#excluir(UUID)}.
     *
     * <p>
     * Valida que o repositório realiza a exclusão física caso não existam
     * impedimentos relacionais.
     */
    @Test
    @DisplayName("excluir: Quando não possuir vínculos, deve remover o registro")
    void testeExcluir_QuandoValido_DeveDeletar() {
        UUID pessoaId = pessoa.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));
        when(contaService.existeContaVinculadaAPessoa(pessoaId)).thenReturn(false);
        when(transacaoService.existeTransacaoVinculadaAPessoa(pessoaId)).thenReturn(false);

        assertDoesNotThrow(() -> pessoaService.excluir(pessoaId));

        verify(pessoaRepository).delete(pessoa);
    }

    /**
     * Testa o bloqueio de exclusão em cascata no método
     * {@link PessoaService#excluir(UUID)}.
     *
     * <p>
     * Garante que a exclusão seja interrompida e comunique o usuário se
     * existirem Contas ativas vinculadas à pessoa.
     */
    @Test
    @DisplayName("excluir: Quando possuir Contas vinculadas, deve lançar ViolacaoDadosException")
    void testeExcluir_QuandoPossuiContas_DeveLancarExcecao() {
        UUID pessoaId = pessoa.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));
        when(contaService.existeContaVinculadaAPessoa(pessoaId)).thenReturn(true);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.excluir(pessoaId));

        assertTrue(excecao.getMessage().contains("Contas vinculadas"));
        verify(pessoaRepository, never()).delete(any());
    }

    /**
     * Testa o bloqueio de exclusão em cascata no método
     * {@link PessoaService#excluir(UUID)}.
     *
     * <p>
     * Garante que a exclusão seja interrompida se existirem transações
     * históricas ou futuras atreladas à pessoa.
     */
    @Test
    @DisplayName("excluir: Quando possuir Transações vinculadas, deve lançar ViolacaoDadosException")
    void testeExcluir_QuandoPossuiTransacoes_DeveLancarExcecao() {
        UUID pessoaId = pessoa.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoaId)).thenReturn(Optional.of(pessoa));
        when(contaService.existeContaVinculadaAPessoa(pessoaId)).thenReturn(false);
        when(transacaoService.existeTransacaoVinculadaAPessoa(pessoaId)).thenReturn(true);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.excluir(pessoaId));

        assertTrue(excecao.getMessage().contains("Transações vinculadas"));
        verify(pessoaRepository, never()).delete(any());
    }

    /**
     * Testa o sucesso no método {@link PessoaService#validarTitularidade(Pessoa)}.
     *
     * <p>
     * Confirma que uma pessoa assinalada como titular passa
     * na validação sem lançar exceções.
     */
    @Test
    @DisplayName("validarTitularidade: Quando titular for verdadeiro, não deve lançar exceção")
    void testeValidarTitularidade_QuandoTitular_NaoDeveLancarExcecao() {
        pessoa.setTitular(true);

        assertDoesNotThrow(() -> pessoaService.validarTitularidade(pessoa));
    }

    /**
     * Testa o bloqueio no método {@link PessoaService#validarTitularidade(Pessoa)}.
     *
     * <p>
     * Confirma que uma pessoa não assinalada como titular lança exceção
     * interrompendo a vinculação indevida com contas.
     */
    @Test
    @DisplayName("validarTitularidade: Quando não for titular, deve lançar ViolacaoDadosException")
    void testeValidarTitularidade_QuandoNaoForTitular_DeveLancarExcecao() {
        pessoa.setTitular(false);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> pessoaService.validarTitularidade(pessoa));

        assertTrue(excecao.getMessage().contains("não é um titular habilitado"));
    }
}
