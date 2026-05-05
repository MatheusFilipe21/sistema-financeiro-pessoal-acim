package br.com.sfpacim.backend.controllers;

import java.math.BigDecimal;
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
import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.dtos.conta.FiltroContaDTO;
import br.com.sfpacim.backend.dtos.conta.ListagemContaDTO;
import br.com.sfpacim.backend.dtos.conta.SelecaoContaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.services.ContaService;
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
 * Testes unitários para a classe {@link ContaController}.
 *
 * <p>
 * Esta classe utiliza {@link WebMvcTest} para carregar apenas a camada web
 * (MVC) e testa o controlador de forma isolada.
 *
 * @author Matheus F. N. Pereira
 */
@WebMvcTest(ContaController.class)
@Import({ JacksonConfig.class, TratadorDeErrosGlobal.class })
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContaService contaService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String NOME = "Investimentos Mercado Pago";
    private static final InstituicaoFinanceira INSTITUICAO = InstituicaoFinanceira.MERCADO_PAGO;
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1500.50");
    private static final String NOME_INVALIDO_BRANCO = " ";
    private static final UUID ID_CONTA = UUID.randomUUID();
    private static final UUID ID_PESSOA = UUID.randomUUID();
    private static final PessoaDTO PESSOA_DTO = new PessoaDTO(ID_PESSOA, "Matheus Filipe do Nascimento Pereira", true);

    /**
     * Testa o método {@link ContaController#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve retornar HTTP 201 Created")
    void testeCadastrar_QuandoDadosValidos_DeveRetornar201() throws Exception {
        CriarAtualizarContaDTO dtoEntrada = new CriarAtualizarContaDTO(NOME, INSTITUICAO, SALDO_INICIAL, ID_PESSOA);
        ContaDTO dtoSaida = new ContaDTO(ID_CONTA, NOME, INSTITUICAO, SALDO_INICIAL, SALDO_INICIAL, ID_PESSOA);

        when(contaService.cadastrar(any(CriarAtualizarContaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(post("/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_CONTA.toString()))
                .andExpect(jsonPath("$.nome").value(NOME))
                .andExpect(jsonPath("$.saldoAtual").value(SALDO_INICIAL.doubleValue()))
                .andExpect(header().exists("Location"));
    }

    /**
     * Testa a validação do DTO no método
     * {@link ContaController#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome em branco (DTO Validation), deve retornar HTTP 422")
    void testeCadastrar_QuandoNomeInvalido_DeveRetornar422() throws Exception {
        CriarAtualizarContaDTO dtoInvalido = new CriarAtualizarContaDTO(NOME_INVALIDO_BRANCO, INSTITUICAO,
                SALDO_INICIAL, ID_PESSOA);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

        mockMvc.perform(post("/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableContent());
    }

    /**
     * Testa o conflito no método
     * {@link ContaController#cadastrar(CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("cadastrar: Quando nome duplicado, deve retornar HTTP 409 Conflict")
    void testeCadastrar_QuandoConflito_DeveRetornar409() throws Exception {
        CriarAtualizarContaDTO dtoEntrada = new CriarAtualizarContaDTO(NOME, INSTITUICAO, SALDO_INICIAL, ID_PESSOA);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(contaService.cadastrar(any(CriarAtualizarContaDTO.class)))
                .thenThrow(new ViolacaoDadosException("Conta duplicada"));

        mockMvc.perform(post("/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isConflict());
    }

    /**
     * Testa o método {@link ContaController#listar(FiltroContaDTO, Pageable)}.
     */
    @Test
    @DisplayName("listar: Deve retornar HTTP 200 OK e a paginação de contas")
    void testeListar_DeveRetornarPaginacao200() throws Exception {
        ListagemContaDTO dto = new ListagemContaDTO(ID_CONTA, NOME, INSTITUICAO, SALDO_INICIAL, PESSOA_DTO.nome());
        Page<ListagemContaDTO> pagina = new PageImpl<>(List.of(dto));

        when(contaService.listar(any(FiltroContaDTO.class), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/contas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(ID_CONTA.toString()))
                .andExpect(jsonPath("$.itens[0].nome").value(NOME))
                .andExpect(jsonPath("$.paginaAtual").value(0));
    }

    /**
     * Testa o método {@link ContaController#listarOpcoesSelecao()}.
     */
    @Test
    @DisplayName("listarOpcoesSelecao: Deve retornar HTTP 200 OK e a lista simplificada")
    void testeListarOpcoesSelecao_DeveRetornarLista200() throws Exception {
        SelecaoContaDTO selecao = new SelecaoContaDTO(ID_CONTA, NOME, INSTITUICAO, PESSOA_DTO.nome());

        when(contaService.listarOpcoes()).thenReturn(List.of(selecao));

        mockMvc.perform(get("/contas/selecao")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_CONTA.toString()))
                .andExpect(jsonPath("$[0].nome").value(NOME));
    }

    /**
     * Testa o método {@link ContaController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando encontrada, deve retornar HTTP 200 OK e o DTO")
    void testeBuscarPorId_QuandoValido_DeveRetornar200() throws Exception {
        ContaDTO dtoSaida = new ContaDTO(ID_CONTA, NOME, INSTITUICAO, SALDO_INICIAL, SALDO_INICIAL, ID_PESSOA);

        when(contaService.buscarPorId(ID_CONTA)).thenReturn(dtoSaida);

        mockMvc.perform(get("/contas/{id}", ID_CONTA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_CONTA.toString()))
                .andExpect(jsonPath("$.nome").value(NOME));
    }

    /**
     * Testa o erro 404 no método {@link ContaController#buscarPorId(UUID)}.
     */
    @Test
    @DisplayName("buscarPorId: Quando não encontrada, deve retornar HTTP 404 Not Found")
    void testeBuscarPorId_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        when(contaService.buscarPorId(ID_CONTA)).thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(get("/contas/{id}", ID_CONTA)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa o método
     * {@link ContaController#atualizar(UUID, CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando válido, deve retornar HTTP 200 OK com dados atualizados")
    void testeAtualizar_QuandoValido_DeveRetornar200() throws Exception {
        CriarAtualizarContaDTO dtoEntrada = new CriarAtualizarContaDTO("Novo Nome", INSTITUICAO, SALDO_INICIAL,
                ID_PESSOA);
        ContaDTO dtoSaida = new ContaDTO(ID_CONTA, "Novo Nome", INSTITUICAO, SALDO_INICIAL, SALDO_INICIAL, ID_PESSOA);

        when(contaService.atualizar(eq(ID_CONTA), any(CriarAtualizarContaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(put("/contas/{id}", ID_CONTA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    /**
     * Testa o erro 404 no método
     * {@link ContaController#atualizar(UUID, CriarAtualizarContaDTO)}.
     */
    @Test
    @DisplayName("atualizar: Quando não encontrado, deve retornar HTTP 404")
    void testeAtualizar_QuandoNaoEncontrado_DeveRetornar404() throws Exception {
        CriarAtualizarContaDTO dtoEntrada = new CriarAtualizarContaDTO(NOME, INSTITUICAO, SALDO_INICIAL, ID_PESSOA);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        when(contaService.atualizar(eq(ID_CONTA), any(CriarAtualizarContaDTO.class)))
                .thenThrow(new EntityNotFoundException("Não encontrado"));

        mockMvc.perform(put("/contas/{id}", ID_CONTA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isNotFound());
    }

    /**
     * Testa o método {@link ContaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve retornar HTTP 204 No Content")
    void testeExcluir_QuandoValido_DeveRetornar204() throws Exception {
        mockMvc.perform(delete("/contas/{id}", ID_CONTA))
                .andExpect(status().isNoContent());
    }

    /**
     * Testa os vínculos de integridade no método
     * {@link ContaController#excluir(UUID)}.
     */
    @Test
    @DisplayName("excluir: Quando houver vínculos (Regra de Negócio), deve retornar HTTP 409")
    void testeExcluir_QuandoComVinculos_DeveRetornar409() throws Exception {
        doThrow(new ViolacaoDadosException("Não é possível excluir conta com transações."))
                .when(contaService).excluir(ID_CONTA);

        mockMvc.perform(delete("/contas/{id}", ID_CONTA))
                .andExpect(status().isConflict());
    }
}
