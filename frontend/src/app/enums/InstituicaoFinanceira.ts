/**
 * Enum que representa as Instituições Financeiras suportadas pelo sistema.
 *
 * @author Matheus F. N. Pereira
 */
export enum InstituicaoFinanceira {
  CAIXA = 'CAIXA',
  BB = 'BB',
  ITAU = 'ITAU',
  BRADESCO = 'BRADESCO',
  SANTANDER = 'SANTANDER',
  SAFRA = 'SAFRA',
  BANRISUL = 'BANRISUL',
  VOTORANTIM = 'VOTORANTIM',
  BANCO_NORDESTE = 'BANCO_NORDESTE',
  BRB = 'BRB',
  NUBANK = 'NUBANK',
  INTER = 'INTER',
  C6 = 'C6',
  PAGBANK = 'PAGBANK',
  MERCADO_PAGO = 'MERCADO_PAGO',
  PICPAY = 'PICPAY',
  NEON = 'NEON',
  NEXT = 'NEXT',
  PAN = 'PAN',
  AGIBANK = 'AGIBANK',
  BTG = 'BTG',
  XP = 'XP',
  RICO = 'RICO',
  DINHEIRO = 'DINHEIRO',
  OUTROS = 'OUTROS',
}

/**
 * Definição de tipo para os metadados visuais da instituição.
 */
export type InstituicaoFinanceiraMetadados = {
  /**
   * Cor de fundo principal (HEX).
   */
  corPrimaria: string;

  /**
   * Cor do texto para garantir contraste (HEX).
   */
  corTexto: string;

  /**
   * Nome do ícone do Material Design.
   */
  icone: string;
};

/**
 * Mapa auxiliar para obter o rótulo legível (display name) de cada instituição.
 *
 * @example
 * InstituicaoFinanceiraNome[InstituicaoFinanceira.MERCADO_PAGO] // Retorna "Mercado Pago"
 */
export const InstituicaoFinanceiraNome: Record<InstituicaoFinanceira, string> = {
  [InstituicaoFinanceira.CAIXA]: 'Caixa Econômica Federal',
  [InstituicaoFinanceira.BB]: 'Banco do Brasil',
  [InstituicaoFinanceira.ITAU]: 'Itaú',
  [InstituicaoFinanceira.BRADESCO]: 'Bradesco',
  [InstituicaoFinanceira.SANTANDER]: 'Santander',
  [InstituicaoFinanceira.SAFRA]: 'Banco Safra',
  [InstituicaoFinanceira.BANRISUL]: 'Banrisul',
  [InstituicaoFinanceira.VOTORANTIM]: 'Banco BV',
  [InstituicaoFinanceira.BANCO_NORDESTE]: 'Banco do Nordeste',
  [InstituicaoFinanceira.BRB]: 'BRB - Banco de Brasília',
  [InstituicaoFinanceira.NUBANK]: 'Nubank',
  [InstituicaoFinanceira.INTER]: 'Banco Inter',
  [InstituicaoFinanceira.C6]: 'C6 Bank',
  [InstituicaoFinanceira.PAGBANK]: 'PagBank',
  [InstituicaoFinanceira.MERCADO_PAGO]: 'Mercado Pago',
  [InstituicaoFinanceira.PICPAY]: 'PicPay',
  [InstituicaoFinanceira.NEON]: 'Neon',
  [InstituicaoFinanceira.NEXT]: 'Next',
  [InstituicaoFinanceira.PAN]: 'Banco Pan',
  [InstituicaoFinanceira.AGIBANK]: 'Agibank',
  [InstituicaoFinanceira.BTG]: 'BTG Pactual',
  [InstituicaoFinanceira.XP]: 'XP Investimentos',
  [InstituicaoFinanceira.RICO]: 'Rico',
  [InstituicaoFinanceira.DINHEIRO]: 'Dinheiro em Espécie',
  [InstituicaoFinanceira.OUTROS]: 'Outros',
};

