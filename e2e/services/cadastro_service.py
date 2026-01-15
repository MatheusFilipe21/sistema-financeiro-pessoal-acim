from pages.cadastro_page import CadastroPage
from services.base_service import BaseService


class CadastroService(BaseService):
    """
    Camada de Serviço/Contexto: Orquestra a lógica de teste para a funcionalidade de Cadastro.
    Contém as ações de alto nível e as asserções.
    """

    def __init__(self, cadastro_page: CadastroPage) -> None:
        """
        Inicializa o Contexto, injetando o Page Object de Cadastro.

        Args:
            cadastro_page: A instância do Page Object de Cadastro.
        """
        super().__init__(cadastro_page)
        self.cadastro_page = cadastro_page

    def navegar_para_cadastro(self) -> None:
        """
        Ação de navegação de alto nível.

        :author: Matheus F. N. Pereira
        """
        self.cadastro_page.visitar()

    def verificar_mensagem_sucesso(self, titulo: str, mensagem: str) -> None:
        """
        Verifica se a mensagem de sucesso está correta após o cadastro,
        incluindo a validação do nome do usuário.

        Args:
            titulo: O título da mensagem de sucesso.
            mensagem: A mensagem de sucesso esperada.

        :author: Matheus F. N. Pereira
        """
        self.verificar_dialog_global('sucesso', titulo, mensagem)

    def verificar_mensagem_erro_email_duplicado(self, email_duplicado: str) -> None:
        """
        Verifica se o Dialog de erro apareceu com a mensagem correta de duplicidade.
        Utiliza o verificador genérico herdado de BaseService.

        Args:
            email_duplicado: O e-mail que gerou o conflito.

        :author: Matheus F. N. Pereira
        """
        titulo = "Dados Inválidos"
        mensagem = f"O e-mail: {email_duplicado} já está cadastrado."

        self.verificar_dialog_global('erro', titulo, mensagem)
