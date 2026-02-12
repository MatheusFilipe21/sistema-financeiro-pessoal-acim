from pages.redefinir_senha_page import RedefinirSenhaPage
from services.base_service import BaseService
from typing import Dict


class RedefinirSenhaService(BaseService):
    """
    Camada de Serviço: Orquestra a lógica de negócios para a funcionalidade de Redefinir Senha.

     Responsabilidades:
    - Agrupar ações atômicas da Page em fluxos de negócio.
    - Realizar validações específicas do domínio de redefinição de senha.
    - Traduzir dados de teste (ex: tabelas do Gherkin) para interações com a UI.
    """

    def __init__(self, redefinir_senha_page: RedefinirSenhaPage) -> None:
        """
        Inicializa o Contexto, injetando o Page Object de Redefinir Senha.

        Args:
            redefinir_senha_page: A instância do Page Object de Redefinir Senha.
        """
        super().__init__(redefinir_senha_page)
        self.redefinir_senha_page = redefinir_senha_page

    def preencher_campos_dinamicos(self, dados_tabela: Dict[str, str]) -> None:
        """
        Preenche o formulário baseado em um dicionário (útil para Data Tables do BDD).

        Mapeia o nome da coluna do BDD para o argumento do método da Page.

        Args:
            dados_tabela: Dict com chaves como 'email'.

        :author: Matheus F. N. Pereira
        """
        self.redefinir_senha_page.preencher_formulario(
            senha=dados_tabela.get('senha', ''),
            confirmacao_senha=dados_tabela.get('confirmar_senha', ''),
        )
