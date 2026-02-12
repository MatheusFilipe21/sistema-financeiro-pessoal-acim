from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from .base_page import BasePage


class RedefinirSenhaPage(BasePage):
    """
    Representa a Página de Redefinição de Senha e centraliza os locators e interações
    específicas desta tela.
    """

    def __init__(self, driver: WebDriver, url_base: str) -> None:
        """
        Inicializa o Page Object de Redefinir Senha com seus seletores específicos.

        Args:
            driver: A instância do WebDriver.
            url_base: A URL base da aplicação.
        """
        super().__init__(driver, url_base)

        # Localizadores
        self.INPUT_SENHA = (By.ID, "input-senha")
        self.INPUT_CONFIRMAR_SENHA = (By.ID, "input-confirmar-senha")

    def preencher_formulario(self, senha: str, confirmacao_senha: str) -> None:
        """
        Preenche o campo do formulário de redefinição senha.
        Utiliza o método 'preencher_campo' da BasePage que já trata o TAB (blur).

        Args:
            senha: Nova senha do usuário.
            confirmar_senha: Confirmação da nova senha do usuário.

        :author: Matheus F. N. Pereira
        """
        self.preencher_campo(self.INPUT_SENHA, senha)
        self.preencher_campo(self.INPUT_CONFIRMAR_SENHA, confirmacao_senha)
