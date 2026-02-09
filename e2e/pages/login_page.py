from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from .base_page import BasePage


class LoginPage(BasePage):
    """
    Representa a Página de Login e centraliza os locators e interações
    específicas desta tela.
    """

    def __init__(self, driver: WebDriver, url_base: str) -> None:
        """
        Inicializa o Page Object de Login com seus seletores específicos.

        Args:
            driver: A instância do WebDriver.
            url_base: A URL base da aplicação.
        """
        super().__init__(driver, url_base)
        self.caminho = "/login"

        # Localizadores
        self.CAMPO_EMAIL = (By.ID, "input-email")
        self.CAMPO_SENHA = (By.ID, "input-senha")

    def preencher_formulario(self, email: str, senha: str) -> None:
        """
        Preenche todos os campos do formulário de login.
        Utiliza o método 'preencher_campo' da BasePage que já trata o TAB (blur).

        :author: Ilka Berenguer
        """
        self.preencher_campo(self.CAMPO_EMAIL, email)
        self.preencher_campo(self.CAMPO_SENHA, senha)
