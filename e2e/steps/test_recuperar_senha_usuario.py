from pytest_bdd import scenarios, given, when, then

from services.recuperar_senha_service import RecuperarSenhaService
from services.base_service import BaseService

from steps.comum_steps import *

scenarios('../features/recuperar_senha_usuario.feature')


@given('que estou na página de recuperar senha')
def step_given_estou_na_pagina_de_recuperar_senha(recuperar_senha_service: RecuperarSenhaService) -> None:
    """
    Navega para a página de recuperar senha.

    Args:
        recuperar_senha_service: Fixture injetada do serviço de recuperação de senha.

    :author: Alexandre Orlando Gracio
    """
    recuperar_senha_service.navegar_para_recuperar_senha()


@when('preencho com o email gerado')
def step_when_preencho_sucesso(recuperar_senha_service: RecuperarSenhaService, massa_dados: dict) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado na massa de dados.

    Args:
        recuperar_senha_service: Fixture injetada do serviço de recuperação de senha.
        massa_dados: Dicionário contendo os dados gerados para o cenário.

    :author: Alexandre Orlando Gracio
    """
    recuperar_senha_service.recuperar_senha_page.preencher_formulario(
        email=massa_dados['email_gerado']
    )


@then('a mensagem deve conter o email gerado')
def step_then_mensagem_sucesso(base_service: BaseService, massa_dados: dict, contexto_teste: dict) -> None:
    """
    Verifica se a mensagem do dialog de sucesso contém o e-mail gerado.

    Args:
        base_service: Fixture para acesso a validações globais (dialogs).
        massa_dados: Dicionário contendo o e-mail gerado.
        contexto_teste: Dicionário para recuperar tipos/títulos salvos em steps anteriores.

    :author: Alexandre Orlando Gracio
    """
    tipo_salvo = contexto_teste.get('ultimo_tipo_dialog', 'sucesso')
    titulo_salvo = contexto_teste.get('ultimo_titulo_dialog', '')

    base_service.verificar_dialog_global(
        tipo_dialog=tipo_salvo,
        titulo_esperado=titulo_salvo,
        mensagem_esperada=massa_dados['email_gerado'],
        ignore_mensagem=False
    )


@when('preencho o formulário de recuperar senha com', target_fixture="datatable")
def step_when_preencho_tabela(recuperar_senha_service: RecuperarSenhaService, datatable: list[list[str]]) -> list[list[str]]:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | email           |
      | email.invalido@ |

    Args:
        recuperar_senha_service: Fixture do serviço de recuperação de senha.
        datatable: Tabela de dados do Gherkin injetada automaticamente.

    :author: Alexandre Orlando Gracio
    """
    cabecalhos = datatable[0]
    valores = datatable[1]

    tabela_mapeada = dict(zip(cabecalhos, valores))

    recuperar_senha_service.preencher_campos_dinamicos(tabela_mapeada)

    return datatable
