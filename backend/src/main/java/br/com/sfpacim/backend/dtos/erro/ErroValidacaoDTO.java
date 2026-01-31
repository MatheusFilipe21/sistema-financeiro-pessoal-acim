package br.com.sfpacim.backend.dtos.erro;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa um erro de validação contendo uma lista de campos com mensagens de
 * erro específicas.
 *
 * @author Matheus F. N. Pereira
 */
@Schema(description = "Representa um erro de validação contendo detalhes sobre os campos inválidos.")
public class ErroValidacaoDTO {

    /**
     * Contém os dados padrão do erro (Status, Título, Mensagem, Rota, Data/Hora).
     */
    private final ErroPadraoDTO erro;

    /**
     * Lista de erros de validação, cada um representando um campo e sua respectiva
     * mensagem de erro.
     */
    @ArraySchema(schema = @Schema(description = "Lista de erros de validação.", example = "[{\"campo\": \"email\", \"mensagem\": \"O email é inválido.\"}]"))
    private final List<CampoMensagemDTO> erros = new ArrayList<>();

    /**
     * Construtor que inicializa o objeto com as informações básicas do erro.
     *
     * @param statusHTTP Status HTTP do erro.
     * @param titulo     Título do erro.
     * @param mensagem   Mensagem descritiva do erro.
     * @param rota       Rota em que o erro ocorreu.
     */
    public ErroValidacaoDTO(HttpStatus statusHTTP, String titulo, String mensagem, String rota) {
        this.erro = new ErroPadraoDTO(statusHTTP, titulo, mensagem, rota);
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
     */
    public void adicionarErro(FieldError erro) {
        this.adicionarErro(erro.getField(), erro.getDefaultMessage());
    }

    /**
     * Obtém o objeto de erro padrão contendo as informações
     * básicas do erro (Status, Título, Mensagem, Rota, Data/Hora).
     *
     * @return O DTO {@link ErroPadraoDTO} associado.
     */
    public ErroPadraoDTO getErro() {
        return erro;
    }

    /**
     * Obtém a lista de erros de validação detalhados por campo.
     *
     * @return A lista de {@link CampoMensagemDTO}.
     */
    public List<CampoMensagemDTO> getErros() {
        return erros;
    }
}
