package br.com.sfpacim.backend.services;

import java.math.BigDecimal;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.dtos.conta.FiltroContaDTO;
import br.com.sfpacim.backend.dtos.conta.ListagemContaDTO;
import br.com.sfpacim.backend.dtos.conta.SelecaoContaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.repositories.ContaRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private MessageSource messageSource;

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

        Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any(), any()))
                .thenReturn("Mensagem Mockada");
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
        when(contaRepository.existeContaDuplicada(usuarioId, pessoaId, INSTITUICAO, NOME_CONTA, null))
                .thenReturn(false);
        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.cadastrar(criarAtualizarContaDTO);

        assertNotNull(resultado, "O DTO retornado não deve ser nulo");
        assertEquals(NOME_CONTA, resultado.nome());
        assertEquals(SALDO_INICIAL, resultado.saldoAtual());

        verify(contaRepository).saveAndFlush(any(Conta.class));
    }

    /**
     * Testa a validação proativa de unicidade de nome no banco no método
     * {@link ContaService#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando banco apontar duplicidade (proativo), deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicadoEmMemoria_DeveLancarExcecao() {
        UUID usuarioId = usuario.getId();
        UUID pessoaId = pessoaTitular.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaId)).thenReturn(pessoaTitular);
        when(contaRepository.existeContaDuplicada(usuarioId, pessoaId, INSTITUICAO, NOME_CONTA, null)).thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> contaService.cadastrar(criarAtualizarContaDTO));
        verify(contaRepository, never()).saveAndFlush(any(Conta.class));
    }

    /**
     * Testa o comportamento reativo do catch (banco de dados) no método
     * {@link ContaService#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar erro de integridade (reativo), deve lançar ViolacaoDadosException")
    void testeCadastrar_ErroBanco_LancaExcecaoReal() {
        UUID usuarioId = usuario.getId();
        UUID pessoaId = pessoaTitular.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaId)).thenReturn(pessoaTitular);
        when(contaRepository.existeContaDuplicada(usuarioId, pessoaId, INSTITUICAO, NOME_CONTA, null))
                .thenReturn(false);

        when(contaRepository.saveAndFlush(any(Conta.class)))
                .thenThrow(new DataIntegrityViolationException("Erro constraint"));

        assertThrows(ViolacaoDadosException.class, () -> contaService.cadastrar(criarAtualizarContaDTO));
    }

    /**
     * Testa a listagem paginada e filtrada no método
     * {@link ContaService#listar(FiltroContaDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar a página de contas filtradas")
    @SuppressWarnings("unchecked")
    void testeListar_ComSucesso() {
        FiltroContaDTO filtro = new FiltroContaDTO(null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conta> pagina = new PageImpl<>(List.of(conta));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<ListagemContaDTO> resultado = contaService.listar(filtro, pageable);

        assertEquals(1, resultado.getContent().size());
    }

    /**
     * Testa a listagem de opções para seleção (Dropdowns) no método
     * {@link ContaService#listarOpcoes()}.
     */
    @Test
    @DisplayName("listarOpcoes: Deve retornar as opções simplificadas")
    void testeListarOpcoes_ComSucesso() {
        SelecaoContaDTO selecao = new SelecaoContaDTO(conta.getId(), NOME_CONTA, INSTITUICAO, pessoaTitular.getNome());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.buscarOpcoesParaSelecao(usuario.getId())).thenReturn(List.of(selecao));

        List<SelecaoContaDTO> resultado = contaService.listarOpcoes();

        assertEquals(1, resultado.size());
        assertEquals(NOME_CONTA, resultado.get(0).nome());
    }

    /**
     * Testa a busca pelo identificador no método
     * {@link ContaService#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Deve retornar a ContaDTO quando encontrada")
    void testeBuscarPorId_ComSucesso() {
        UUID id = conta.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuario.getId(), id))
                .thenReturn(Optional.of(conta));

        ContaDTO resultado = contaService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(conta.getNome(), resultado.nome());
    }

    /**
     * Testa a blindagem da consulta no método privado de validação
     * {@link ContaService#obterEntidadeValidada(UUID, UUID)}.
     */
    @Test
    @DisplayName("obterEntidadeValidada: Deve lançar EntityNotFoundException na falha do lambda orElseThrow")
    void testeObterEntidadeValidada_NaoEncontrada_DeveLancarExcecao() {
        UUID id = UUID.randomUUID();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuario.getId(), id))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> contaService.buscarPorId(id));
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
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaTitular.getId())).thenReturn(pessoaTitular);
        when(contaRepository.existeContaDuplicada(usuarioId, pessoaTitular.getId(), InstituicaoFinanceira.INTER,
                NOME_CONTA_NOVO, contaId)).thenReturn(false);

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
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaTitular.getId())).thenReturn(pessoaTitular);
        when(contaRepository.existeContaDuplicada(usuarioId, pessoaTitular.getId(), INSTITUICAO, NOME_CONTA, contaId))
                .thenReturn(false);

        when(contaRepository.saveAndFlush(any(Conta.class))).thenAnswer(i -> i.getArguments()[0]);

        contaService.atualizar(contaId, dtoSaldoAlterado);

        assertEquals(new BigDecimal("900.00"), conta.getSaldoAtual(),
                "O saldo atual deveria ter sido recalculado com a diferença");
        assertEquals(novoSaldoInicial, conta.getSaldoInicial());
    }

    /**
     * Testa o fluxo de transferência de titularidade no método
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
        when(contaRepository.existeContaDuplicada(usuarioId, novaPessoa.getId(), INSTITUICAO, NOME_CONTA, contaId))
                .thenReturn(false);

        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.atualizar(contaId, dtoTransferencia);

        assertEquals(novaPessoa.getId(), resultado.pessoaId());
        verify(pessoaService).validarTitularidade(novaPessoa);
    }

    /**
     * Testa a validação proativa de unicidade na atualização no método
     * {@link ContaService#atualizar(UUID, CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando duplicado no banco (proativo), deve lançar ViolacaoDadosException")
    void testeAtualizar_QuandoNomeDuplicado_DeveLancarExcecao() {
        UUID contaId = conta.getId();
        UUID usuarioId = usuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)).thenReturn(Optional.of(conta));
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoaTitular.getId())).thenReturn(pessoaTitular);

        when(contaRepository.existeContaDuplicada(usuarioId, pessoaTitular.getId(), INSTITUICAO, NOME_CONTA, contaId))
                .thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> contaService.atualizar(contaId, criarAtualizarContaDTO));
        verify(contaRepository, never()).saveAndFlush(any(Conta.class));
    }

    /**
     * Testa a exclusão favorável no método
     * {@link ContaService#excluir(UUID)}.
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

        assertThrows(ViolacaoDadosException.class, () -> contaService.excluir(contaId));
        verify(contaRepository, never()).delete(any(Conta.class));
    }

    /**
     * Testa a verificação de vínculo com pessoa no método
     * {@link ContaService#existeContaVinculadaAPessoa(UUID)}.
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
}