/**
 * Retorna os metadados visuais (cor, ícone) de uma instituição.
 * Possui fallback para 'OUTROS' caso não encontre.
 */
export function obterMetadadosInstituicao(
  instituicaoFinanceira: InstituicaoFinanceira
): InstituicaoFinanceiraMetadados {
  return Metadados[instituicaoFinanceira] || Metadados[InstituicaoFinanceira.OUTROS];
}

/**
 * Retorna o estilo CSS para aplicar em Badges/Tags.
 * Uso no HTML: [ngStyle]="obterEstiloInstituicao(conta.instituicao)"
 */
export function obterEstiloInstituicao(instituicaoFinanceira: InstituicaoFinanceira) {
  const metaDados = obterMetadadosInstituicao(instituicaoFinanceira);

  return {
    'background-color': metaDados.corPrimaria,
    color: metaDados.corTexto,
    border: `1px solid ${metaDados.corPrimaria}`,
  };
}

/**
 * Retorna a lista de instituições formatada.
 * Já retorna ordenado alfabeticamente pelo nome.
 *
 * @returns Array de objetos { nome: 'Mercado Pago', valor: 'MERCADO_PAGO' }
 */
export function obterOpcoesInstituicoes(): { nome: string; valor: InstituicaoFinanceira }[] {
  return Object.keys(InstituicaoFinanceira)
    .map((key) => ({
      valor: key as InstituicaoFinanceira,
      nome: InstituicaoFinanceiraNome[key as InstituicaoFinanceira],
    }))
    .sort((a, b) => a.nome.localeCompare(b.nome));
}

/**
 * Configurações visuais (Cores, Contraste e Ícones) para cada instituição.
 * Utilizado para renderizar badges, cards e avatares.
 */
const Metadados: Record<InstituicaoFinanceira, InstituicaoFinanceiraMetadados> = {
  [InstituicaoFinanceira.CAIXA]: {
    corPrimaria: '#005CA9',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.BB]: {
    corPrimaria: '#F8D117',
    corTexto: '#003DA5',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.ITAU]: {
    corPrimaria: '#EC7000',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.BRADESCO]: {
    corPrimaria: '#CC092F',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.SANTANDER]: {
    corPrimaria: '#EC0000',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.BANRISUL]: {
    corPrimaria: '#004F9E',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.BANCO_NORDESTE]: {
    corPrimaria: '#FE0000',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.BRB]: {
    corPrimaria: '#005CAA',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.SAFRA]: {
    corPrimaria: '#D4AF37',
    corTexto: '#000',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.VOTORANTIM]: {
    corPrimaria: '#003A70',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.BTG]: {
    corPrimaria: '#0E1226',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.PAN]: {
    corPrimaria: '#0066B3',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.AGIBANK]: {
    corPrimaria: '#3446E4',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.NUBANK]: {
    corPrimaria: '#820AD1',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.INTER]: {
    corPrimaria: '#FF7A00',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.C6]: {
    corPrimaria: '#242424',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.NEON]: {
    corPrimaria: '#00FFFF',
    corTexto: '#000',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.NEXT]: {
    corPrimaria: '#00FF5F',
    corTexto: '#000',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.MERCADO_PAGO]: {
    corPrimaria: '#009EE3',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.PAGBANK]: {
    corPrimaria: '#00C853',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.PICPAY]: {
    corPrimaria: '#11C76F',
    corTexto: '#FFF',
    icone: 'account_balance',
  },
  [InstituicaoFinanceira.XP]: { corPrimaria: '#000000', corTexto: '#FFF', icone: 'trending_up' },
  [InstituicaoFinanceira.RICO]: { corPrimaria: '#FF4500', corTexto: '#FFF', icone: 'trending_up' },
  [InstituicaoFinanceira.DINHEIRO]: { corPrimaria: '#1B5E20', corTexto: '#FFF', icone: 'payments' },
  [InstituicaoFinanceira.OUTROS]: { corPrimaria: '#757575', corTexto: '#FFF', icone: 'payments' },
} as any;
