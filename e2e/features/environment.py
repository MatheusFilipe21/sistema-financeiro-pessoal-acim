from selenium import webdriver
from selenium.webdriver.chrome.options import Options
import os
from faker import Faker

# A URL do frontend (Angular)
ANGULAR_URL_BASE = os.environ.get("FRONTEND_URL", "http://localhost:4200")

# Inicializa o faker com o locale pt_BR
fake = Faker(['pt_BR'])


def before_all(context):
    """
    Executado antes de todos os cenários. Configura o driver do Selenium.
    """
    options = Options()

    # Não mostra a interface gráfica
    options.add_argument('--headless')
    # Desativa o modo sandbox
    options.add_argument('--no-sandbox')
    # Corrige problemas de memória
    options.add_argument('--disable-dev-shm-usage')

    # O Selenium/Behave usa o 'chromedriver' que foi instalado junto com o Chrome no postCreateCommand
    context.driver = webdriver.Chrome(options=options)
    context.url_base = ANGULAR_URL_BASE

    print(f"\n--- Iniciando E2E em: {context.url_base} (Chrome Headless) ---")


def before_scenario(context, scenario):
    """
    Executado antes de cada cenário. Gera dados únicos para o teste de cadastro.
    """
    if 'cadastro' in scenario.tags:
        context.email_gerado = fake.unique.email()
        context.nome_padrao = "MATHEUS FILIPE DO NASCIMENTO PEREIRA"
        context.senha_padrao = "Ab123456"

        print("\n--- Dados gerados para o cenário: ---")
        print(f"E-mail: {context.email_gerado}")


def after_all(context):
    """
    Executado depois de todos os cenários. Fecha o navegador.
    """
    if context.driver:
        context.driver.quit()
