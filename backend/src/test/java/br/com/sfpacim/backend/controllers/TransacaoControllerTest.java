package br.com.sfpacim.backend.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.FiltroTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.ListagemTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.TransacaoDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import br.com.sfpacim.backend.services.TokenService;
import br.com.sfpacim.backend.services.TransacaoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes unitários para a classe {@link TransacaoController}.
 *
 * <p>
 * Esta classe utiliza {@link WebMvcTest} para validar a camada de exposição
 * REST,
 * garantindo o correto mapeamento de endpoints, validação de DTOs e tratamento
 * de exceções de negócio através do {@link TratadorDeErrosGlobal}.
 *
 * @author Matheus F. N. Pereira
 */
@WebMvcTest(TransacaoController.class)
@Import({ JacksonConfig.class, TratadorDeErrosGlobal.class })
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransacaoService transacaoService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final UUID ID_TRANSACAO = UUID.randomUUID();
    private static final UUID ID_CATEGORIA = UUID.randomUUID();
    private static final UUID ID_CONTA = UUID.randomUUID();
    private static final UUID ID_PESSOA = UUID.randomUUID();
    private static final String DESCRICAO = "Compra de Teste";
    private static final BigDecimal VALOR = new BigDecimal("250.00");

    /**
     * Testa o método
     * {@link TransacaoController#cadastrar(CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve retornar HTTP 201 Created")
    void testeCadastrar_QuandoDadosValidos_DeveRetornar201() throws Exception {
        CriarAtualizarTransacaoDTO dtoEntrada = new CriarAtualizarTransacaoDTO(
                DESCRICAO, VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        TransacaoDTO dtoSaida = new TransacaoDTO(
                ID_TRANSACAO, DESCRICAO, VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        when(transacaoService.cadastrar(any(CriarAtualizarTransacaoDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_TRANSACAO.toString()))
                .andExpect(jsonPath("$.descricao").value(DESCRICAO))
                .andExpect(header().exists("Location"));
    }

    /**
     * Testa a validação de entrada do Bean Validation no endpoint
     * {@link TransacaoController#cadastrar(CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando valor negativo (Validation), deve retornar HTTP 422")
    void testeCadastrar_QuandoValorNegativo_DeveRetornar422() throws Exception {
        CriarAtualizarTransacaoDTO dtoInvalido = new CriarAtualizarTransacaoDTO(
                DESCRICAO, new BigDecimal("-10.00"), LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa a listagem paginada no método
     * {@link TransacaoController#listar(FiltroTransacaoDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar HTTP 200 OK e a página de transações")
    void testeListar_DeveRetornarPaginacao200() throws Exception {
        ListagemTransacaoDTO dtoMock = mock(ListagemTransacaoDTO.class);
        Page<ListagemTransacaoDTO> pagina = new PageImpl<>(List.of(dtoMock));

        when(transacaoService.listar(any(FiltroTransacaoDTO.class), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/transacoes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginaAtual").value(0))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    /**
     * Testa o método {@link TransacaoController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando encontrada, deve retornar HTTP 200 OK e o DTO")
    void testeBuscarPorId_QuandoValido_DeveRetornar200() throws Exception {
        TransacaoDTO dtoSaida = new TransacaoDTO(
                ID_TRANSACAO, DESCRICAO, VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        when(transacaoService.buscarPorId(ID_TRANSACAO)).thenReturn(dtoSaida);

        mockMvc.perform(get("/transacoes/{id}", ID_TRANSACAO)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_TRANSACAO.toString()))
                .andExpect(jsonPath("$.descricao").value(DESCRICAO));
    }

    /**
     * Testa o erro 404 no método {@link TransacaoController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando não encontrada, deve retornar HTTP 404 Not Found")
    void testeBuscarPorId_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        when(transacaoService.buscarPorId(ID_TRANSACAO)).thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(get("/transacoes/{id}", ID_TRANSACAO)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa a atualização com sucesso via
     * {@link TransacaoController#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando válido, deve retornar HTTP 200 OK com dados atualizados")
    void testeAtualizar_QuandoValido_DeveRetornar200() throws Exception {
        CriarAtualizarTransacaoDTO dtoEntrada = new CriarAtualizarTransacaoDTO(
                "Nova Descricao", VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        TransacaoDTO dtoSaida = new TransacaoDTO(
                ID_TRANSACAO, "Nova Descricao", VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        when(transacaoService.atualizar(eq(ID_TRANSACAO), any(CriarAtualizarTransacaoDTO.class)))
                .thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(put("/transacoes/{id}", ID_TRANSACAO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Nova Descricao"));
    }

    /**
     * Testa o tratamento de exceção de negócio no método
     * {@link TransacaoController#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando ID de dependência for inválido, deve retornar HTTP 422 Unprocessable Entity")
    void testeAtualizar_QuandoErroNegocio_DeveRetornar422() throws Exception {
        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                DESCRICAO, VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        String jsonRequisicao = objectMapper.writeValueAsString(dto);

        when(transacaoService.atualizar(eq(ID_TRANSACAO), any(CriarAtualizarTransacaoDTO.class)))
                .thenThrow(new RegraDeNegocioException("ID da conta inválido"));

        mockMvc.perform(put("/transacoes/{id}", ID_TRANSACAO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa o tratamento de exceção 404 no método
     * {@link TransacaoController#atualizar(UUID, CriarAtualizarTransacaoDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando transação não for encontrada, deve retornar HTTP 404 Not Found")
    void testeAtualizar_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        CriarAtualizarTransacaoDTO dto = new CriarAtualizarTransacaoDTO(
                DESCRICAO, VALOR, LocalDate.now(), LocalDate.now(), null,
                TipoTransacao.DESPESA, StatusTransacao.PENDENTE, null,
                ID_CATEGORIA, ID_CONTA, ID_PESSOA);

        String jsonRequisicao = objectMapper.writeValueAsString(dto);

        when(transacaoService.atualizar(eq(ID_TRANSACAO), any(CriarAtualizarTransacaoDTO.class)))
                .thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(put("/transacoes/{id}", ID_TRANSACAO)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa o endpoint de exclusão {@link TransacaoController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando ID existe, deve retornar HTTP 204 No Content")
    void testeExcluir_QuandoValido_DeveRetornar204() throws Exception {
        mockMvc.perform(delete("/transacoes/{id}", ID_TRANSACAO))
                .andExpect(status().isNoContent());
    }

    /**
     * Testa o cenário de transação inexistente na exclusão do método
     * {@link TransacaoController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando não encontrada, deve retornar HTTP 404 Not Found")
    void testeExcluir_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        doThrow(new EntityNotFoundException("Não encontrado"))
                .when(transacaoService).excluir(ID_TRANSACAO);

        mockMvc.perform(delete("/transacoes/{id}", ID_TRANSACAO))
                .andExpect(status().isNotFound());
    }
}
