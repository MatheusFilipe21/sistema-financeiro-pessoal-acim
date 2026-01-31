package br.com.sfpacim.backend.dtos.erro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Estrutura padrão para representar erros na API.
 *
 * @author Matheus F. N. Pereira
 */
@Schema(description = "Estrutura padrão para representar erros na API.")
public record ErroPadraoDTO(

        @Schema(description = "Código de status HTTP do erro.", example = "400") //
        Integer status,

        @Schema(description = "Título do erro.", example = "Violação de Dados") //
        String titulo,

        @Schema(description = "Mensagem detalhada do erro.", example = "O e-mail informado já está cadastrado.") //
        String mensagem,

        @Schema(description = "Data e hora do erro no formato dd/MM/yyyy HH:mm.", example = "11/11/2025 08:00") //
        String dataHora,

        @Schema(description = "Rota da requisição que gerou o erro.", example = "/api/autenticacao/cadastro") //
        String rota) {

    /**
     * Construtor customizado para inicializar todos os atributos
     * e formatar a data/hora automaticamente.
     *
     * @param statusHTTP Status HTTP.
     * @param titulo     Título do erro.
     * @param mensagem   Mensagem detalhada do erro.
     * @param rota       Rota da requisição.
     */
    public ErroPadraoDTO(HttpStatus statusHTTP, String titulo, String mensagem, String rota) {
        this(
                statusHTTP.value(),
                titulo,
                mensagem,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rota);
    }
}
