from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from typing import Tuple, Any, Dict, Literal


# Definição de Tipo para o argumento
TIPO_DIALOG = Literal['erro', 'mensagem']


class BasePage:
    """
    Classe Base que todas as Page Objects devem herdar.
    Contém o WebDriver e métodos de baixo nível para interação com o DOM.

    Esta classe abstrai a complexidade do Selenium e centraliza o tratamento de
    esperas explícitas (Waits).

    Nota: Esta classe não deve conter asserções (asserts). Ela deve apenas
    retornar o estado da página ou realizar ações.
    """

    def __init__(self, driver: WebDriver, url_base: str = "") -> None:
        """
        Inicializa a BasePage com o driver e a URL base da aplicação.

        Args:
            driver: A instância do WebDriver (Chrome, Firefox, etc).
            url_base: A URL raiz da aplicação para navegação relativa.
        """
        self.driver = driver
        self.url_base = url_base

        # Tempo padrão de espera para elementos
        self.espera = WebDriverWait(driver, 10)

        # Padrões para Dialogs
        # Ex: id="dialog-erro-global", id="titulo-erro", id="mensagem-erro"
        self.PREFIXO_DIALOG = "dialog-"
        self.SUFIXO_DIALOG_GLOBAL = "-global"
        self.PREFIXO_TITULO = "titulo-"
        self.PREFIXO_MENSAGEM = "mensagem-"

        # Prefixos padronizados do projeto
        self.PREFIXO_CAMPO_ERRO = "erro-"
        self.PREFIXO_BTN = "btn-"

    def visitar(self) -> None:
        """
        Navega para a URL completa da página.

        Concatena a `url_base` com o atributo `self.caminho` que deve ser
        definido nas classes filhas (Page Objects específicas).

        :author: Matheus F. N. Pereira
        """
        caminho = getattr(self, 'caminho', '')
        url_completa = f"{self.url_base}{caminho}"
        self.driver.get(url_completa)
        print(f"Navegando para: {url_completa}")
    
    def obter_url_atual(self) -> str:
        """
        Retorna a URL atual do navegador.

        Returns:
            str: A URL atual do navegador.

        :author: Alexandre Orlando Gracio
        """
        return self.driver.current_url

    def aguardar_elemento_visivel(self, localizador: Tuple[str, str]) -> Any:
        """
        Aguarda até que um elemento esteja presente no DOM e visível.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor"). Ex: (By.ID, "user").

        Returns:
            WebElement: O elemento encontrado e visível.

        Raises:
            TimeoutException: Se o elemento não aparecer no tempo estipulado.

        :author: Matheus F. N. Pereira
        """
        return self.espera.until(EC.visibility_of_element_located(localizador))

    def aguardar_elemento_clicavel(self, localizador: Tuple[str, str]) -> Any:
        """
        Aguarda até que um elemento esteja visível E habilitado para clique.

        Use este método para botões e links para evitar erros de interseção
        ou elementos desabilitados momentaneamente.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor").

        Returns:
            WebElement: O elemento pronto para interação.

        :author: Matheus F. N. Pereira
        """
        return self.espera.until(EC.element_to_be_clickable(localizador))

    def obter_texto_elemento(self, localizador: Tuple[str, str]) -> str:
        """
        Aguarda a visibilidade do elemento e retorna seu texto interno.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor").

        Returns:
            str: O texto contido no elemento (ex: innerText).

        :author: Matheus F. N. Pereira
        """
        elemento = self.aguardar_elemento_visivel(localizador)
        return elemento.text

    def clicar(self, localizador: Tuple[str, str]) -> None:
        """
        Aguarda o elemento estar clicável e realiza a ação de clique.

        Args:
            localizador: Uma tupla (By.TIPO, "seletor") para encontrar o elemento.

        :author: Matheus F. N. Pereira
        """
        elemento = self.aguardar_elemento_clicavel(localizador)
        elemento.click()

    def preencher_campo(self, localizador: Tuple[str, str], texto: str) -> None:
        """
        Preenche um campo de formulário de forma segura.

        Fluxo:
        1. Aguarda visibilidade.
        2. Limpa o campo (clear).
        3. Digita o texto.
        4. Pressiona TAB para retirar o foco e disparar eventos 'blur' (validação).

        Args:
            localizador: Uma tupla (By.TIPO, "seletor").
            texto: O valor string a ser inserido.

        :author: Matheus F. N. Pereira
        """
        elemento = self.aguardar_elemento_visivel(localizador)
        elemento.clear()
        elemento.send_keys(texto)
        elemento.send_keys(Keys.TAB)

    def obter_mensagem_erro_campo(self, nome_campo: str) -> str:
        """
        Busca o texto de erro associado a um campo específico.

        Pressupõe que o ID do erro segue o padrão: 'erro-{nome_campo}'.
        Ex: Se nome_campo='email', busca id='erro-email'.

        Args:
            nome_campo: O sufixo do ID do campo (ex: 'email', 'senha').

        Returns:
            str: O texto da mensagem de erro encontrada.

        :author: Matheus F. N. Pereira
        """
        id_campo_erro = f"{self.PREFIXO_CAMPO_ERRO}{nome_campo.lower()}"
        return self.obter_texto_elemento((By.ID, id_campo_erro))

    def is_botao_habilitado(self, texto_botao: str) -> bool:
        """
        Verifica se um botão está habilitado para interação.

        Utiliza o padrão de ID 'btn-{texto_botao}' removendo espaços.
        Ex: 'Cadastrar' -> busca id='btn-cadastrar'.

        Args:
            texto_botao: O texto que compõe o ID do botão.

        Returns:
            bool: True se o botão existe e está habilitado (enabled), 
                  False caso contrário ou se não for encontrado.

        :author: Matheus F. N. Pereira
        """
        # 'Salvar Tudo' vira 'salvar-tudo' para compor o ID
        sufixo = texto_botao.lower().replace(" ", "-")
        id_botao = f"{self.PREFIXO_BTN}{sufixo}"

        try:
            elemento = self.espera.until(
                EC.presence_of_element_located((By.ID, id_botao)))
            return elemento.is_enabled()
        except:
            return False
            raise

    def clicar_botao_dinamico(self, texto_botao: str) -> None:
        """
        Helper para clicar em botões baseados na convenção de ID 'btn-{texto}'.

        Args:
            texto_botao: O texto base para formar o ID (ex: 'Entrar').

        :author: Matheus F. N. Pereira
        """
        sufixo = texto_botao.lower().replace(" ", "-")
        id_botao = f"{self.PREFIXO_BTN}{sufixo}"
        self.clicar((By.ID, id_botao))

    def obter_dados_dialog(self, tipo: TIPO_DIALOG) -> Dict[str, str]:
        """
        Recupera título, mensagem e o tipo visual (ex: sucesso, aviso) do dialog.

        Lógica de IDs esperada no HTML:
        - Container (Service): id="dialog-{tipo}-global"
        - Título (Template):   id="titulo-{tipo}"
        - Mensagem (Template): id="mensagem-{tipo}"

        Lógica de Tipo (apenas para 'mensagem'):
        - Busca classe 'tipo-{valor}' na div id="mensagem-dialog"

        Args:
            tipo: O tipo base do dialog ('erro' ou 'mensagem').

        Returns:
            Dict com chaves: 'titulo', 'mensagem', 'tipo'.
            Ex: {'titulo': 'Salvo', 'mensagem': 'Sucesso', 'tipo': 'sucesso'}

        :author: Matheus F. N. Pereira
        """
        import logging

        tipo_lower = tipo.lower()

        id_container = f"{self.PREFIXO_DIALOG}{tipo_lower}{self.SUFIXO_DIALOG_GLOBAL}"
        id_titulo = f"{self.PREFIXO_TITULO}{tipo_lower}"
        id_mensagem = f"{self.PREFIXO_MENSAGEM}{tipo_lower}"

        self.aguardar_elemento_visivel((By.ID, id_container))

        titulo_texto = self.obter_texto_elemento((By.ID, id_titulo))
        mensagem_texto = self.obter_texto_elemento((By.ID, id_mensagem))

        tipo_identificado = tipo_lower

        if tipo_lower == 'mensagem':
            try:
                elemento_interno = self.driver.find_element(
                    By.ID, "mensagem-dialog")
                classes = elemento_interno.get_attribute("class") or ""

                for cls in classes.split():
                    if cls.startswith("tipo-"):
                        tipo_identificado = cls.replace("tipo-", "")
                        break
            except Exception as e:
                logging.error(f"Erro ao tentar salvar: {e}")
                raise

        return {
            "titulo": titulo_texto,
            "mensagem": mensagem_texto,
            "tipo": tipo_identificado
        }
