from pytest_bdd import scenarios, given, when

from services.login_service import LoginService

from steps.comum_steps import *

scenarios('../features/login_usuario.feature')


@given('que estou na página de login')
def step_given_estou_na_pagina_de_login(login_service: LoginService) -> None:
    """
    Navega para a página de login.

    Args:
        login_service: Fixture injetada do serviço de login.

    :author: Ilka Berenguer
    """
    login_service.navegar_para_login()


@when('preencho o email gerado e a senha padrão')
def step_when_preencho_sucesso(login_service: LoginService, massa_dados: dict) -> None:
    """
    Preenche o formulário usando o e-mail randômico e a senha padrão da massa de dados.

    Args:
        login_service: Fixture injetada do serviço de login.
        massa_dados: Dicionário contendo os dados gerados para o cenário.

    :author: Ilka Berenguer
    """
    email = massa_dados['email_gerado']
    senha = massa_dados['senha_padrao']

    login_service.login_page.preencher_formulario(email, senha)


@when('preencho o formulário de login com', target_fixture="datatable")
def step_when_preencho_tabela(login_service: LoginService, datatable: list[list[str]]) -> list[list[str]]:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | email           | senha |
      | email.invalido@ | 123   |

    Args:
        login_service: Fixture injetada do serviço de login.
        datatable: Tabela de dados do Gherkin injetada automaticamente.

    :author: Ilka Berenguer
    """
    cabecalhos = datatable[0]
    valores = datatable[1]

    tabela_mapeada = dict(zip(cabecalhos, valores))

    login_service.preencher_campos_dinamicos(tabela_mapeada)

    return datatable
