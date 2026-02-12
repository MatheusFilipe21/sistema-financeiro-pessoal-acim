from behave import step, then, given
from utils.contexto import Contexto

# Mapeamento de texto amigável do BDD para o código técnico
# Ex: "erro" -> "erro", "sucesso" -> "mensagem"
MAPA_TIPO_DIALOG = {
    "erro": "erro",
    "sucesso": "mensagem",
    "aviso": "mensagem",
    "info": "mensagem"
}


@step('clico no botão "{}"')
def step_clicar_botao_generico(contexto: Contexto, nome_botao: str) -> None:
    """
    Clica em um botão baseado no seu nome visível ou identificador.
    Delega a ação para a BasePage.

    Args:
        nome_botao: O texto ou nome que compõe o ID do botão (ex: 'Cadastrar').

    :author: Matheus F. N. Pereira
    """
    contexto.base_page.clicar_botao_dinamico(nome_botao)


@then('o botão "{}" deve estar "{}"')
def step_validar_estado_botao(contexto: Contexto, nome_botao: str, estado: str) -> None:
    """
    Valida se um botão está habilitado ou desabilitado.

    Args:
        nome_botao: O nome do botão.
        estado: 'habilitado' ou 'desabilitado'.

    :author: Matheus F. N. Pereira
    """
    deve_estar_habilitado = estado.lower() == 'habilitado'
    contexto.base_service.verificar_estado_botao(
        nome_botao, deve_estar_habilitado)


@then('deve ser exibido um dialog de "{}" com título "{}"')
def step_validar_titulo_dialog_generico(contexto: Contexto, tipo: str, titulo: str) -> None:
    """
    Valida a presença e o título de um dialog global.

    Args:
        tipo: O tipo do dialog ('erro', 'sucesso', 'aviso').
        titulo: O título esperado.

    :author: Matheus F. N. Pereira
    """
    tipo_tecnico = MAPA_TIPO_DIALOG.get(tipo.lower(), "mensagem")

    tipo_visual = tipo.lower() if tipo.lower() != 'erro' else None

    contexto.base_service.verificar_dialog_global(
        tipo_dialog=tipo_tecnico,
        titulo_esperado=titulo,
        ignore_mensagem=True,
        tipo_visual_esperado=tipo_visual
    )

    contexto.ultimo_tipo_dialog = tipo_tecnico
    contexto.ultimo_titulo_dialog = titulo
    contexto.ultimo_tipo_visual = tipo_visual


@then('a mensagem do dialog deve conter "{}"')
def step_validar_mensagem_dialog(contexto: Contexto, mensagem: str) -> None:
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
    if "{email}" in mensagem and hasattr(contexto, 'email_gerado'):
        mensagem = mensagem.format(email=contexto.email_gerado)

    try:
        if not hasattr(contexto, 'ultimo_tipo_dialog'):
            raise AttributeError("Estado do dialog não encontrado.")
    except AttributeError:
        raise AttributeError(
            "Erro de Fluxo BDD: Você tentou validar a mensagem de um dialog "
            "sem antes validar o título (onde o tipo é definido)."
        )

    tipo_salvo = getattr(contexto, 'ultimo_tipo_dialog', 'mensagem')
    titulo_salvo = getattr(contexto, 'ultimo_titulo_dialog', '')
    tipo_visual_salvo = getattr(contexto, 'ultimo_tipo_visual', None)

    contexto.base_service.verificar_dialog_global(
        tipo_dialog=tipo_salvo,
        titulo_esperado=titulo_salvo,
        mensagem_esperada=mensagem,
        ignore_mensagem=False,
        tipo_visual_esperado=tipo_visual_salvo
    )


@then('deve ser exibida a mensagem de erro "{}" no campo "{}"')
def step_validar_erro_campo(contexto: Contexto, mensagem: str, campo: str) -> None:
    """
    Valida a mensagem de erro de validação (form validation) associada a um input.

    Args:
        mensagem: O texto exato do erro esperado.
        campo: O identificador do campo (ex: 'email', 'senha').

    :author: Matheus F. N. Pereira
    """
    contexto.base_service.verificar_mensagem_erro_validacao(mensagem, campo)


@then('devo ser direcionado para a página de "{}"')
def step_devo_ser_direcionado_para_pagina(contexto: Contexto, caminho_esperado: str) -> None:
    """
    Verifica se a URL atual do navegador corresponde à página esperada.

    Args:
        caminho_esperado: O caminho relativo esperado na URL (ex: 'dashboard', 'recuperar-senha').

    :author: Alexandre Orlando Gracio
    """
    contexto.base_service.verificar_pagina_atual(caminho_esperado)


@given('que realizo um cadastro com sucesso com nome padrão, email gerado e a senha padrão')
def step_given_usuario_ja_cadastrado(contexto: Contexto) -> None:
    """
    Pré-condição: Cadastra um usuário real via UI para "queimar" o e-mail no banco.

    Este passo realiza um cadastro completo (preenchimento e submit) para garantir
    que, ao tentar usar este e-mail novamente no teste, o sistema acuse duplicidade.

    Args:
        contexto: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    contexto.cadastro_service.navegar_para_cadastro()

    contexto.cadastro_service.realizar_cadastro(
        nome=contexto.nome_padrao,
        email=contexto.email_gerado,
        senha=contexto.senha_padrao,
        confirmar_senha=contexto.senha_padrao
    )

    contexto.cadastro_service.verificar_sucesso_cadastro(contexto.nome_padrao)
