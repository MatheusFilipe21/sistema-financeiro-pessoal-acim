from pages.base_page import BasePage, TIPO_DIALOG


class BaseService:
    """
    Classe Base de Serviço que centraliza as regras de negócio e validações
    compartilhadas por toda a aplicação.

    Esta classe atua como uma camada intermediária entre os Steps (BDD) e as
    Pages (Selenium), garantindo que as validações sejam reutilizáveis e
    desacopladas da implementação direta do driver.

    :author: Matheus F. N. Pereira
    """

    def __init__(self, base_page: BasePage) -> None:
        """
        Inicializa o BaseService com uma instância de BasePage.

        Args:
            base_page: Instância de qualquer Page Object que herde de BasePage,
                       permitindo acesso aos métodos de interação com o DOM.
        """
        self.base_page = base_page

    def verificar_dialog_global(self,
                                tipo_dialog: TIPO_DIALOG,
                                titulo_esperado: str,
                                mensagem_esperada: str = None,
                                ignore_mensagem: bool = False,
                                tipo_visual_esperado: str = None) -> None:
        """
        Verifica se o Dialog global exibe os dados corretos (Título, Mensagem e Tipo).

        Utiliza o método 'obter_dados_dialog' da BasePage para extrair as informações
        do DOM e realiza as asserções de negócio.

        Args:
            tipo_dialog: O tipo do dialog a ser verificado ('erro' ou 'mensagem').
            titulo_esperado: O texto exato esperado no título do dialog.
            mensagem_esperada: O texto (parcial ou total) esperado no corpo da mensagem.
                               Se None, a validação da mensagem é pulada a menos que
                               ignore_mensagem seja False.
            ignore_mensagem: Se True, não valida o conteúdo da mensagem. Padrão False.
            tipo_visual_esperado: (Opcional) Verifica o subtipo visual do dialog 
                                  (ex: 'sucesso', 'aviso', 'info'). Útil para garantir
                                  que um dialog de mensagem é verde (sucesso) e não amarelo.

        Raises:
            AssertionError: Se o título, mensagem ou tipo visual não corresponderem ao esperado.

        :author: Matheus F. N. Pereira
        """
        dados_dialog = self.base_page.obter_dados_dialog(tipo_dialog)

        titulo_real = dados_dialog['titulo']
        assert titulo_real == titulo_esperado, \
            f"Título Incorreto. Esperado: '{titulo_esperado}', Obtido: '{titulo_real}'"

        if not ignore_mensagem and mensagem_esperada:
            mensagem_real = dados_dialog['mensagem']
            assert mensagem_esperada in mensagem_real, \
                f"Mensagem Incorreta. Esperado conter: '{mensagem_esperada}', Obtido: '{mensagem_real}'"

        if tipo_visual_esperado:
            tipo_real = dados_dialog['tipo']
            assert tipo_real == tipo_visual_esperado, \
                f"Tipo visual incorreto. Esperado: '{tipo_visual_esperado}', Obtido: '{tipo_real}'"

    def verificar_mensagem_erro_validacao(self, mensagem_esperada: str, campo: str) -> None:
        """
        Valida se a mensagem de erro de validação exibida em um campo específico
        corresponde ao esperado.

        O método solicita à BasePage o texto do erro associado ao campo e realiza
        a asserção.

        Args:
            mensagem_esperada: O texto exato da mensagem de erro que deve ser exibida.
            campo: O sufixo do ID do campo (ex: 'email', 'senha') onde o erro deve aparecer.
                   O mapeamento para o seletor real ocorre na BasePage.

        Raises:
            AssertionError: Se a mensagem encontrada no DOM for diferente da esperada.

        :author: Alexandre Orlando Gracio
        """
        mensagem_obtida = self.base_page.obter_mensagem_erro_campo(campo)

        assert mensagem_obtida == mensagem_esperada, \
            f"Erro no campo '{campo}'. Esperado: '{mensagem_esperada}', Obtido: '{mensagem_obtida}'"

    def verificar_estado_botao(self, nome_botao: str, deve_estar_habilitado: bool) -> None:
        """
        Verifica se um botão específico está no estado esperado (Habilitado/Desabilitado).

        Args:
            nome_botao: Texto que identifica o botão (ex: 'Cadastrar').
            deve_estar_habilitado: True para verificar se está habilitado, 
                                   False para verificar se está desabilitado.

        Raises:
            AssertionError: Se o estado do botão não for o esperado.

        :author: Alexandre Orlando Gracio
        """
        esta_habilitado = self.base_page.is_botao_habilitado(nome_botao)

        estado_esperado_str = "habilitado" if deve_estar_habilitado else "desabilitado"

        assert esta_habilitado == deve_estar_habilitado, \
            f"O botão '{nome_botao}' deveria estar {estado_esperado_str}, mas não está."
