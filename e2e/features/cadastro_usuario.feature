# language: pt
Funcionalidade: Cadastro de Usuário
  Como um novo usuário do sistema
  Eu quero me cadastrar com sucesso
  Para poder acessar a minha conta

  Contexto:
    Dado que estou na página de cadastro

  @cadastro
  Cenário: CT001 - Cadastro bem-sucedido com dados válidos
    Dado que estou na página de cadastro
    Quando preencho "MATHEUS FILIPE DO NASCIMENTO PEREIRA", o email gerado, e senhas "Ab123456" e "Ab123456"
    E clico no botão "Cadastrar"
    Então deve ser exibido um dialog de "sucesso" com título "Cadastro realizado com sucesso!"
    E a mensagem deve conter o texto "O usuário MATHEUS FILIPE DO NASCIMENTO PEREIRA foi cadastrado"

  @cadastro
  Cenário: CT002 - Tentativa de cadastro com email duplicado
    Dado que já existe um usuário cadastrado com o email gerado
    Quando tento me cadastrar novamente com o mesmo email gerado
    Então deve ser exibido um dialog de "erro" com título "Dados Inválidos"
    E a mensagem do dialog deve ser "O e-mail: {email} já está cadastrado."

  @cadastro
  Cenário: CT003 - Validação de formato de dados inválidos
    Quando preencho o formulário de cadastro com
      | nome    | email           | senha          | confirmar_senha |
      | Matheus | email.invalido@ | senha_invalida | senha_invalida2 |
    Então deve ser exibida a mensagem de erro "Formato de e-mail inválido." no campo "email"
    E deve ser exibida a mensagem de erro "A senha não atende aos requisitos mínimos." no campo "senha"
    E deve ser exibida a mensagem de erro "As senhas não conferem." no campo "confirmar-senha"
    E o botão "Cadastrar" deve estar "desabilitado"

  @cadastro
  Cenário: CT004 - Validação de campos obrigatórios
    Quando preencho o formulário de cadastro com
      | nome | email | senha | confirmar_senha |
      |      |       |       |                 |
    Então deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "nome"
    E deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "email"
    E deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "senha"
    E deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "confirmar-senha"
    E o botão "Cadastrar" deve estar "desabilitado"
