import { InstituicaoFinanceira } from '../enums/InstituicaoFinanceira';
import { InstituicaoEstilo } from './instituicao-estilo';

/**
 * Suite de testes para o Pipe InstituicaoEstilo.
 * Verifica a geração do objeto de estilos CSS (background, cor, borda).
 *
 * @author Matheus F. N. Pereira
 */
describe('InstituicaoEstilo Pipe', () => {
  let pipe: InstituicaoEstilo;

  /**
   * Configuração inicial executada antes de cada teste.
   */
  beforeEach(() => {
    pipe = new InstituicaoEstilo();
  });

  /**
   * Verifica se a instância do pipe é criada com sucesso.
   */
  it('deve criar uma instância do pipe', () => {
    expect(pipe).toBeTruthy();
  });

  /**
   * Verifica se o objeto retornado contém as propriedades CSS esperadas com os valores configurados.
   */
  it('deve retornar o objeto de estilo contendo as cores configuradas para o MERCADO_PAGO', () => {
    const estilo = pipe.transform(InstituicaoFinanceira.MERCADO_PAGO);

    expect(estilo).toBeDefined();
    expect(estilo['background-color']).toBe('#009EE3');
    expect(estilo['color']).toBe('#FFF');
    expect(estilo['border']).toBe('1px solid #009EE3');
  });

  /**
   * Verifica se instituições diferentes retornam configurações visuais distintas.
   */
  it('deve garantir que instituições diferentes retornem cores de fundo diferentes', () => {
    const estiloMercadoPago = pipe.transform(InstituicaoFinanceira.MERCADO_PAGO);
    const estiloBB = pipe.transform(InstituicaoFinanceira.BB);

    expect(estiloMercadoPago['background-color']).not.toBe(estiloBB['background-color']);
  });
});
