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
import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.FiltroPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.SelecaoPessoaDTO;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.PessoaService;
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
 * Testes unitários para a classe {@link PessoaController}.
 *
 * <p>
 * Esta classe utiliza {@link WebMvcTest} para carregar apenas a camada web
 * (MVC) e testa o controlador de forma isolada.
 *
 * @author Matheus F. N. Pereira
 */
@WebMvcTest(PessoaController.class)
@Import({ JacksonConfig.class, TratadorDeErrosGlobal.class })
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PessoaService pessoaService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final boolean TITULAR = true;
    private static final String NOME_INVALIDO_BRANCO = " ";
    private static final UUID ID_PESSOA = UUID.randomUUID();

    /**
     * Testa o método {@link PessoaController#cadastrar(CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve retornar HTTP 201 Created")
    void testeCadastrar_QuandoDadosValidos_DeveRetornar201() throws Exception {
        CriarAtualizarPessoaDTO dtoEntrada = new CriarAtualizarPessoaDTO(NOME, TITULAR);
        PessoaDTO dtoSaida = new PessoaDTO(ID_PESSOA, NOME, TITULAR);

        when(pessoaService.cadastrar(any(CriarAtualizarPessoaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(post("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_PESSOA.toString()))
                .andExpect(jsonPath("$.nome").value(NOME))
                .andExpect(header().exists("Location"));
    }

    /**
     * Testa a validação do DTO no método
     * {@link PessoaController#cadastrar(CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome em branco (DTO Validation), deve retornar HTTP 422")
    void testeCadastrar_QuandoNomeInvalido_DeveRetornar422() throws Exception {
        CriarAtualizarPessoaDTO dtoInvalido = new CriarAtualizarPessoaDTO(NOME_INVALIDO_BRANCO, TITULAR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

        mockMvc.perform(post("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa o conflito no método
     * {@link PessoaController#cadastrar(CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado, deve retornar HTTP 409 Conflict")
    void testeCadastrar_QuandoConflito_DeveRetornar409() throws Exception {
        CriarAtualizarPessoaDTO dtoEntrada = new CriarAtualizarPessoaDTO(NOME, TITULAR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(pessoaService.cadastrar(any(CriarAtualizarPessoaDTO.class)))
                .thenThrow(new ViolacaoDadosException("Nome duplicado"));

        mockMvc.perform(post("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isConflict());
    }

    /**
     * Testa o método {@link PessoaController#listar(FiltroPessoaDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar HTTP 200 OK e a paginação de pessoas")
    void testeListar_DeveRetornarPaginacao200() throws Exception {
        PessoaDTO dto = new PessoaDTO(ID_PESSOA, NOME, TITULAR);
        Page<PessoaDTO> pagina = new PageImpl<>(List.of(dto));

        when(pessoaService.listar(any(FiltroPessoaDTO.class), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/pessoas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(ID_PESSOA.toString()))
                .andExpect(jsonPath("$.itens[0].nome").value(NOME))
                .andExpect(jsonPath("$.paginaAtual").value(0));
    }

    /**
     * Testa o método {@link PessoaController#listarOpcoesSelecao(Boolean)}.
     */
    @Test
    @DisplayName("listarOpcoesSelecao: Deve retornar HTTP 200 OK e a lista simplificada")
    void testeListarOpcoesSelecao_DeveRetornarLista200() throws Exception {
        SelecaoPessoaDTO selecao = new SelecaoPessoaDTO(ID_PESSOA, NOME);

        when(pessoaService.listarOpcoes(any())).thenReturn(List.of(selecao));

        mockMvc.perform(get("/pessoas/selecao")
                .contentType(MediaType.APPLICATION_JSON)
                .param("titular", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_PESSOA.toString()))
                .andExpect(jsonPath("$[0].nome").value(NOME));
    }

    /**
     * Testa o método {@link PessoaController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando encontrada, deve retornar HTTP 200 OK e o DTO")
    void testeBuscarPorId_QuandoValido_DeveRetornar200() throws Exception {
        PessoaDTO dtoSaida = new PessoaDTO(ID_PESSOA, NOME, TITULAR);

        when(pessoaService.buscarPorId(ID_PESSOA)).thenReturn(dtoSaida);

        mockMvc.perform(get("/pessoas/{id}", ID_PESSOA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_PESSOA.toString()))
                .andExpect(jsonPath("$.nome").value(NOME));
    }

    /**
     * Testa o erro 404 no método {@link PessoaController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando não encontrada, deve retornar HTTP 404 Not Found")
    void testeBuscarPorId_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        when(pessoaService.buscarPorId(ID_PESSOA)).thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(get("/pessoas/{id}", ID_PESSOA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa o método
     * {@link PessoaController#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando válido, deve retornar HTTP 200 OK com dados atualizados")
    void testeAtualizar_QuandoValido_DeveRetornar200() throws Exception {
        CriarAtualizarPessoaDTO dtoEntrada = new CriarAtualizarPessoaDTO("Novo Nome", TITULAR);
        PessoaDTO dtoSaida = new PessoaDTO(ID_PESSOA, "Novo Nome", TITULAR);

        when(pessoaService.atualizar(eq(ID_PESSOA), any(CriarAtualizarPessoaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(put("/pessoas/{id}", ID_PESSOA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    /**
     * Testa o erro 404 no método
     * {@link PessoaController#atualizar(UUID, CriarAtualizarPessoaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando não encontrado, deve retornar HTTP 404")
    void testeAtualizar_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        CriarAtualizarPessoaDTO dtoEntrada = new CriarAtualizarPessoaDTO(NOME, TITULAR);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(pessoaService.atualizar(eq(ID_PESSOA), any(CriarAtualizarPessoaDTO.class)))
                .thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(put("/pessoas/{id}", ID_PESSOA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa o método {@link PessoaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve retornar HTTP 204 No Content")
    void testeExcluir_QuandoValido_DeveRetornar204() throws Exception {
        mockMvc.perform(delete("/pessoas/{id}", ID_PESSOA))
                .andExpect(status().isNoContent());
    }

    /**
     * Testa os vínculos de integridade no método
     * {@link PessoaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando houver vínculos (Regra de Negócio), deve retornar HTTP 409")
    void testeExcluir_QuandoComVinculos_DeveRetornar409() throws Exception {
        doThrow(new ViolacaoDadosException("Não é possível excluir pessoa com vínculos."))
                .when(pessoaService).excluir(ID_PESSOA);

        mockMvc.perform(delete("/pessoas/{id}", ID_PESSOA))
                .andExpect(status().isConflict());
    }
}
