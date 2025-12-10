import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Autenticacao as AutenticacaoService } from '../../services/autenticacao';
import { Dialog as DialogService } from '../../services/dialog';
import { DadosRedefinicaoSenhaDTO } from '../../dtos/autenticacao/DadosRedefinicaoSenhaDTO';
import { validarSenhasIguais } from '../../validators/validar-senhas-iguais';

/**
 * Componente responsável pela redefinição de senha (RF17).
 * Utiliza validação visual de requisitos e tokens de segurança.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-redefinir-senha',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
  ],
  templateUrl: './redefinir-senha.html',
  styleUrls: ['./redefinir-senha.scss'],
})
export class RedefinirSenha implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly autenticacaoService = inject(AutenticacaoService);
  private readonly dialogService = inject(DialogService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

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
  private token: string | null = null;

  /**
   * Construtor do componente.
   * Inicializa o formulário reativo (ReactiveForms) com os campos
   * e validadores necessários para a redefinição e senha.
   */
  constructor() {
    this.formulario = this.formBuilder.group(
      {
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
      { validators: validarSenhasIguais }
    );

    // Escuta as mudanças no campo 'senha' para atualizar a UI de requisitos
    this.formulario.get('senha')?.valueChanges.subscribe((valor) => {
      this.atualizarRequisitosSenha(valor || '');
    });
  }

  /**
   * Ao iniciar, captura o token da URL.
   * Se o token não estiver presente, redireciona para o login por segurança.
   */
  ngOnInit(): void {
    this.token = this.activatedRoute.snapshot.queryParamMap.get('token');

    if (!this.token) {
      this.router.navigate(['/login']);
    }
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

    if (nomeControle === 'senha' && control.invalid) {
      return 'A senha não atende aos requisitos mínimos.';
    }

    if (nomeControle === 'confirmarSenha' && control.hasError('senhasNaoConferem')) {
      return 'As senhas não conferem.';
    }
    return '';
  }
  /**
   * Manipula o evento de submissão do formulário de redefinição de senha.
   * Verifica a validade do formulário e chama o serviço de autenticação.
   */
  aoEnviar(): void {
    if (this.formulario.invalid || !this.token) {
      this.formulario.markAllAsTouched();
      return;
    }

    const dados: DadosRedefinicaoSenhaDTO = {
      token: this.token,
      senha: this.formulario.get('senha')?.value,
    };

    this.autenticacaoService.redefinirSenha(dados).subscribe({
      next: () => {
        this.dialogService
          .mostrarSucesso(
            'Senha Alterada',
            'Sua senha foi redefinida com sucesso! Você já pode acessar sua conta.'
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
