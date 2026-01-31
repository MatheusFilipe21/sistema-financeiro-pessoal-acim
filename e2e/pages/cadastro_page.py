from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from .base_page import BasePage


class CadastroPage(BasePage):
    """
    Representa a Página de Cadastro e centraliza os locators e interações
    específicas desta tela.
    """

    def __init__(self, driver: WebDriver, url_base: str) -> None:
        """
        Inicializa o Page Object de Cadastro com seus seletores específicos.

        Args:
            driver: A instância do WebDriver.
            url_base: A URL base da aplicação.
        """
        super().__init__(driver, url_base)
        self.caminho = "/cadastro"

        # Localizadores
        self.CAMPO_NOME = (By.ID, "input-nome")
        self.CAMPO_EMAIL = (By.ID, "input-email")
        self.CAMPO_SENHA = (By.ID, "input-senha")
        self.CAMPO_CONFIRMAR_SENHA = (By.ID, "input-confirmar-senha")
        self.BOTAO_CADASTRAR = (By.ID, "btn-cadastrar")

    def preencher_formulario(self, nome: str, email: str, senha: str, confirmar_senha: str) -> None:
        """
        Preenche todos os campos do formulário de cadastro.
        Utiliza o método 'preencher_campo' da BasePage que já trata o TAB (blur).

        Args:
            nome: Nome do usuário.
            email: E-mail do usuário.
            senha: Senha do usuário.
            confirmar_senha: Confirmação da senha do usuário.

        :author: Matheus F. N. Pereira
        """
        self.preencher_campo(self.CAMPO_NOME, nome)
        self.preencher_campo(self.CAMPO_EMAIL, email)
        self.preencher_campo(self.CAMPO_SENHA, senha)
        self.preencher_campo(self.CAMPO_CONFIRMAR_SENHA, confirmar_senha)

    def clicar_cadastrar(self) -> None:
        """
        Clica no botão de cadastrar utilizando o locator explícito da classe.

        :author: Matheus F. N. Pereira
        """
        self.clicar(self.BOTAO_CADASTRAR)

    def is_botao_cadastrar_habilitado(self) -> bool:
        """
        Verifica o estado do botão de cadastrar.

        Nota: Não faz asserção. Apenas retorna o estado (True/False).

        Returns:
            bool: True se estiver habilitado, False se estiver desabilitado.

        :author: Alexandre Orlando Gracio
        """
        try:
            botao = self.aguardar_elemento_visivel(self.BOTAO_CADASTRAR)
            return botao.is_enabled()
        except:
            return False
            raise
