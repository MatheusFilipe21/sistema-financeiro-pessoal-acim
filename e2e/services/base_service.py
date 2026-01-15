from pages.base_page import BasePage
from typing import Literal


class BaseService:
    """
    Classe Base de Serviço que contém lógicas de teste compartilhadas
    por toda a aplicação.
    """
    TIPO_DIALOG = Literal['erro', 'mensagem']

    def __init__(self, base_page: BasePage) -> None:
        """
        Inicializa o BaseService.

        Args:
            base_page: Qualquer Page Object que herde de BasePage.
        """
        self.base_page = base_page

    def verificar_dialog_global(self, tipo_dialog: TIPO_DIALOG, titulo_esperado: str, mensagem_esperada: str) -> None:
        """
        Verifica se o Dialog global exibe o título e a mensagem corretos.

        Args:
            tipo_dialog: Tipo do dialog
            titulo_esperado: O título do erro.
            mensagem_esperada: A mensagem detalhada do erro.

        :author: Matheus F. N. Pereira
        """

        dialog = titulo = mensagem = None

        if tipo_dialog == "erro":
            dialog = self.base_page.ERRO_DIALOG_GLOBAL
            titulo = self.base_page.TITULO_ERRO_DIALOG_GLOBAL
            mensagem = self.base_page.MENSAGEM_ERRO_DIALOG_GLOBAL
        else:
            dialog = self.base_page.MENSAGEM_DIALOG_GLOBAL
            titulo = self.base_page.TITULO_MENSAGEM_DIALOG_GLOBAL
            mensagem = self.base_page.MENSAGEM_MENSAGEM_DIALOG_GLOBAL

        self.base_page.aguardar_elemento_visivel(dialog)

        titulo = self.base_page.obter_texto_de_elemento_visivel(titulo)

        assert titulo == titulo_esperado, f"Título Esperado: '{titulo_esperado}', Obtido: '{titulo}'"

        mensagem = self.base_page.obter_texto_de_elemento_visivel(mensagem)

        assert mensagem == mensagem_esperada, f"Mensagem Esperada: '{mensagem_esperada}', Obtido: '{mensagem}'"
