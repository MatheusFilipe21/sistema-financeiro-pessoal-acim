package br.com.sfpacim.backend.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;

/**
 * Teste de Integração para o {@link ContaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando um banco de dados em memória (H2) configurado pelo @DataJpaTest.
 *
 * @author Matheus F. N. Pereira
 */
@DataJpaTest
class ContaRepositoryTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME_USUARIO = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL_USUARIO = "matheusfnpereira@gmail.com";
    private static final String SENHA_USUARIO = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";
    private static final String NOME_PESSOA = "Matheus Filipe do Nascimento Pereira";
    private static final String NOME_CONTA_1 = "Investimentos Mercado Pago";
    private static final String NOME_CONTA_2 = "Cofrinhos Mercado Pago";
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("100.00");

    private Usuario usuario;
    private Pessoa pessoa;
    private Conta conta1;
    private Conta conta2;

    /**
     * Configura o cenário inicial antes de cada teste.
     * Instancia o usuário e as pessoas.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);

        pessoa = new Pessoa(NOME_PESSOA, usuario);
        pessoa.setTitular(true);

        conta1 = new Conta(NOME_CONTA_1, InstituicaoFinanceira.NUBANK, SALDO_INICIAL, pessoa);
        conta2 = new Conta(NOME_CONTA_2, InstituicaoFinanceira.ITAU, SALDO_INICIAL, pessoa);
    }

    /**
     * Testa o método {@link ContaRepository#findByPessoa(Pessoa)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde as contas vinculadas a uma pessoa
     * específica são retornadas corretamente.
     */
    @Test
    @DisplayName("findByPessoa quando existirem registros, deve retornar lista com as contas")
    void testeFindByPessoa_QuandoExistiremRegistros_DeveRetornarLista() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.persist(conta2);
        entityManager.flush();

        List<Conta> resultado = contaRepository.findByPessoa(pessoa);

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(2, resultado.size(), "Deveria retornar exatos 2 registros");

        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(NOME_CONTA_1)));
        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(NOME_CONTA_2)));
    }

    /**
     * Testa o isolamento de dados.
     *
     * <p>
     * Valida se a busca NÃO retorna registros que pertencem a outra pessoa.
     */
    @Test
    @DisplayName("findByPessoa não deve retornar registros de outra pessoa")
    void testeFindByPessoa_QuandoPessoaForDiferente_NaoDeveRetornarRegistros() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Pessoa pessoaIntrusa = new Pessoa("Outra Pessoa", usuario);
        entityManager.persist(pessoaIntrusa);
        entityManager.flush();

        List<Conta> resultado = contaRepository.findByPessoa(pessoaIntrusa);

        assertTrue(resultado.isEmpty(), "A lista deveria estar vazia para a pessoa sem contas");
    }
}
