import {
  InstituicaoFinanceira,
  InstituicaoFinanceiraNome,
  obterEstiloInstituicao,
  obterMetadadosInstituicao,
  obterOpcoesInstituicoes,
} from './InstituicaoFinanceira';

describe('InstituicaoFinanceira', () => {
  /**
   * Testes para a função obterMetadadosInstituicao.
   */
  describe('obterMetadadosInstituicao', () => {
    it('deve retornar os metadados corretos para uma instituição conhecida (Ex: Mercado Pago)', () => {
      const resultado = obterMetadadosInstituicao(InstituicaoFinanceira.MERCADO_PAGO);

      expect(resultado).toBeDefined();
      expect(resultado.corPrimaria).toBe('#009EE3');
      expect(resultado.corTexto).toBe('#FFF');
      expect(resultado.icone).toBe('account_balance');
    });

    it('deve retornar os metadados corretos para a opção OUTROS', () => {
      const resultado = obterMetadadosInstituicao(InstituicaoFinanceira.OUTROS);

      expect(resultado).toBeDefined();
      expect(resultado.corPrimaria).toBe('#757575');
      expect(resultado.icone).toBe('payments');
    });

    it('deve retornar o fallback (OUTROS) caso a instituição não esteja mapeada explicitamente (defensivo)', () => {
      const instituicaoInexistente = 'BANCO_FANTASMA' as InstituicaoFinanceira;
      const resultado = obterMetadadosInstituicao(instituicaoInexistente);

      expect(resultado.corPrimaria).toBe('#757575');
    });
  });

  /**
   * Testes para a função obterEstiloInstituicao.
   */
  describe('obterEstiloInstituicao', () => {
    it('deve retornar o objeto CSS com as cores corretas', () => {
      const estilo = obterEstiloInstituicao(InstituicaoFinanceira.BB);

      expect(estilo).toEqual({
        'background-color': '#F8D117',
        color: '#003DA5',
        border: '1px solid #F8D117',
      });
    });

    it('deve retornar o estilo para dinheiro em espécie', () => {
      const estilo = obterEstiloInstituicao(InstituicaoFinanceira.DINHEIRO);

      expect(estilo['background-color']).toBe('#1B5E20');
      expect(estilo.color).toBe('#FFF');
    });
  });

  /**
   * Testes para a função obterOpcoesInstituicoes (Usada nos Selects).
   */
  describe('obterOpcoesInstituicoes', () => {
    it('deve retornar uma lista contendo todas as chaves do Enum', () => {
      const opcoes = obterOpcoesInstituicoes();
      const totalChavesEnum = Object.keys(InstituicaoFinanceira).length;

      expect(opcoes.length).toBe(totalChavesEnum);
    });

    it('deve mapear corretamente as propriedades "valor" e "nome"', () => {
      const opcoes = obterOpcoesInstituicoes();
      const opcaoCaixa = opcoes.find((o) => o.valor === InstituicaoFinanceira.CAIXA);

      expect(opcaoCaixa).toBeDefined();
      expect(opcaoCaixa?.nome).toBe('Caixa Econômica Federal');
    });

    it('deve retornar a lista ordenada alfabeticamente pelo NOME', () => {
      const opcoes = obterOpcoesInstituicoes();

      const indiceAgibank = opcoes.findIndex((o) => o.valor === InstituicaoFinanceira.AGIBANK);
      const indiceBB = opcoes.findIndex((o) => o.valor === InstituicaoFinanceira.BB);
      const indiceMercadoPago = opcoes.findIndex(
        (o) => o.valor === InstituicaoFinanceira.MERCADO_PAGO
      );

      expect(indiceAgibank).toBeLessThan(indiceBB);
      expect(indiceBB).toBeLessThan(indiceMercadoPago);
    });

    it('deve garantir que nomes com acentos sejam ordenados corretamente (localeCompare)', () => {
      const opcoes = obterOpcoesInstituicoes();

      const nomesExtraidos = opcoes.map((o) => o.nome);
      const nomesOrdenadosManualmente = [...nomesExtraidos].sort((a, b) => a.localeCompare(b));

      expect(nomesExtraidos).toEqual(nomesOrdenadosManualmente);
    });
  });

  /**
   * Teste de integridade do Objeto de Nomes.
   */
  describe('InstituicaoFinanceiraNome', () => {
    it('deve ter um nome amigável para cada item do Enum', () => {
      Object.values(InstituicaoFinanceira).forEach((chave) => {
        expect(InstituicaoFinanceiraNome[chave]).toBeTruthy();
        expect(typeof InstituicaoFinanceiraNome[chave]).toBe('string');
      });
    });
  });
});
