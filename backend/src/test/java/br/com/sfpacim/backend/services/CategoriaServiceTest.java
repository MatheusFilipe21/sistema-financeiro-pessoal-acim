package br.com.sfpacim.backend.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.FiltroCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.SelecaoCategoriaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.repositories.CategoriaRepository;

/**
 * Testes unitários para a classe {@link CategoriaService}.
 *
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas, validando
 * regras de negócio, bloqueios de alteração de categorias do sistema e
 * unicidade de dados.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

    @Mock
    private TransacaoService transacaoService;

    @InjectMocks
    private CategoriaService categoriaService;

    private static final String NOME_CATEGORIA = "Streaming";
    private static final String NOME_CATEGORIA_SISTEMA = "Alimentação";
    private static final TipoCategoria TIPO = TipoCategoria.DESPESA;
    private static final String ICONE = "tv";
    private static final String COR = "#0000FF";

    private Usuario usuario;
    private Categoria categoriaUsuario;
    private Categoria categoriaSistema;
    private CriarAtualizarCategoriaDTO criarAtualizarDTO;

    /**
     * Configura o cenário comum antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Usuario Teste", "teste@email.com", "senha123");
        usuario.setId(UUID.randomUUID());

        categoriaUsuario = new Categoria(NOME_CATEGORIA, TIPO, ICONE, COR, usuario);
        categoriaUsuario.setId(UUID.randomUUID());

        categoriaSistema = new Categoria(NOME_CATEGORIA_SISTEMA, TIPO, "restaurant", "#FF0000", null);
        categoriaSistema.setId(UUID.randomUUID());

        criarAtualizarDTO = new CriarAtualizarCategoriaDTO(NOME_CATEGORIA, TIPO, ICONE, COR);

        Mockito.lenient().when(messageSource.getMessage(anyString(), any(), any(), any()))
                .thenReturn("Mensagem Mockada");
    }

    /**
     * Testa o cenário de sucesso no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Deve salvar e retornar CategoriaDTO com sucesso")
    void testeCadastrar_ComSucesso() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.existeCategoriaDuplicada(usuario.getId(), NOME_CATEGORIA, null))
                .thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoriaUsuario);

        CategoriaDTO resultado = categoriaService.cadastrar(criarAtualizarDTO);

        assertNotNull(resultado);
        assertEquals(NOME_CATEGORIA, resultado.nome());
        verify(categoriaRepository).saveAndFlush(any(Categoria.class));
    }

    /**
     * Testa a validação proativa de unicidade de nome no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Deve lançar exceção quando o banco apontar nome duplicado (Proativo)")
    void testeCadastrar_NomeDuplicado_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.existeCategoriaDuplicada(usuario.getId(), NOME_CATEGORIA, null))
                .thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> categoriaService.cadastrar(criarAtualizarDTO));
        verify(categoriaRepository, never()).saveAndFlush(any(Categoria.class));
    }

    /**
     * Testa a ramificação do bloqueio reativo de banco de dados no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Deve lançar ViolacaoDadosException quando ocorrer DataIntegrityViolationException (Reativo)")
    void testeCadastrar_DataIntegrityViolation_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.existeCategoriaDuplicada(any(), any(), any())).thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        assertThrows(ViolacaoDadosException.class, () -> categoriaService.cadastrar(criarAtualizarDTO));
    }

    /**
     * Testa a listagem paginada e filtrada no método
     * {@link CategoriaService#listar(FiltroCategoriaDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar a página de categorias filtradas")
    @SuppressWarnings("unchecked")
    void testeListar_ComSucesso() {
        FiltroCategoriaDTO filtro = new FiltroCategoriaDTO(null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Categoria> pagina = new PageImpl<>(List.of(categoriaSistema, categoriaUsuario));

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<CategoriaDTO> resultado = categoriaService.listar(filtro, pageable);

        assertEquals(2, resultado.getContent().size());
    }

    /**
     * Testa a listagem de opções para seleção (Dropdowns) no método
     * {@link CategoriaService#listarOpcoes()}.
     */
    @Test
    @DisplayName("listarOpcoes: Deve retornar as opções simplificadas")
    void testeListarOpcoes_ComSucesso() {
        SelecaoCategoriaDTO selecao = new SelecaoCategoriaDTO(categoriaUsuario.getId(), NOME_CATEGORIA, TIPO, ICONE,
                COR);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarOpcoesParaSelecao(usuario.getId())).thenReturn(List.of(selecao));

        List<SelecaoCategoriaDTO> resultado = categoriaService.listarOpcoes();

        assertEquals(1, resultado.size());
        assertEquals(NOME_CATEGORIA, resultado.get(0).nome());
    }

    /**
     * Testa a busca pelo identificador no método
     * {@link CategoriaService#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Deve retornar a CategoriaDTO quando encontrada")
    void testeBuscarPorId_ComSucesso() {
        UUID id = categoriaUsuario.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));

        CategoriaDTO resultado = categoriaService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(categoriaUsuario.getNome(), resultado.nome());
    }

    /**
     * Testa a blindagem da consulta no método privado de validação
     * quando a entidade não pertence ao usuário ou não existe.
     */
    @Test
    @DisplayName("obterEntidadeValidada: Deve lançar EntityNotFoundException na falha do lambda orElseThrow")
    void testeObterEntidadeValidada_NaoEncontrada_DeveLancarExcecao() {
        UUID id = UUID.randomUUID();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoriaService.buscarPorId(id));
    }

    /**
     * Testa a atualização com sucesso no método
     * {@link CategoriaService#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Deve atualizar categoria do usuário com sucesso")
    void testeAtualizar_ComSucesso() {
        UUID id = categoriaUsuario.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));
        when(categoriaRepository.existeCategoriaDuplicada(usuario.getId(), NOME_CATEGORIA, id))
                .thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoriaUsuario);

        CategoriaDTO resultado = categoriaService.atualizar(id, criarAtualizarDTO);

        assertNotNull(resultado);
        verify(categoriaRepository).saveAndFlush(categoriaUsuario);
    }

    /**
     * Testa bloqueio de edição em categorias padrão no método
     * {@link CategoriaService#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Deve lançar exceção se tentar alterar categoria do sistema")
    void testeAtualizar_CategoriaSistema_DeveLancarExcecao() {
        UUID id = categoriaSistema.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaSistema));

        assertThrows(RegraDeNegocioException.class, () -> categoriaService.atualizar(id, criarAtualizarDTO));
        verify(categoriaRepository, never()).saveAndFlush(any(Categoria.class));
    }

    /**
     * Testa exclusão com sucesso no método
     * {@link CategoriaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Deve remover a categoria com sucesso")
    void testeExcluir_ComSucesso() {
        UUID id = categoriaUsuario.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));
        when(transacaoService.existeTransacaoVinculadaACategoria(id)).thenReturn(false);

        assertDoesNotThrow(() -> categoriaService.excluir(id));
        verify(categoriaRepository).delete(categoriaUsuario);
    }

    /**
     * Testa bloqueio de exclusão em categorias padrão no método
     * {@link CategoriaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Deve lançar exceção se tentar excluir categoria do sistema")
    void testeExcluir_CategoriaSistema_DeveLancarExcecao() {
        UUID id = categoriaSistema.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaSistema));

        assertThrows(RegraDeNegocioException.class, () -> categoriaService.excluir(id));
        verify(transacaoService, never()).existeTransacaoVinculadaACategoria(any());
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    /**
     * Testa validação de dependências no método
     * {@link CategoriaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Deve lançar ViolacaoDadosException quando possuir transações vinculadas")
    void testeExcluir_ComTransacoesVinculadas_DeveLancarExcecao() {
        UUID id = categoriaUsuario.getId();
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));
        when(transacaoService.existeTransacaoVinculadaACategoria(id)).thenReturn(true);

        assertThrows(ViolacaoDadosException.class, () -> categoriaService.excluir(id));
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }
}
