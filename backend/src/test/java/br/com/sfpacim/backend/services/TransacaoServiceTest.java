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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.FiltroTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.ListagemTransacaoDTO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private MessageSource messageSource;

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

        Answer<String> answerMensagemDinamica = invocation -> {
            String codigo = invocation.getArgument(0);
            if ("erro.filtro.data.invalida".equals(codigo))
                return "A data de início não pode ser posterior à data final.";
            if ("erro.transacao.integridade.salvar".equals(codigo))
                return "Erro de integridade ao salvar transação.";
            if ("erro.recurso.nao-encontrado".equals(codigo))
                return "Transação não encontrada ou acesso negado.";
            if ("transacao.nome.singular".equals(codigo))
                return "Transação";
            return "Mensagem Mockada";
        };

        Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(answerMensagemDinamica);
        Mockito.lenient().when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenAnswer(answerMensagemDinamica);
    }

    /**
     * Testa cadastro de Despesa Paga no método
     * {@link TransacaoService#cadastrar(CriarAtualizarTransacaoDTO)}.
     * 
     * <p>
     * Cobre efetivação de Débito.
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
     * Testa cadastro de Receita Paga no método
     * {@link TransacaoService#cadastrar(CriarAtualizarTransacaoDTO)}.
     * 
     * <p>
     * Cobre efetivação de Crédito.
     */
    @Test
    @DisplayName("cadastrar: Quando Receita PAGA, deve efetivar crédito na conta e salvar")
    void testeCadastrar_QuandoReceitaPaga_DeveCreditarESalvar() {
        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Salário", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), LocalDate.now(),
                TipoTransacao.RECEITA, StatusTransacao.PAGO, null,
                categoria.getId(), conta.getId(), pessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaService.obterEntidadeValidada(usuario.getId(), dto.categoriaId())).thenReturn(categoria);
        when(contaService.obterEntidadeValidada(usuario.getId(), dto.contaId())).thenReturn(conta);
        when(pessoaService.obterEntidadeValidada(usuario.getId(), dto.pessoaId())).thenReturn(pessoa);

        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.cadastrar(dto);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("1100.00"), conta.getSaldoAtual());
        verify(transacaoRepository).saveAndFlush(any(Transacao.class));
    }

    /**
     * Testa cadastro de Receita Pendente sem Pessoa no método
     * {@link TransacaoService#cadastrar(CriarAtualizarTransacaoDTO)}.
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
     * Testa erro de integridade de banco na persistência da transação no método
     * {@link TransacaoService#cadastrar(CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando erro de banco, deve lançar RegraDeNegocioException")
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

        assertEquals("Erro de integridade ao salvar transação.", excecao.getMessage());
    }

    /**
     * Testa listagem paginada no método
     * {@link TransacaoService#listar(FiltroTransacaoDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar página de ListagemTransacaoDTO")
    @SuppressWarnings("unchecked")
    void testeListar_DeveRetornarPagina() {
        FiltroTransacaoDTO filtro = new FiltroTransacaoDTO(
                null, null, null, LocalDate.now(), LocalDate.now().plusDays(5), null, null,
                null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transacao> pagina = new PageImpl<>(List.of(transacao));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<ListagemTransacaoDTO> resultado = transacaoService.listar(filtro, pageable);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getTotalElements());
    }

    /**
     * Testa validação de datas na listagem no método
     * {@link TransacaoService#listar(FiltroTransacaoDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve lançar RegraDeNegocioException quando data inicial for maior que final")
    void testeListar_DataInicialMaior_DeveLancarExcecao() {
        FiltroTransacaoDTO filtro = new FiltroTransacaoDTO(
                null, null, null, LocalDate.now().plusDays(1), LocalDate.now(), null, null,
                null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class,
                () -> transacaoService.listar(filtro, pageable));

        assertEquals("A data de início não pode ser posterior à data final.", exception.getMessage());
        verifyNoInteractions(contextoUsuarioService, transacaoRepository);
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
     * Testa estorno de Crédito, com transação mantendo a mesma pessoa no método
     * {@link TransacaoService#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("atualizar: Estornando original (Receita Paga), mantendo a mesma pessoa")
    void testeAtualizar_EstornandoReceitaMantendoAMesmaPessoa() {
        transacao.setTipo(TipoTransacao.RECEITA);
        transacao.setStatus(StatusTransacao.PAGO);
        conta.creditar(VALOR_TRANSACAO);

        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                "Manteve Pessoa", VALOR_TRANSACAO, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.RECEITA, StatusTransacao.PENDENTE, null,
                categoria.getId(), conta.getId(), pessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));
        when(transacaoRepository.saveAndFlush(any(Transacao.class))).thenAnswer(i -> i.getArguments()[0]);

        TransacaoDTO resultado = transacaoService.atualizar(transacao.getId(), dto);

        assertEquals(new BigDecimal("1000.00"), conta.getSaldoAtual());
        assertEquals(pessoa.getId(), resultado.pessoaId());

        verify(pessoaService, never()).obterEntidadeValidada(any(), any());
    }

    /**
     * Testa ramificação do IF onde a transação NÃO TINHA pessoa e passa a ter no
     * método {@link TransacaoService#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
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
     * Testa exclusão estornando uma Despesa Paga no método
     * {@link TransacaoService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando Despesa Paga, deve estornar (creditar) na conta e remover")
    void testeExcluir_DespesaPaga_DeveEstornar() {
        transacao.setTipo(TipoTransacao.DESPESA);
        transacao.setStatus(StatusTransacao.PAGO);
        conta.debitar(VALOR_TRANSACAO);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId()))
                .thenReturn(Optional.of(transacao));

        assertDoesNotThrow(() -> transacaoService.excluir(transacao.getId()));

        assertEquals(new BigDecimal("1000.00"), conta.getSaldoAtual());
        verify(transacaoRepository).delete(transacao);
    }

    /**
     * Testa exclusão de Despesa Pendente sem estorno no método
     * {@link TransacaoService#excluir(UUID)}.
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
     * Testa verificações booleanas de vínculos (Pessoa, Conta e Categoria)
     * delegando para o repositório.
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

        when(transacaoRepository.existsByUsuarioIdAndCategoriaId(usuario.getId(), idAlvo)).thenReturn(true);
        assertTrue(transacaoService.existeTransacaoVinculadaACategoria(idAlvo));
    }

    /**
     * Testa blindagem da consulta no método privado de validação
     * {@link TransacaoService#obterEntidadeValidada(UUID, UUID)}.
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

    /**
     * Testa validação de datas na listagem no método
     * {@link TransacaoService#listar(FiltroTransacaoDTO, Pageable)}.
     * Cenário: Data inicial é nula (cobre short-circuit do inicio != null).
     */
    @Test
    @DisplayName("listar: Quando data inicial for nula, não deve validar coerência e deve listar com sucesso")
    @SuppressWarnings("unchecked")
    void testeListar_DataInicialNula_DevePassarSemLancarExcecao() {
        FiltroTransacaoDTO filtro = new FiltroTransacaoDTO(
                null, null, null, null, LocalDate.now(), null, null,
                null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transacao> pagina = new PageImpl<>(List.of(transacao));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        assertDoesNotThrow(() -> transacaoService.listar(filtro, pageable));
    }

    /**
     * Testa validação de datas na listagem no método
     * {@link TransacaoService#listar(FiltroTransacaoDTO, Pageable)}.
     * Cenário: Data final é nula (cobre short-circuit do fim != null).
     */
    @Test
    @DisplayName("listar: Quando data final for nula, não deve validar coerência e deve listar com sucesso")
    @SuppressWarnings("unchecked")
    void testeListar_DataFinalNula_DevePassarSemLancarExcecao() {
        FiltroTransacaoDTO filtro = new FiltroTransacaoDTO(
                null, null, null, LocalDate.now(), null, null, null,
                null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transacao> pagina = new PageImpl<>(List.of(transacao));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(transacaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        assertDoesNotThrow(() -> transacaoService.listar(filtro, pageable));
    }
}
