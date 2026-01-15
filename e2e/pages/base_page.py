from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.common.by import By
from typing import Tuple, Any


class BasePage:
    """
    Classe Base que todas as Page Objects devem herdar.
    Contém o WebDriver e métodos comuns de navegação, espera e interação.
    """

    def __init__(self, driver: WebDriver, url_base: str) -> None:
        """
        Inicializa a BasePage com o driver e a URL base da aplicação.

        Args:
            driver: A instância do WebDriver.
            url_base: A URL base da aplicação.
        """
        self.driver = driver
        self.url_base = url_base

        self.espera = WebDriverWait(driver, 10)

        self.ERRO_DIALOG_GLOBAL = (By.ID, "dialog-erro-global")
        self.TITULO_ERRO_DIALOG_GLOBAL = (By.ID, "titulo-erro")
        self.MENSAGEM_ERRO_DIALOG_GLOBAL = (By.ID, "mensagem-erro")
        self.MENSAGEM_DIALOG_GLOBAL = (By.ID, "dialog-mensagem-global")
        self.TITULO_MENSAGEM_DIALOG_GLOBAL = (By.ID, "mensagem-titulo")
        self.MENSAGEM_MENSAGEM_DIALOG_GLOBAL = (By.ID, "mensagem-mensagem")

    def visitar(self) -> None:
        """
        Navega para a URL completa, concatenando a url_base com o self.caminho.
        O self.caminho deve ser definido na Page Object herdeira.

        :author: Matheus F. N. Pereira
        """
        url_completa = f"{self.url_base}{getattr(self, 'caminho', '')}"
        self.driver.get(url_completa)
        print(f"Navegando para: {url_completa}")

    def aguardar_elemento_visivel(self, localizador: Tuple[str, str]) -> Any:
        """
        Espera até que um elemento específico esteja visível na tela e o retorna.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor") para encontrar o elemento.

        :author: Matheus F. N. Pereira
        """
        return self.espera.until(EC.visibility_of_element_located(localizador))

    def obter_texto_de_elemento_visivel(self, localizador: Tuple[str, str]) -> str:
        """
        Aguarda que o elemento fique visível e retorna o texto contido nele.
        Ideal para elementos globais como Snackbars ou Alerts.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor") para encontrar o elemento.

        Returns:
            O texto (str) do elemento visível.

        :author: Matheus F. N. Pereira
        """
        elemento = self.aguardar_elemento_visivel(localizador)
        return elemento.text

    def clicar(self, localizador: Tuple[str, str]) -> None:
        """
        Aguarda o elemento estar visível e realiza o clique.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor") para encontrar o elemento.

        :author: Matheus F. N. Pereira
        """
        elemento = self.aguardar_elemento_visivel(localizador)
        elemento.click()

    def preencher_campo(self, localizador: Tuple[str, str], texto: str) -> None:
        """
        Aguarda o campo estar visível, limpa seu conteúdo e escreve o novo texto.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor") para encontrar o elemento.
            texto: O valor a ser escrito no campo.

        :author: Matheus F. N. Pereira
        """
        elemento = self.aguardar_elemento_visivel(localizador)
        elemento.clear()
        elemento.send_keys(texto)
