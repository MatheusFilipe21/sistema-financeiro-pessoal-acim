# language: pt
Funcionalidade: Recuperação de Senha

Para permitir que usuários recuperem a senha
Eu, como um cliente de API,
Quero enviar dados para os endpoints de recuperação de senha.
Para que eu possa recuperar o acesso à minha conta de forma segura.

Cenário: CT001 - Recuperação de senha ao inserir um e-mail 
    Dado que já existe um usuário cadastrado com nome "Catherine Aussourd" e email "caatmarie@gmail.com"
    E que eu tenho o seguinte dado para recuperar senha
      """
      {
        "email": "caatmarie@gmail.com"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/recuperar-senha"
    Então o status da resposta deve ser 204
    E o corpo da resposta deve ser vazio

Cenário: CT002 - Validação de campo obrigatório para recuperação de senha
    Dado que eu tenho o seguinte dado para recuperar senha
      """
      {
        "email": ""
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/recuperar-senha"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/recuperar-senha"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "email"
    E o campo "erros.mensagem" deve conter o item "O e-mail é obrigatório."
    