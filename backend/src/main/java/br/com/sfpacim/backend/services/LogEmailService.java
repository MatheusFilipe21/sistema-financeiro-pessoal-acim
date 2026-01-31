package br.com.sfpacim.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.services.interfaces.EmailService;

/**
 * Implementação de {@link EmailService} para ambiente de desenvolvimento.
 * 
 * <p>
 * Apenas registra o envio no console (Log) sem realizar disparo real,
 * evitando bloqueios ou custos durante testes.
 *
 * @author Matheus F. N. Pereira
 */
@Slf4j
@Service
@Profile("dev")
public class LogEmailService implements EmailService {

    @Override
    public void enviar(String destinatario, String assunto, String mensagem) {
        log.info("--------------------------------------------------");
        log.info("📧 [SIMULAÇÃO DE ENVIO DE E-MAIL]");
        log.info("Para: {}", destinatario);
        log.info("Assunto: {}", assunto);
        log.info("Mensagem: \n{}", mensagem);
        log.info("--------------------------------------------------");
    }
}
