import { describe, beforeEach, it, expect } from 'vitest';
import { InstituicaoFinanceira } from '../enums/InstituicaoFinanceira';
import { InstituicaoNome } from './instituicao-nome';

/**
 * Suite de testes para o Pipe InstituicaoNome.
 * Verifica a transformação de Enums em strings legíveis.
 *
 * @author Matheus F. N. Pereira
 */
describe('InstituicaoNome Pipe', () => {
  let pipe: InstituicaoNome;

  /**
   * Configuração inicial executada antes de cada teste.
   * Cria uma nova instância isolada do Pipe.
   */
  beforeEach(() => {
    pipe = new InstituicaoNome();
  });

  /**
   * Verifica se a instância do pipe é criada com sucesso.
   */
  it('deve criar uma instância do pipe', () => {
    expect(pipe).toBeTruthy();
  });

  /**
   * Verifica a conversão correta de um Enum mapeado (Mercado Pago) para seu nome de exibição.
   */
  it('deve transformar o enum InstituicaoFinanceira.MERCADO_PAGO no texto "Mercado Pago"', () => {
    const resultado = pipe.transform(InstituicaoFinanceira.MERCADO_PAGO);
    expect(resultado).toBe('Mercado Pago');
  });

  /**
   * Verifica a conversão correta de um Enum mapeado (Banco do Brasil) para seu nome de exibição.
   */
  it('deve transformar o enum InstituicaoFinanceira.BB no texto "Banco do Brasil"', () => {
    const resultado = pipe.transform(InstituicaoFinanceira.BB);
    expect(resultado).toBe('Banco do Brasil');
  });

  /**
   * Verifica o comportamento de fallback quando o valor fornecido não existe no mapa.
   */
  it('deve retornar "Desconhecido" caso o valor fornecido não esteja mapeado', () => {
    const valorInvalido = 'BANCO_NAO_EXISTE' as InstituicaoFinanceira;
    const resultado = pipe.transform(valorInvalido);
    expect(resultado).toBe('Desconhecido');
  });
});
