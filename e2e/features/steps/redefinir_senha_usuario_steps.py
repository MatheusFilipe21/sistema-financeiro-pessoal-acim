from behave import given, when
from utils.contexto import Contexto


@given('que solicitei a recuperação de senha para o email gerado')
def step_given_solicitei_recuperacao_de_senha_para_o_email_gerado(contexto: Contexto) -> None:
    """
    Solicita uma recuperação de senha para o e-mail gerado.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    contexto.recuperar_senha_service.solicitar_recuperacao_senha(
        contexto.email_gerado)


@given('acesso o link de redefinição enviado por e-mail')
def step_given_acesso_link_email(contexto: Contexto) -> None:
    """
    Usa o MailhogService para pegar o link e o Driver para navegar.

    Args:
    contexto: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    email_data = contexto.mailhog_service.buscar_ultimo_email(
        contexto.email_gerado)

    link = contexto.mailhog_service.extrair_link_do_corpo(email_data)

    contexto.driver.get(link)


@when('preencho o formulário de redefinição com')
def step_when_preencho_redefinicao(contexto: Contexto) -> None:
    """
    Preenche a nova senha e confirmação via Data Table.

    Args:
    contexto: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    dados = contexto.table[0].as_dict()
    contexto.redefinir_senha_service.preencher_campos_dinamicos(dados)

@given('que estou na tela de redefinição com token inválido')
def step_given_token_invalido(contexto: Contexto) -> None:
    """
    Acessa diretamente a URL de redefinição com um token inválido para simular o cenário.

    Args:
    contexto: O contexto de execução do Behave.

    :author: Alexandre Orlando Gracio
    """
    url = f"{contexto.url_base}/redefinir-senha?token=token_invalido"
    contexto.driver.get(url)