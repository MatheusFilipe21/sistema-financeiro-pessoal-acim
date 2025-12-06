from pages.cadastro_page import CadastroPage


class CadastroService:
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
        self.cadastro_page = cadastro_page

    def navegar_para_cadastro(self) -> None:
        """
        Ação de navegação de alto nível.

        :author: Matheus F. N. Pereira
        """
        self.cadastro_page.visitar()

    def verificar_mensagem_sucesso(self, nome_esperado: str) -> None:
        """
        Verifica se a mensagem de sucesso está correta após o cadastro,
        incluindo a validação do nome do usuário.

        Args:
            nome_esperado: O nome do usuário que deve aparecer na mensagem.

        :author: Matheus F. N. Pereira
        """
        mensagem_bruta = self.cadastro_page.obter_texto_de_elemento_visivel(
            self.cadastro_page.MENSAGEM_SUCESSO
        )

        mensagem_real = mensagem_bruta.split('\n')[0].strip()

        mensagem_esperada = f"Usuário {nome_esperado} cadastrado com sucesso!"

        assert mensagem_real == mensagem_esperada, f"Esperado: '{mensagem_esperada}', Obtido: '{mensagem_real}'"
