from behave import given, when, then
from behave.runner import Context
from pages.cadastro_page import CadastroPage
from services.cadastro_service import CadastroService


@given('que estou na pagina de cadastro')
def step_given_estou_na_pagina_de_cadastro(context: Context) -> None:
    """
    Inicializa as camadas Page Object e Service e navega para a página.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_page = CadastroPage(context.driver, context.url_base)
    context.cadastro_service = CadastroService(context.cadastro_page)
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


@when('clico no botao "Cadastrar"')
def step_when_clico_em_cadastrar(context: Context) -> None:
    """
    Clica no botão de cadastrar.

    :author: Matheus F. N. Pereira
    """
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
