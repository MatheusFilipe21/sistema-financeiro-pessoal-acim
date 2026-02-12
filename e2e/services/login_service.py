from pages.login_page import LoginPage
from services.base_service import BaseService
from typing import Dict


class LoginService(BaseService):
    """
    Camada de Serviço: Orquestra a lógica de negócios para a funcionalidade de Login.

    Responsabilidades:
    - Agrupar ações atômicas da Page em fluxos de negócio (ex: realizar login).
    - Realizar validações específicas do domínio de login.
    - Traduzir dados de teste (ex: tabelas do Gherkin) para interações com a UI.
    """

    def __init__(self, login_page: LoginPage) -> None:
        """
        Inicializa o Contexto, injetando o Page Object de Login.

        Args:
            login_page: A instância do Page Object de Login.
        """
        super().__init__(login_page)
        self.login_page = login_page

    def navegar_para_login(self) -> None:
        """
        Navega para a URL da tela de login.

        :author: Ilka Berenguer
        """
        self.login_page.visitar()

    def preencher_campos_dinamicos(self, dados_tabela: Dict[str, str]) -> None:
        """
        Preenche o formulário baseado em um dicionário (útil para Data Tables do BDD).

        Mapeia os nomes das colunas do BDD para os argumentos do método da Page.

        Args:
            dados_tabela: Dict com chaves como 'email' e 'senha'

        :author: Ilka Berenguer
        """
        self.login_page.preencher_formulario(
            email=dados_tabela.get('email', ''),
            senha=dados_tabela.get('senha', ''),
        )
