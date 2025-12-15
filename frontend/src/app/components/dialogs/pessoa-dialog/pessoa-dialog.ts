import { Component, inject, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import {
  BaseFormularioDialog,
  TipoOperacao,
} from '../base-formulario-dialog/base-formulario-dialog';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { CriarAtualizarPessoaDTO } from '../../../dtos/pessoa/CriarAtualizarPessoaDTO';
import { finalize } from 'rxjs';

/**
 * Interface que define os dados esperados pelo Dialog ao ser aberto.
 */
export interface DadosPessoaDialog {
  pessoa?: PessoaDTO;
  acao: TipoOperacao;
}

/**
 * Componente de Dialog responsável pelo formulário de criação, edição e exclusão de Pessoas.
 * * Utiliza o wrapper `app-base-formulario-dialog` para manter o layout padrão
 * e gerencia a lógica específica dos campos e integração com o serviço.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-pessoa-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    BaseFormularioDialog,
  ],
  templateUrl: './pessoa-dialog.html',
  styleUrl: './pessoa-dialog.scss',
})
export class PessoaDialog implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PessoaDialog>);
  private readonly pessoaService = inject(PessoaService);

  /** Formulário reativo principal do componente. */
  formulario: FormGroup;

  /** Título dinâmico exibido no cabeçalho (Nova/Editar/Excluir). */
  titulo: string = '';

  /** Operação atual que define o comportamento dos botões. */
  operacao: TipoOperacao = 'cadastrar';

  /** Controle de estado para exibir spinner e bloquear ações. */
  processando = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: DadosPessoaDialog) {
    this.formulario = this.formBuilder.group({
      id: [null],
      nome: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.configurarDialog();
  }

  /**
   * Obtém a mensagem de erro de validação para um controle específico.
   * Usado no template para feedback visual.
   *
   * @param nomeControle O nome do FormControl.
   * @returns A mensagem de erro ou string vazia.
   */
  obterMensagemErro(nomeControle: string): string {
    const control = this.formulario.get(nomeControle);

    if (!control) return '';

    if (control.hasError('required')) {
      return 'Este campo é obrigatório.';
    }

    return '';
  }

  /**
   * Configura o estado inicial do modal com base nos dados recebidos (`this.data`).
   * Define o título, preenche o formulário (se edição) e bloqueia campos (se exclusão).
   */
  private configurarDialog() {
    this.operacao = this.data.acao;

    if (this.operacao === 'cadastrar') {
      this.titulo = 'Nova Pessoa';
    } else {
      this.titulo = this.operacao === 'editar' ? 'Editar Pessoa' : 'Excluir Pessoa';

      if (this.data.pessoa) {
        this.formulario.patchValue(this.data.pessoa);
      }

      if (this.operacao === 'excluir') {
        this.formulario.disable();
      }
    }
  }

  /**
   * Método público chamado pelo evento `confirmar` do componente base.
   * Redireciona para `salvar()` ou `excluir()` dependendo da operação.
   */
  confirmarAcao() {
    if (this.operacao === 'excluir') {
      this.excluir();
    } else {
      this.salvar();
    }
  }

  /**
   * Envia os dados do formulário para criação ou atualização via API.
   * Fecha o modal retornando o objeto salvo em caso de sucesso.
   */
  private salvar() {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.processando = true;

    const { id, ...dadosForm } = this.formulario.getRawValue();
    const dto: CriarAtualizarPessoaDTO = dadosForm;

    const requisicao =
      this.operacao === 'cadastrar'
        ? this.pessoaService.cadastrar(dto)
        : this.pessoaService.atualizar(id, dto);

    requisicao.pipe(finalize(() => (this.processando = false))).subscribe({
      next: (pessoaRetornada) => {
        this.dialogRef.close(pessoaRetornada);
      },
    });
  }

  /**
   * Solicita a exclusão do registro via API.
   * Fecha o modal retornando `true` em caso de sucesso.
   */
  private excluir() {
    this.processando = true;
    const id = this.formulario.get('id')?.value;

    this.pessoaService
      .excluir(id)
      .pipe(finalize(() => (this.processando = false)))
      .subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
      });
  }

  /**
   * Fecha o modal sem realizar ações (cancelar).
   */
  fechar() {
    this.dialogRef.close();
  }
}
