# language: pt
Funcionalidade: Cadastro de Novo Usuário

  Para permitir que novos usuários acessem o sistema
  Eu, como um cliente de API,
  Quero enviar dados válidos para o endpoint de cadastro
  Para criar uma nova conta e receber os detalhes do usuário criado.

  @limparUsuarios
  Cenário: CT001 - Cadastro de usuário com todos os campos válidos
    Dado que eu tenho os seguintes dados para o novo usuário
      """
      {
        "nome": "Matheus Filipe do Nascimento Pereira",
        "email": "matheusfnpereira@gmail.com",
        "senha": "Ab123456"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/cadastro"
    Então o status da resposta deve ser 201
    E o corpo da resposta deve conter o campo "id"
    E o campo "email" na resposta deve ser "matheusfnpereira@gmail.com"

  @limparUsuarios
  Cenário: CT002 - Tentativa de cadastro com email duplicado
    Dado que já existe um usuário cadastrado com email "matheusfnpereira@gmail.com"
    E que eu tenho os seguintes dados para o novo usuário
      """
      {
        "nome": "Matheus Filipe do Nascimento Pereira",
        "email": "matheusfnpereira@gmail.com",
        "senha": "Ab123456"
      }
      """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/cadastro"
    Então o status da resposta deve ser 400
    E o campo "titulo" na resposta deve ser "Dados Inválidos"
    E o campo "mensagem" na resposta deve ser "O e-mail: matheusfnpereira@gmail.com já está cadastrado."
    E o campo "rota" na resposta deve ser "/api/autenticacao/cadastro"
    E o corpo da resposta deve conter o campo "dataHora"

    Cenário: CT003 - Tentativa de cadastro com campos inválidos
    Dado que eu tenho os seguintes dados para o novo usuário
        """
        {
            "nome": "Matheus",
            "email": "email.invalido@",
            "senha": "senha_invalida"
        }
        """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/cadastro"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/cadastro"
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "email"
    E o campo "erros.mensagem" deve conter o item "O formato do e-mail é inválido."
    E o campo "erros.campo" deve conter o item "senha"
    E o campo "erros.mensagem" deve conter o item "A senha deve ter no mínimo 8 caracteres, contendo ao menos uma letra maiúscula, uma minúscula e um número."

Cenário: CT004 - Validar campos obrigatórios
    Dado que eu tenho os seguintes dados para o novo usuário
        """
        {
            "nome": "",
            "email": "",
            "senha": ""
        }
        """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/cadastro"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/cadastro"
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "email"
    E o campo "erros.mensagem" deve conter o item "O e-mail é obrigatório."
    E o campo "erros.campo" deve conter o item "senha"
    E o campo "erros.mensagem" deve conter o item "A senha deve ter no mínimo 8 caracteres, contendo ao menos uma letra maiúscula, uma minúscula e um número."
    E o campo "erros.campo" deve conter o item "senha"
    E o campo "erros.mensagem" deve conter o item "A senha é obrigatória."
    E o campo "erros.campo" deve conter o item "nome"
    E o campo "erros.mensagem" deve conter o item "O nome é obrigatório."

Cenário: CT005 - Validar regra da senha
    Dado que eu tenho os seguintes dados para o novo usuário
        """
        {
            "nome": "Ilka Berenguer",
            "email": "ifbp@cesar.school",
            "senha": "abdf1234"
        }
        """
    Quando o cliente faz uma requisição POST para "/api/autenticacao/cadastro"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o campo "erro.rota" na resposta deve ser "/api/autenticacao/cadastro"
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "senha"
    E o campo "erros.mensagem" deve conter o item "A senha deve ter no mínimo 8 caracteres, contendo ao menos uma letra maiúscula, uma minúscula e um número."
