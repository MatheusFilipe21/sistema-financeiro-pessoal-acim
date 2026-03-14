import { Component, inject, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import {
  BaseFormularioDialog,
  TipoOperacao,
} from '../base-formulario-dialog/base-formulario-dialog';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { CriarAtualizarPessoaDTO } from '../../../dtos/pessoa/CriarAtualizarPessoaDTO';
import { finalize } from 'rxjs';
import { Dialog as DialogService } from '../../../services/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Interface que define os dados esperados pelo Dialog ao ser aberto.
 */
export interface DadosPessoaDialog {
  pessoa?: PessoaDTO;
  acao: TipoOperacao;
}

/**
 * Componente de Dialog responsável pelo formulário de criação, edição e exclusão de Pessoas.
 * Utiliza o wrapper `app-base-formulario-dialog` para manter o layout padrão
 * e gerencia a lógica específica dos campos e integração com o serviço.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-pessoa-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    BaseFormularioDialog,
  ],
  templateUrl: './pessoa-dialog.html',
  styleUrl: './pessoa-dialog.scss',
})
export class PessoaDialog implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PessoaDialog>);
  private readonly pessoaService = inject(PessoaService);
  private readonly dialogService = inject(DialogService);

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
      titular: [false],
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

  /**
   * Abre o dialog informativo para explicar a regra de titularidade.
   */
  abrirInfoTitular(): void {
    this.dialogService.mostrarInfo(
      'Regra de Titularidade',
      `Apenas pessoas marcadas como <b>Titulares</b> podem possuir <b>Contas Bancárias</b> e <b>Cartões de Crédito</b> no sistema.
        
        Pessoas <b>não Titulares</b> servem apenas para categorizar <b>quem gerou a Transação</b>.

        <hr class="my-3 border-gray-200">
        Exemplos Práticos:
        <ul>
          <li><b>Você</b> (Titular): É o dono da conta no Mercado Pago.</li>
          <li><b>Cônjuge</b> (Titular): É o dono da conta no Itaú.</li>
          <li><b>Seu Filho</b> (Não Titular): Não tem conta no sistema.</li>
        </ul>
        Se na sua casa o controle de gastos é centralizado, então você registra as contas de ambos (Você e Cônjuge) para organizar a vida financeira em conjunto.

        Ao pagar a escola, você usa a sua Conta, mas marca que a despesa foi do seu filho. Assim, os relatórios mostram quanto você gastou com ele.`,
      'Entendi',
    );
  }
}
