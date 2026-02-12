from behave.runner import Context
from selenium.webdriver.remote.webdriver import WebDriver

# Imports das classes de Página
from pages.base_page import BasePage
from pages.cadastro_page import CadastroPage
from pages.recuperar_senha_page import RecuperarSenhaPage
from pages.login_page import LoginPage
from pages.redefinir_senha_page import RedefinirSenhaPage

# Imports das classes de Serviço
from services.mailhog_service import MailhogService
from services.base_service import BaseService
from services.cadastro_service import CadastroService
from services.recuperar_senha_service import RecuperarSenhaService
from services.login_service import LoginService
from services.redefinir_senha_service import RedefinirSenhaService


class Contexto(Context):
    """
    Interface que descreve a estrutura do Contexto em tempo de execução.

    Esta classe não é instanciada. Ela serve apenas como Type Hint (Dica de Tipo)
    para os argumentos 'context' nos Steps Definitions.

    :author: Matheus F. N. Pereira
    """

    # Driver e Configurações Globais
    driver: WebDriver
    url_base: str

    # Massa de Dados (Variáveis Dinâmicas)
    email_gerado: str
    nome_padrao: str
    senha_padrao: str

    # Variáveis de Estado (Validação de Dialogs)
    ultimo_tipo_dialog: str
    ultimo_titulo_dialog: str

    # Serviços de Infraestrutura
    mailhog_service: MailhogService

    # Serviços de Negócio
    base_service: BaseService
    cadastro_service: CadastroService
    recuperar_senha_service: RecuperarSenhaService
    login_service: LoginService
    redefinir_senha_service: RedefinirSenhaService

    # Page Objects
    base_page: BasePage
    cadastro_page: CadastroPage
    recuperar_senha_page: RecuperarSenhaPage
    login_page: LoginPage
    redefinir_senha_page: RedefinirSenhaPage
