from pytest_bdd import scenarios, given, when, parsers

from steps.comum_steps import *

from services.cadastro_service import CadastroService

scenarios('../features/cadastro_usuario.feature')


@given('que estou na página de cadastro')
def step_given_estou_na_pagina_de_cadastro(cadastro_service: CadastroService) -> None:
    """
    Navega para a página de cadastro.

    Inicializa o fluxo garantindo que o Service navegue para a URL correta
    definida na Page Object.

    Args:
        cadastro_service: Fixture injetada do serviço de cadastro.

    :author: Matheus F. N. Pereira
    """
    cadastro_service.navegar_para_cadastro()


@when(parsers.parse('preencho "{nome}", o email gerado, e senhas "{senha}" e "{confirmar_senha}"'))
def step_when_preencho_sucesso(cadastro_service: CadastroService, massa_dados: dict, nome: str, senha: str, confirmar_senha: str) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado na fixture de massa de dados.

    Args:
        cadastro_service: Fixture injetada do serviço de cadastro.
        massa_dados: Dicionário contendo os dados gerados isoladamente para o cenário atual.
        nome: Nome capturado do step Gherkin.
        senha: Senha capturada do step Gherkin.
        confirmar_senha: Confirmação de senha capturada do step Gherkin.

    :author: Matheus F. N. Pereira
    """
    cadastro_service.cadastro_page.preencher_formulario(
        nome=nome,
        email=massa_dados['email_gerado'],
        senha=senha,
        confirmar_senha=confirmar_senha
    )


@when('preencho o formulário de cadastro com', target_fixture="datatable")
def step_when_preencho_tabela(cadastro_service: CadastroService, datatable: list[list[str]]) -> None:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação (CT003) onde múltiplos cenários de dados
    são testados em sequência. O método lê a primeira linha da tabela injetada
    pelo pytest-bdd e a repassa para o Service.

    Exemplo Gherkin:
      | nome | email           | senha | confirmar_senha |
      | John | email.invalido@ | 123   | 123             |

    Args:
        cadastro_service: Fixture injetada do serviço de cadastro.
        datatable: Tabela de dados do Gherkin injetada automaticamente.

    :author: Alexandre Orlando Gracio
    """
    cabecalhos = datatable[0]
    valores = datatable[1]

    tabela_mapeada = dict(zip(cabecalhos, valores))

    cadastro_service.preencher_campos_dinamicos(tabela_mapeada)

    return datatable


@when('tento me cadastrar novamente com o mesmo email gerado')
def step_when_tento_cadastrar_duplicado(cadastro_service: CadastroService, massa_dados: dict) -> None:
    """
    Fluxo de exceção: Tenta realizar um novo cadastro utilizando exatamente
    os mesmos dados (principalmente o e-mail) gerados para o cenário.

    Args:
        cadastro_service: Fixture injetada do serviço de cadastro.
        massa_dados: Dicionário contendo os dados gerados isoladamente para o cenário atual.

    :author: Matheus F. N. Pereira
    """
    cadastro_service.realizar_cadastro(
        nome=massa_dados['nome_padrao'],
        email=massa_dados['email_gerado'],
        senha=massa_dados['senha_padrao'],
        confirmar_senha=massa_dados['senha_padrao']
    )
