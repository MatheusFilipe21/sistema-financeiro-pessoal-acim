import { InstituicaoFinanceira } from '../enums/InstituicaoFinanceira';
import { InstituicaoIcone } from './instituicao-icone';

/**
 * Suite de testes para o Pipe InstituicaoIcone.
 * Verifica a recuperação do ícone Material Design correto para cada instituição.
 *
 * @author Matheus F. N. Pereira
 */
describe('InstituicaoIcone Pipe', () => {
  let pipe: InstituicaoIcone;

  /**
   * Configuração inicial executada antes de cada teste.
   * Instancia o pipe para garantir isolamento.
   */
  beforeEach(() => {
    pipe = new InstituicaoIcone();
  });

  /**
   * Verifica se a instância do pipe é criada com sucesso.
   */
  it('deve criar uma instância do pipe', () => {
    expect(pipe).toBeTruthy();
  });

  /**
   * Verifica se retorna o ícone de banco tradicional para a Caixa Econômica.
   */
  it('deve retornar o ícone "account_balance" para bancos tradicionais como CAIXA', () => {
    const resultado = pipe.transform(InstituicaoFinanceira.CAIXA);
    expect(resultado).toBe('account_balance');
  });

  /**
   * Verifica se retorna o ícone de banco para fintechs como o Mercado Pago.
   */
  it('deve retornar o ícone "account_balance" para fintechs como MERCADO_PAGO', () => {
    const resultado = pipe.transform(InstituicaoFinanceira.MERCADO_PAGO);
    expect(resultado).toBe('account_balance');
  });

  /**
   * Verifica se retorna o ícone de pagamentos para dinheiro em espécie.
   */
  it('deve retornar o ícone "payments" para DINHEIRO', () => {
    const resultado = pipe.transform(InstituicaoFinanceira.DINHEIRO);
    expect(resultado).toBe('payments');
  });
});
