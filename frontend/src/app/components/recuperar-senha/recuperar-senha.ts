import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Autenticacao as AutenticacaoService } from '../../services/autenticacao';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterModule } from '@angular/router';
import { Dialog as DialogService } from '../../services/dialog';

/**
 * Componente responsável pela solicitação de recuperação de senha (RF16).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-recuperar-senha',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    RouterModule,
  ],
  templateUrl: './recuperar-senha.html',
  styleUrls: ['./recuperar-senha.scss'],
})
export class RecuperarSenha {
  private readonly formBuilder = inject(FormBuilder);
  private readonly autenticacaoService = inject(AutenticacaoService);
  private readonly dialogService = inject(DialogService);
  private readonly router = inject(Router);

  formulario: FormGroup;

  constructor() {
    this.formulario = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
    });
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
    if (!control) return '';

    if (control.hasError('required')) {
      return 'Este campo é obrigatório.';
    }

    if (control.hasError('email')) {
      return 'Formato de e-mail inválido.';
    }

    return '';
  }

  /**
   * Envia a solicitação de recuperação.
   */
  aoEnviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const email = this.formulario.get('email')?.value;

    this.autenticacaoService.esqueciSenha(email).subscribe({
      next: () => {
        this.dialogService
          .mostrarSucesso(
            'E-mail Enviado',
            `Se o e-mail <strong>${email}</strong> estiver cadastrado, você receberá as instruções em instantes.\n\nVerifique também sua caixa de Spam ou Lixo Eletrônico.`
          )
          .subscribe(() => {
            this.router.navigate(['/login']);
          });
      },
    });
  }
}
