package br.com.sfpacim.backend.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sfpacim.backend.models.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Serviço responsável por gerenciar Tokens JWT (Geração e Validação).
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class TokenService {

    /**
     * A chave secreta usada para assinar o token.
     */
    @Value("${api.security.token.secret}")
    private String chaveSecreta;

    /**
     * O tempo de expiração do token.
     */
    @Value("${api.security.token.expiration-ms}")
    private Long tempoExpiracaoMs;

    /**
     * Gera um novo Token JWT assinado para um usuário autenticado.
     *
     * @param usuario A entidade {@link Usuario} autenticada.
     * @return Uma string (o Token JWT).
     */
    public String gerarToken(Usuario usuario) {
        SecretKey chave = this.getChaveDeAssinatura();
        Instant agora = LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"));
        Instant expiracao = agora.plusMillis(this.tempoExpiracaoMs);

        return Jwts.builder()
                .issuer("SFP-ACIM API")
                .subject(usuario.getEmail())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();
    }

    /**
     * Gera um Token JWT específico para recuperação de senha com validade curta.
     * 
     * <p>
     * Utiliza uma chave de assinatura dinâmica composta pelo segredo da API
     * e pela senha atual do usuário. Isso garante que, caso a senha seja alterada,
     * este token seja invalidado automaticamente, impedindo reuso.
     *
     * @param usuario A entidade {@link Usuario} que solicitou a recuperação.
     * @return O Token JWT assinado com validade de 30 minutos.
     */
    public String gerarTokenRecuperacao(Usuario usuario) {
        SecretKey chaveDinamica = this.getChaveDeAssinaturaDinamica(usuario.getSenha());
        Instant agora = LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"));
        Instant expiracao = agora.plus(4, ChronoUnit.HOURS);

        return Jwts.builder()
                .issuer("SFP-ACIM API")
                .subject(usuario.getEmail())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chaveDinamica)
                .compact();
    }

    /**
     * Valida um Token JWT (usado pelo Filtro JWT).
     *
     * @param token O token (String) vindo do cabeçalho Authorization.
     * @return O "subject" (e-mail) do usuário se o token for válido.
     * @throws io.jsonwebtoken.JwtException Se o token for inválido, expirado
     *                                      ou a assinatura falhar.
     */
    public String validarToken(String token) {
        SecretKey chave = this.getChaveDeAssinatura();

        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Valida um Token JWT de recuperação verificando sua integridade e autoria.
     * 
     * <p>
     * Reconstrói a chave de assinatura usando a senha atual do usuário fornecido.
     * Se a senha tiver sido alterada desde a emissão do token, a assinatura não
     * corresponderá e uma exceção será lançada.
     *
     * @param token   O token JWT recebido na requisição.
     * @param usuario O usuário recuperado do banco de dados.
     * @return O "subject" (e-mail) do usuário, caso o token seja válido.
     */
    public String validarTokenRecuperacao(String token, Usuario usuario) {
        SecretKey chaveDinamica = this.getChaveDeAssinaturaDinamica(usuario.getSenha());

        return Jwts.parser()
                .verifyWith(chaveDinamica)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Helper (auxiliar) que extrai o "subject" (e-mail) do Token sem verificar a
     * assinatura.
     * 
     * <p>
     * Este método é necessário para para buscar o usuário e obter sua senha atual,
     * para só então reconstruir a chave dinâmica e validar a assinatura real.
     *
     * @param token O token JWT completo.
     * @return O e-mail contido no payload ou null se a leitura falhar.
     */
    public String obterEmailDoToken(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length < 2) {
                return null;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);

            return new ObjectMapper().readTree(payloadJson).get("sub").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper (auxiliar) que converte a chave secreta (String)
     * do .yml para o formato SecretKey (HMAC-SHA) exigido pela
     * biblioteca jjwt.
     */
    private SecretKey getChaveDeAssinatura() {
        return Keys.hmacShaKeyFor(this.chaveSecreta.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Helper (auxiliar) que converte a chave secreta da aplicação combinada
     * com a senha do usuário para o formato SecretKey (HMAC-SHA).
     * 
     * <p>
     * Essa combinação cria uma assinatura única atrelada ao estado atual da senha.
     *
     * @param senhaUsuario A senha criptografada (hash) atual do usuário.
     * @return A chave HMAC pronta para uso no JJWT.
     */
    private SecretKey getChaveDeAssinaturaDinamica(String senhaUsuario) {
        String segredoCombinado = this.chaveSecreta + senhaUsuario;
        return Keys.hmacShaKeyFor(segredoCombinado.getBytes(StandardCharsets.UTF_8));
    }
}
