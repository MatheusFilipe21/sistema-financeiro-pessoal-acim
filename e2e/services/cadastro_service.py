from pages.cadastro_page import CadastroPage
from services.base_service import BaseService
from typing import Dict


class CadastroService(BaseService):
    """
    Camada de Serviço: Orquestra a lógica de negócios para a funcionalidade de Cadastro.

    Responsabilidades:
    - Agrupar ações atômicas da Page em fluxos de negócio (ex: realizar cadastro).
    - Realizar validações específicas do domínio de cadastro.
    - Traduzir dados de teste (ex: tabelas do Gherkin) para interações com a UI.
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
        Navega para a URL da tela de cadastro.

        :author: Matheus F. N. Pereira
        """
        self.cadastro_page.visitar()

    def realizar_cadastro(self, nome: str, email: str, senha: str, confirmar_senha: str) -> None:
        """
        Fluxo completo: Preenche o formulário e clica em cadastrar.

        :author: Matheus F. N. Pereira
        """
        self.cadastro_page.preencher_formulario(
            nome, email, senha, confirmar_senha)
        self.cadastro_page.clicar_cadastrar()

    def preencher_campos_dinamicos(self, dados_tabela: Dict[str, str]) -> None:
        """
        Preenche o formulário baseado em um dicionário (útil para Data Tables do BDD).

        Mapeia os nomes das colunas do BDD para os argumentos do método da Page.

        Args:
            dados_tabela: Dict com chaves como 'nome', 'email', 'senha', etc.

        :author: Matheus F. N. Pereira
        """
        self.cadastro_page.preencher_formulario(
            nome=dados_tabela.get('nome', ''),
            email=dados_tabela.get('email', ''),
            senha=dados_tabela.get('senha', ''),
            confirmar_senha=dados_tabela.get('confirmar_senha', '')
        )

    def verificar_sucesso_cadastro(self, nome_usuario: str) -> None:
        """
        Verifica se o cadastro foi concluído com sucesso.
        Valida: Título do dialog, mensagem contendo o nome e a cor verde (sucesso).

        :author: Matheus F. N. Pereira
        """
        titulo_esperado = "Cadastro realizado com sucesso!"
        mensagem_parcial = f"O usuário {nome_usuario} foi cadastrado"
        self.verificar_dialog_global(
            tipo_dialog='mensagem',
            titulo_esperado=titulo_esperado,
            mensagem_esperada=mensagem_parcial,
            tipo_visual_esperado='sucesso'
        )

    def verificar_erro_email_duplicado(self, email: str) -> None:
        """
        Valida o cenário de tentativa de cadastro com e-mail já existente.

        :author: Matheus F. N. Pereira
        """
        titulo_esperado = "Dados Inválidos"
        mensagem_esperada = f"O e-mail: {email} já está cadastrado."

        self.verificar_dialog_global(
            tipo_dialog='erro',
            titulo_esperado=titulo_esperado,
            mensagem_esperada=mensagem_esperada
        )

    def validar_botao_cadastrar_desabilitado(self) -> None:
        """
        Regra de Negócio: O botão cadastrar deve permanecer desabilitado 
        se o formulário estiver inválido.

        :author: Matheus F. N. Pereira
        """
        esta_habilitado = self.cadastro_page.is_botao_cadastrar_habilitado()
        assert esta_habilitado is False, \
            "Falha de validação: O botão 'Cadastrar' deveria estar desabilitado, mas está ativo."
