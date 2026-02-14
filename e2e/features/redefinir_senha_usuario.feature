# language: pt
Funcionalidade: Redefinir Senha
  Como um usuário com um token de recuperação válido
  Eu quero definir uma nova senha para minha conta
  Para que eu possa voltar a acessar o sistema com segurança

  Contexto: Obter o link de recuperação
    Dado que realizo um cadastro com sucesso com nome padrão, email gerado e a senha padrão
    E que solicitei a recuperação de senha para o email gerado
    E acesso o link de redefinição enviado por e-mail

  @redefinirSenha
  Cenário: CT001 - Redefinir senha com sucesso
    Quando preencho o formulário de redefinição com
      | senha         | confirmar_senha |
      | NovaSenha@123 | NovaSenha@123   |
    E clico no botão "Redefinir Senha"
    Então deve ser exibido um dialog de "sucesso" com título "Senha Alterada"
    E a mensagem do dialog deve conter "Sua senha foi redefinida com sucesso! Você já pode acessar sua conta."
    Quando clico no botão "Ok"
    Então devo ser direcionado para a página de "login"

  @redefinirSenha
  Cenário: CT002 - Redefinição de senha com token inválido ou expirado
    Dado que estou na tela de redefinição com token inválido
    Quando preencho o formulário de redefinição com
      | senha         | confirmar_senha |
      | NovaSenha@123 | NovaSenha@123   |
    E clico no botão "Redefinir Senha"
    Então deve ser exibido um dialog de "erro" com título "Operação Não Permitida"
    E a mensagem do dialog deve conter "Token inválido ou expirado."

  @redefinirSenha
  Cenário: CT003 - Validação de campos obrigatórios
    Quando preencho o formulário de redefinição com
      | senha | confirmar_senha |
      |       |                 |
    Então deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "senha"
    E deve ser exibida a mensagem de erro "Este campo é obrigatório." no campo "confirmar-senha"
    E o botão "Redefinir Senha" deve estar "desabilitado"

  @redefinirSenha
  Cenário: CT004 - Validação de senhas divergentes
    Quando preencho o formulário de redefinição com
      | senha         | confirmar_senha |
      | NovaSenha@123 | OutraSenha@123  |
    Então deve ser exibida a mensagem de erro "As senhas não conferem." no campo "confirmar-senha"
    E o botão "Redefinir Senha" deve estar "desabilitado"

  @redefinirSenha
  Cenário: CT005 - Validação de senha fraca
    Quando preencho o formulário de redefinição com
      | senha | confirmar_senha |
      | 123   | 123             |
    Então deve ser exibida a mensagem de erro "A senha não atende aos requisitos mínimos." no campo "senha"
    E o botão "Redefinir Senha" deve estar "desabilitado"
