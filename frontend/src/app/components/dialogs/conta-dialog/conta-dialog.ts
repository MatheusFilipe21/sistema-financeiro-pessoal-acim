import { Component, inject, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { finalize } from 'rxjs';
import { ContaDTO } from '../../../dtos/conta/ContaDTO';
import {
  BaseFormularioDialog,
  TipoOperacao,
} from '../base-formulario-dialog/base-formulario-dialog';
import { Conta as ContaService } from '../../../services/conta';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import { obterOpcoesInstituicoes } from '../../../enums/InstituicaoFinanceira';
import { CriarAtualizarContaDTO } from '../../../dtos/conta/CriarAtualizarContaDTO';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { FormatacaoDecimal } from '../../../directives/formatacao-decimal';

/**
 * Interface que define os dados esperados pelo Dialog ao ser aberto.
 */
export interface DadosContaDialog {
  conta?: ContaDTO;
  acao: TipoOperacao;
}

/**
 * Componente de Dialog responsável pelo formulário de criação, edição e exclusão de Contas.
 *
 * Utiliza o wrapper `app-base-formulario-dialog` para manter o layout padrão
 * e gerencia a lógica específica dos campos, carregamento de titulares e integração com serviços.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-conta-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    BaseFormularioDialog,
    FormatacaoDecimal,
  ],
  templateUrl: './conta-dialog.html',
  styleUrl: './conta-dialog.scss',
})
export class ContaDialog implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ContaDialog>);
  private readonly contaService = inject(ContaService);
  private readonly pessoaService = inject(PessoaService);

  /**
   * Formulário reativo principal do componente.
   */
  formulario: FormGroup;

  /**
   * Título dinâmico exibido no cabeçalho (Nova/Editar/Excluir).
   */
  titulo: string = '';

  /**
   * Operação atual que define o comportamento dos botões e bloqueio de campos.
   */
  operacao: TipoOperacao = 'cadastrar';

  /**
   * Controle de estado para exibir spinner e bloquear ações durante requisições.
   */
  processando = false;

  /**
   * Lista de pessoas disponíveis para vincular (Apenas Titulares).
   * Carregada via serviço ao iniciar o componente.
   */
  pessoas: PessoaDTO[] = [];

  /**
   * Lista de opções de instituições financeiras para o select.
   */
  opcoesInstituicao = obterOpcoesInstituicoes();

  constructor(@Inject(MAT_DIALOG_DATA) public data: DadosContaDialog) {
    this.formulario = this.formBuilder.group({
      id: [null],
      nome: ['', [Validators.required]],
      instituicao: [null, [Validators.required]],
      pessoaId: [null, [Validators.required]],
      saldoInicial: ['', [Validators.required]],
    });
  }

  /**
   * Inicializa o componente carregando os dados necessários (Lista de Pessoas).
   */
  ngOnInit(): void {
    this.carregarPessoas();
  }

  /**
   * Carrega a lista completa de pessoas do backend e aplica a regra de negócio:
   * Apenas pessoas marcadas como `titular: true` podem ser vinculadas a uma conta.
   *
   * Após carregar, chama a configuração do dialog para garantir que o patchValue
   * (no caso de edição) funcione corretamente com a lista já populada.
   */
  private carregarPessoas() {
    this.pessoaService.listar().subscribe({
      next: (todasPessoas) => {
        this.pessoas = todasPessoas
          .filter((p) => p.titular)
          .sort((a, b) => a.nome.localeCompare(b.nome));

        this.formulario.get('pessoaId')?.updateValueAndValidity({ emitEvent: true });

        this.configurarDialog();
      },
      error: () => {
        this.configurarDialog();
      },
    });
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

    if (control.hasError('required')) return 'Este campo é obrigatório.';

    return '';
  }

  /**
   * Configura o estado inicial do modal com base nos dados recebidos (`this.data`).
   * Define o título, preenche o formulário (se edição) e bloqueia campos (se exclusão).
   */
  private configurarDialog() {
    this.operacao = this.data.acao;

    if (this.operacao === 'cadastrar') {
      this.titulo = 'Nova Conta';
    } else {
      this.titulo = this.operacao === 'editar' ? 'Editar Conta' : 'Excluir Conta';

      if (this.data.conta) {
        const saldoFormatado =
          this.data.conta.saldoInicial === undefined
            ? '0.00'
            : this.data.conta.saldoInicial.toFixed(2);

        const dados = {
          ...this.data.conta,
          saldoInicial: saldoFormatado,
          pessoaId: this.data.conta.pessoa.id,
        };

        this.formulario.patchValue(dados);
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

    const { id, ...dadosFormulario } = this.formulario.getRawValue();
    const dto: CriarAtualizarContaDTO = dadosFormulario;

    const requisicao =
      this.operacao === 'cadastrar'
        ? this.contaService.cadastrar(dto)
        : this.contaService.atualizar(id, dto);

    requisicao.pipe(finalize(() => (this.processando = false))).subscribe({
      next: (contaRetornada) => {
        this.dialogRef.close(contaRetornada);
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

    this.contaService
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
