# language: pt
Funcionalidade: Cadastro de Novo Usuário

  Para permitir que novos usuários acessem o sistema
  Eu, como um cliente de API,
  Quero enviar dados válidos para o endpoint de cadastro
  Para criar uma nova conta e receber os detalhes do usuário criado.

  @limparUsuarios
  Cenário: Cadastro de usuário com todos os campos válidos
    Dado que eu tenho os seguintes dados válidos para o novo usuário
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
