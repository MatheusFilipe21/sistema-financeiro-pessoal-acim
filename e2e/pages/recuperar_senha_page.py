from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC

from .base_page import BasePage


class RecuperarSenhaPage(BasePage):
    """
    Representa a Página de Recuperar Senha e centraliza os locators e interações
    específicas desta tela.
    """

    def __init__(self, driver: WebDriver, url_base: str) -> None:
        """
        Inicializa o Page Object de Recuperar Senha com seus seletores específicos.

        Args:
            driver: A instância do WebDriver.
            url_base: A URL base da aplicação.
        """
        super().__init__(driver, url_base)
        self.caminho = "/recuperar-senha"

        # Localizadores
        self.CAMPO_EMAIL = (By.ID, "input-email")

    def preencher_formulario(self, email: str) -> None:
        """
        Preenche o campo do formulário de recuperar senha.
        Utiliza o método 'preencher_campo' da BasePage que já trata o TAB (blur).

        Args:
            email: E-mail do usuário.

        :author: Alexandre Orlando Gracio
        """
        self.preencher_campo(self.CAMPO_EMAIL, email)
