from pages.base_page import BasePage


class BaseService:
    """
    Classe Base de Serviço que contém lógicas de teste compartilhadas
    por toda a aplicação.
    """

    def __init__(self, base_page: BasePage) -> None:
        """
        Inicializa o BaseService.

        Args:
            base_page: Qualquer Page Object que herde de BasePage.
        """
        self.base_page = base_page

    def verificar_erro_global(self, titulo_esperado: str, mensagem_esperada: str) -> None:
        """
        Verifica se o Dialog de erro global exibe o título e a mensagem corretos.

        Args:
            titulo_esperado: O título do erro.
            mensagem_esperada: A mensagem detalhada do erro.

        :author: Matheus F. N. Pereira
        """
        self.base_page.aguardar_elemento_visivel(
            self.base_page.DIALOG_GLOBAL
        )

        titulo_real = self.base_page.obter_texto_de_elemento_visivel(
            self.base_page.TITULO_ERRO_DIALOG_GLOBAL
        )
        assert titulo_real == titulo_esperado, f"Título Esperado: '{titulo_esperado}', Obtido: '{titulo_real}'"

        mensagem_real = self.base_page.obter_texto_de_elemento_visivel(
            self.base_page.MENSAGEM_ERRO_DIALOG_GLOBAL
        )
        assert mensagem_real == mensagem_esperada, f"Mensagem Esperada: '{mensagem_esperada}', Obtido: '{mensagem_real}'"
