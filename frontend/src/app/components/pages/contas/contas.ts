import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ContainerFiltro } from '../../shared/container-filtro/container-filtro';
import { GridBase } from '../../shared/grid-base/grid-base';
import { CardBase } from '../../shared/card-base/card-base';
import { ColunaTabela, TabelaBase } from '../../shared/tabela-base/tabela-base';
import { AlternadorVisualizacao } from '../../shared/alternador-visualizacao/alternador-visualizacao';
import { BotaoAdicionar } from '../../shared/botao-adicionar/botao-adicionar';
import { Dialog as DialogService } from '../../../services/dialog';
import { Conta as ContaService } from '../../../services/conta';
import { ContaDTO } from '../../../dtos/conta/ContaDTO';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import {
  InstituicaoFinanceira,
  InstituicaoFinanceiraNome,
  obterOpcoesInstituicoes,
} from '../../../enums/InstituicaoFinanceira';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { MatSelectModule } from '@angular/material/select';
import { InstituicaoEstilo } from '../../../pipes/instituicao-estilo';
import { InstituicaoIcone } from '../../../pipes/instituicao-icone';
import { InstituicaoNome } from '../../../pipes/instituicao-nome';

/**
 * Componente de página responsável pela listagem e gestão de Contas Bancárias.
 *
 * Apresenta os dados recuperados do backend em dois modos de visualização
 * (Cards ou Tabela) e gerencia a filtragem local e ações de CRUD.
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-contas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    ContainerFiltro,
    AlternadorVisualizacao,
    BotaoAdicionar,
    GridBase,
    CardBase,
    TabelaBase,
    InstituicaoNome,
    InstituicaoIcone,
    InstituicaoEstilo,
  ],
  templateUrl: './contas.html',
  styleUrl: './contas.scss',
})
export class Contas implements OnInit {
  private readonly contaService = inject(ContaService);
  private readonly dialogService = inject(DialogService);
  private readonly pessoaService = inject(PessoaService);

  /**
   * Define o modo de exibição atual da lista.
   * - true: Exibe Grid de Cards (Ideal para Mobile/Visual).
   * - false: Exibe Tabela (Ideal para Desktop/Dados densos).
   */
  visualizacaoEmCards = true;

  /**
   * Armazena a lista completa de contas recuperada do Backend (Cache local).
   * Usada como referência para restaurar os dados quando o filtro é limpo.
   */
  contas: ContaDTO[] = [];

  /**
   * Lista efetivamente renderizada na tela.
   * Pode conter todos os registros ou apenas os que correspondem ao filtro.
   */
  contasFiltradas: ContaDTO[] = [];

  /**
   * Objeto de estado do filtro de pesquisa.
   */
  filtro = {
    nome: '',
    pessoasIds: [] as string[],
    instituicoes: [] as InstituicaoFinanceira[],
  };

  /**
   * Lista de pessoas para popular o Select de filtro.
   */
  opcoesPessoas: PessoaDTO[] = [];

  /**
   * Lista de opções de instituições financeiras para o select.
   */
  opcoesInstituicoes = obterOpcoesInstituicoes();

  /**
   * Configuração das colunas para o componente genérico de Tabela.
   * Utiliza formatadores para transformar Enums e Valores Monetários em texto amigável.
   */
  colunasTabela: ColunaTabela[] = [
    { chave: 'nome', titulo: 'Nome da Conta' },
    {
      chave: 'instituicao',
      titulo: 'Instituição',
      formatador: (valor: InstituicaoFinanceira) => InstituicaoFinanceiraNome[valor],
    },
    {
      chave: 'pessoa',
      titulo: 'Titular',
      caminhoOrdenacao: 'pessoa.nome',
      formatador: (pessoa: PessoaDTO) => pessoa.nome,
    },
    {
      chave: 'saldoAtual',
      titulo: 'Saldo',
      formatador: (valor: number) =>
        new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor),
    },
  ];

  /**
   * Inicializa o componente carregando os dados do servidor.
   */
  ngOnInit(): void {
    this.carregarDados();
    this.carregarOpcoesFiltro();
  }

  /**
   * Busca a lista de contas do backend e inicializa a visualização.
   *
   * Atualiza tanto a lista "cache" quanto a lista filtrada.
   */
  carregarDados() {
    this.contaService.listar().subscribe({
      next: (dados) => {
        this.contas = dados;
        this.filtrar();
      },
    });
  }

  /**
   * Carrega a lista de pessoas (apenas titulares) para preencher o filtro.
   */
  carregarOpcoesFiltro() {
    this.pessoaService.listar().subscribe((todos) => {
      this.opcoesPessoas = todos
        .filter((p) => p.titular)
        .sort((a, b) => a.nome.localeCompare(b.nome));
    });
  }

  /**
   * Lógica de Filtragem Avançada
   */
  filtrar() {
    const termo = this.filtro.nome.trim().toLowerCase();
    const idsPessoas = this.filtro.pessoasIds;
    const idsInst = this.filtro.instituicoes;

    this.contasFiltradas = this.contas.filter((c) => {
      const matchesNome = c.nome.toLowerCase().includes(termo);

      const matchesPessoa = idsPessoas.length === 0 || idsPessoas.includes(c.pessoa.id);

      const matchesInst = idsInst.length === 0 || idsInst.includes(c.instituicao);

      return matchesNome && matchesPessoa && matchesInst;
    });
  }

  /**
   * Limpa os campos de filtro e restaura a lista completa.
   */
  limpar() {
    this.filtro.nome = '';
    this.filtro.pessoasIds = [];
    this.filtro.instituicoes = [];
    this.filtrar();
  }

  /**
   * Abre o modal de cadastro.
   * Passa explicitamente a ação 'cadastrar'.
   */
  aoAdicionar() {
    this.dialogService.abrirFormularioConta('cadastrar').subscribe({
      next: (resultado) => {
        if (resultado && typeof resultado === 'object') {
          this.carregarDados();
        }
      },
    });
  }

  /**
   * Abre o modal de edição.
   * Passa explicitamente a ação 'editar' e o objeto.
   */
  aoEditar(conta: ContaDTO) {
    this.dialogService.abrirFormularioConta('editar', conta).subscribe({
      next: (resultado) => {
        if (resultado && typeof resultado === 'object') {
          this.carregarDados();
        }
      },
    });
  }

  /**
   * Abre o modal de exclusão.
   * Passa explicitamente a ação 'excluir' e o objeto.
   */
  aoExcluir(conta: ContaDTO) {
    this.dialogService.abrirFormularioConta('excluir', conta).subscribe({
      next: (sucesso) => {
        if (sucesso === true) {
          this.carregarDados();
        }
      },
    });
  }
}
