import { Component, inject, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { finalize } from 'rxjs';
import { CategoriaDTO } from '../../../dtos/categoria/CategoriaDTO';
import { CriarAtualizarCategoriaDTO } from '../../../dtos/categoria/CriarAtualizarCategoriaDTO';
import { Categoria as CategoriaService } from '../../../services/categoria';
import {
  BaseFormularioDialog,
  TipoOperacao,
} from '../base-formulario-dialog/base-formulario-dialog';
import { obterOpcoesTipoCategoria } from '../../../enums/TipoCategoria';

/**
 * Interface que define os dados esperados pelo Dialog ao ser aberto.
 */
export interface DadosCategoriaDialog {
  categoria?: CategoriaDTO;
  acao: TipoOperacao;
}

/**
 * Componente de Dialog responsável pelo formulário de criação, edição e exclusão de Categorias.
 * Utiliza o wrapper `app-base-formulario-dialog` para manter o layout padrão
 * e gerencia a lógica específica dos campos e integração com o serviço.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-categoria-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    BaseFormularioDialog,
  ],
  templateUrl: './categoria-dialog.html',
  styleUrl: './categoria-dialog.scss',
})
export class CategoriaDialog implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<CategoriaDialog>);
  private readonly categoriaService = inject(CategoriaService);

  /** Formulário reativo principal do componente. */
  formulario: FormGroup;

  /** Título dinâmico exibido no cabeçalho (Nova/Editar/Excluir). */
  titulo: string = '';

  /** Operação atual que define o comportamento dos botões. */
  operacao: TipoOperacao = 'cadastrar';

  /** Controle de estado para exibir spinner e bloquear ações. */
  processando = false;

  /** Tipos de categorias disponíveis. */
  readonly opcoesTipo = obterOpcoesTipoCategoria();

  /** Lista simplificada de cores úteis para finanças */
  readonly coresDisponiveis = [
    '#F44336',
    '#E91E63',
    '#9C27B0',
    '#673AB7',
    '#3F51B5',
    '#2196F3',
    '#03A9F4',
    '#00BCD4',
    '#009688',
    '#4CAF50',
    '#8BC34A',
    '#CDDC39',
    '#FFEB3B',
    '#FFC107',
    '#FF9800',
    '#FF5722',
    '#795548',
    '#9E9E9E',
    '#607D8B',
    '#000000',
  ];

  /** Lista simplificada de ícones úteis para finanças */
  readonly iconesDisponiveis = [
    'category',
    'restaurant',
    'shopping_cart',
    'directions_car',
    'home',
    'flight',
    'school',
    'medical_services',
    'pets',
    'work',
    'savings',
    'attach_money',
    'trending_up',
    'trending_down',
    'account_balance',
    'credit_card',
    'receipt',
    'phone_android',
    'wifi',
    'lightbulb',
    'local_gas_station',
    'fitness_center',
  ];

  constructor(@Inject(MAT_DIALOG_DATA) public data: DadosCategoriaDialog) {
    this.formulario = this.formBuilder.group({
      id: [null],
      nome: ['', [Validators.required]],
      tipo: [null, [Validators.required]],
      icone: ['category', [Validators.required]],
      cor: ['#F44336', [Validators.required]],
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
      this.titulo = 'Nova Categoria';
    } else {
      this.titulo = this.operacao === 'editar' ? 'Editar Categoria' : 'Excluir Categoria';
      if (this.data.categoria) {
        this.formulario.patchValue(this.data.categoria);
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
    const dto: CriarAtualizarCategoriaDTO = dadosForm;

    const requisicao =
      this.operacao === 'cadastrar'
        ? this.categoriaService.cadastrar(dto)
        : this.categoriaService.atualizar(id, dto);

    requisicao.pipe(finalize(() => (this.processando = false))).subscribe({
      next: (categoriaRetornada) => {
        this.dialogRef.close(categoriaRetornada);
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

    this.categoriaService
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
   * Método auxiliar para selecionar o ícone ao clicar na lista.
   */
  selecionarIcone(icone: string) {
    if (this.operacao !== 'excluir') {
      this.formulario.get('icone')?.setValue(icone);
    }
  }

  /**
   * Método auxiliar para selecionar a cor ao clicar na lista.
   */
  selecionarCor(cor: string) {
    if (this.operacao !== 'excluir') {
      this.formulario.get('cor')?.setValue(cor);
    }
  }
}
