/**
 * Enum que define a natureza financeira de uma categoria.
 *
 * @author Matheus F. N. Pereira
 */
export enum TipoCategoria {
  RECEITA = 'RECEITA',
  DESPESA = 'DESPESA',
  AMBOS = 'AMBOS',
}

/**
 * Definição de tipo para os metadados visuais do tipo de categoria.
 */
export type TipoCategoriaMetadados = {
  /**
   * Cor de fundo/texto para badges (HEX).
   */
  cor: string;

  /**
   * Ícone representativo (Material Icons).
   */
  icone: string;
};

/**
 * Mapa para obter o rótulo legível (display name) de cada tipo.
 */
export const TipoCategoriaNome: Record<TipoCategoria, string> = {
  [TipoCategoria.RECEITA]: 'Receita',
  [TipoCategoria.DESPESA]: 'Despesa',
  [TipoCategoria.AMBOS]: 'Ambos (Receita/Despesa)',
};

/**
 * Configurações visuais para cada tipo.
 */
const Metadados: Record<TipoCategoria, TipoCategoriaMetadados> = {
  [TipoCategoria.RECEITA]: {
    cor: '#2E7D32',
    icone: 'arrow_circle_up',
  },
  [TipoCategoria.DESPESA]: {
    cor: '#C62828',
    icone: 'arrow_circle_down',
  },
  [TipoCategoria.AMBOS]: {
    cor: '#1565C0',
    icone: 'swap_vert',
  },
};

/**
 * Retorna os metadados visuais (cor, ícone) de um tipo.
 */
export function obterMetadadosTipoCategoria(tipo: TipoCategoria): TipoCategoriaMetadados {
  return Metadados[tipo];
}

/**
 * Retorna o estilo CSS para aplicar em Badges/Tags do tipo.
 * Uso no HTML: [ngStyle]="obterEstiloTipoCategoria(categoria.tipo)"
 */
export function obterEstiloTipoCategoria(tipo: TipoCategoria) {
  const metaDados = obterMetadadosTipoCategoria(tipo);

  return {
    'background-color': metaDados.cor,
    color: '#FFF',
    border: `1px solid ${metaDados.cor}`,
  };
}

/**
 * Retorna a lista de tipos formatada para Selects/Dropdowns.
 *
 * @returns Array de objetos { nome: 'Receita', valor: 'RECEITA' }
 */
export function obterOpcoesTipoCategoria(): { nome: string; valor: TipoCategoria }[] {
  return Object.keys(TipoCategoria).map((key) => ({
    valor: key as TipoCategoria,
    nome: TipoCategoriaNome[key as TipoCategoria],
  }));
}
