from pages.recuperar_senha_page import RecuperarSenhaPage
from services.base_service import BaseService
from typing import Dict


class RecuperarSenhaService(BaseService):
    """
    Camada de Serviço: Orquestra a lógica de negócios para a funcionalidade de Recuperar Senha.

    Responsabilidades:
    - Agrupar ações atômicas da Page em fluxos de negócio (ex: solicitar recuperação de senha).
    - Realizar validações específicas do domínio de recuperação de senha.
    - Traduzir dados de teste (ex: tabelas do Gherkin) para interações com a UI.
    """

    def __init__(self, recuperar_senha_page: RecuperarSenhaPage) -> None:
        """
        Inicializa o Contexto, injetando o Page Object de Recuperar Senha.

        Args:
            recuperar_senha_page: A instância do Page Object de Recuperar Senha.
        """
        super().__init__(recuperar_senha_page)
        self.recuperar_senha_page = recuperar_senha_page

    def navegar_para_recuperar_senha(self) -> None:
        """
        Navega para a URL da tela de recuperar senha.

        :author: Alexandre Orlando Gracio
        """
        self.recuperar_senha_page.visitar()

    def preencher_campos_dinamicos(self, dados_tabela: Dict[str, str]) -> None:
        """
        Preenche o formulário baseado em um dicionário (útil para Data Tables do BDD).

        Mapeia o nome da coluna do BDD para o argumento do método da Page.

        Args:
            dados_tabela: Dict com chaves como 'email'.

        :author: Alexandre Orlando Gracio
        """
        self.recuperar_senha_page.preencher_formulario(
            email=dados_tabela.get('email', ''),
        )

    def solicitar_recuperacao_senha(self, email: str) -> None:
        """
        Executa o fluxo completo de solicitar a recuperação de senha.

        Etapas:
        1. Navega para a página.
        2. Preenche o e-mail.
        3. Clica em enviar.
        4. Valida se o dialog de sucesso apareceu (Garante que o estado do sistema mudou).

        Args:
            email: O e-mail para o qual a recuperação será solicitada.

        :author: Matheus F. N. Pereira
        """
        self.navegar_para_recuperar_senha()

        self.preencher_campos_dinamicos({'email': email})

        self.recuperar_senha_page.clicar_botao_enviar_link()

        self.verificar_dialog_global(
            tipo_dialog="mensagem",
            titulo_esperado="E-mail Enviado",
            mensagem_esperada=email,
            ignore_mensagem=False
        )
