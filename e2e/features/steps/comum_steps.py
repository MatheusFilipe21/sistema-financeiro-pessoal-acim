from behave import step, then
from behave.runner import Context

# Mapeamento de texto amigável do BDD para o código técnico
# Ex: "erro" -> "erro", "sucesso" -> "mensagem"
MAPA_TIPO_DIALOG = {
    "erro": "erro",
    "sucesso": "mensagem",
    "aviso": "mensagem",
    "info": "mensagem"
}


@step('clico no botão "{}"')
def step_clicar_botao_generico(context: Context, nome_botao: str) -> None:
    """
    Clica em um botão baseado no seu nome visível ou identificador.
    Delega a ação para a BasePage.

    Args:
        nome_botao: O texto ou nome que compõe o ID do botão (ex: 'Cadastrar').

    :author: Matheus F. N. Pereira
    """
    context.base_page.clicar_botao_dinamico(nome_botao)


@then('o botão "{}" deve estar "{}"')
def step_validar_estado_botao(context: Context, nome_botao: str, estado: str) -> None:
    """
    Valida se um botão está habilitado ou desabilitado.

    Args:
        nome_botao: O nome do botão.
        estado: 'habilitado' ou 'desabilitado'.

    :author: Matheus F. N. Pereira
    """
    deve_estar_habilitado = estado.lower() == 'habilitado'
    context.base_service.verificar_estado_botao(
        nome_botao, deve_estar_habilitado)


@then('deve ser exibido um dialog de "{}" com título "{}"')
def step_validar_titulo_dialog_generico(context: Context, tipo: str, titulo: str) -> None:
    """
    Valida a presença e o título de um dialog global.

    Args:
        tipo: O tipo do dialog ('erro', 'sucesso', 'aviso').
        titulo: O título esperado.

    :author: Matheus F. N. Pereira
    """
    tipo_tecnico = MAPA_TIPO_DIALOG.get(tipo.lower(), "mensagem")

    tipo_visual = tipo.lower() if tipo.lower() != 'erro' else None

    context.base_service.verificar_dialog_global(
        tipo_dialog=tipo_tecnico,
        titulo_esperado=titulo,
        ignore_mensagem=True,
        tipo_visual_esperado=tipo_visual
    )

    context.ultimo_tipo_dialog = tipo_tecnico
    context.ultimo_titulo_dialog = titulo
    context.ultimo_tipo_visual = tipo_visual


@then('a mensagem do dialog deve ser "{}"')
def step_validar_mensagem_dialog(context: Context, mensagem: str) -> None:
    """
    Valida o corpo da mensagem do dialog que foi verificado no passo anterior.
    Suporta interpolação dinâmica de variáveis como {email}.

    Este passo recupera o tipo e o título salvos no contexto para reutilizar
    a validação completa do BaseService.

    Args:
        mensagem: O texto esperado no corpo do dialog.

    Raises:
        AttributeError: Se este passo for chamado sem antes chamar o passo de validação do título.

    :author: Matheus F. N. Pereira
    """
    if "{email}" in mensagem and hasattr(context, 'email_gerado'):
        mensagem = mensagem.format(email=context.email_gerado)

    try:
        tipo_salvo = getattr(context, 'ultimo_tipo_dialog', 'mensagem')
        titulo_salvo = getattr(context, 'ultimo_titulo_dialog', '')
        tipo_visual_salvo = getattr(context, 'ultimo_tipo_visual', None)

        if not hasattr(context, 'ultimo_tipo_dialog'):
            raise AttributeError("Estado do dialog não encontrado.")

    except AttributeError:
        raise AttributeError(
            "Erro de Fluxo BDD: Você tentou validar a mensagem de um dialog "
            "sem antes validar o título (onde o tipo é definido)."
        )

    context.base_service.verificar_dialog_global(
        tipo_dialog=tipo_salvo,
        titulo_esperado=titulo_salvo,
        mensagem_esperada=mensagem,
        ignore_mensagem=False,
        tipo_visual_esperado=tipo_visual_salvo
    )


@then('deve ser exibida a mensagem de erro "{}" no campo "{}"')
def step_validar_erro_campo(context: Context, mensagem: str, campo: str) -> None:
    """
    Valida a mensagem de erro de validação (form validation) associada a um input.

    Args:
        mensagem: O texto exato do erro esperado.
        campo: O identificador do campo (ex: 'email', 'senha').

    :author: Matheus F. N. Pereira
    """
    context.base_service.verificar_mensagem_erro_validacao(mensagem, campo)
