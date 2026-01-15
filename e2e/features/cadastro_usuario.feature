# language: pt
Funcionalidade: Cadastro de Novo Usuário

  Como um novo usuário do sistema
  Eu quero me cadastrar com sucesso
  Para poder acessar a minha conta

  @cadastro
  Cenário: CT001 - Cadastro bem-sucedido com dados válidos
    Dado que estou na página de cadastro
    Quando preencho "MATHEUS FILIPE DO NASCIMENTO PEREIRA", o email gerado, e senhas "Ab123456" e "Ab123456"
    E clico no botão "Cadastrar"
    Então deve ser exibido uma mensagem de sucesso com título "Cadastro realizado com sucesso!" e mensagem "O usuário MATHEUS FILIPE DO NASCIMENTO PEREIRA foi cadastrado, acesse a tela de login ou clique no OK para ser redirecionado e acessar o sistema."

  @cadastro
  Cenário: CT002 - Tentativa de cadastro com email duplicado
    Dado que já existe um usuário cadastrado com o email gerado
    Quando tento me cadastrar novamente com o mesmo email gerado
    Então deve ser exibido um erro com título "Dados Inválidos" e mensagem "O e-mail: {email} já está cadastrado."