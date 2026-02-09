from behave import given, when
from behave.runner import Context


@given('que estou na página de login')
def step_given_estou_na_pagina_de_login(context: Context) -> None:
    """
    Navega para a página de login.

    Inicializa o fluxo garantindo que o Service navegue para a URL correta
    definida na Page Object.

    Args:
        context: O contexto de execução do Behave.

    :author: Ilka Berenguer
    """
    context.login_service.navegar_para_login()


@when('preencho o email gerado e a senha padrão')
def step_when_preencho_sucesso(context: Context) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado no contexto e a senha padrão.

    Args:
        context: O contexto de execução do Behave.

    :author: Ilka Berenguer
    """
    email = context.email_gerado
    senha = context.senha_padrao

    context.login_service.login_page.preencher_formulario(email, senha)


@when('preencho o formulário de login com')
def step_when_preencho_tabela(context: Context) -> None:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | email           | senha |
      | email.invalido@ | 123   |

    Args:
        context: O contexto de execução do Behave (contém context.table).

    :author: Ilka Berenguer
    """
    row = context.table[0]

    context.login_service.preencher_campos_dinamicos(row.as_dict())
