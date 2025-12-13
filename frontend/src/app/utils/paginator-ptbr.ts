import { MatPaginatorIntl } from '@angular/material/paginator';
import { Injectable } from '@angular/core';

/**
 * Serviço de internacionalização (i18n) para o componente MatPaginator.
 * Responsável por traduzir os rótulos e formatar a exibição de intervalos
 * para o padrão Português do Brasil (PT-BR).
 *
 * @author Matheus F. N. Pereira
 */
@Injectable()
export class PaginatorPtBrIntl extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Itens por página:';
  override nextPageLabel = 'Próxima página';
  override previousPageLabel = 'Página anterior';
  override firstPageLabel = 'Primeira página';
  override lastPageLabel = 'Última página';

  override getRangeLabel = (page: number, pageSize: number, length: number) => {
    if (length === 0 || pageSize === 0) {
      return `0 de ${length}`;
    }

    length = Math.max(length, 0);
    const startIndex = page * pageSize;

    const endIndex =
      startIndex < length ? Math.min(startIndex + pageSize, length) : startIndex + pageSize;

    return `${startIndex + 1} - ${endIndex} de ${length}`;
  };
}
