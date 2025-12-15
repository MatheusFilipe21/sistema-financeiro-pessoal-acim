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
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';
import { AlternadorVisualizacao } from '../../shared/alternador-visualizacao/alternador-visualizacao';
import { BotaoAdicionar } from '../../shared/botao-adicionar/botao-adicionar';
import { Dialog as DialogService } from '../../../services/dialog';

/**
 * Componente de página responsável pela listagem e gestão de Pessoas.
 *
 * Apresenta os dados recuperados do backend em dois modos de visualização
 * (Cards ou Tabela) e gerencia a filtragem local e ações de CRUD (Editar/Excluir).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-pessoas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    ContainerFiltro,
    AlternadorVisualizacao,
    BotaoAdicionar,
    GridBase,
    CardBase,
    TabelaBase,
  ],
  templateUrl: './pessoas.html',
  styleUrl: './pessoas.scss',
})
export class Pessoas implements OnInit {
  private readonly pessoaService = inject(PessoaService);
  private readonly dialogService = inject(DialogService);

  /**
   * Define o modo de exibição atual da lista.
   * - true: Exibe Grid de Cards (Ideal para Mobile/Visual).
   * - false: Exibe Tabela (Ideal para Desktop/Dados densos).
   */
  visualizacaoEmCards = true;

  /**
   * Armazena a lista completa de pessoas recuperada do Backend (Cache local).
   * Usada como referência para restaurar os dados quando o filtro é limpo.
   */
  pessoas: PessoaDTO[] = [];

  /**
   * Lista efetivamente renderizada na tela.
   * Pode conter todos os registros ou apenas os que correspondem ao filtro.
   */
  pessoasFiltradas: PessoaDTO[] = [];

  /**
   * Objeto de estado do filtro de pesquisa.
   */
  filtro = {
    nome: '',
  };

  /**
   * Configuração das colunas para o componente genérico de Tabela.
   */
  colunasTabela: ColunaTabela[] = [{ chave: 'nome', titulo: 'Nome' }];

  /**
   * Inicializa o componente carregando os dados do servidor.
   */
  ngOnInit(): void {
    this.carregarDados();
  }

  /**
   * (RF47) Busca a lista de pessoas do backend e inicializa a visualização.
   *
   * Atualiza tanto a lista "cache" quanto a lista filtrada.
   */
  carregarDados() {
    this.pessoaService.listar().subscribe({
      next: (dados) => {
        this.pessoas = dados;
        this.filtrar();
      },
    });
  }

  /**
   * Filtra a lista localmente baseada no input do usuário.
   *
   * <p>
   * Acionado pelo evento (ngModelChange) do input para feedback instantâneo.
   * A busca é insensível a maiúsculas/minúsculas (case-insensitive).
   */
  filtrar() {
    const termo = this.filtro.nome.trim().toLowerCase();

    if (termo) {
      this.pessoasFiltradas = this.pessoas.filter((p) => p.nome.toLowerCase().includes(termo));
    } else {
      this.pessoasFiltradas = [...this.pessoas];
    }
  }

  /**
   * Limpa os campos de filtro e restaura a lista completa.
   */
  limpar() {
    this.filtro.nome = '';
    this.filtrar();
  }

  /**
   * Abre o modal de cadastro.
   * Passa explicitamente a ação 'cadastrar'.
   */
  aoAdicionar() {
    this.dialogService.abrirFormularioPessoa('cadastrar').subscribe({
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
  aoEditar(pessoa: PessoaDTO) {
    this.dialogService.abrirFormularioPessoa('editar', pessoa).subscribe({
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
  aoExcluir(pessoa: PessoaDTO) {
    this.dialogService.abrirFormularioPessoa('excluir', pessoa).subscribe({
      next: (sucesso) => {
        if (sucesso === true) {
          this.carregarDados();
        }
      },
    });
  }
}
