import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Autenticacao as AutenticacaoService } from '../../../services/autenticacao';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterModule } from '@angular/router';
import { validarSenhasIguais } from '../../../validators/validar-senhas-iguais';
import { DadosCadastroUsuarioDTO } from '../../../dtos/usuario/DadosCadastroUsuarioDTO';
import { Dialog as DialogService } from '../../../services/dialog';

/**
 * Componente responsável pelo formulário e lógica
 * de cadastro de novos usuários (RF01).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
  ],
  templateUrl: './cadastro.html',
  styleUrls: ['./cadastro.scss'],
})
export class Cadastro {
  private readonly formBuilder = inject(FormBuilder);
  private readonly autenticacaoService = inject(AutenticacaoService);
  private readonly dialogService = inject(DialogService);
  private readonly router = inject(Router);

  formulario: FormGroup;
  esconderSenha = signal(true);
  esconderConfirmarSenha = signal(true);
  senhaEstaEmFoco = signal(false);
  requisitosSenha = {
    minLength: false,
    temMaiuscula: false,
    temMinuscula: false,
    temNumero: false,
  };

  /**
   * Construtor do componente.
   * Inicializa o formulário reativo (ReactiveForms) com os campos
   * e validadores necessários para o cadastro.
   */
  constructor() {
    this.formulario = this.formBuilder.group(
      {
        // RF04: Nome obrigatório
        nome: ['', [Validators.required]],

        // RF02 e RF04: E-mail obrigatório e em formato válido
        email: ['', [Validators.required, Validators.email]],

        // RF03 e RF04: Senha obrigatória e complexa (Mínimo 8, 1 maiúscula, 1 minúscula, 1 número)
        senha: [
          '',
          [
            Validators.required,
            Validators.minLength(8),
            // (RF03) Regex: Mínimo 8, 1 maiúscula, 1 minúscula, 1 número
            Validators.pattern(String.raw`^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$`),
          ],
        ],
        confirmarSenha: ['', Validators.required],
      },
      {
        validators: validarSenhasIguais,
      },
    );

    // Escuta as mudanças no campo 'senha' para atualizar a UI de requisitos
    this.formulario.get('senha')?.valueChanges.subscribe((valor) => {
      this.atualizarRequisitosSenha(valor || '');
    });
  }

  /**
   * Alterna a visibilidade da senha no campo de input (o "olho").
   * Atualiza o signal 'esconderSenha'.
   */
  alternarVisibilidadeSenha(): void {
    this.esconderSenha.update((valor) => !valor);
  }

  /**
   * Alterna a visibilidade da confirmação de senha no campo de input (o "olho").
   * Atualiza o signal 'esconderConfirmarSenha'.
   */
  alternarVisibilidadeConfirmarSenha(): void {
    this.esconderConfirmarSenha.update((valor) => !valor);
  }

  /**
   * Define o signal 'senhaEstaEmFoco' como true.
   * Chamado pelo evento (focus) do input de senha.
   */
  aoFocarSenha(): void {
    this.senhaEstaEmFoco.set(true);
  }

  /**
   * Define o signal 'senhaEstaEmFoco' como false.
   * Chamado pelo evento (blur) do input de senha.
   */
  aoDesfocarSenha(): void {
    this.senhaEstaEmFoco.set(false);
  }

  /**
   * Obtém a mensagem de erro de validação para um controle específico do formulário.
   * Usado para exibir feedback dinâmico no template.
   *
   * @param nomeControle O nome do FormControl (ex: 'nome', 'email').
   * @returns A mensagem de erro formatada ou uma string vazia.
   */
  obterMensagemErro(nomeControle: string): string {
    const control = this.formulario.get(nomeControle);
    // Só mostra erros se o campo foi "tocado"
    if (!control) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Este campo é obrigatório.';
    }

    if (control.hasError('email')) {
      return 'Formato de e-mail inválido.';
    }

    if (nomeControle === 'senha' && control.invalid) {
      return 'A senha não atende aos requisitos mínimos.';
    }

    if (nomeControle === 'confirmarSenha' && control.hasError('senhasNaoConferem')) {
      return 'As senhas não conferem.';
    }
    return '';
  }

  /**
   * Manipula o evento de submissão do formulário de cadastro.
   * Verifica a validade do formulário e chama o serviço de autenticação.
   */
  aoEnviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { confirmarSenha, ...dadosParaEnvio } = this.formulario.value;
    const dto: DadosCadastroUsuarioDTO = dadosParaEnvio;

    this.autenticacaoService.registrar(dto).subscribe({
      next: (usuario) => {
        this.dialogService
          .mostrarSucesso(
            'Cadastro realizado com sucesso!',
            `O usuário ${usuario.nome} foi cadastrado, acesse a tela de login ou clique no OK para ser redirecionado e acessar o sistema.`,
          )
          .subscribe(() => {
            this.router.navigate(['/login']);
          });
      },
    });
  }

  /**
   * Atualiza os indicadores de requisitos da senha
   * com base no valor atual do campo.
   *
   * @param valor O valor atual do campo senha.
   */
  private atualizarRequisitosSenha(valor: string): void {
    this.requisitosSenha.minLength = valor.length >= 8;
    this.requisitosSenha.temMaiuscula = /[A-Z]/.test(valor);
    this.requisitosSenha.temMinuscula = /[a-z]/.test(valor);
    this.requisitosSenha.temNumero = /\d/.test(valor);
  }
}
