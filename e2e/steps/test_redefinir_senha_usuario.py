from pytest_bdd import scenarios, given, when
from services.redefinir_senha_service import RedefinirSenhaService
from services.recuperar_senha_service import RecuperarSenhaService
from services.mailhog_service import MailhogService
from services.base_service import BaseService
from selenium.webdriver.remote.webdriver import WebDriver

from steps.comum_steps import *

scenarios('../features/redefinir_senha_usuario.feature')


@given('que solicitei a recuperação de senha para o email gerado')
def step_given_solicitei_recuperacao_senha(recuperar_senha_service: RecuperarSenhaService, massa_dados: dict) -> None:
    """
    Solicita uma recuperação de senha para o e-mail gerado contido na massa de dados.

    Args:
        recuperar_senha_service: Fixture injetada do serviço de recuperação de senha.
        massa_dados: Dicionário contendo os dados gerados para o cenário.

    :author: Matheus F. N. Pereira
    """
    recuperar_senha_service.solicitar_recuperacao_senha(
        massa_dados['email_gerado'])


@given('acesso o link de redefinição enviado por e-mail')
def step_given_acesso_link_email(mailhog_service: MailhogService, driver: WebDriver, massa_dados: dict) -> None:
    """
    Usa o MailhogService para capturar o e-mail, extrair o link e navegar via Driver.

    Args:
        mailhog_service: Fixture do serviço de integração com Mailhog.
        driver: Instância do WebDriver para navegação.
        massa_dados: Dicionário contendo o e-mail alvo da busca.

    :author: Matheus F. N. Pereira
    """
    email_data = mailhog_service.buscar_ultimo_email(
        massa_dados['email_gerado'])
    link = mailhog_service.extrair_link_do_corpo(email_data)
    driver.get(link)


@when('preencho o formulário de redefinição com', target_fixture="datatable")
def step_when_preencho_redefinicao(redefinir_senha_service: RedefinirSenhaService, datatable: list[list[str]]) -> list[list[str]]:
    """
    Preenche a nova senha e confirmação via Data Table do Gherkin.

    Args:
        redefinir_senha_service: Fixture do serviço de redefinição de senha.
        datatable: Tabela de dados do Gherkin injetada automaticamente.

    :author: Matheus F. N. Pereira
    """
    cabecalhos = datatable[0]
    valores = datatable[1]
    tabela_mapeada = dict(zip(cabecalhos, valores))

    redefinir_senha_service.preencher_campos_dinamicos(tabela_mapeada)
    return datatable


@given('que estou na tela de redefinição com token inválido')
def step_given_token_invalido(driver: WebDriver, base_service: BaseService) -> None:
    """
    Acessa diretamente a URL de redefinição com um token inválido.

    Exemplo Gherkin:
      | senha | confirmar_senha |
      | 123   | 123             |

    Args:
        driver: Instância do WebDriver para navegação.
        base_service: Fixture do serviço base para obtenção da URL configurada.

    :author: Alexandre Orlando Gracio
    """
    url = f"{base_service.base_page.url_base}/redefinir-senha?token=token_invalido"
    driver.get(url)
