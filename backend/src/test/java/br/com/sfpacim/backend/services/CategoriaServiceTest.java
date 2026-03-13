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

import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.repositories.CategoriaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe {@link CategoriaService}.
 *
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas
 * (Repositories e ContextoUsuarioService).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

    @InjectMocks
    private CategoriaService categoriaService;

    private static final String NOME_CATEGORIA = "Alimentação";
    private static final String NOME_CATEGORIA_NOVO = "Gastronomia";
    private static final TipoCategoria TIPO = TipoCategoria.DESPESA;
    private static final String ICONE = "restaurant";
    private static final String COR = "#FF0000";

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

        categoriaSistema = new Categoria("Salário", TipoCategoria.RECEITA, "money", "#00FF00", null);
        categoriaSistema.setId(UUID.randomUUID());

        criarAtualizarDTO = new CriarAtualizarCategoriaDTO(NOME_CATEGORIA, TIPO, ICONE, COR);
    }

    /**
     * Testa o método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     * Valida o cenário de sucesso.
     */
    @Test
    @DisplayName("cadastrar: quando dados válidos, deve vincular ao usuário e salvar")
    void testeCadastrar_QuandoDadosValidos_DeveSalvarCategoria() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.existsByNomeAndUsuarioConflitoCadastro(NOME_CATEGORIA, usuario)).thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoriaUsuario);

        CategoriaDTO resultado = categoriaService.cadastrar(criarAtualizarDTO);

        assertNotNull(resultado, "O dto retornado não deve ser nulo");
        assertEquals(NOME_CATEGORIA, resultado.nome());
        assertEquals(TIPO, resultado.tipo());

        verify(categoriaRepository).saveAndFlush(any(Categoria.class));
    }

    /**
     * Testa o cadastro com nome duplicado.
     * Deve lançar ViolacaoDadosException.
     */
    @Test
    @DisplayName("cadastrar: quando nome duplicado, deve lançar exceção")
    void testeCadastrar_QuandoNomeDuplicado_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.existsByNomeAndUsuarioConflitoCadastro(NOME_CATEGORIA, usuario)).thenReturn(true);

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> categoriaService.cadastrar(criarAtualizarDTO));

        assertTrue(excecao.getMessage().contains("Já existe uma categoria"),
                "A mensagem deve indicar duplicidade de nome");

        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o bloco catch(DataIntegrityViolationException) no método
     * salvarEntidade.
     *
     * <p>
     * Simula erro de constraint no banco de dados.
     */
    @Test
    @DisplayName("cadastrar: quando erro banco, deve converter para ViolacaoDadosException")
    void testeCadastrar_QuandoErroBanco_DeveLancarExcecaoNegocio() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.existsByNomeAndUsuarioConflitoCadastro(NOME_CATEGORIA, usuario)).thenReturn(false);

        when(categoriaRepository.saveAndFlush(any(Categoria.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> categoriaService.cadastrar(criarAtualizarDTO));

        assertTrue(excecao.getMessage().contains("Já existe uma categoria"),
                "A exceção do banco deve ser traduzida para uma mensagem amigável");
    }

    /**
     * Testa o método {@link CategoriaService#listar()}.
     * Deve retornar as categorias do usuário e do sistema.
     */
    @Test
    @DisplayName("listar: deve retornar categorias visíveis para o usuário")
    void testeListar_DeveRetornarCategorias() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioOrUsuarioIsNullOrderByNomeAsc(usuario))
                .thenReturn(List.of(categoriaSistema, categoriaUsuario));

        List<CategoriaDTO> resultado = categoriaService.listar();

        assertFalse(resultado.isEmpty());
        assertEquals(2, resultado.size());
        assertEquals("Salário", resultado.get(0).nome());
        assertEquals(NOME_CATEGORIA, resultado.get(1).nome());
    }

    /**
     * Testa o método {@link CategoriaService#atualizar}.
     * Valida atualização de uma categoria do usuário.
     */
    @Test
    @DisplayName("atualizar: quando categoria do usuário, deve atualizar os dados")
    void testeAtualizar_QuandoCategoriaUsuario_DeveAtualizar() {
        CriarAtualizarCategoriaDTO dtoNovo = new CriarAtualizarCategoriaDTO(NOME_CATEGORIA_NOVO, TIPO, ICONE, COR);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaUsuario.getId())).thenReturn(Optional.of(categoriaUsuario));
        when(categoriaRepository.existsByNomeAndUsuarioConflito(NOME_CATEGORIA_NOVO, usuario, categoriaUsuario.getId()))
                .thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoriaUsuario);

        CategoriaDTO resultado = categoriaService.atualizar(categoriaUsuario.getId(), dtoNovo);

        assertEquals(NOME_CATEGORIA_NOVO, resultado.nome());
        assertEquals(NOME_CATEGORIA_NOVO, categoriaUsuario.getNome());
    }

    /**
     * Testa tentativa de atualizar uma categoria do sistema.
     * Deve lançar RegraDeNegocioException.
     */
    @Test
    @DisplayName("atualizar: quando categoria do sistema, deve lançar RegraDeNegocioException")
    void testeAtualizar_QuandoCategoriaSistema_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaSistema.getId())).thenReturn(Optional.of(categoriaSistema));

        RegraDeNegocioException excecao = assertThrowsExactly(RegraDeNegocioException.class,
                () -> categoriaService.atualizar(categoriaSistema.getId(), criarAtualizarDTO));

        assertTrue(excecao.getMessage().contains("não podem ser alteradas"),
                "A mensagem deve informar sobre a imutabilidade do sistema");

        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa atualização com nome duplicado para outra categoria existente.
     */
    @Test
    @DisplayName("atualizar: quando nome duplicado na edição, deve lançar exceção")
    void testeAtualizar_QuandoNomeDuplicado_DeveLancarExcecao() {
        CriarAtualizarCategoriaDTO dtoDuplicado = new CriarAtualizarCategoriaDTO("Outra", TIPO, ICONE, COR);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaUsuario.getId())).thenReturn(Optional.of(categoriaUsuario));
        when(categoriaRepository.existsByNomeAndUsuarioConflito("Outra", usuario, categoriaUsuario.getId()))
                .thenReturn(true);

        ViolacaoDadosException excecao = assertThrowsExactly(ViolacaoDadosException.class,
                () -> categoriaService.atualizar(categoriaUsuario.getId(), dtoDuplicado));

        assertTrue(excecao.getMessage().contains("Já existe uma categoria"),
                "A mensagem deve indicar duplicidade");

        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa tentativa de atualizar categoria de outro usuário.
     * Deve lançar EntityNotFoundException.
     */
    @Test
    @DisplayName("atualizar: quando categoria pertence a outro usuário, deve lançar EntityNotFoundException")
    void testeAtualizar_QuandoOutroUsuario_DeveLancarExcecao() {
        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        Categoria categoriaIntrusa = new Categoria("Intrusa", TIPO, ICONE, COR, intruso);
        categoriaIntrusa.setId(UUID.randomUUID());

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaIntrusa.getId())).thenReturn(Optional.of(categoriaIntrusa));

        UUID idIntruso = categoriaIntrusa.getId();

        assertThrows(EntityNotFoundException.class,
                () -> categoriaService.atualizar(idIntruso, criarAtualizarDTO));

        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o método {@link CategoriaService#excluir}.
     * Valida exclusão de categoria do usuário.
     */
    @Test
    @DisplayName("excluir: quando categoria do usuário, deve remover")
    void testeExcluir_QuandoCategoriaUsuario_DeveDeletar() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaUsuario.getId())).thenReturn(Optional.of(categoriaUsuario));

        categoriaService.excluir(categoriaUsuario.getId());

        verify(categoriaRepository).delete(categoriaUsuario);
    }

    /**
     * Testa tentativa de excluir categoria do sistema.
     * Deve lançar RegraDeNegocioException.
     */
    @Test
    @DisplayName("excluir: quando categoria do sistema, deve lançar RegraDeNegocioException")
    void testeExcluir_QuandoCategoriaSistema_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaSistema.getId())).thenReturn(Optional.of(categoriaSistema));

        RegraDeNegocioException excecao = assertThrowsExactly(RegraDeNegocioException.class,
                () -> categoriaService.excluir(categoriaSistema.getId()));

        assertTrue(excecao.getMessage().contains("Não é possível excluir"),
                "A mensagem deve impedir exclusão do sistema");

        verify(categoriaRepository, never()).delete(any());
    }

    /**
     * Testa o throw do método privado buscarCategoriaValidada.
     * Cenário: Categoria não encontrada no banco.
     */
    @Test
    @DisplayName("excluir: quando categoria não encontrada, deve lançar EntityNotFoundException")
    void testeExcluir_QuandoNaoEncontrada_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findById(categoriaUsuario.getId())).thenReturn(Optional.empty());

        UUID idInexistente = categoriaUsuario.getId();

        assertThrows(EntityNotFoundException.class,
                () -> categoriaService.excluir(idInexistente));

        verify(categoriaRepository, never()).delete(any());
    }
}
