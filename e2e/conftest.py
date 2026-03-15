import pytest
import os
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from faker import Faker

# Imports das classes de Página
from services.mailhog_service import MailhogService
from pages.base_page import BasePage
from pages.cadastro_page import CadastroPage
from pages.login_page import LoginPage
from pages.recuperar_senha_page import RecuperarSenhaPage
from pages.redefinir_senha_page import RedefinirSenhaPage

# Imports das classes de Serviço
from services.base_service import BaseService
from services.cadastro_service import CadastroService
from services.login_service import LoginService
from services.recuperar_senha_service import RecuperarSenhaService
from services.redefinir_senha_service import RedefinirSenhaService

# Constantes e Configurações Globais
# A URL do frontend (Angular)
ANGULAR_URL_BASE = os.environ.get("FRONTEND_URL", "http://localhost:4200")

# Define se o navegador rodará em modo invisível (headless).
HEADLESS_MODE = os.environ.get("HEADLESS_MODE", "True").lower() == ("true")

# Inicializa o faker com o locale pt_BR
fake = Faker(['pt_BR'])


@pytest.fixture(scope="function")
def driver():
    """
    Inicializa o WebDriver antes de cada cenário e encerra depois.
    O 'yield' é a fronteira entre o Setup e o Teardown.

    :author: Matheus F. N. Pereira
    """
    options = Options()

    if HEADLESS_MODE:
        options.add_argument('--headless')

    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument("--window-size=1920,1080")

    _driver = webdriver.Chrome(options=options)

    modo_execucao = "Headless" if HEADLESS_MODE else "Com Interface Gráfica"
    print(
        f"\n--- Iniciando E2E em: {ANGULAR_URL_BASE} (Modo: {modo_execucao}) ---")

    yield _driver

    _driver.quit()
    print("\n--- Driver encerrado ---")


@pytest.fixture(scope="function")
def massa_dados():
    """
    Gera dados padrões e únicos (isolados) para cada cenário que solicitar esta fixture.

    :author: Matheus F. N. Pereira
    """
    dados = {
        "email_gerado": fake.unique.email(),
        "nome_padrao": "USUÁRIO DE TESTE SFP-ACIM",
        "senha_padrao": "Ab123456"
    }
    print(f"\nDados Gerados -> E-mail: {dados['email_gerado']}")
    return dados


@pytest.fixture(scope="function")
def contexto_teste():
    """
    Dicionário em branco para compartilhar estados dinâmicos entre os steps 
    de um mesmo cenário (ex: validar mensagens de dialog que apareceram no When e serão validadas no Then).

    :author: Matheus F. N. Pereira
    """
    return {}


@pytest.fixture
def mailhog_service():
    """
    Instancia e injeta o serviço de integração com a API do Mailhog.

    :author: Matheus F. N. Pereira
    """
    return MailhogService()


@pytest.fixture
def base_service(driver):
    """
    Instancia e injeta o serviço base, atrelando-o à sua respectiva Page.

    :author: Matheus F. N. Pereira
    """
    return BaseService(BasePage(driver, ANGULAR_URL_BASE))


@pytest.fixture
def cadastro_service(driver):
    """
    Instancia e injeta o serviço de Cadastro, atrelando-o à sua respectiva Page.

    :author: Matheus F. N. Pereira
    """
    return CadastroService(CadastroPage(driver, ANGULAR_URL_BASE))


@pytest.fixture
def login_service(driver):
    """
    Instancia e injeta o serviço de Login, atrelando-o à sua respectiva Page.

    :author: Matheus F. N. Pereira
    """
    return LoginService(LoginPage(driver, ANGULAR_URL_BASE))


@pytest.fixture
def recuperar_senha_service(driver):
    """
    Instancia e injeta o serviço de Recuperação de Senha, atrelando-o à sua respectiva Page.

    :author: Matheus F. N. Pereira
    """
    return RecuperarSenhaService(RecuperarSenhaPage(driver, ANGULAR_URL_BASE))


@pytest.fixture
def redefinir_senha_service(driver):
    """
    Instancia e injeta o serviço de Redefinição de Senha, atrelando-o à sua respectiva Page.

    :author: Matheus F. N. Pereira
    """
    return RedefinirSenhaService(RedefinirSenhaPage(driver, ANGULAR_URL_BASE))
