from behave import given, when, then
from utils.contexto import Contexto


@given('que estou na página de cadastro')
def step_given_estou_na_pagina_de_cadastro(contexto: Contexto) -> None:
    """
    Navega para a página de cadastro.

    Inicializa o fluxo garantindo que o Service navegue para a URL correta
    definida na Page Object.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    contexto.cadastro_service.navegar_para_cadastro()


@when('preencho "{}", o email gerado, e senhas "{}" e "{}"')
def step_when_preencho_sucesso(contexto: Contexto, nome: str, senha: str, confirmar_senha: str) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado no contexto.

    Args:
        contexto: O contexto de execução do Behave.
        nome: Nome vindo do step Gherkin.
        senha: Senha vinda do step Gherkin.
        confirmar_senha: Confirmação de senha vinda do step Gherkin.

    :author: Matheus F. N. Pereira
    """
    contexto.cadastro_service.cadastro_page.preencher_formulario(
        nome=nome,
        email=contexto.email_gerado,
        senha=senha,
        confirmar_senha=confirmar_senha
    )


@when('preencho o formulário de cadastro com')
def step_when_preencho_tabela(contexto: Contexto) -> None:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação (CT003) onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | nome | email           | senha | confirmar_senha |
      | John | email.invalido@ | 123   | 123             |

    Args:
        contexto: O contexto de execução do Behave (contém contexto.table).

    :author: Alexandre Orlando Gracio
    """
    row = contexto.table[0]

    contexto.cadastro_service.preencher_campos_dinamicos(row.as_dict())


@when('tento me cadastrar novamente com o mesmo email gerado')
def step_when_tento_cadastrar_duplicado(contexto: Contexto) -> None:
    """
    Fluxo de exceção: Tenta realizar um novo cadastro utilizando exatamente
    os mesmos dados (principalmente o e-mail) do @given anterior.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    contexto.cadastro_service.realizar_cadastro(
        nome=contexto.nome_padrao,
        email=contexto.email_gerado,
        senha=contexto.senha_padrao,
        confirmar_senha=contexto.senha_padrao
    )
