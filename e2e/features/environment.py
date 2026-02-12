from selenium import webdriver
from selenium.webdriver.chrome.options import Options
import os
from faker import Faker
from utils.contexto import Contexto

# Imports das classes de Página
from services.mailhog_service import MailhogService
from pages.base_page import BasePage
from pages.cadastro_page import CadastroPage
from pages.recuperar_senha_page import RecuperarSenhaPage
from pages.login_page import LoginPage
from pages.redefinir_senha_page import RedefinirSenhaPage

# Imports das classes de Serviço
from services.base_service import BaseService
from services.cadastro_service import CadastroService
from services.recuperar_senha_service import RecuperarSenhaService
from services.login_service import LoginService
from services.redefinir_senha_service import RedefinirSenhaService

# Constantes e Configurações Globais
# A URL do frontend (Angular)
ANGULAR_URL_BASE = os.environ.get("FRONTEND_URL", "http://localhost:4200")

# Inicializa o faker com o locale pt_BR
fake = Faker(['pt_BR'])

# Dados padrão para testes
NOME_PADRAO = "USUÁRIO DE TESTE SFP-ACIM"
SENHA_PADRAO = "Ab123456"


def before_all(contexto: Contexto):
    """
    Executado uma única vez antes de todos os testes.

    Responsabilidade:
    - Configurar o driver do navegador.
    - Instanciar todos os objetos Page e Service e anexá-los ao Contexto.

    :author: Matheus F. N. Pereira
    """
    _configurar_driver(contexto)
    _injetar_dependencias(contexto)


def before_scenario(contexto, scenario):
    """
    Executado antes de cada cenário individual.

    Responsabilidade:
    - Identificar se o cenário precisa de massa de dados.
    - Gerar dados frescos (e-mail único) para garantir isolamento dos testes.

    :author: Matheus F. N. Pereira
    """
    tags_que_precisam_dados = {
        'cadastro',
        'login',
        'recuperarSenha',
        'redefinirSenha'
    }

    if not tags_que_precisam_dados.isdisjoint(scenario.tags):
        _gerar_massa_dados(contexto)

        print(f"\n--- Cenário: {scenario.name} ---")
        print(f"Dados Gerados -> E-mail: {contexto.email_gerado}")


def after_all(contexto: Contexto):
    """
    Executado ao final de toda a suíte de testes.

    Responsabilidade:
    - Fechar o navegador e liberar memória.

    :author: Matheus F. N. Pereira
    """
    if hasattr(contexto, 'driver'):
        contexto.driver.quit()
        print("\n--- Driver encerrado ---")


def _configurar_driver(contexto: Contexto):
    """
    Configura as opções do Chrome e inicializa o WebDriver.
    """
    options = Options()

    # Não mostra a interface gráfica
    options.add_argument('--headless')
    # Desativa o modo sandbox
    options.add_argument('--no-sandbox')
    # Corrige problemas de memória
    options.add_argument('--disable-dev-shm-usage')

    # Define o tamanho da janela para evitar layout responsivo (mobile)
    # que quebraria a visibilidade dos elementos.
    options.add_argument("--window-size=1920,1080")

    # Inicializa o WebDriver
    contexto.driver = webdriver.Chrome(options=options)
    contexto.url_base = ANGULAR_URL_BASE

    print(f"\n--- Iniciando E2E em: {contexto.url_base} (Chrome Headless) ---")


def _injetar_dependencias(contexto: Contexto):
    """
    Realiza a injeção de dependência manual.
    Instancia Pages e Services ligando-os entre si e ao Contexto.
    """
    contexto.mailhog_service = MailhogService()

    contexto.base_page = BasePage(contexto.driver, contexto.url_base)
    contexto.base_service = BaseService(contexto.base_page)

    contexto.cadastro_page = CadastroPage(contexto.driver, contexto.url_base)
    contexto.cadastro_service = CadastroService(contexto.cadastro_page)

    contexto.recuperar_senha_page = RecuperarSenhaPage(
        contexto.driver, contexto.url_base)
    contexto.recuperar_senha_service = RecuperarSenhaService(
        contexto.recuperar_senha_page)

    contexto.login_page = LoginPage(contexto.driver, contexto.url_base)
    contexto.login_service = LoginService(contexto.login_page)

    contexto.redefinir_senha_page = RedefinirSenhaPage(
        contexto.driver, contexto.url_base)
    contexto.redefinir_senha_service = RedefinirSenhaService(
        contexto.redefinir_senha_page)


def _gerar_massa_dados(contexto: Contexto):
    """
    Gera e anexa ao contexto dados para o teste atual.
    """
    contexto.email_gerado = fake.unique.email()
    contexto.nome_padrao = NOME_PADRAO
    contexto.senha_padrao = SENHA_PADRAO
