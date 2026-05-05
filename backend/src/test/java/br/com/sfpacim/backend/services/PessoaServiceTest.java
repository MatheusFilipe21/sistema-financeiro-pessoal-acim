package br.com.sfpacim.backend.services;

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

import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.FiltroPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.SelecaoPessoaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;

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
    private MessageSource messageSource;

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
     * padroniza as respostas do MessageSource através de uma Answer dinâmica.
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

        Answer<String> answerMensagemDinamica = invocation -> {
            String codigo = invocation.getArgument(0);
            if (codigo.contains("erro.unicidade"))
                return "Erro de Unicidade";
            if (codigo.contains("erro.recurso.nao-encontrado"))
                return "Não encontrado";
            if (codigo.contains("erro.pessoa.titular.invalido"))
                return "Titular inválido";
            return "Mensagem Mockada";
        };

        Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(answerMensagemDinamica);
        Mockito.lenient().when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenAnswer(answerMensagemDinamica);
    }

    /**
     * Testa o cenário de sucesso no método
     * {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve vincular ao usuário e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarPessoa() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.existeNomeDuplicado(usuario.getId(), NOME, null)).thenReturn(false);
        when(pessoaRepository.saveAndFlush(any(Pessoa.class))).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.cadastrar(criarAtualizarPessoaDTO);

        assertNotNull(resultado);
        assertEquals(NOME, resultado.nome());
        verify(pessoaRepository).saveAndFlush(any(Pessoa.class));
    }

    /**
     * Testa a validação proativa de unicidade no método
     * {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome já existe no banco, deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicado_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.existeNomeDuplicado(usuario.getId(), NOME, null)).thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> pessoaService.cadastrar(criarAtualizarPessoaDTO));
        verify(pessoaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa a captura reativa de erro de integridade no método
     * {@link PessoaService#cadastrar(CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando ocorrer erro de integridade no banco, deve lançar ViolacaoDadosException")
    void testeCadastrar_ErroIntegridade_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.existeNomeDuplicado(any(), any(), any())).thenReturn(false);
        when(pessoaRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("DB Error"));

        assertThrows(ViolacaoDadosException.class, () -> pessoaService.cadastrar(criarAtualizarPessoaDTO));
    }

    /**
     * Testa a listagem paginada no método
     * {@link PessoaService#listar(FiltroPessoaDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar página de pessoas filtradas")
    @SuppressWarnings("unchecked")
    void testeListar_DeveRetornarPagina() {
        FiltroPessoaDTO filtro = new FiltroPessoaDTO(null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pessoa> pagina = new PageImpl<>(List.of(pessoa));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<PessoaDTO> resultado = pessoaService.listar(filtro, pageable);

        assertEquals(1, resultado.getContent().size());
        assertEquals(NOME, resultado.getContent().get(0).nome());
    }

    /**
     * Testa a busca de opções simplificadas no método
     * {@link PessoaService#listarOpcoes(Boolean)}.
     */
    @Test
    @DisplayName("listarOpcoes: Deve retornar lista simplificada para seleção")
    void testeListarOpcoes_DeveRetornarLista() {
        SelecaoPessoaDTO selecao = new SelecaoPessoaDTO(pessoa.getId(), NOME);
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.buscarOpcoesParaSelecao(usuario.getId(), true)).thenReturn(List.of(selecao));

        List<SelecaoPessoaDTO> resultado = pessoaService.listarOpcoes(true);

        assertEquals(1, resultado.size());
        assertEquals(NOME, resultado.get(0).nome());
    }

    /**
     * Testa a busca por ID no método {@link PessoaService#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Deve retornar DTO quando encontrado")
    void testeBuscarPorId_ComSucesso() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoa.getId())).thenReturn(Optional.of(pessoa));

        PessoaDTO resultado = pessoaService.buscarPorId(pessoa.getId());

        assertNotNull(resultado);
        assertEquals(NOME, resultado.nome());
    }

    /**
     * Testa a atualização com sucesso no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando dados válidos, deve alterar dados e salvar")
    void testeAtualizar_QuandoValido_DeveSalvar() {
        CriarAtualizarPessoaDTO dto = new CriarAtualizarPessoaDTO(NOME_NOVO, TITULAR_NOVO);
        UUID id = pessoa.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), id)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existeNomeDuplicado(usuario.getId(), NOME_NOVO, id)).thenReturn(false);
        when(pessoaRepository.saveAndFlush(any())).thenReturn(pessoa);

        PessoaDTO resultado = pessoaService.atualizar(id, dto);

        assertEquals(NOME_NOVO, resultado.nome());
        assertEquals(TITULAR_NOVO, resultado.titular());
    }

    /**
     * Testa a mesclagem quando o titular vem nulo no DTO no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando titular for nulo, deve preservar o valor original")
    void testeAtualizar_QuandoTitularNulo_DeveManterOriginal() {
        CriarAtualizarPessoaDTO dto = new CriarAtualizarPessoaDTO(NOME_NOVO, null);
        UUID id = pessoa.getId();
        pessoa.setTitular(true);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), id)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existeNomeDuplicado(usuario.getId(), NOME_NOVO, id)).thenReturn(false);
        when(pessoaRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        PessoaDTO resultado = pessoaService.atualizar(id, dto);

        assertEquals(NOME_NOVO, resultado.nome());
        assertTrue(resultado.titular());
    }

    /**
     * Testa falha de acesso no método
     * {@link PessoaService#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando ID não pertencer ao usuário, deve lançar EntityNotFoundException")
    void testeAtualizar_AcessoNegado_DeveLancarExcecao() {
        UUID idInvalido = UUID.randomUUID();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(any(), any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> pessoaService.atualizar(idInvalido, criarAtualizarPessoaDTO));
    }

    /**
     * Testa exclusão com sucesso no método {@link PessoaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando não possuir vínculos, deve remover")
    void testeExcluir_ComSucesso() {
        UUID id = pessoa.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), id)).thenReturn(Optional.of(pessoa));
        when(contaService.existeContaVinculadaAPessoa(id)).thenReturn(false);
        when(transacaoService.existeTransacaoVinculadaAPessoa(id)).thenReturn(false);

        assertDoesNotThrow(() -> pessoaService.excluir(id));
        verify(pessoaRepository).delete(pessoa);
    }

    /**
     * Testa bloqueio por contas vinculadas na exclusão no método
     * {@link PessoaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando possuir contas, deve lançar ViolacaoDadosException")
    void testeExcluir_ComContas_DeveLancarExcecao() {
        UUID id = pessoa.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), id)).thenReturn(Optional.of(pessoa));
        when(contaService.existeContaVinculadaAPessoa(id)).thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> pessoaService.excluir(id));
    }

    /**
     * Testa a falha de titularidade no método
     * {@link PessoaService#validarTitularidade(Pessoa)}.
     */
    @Test
    @DisplayName("validarTitularidade: Quando não for titular, deve lançar RegraDeNegocioException")
    void testeValidarTitularidade_DeveLancarExcecao() {
        pessoa.setTitular(false);
        assertThrows(RegraDeNegocioException.class, () -> pessoaService.validarTitularidade(pessoa));
    }

    /**
     * Testa o sucesso de titularidade no método
     * {@link PessoaService#validarTitularidade(Pessoa)}.
     */
    @Test
    @DisplayName("validarTitularidade: Quando for titular, não deve lançar exceção")
    void testeValidarTitularidade_Sucesso() {
        pessoa.setTitular(true);
        assertDoesNotThrow(() -> pessoaService.validarTitularidade(pessoa));
    }

    /**
     * Testa o bloqueio de exclusão especificamente por dependência de transações
     * no método {@link PessoaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando possuir apenas Transações vinculadas, deve lançar ViolacaoDadosException")
    void testeExcluir_QuandoPossuiApenasTransacoes_DeveLancarExcecao() {
        UUID id = pessoa.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(pessoaRepository.findByUsuarioIdAndId(usuario.getId(), id)).thenReturn(Optional.of(pessoa));

        when(contaService.existeContaVinculadaAPessoa(id)).thenReturn(false);
        when(transacaoService.existeTransacaoVinculadaAPessoa(id)).thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> pessoaService.excluir(id));

        verify(pessoaRepository, never()).delete(any(Pessoa.class));
    }
}
