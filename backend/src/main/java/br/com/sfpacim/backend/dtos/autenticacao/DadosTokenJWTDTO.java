package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Record) que representa a resposta JSON (corpo)
 * enviada após um login bem-sucedido.
 *
 * @author Matheus F. N. Pereira
 *
 * @param token O token JWT gerado.
 */
@Schema(description = "DTO de resposta do login, contendo o token JWT.")
public record DadosTokenJWTDTO(

        @Schema(description = "Token de autenticação Bearer (JWT).", example = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2MzMwNjE3NiwiZXhwIjoxNzYzMzM0OTc2fQ.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ") //
        String token) {
}