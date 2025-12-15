package br.com.sfpacim.backend.doc;

/**
 * Classe utilitária (constante) para armazenar exemplos de JSON
 * usados na documentação do Swagger (OpenAPI).
 * 
 * <p>
 * Centraliza os payloads de erro para garantir consistência na
 * documentação da API.
 *
 * @author Matheus F. N. Pereira
 */
public final class ExemplosDocumentacao {

    // Previne a instanciação
    private ExemplosDocumentacao() {

    }

    /**
     * Exemplo de resposta para Erro 401 (Unauthorized).
     */
    public static final String ERRO_401 = """
            {
              "status": 401,
              "titulo": "Não Autorizado",
              "mensagem": "Falha na autenticação. Token inválido ou expirado.",
              "dataHora": "11/11/2025 08:00",
              "rota": "/api/recurso"
            }
            """;

    /**
     * Exemplo de resposta para Erro 404 (Not Found).
     */
    public static final String ERRO_404 = """
            {
              "status": 404,
              "titulo": "Recurso Não Encontrado",
              "mensagem": "O recurso solicitado não foi encontrado ou você não tem permissão para acessá-lo.",
              "dataHora": "11/11/2025 08:00",
              "rota": "/api/recurso/123"
            }
            """;

    /**
     * Exemplo de resposta para Erro 409 (Conflict).
     */
    public static final String ERRO_409 = """
            {
              "status": 409,
              "titulo": "Conflito de Dados",
              "mensagem": "Já existe um registro cadastrado com estes dados (ex: e-mail, nome).",
                "dataHora": "11/11/2025 08:00",
              "rota": "/api/recurso"
            }
            """;

    /**
     * Exemplo de resposta para Erro 422 (Unprocessable Entity - Falha de
     * Validação).
     */
    public static final String ERRO_422 = """
            {
              "erro": {
                "status": 422,
                "titulo": "Erro de Validação",
                "mensagem": "Um ou mais campos estão inválidos.",
                "dataHora": "11/11/2025 08:00",
                "rota": "/api/recurso"
              },
              "erros": [
                {
                  "campo": "nome_do_campo",
                  "mensagem": "A mensagem de erro da validação (ex: obrigatório, tamanho inválido)."
                }
              ]
            }
            """;

    /**
     * Exemplo de resposta para Erro 500 (Internal Server Error).
     */
    public static final String ERRO_500 = """
            {
              "status": 500,
              "titulo": "Erro Interno",
              "mensagem": "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.",
              "dataHora": "11/11/2025 08:00",
              "rota": "/api/autenticacao/cadastro"
            }
            """;
}
