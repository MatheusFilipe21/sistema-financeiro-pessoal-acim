# language: pt
Funcionalidade: Cadastro de Novo Usuário

  Como um novo usuário do sistema
  Eu quero me cadastrar com sucesso
  Para poder acessar a minha conta

  @cadastro
  Cenário: Cadastro bem-sucedido com dados válidos
    Dado que estou na página de cadastro
    Quando preencho "MATHEUS FILIPE DO NASCIMENTO PEREIRA", o email gerado, e senhas "Ab123456" e "Ab123456"
    E clico no botão "Cadastrar"
    Então uma mensagem de sucesso deve ser exibida com o texto "Usuário MATHEUS FILIPE DO NASCIMENTO PEREIRA cadastrado com sucesso!"

  @cadastro
  Cenário: Tentativa de cadastro com email duplicado
    Dado que já existe um usuário cadastrado com o email gerado
    Quando tento me cadastrar novamente com o mesmo email gerado
    Então deve ser exibido um erro com título "Violação de Dados" e mensagem "O e-mail: {email} já está cadastrado."