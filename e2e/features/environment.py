from selenium import webdriver
from selenium.webdriver.chrome.options import Options
import os
from faker import Faker

# Imports das classes de Página e Serviço
from pages.base_page import BasePage
from pages.cadastro_page import CadastroPage
from pages.recuperar_senha_page import RecuperarSenhaPage
from pages.login_page import LoginPage
from services.base_service import BaseService
from services.cadastro_service import CadastroService
from services.recuperar_senha_service import RecuperarSenhaService
from services.login_service import LoginService

# A URL do frontend (Angular)
ANGULAR_URL_BASE = os.environ.get("FRONTEND_URL", "http://localhost:4200")

# Inicializa o faker com o locale pt_BR
fake = Faker(['pt_BR'])


def before_all(context):
    """
    Executado antes de todos os cenários.
    Responsabilidade: Configurar o Driver e realizar a Injeção de Dependência.

    Aqui instanciamos todas as Pages e Services uma única vez, ligando-os ao
    Contexto do Behave. Isso evita ter que criar objetos dentro de cada Step.
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
    context.driver = webdriver.Chrome(options=options)
    context.url_base = ANGULAR_URL_BASE

    print(f"\n--- Iniciando E2E em: {context.url_base} (Chrome Headless) ---")

    context.base_page = BasePage(context.driver, context.url_base)
    context.base_service = BaseService(context.base_page)

    context.cadastro_page = CadastroPage(context.driver, context.url_base)
    context.cadastro_service = CadastroService(context.cadastro_page)

    context.recuperar_senha_page = RecuperarSenhaPage(context.driver, context.url_base)
    context.recuperar_senha_service = RecuperarSenhaService(context.recuperar_senha_page)

    context.login_page = LoginPage(context.driver, context.url_base)
    context.login_service = LoginService(context.login_page)

def before_scenario(context, scenario):
    """
    Executado antes de cada cenário.
    Responsabilidade: Gerar massa de dados fresca para garantir independência dos testes.
    """
    SENHA_PADRAO = "Ab123456"

    if 'cadastro' in scenario.tags:
        # Gera dados únicos para evitar conflito de "E-mail já cadastrado"
        # em testes que deveriam ser de sucesso.
        context.email_gerado = fake.unique.email()
        context.nome_padrao = "MATHEUS FILIPE DO NASCIMENTO PEREIRA"
        context.senha_padrao = SENHA_PADRAO

        print(f"\n--- Cenário: {scenario.name} ---")
        print(f"Dados gerados -> E-mail: {context.email_gerado}")

    if 'recuperarSenha' in scenario.tags:
        # Gera um e-mail para o teste de recuperação de senha.
        context.email_gerado = fake.unique.email()

        print(f"\n--- Cenário: {scenario.name} ---")
        print(f"Dados gerados -> E-mail: {context.email_gerado}")

    if 'login' in scenario.tags:
        # Gera dados únicos para evitar conflito de "E-mail já cadastrado"
        # em testes que deveriam ser de sucesso.
        context.email_gerado = fake.unique.email()
        context.nome_padrao = "ILKA BERENGUER"
        context.senha_padrao = SENHA_PADRAO

        print(f"\n--- Cenário: {scenario.name} ---")
        print(f"Dados gerados -> E-mail: {context.email_gerado}")

def after_all(context):
    """
    Executado depois de todos os cenários.
    Responsabilidade: Limpeza de recursos (encerrar o navegador).
    """
    if hasattr(context, 'driver'):
        context.driver.quit()
        print("\n--- Driver encerrado ---")
