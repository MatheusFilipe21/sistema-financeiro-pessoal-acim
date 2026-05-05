package br.com.sfpacim.backend.dtos.erro;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para encapsular um erro de validação contendo uma
 * lista de campos com mensagens de erro específicas.
 *
 * @author Matheus F. N. Pereira
 *
 * @param erro  Contém os dados padrão do erro (Status, Título, Mensagem, Rota,
 *              Data/Hora).
 * @param erros Lista de erros de validação, cada um representando um campo e
 *              sua respectiva mensagem de erro.
 */
@Schema(description = "${erro.descricao.schema.validacao}")
public record ErroValidacaoDTO(

        @Schema(description = "${erro.descricao.validacao.erro}") //
        ErroPadraoDTO erro,

        @ArraySchema(schema = @Schema(description = "${erro.descricao.validacao.erros}")) //
        List<CampoMensagemDTO> erros) {

    /**
     * Construtor customizado que inicializa o objeto com as informações básicas do
     * erro e prepara a lista de erros de validação vazia.
     *
     * @param statusHTTP Status HTTP do erro.
     * @param titulo     Título do erro.
     * @param mensagem   Mensagem descritiva do erro.
     * @param rota       Rota em que o erro ocorreu.
     */
    public ErroValidacaoDTO(HttpStatus statusHTTP, String titulo, String mensagem, String rota) {
        this(new ErroPadraoDTO(statusHTTP, titulo, mensagem, rota), new ArrayList<>());
    }

    /**
     * Adiciona um erro de validação à lista de erros.
     *
     * @param campo    Nome do campo onde ocorreu o erro.
     * @param mensagem Mensagem descritiva do erro.
     */
    public void adicionarErro(String campo, String mensagem) {
        this.erros.add(new CampoMensagemDTO(campo, mensagem));
    }

    /**
     * Adiciona um erro de validação à lista de erros com base
     * em um FieldError do Spring Validation.
     *
     * @param erro O erro capturado pelo Spring.
     */
    public void adicionarErro(FieldError erro) {
        this.adicionarErro(erro.getField(), erro.getDefaultMessage());
    }
}
