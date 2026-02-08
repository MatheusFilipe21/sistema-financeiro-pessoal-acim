# language: pt
Funcionalidade: Login de usuário
  Para permitir que usuários autenticados acessem o sistema
  Eu, como um cliente de API,
  Quero enviar credenciais válidas para o endpoint de login
  Para receber um token de autenticação.

  @limparUsuarios
  Cenário: CT001 - Login com credenciais válidas retorna token
    Dado que já existe um usuário cadastrado com nome "Alexandre Orlando Gracio" e email "aog@cesar.school"
    E que eu tenho os seguintes dados para o login do usuário
      """
      {
        "email": "aog@cesar.school",
        "senha": "Ab123456"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/login"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve conter o campo "token"

  Cenário: CT002 - Login com e-mail em formato inválido retorna erro 422
    Dado que eu tenho os seguintes dados para o login do usuário
      """
      {
        "email": "email.invalido@",
        "senha": "Ab123456"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/login"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/login"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "email"
    E o campo "erros.mensagem" deve conter o item "O formato do e-mail é inválido."

  @limparUsuarios
  Cenário: CT003 - Login com senha inválida retorna erro 401
    Dado que já existe um usuário cadastrado com nome "Alexandre Orlando Gracio" e email "aog@cesar.school"
    E que eu tenho os seguintes dados para o login do usuário
      """
      {
        "email": "aog@cesar.school",
        "senha": "senha_invalida"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/login"
    Então o status da resposta deve ser 401
    E o campo "titulo" na resposta deve ser "Falha na Autenticação"
    E o campo "mensagem" na resposta deve ser "E-mail ou senha inválidos."
    E o corpo da resposta deve conter o campo "dataHora"
    E o campo "rota" na resposta deve ser "/api/autenticacao/login"

  Cenário: CT004 - Login com campos vazios retorna erro 422
    Dado que eu tenho os seguintes dados para o login do usuário
      """
      {
        "email": "",
        "senha": ""
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/login"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/login"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "email"
    E o campo "erros.mensagem" deve conter o item "O e-mail é obrigatório."
    E o campo "erros.campo" deve conter o item "senha"
    E o campo "erros.mensagem" deve conter o item "A senha é obrigatória."
