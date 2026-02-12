from behave import given, when, then
from utils.contexto import Contexto


@given('que estou na página de recuperar senha')
def step_given_estou_na_pagina_de_recuperar_senha(contexto: Contexto) -> None:
    """
    Navega para a página de recuperar senha.

    Inicializa o fluxo garantindo que o Service navegue para a URL correta
    definida na Page Object.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Alexandre Orlando Gracio
    """
    contexto.recuperar_senha_service.navegar_para_recuperar_senha()


@when('preencho com o email gerado')
def step_when_preencho_sucesso(contexto: Contexto) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado no contexto.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Alexandre Orlando Gracio
    """
    contexto.recuperar_senha_page.preencher_formulario(
        email=contexto.email_gerado,
    )


@then('a mensagem deve conter o email gerado')
def step_when_mensagem_sucesso(contexto: Contexto) -> None:
    """
    Verifica se a mensagem do dialog de sucesso contém o e-mail gerado.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Alexandre Orlando Gracio
    """
    tipo_salvo = getattr(contexto, 'ultimo_tipo_dialog', 'mensagem')
    titulo_salvo = getattr(contexto, 'ultimo_titulo_dialog', '')

    contexto.base_service.verificar_dialog_global(
        tipo_dialog=tipo_salvo,
        titulo_esperado=titulo_salvo,
        mensagem_esperada=contexto.email_gerado,
        ignore_mensagem=False
    )


@when('preencho o formulário de recuperar senha com')
def step_when_preencho_tabela(contexto: Contexto) -> None:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | email           |
      | email.invalido@ |

    Args:
        contexto: O contexto de execução do Behave (contém contexto.table).

    :author: Alexandre Orlando Gracio
    """
    row = contexto.table[0]

    contexto.recuperar_senha_service.preencher_campos_dinamicos(row.as_dict())
