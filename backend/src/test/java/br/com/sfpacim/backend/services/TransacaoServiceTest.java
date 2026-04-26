package br.com.sfpacim.backend.services;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.TransacaoDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import br.com.sfpacim.backend.repositories.TransacaoRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe {@link TransacaoService}.
 *
 * <p>
 * Valida a orquestração de chamadas aos serviços de domínio, regras
 * matemáticas de efetivação/estorno e isolamento de dados do usuário.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private ContaService contaService;

    @Mock
    private PessoaService pessoaService;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuario;
    private Categoria categoria;
    private Conta conta;
    private Pessoa pessoa;
    private Transacao transacao;

    private static final BigDecimal VALOR_TRANSACAO = new BigDecimal("100.00");

    /**
     * Configura o cenário comum antes de cada teste com entidades simuladas.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Matheus F. N. Pereira", "matheus@email.com", "senha123");
        usuario.setId(UUID.randomUUID());

        categoria = new Categoria("Alimentação", TipoCategoria.DESPESA, "restaurant", "#FF0000", usuario);
        categoria.setId(UUID.randomUUID());

        Pessoa titular = new Pessoa("Titular Conta", usuario);
        titular.setId(UUID.randomUUID());

        conta = new Conta("Conta Corrente", InstituicaoFinanceira.ITAU, new BigDecimal("1000.00"), titular);
        conta.setId(UUID.randomUUID());

        pessoa = new Pessoa("Fornecedor", usuario);
        pessoa.setId(UUID.randomUUID());

        transacao = Transacao.builder()
                .id(UUID.randomUUID())
                .descricao("Compra Mercado")
                .valor(VALOR_TRANSACAO)
                .dataCompetencia(LocalDate.now())
                .dataVencimento(LocalDate.now())
                .tipo(TipoTransacao.DESPESA)
                .status(StatusTransacao.PENDENTE)
                .usuario(usuario)
                .categoria(categoria)
                .conta(conta)
                .pessoa(pessoa)
                .build();
    }

    /**
     * Testa cadastro de Despesa Paga no método
     * {@link TransacaoService#cadastrar(CriarAtualizarTransacaoDTO)}.
     * <p>
     * Cobre efetivação de Débito.
     * </p>
     */
    @Test
    @DisplayName("cadastrar: Quando Despesa PAGA, deve efetivar débito na conta e salvar")
    void testeCadastrar_QuandoDespesaPaga_DeveDebitarESalvar() {
        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Supermercado", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), LocalDate.now(),
                TipoTransacao.DESPESA, StatusTransacao.PAGO, null,
                categoria.getId(), conta.getId(), pessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaService.obterEntidadeValidada(usuario.getId(), dto.categoriaId())).thenReturn(categoria);
        when(contaService.obterEntidadeValidada(usuario.getId(), dto.contaId())).thenReturn(conta);
        when(pessoaService.obterEntidadeValidada(usuario.getId(), dto.pessoaId())).thenReturn(pessoa);

        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.cadastrar(dto);

        assertNotNull(resultado);

        assertEquals(new BigDecimal("900.00"), conta.getSaldoAtual());
        verify(transacaoRepository).saveAndFlush(any(Transacao.class));
    }

    /**
     * Testa cadastro de Receita Pendente sem Pessoa.
     *
     * <p>
     * Cobre ramo Sem Pessoa e Sem Efetivação.
     */
    @Test
    @DisplayName("cadastrar: Quando Receita PENDENTE sem pessoa, não deve alterar saldo e salvar")
    void testeCadastrar_QuandoReceitaPendenteSemPessoa_NaoDeveEfetivar() {
        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Salário", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.RECEITA, StatusTransacao.PENDENTE, null,
                categoria.getId(), conta.getId(), null);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaService.obterEntidadeValidada(usuario.getId(), dto.categoriaId())).thenReturn(categoria);
        when(contaService.obterEntidadeValidada(usuario.getId(), dto.contaId())).thenReturn(conta);
        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.cadastrar(dto);

        assertNull(resultado.pessoaId());
        assertEquals(new BigDecimal("1000.00"), conta.getSaldoAtual());
        verify(pessoaService, never()).obterEntidadeValidada(any(), any());
    }

    /**
     * Testa listagem por período (paginada) no método
     * {@link TransacaoService#listarPorPeriodo(LocalDate, LocalDate, Pageable)}.
     */
    @Test
    @DisplayName("listarPorPeriodo: Deve retornar página de TransacaoDTO")
    void testeListarPorPeriodo_DeveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate inicio = LocalDate.now().minusDays(5);
        LocalDate fim = LocalDate.now().plusDays(5);
        Page<Transacao> pagina = new PageImpl<>(List.of(transacao));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndDataVencimentoBetween(usuario.getId(), inicio, fim, pageable))
                .thenReturn(pagina);

        Page<TransacaoDTO> resultado = transacaoService.listarPorPeriodo(inicio, fim, pageable);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getTotalElements());
    }

    /**
     * Testa listagem por período (paginada) no método
     * {@link TransacaoService#listarPorPeriodo(LocalDate, LocalDate, Pageable)}.
     * Cenário: A data inicial é posterior à data final.
     */
    @Test
    @DisplayName("listarPorPeriodo: Deve lançar RegraDeNegocioException quando data inicial for maior que final")
    void testeListarPorPeriodo_DataInicialMaior_DeveLancarExcecao() {
        Pageable pageable = PageRequest.of(0, 10);

        LocalDate inicio = LocalDate.now().plusDays(1);
        LocalDate fim = LocalDate.now();

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> transacaoService.listarPorPeriodo(inicio, fim, pageable));

        assertEquals("A data de início não pode ser posterior à data final.", exception.getMessage());

        verifyNoInteractions(contextoUsuarioService, transacaoRepository);
    }

    /**
     * Testa listagem por período (paginada) no método
     * {@link TransacaoService#listarPorPeriodo(LocalDate, LocalDate, Pageable)}.
     * Cenário: O período selecionado tem mais de 90 dias.
     */
    @Test
    @DisplayName("listarPorPeriodo: Deve lançar RegraDeNegocioException quando período for maior que 90 dias")
    void testeListarPorPeriodo_PeriodoMaiorQue90Dias_DeveLancarExcecao() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate inicio = LocalDate.now();

        LocalDate fim = LocalDate.now().plusDays(91);

        RegraDeNegocioException exception = assertThrows(
                RegraDeNegocioException.class,
                () -> transacaoService.listarPorPeriodo(inicio, fim, pageable));

        assertEquals("O período de consulta não pode ultrapassar 90 dias.", exception.getMessage());

        verifyNoInteractions(contextoUsuarioService, transacaoRepository);
    }

    /**
     * Testa listagem por período (paginada) no método
     * {@link TransacaoService#listarPorPeriodo(LocalDate, LocalDate, Pageable)}.
     * Cenário de Limite: O período tem exatamente 90 dias.
     */
    @Test
    @DisplayName("listarPorPeriodo: Deve retornar página quando período for exatamente 90 dias (Limite)")
    void testeListarPorPeriodo_PeriodoExatamente90Dias_DeveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate inicio = LocalDate.now();

        LocalDate fim = LocalDate.now().plusDays(90);
        Page<Transacao> pagina = new PageImpl<>(List.of(transacao));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndDataVencimentoBetween(usuario.getId(), inicio, fim, pageable))
                .thenReturn(pagina);

        Page<TransacaoDTO> resultado = transacaoService.listarPorPeriodo(inicio, fim, pageable);

        assertFalse(resultado.isEmpty());

        verify(transacaoRepository).findByUsuarioIdAndDataVencimentoBetween(usuario.getId(), inicio, fim, pageable);
    }

    /**
     * Testa busca por ID com sucesso no método
     * {@link TransacaoService#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando válida, deve retornar TransacaoDTO")
    void testeBuscarPorId_QuandoValido_DeveRetornarDTO() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));

        TransacaoDTO resultado = transacaoService.buscarPorId(transacao.getId());

        assertEquals(transacao.getDescricao(), resultado.descricao());
        assertEquals(transacao.getCategoria().getId(), resultado.categoriaId());
    }

    /**
     * Testa erro de integridade de banco na persistência da transação.
     */
    @Test
    @DisplayName("salvarEntidade: Quando erro de banco, deve lançar RegraDeNegocioException")
    void testeCadastrar_QuandoErroDeIntegridade_DeveLancarExcecao() {
        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Teste Constraint", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                categoria.getId(), conta.getId(), null);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaService.obterEntidadeValidada(any(), any())).thenReturn(categoria);
        when(contaService.obterEntidadeValidada(any(), any())).thenReturn(conta);

        when(transacaoRepository.saveAndFlush(any(Transacao.class)))
                .thenThrow(new DataIntegrityViolationException("Erro SQL no banco"));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> transacaoService.cadastrar(dto));
        assertTrue(excecao.getMessage().contains("Erro de integridade"));
    }

    /**
     * Testa atualização de relacionamentos com efetivação de Crédito no método
     * {@link TransacaoService#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("atualizar: Mudando categoria, conta, pessoa e efetivando (Receita Paga)")
    void testeAtualizar_MudandoRelacionamentosEEfetivando() {
        transacao.setStatus(StatusTransacao.PENDENTE);

        Categoria novaCat = new Categoria("Renda", TipoCategoria.RECEITA, "icon", "#fff", usuario);
        novaCat.setId(UUID.randomUUID());

        Conta novaConta = new Conta("Nova Conta", InstituicaoFinanceira.NUBANK, new BigDecimal("500.00"), pessoa);
        novaConta.setId(UUID.randomUUID());

        Pessoa novaPessoa = new Pessoa("Novo Cliente", usuario);
        novaPessoa.setId(UUID.randomUUID());

        CriarAtualizarTransacaoDTO dtoUpdate = new CriarAtualizarTransacaoDTO(
                "Novo Trampo", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), LocalDate.now(),
                TipoTransacao.RECEITA, StatusTransacao.PAGO, null,
                novaCat.getId(), novaConta.getId(), novaPessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));
        when(categoriaService.obterEntidadeValidada(usuario.getId(), novaCat.getId())).thenReturn(novaCat);
        when(contaService.obterEntidadeValidada(usuario.getId(), novaConta.getId())).thenReturn(novaConta);
        when(pessoaService.obterEntidadeValidada(usuario.getId(), novaPessoa.getId())).thenReturn(novaPessoa);
        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.atualizar(transacao.getId(), dtoUpdate);

        assertEquals(new BigDecimal("600.00"), novaConta.getSaldoAtual());
        assertEquals(novaCat.getId(), resultado.categoriaId());
        assertEquals(novaConta.getId(), resultado.contaId());
        assertEquals(novaPessoa.getId(), resultado.pessoaId());
    }

    /**
     * Testa estorno de Débito, remoção de pessoa e passagem para Pendente no método
     * {@link TransacaoService#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("atualizar: Estornando original (Despesa Paga), removendo pessoa e virando Pendente")
    void testeAtualizar_EstornandoMantendoRelacionamentosERemovendoPessoa() {

        transacao.setStatus(StatusTransacao.PAGO);
        conta.debitar(VALOR_TRANSACAO);

        CriarAtualizarTransacaoDTO dtoUpdate = new CriarAtualizarTransacaoDTO(
                "Atualizada", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                categoria.getId(), conta.getId(), null);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));
        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.atualizar(transacao.getId(), dtoUpdate);

        assertEquals(new BigDecimal("1000.00"), conta.getSaldoAtual());
        assertNull(resultado.pessoaId());

        verify(categoriaService, never()).obterEntidadeValidada(any(), any());
        verify(contaService, never()).obterEntidadeValidada(any(), any());
    }

    /**
     * Testa ramificação do IF onde a transação NÃO TINHA pessoa e passa a ter.
     * Cobre a condição: dto.pessoaId() != null && transacao.getPessoa() == null.
     */
    @Test
    @DisplayName("atualizar: Quando transação não tinha pessoa e DTO adiciona, deve buscar e vincular")
    void testeAtualizar_AdicionandoPessoaEmTransacaoSemPessoa() {
        transacao.setPessoa(null);
        UUID transacaoId = transacao.getId();
        UUID usuarioId = usuario.getId();

        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Adicionou Fornecedor", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                categoria.getId(), conta.getId(), pessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuarioId, transacaoId)).thenReturn(Optional.of(transacao));
        when(pessoaService.obterEntidadeValidada(usuarioId, pessoa.getId())).thenReturn(pessoa);
        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.atualizar(transacaoId, dto);

        assertNotNull(resultado.pessoaId());
        assertEquals(pessoa.getId(), resultado.pessoaId());
        verify(pessoaService).obterEntidadeValidada(usuarioId, pessoa.getId());
    }

    /**
     * Testa ramificação do IF onde a transação JÁ TINHA a mesma pessoa.
     * Cobre a condição onde IDs são iguais e a lógica pula o bloco IF otimizando
     * processamento.
     */
    @Test
    @DisplayName("atualizar: Quando DTO envia a mesma pessoa, não deve buscar novamente no banco")
    void testeAtualizar_MantendoAMesmaPessoa() {
        UUID transacaoId = transacao.getId();
        UUID usuarioId = usuario.getId();

        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Manteve Pessoa", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                categoria.getId(), conta.getId(), pessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuarioId, transacaoId)).thenReturn(Optional.of(transacao));
        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.atualizar(transacaoId, dto);

        assertEquals(pessoa.getId(), resultado.pessoaId());

        verify(pessoaService, never()).obterEntidadeValidada(any(), any());
    }

    /**
     * Testa exclusão estornando uma Receita Paga no método
     * {@link TransacaoService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando Receita Paga, deve estornar (debitar) da conta e remover")
    void testeExcluir_ReceitaPaga_DeveEstornar() {
        transacao.setTipo(TipoTransacao.RECEITA);
        transacao.setStatus(StatusTransacao.PAGO);
        conta.creditar(VALOR_TRANSACAO);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));

        assertDoesNotThrow(() -> transacaoService.excluir(transacao.getId()));

        assertEquals(new BigDecimal("1000.00"), conta.getSaldoAtual());
        verify(transacaoRepository).delete(transacao);
    }

    /**
     * Testa exclusão de Despesa Pendente sem estorno.
     */
    @Test
    @DisplayName("excluir: Quando Despesa Pendente, não deve estornar, apenas remover")
    void testeExcluir_Pendente_NaoDeveEstornar() {
        transacao.setStatus(StatusTransacao.PENDENTE);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));

        transacaoService.excluir(transacao.getId());

        assertEquals(new BigDecimal("1000.00"), conta.getSaldoAtual());
        verify(transacaoRepository).delete(transacao);
    }

    /**
     * Testa verificações booleanas de vínculos (Pessoa e Conta).
     */
    @Test
    @DisplayName("Verificações de vínculos: Devem repassar a chamada de validação para o repository")
    void testeVerificacoesVinculos() {
        UUID idAlvo = UUID.randomUUID();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);

        when(transacaoRepository.existsByUsuarioIdAndPessoaId(usuario.getId(), idAlvo)).thenReturn(true);
        assertTrue(transacaoService.existeTransacaoVinculadaAPessoa(idAlvo));

        when(transacaoRepository.existsByUsuarioIdAndContaId(usuario.getId(), idAlvo)).thenReturn(false);
        assertFalse(transacaoService.existeTransacaoVinculadaAConta(idAlvo));
    }

    /**
     * Testa blindagem da consulta no método privado de validação
     * (EntityNotFoundException).
     */
    @Test
    @DisplayName("obterEntidadeValidada: Quando transação não encontrada, deve lançar EntityNotFoundException")
    void testeObterEntidadeValidada_QuandoNaoEncontrada_LancaExcecao() {
        UUID idDesconhecido = UUID.randomUUID();
        UUID usuarioId = usuario.getId();

        when(transacaoRepository.findByUsuarioIdAndId(usuarioId, idDesconhecido))
                .thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> transacaoService.obterEntidadeValidada(usuarioId, idDesconhecido));

        assertTrue(excecao.getMessage().contains("não encontrada ou acesso negado"));
    }
}
