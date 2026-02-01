import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { ContainerFiltro } from '../../shared/container-filtro/container-filtro';
import { ColunaTabela, TabelaBase } from '../../shared/tabela-base/tabela-base';
import { BotaoAdicionar } from '../../shared/botao-adicionar/botao-adicionar';

import { Categoria as CategoriaService } from '../../../services/categoria';
import { Dialog as DialogService } from '../../../services/dialog';
import {
  obterEstiloTipoCategoria,
  obterOpcoesTipoCategoria,
  TipoCategoria,
  TipoCategoriaNome,
} from '../../../enums/TipoCategoria';
import { CategoriaDTO } from '../../../dtos/categoria/CategoriaDTO';

/**
 * Componente de página responsável pela listagem e gestão de Categorias.
 *
 * Apresenta os dados recuperados do backend e gerencia a filtragem local
 * e ações de CRUD (Editar/Excluir).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ContainerFiltro,
    BotaoAdicionar,
    TabelaBase,
  ],
  templateUrl: './categorias.html',
  styleUrl: './categorias.scss',
})
export class Categorias implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  private readonly dialogService = inject(DialogService);

  /**
   * Helper para uso no Template.
   */
  readonly opcoesTipo = obterOpcoesTipoCategoria().filter(
    (opcao) => opcao.valor !== TipoCategoria.AMBOS,
  );

  /**
   * Armazena a lista completa (Cache local).
   */
  categorias: CategoriaDTO[] = [];

  /**
   * Lista efetivamente renderizada (Filtrada).
   */
  categoriasFiltradas: CategoriaDTO[] = [];

  /**
   * Objeto de estado do filtro de pesquisa.
   */
  filtro = {
    nome: '',
    tipo: null as TipoCategoria | null,
  };

  /**
   * Configuração das colunas para o componente genérico de Tabela.
   */
  colunasTabela: ColunaTabela[] = [
    { chave: 'nome', titulo: 'Nome' },
    {
      chave: 'tipo',
      titulo: 'Tipo',
      formatador: (valor: TipoCategoria) => TipoCategoriaNome[valor],
      obterEstilo: (valor: TipoCategoria) => ({
        color: obterEstiloTipoCategoria(valor)['background-color'],
        fontWeight: 'bold',
      }),
    },
    {
      chave: 'sistema',
      titulo: 'Origem',
      formatador: (sistema: boolean) => (sistema ? 'Padrão do Sistema' : 'Personalizada'),
    },
  ];

  /**
   * Inicializa o componente carregando os dados.
   */
  ngOnInit(): void {
    this.carregarDados();
  }

  /**
   * Busca a lista do backend e inicializa a visualização.
   */
  carregarDados() {
    this.categoriaService.listar().subscribe({
      next: (dados) => {
        this.categorias = dados;
        this.filtrar();
      },
    });
  }

  /**
   * Filtra a lista localmente baseada no input do usuário.
   */
  filtrar() {
    const termo = this.filtro.nome.trim().toLowerCase();

    let resultado = this.categorias;

    if (termo) {
      resultado = resultado.filter((c) => c.nome.toLowerCase().includes(termo));
    }

    if (this.filtro.tipo) {
      resultado = resultado.filter(
        (c) => c.tipo === this.filtro.tipo || c.tipo === TipoCategoria.AMBOS,
      );
    }

    this.categoriasFiltradas = resultado;
  }

  /**
   * Limpa os campos de filtro e restaura a lista.
   */
  limpar() {
    this.filtro.nome = '';
    this.filtro.tipo = null;
    this.filtrar();
  }

  /**
   * Verifica se a ação está bloqueada.
   */
  verificarBloqueioAcao = (categoria: CategoriaDTO) => {
    return categoria.sistema;
  };

  /**
   * Abre o modal de cadastro.
   */
  aoAdicionar() {
    this.dialogService.abrirFormularioCategoria('cadastrar').subscribe({
      next: (resultado) => {
        if (resultado) {
          this.carregarDados();
        }
      },
    });
  }

  /**
   * Abre o modal de edição.
   */
  aoEditar(categoria: CategoriaDTO) {
    if (categoria.sistema) {
      return;
    }
    this.dialogService.abrirFormularioCategoria('editar', categoria).subscribe({
      next: (resultado) => {
        if (resultado) {
          this.carregarDados();
        }
      },
    });
  }

  /**
   * Abre o modal de exclusão.
   */
  aoExcluir(categoria: CategoriaDTO) {
    if (categoria.sistema) {
      return;
    }
    this.dialogService.abrirFormularioCategoria('excluir', categoria).subscribe({
      next: (sucesso) => {
        if (sucesso) {
          this.carregarDados();
        }
      },
    });
  }
}
