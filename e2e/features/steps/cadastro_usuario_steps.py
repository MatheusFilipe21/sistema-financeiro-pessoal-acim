from behave import given, when, then
from behave.runner import Context
from pages.cadastro_page import CadastroPage
from services.cadastro_service import CadastroService


@given('que estou na página de cadastro')
def step_given_estou_na_pagina_de_cadastro(context: Context) -> None:
    """
    Inicializa as camadas Page Object e Service e navega para a página.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_page = CadastroPage(context.driver, context.url_base)
    context.cadastro_service = CadastroService(context.cadastro_page)
    context.cadastro_service.navegar_para_cadastro()


@given('que já existe um usuário cadastrado com o email gerado')
def step_given_usuario_ja_cadastrado(context: Context) -> None:
    """
    Cadastra um usuário para usar o e-mail no banco de dados.

    :author: Matheus F. N. Pereira
    """
    step_given_estou_na_pagina_de_cadastro(context)

    email = context.email_gerado
    nome = context.nome_padrao
    senha = context.senha_padrao

    context.cadastro_page.preencher_formulario(nome, email, senha, senha)
    context.cadastro_page.clicar_cadastrar()

    context.cadastro_service.verificar_mensagem_sucesso(nome)

    context.cadastro_service.navegar_para_cadastro()


@when('preencho "{nome}", o email gerado, e senhas "{senha}" e "{confirmar_senha}"')
def step_when_preencho_formulario(context: Context, nome: str, senha: str, confirmar_senha: str) -> None:
    """
    Preenche os campos do formulário, injetando o e-mail gerado.

    Args:
        nome: Nome vindo do Gherkin.
        senha: Senha vinda do Gherkin.
        confirmar_senha: Confirmação de senha vinda do Gherkin.

    :author: Matheus F. N. Pereira
    """
    # Recupera o e-mail aleatório gerado pelo Faker no environment.py
    email = context.email_gerado

    context.cadastro_page.preencher_formulario(
        nome, email, senha, confirmar_senha)


@when('clico no botão "Cadastrar"')
def step_when_clico_em_cadastrar(context: Context) -> None:
    """
    Clica no botão de cadastrar.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_page.clicar_cadastrar()


@when('tento me cadastrar novamente com o mesmo email gerado')
def step_when_tento_cadastrar_novamente(context: Context) -> None:
    """
    Tenta cadastrar usando o mesmo e-mail do passo anterior.

    :author: Matheus F. N. Pereira
    """
    email_duplicado = context.email_gerado
    nome = context.nome_padrao
    senha = context.senha_padrao

    context.cadastro_page.preencher_formulario(
        nome, email_duplicado, senha, senha)
    context.cadastro_page.clicar_cadastrar()


@then('uma mensagem de sucesso deve ser exibida com o texto "Usuário {nome_esperado} cadastrado com sucesso!"')
def step_then_mensagem_sucesso_exibida(context: Context, nome_esperado: str) -> None:
    """
    Verifica a mensagem de sucesso chamando o Service.

    Args:
        nome_esperado: O nome do usuário esperado na mensagem de sucesso.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_service.verificar_mensagem_sucesso(nome_esperado)


@then('uma mensagem de erro deve ser exibida informando que o email ja esta cadastrado')
def step_then_mensagem_erro_duplicidade(context: Context) -> None:
    """
    Verifica se o Dialog de erro aparece com a mensagem correta.

    :author: Matheus F. N. Pereira
    """
    email_duplicado = context.email_gerado
    context.cadastro_service.verificar_mensagem_erro_email_duplicado(
        email_duplicado)


@then('deve ser exibido um erro com título "{titulo}" e mensagem "{mensagem}"')
def step_then_validar_erro_explicito(context: Context, titulo: str, mensagem: str) -> None:
    """
    Valida o erro global usando os textos fornecidos no Gherkin.
    Substitui o placeholder '{email}' pelo valor real gerado no teste.

    Args:
        titulo: O título esperado do Dialog.
        mensagem_template: A mensagem esperada, podendo conter '{email}'.

    :author: Matheus F. N. Pereira
    """
    email_real = context.email_gerado
    mensagem_formatada = mensagem.format(email=email_real)

    context.cadastro_service.verificar_erro_global(titulo, mensagem_formatada)
