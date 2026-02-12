import requests
import time
import re
import os
import quopri
from typing import Dict, Optional, List


class MailhogService:
    """
    Serviço especializado na comunicação com a API do Mailhog.
    Responsável por interagir com a infraestrutura de e-mail nos testes.

    Esta classe abstrai a complexidade de requisições HTTP, decodificação de
    conteúdo (Quoted-Printable) e extração de dados sensíveis (Links/Tokens),
    permitindo validações de e-mail sem necessidade de interface gráfica.

    :author: Matheus F. N. Pereira
    """

    def __init__(self) -> None:
        """
        Inicializa o serviço configurando o host da API do Mailhog.

        A estratégia de resolução do host segue a ordem:
        1. Variável de ambiente 'MAILHOG_HOST' (se definida).
        2. Padrão 'host.docker.internal' (para comunicação Docker-Host).

        :author: Matheus F. N. Pereira
        """
        self.host = os.getenv("MAILHOG_HOST", "host.docker.internal")
        self.api_url = f"http://{self.host}:8025/api/v2"

    def buscar_ultimo_email(self, destinatario: str, timeout: int = 10) -> Dict:
        """
        Realiza polling na API até encontrar um e-mail para o destinatário ou estourar o tempo.

        O método consulta a lista de mensagens repetidamente em intervalos curtos
        para garantir que o teste prossiga assim que o e-mail chegar.

        Args:
            destinatario: O endereço de e-mail do destinatário.
            timeout: Tempo máximo de espera em segundos.

        Returns:
            Dict: O objeto JSON contendo os dados do e-mail encontrado.

        Raises:
            AssertionError: Se nenhum e-mail for encontrado após o tempo limite.

        :author: Matheus F. N. Pereira
        """
        fim_tempo = time.time() + timeout

        while time.time() < fim_tempo:
            mensagens = self._obter_todas_mensagens()
            email_encontrado = self._filtrar_mensagem_por_destinatario(
                mensagens, destinatario)

            if email_encontrado:
                return email_encontrado

            time.sleep(0.5)

        raise AssertionError(
            f"E-mail para '{destinatario}' não encontrado após {timeout}s.")

    def extrair_link_do_corpo(self, email_json: Dict) -> str:
        """
        Decodifica o corpo do e-mail e extrai o primeiro link HTTP/HTTPS encontrado.

        Realiza a decodificação de Quoted-Printable antes de aplicar o Regex
        para garantir que o link não esteja quebrado (ex: com '=3D' ou quebras de linha).

        Args:
            email_json: O dicionário do e-mail retornado pela API.

        Returns:
            str: A URL limpa pronta para navegação.

        Raises:
            AssertionError: Se nenhum link válido for encontrado no corpo.

        :author: Matheus F. N. Pereira
        """
        corpo_bruto = email_json.get('Content', {}).get('Body', '')
        corpo_decodificado = self._decodificar_quoted_printable(corpo_bruto)

        # Regex captura http://... ou https://... até encontrar espaço, aspas ou quebra de linha
        match = re.search(r'(https?://[^\s<>"]+)', corpo_decodificado)

        if not match:
            raise AssertionError("Nenhum link encontrado no corpo do e-mail.")

        link = match.group(1)

        return link

    def limpar_caixa_entrada(self) -> None:
        """
        Apaga todos os e-mails do Mailhog via API.

        Útil para executar no 'Background' ou 'Before Scenario' para garantir
        um estado limpo antes dos testes. Falhas na requisição são ignoradas silenciosamente.

        :author: Matheus F. N. Pereira
        """
        try:
            requests.delete(f"{self.api_url}/messages", timeout=2)
        except Exception:
            pass

    def _obter_todas_mensagens(self) -> List[Dict]:
        """
        Realiza a requisição GET para obter todas as mensagens do Mailhog.

        Returns:
            List[Dict]: Lista de mensagens ou lista vazia em caso de erro.

        :author: Matheus F. N. Pereira
        """
        try:
            response = requests.get(f"{self.api_url}/messages", timeout=2)
            return response.json().get('items', []) if response.status_code == 200 else []
        except requests.RequestException:
            return []

    def _filtrar_mensagem_por_destinatario(self, mensagens: List[Dict], destinatario: str) -> Optional[Dict]:
        """
        Filtra a lista de mensagens buscando pelo destinatário especificado.

        Verifica tanto o objeto estruturado 'To' (mais confiável) quanto os Headers
        brutos para garantir compatibilidade.

        Args:
            mensagens: Lista de mensagens vindas da API.
            destinatario: E-mail a ser buscado.

        Returns:
            Optional[Dict]: O e-mail encontrado ou None.

        :author: Matheus F. N. Pereira
        """
        destinatario = destinatario.lower()

        for msg in mensagens:
            recipients = msg.get('To', [])
            for rec in recipients:
                full_email = f"{rec['Mailbox']}@{rec['Domain']}"
                if full_email.lower() == destinatario:
                    return msg

            headers_to = msg.get('Content', {}).get(
                'Headers', {}).get('To', [])
            for h in headers_to:
                if destinatario in h.lower():
                    return msg

        return None

    def _decodificar_quoted_printable(self, texto: str) -> str:
        """
        Decodifica uma string do formato Quoted-Printable para UTF-8.

        Args:
            texto: O texto bruto do corpo do e-mail.

        Returns:
            str: O texto decodificado ou o texto original em caso de falha.

        :author: Matheus F. N. Pereira
        """
        try:
            return quopri.decodestring(texto.encode('utf-8')).decode('utf-8')
        except Exception:
            return texto
