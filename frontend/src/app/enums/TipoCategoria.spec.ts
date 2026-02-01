import {
  TipoCategoria,
  TipoCategoriaNome,
  obterEstiloTipoCategoria,
  obterMetadadosTipoCategoria,
  obterOpcoesTipoCategoria,
} from './TipoCategoria';

describe('TipoCategoria', () => {
  /**
   * Testes para a função obterMetadadosTipoCategoria.
   */
  describe('obterMetadadosTipoCategoria', () => {
    it('deve retornar a cor verde e ícone de subida para RECEITA', () => {
      const resultado = obterMetadadosTipoCategoria(TipoCategoria.RECEITA);

      expect(resultado).toBeDefined();
      expect(resultado.cor).toBe('#2E7D32');
      expect(resultado.icone).toBe('arrow_circle_up');
    });

    it('deve retornar a cor vermelha e ícone de descida para DESPESA', () => {
      const resultado = obterMetadadosTipoCategoria(TipoCategoria.DESPESA);

      expect(resultado).toBeDefined();
      expect(resultado.cor).toBe('#C62828');
      expect(resultado.icone).toBe('arrow_circle_down');
    });
  });

  /**
   * Testes para a função obterEstiloTipoCategoria.
   */
  describe('obterEstiloTipoCategoria', () => {
    it('deve retornar o objeto CSS correto para AMBOS', () => {
      const estilo = obterEstiloTipoCategoria(TipoCategoria.AMBOS);

      expect(estilo).toEqual({
        'background-color': '#1565C0',
        color: '#FFF',
        border: '1px solid #1565C0',
      });
    });
  });

  /**
   * Testes para a função obterOpcoesTipoCategoria.
   */
  describe('obterOpcoesTipoCategoria', () => {
    it('deve retornar as 3 opções do Enum', () => {
      const opcoes = obterOpcoesTipoCategoria();
      expect(opcoes.length).toBe(3);
    });

    it('deve mapear corretamente o nome amigável', () => {
      const opcoes = obterOpcoesTipoCategoria();
      const opcaoReceita = opcoes.find((o) => o.valor === TipoCategoria.RECEITA);

      expect(opcaoReceita).toBeDefined();
      expect(opcaoReceita?.nome).toBe('Receita');
    });
  });

  /**
   * Teste de integridade do Objeto de Nomes.
   */
  describe('TipoCategoriaNome', () => {
    it('deve ter um nome amigável para cada item do Enum', () => {
      Object.values(TipoCategoria).forEach((chave) => {
        expect(TipoCategoriaNome[chave]).toBeTruthy();
      });
    });
  });
});
