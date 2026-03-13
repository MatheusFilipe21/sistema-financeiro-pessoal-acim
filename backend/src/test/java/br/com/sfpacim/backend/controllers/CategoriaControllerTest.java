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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sfpacim.backend.config.JacksonConfig;
import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
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
     * Testa o endpoint POST /categorias.
     * Valida o cenário de sucesso.
     *
     * <p>
     * Verifica se, ao enviar dados válidos, o controlador retorna HTTP 201
     * (Created), o DTO criado e o cabeçalho 'Location'.
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
     * Testa a validação do endpoint POST /categorias (Nome Obrigatório).
     *
     * <p>
     * Verifica se o @Valid barra nomes em branco, retornando HTTP 422.
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
     * Testa o erro de conflito (Nome Duplicado) no cadastro.
     *
     * <p>
     * Simula o serviço lançando ViolacaoDadosException e verifica se o
     * TratadorDeErrosGlobal converte corretamente para HTTP 409 (Conflict).
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
     * Testa o endpoint GET /categorias.
     * Valida o cenário de sucesso.
     */
    @Test
    @DisplayName("listar: Deve retornar HTTP 200 OK e a lista de categorias")
    void testeListar_DeveRetornarLista() throws Exception {
        List<CategoriaDTO> lista = List
                .of(new CategoriaDTO(ID_CATEGORIA, NOME, TIPO, ICONE, COR, false));

        when(categoriaService.listar()).thenReturn(lista);

        mockMvc.perform(get("/categorias")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_CATEGORIA.toString()))
                .andExpect(jsonPath("$[0].nome").value(NOME));
    }

    /**
     * Testa o endpoint PUT /categorias/{id}.
     * Valida o cenário de sucesso.
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
     * Testa o endpoint PUT /categorias/{id} quando o registro não existe.
     *
     * <p>
     * Simula EntityNotFoundException e espera HTTP 404 (Not Found).
     */
    @Test
    @DisplayName("atualizar: Quando não encontrado, deve retornar HTTP 404")
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
     * Testa o endpoint DELETE /categorias/{id}.
     * Valida o cenário de sucesso.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve retornar HTTP 204 No Content")
    void testeExcluir_QuandoValido_DeveRetornar204() throws Exception {
        mockMvc.perform(delete("/categorias/{id}", ID_CATEGORIA))
                .andExpect(status().isNoContent());
    }

    /**
     * Testa o endpoint DELETE /categorias/{id} quando há regra de negócio
     * impeditiva.
     * (Ex: Tentar excluir categoria padrão do sistema).
     *
     * <p>
     * O TratadorDeErrosGlobal mapeia RegraDeNegocioException para 422 Unprocessable
     * Entity.
     */
    @Test
    @DisplayName("excluir: Quando for categoria do sistema (Regra de Negócio), deve retornar HTTP 422")
    void testeExcluir_QuandoSistema_DeveRetornar422() throws Exception {
        doThrow(new RegraDeNegocioException("Não é possível excluir uma categoria padrão do sistema."))
                .when(categoriaService).excluir(ID_CATEGORIA);

        mockMvc.perform(delete("/categorias/{id}", ID_CATEGORIA))
                .andExpect(status().isUnprocessableContent());
    }
}