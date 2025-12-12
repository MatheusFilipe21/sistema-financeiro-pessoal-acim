import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Autenticacao as AutenticacaoService } from '../../services/autenticacao';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Componente responsável pelo formulário e lógica
 * de login de usuários (RF08).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class Login implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly autenticacaoService = inject(AutenticacaoService);
  private readonly router = inject(Router);

  formulario: FormGroup;
  esconderSenha = signal(true);

  /**
   * Construtor do componente.
   * Inicializa o formulário reativo (ReactiveForms) com os campos
   * e validadores necessários para o login.
   */
  constructor() {
    this.formulario = this.formBuilder.group({
      // Validação simples (RF09)
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required]],
    });
  }

  /**
   * Método do ciclo de vida do Angular (OnInit).
   *
   * Ao carregar a tela de Login, invoca o serviço de autenticação para
   * realizar o logout forçado (limpeza de token), garantindo que o usuário
   * inicie uma nova sessão a partir de um estado limpo.
   */
  ngOnInit(): void {
    this.autenticacaoService.deslogar();
  }

  /**
   * Alterna a visibilidade da senha no campo de input (o "olho").
   * Atualiza o signal 'esconderSenha'.
   */
  alternarVisibilidadeSenha(): void {
    this.esconderSenha.update((valor) => !valor);
  }

  /**
   * Obtém a mensagem de erro de validação para um controle específico do formulário.
   * Usado para exibir feedback dinâmico no template.
   *
   * @param nomeControle O nome do FormControl (ex: 'email', 'senha').
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
    return '';
  }

  /**
   * Manipula o evento de submissão do formulário de login.
   * Verifica a validade do formulário e chama o serviço de autenticação.
   */
  aoEnviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.autenticacaoService.login(this.formulario.value).subscribe({
      next: () => {},
    });
  }
}
