import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewChild,
  AfterViewInit,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Interface para definição das colunas da tabela.
 */
export interface ColunaTabela {
  chave: string;
  titulo: string;
  formatador?: (valor: any) => string;
  obterEstilo?: (valor: any) => { [klass: string]: any };
  caminhoOrdenacao?: string;
}

/**
 * Componente genérico de Tabela (Wrapper do MatTable).
 * Gerencia a exibição de dados, paginação e ações padrão (Editar/Excluir).
 *
 * @author Matheus F. N. Pereira
 */
@Component({
  selector: 'app-tabela-base',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './tabela-base.html',
  styleUrl: './tabela-base.scss',
})
export class TabelaBase implements AfterViewInit, OnChanges {
  @Input({ required: true }) colunas: ColunaTabela[] = [];
  @Input({ required: true }) dados: any[] = [];

  @Input() desabilitarAcoes?: (item: any) => boolean;

  @Output() editar = new EventEmitter<any>();
  @Output() excluir = new EventEmitter<any>();

  dataSource = new MatTableDataSource<any>([]);
  colunasExibidas: string[] = [];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  /**
   * Atualiza o DataSource e as Colunas quando os Inputs mudam.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dados']) {
      this.atualizarFonteDados();
    }

    if (changes['colunas']) {
      this.colunasExibidas = [...this.colunas.map((c) => c.chave), 'acoes'];
    }
  }

  /**
   * Conecta o Paginator e o Sort ao DataSource após a visualização carregar.
   */
  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  /**
   * Verifica se a ação deve estar desabilitada para o item atual.
   */
  verificarAcaoDesabilitada(item: any): boolean {
    return this.desabilitarAcoes ? this.desabilitarAcoes(item) : false;
  }

  /**
   * Emite o evento com o objeto completo a ser editado.
   */
  aoEditar(item: any) {
    this.editar.emit(item);
  }

  /**
   * Emite o evento com o objeto completo a ser excluído.
   */
  aoExcluir(item: any) {
    this.excluir.emit(item);
  }

  /**
   * Recria o DataSource com os novos dados recebidos e configura a lógica de ordenação personalizada.
   */
  private atualizarFonteDados() {
    this.dataSource = new MatTableDataSource(this.dados);

    this.dataSource.sortingDataAccessor = (item: any, property: string) => {
      const coluna = this.colunas.find((c) => c.chave === property);

      if (coluna?.caminhoOrdenacao) {
        return this.obterValorAninhado(item, coluna.caminhoOrdenacao);
      }

      return item[property];
    };

    this.dataSource.sort = this.sort;
  }

  /**
   * Função auxiliar para acessar propriedades profundas dentro de objetos (ex: 'categoria.nome').
   * Utilizada pelo sortingDataAccessor para permitir ordenar colunas que exibem objetos.
   *
   * @param objeto O objeto da linha atual (ex: ContaDTO).
   * @param caminho O caminho da propriedade separado por pontos (ex: 'pessoa.nome').
   * @returns O valor final encontrado (string/number) ou null se o caminho for inválido.
   */
  private obterValorAninhado(objeto: any, caminho: string): string | number {
    return caminho.split('.').reduce((acc, part) => (acc ? acc[part] : null), objeto);
  }
}
