package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO com os dados necessários para efetivar a troca de senha.
 *
 * @param token O token JWT recebido por e-mail.
 * @param senha A nova senha desejada (deve seguir as regras de
 *              complexidade).
 */
public record DadosRedefinicaoSenhaDTO(

        /**
         * O token é obrigatório.
         */
        @Schema(description = "Token de recuperação Bearer (JWT).", example = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2NTExMTMzMCwiZXhwIjoxNzY1MTI1NzMwfQ.MB0urXWxU4Hr4J3XA61x_RfzKmNzbs3uJ5rwVStnzg8oCfHYlgN-vsNrrU7CKYNnfs8MvUrMnK1Vw2Dkbr2pPg") //
        @NotBlank(message = "O token é obrigatório") //
        String token,

        /**
         * A senha deve ser complexa.
         * A senha é obrigatória.
         * Regra: Mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número.
         */
        @Schema(description = "Senha de acesso (mínimo 8 caracteres, 1 maiúscula, 1 minúscula, 1 número).", example = "Ab1234567") //
        @NotBlank(message = "A senha é obrigatória.") //
        @Pattern(regexp = "^(?=.*[\\d])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "A senha deve ter no mínimo 8 caracteres, contendo ao menos uma letra maiúscula, uma minúscula e um número.") //
        String senha) {
}
