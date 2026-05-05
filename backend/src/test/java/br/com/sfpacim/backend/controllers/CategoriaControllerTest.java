package br.com.sfpacim.backend.controllers;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sfpacim.backend.config.JacksonConfig;
import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.FiltroCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.SelecaoCategoriaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.services.CategoriaService;
import br.com.sfpacim.backend.services.TokenService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes unitários para a classe {@link CategoriaController}.
 *
 * <p>
 * Esta classe utiliza {@link WebMvcTest} para carregar apenas a camada web
 * (MVC) e testa o controlador de forma isolada.
 *
 * @author Matheus F. N. Pereira
 */
@WebMvcTest(CategoriaController.class)
@Import({ JacksonConfig.class, TratadorDeErrosGlobal.class })
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String NOME = "Alimentação";
    private static final TipoCategoria TIPO = TipoCategoria.DESPESA;
    private static final String ICONE = "restaurant";
    private static final String COR = "#FF0000";
    private static final String NOME_INVALIDO_BRANCO = " ";
    private static final UUID ID_CATEGORIA = UUID.randomUUID();

    /**
     * Testa o método
     * {@link CategoriaController#cadastrar(CriarAtualizarCategoriaDTO)}.
     * Valida o cenário de sucesso garantindo a geração do Location.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve retornar HTTP 201 Created")
    void testeCadastrar_QuandoDadosValidos_DeveRetornar201() throws Exception {
        CriarAtualizarCategoriaDTO dtoEntrada = new CriarAtualizarCategoriaDTO(NOME, TIPO, ICONE, COR);
        CategoriaDTO dtoSaida = new CategoriaDTO(ID_CATEGORIA, NOME, TIPO, ICONE, COR, false);

        when(categoriaService.cadastrar(any(CriarAtualizarCategoriaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_CATEGORIA.toString()))
                .andExpect(jsonPath("$.nome").value(NOME))
                .andExpect(jsonPath("$.tipo").value(TIPO.toString()))
                .andExpect(header().exists("Location"));
    }

    /**
     * Testa a validação do DTO no método
     * {@link CategoriaController#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome em branco (DTO Validation), deve retornar HTTP 422")
    void testeCadastrar_QuandoNomeInvalido_DeveRetornar422() throws Exception {
        CriarAtualizarCategoriaDTO dtoInvalido = new CriarAtualizarCategoriaDTO(NOME_INVALIDO_BRANCO, TIPO, ICONE, COR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa a conversão de exceção de violação no método
     * {@link CategoriaController#cadastrar(CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado, deve retornar HTTP 409 Conflict")
    void testeCadastrar_QuandoConflito_DeveRetornar409() throws Exception {
        CriarAtualizarCategoriaDTO dtoEntrada = new CriarAtualizarCategoriaDTO(NOME, TIPO, ICONE, COR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(categoriaService.cadastrar(any(CriarAtualizarCategoriaDTO.class)))
                .thenThrow(new ViolacaoDadosException("Categoria duplicada"));

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isConflict());
    }

    /**
     * Testa o método
     * {@link CategoriaController#listar(FiltroCategoriaDTO, Pageable)}.
     * Valida o retorno do envelopamento de paginação.
     */
    @Test
    @DisplayName("listar: Deve retornar HTTP 200 OK e a paginação de categorias")
    void testeListar_DeveRetornarPaginacao200() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(ID_CATEGORIA, NOME, TIPO, ICONE, COR, false);
        Page<CategoriaDTO> pagina = new PageImpl<>(List.of(dto));

        when(categoriaService.listar(any(), any())).thenReturn(pagina);

        mockMvc.perform(get("/categorias")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(ID_CATEGORIA.toString()))
                .andExpect(jsonPath("$.itens[0].nome").value(NOME))
                .andExpect(jsonPath("$.paginaAtual").value(0));
    }

    /**
     * Testa o método {@link CategoriaController#listarOpcoesSelecao()}.
     * Valida o retorno da lista simples.
     */
    @Test
    @DisplayName("listarOpcoesSelecao: Deve retornar HTTP 200 OK e a lista simplificada")
    void testeListarOpcoesSelecao_DeveRetornarLista200() throws Exception {
        SelecaoCategoriaDTO selecao = new SelecaoCategoriaDTO(ID_CATEGORIA, NOME, TIPO, ICONE, COR);

        when(categoriaService.listarOpcoes()).thenReturn(List.of(selecao));

        mockMvc.perform(get("/categorias/selecao")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_CATEGORIA.toString()))
                .andExpect(jsonPath("$[0].nome").value(NOME));
    }

    /**
     * Testa o método {@link CategoriaController#buscarPorId(UUID)}.
     * Valida a resposta com a entidade encontrada.
     */
    @Test
    @DisplayName("buscarPorId: Quando encontrada, deve retornar HTTP 200 OK e o DTO")
    void testeBuscarPorId_QuandoValido_DeveRetornar200() throws Exception {
        CategoriaDTO dtoSaida = new CategoriaDTO(ID_CATEGORIA, NOME, TIPO, ICONE, COR, false);

        when(categoriaService.buscarPorId(ID_CATEGORIA)).thenReturn(dtoSaida);

        mockMvc.perform(get("/categorias/{id}", ID_CATEGORIA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_CATEGORIA.toString()))
                .andExpect(jsonPath("$.nome").value(NOME));
    }

    /**
     * Testa o erro 404 no método {@link CategoriaController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando não encontrada, deve retornar HTTP 404 Not Found")
    void testeBuscarPorId_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        when(categoriaService.buscarPorId(ID_CATEGORIA)).thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(get("/categorias/{id}", ID_CATEGORIA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa o método
     * {@link CategoriaController#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     * Valida a persistência e retorno.
     */
    @Test
    @DisplayName("atualizar: Quando válido, deve retornar HTTP 200 OK com dados atualizados")
    void testeAtualizar_QuandoValido_DeveRetornar200() throws Exception {
        CriarAtualizarCategoriaDTO dtoEntrada = new CriarAtualizarCategoriaDTO("Novo Nome", TIPO, ICONE, COR);
        CategoriaDTO dtoSaida = new CategoriaDTO(ID_CATEGORIA, "Novo Nome", TIPO, ICONE, COR, false);

        when(categoriaService.atualizar(eq(ID_CATEGORIA), any(CriarAtualizarCategoriaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(put("/categorias/{id}", ID_CATEGORIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    /**
     * Testa o erro 404 no método
     * {@link CategoriaController#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando não encontrado, deve retornar HTTP 404 Not Found")
    void testeAtualizar_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        CriarAtualizarCategoriaDTO dtoEntrada = new CriarAtualizarCategoriaDTO(NOME, TIPO, ICONE, COR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(categoriaService.atualizar(eq(ID_CATEGORIA), any(CriarAtualizarCategoriaDTO.class)))
                .thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(put("/categorias/{id}", ID_CATEGORIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa erro de negócio no método
     * {@link CategoriaController#atualizar(UUID, CriarAtualizarCategoriaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando for categoria do sistema, deve retornar HTTP 422")
    void testeAtualizar_QuandoSistema_DeveRetornar422() throws Exception {
        CriarAtualizarCategoriaDTO dtoEntrada = new CriarAtualizarCategoriaDTO(NOME, TIPO, ICONE, COR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(categoriaService.atualizar(eq(ID_CATEGORIA), any(CriarAtualizarCategoriaDTO.class)))
                .thenThrow(new RegraDeNegocioException("Não pode alterar categoria do sistema"));

        mockMvc.perform(put("/categorias/{id}", ID_CATEGORIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa o método {@link CategoriaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve retornar HTTP 204 No Content")
    void testeExcluir_QuandoValido_DeveRetornar204() throws Exception {
        mockMvc.perform(delete("/categorias/{id}", ID_CATEGORIA))
                .andExpect(status().isNoContent());
    }

    /**
     * Testa a proteção de sistema no método
     * {@link CategoriaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando for categoria do sistema (Regra de Negócio), deve retornar HTTP 422")
    void testeExcluir_QuandoSistema_DeveRetornar422() throws Exception {
        doThrow(new RegraDeNegocioException("Não é possível excluir uma categoria padrão do sistema."))
                .when(categoriaService).excluir(ID_CATEGORIA);

        mockMvc.perform(delete("/categorias/{id}", ID_CATEGORIA))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa o bloqueio por vínculos de integridade no método
     * {@link CategoriaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando possuir vínculos (ViolacaoDadosException), deve retornar HTTP 409")
    void testeExcluir_QuandoVinculo_DeveRetornar409() throws Exception {
        doThrow(new ViolacaoDadosException("Existem transações atreladas."))
                .when(categoriaService).excluir(ID_CATEGORIA);

        mockMvc.perform(delete("/categorias/{id}", ID_CATEGORIA))
                .andExpect(status().isConflict());
    }
}
