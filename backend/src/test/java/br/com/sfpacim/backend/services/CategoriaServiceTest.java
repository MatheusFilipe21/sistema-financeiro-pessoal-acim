package br.com.sfpacim.backend.services;

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

import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.repositories.CategoriaRepository;
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
    private CategoriaRepository categoriaRepository;

    @Mock
    private ContextoUsuarioService contextoUsuarioService;

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
    }

    /**
     * Testa o cenário de sucesso no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve salvar e retornar CategoriaDTO")
    void testeCadastrar_QuandoDadosValidos_DeveSalvar() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(Collections.emptyList());
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoriaUsuario);

        CategoriaDTO resultado = categoriaService.cadastrar(criarAtualizarDTO);

        assertNotNull(resultado);
        assertEquals(NOME_CATEGORIA, resultado.nome());
        verify(categoriaRepository).saveAndFlush(any(Categoria.class));
    }

    /**
     * Testa a validação em memória no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado na memória, deve lançar ViolacaoDadosException")
    void testeCadastrar_QuandoNomeDuplicadoEmMemoria_DeveLancarExcecao() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(List.of(categoriaUsuario));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> categoriaService.cadastrar(criarAtualizarDTO));

        assertTrue(excecao.getMessage().contains(NOME_CATEGORIA));
        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa o bloqueio real do banco de dados no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar exceção de integridade, deve tratar como ViolacaoDadosException")
    void testeCadastrar_ErroBanco_LancaExcecaoReal() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(Collections.emptyList());

        when(categoriaRepository.saveAndFlush(any(Categoria.class)))
                .thenThrow(new DataIntegrityViolationException("Simulação constraint"));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> categoriaService.cadastrar(criarAtualizarDTO));

        assertTrue(excecao.getMessage().contains(NOME_CATEGORIA));
    }

    /**
     * Testa a ramificação do 'return null' no método
     * {@link CategoriaService#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando banco lançar erro, testa o catch cobrindo o return null (Mock Estático)")
    void testeCadastrar_ErroBanco_CobrindoReturnNull() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(Collections.emptyList());

        when(categoriaRepository.saveAndFlush(any(Categoria.class)))
                .thenThrow(new DataIntegrityViolationException("Erro"));

        try (MockedStatic<MetodosUteis> utilMock = mockStatic(MetodosUteis.class)) {
            assertThrows(NullPointerException.class, () -> categoriaService.cadastrar(criarAtualizarDTO));
            utilMock.verify(() -> MetodosUteis.validarUnicidade(true, Categoria.class.getSimpleName(), NOME_CATEGORIA));
        }
    }

    /**
     * Testa a listagem mista no método {@link CategoriaService#listar()}.
     */
    @Test
    @DisplayName("listar: Deve retornar as categorias do usuário e do sistema ordenadas")
    void testeListar_DeveRetornarCategorias() {
        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(List.of(categoriaSistema, categoriaUsuario));

        List<CategoriaDTO> resultado = categoriaService.listar();

        assertEquals(2, resultado.size());
        assertEquals(NOME_CATEGORIA_SISTEMA, resultado.get(0).nome());
        assertEquals(NOME_CATEGORIA, resultado.get(1).nome());
    }

    /**
     * Testa atualização com sucesso no método
     * {@link CategoriaService#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando categoria do usuário e dados válidos, deve atualizar")
    void testeAtualizar_QuandoValido_DeveAtualizar() {
        UUID id = categoriaUsuario.getId();
        CriarAtualizarCategoriaDTO dtoNovo = new CriarAtualizarCategoriaDTO("Internet", TIPO, "wifi", COR);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));

        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(List.of(categoriaUsuario));
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenReturn(categoriaUsuario);

        CategoriaDTO resultado = categoriaService.atualizar(id, dtoNovo);

        assertEquals("Internet", resultado.nome());
        verify(categoriaRepository).saveAndFlush(any(Categoria.class));
    }

    /**
     * Testa ramificação de conflito de IDs (outra categoria com o nome desejado) no
     * atualizar.
     */
    @Test
    @DisplayName("atualizar: Quando nome pertence a outra categoria na memória, deve lançar ViolacaoDadosException")
    void testeAtualizar_QuandoNomeDuplicadoOutroId_DeveLancarExcecao() {
        UUID id = categoriaUsuario.getId();

        Categoria outraCategoria = new Categoria("Internet", TIPO, "wifi", COR, usuario);
        outraCategoria.setId(UUID.randomUUID());

        CriarAtualizarCategoriaDTO dtoConflito = new CriarAtualizarCategoriaDTO("Internet", TIPO, "wifi", COR);

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));

        when(categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId()))
                .thenReturn(List.of(outraCategoria));

        ViolacaoDadosException excecao = assertThrows(ViolacaoDadosException.class,
                () -> categoriaService.atualizar(id, dtoConflito));

        assertTrue(excecao.getMessage().contains("Internet"));
        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa bloqueio de edição em categorias padrão no método
     * {@link CategoriaService#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando tentar alterar categoria do sistema, deve lançar RegraDeNegocioException")
    void testeAtualizar_QuandoCategoriaSistema_DeveLancarExcecao() {
        UUID id = categoriaSistema.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaSistema));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> categoriaService.atualizar(id, criarAtualizarDTO));

        assertTrue(excecao.getMessage().contains("não podem ser alteradas"));
        verify(categoriaRepository, never()).saveAndFlush(any());
    }

    /**
     * Testa bloqueio de exclusão em categorias padrão no método
     * {@link CategoriaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando tentar excluir categoria do sistema, deve lançar RegraDeNegocioException")
    void testeExcluir_QuandoCategoriaSistema_DeveLancarExcecao() {
        UUID id = categoriaSistema.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaSistema));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> categoriaService.excluir(id));

        assertTrue(excecao.getMessage().contains("Não é possível excluir"));
        verify(categoriaRepository, never()).delete(any());
    }

    /**
     * Testa exclusão com sucesso no método {@link CategoriaService#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando categoria do usuário, deve remover com sucesso")
    void testeExcluir_QuandoValido_DeveDeletar() {
        UUID id = categoriaUsuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(), id))
                .thenReturn(Optional.of(categoriaUsuario));

        assertDoesNotThrow(() -> categoriaService.excluir(id));

        verify(categoriaRepository).delete(categoriaUsuario);
    }

    /**
     * Testa blindagem da consulta no método privado de validação.
     */
    @Test
    @DisplayName("buscarCategoriaValidada: Quando não encontrada, deve lançar EntityNotFoundException")
    void testeBuscarCategoriaValidada_QuandoNaoEncontrada_DeveLancarExcecao() {
        UUID id = categoriaUsuario.getId();

        when(contextoUsuarioService.getUsuarioAutenticado()).thenReturn(usuario);
        when(categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(), id)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> categoriaService.excluir(id));

        assertTrue(excecao.getMessage().contains("não encontrada ou acesso negado"));
    }
}
