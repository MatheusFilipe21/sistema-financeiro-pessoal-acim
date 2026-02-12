# language: pt
Funcionalidade: Login de Usuário
  Como um usuário cadastrado do sistema,
  Eu quero fazer login com sucesso,
  Para poder acessar meu dashboard financeiro.

  Contexto:
    Dado que estou na página de login

  @login
  Cenário: CT001 - Login bem-sucedido com dados válidos
    Dado que realizo um cadastro com sucesso com nome padrão, email gerado e a senha padrão
    E que estou na página de login
    Quando preencho o email gerado e a senha padrão
    E clico no botão "Entrar"
    Então devo ser direcionado para a página de "dashboard"

  Cenário: CT002 - Login de usuário com e-mail inválido
    Quando preencho o formulário de login com
      | email           | senha    |
      | email.invalido@ | senha123 |
    Então deve ser exibida a mensagem de erro "Formato de e-mail inválido." no campo "email"
    E o botão "Entrar" deve estar "desabilitado"

  Cenário: CT003 - Login de usuário com senha inválida
    Quando preencho o formulário de login com
      | email                      | senha    |
      | matheusfnpereira@gmail.com | 123456Ja |
    E clico no botão "Entrar"
    Então deve ser exibido um dialog de "erro" com título "Falha na Autenticação"
    E a mensagem do dialog deve conter "E-mail ou senha inválidos."

  Cenário: CT004 - Login com campos em branco
    Quando preencho o formulário de login com
      | email | senha |
      |       |       |
    Então deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "email"
    E deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "senha"
    E o botão "Entrar" deve estar "desabilitado"
