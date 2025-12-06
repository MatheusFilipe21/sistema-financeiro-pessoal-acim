# language: pt
Funcionalidade: Cadastro de Novo Usuário

  Como um novo usuário do sistema
  Eu quero me cadastrar com sucesso
  Para poder acessar a minha conta

  @cadastro
  Cenario: Cadastro bem-sucedido com dados validos
    Dado que estou na pagina de cadastro
    Quando preencho "MATHEUS FILIPE DO NASCIMENTO PEREIRA", o email gerado, e senhas "Ab123456" e "Ab123456"
    E clico no botao "Cadastrar"
    Entao uma mensagem de sucesso deve ser exibida com o texto "Usuário MATHEUS FILIPE DO NASCIMENTO PEREIRA cadastrado com sucesso!"
