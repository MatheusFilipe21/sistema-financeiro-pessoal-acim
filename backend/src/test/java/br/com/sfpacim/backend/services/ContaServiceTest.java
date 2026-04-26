package br.com.sfpacim.backend.services;

import java.math.BigDecimal;
import java.util.Collections;
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

import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.repositories.ContaRepository;
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
 * Testes unitários para a classe {@link ContaService}.
 *
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas, validando
 * regras de negócio, recálculos de saldo e o isolamento de dados. Faz uso do
 * MockedStatic para cobertura de ramificações defensivas nos blocos catch.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private PessoaService pessoaService;

    @Mock
    private TransacaoService transacaoService;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

    @InjectMocks
    private ContaService contaService;

    private static final String NOME_CONTA = "Investimentos Mercado Pago";
    private static final String NOME_CONTA_NOVO = "Cofrinhos Mercado Pago";
    private static final InstituicaoFinanceira INSTITUICAO = InstituicaoFinanceira.MERCADO_PAGO;
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1000.00");

    private Usuario usuario;
    private Pessoa pessoaTitular;
    private Conta conta;
    private CriarAtualizarContaDTO criarAtualizarContaDTO;

    /**
     * Configura o cenário comum antes de cada teste com IDs simulados.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Usuario Teste", "teste@email.com", "senha123");
        usuario.setId(UUID.randomUUID());

        pessoaTitular = new Pessoa("Titular", usuario);
        pessoaTitular.setId(UUID.randomUUID());
        pessoaTitular.setTitular(true);

        conta = new Conta(NOME_CONTA, INSTITUICAO, SALDO_INICIAL, pessoaTitular);
        conta.setId(UUID.randomUUID());

        criarAtualizarContaDTO = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, SALDO_INICIAL,
                pessoaTitular.getId());
    }

    /**
     * Testa o cenário de sucesso no método
     * {@link ContaService#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve vincular à pessoa e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarConta() {
        UUID usuarioId = usuario.getId();
        UUID pessoaId = pessoaTitular.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaId)).thenReturn(pessoaTitular);
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId)).thenReturn(Collections.emptyList());
        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.cadastrar(criarAtualizarContaDTO);

        assertNotNull(resultado, "O DTO retornado não deve ser nulo");
        assertEquals(NOME_CONTA, resultado.nome());
        assertEquals(SALDO_INICIAL, resultado.saldoAtual());

        verify(contaRepository).saveAndFlush(any(Conta.class));
    }

    /**
     * Testa a validação de unicidade de nome em memória no método
     * {@link ContaService#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado para a pessoa na memória, deve lançar exceção")
    void testeCadastrar_QuandoNomeDuplicadoEmMemoria_DeveLancarExcecao() {
        UUID usuarioId = usuario.getId();
        UUID pessoaId = pessoaTitular.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaId)).thenReturn(pessoaTitular);
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId)).thenReturn(List.of(conta));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.cadastrar(criarAtualizarContaDTO));

        assertTrue(excecao.getMessage().contains(NOME_CONTA));
        verify(contaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o comportamento real do catch no método
     * {@link ContaService#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar erro de integridade, deve lançar ViolacaoDadosException")
    void testeCadastrar_ErroBanco_LancaExcecaoReal() {
        UUID usuarioId = usuario.getId();
        UUID pessoaId = pessoaTitular.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaId)).thenReturn(pessoaTitular);
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId)).thenReturn(Collections.emptyList());

        when(contaRepository.saveAndFlush(any(Conta.class)))
                .thenThrow(new DataIntegrityViolationException("Erro constraint"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.cadastrar(criarAtualizarContaDTO));

        assertTrue(excecao.getMessage().contains(NOME_CONTA));
    }

    /**
     * Testa a ramificação do 'return null' dentro do catch durante o cadastro.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar erro, testa o catch cobrindo o return null (Mock Estático)")
    void testeCadastrar_ErroBanco_CobrindoReturnNull() {
        UUID usuarioId = usuario.getId();
        UUID pessoaId = pessoaTitular.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaId)).thenReturn(pessoaTitular);
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId)).thenReturn(Collections.emptyList());

        when(contaRepository.saveAndFlush(any(Conta.class)))
                .thenThrow(new DataIntegrityViolationException("Erro constraint"));

        try (MockedStatic<MetodosUteis> utilMock = mockStatic(MetodosUteis.class)) {
            assertThrows(NullPointerException.class, () -> contaService.cadastrar(criarAtualizarContaDTO));
            utilMock.verify(() -> MetodosUteis.validarUnicidade(true, Conta.class.getSimpleName(), NOME_CONTA,
                    pessoaTitular.getNome()));
        }
    }

    /**
     * Testa a ordenação composta do método {@link ContaService#listar()}.
     */
    @Test
    @DisplayName("listar: Deve ordenar por Nome do Titular e depois por Nome da Conta")
    void testeListar_DeveRetornarOrdenadoPorTitularEConta() {
        Pessoa bruno = new Pessoa("Bruno", usuario);
        Pessoa ana = new Pessoa("Ana", usuario);

        Conta contaBruno = new Conta("Conta Itaú", InstituicaoFinanceira.ITAU, BigDecimal.ZERO, bruno);
        Conta contaAnaNubank = new Conta("Conta Nubank", InstituicaoFinanceira.NUBANK, BigDecimal.ZERO, ana);
        Conta contaAnaInter = new Conta("Conta Inter", InstituicaoFinanceira.INTER, BigDecimal.ZERO, ana);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioId(usuario.getId()))
                .thenReturn(List.of(contaBruno, contaAnaNubank, contaAnaInter));

        List<ContaDTO> resultado = contaService.listar();

        assertEquals(3, resultado.size());
        assertEquals("Ana", resultado.get(0).pessoa().nome());
        assertEquals("Conta Inter", resultado.get(0).nome());
        assertEquals("Ana", resultado.get(1).pessoa().nome());
        assertEquals("Conta Nubank", resultado.get(1).nome());
        assertEquals("Bruno", resultado.get(2).pessoa().nome());
        assertEquals("Conta Itaú", resultado.get(2).nome());
    }

    /**
     * Testa a atualização com sucesso no método
     * {@link ContaService#atualizar(UUID, CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando válido e sem mudança de pessoa, deve atualizar os dados")
    void testeAtualizar_QuandoValido_DeveAtualizar() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();
        CriarAtualizarContaDTO dtoNovo = new CriarAtualizarContaDTO(NOME_CONTA_NOVO, InstituicaoFinanceira.INTER,
                SALDO_INICIAL, pessoaTitular.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaTitular.getId()))
                .thenReturn(List.of(conta));

        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.atualizar(contaId, dtoNovo);

        assertEquals(NOME_CONTA_NOVO, resultado.nome());
        assertEquals(InstituicaoFinanceira.INTER, resultado.instituicao());
    }

    /**
     * Testa a lógica de recálculo (Delta) no método
     * {@link ContaService#atualizar(UUID, CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando saldo inicial muda, deve recalcular saldo atual (Delta)")
    void testeAtualizar_QuandoSaldoInicialMuda_DeveRecalcularSaldoAtual() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        conta.debitar(new BigDecimal("200.00"));

        BigDecimal novoSaldoInicial = new BigDecimal("1100.00");
        CriarAtualizarContaDTO dtoSaldoAlterado = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, novoSaldoInicial,
                pessoaTitular.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaTitular.getId()))
                .thenReturn(Collections.emptyList());
        when(contaRepository.saveAndFlush(any(Conta.class))).thenAnswer(i -> i.getArguments()[0]);

        contaService.atualizar(contaId, dtoSaldoAlterado);

        assertEquals(new BigDecimal("900.00"), conta.getSaldoAtual(),
                "O saldo atual deveria ter sido recalculado com a diferença");
        assertEquals(novoSaldoInicial, conta.getSaldoInicial());
    }

    /**
     * Testa o bloco IF de transferência de titularidade no método
     * {@link ContaService#atualizar(UUID, CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando troca de pessoa, deve validar nova pessoa e atualizar vínculo")
    void testeAtualizar_QuandoTrocaTitular_DeveAtualizarPessoa() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        Pessoa novaPessoa = new Pessoa("Novo Dono", usuario);
        novaPessoa.setId(UUID.randomUUID());
        novaPessoa.setTitular(true);

        CriarAtualizarContaDTO dtoTransferencia = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, SALDO_INICIAL,
                novaPessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(pessoaService.obterEntidadeValidada(usuarioId, novaPessoa.getId())).thenReturn(novaPessoa);
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, novaPessoa.getId()))
                .thenReturn(Collections.emptyList());
        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.atualizar(contaId, dtoTransferencia);

        assertEquals(novaPessoa.getId(), resultado.pessoa().id());
        verify(pessoaService).validarTitularidade(novaPessoa);
    }

    /**
     * Testa a ramificação do 'return null' dentro do catch durante a atualização.
     */
    @Test
    @DisplayName("atualizar: Quando banco lançar erro, testa o catch cobrindo o return null (Mock Estático)")
    void testeAtualizar_ErroBanco_CobrindoReturnNull() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaTitular.getId()))
                .thenReturn(Collections.emptyList());

        when(contaRepository.saveAndFlush(any(Conta.class)))
                .thenThrow(new DataIntegrityViolationException("Erro constraint"));

        try (MockedStatic<MetodosUteis> utilMock = mockStatic(MetodosUteis.class)) {
            assertThrows(NullPointerException.class, () -> contaService.atualizar(contaId, criarAtualizarContaDTO));
            utilMock.verify(() -> MetodosUteis.validarUnicidade(true, Conta.class.getSimpleName(), NOME_CONTA,
                    pessoaTitular.getNome()));
        }
    }

    /**
     * Testa a exclusão favorável no método {@link ContaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando não possuir vínculos, deve remover a conta")
    void testeExcluir_QuandoValido_DeveDeletar() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(transacaoService.existeTransacaoVinculadaAConta(contaId)).thenReturn(false);

        assertDoesNotThrow(() -> contaService.excluir(contaId));

        verify(contaRepository).delete(conta);
    }

    /**
     * Testa o bloqueio de exclusão em cascata no método
     * {@link ContaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando possuir Transações vinculadas, deve lançar ViolacaoDadosException")
    void testeExcluir_QuandoPossuiTransacoes_DeveLancarExcecao() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(transacaoService.existeTransacaoVinculadaAConta(contaId)).thenReturn(true);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.excluir(contaId));

        assertTrue(excecao.getMessage().contains("Transações"));
        verify(contaRepository, never()).delete(any());
    }

    /**
     * Testa o método de validação local
     * {@link ContaService#obterEntidadeValidada(UUID, UUID)}.
     */
    @Test
    @DisplayName("obterEntidadeValidada: Quando não encontrada, deve lançar EntityNotFoundException")
    void testeObterEntidadeValidada_QuandoNaoEncontrada_DeveLancarExcecao() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> contaService.obterEntidadeValidada(usuarioId, contaId));

        assertTrue(excecao.getMessage().contains("não encontrada ou acesso negado"));
    }

    /**
     * Testa o método interno para comunicação com PessoaService.
     */
    @Test
    @DisplayName("existeContaVinculadaAPessoa: Deve retornar verdadeiro se o repositório acusar existência")
    void testeExisteContaVinculadaAPessoa_DeveRetornarTrue() {
        UUID pessoaId = pessoaTitular.getId();
        UUID usuarioId = usuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.existsByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId)).thenReturn(true);

        boolean resultado = contaService.existeContaVinculadaAPessoa(pessoaId);

        assertTrue(resultado);
    }

    /**
     * Testa a validação de unicidade na atualização no método
     * {@link ContaService#atualizar(UUID, CriarAtualizarContaDTO)}.
     *
     * <p>
     * Este teste cobre a ramificação de busca em memória onde um nome normalizado
     * coincide com o de outra conta já cadastrada (IDs diferentes), disparando a
     * validação
     * antes mesmo da tentativa de persistência no banco de dados.
     */
    @Test
    @DisplayName("atualizar: Quando nome pertence a outra conta na memória, deve lançar ViolacaoDadosException")
    void testeAtualizar_QuandoNomeDuplicadoOutroId_DeveLancarExcecao() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        Conta outraConta = new Conta(NOME_CONTA_NOVO, INSTITUICAO, SALDO_INICIAL, pessoaTitular);
        outraConta.setId(UUID.randomUUID());

        CriarAtualizarContaDTO dtoConflito = new CriarAtualizarContaDTO(NOME_CONTA_NOVO, INSTITUICAO, SALDO_INICIAL,
                pessoaTitular.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaTitular.getId()))
                .thenReturn(List.of(outraConta));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.atualizar(contaId, dtoConflito));

        assertTrue(excecao.getMessage().contains(NOME_CONTA_NOVO));
        verify(contaRepository, never()).saveAndFlush(any());
    }
}
