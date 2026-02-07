# language: pt
Funcionalidade: Redefinição de Senha

Para permitir que usuários redefinam a senha
Eu, como um cliente de API,
Quero enviar dados para os endpoints de redefinição de senha.
Para que eu possa recuperar o acesso à minha conta de forma segura.

@limparUsuarios 
Cenário: CT001 - Redefinição de senha com campos válidos
    Dado que já existe um usuário cadastrado com nome "Catherine Aussourd" e email "caatmarie@gmail.com"
    E que eu tenho o seguinte dado com o token gerado para o usuário de email "caatmarie@gmail.com" e a senha "Ab7654321"
    Quando o cliente faz uma requisição POST para "/api/autenticacao/redefinir-senha"
    Então o status da resposta deve ser 204
    E o corpo da resposta deve ser vazio
    
Cenário: CT002 - Redefinição de senha com token inválido ou expirado
    Dado que eu tenho o seguinte dado para redefinir senha
      """
      {
        "token": "token_invalido",
        "senha": "Ab7654321"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/redefinir-senha"
    Então o status da resposta deve ser 422
    E o campo "titulo" na resposta deve ser "Operação Não Permitida"
    E o campo "mensagem" na resposta deve ser "Token inválido ou expirado."
    E o corpo da resposta deve conter o campo "dataHora"
    E o campo "rota" na resposta deve ser "/api/autenticacao/redefinir-senha"

Cenário: CT003 - Validar campos obrigatórios
    Dado que eu tenho o seguinte dado para redefinir senha
      """
      {
        "token": "",
        "senha": ""
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/redefinir-senha"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/redefinir-senha"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "token"
    E o campo "erros.mensagem" deve conter o item "O token é obrigatório"
    E o campo "erros.campo" deve conter o item "senha"
    E o campo "erros.mensagem" deve conter o item "A senha é obrigatória."
    E o campo "erros.mensagem" deve conter o item "A senha deve ter no mínimo 8 caracteres, contendo ao menos uma letra maiúscula, uma minúscula e um número."
    