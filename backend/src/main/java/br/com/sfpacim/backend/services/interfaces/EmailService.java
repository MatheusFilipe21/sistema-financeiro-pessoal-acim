package br.com.sfpacim.backend.services.interfaces;

/**
 * Define o contrato para envio de e-mails na aplicação.
 * 
 * <p>
 * Permite alternar entre implementações reais (JavaMail) e simuladas (Log)
 * dependendo do perfil ativo.
 *
 * @author Matheus F. N. Pereira
 */
public interface EmailService {

    /**
     * Envia um e-mail simples.
     *
     * @param destinatario O endereço de e-mail do destinatário.
     * @param assunto      O título do e-mail.
     * @param mensagem     O corpo do e-mail.
     */
    void enviar(String destinatario, String assunto, String mensagem);
}
