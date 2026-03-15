from pytest_bdd import given, when, then, parsers

from services.base_service import BaseService
from services.cadastro_service import CadastroService

# Mapeamento de texto amigável do BDD para o código técnico
# Ex: "erro" -> "erro", "sucesso" -> "mensagem"
MAPA_TIPO_DIALOG = {
    "erro": "erro",
    "sucesso": "mensagem",
    "aviso": "mensagem",
    "info": "mensagem"
}


@when(parsers.parse('clico no botão "{nome_botao}"'))
def step_clicar_botao_generico(base_service: BaseService, nome_botao: str) -> None:
    """
    Clica em um botão baseado no seu nome visível ou identificador.
    Delega a ação para a BasePage através do BaseService.

    Args:
        base_service: Fixture injetada do serviço base.
        nome_botao: O texto ou nome que compõe o ID do botão (ex: 'Cadastrar').

    :author: Matheus F. N. Pereira
    """
    base_service.base_page.clicar_botao_dinamico(nome_botao)


@then(parsers.parse('o botão "{nome_botao}" deve estar "{estado}"'))
def step_validar_estado_botao(base_service: BaseService, nome_botao: str, estado: str) -> None:
    """
    Valida se um botão está habilitado ou desabilitado.

    Args:
        base_service: Fixture injetada do serviço base.
        nome_botao: O nome do botão.
        estado: 'habilitado' ou 'desabilitado'.

    :author: Matheus F. N. Pereira
    """
    deve_estar_habilitado = estado.lower() == 'habilitado'
    base_service.verificar_estado_botao(nome_botao, deve_estar_habilitado)


@then(parsers.parse('deve ser exibido um dialog de "{tipo}" com título "{titulo}"'))
def step_validar_titulo_dialog_generico(base_service: BaseService, contexto_teste: dict, tipo: str, titulo: str) -> None:
    """
    Valida a presença e o título de um dialog global e salva o estado no contexto da requisição.

    Args:
        base_service: Fixture injetada do serviço base.
        contexto_teste: Dicionário injetado para guardar estados temporários do cenário.
        tipo: O tipo do dialog ('erro', 'sucesso', 'aviso').
        titulo: O título esperado.

    :author: Matheus F. N. Pereira
    """
    tipo_tecnico = MAPA_TIPO_DIALOG.get(tipo.lower(), "mensagem")
    tipo_visual = tipo.lower() if tipo.lower() != 'erro' else None

    base_service.verificar_dialog_global(
        tipo_dialog=tipo_tecnico,
        titulo_esperado=titulo,
        ignore_mensagem=True,
        tipo_visual_esperado=tipo_visual
    )

    contexto_teste['ultimo_tipo_dialog'] = tipo_tecnico
    contexto_teste['ultimo_titulo_dialog'] = titulo
    contexto_teste['ultimo_tipo_visual'] = tipo_visual


@then(parsers.parse('a mensagem do dialog deve conter "{mensagem}"'))
def step_validar_mensagem_dialog(base_service: BaseService, contexto_teste: dict, massa_dados: dict, mensagem: str) -> None:
    """
    Valida o corpo da mensagem do dialog que foi verificado no passo anterior.
    Suporta interpolação dinâmica de variáveis como {email}.

    Args:
        base_service: Fixture injetada do serviço base.
        contexto_teste: Dicionário injetado contendo o estado do dialog anterior.
        massa_dados: Dicionário injetado com os dados randômicos da execução.
        mensagem: O texto esperado no corpo do dialog.

    Raises:
        AttributeError: Se este passo for chamado sem antes chamar o passo de validação do título.

    :author: Matheus F. N. Pereira
    """
    if "{email}" in mensagem and 'email_gerado' in massa_dados:
        mensagem = mensagem.format(email=massa_dados['email_gerado'])

    if 'ultimo_tipo_dialog' not in contexto_teste:
        raise AttributeError(
            "Erro de Fluxo BDD: Você tentou validar a mensagem de um dialog "
            "sem antes validar o título (onde o tipo é definido)."
        )

    tipo_salvo = contexto_teste.get('ultimo_tipo_dialog', 'mensagem')
    titulo_salvo = contexto_teste.get('ultimo_titulo_dialog', '')
    tipo_visual_salvo = contexto_teste.get('ultimo_tipo_visual', None)

    base_service.verificar_dialog_global(
        tipo_dialog=tipo_salvo,
        titulo_esperado=titulo_salvo,
        mensagem_esperada=mensagem,
        ignore_mensagem=False,
        tipo_visual_esperado=tipo_visual_salvo
    )


@then(parsers.parse('deve ser exibida a mensagem de erro "{mensagem}" no campo "{campo}"'))
def step_validar_erro_campo(base_service: BaseService, mensagem: str, campo: str) -> None:
    """
    Valida a mensagem de erro de validação (form validation) associada a um input.

    Args:
        base_service: Fixture injetada do serviço base.
        mensagem: O texto exato do erro esperado.
        campo: O identificador do campo (ex: 'email', 'senha').

    :author: Matheus F. N. Pereira
    """
    base_service.verificar_mensagem_erro_validacao(mensagem, campo)


@then(parsers.parse('devo ser direcionado para a página de "{caminho_esperado}"'))
def step_devo_ser_direcionado_para_pagina(base_service: BaseService, caminho_esperado: str) -> None:
    """
    Verifica se a URL atual do navegador corresponde à página esperada.

    Args:
        base_service: Fixture injetada do serviço base.
        caminho_esperado: O caminho relativo esperado na URL.

    :author: Alexandre Orlando Gracio
    """
    base_service.verificar_pagina_atual(caminho_esperado)


@given('que realizo um cadastro com sucesso com nome padrão, email gerado e a senha padrão')
def step_given_usuario_ja_cadastrado(cadastro_service: CadastroService, massa_dados: dict) -> None:
    """
    Pré-condição: Cadastra um usuário real via UI para "queimar" o e-mail no banco.

    Args:
        cadastro_service: Fixture injetada do serviço de cadastro.
        massa_dados: Dicionário injetado com os dados randômicos da execução.

    :author: Matheus F. N. Pereira
    """
    cadastro_service.navegar_para_cadastro()

    cadastro_service.realizar_cadastro(
        nome=massa_dados['nome_padrao'],
        email=massa_dados['email_gerado'],
        senha=massa_dados['senha_padrao'],
        confirmar_senha=massa_dados['senha_padrao']
    )

    cadastro_service.verificar_sucesso_cadastro(massa_dados['nome_padrao'])
