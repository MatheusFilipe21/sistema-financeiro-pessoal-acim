# language: pt
Funcionalidade: Recuperar Senha
  Como um usuário do sistema
  Eu quero solicitar a recuperação da minha senha
  Para receber um link de redefinição por e-mail

  Contexto:
    Dado que estou na página de recuperar senha

  @recuperarSenha
  Cenário: CT001 - Solicitar envio do link com e-mail válido
    Quando preencho com o email gerado
    E clico no botão "Enviar Link"
    Então deve ser exibido um dialog de "sucesso" com título "E-mail Enviado"
    E a mensagem deve conter o email gerado
    Quando clico no botão "Ok"
    Então devo ser direcionado para a página de "login"

  Cenário: CT002 - Validação de campo obrigatório para recuperação de senha
    Quando preencho o formulário de recuperar senha com
      | email |
      |       |
    Então deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "email"
    E o botão "Enviar Link" deve estar "desabilitado"

  @recuperarSenha
  Cenário: CT003 - Bloquear envio do link enquanto o e-mail for inválido
    Quando preencho o formulário de recuperar senha com
      | email           |
      | email.invalido@ |
    Então deve ser exibida a mensagem de erro "Formato de e-mail inválido." no campo "email"
    E o botão "Enviar Link" deve estar "desabilitado"
