from behave import given, when, then
from behave.runner import Context


@given('que estou na página de cadastro')
def step_given_estou_na_pagina_de_cadastro(context: Context) -> None:
    """
    Navega para a página de cadastro.

    Inicializa o fluxo garantindo que o Service navegue para a URL correta
    definida na Page Object.

    Args:
        context: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_service.navegar_para_cadastro()


@given('que já existe um usuário cadastrado com o email gerado')
def step_given_usuario_ja_cadastrado(context: Context) -> None:
    """
    Pré-condição: Cadastra um usuário real via UI para "queimar" o e-mail no banco.

    Este passo realiza um cadastro completo (preenchimento e submit) para garantir
    que, ao tentar usar este e-mail novamente no teste, o sistema acuse duplicidade.

    Args:
        context: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_service.navegar_para_cadastro()

    context.cadastro_service.realizar_cadastro(
        nome=context.nome_padrao,
        email=context.email_gerado,
        senha=context.senha_padrao,
        confirmar_senha=context.senha_padrao
    )

    context.cadastro_service.verificar_sucesso_cadastro(context.nome_padrao)

    context.cadastro_service.navegar_para_cadastro()


@when('preencho "{}", o email gerado, e senhas "{}" e "{}"')
def step_when_preencho_sucesso(context: Context, nome: str, senha: str, confirmar_senha: str) -> None:
    """
    Preenche o formulário usando o e-mail randômico armazenado no contexto.

    Args:
        context: O contexto de execução do Behave.
        nome: Nome vindo do step Gherkin.
        senha: Senha vinda do step Gherkin.
        confirmar_senha: Confirmação de senha vinda do step Gherkin.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_service.cadastro_page.preencher_formulario(
        nome=nome,
        email=context.email_gerado,
        senha=senha,
        confirmar_senha=confirmar_senha
    )


@when('preencho o formulário de cadastro com')
def step_when_preencho_tabela(context: Context) -> None:
    """
    Preenche o formulário usando uma Data Table do Gherkin.

    Útil para testes de validação (CT003) onde múltiplos cenários de dados
    são testados em sequência. O método converte a linha da tabela em um
    dicionário para o Service.

    Exemplo Gherkin:
      | nome | email           | senha | confirmar_senha |
      | John | email.invalido@ | 123   | 123             |

    Args:
        context: O contexto de execução do Behave (contém context.table).

    :author: Alexandre Orlando Gracio
    """
    row = context.table[0]

    context.cadastro_service.preencher_campos_dinamicos(row.as_dict())


@when('tento me cadastrar novamente com o mesmo email gerado')
def step_when_tento_cadastrar_duplicado(context: Context) -> None:
    """
    Fluxo de exceção: Tenta realizar um novo cadastro utilizando exatamente
    os mesmos dados (principalmente o e-mail) do @given anterior.

    Args:
        context: O contexto de execução do Behave.

    :author: Matheus F. N. Pereira
    """
    context.cadastro_service.realizar_cadastro(
        nome=context.nome_padrao,
        email=context.email_gerado,
        senha=context.senha_padrao,
        confirmar_senha=context.senha_padrao
    )


@then('a mensagem deve conter o texto "{}"')
def step_then_validar_conteudo_mensagem(context: Context, texto_parcial: str) -> None:
    """
    Valida se o corpo do dialog contém um trecho de texto específico.

    Este step depende do estado (tipo e título do dialog) ter sido validado
    no passo anterior.

    Args:
        context: O contexto de execução do Behave.
        texto_parcial: O trecho do texto que deve estar presente na mensagem.

    :author: Matheus F. N. Pereira
    """
    tipo_salvo = getattr(context, 'ultimo_tipo_dialog', 'mensagem')
    titulo_salvo = getattr(context, 'ultimo_titulo_dialog', '')

    context.base_service.verificar_dialog_global(
        tipo_dialog=tipo_salvo,
        titulo_esperado=titulo_salvo,
        mensagem_esperada=texto_parcial,
        ignore_mensagem=False
    )
