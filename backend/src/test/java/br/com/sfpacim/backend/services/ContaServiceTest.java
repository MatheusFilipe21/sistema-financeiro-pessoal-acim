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
import br.com.sfpacim.backend.repositories.PessoaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe {@link ContaService}.
 *
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas
 * (Repositories e ContextoUsuarioService).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private PessoaRepository pessoaRepository;

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
    private Pessoa pessoaDependente;
    private Conta conta;
    private CriarAtualizarContaDTO criarAtualizarContaDTO;

    /**
     * Configura o cenário comum antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Usuario Teste", "teste@email.com", "senha123");
        usuario.setId(UUID.randomUUID());

        pessoaTitular = new Pessoa("Titular", usuario);
        pessoaTitular.setId(UUID.randomUUID());
        pessoaTitular.setTitular(true);

        pessoaDependente = new Pessoa("Dependente", usuario);
        pessoaDependente.setId(UUID.randomUUID());
        pessoaDependente.setTitular(false);

        conta = new Conta(NOME_CONTA, INSTITUICAO, SALDO_INICIAL, pessoaTitular);
        conta.setId(UUID.randomUUID());

        criarAtualizarContaDTO = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, SALDO_INICIAL,
                pessoaTitular.getId());
    }

    /**
     * Testa o método {@link ContaService#cadastrar(CriarAtualizarContaDTO)}.
     * Valida o cenário de sucesso.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve vincular à pessoa e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarConta() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoaTitular.getId())).thenReturn(Optional.of(pessoaTitular));
        when(contaRepository.findByPessoa(pessoaTitular)).thenReturn(Collections.emptyList());
        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.cadastrar(criarAtualizarContaDTO);

        assertNotNull(resultado, "O DTO retornado não deve ser nulo");
        assertEquals(NOME_CONTA, resultado.nome());
        assertEquals(SALDO_INICIAL, resultado.saldoAtual());

        verify(contaRepository).saveAndFlush(any(Conta.class));
    }

    /**
     * Testa o cadastro tentando vincular a uma pessoa que NÃO é titular.
     * Deve lançar ViolacaoDadosException.
     */
    @Test
    @DisplayName("cadastrar: Quando pessoa não é titular, deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoPessoaNaoTitular_DeveLancarExcecao() {
        CriarAtualizarContaDTO dtoDependente = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, SALDO_INICIAL,
                pessoaDependente.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoaDependente.getId())).thenReturn(Optional.of(pessoaDependente));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.cadastrar(dtoDependente));

        assertTrue(excecao.getMessage().contains("não é um titular habilitado"),
                "A mensagem deve informar sobre a titularidade");

        verify(contaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o cadastro com nome duplicado para a MESMA pessoa.
     * Deve lançar ViolacaoDadosException.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado para a pessoa, deve lançar exceção")
    void testeCadastrar_QuandoNomeDuplicado_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoaTitular.getId())).thenReturn(Optional.of(pessoaTitular));
        when(contaRepository.findByPessoa(pessoaTitular)).thenReturn(List.of(conta));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.cadastrar(criarAtualizarContaDTO));

        assertTrue(excecao.getMessage().contains("Já existe uma conta"),
                "A mensagem deve indicar duplicidade de nome");

        verify(contaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o método {@link ContaService#listar()}.
     * Deve retornar as contas de todas as pessoas do usuário.
     */
    @Test
    @DisplayName("listar: Deve retornar todas as contas das pessoas do usuário")
    void testeListar_DeveRetornarContas() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(pessoaTitular));
        when(contaRepository.findByPessoa(pessoaTitular)).thenReturn(List.of(conta));

        List<ContaDTO> resultado = contaService.listar();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(NOME_CONTA, resultado.get(0).nome());
    }

    /**
     * Testa o método {@link ContaService#atualizar}.
     * Valida atualização simples (sem mudança de saldo inicial).
     */
    @Test
    @DisplayName("atualizar: Quando válido, deve atualizar os dados")
    void testeAtualizar_QuandoValido_DeveAtualizar() {
        CriarAtualizarContaDTO dtoNovo = new CriarAtualizarContaDTO(NOME_CONTA_NOVO, INSTITUICAO, SALDO_INICIAL,
                pessoaTitular.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(contaRepository.findByPessoa(pessoaTitular)).thenReturn(List.of(conta));
        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.atualizar(conta.getId(), dtoNovo);

        assertEquals(NOME_CONTA_NOVO, resultado.nome());
        assertEquals(NOME_CONTA_NOVO, conta.getNome());
    }

    /**
     * Testa a lógica de recálculo de saldo (Delta).
     * Cenário: Saldo Inicial aumenta em 100.00 -> Saldo Atual deve aumentar em
     * 100.00.
     */
    @Test
    @DisplayName("atualizar: Quando saldo inicial muda, deve recalcular saldo atual (Delta)")
    void testeAtualizar_QuandoSaldoInicialMuda_DeveRecalcularSaldoAtual() {

        conta.setSaldoAtual(new BigDecimal("800.00"));

        BigDecimal novoSaldoInicial = new BigDecimal("1100.00");
        CriarAtualizarContaDTO dtoSaldoAlterado = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, novoSaldoInicial,
                pessoaTitular.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(contaRepository.findByPessoa(pessoaTitular)).thenReturn(List.of(conta));
        when(contaRepository.saveAndFlush(any(Conta.class))).thenAnswer(i -> i.getArguments()[0]);

        contaService.atualizar(conta.getId(), dtoSaldoAlterado);

        assertEquals(new BigDecimal("900.00"), conta.getSaldoAtual(),
                "O saldo atual deveria ter sido recalculado com a diferença");
        assertEquals(novoSaldoInicial, conta.getSaldoInicial());
    }

    /**
     * Testa tentativa de atualizar conta de outro usuário.
     */
    @Test
    @DisplayName("atualizar: Quando conta pertence a outro usuário, deve lançar EntityNotFoundException")
    void testeAtualizar_QuandoOutroUsuario_DeveLancarExcecao() {
        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        Pessoa pessoaIntrusa = new Pessoa("Pessoa Intrusa", intruso);
        Conta contaIntrusa = new Conta("Conta Intrusa", INSTITUICAO, SALDO_INICIAL, pessoaIntrusa);
        contaIntrusa.setId(UUID.randomUUID());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findById(contaIntrusa.getId())).thenReturn(Optional.of(contaIntrusa));

        UUID idContaIntrusa = contaIntrusa.getId();

        assertThrows(EntityNotFoundException.class,
                () -> contaService.atualizar(idContaIntrusa, criarAtualizarContaDTO));

        verify(contaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o método {@link ContaService#excluir}.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve remover a conta")
    void testeExcluir_QuandoValido_DeveDeletar() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));

        contaService.excluir(conta.getId());

        verify(contaRepository).delete(conta);
    }

    /**
     * Testa o bloco catch(DataIntegrityViolationException) no método
     * salvarEntidade.
     * 
     * <p>
     * Simula uma "Race Condition": A validação em memória passa (lista vazia),
     * mas o banco rejeita o insert por duplicidade no exato momento da gravação.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lança DataIntegrityViolation, deve converter para ViolacaoDadosException")
    void testeCadastrar_QuandoErroBanco_DeveLancarExcecaoNegocio() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoaTitular.getId())).thenReturn(Optional.of(pessoaTitular));

        when(contaRepository.findByPessoa(pessoaTitular)).thenReturn(Collections.emptyList());

        when(contaRepository.saveAndFlush(any(Conta.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> contaService.cadastrar(criarAtualizarContaDTO));

        assertTrue(excecao.getMessage().contains("Já existe uma conta"),
                "A exceção do banco deve ser traduzida para uma mensagem amigável");
    }

    /**
     * Testa o bloco IF de transferência de titularidade no método atualizar.
     *
     * <p>
     * Cenário: O usuário decide mover a conta de "Titular" para "Outra Pessoa"
     * (ambas dele).
     * Deve validar a nova pessoa e atualizar a referência.
     */
    @Test
    @DisplayName("atualizar: Quando troca de pessoa, deve validar nova pessoa e atualizar vínculo")
    void testeAtualizar_QuandoTrocaTitular_DeveAtualizarPessoa() {
        Pessoa novaPessoa = new Pessoa("Novo Dono", usuario);
        novaPessoa.setId(UUID.randomUUID());
        novaPessoa.setTitular(true);

        CriarAtualizarContaDTO dtoTransferencia = new CriarAtualizarContaDTO(NOME_CONTA, INSTITUICAO, SALDO_INICIAL,
                novaPessoa.getId());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(contaRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(pessoaRepository.findById(novaPessoa.getId())).thenReturn(Optional.of(novaPessoa));
        when(contaRepository.findByPessoa(novaPessoa)).thenReturn(Collections.emptyList());
        when(contaRepository.saveAndFlush(any(Conta.class))).thenReturn(conta);

        ContaDTO resultado = contaService.atualizar(conta.getId(), dtoTransferencia);

        assertNotNull(resultado, "O resultado não deve ser nulo");
        assertEquals(novaPessoa.getId(), resultado.pessoa().id(), "O DTO deve conter o ID da nova pessoa");
        assertEquals("Novo Dono", resultado.pessoa().nome(), "O DTO deve conter o nome da nova pessoa");

        verify(pessoaRepository).findById(novaPessoa.getId());
    }

    /**
     * Testa o throw do método privado buscarPessoaDoUsuario via cadastrar.
     * <p>
     * Cenário: Tenta cadastrar conta para uma pessoa que não existe no banco
     * ou pertence a outro usuário.
     */
    @Test
    @DisplayName("cadastrar: Quando pessoa não encontrada (buscarPessoaDoUsuario), deve lançar EntityNotFoundException")
    void testeCadastrar_QuandoPessoaNaoExiste_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findById(pessoaTitular.getId())).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> contaService.cadastrar(criarAtualizarContaDTO));

        assertTrue(excecao.getMessage().contains("não encontrada ou acesso negado"));

        verify(contaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa a ordenação composta da listagem de contas.
     * Critério 1: Nome do Titular (Pessoa)
     * Critério 2: Nome da Conta
     */
    @Test
    @DisplayName("listar: Deve ordenar por Nome do Titular e depois por Nome da Conta")
    void testeListar_DeveRetornarOrdenadoPorTitularEConta() {
        usuario = new Usuario("User", "user@email.com", "123");

        Pessoa bruno = new Pessoa("Bruno", usuario);
        bruno.setId(UUID.randomUUID());

        Pessoa ana = new Pessoa("Ana", usuario);
        ana.setId(UUID.randomUUID());

        Conta contaBruno = new Conta("Conta Itaú", InstituicaoFinanceira.ITAU, BigDecimal.ZERO, bruno);

        Conta contaAnaNubank = new Conta("Conta Nubank", InstituicaoFinanceira.NUBANK, BigDecimal.ZERO, ana);
        Conta contaAnaInter = new Conta("Conta Inter", InstituicaoFinanceira.INTER, BigDecimal.ZERO, ana);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);

        when(pessoaRepository.findByUsuario(usuario)).thenReturn(List.of(bruno, ana));

        when(contaRepository.findByPessoa(bruno)).thenReturn(List.of(contaBruno));

        when(contaRepository.findByPessoa(ana)).thenReturn(List.of(contaAnaNubank, contaAnaInter));

        List<ContaDTO> resultado = contaService.listar();

        assertEquals(3, resultado.size());

        assertEquals("Ana", resultado.get(0).pessoa().nome());
        assertEquals("Conta Inter", resultado.get(0).nome());

        assertEquals("Ana", resultado.get(1).pessoa().nome());
        assertEquals("Conta Nubank", resultado.get(1).nome());

        assertEquals("Bruno", resultado.get(2).pessoa().nome());
        assertEquals("Conta Itaú", resultado.get(2).nome());
    }
}
