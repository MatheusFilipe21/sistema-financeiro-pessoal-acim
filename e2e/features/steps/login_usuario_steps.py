from behave import given, when
from utils.contexto import Contexto


@given('que estou na página de login')
def step_given_estou_na_pagina_de_login(contexto: Contexto) -> None:
    """
    Navega para a página de login.

    Inicializa o fluxo garantindo que o Service navegue para a URL correta
    definida na Page Object.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Ilka Berenguer
    """
    contexto.login_service.navegar_para_login()


@when('preencho o email gerado e a senha padrão')
def step_when_preencho_sucesso(contexto: Contexto) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado no contexto e a senha padrão.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Ilka Berenguer
    """
    email = contexto.email_gerado
    senha = contexto.senha_padrao

    contexto.login_service.login_page.preencher_formulario(email, senha)


@when('preencho o formulário de login com')
def step_when_preencho_tabela(contexto: Contexto) -> None:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | email           | senha |
      | email.invalido@ | 123   |

    Args:
        contexto: O contexto de execução do Behave (contém contexto.table).

    :author: Ilka Berenguer
    """
    row = contexto.table[0]

    contexto.login_service.preencher_campos_dinamicos(row.as_dict())
