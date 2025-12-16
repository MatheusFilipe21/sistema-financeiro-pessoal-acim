package br.com.sfpacim.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sfpacim.backend.config.SegurancaConfig;
import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.services.ContaService;
import br.com.sfpacim.backend.services.TokenService;
import jakarta.persistence.EntityNotFoundException;

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
@Import({ SegurancaConfig.class, TratadorDeErrosGlobal.class })
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
     * Testa o endpoint POST /contas.
     * Valida o cenário de sucesso.
     *
     * <p>
     * Verifica se, ao enviar dados válidos, o controlador retorna HTTP 201
     * (Created), o DTO criado e o cabeçalho 'Location'.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve retornar HTTP 201 Created")
    void testeCadastrar_QuandoDadosValidos_DeveRetornar201() throws Exception {
        CriarAtualizarContaDTO dtoEntrada = new CriarAtualizarContaDTO(NOME, INSTITUICAO, SALDO_INICIAL, ID_PESSOA);
        ContaDTO dtoSaida = new ContaDTO(ID_CONTA, NOME, INSTITUICAO, SALDO_INICIAL, SALDO_INICIAL, PESSOA_DTO);

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
     * Testa a validação do endpoint POST /contas (Nome Obrigatório).
     *
     * <p>
     * Verifica se o @Valid barra nomes em branco, retornando HTTP 422.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("cadastrar: Quando nome em branco (DTO Validation), deve retornar HTTP 422")
    void testeCadastrar_QuandoNomeInvalido_DeveRetornar422() throws Exception {
        CriarAtualizarContaDTO dtoInvalido = new CriarAtualizarContaDTO(NOME_INVALIDO_BRANCO, INSTITUICAO,
                SALDO_INICIAL, ID_PESSOA);
        String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

        mockMvc.perform(post("/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa o erro de conflito (Nome Duplicado) no cadastro.
     *
     * <p>
     * Simula o serviço lançando ViolacaoDadosException e verifica se o
     * TratadorDeErrosGlobal converte corretamente para HTTP 409 (Conflict).
     */
    @SuppressWarnings("null")
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
     * Testa o endpoint GET /contas.
     * Valida o cenário de sucesso.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("listar: Deve retornar HTTP 200 OK e a lista de contas")
    void testeListar_DeveRetornarLista() throws Exception {
        List<ContaDTO> lista = List
                .of(new ContaDTO(ID_CONTA, NOME, INSTITUICAO, SALDO_INICIAL, SALDO_INICIAL, PESSOA_DTO));

        when(contaService.listar()).thenReturn(lista);

        mockMvc.perform(get("/contas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_CONTA.toString()))
                .andExpect(jsonPath("$[0].nome").value(NOME));
    }

    /**
     * Testa o endpoint PUT /contas/{id}.
     * Valida o cenário de sucesso.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("atualizar: Quando válido, deve retornar HTTP 200 OK com dados atualizados")
    void testeAtualizar_QuandoValido_DeveRetornar200() throws Exception {
        CriarAtualizarContaDTO dtoEntrada = new CriarAtualizarContaDTO("Novo Nome", INSTITUICAO, SALDO_INICIAL,
                ID_PESSOA);
        ContaDTO dtoSaida = new ContaDTO(ID_CONTA, "Novo Nome", INSTITUICAO, SALDO_INICIAL, SALDO_INICIAL, PESSOA_DTO);

        when(contaService.atualizar(eq(ID_CONTA), any(CriarAtualizarContaDTO.class))).thenReturn(dtoSaida);

        String jsonRequisicao = objectMapper.writeValueAsString(dtoEntrada);

        mockMvc.perform(put("/contas/{id}", ID_CONTA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    /**
     * Testa o endpoint PUT /contas/{id} quando o registro não existe.
     *
     * <p>
     * Simula EntityNotFoundException e espera HTTP 404 (Not Found).
     */
    @SuppressWarnings("null")
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
     * Testa o endpoint DELETE /contas/{id}.
     * Valida o cenário de sucesso.
     */
    @Test
    @DisplayName("excluir: Quando válido, deve retornar HTTP 204 No Content")
    void testeExcluir_QuandoValido_DeveRetornar204() throws Exception {
        mockMvc.perform(delete("/contas/{id}", ID_CONTA))
                .andExpect(status().isNoContent());
    }

    /**
     * Testa o endpoint DELETE /contas/{id} quando há violação de integridade.
     * (Ex: Tentar excluir conta com transações).
     *
     * <p>
     * Espera HTTP 409 Conflict.
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
