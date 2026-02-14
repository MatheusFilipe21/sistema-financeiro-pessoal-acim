# language: pt
Funcionalidade: Gestão de Pessoas
  Para permitir que usuários autenticados gerenciem suas pessoas,
  Eu, como um cliente de API,
  Quero enviar dados para os endpoints de pessoas,
  Para que eu possa criar, recuperar, atualizar e deletar as pessoas da minha conta.

  Contexto: Garantir que o usuário está cadastrado e autenticado
    Dado que já existe um usuário cadastrado com nome "Ilka Berenguer" e email "ilkafb@hotmail.com"
    E que estou autenticado como o usuário de email "ilkafb@hotmail.com"

  @limparUsuarios
  Cenário: CT001 - Cadastro de pessoa com todos os campos válidos
    E que eu tenho os seguintes dados para cadastrar uma nova pessoa
      """
      {
        "nome": "Ilka Fernanda Berenguer",
        "titular": true
      }
      """
    Quando o cliente faz uma requisição POST para "/api/pessoas"
    Então o status da resposta deve ser 201
    E o corpo da resposta deve conter o campo "id"
    E o campo "nome" na resposta deve ser "Ilka Fernanda Berenguer"
    E o campo "titular" na resposta deve ser "true"

  @limparUsuarios
  Cenário: CT002 - Validar campos obrigatórios
    Dado que eu tenho os seguintes dados para a nova pessoa
      """
      {
        "nome": ""
      }
      """
    Quando o cliente faz uma requisição POST para "/api/pessoas"
    Então o status da resposta deve ser 422
    E o campo "erro.titulo" na resposta deve ser "Dados Inválidos"
    E o campo "erro.mensagem" na resposta deve ser "Um ou mais campos estão inválidos."
    E o campo "erro.rota" na resposta deve ser "/api/pessoas"
    E o corpo da resposta deve conter o campo "erro.dataHora"
    E o corpo da resposta deve conter o campo "erros"
    E o campo "erros.campo" deve conter o item "nome"
    E o campo "erros.mensagem" deve conter o item "O nome é obrigatório."

  @limparUsuarios
  Cenário: CT003 - Tentativa de cadastro nome duplicado
    Dado que já existe uma pessoa cadastrada com nome "Ilka Fernanda Berenguer" para o usuário com o email "ilkafb@hotmail.com"
    E que eu tenho os seguintes dados para a nova pessoa
      """
      {
        "nome": "Ilka Fernanda Berenguer",
        "titular": true
      }
      """
    Quando o cliente faz uma requisição POST para "/api/pessoas"
    Então o status da resposta deve ser 409
    E o campo "titulo" na resposta deve ser "Conflito de Dados"
    E o campo "mensagem" na resposta deve ser "Já existe uma pessoa cadastrada com o nome 'Ilka Fernanda Berenguer'."
    E o campo "rota" na resposta deve ser "/api/pessoas"
    E o corpo da resposta deve conter o campo "dataHora"
