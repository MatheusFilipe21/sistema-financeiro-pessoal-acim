package br.com.sfpacim.backend.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Classe de configuração para o Jackson (JSON).
 *
 * <p>
 * Define a personalização do {@link ObjectMapper} utilizado por toda a
 * aplicação, garantindo consistência na serialização e desserialização de
 * dados, especialmente para tipos modernos do Java e tratamento de erros.
 * </p>
 *
 * @author Matheus F. N. Pereira
 */
@Configuration
public class JacksonConfig {

    /**
     * Expõe o ObjectMapper customizado como um Bean primário gerenciado pelo
     * Spring.
     *
     * <p>
     * Esta configuração garante que:
     * 1. O suporte a tipos de data do Java 8+ (LocalDate, LocalDateTime) esteja
     * ativo.
     * 2. Datas não sejam enviadas como arrays numéricos (Timestamps), mas como
     * Strings formatadas.
     * 3. A aplicação não falhe ao receber campos desconhecidos no JSON (tolerância
     * a versões).
     * </p>
     *
     * @return Uma instância configurada do ObjectMapper.
     */
    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
