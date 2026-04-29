package br.com.sfpacim.backend.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link ContaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando um banco de dados em memória configurado pelo @DataJpaTest.
 * Valida estritamente a aplicação do conceito de Defense in Depth.
 *
 * @author Matheus F. N. Pereira
 */
class ContaRepositoryTest extends BaseRepositoryTest {

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
     * Instancia a hierarquia de usuário, pessoa e contas.
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
     * Testa o método
     * {@link ContaRepository#findByPessoaUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde a conta é encontrada e pertence
     * ao usuário informado.
     */
    @Test
    @DisplayName("findByPessoaUsuarioIdAndId quando conta existir e pertencer ao usuário, deve retornar Optional com a conta")
    void testeFindByPessoaUsuarioIdAndId_QuandoExistir_DeveRetornarOptionalComConta() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        Optional<Conta> resultado = contaRepository.findByPessoaUsuarioIdAndId(usuario.getId(), conta1.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(NOME_CONTA_1, resultado.get().getNome(), "Deveria retornar a conta correta");
    }

    /**
     * Testa o isolamento no método
     * {@link ContaRepository#findByPessoaUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida se a busca bloqueia o acesso a uma conta existente quando
     * consultada por um usuário diferente do titular.
     */
    @Test
    @DisplayName("findByPessoaUsuarioIdAndId quando conta for de outro usuário, deve retornar Optional vazio")
    void testeFindByPessoaUsuarioIdAndId_QuandoOutroUsuario_DeveRetornarOptionalVazio() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(usuarioIntruso);
        entityManager.flush();

        Optional<Conta> resultado = contaRepository.findByPessoaUsuarioIdAndId(usuarioIntruso.getId(), conta1.getId());

        assertFalse(resultado.isPresent(), "O Optional deveria estar vazio pois a conta pertence a outro usuário");
    }

    /**
     * Testa o método {@link ContaRepository#findByPessoaUsuarioId(UUID)}.
     *
     * <p>
     * Valida o cenário de listagem geral de contas de um tenant (usuário).
     */
    @Test
    @DisplayName("findByPessoaUsuarioId quando existirem contas, deve retornar a lista completa do usuário")
    void testeFindByPessoaUsuarioId_QuandoExistiremContas_DeveRetornarLista() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.persist(conta2);
        entityManager.flush();

        List<Conta> resultado = contaRepository.findByPessoaUsuarioId(usuario.getId());

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(2, resultado.size(), "Deveria retornar exatos 2 registros");

        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(NOME_CONTA_1)));
    }

    /**
     * Testa o método
     * {@link ContaRepository#findByPessoaUsuarioIdAndPessoaId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de filtro específico, trazendo as contas de apenas uma
     * pessoa
     * sob a titularidade do usuário autenticado.
     */
    @Test
    @DisplayName("findByPessoaUsuarioIdAndPessoaId deve retornar apenas as contas da pessoa especificada")
    void testeFindByPessoaUsuarioIdAndPessoaId_DeveFiltrarCorretamente() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Pessoa pessoa2 = new Pessoa("Outra Pessoa", usuario);
        Conta contaOutraPessoa = new Conta("Conta Isolada", InstituicaoFinanceira.OUTROS, BigDecimal.ZERO, pessoa2);

        entityManager.persist(pessoa2);
        entityManager.persist(contaOutraPessoa);
        entityManager.flush();

        List<Conta> resultado = contaRepository.findByPessoaUsuarioIdAndPessoaId(usuario.getId(), pessoa.getId());

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(1, resultado.size(), "Deveria trazer apenas a conta vinculada à primeira pessoa");
        assertEquals(NOME_CONTA_1, resultado.get(0).getNome());
    }

    /**
     * Testa o método
     * {@link ContaRepository#existsByPessoaUsuarioIdAndPessoaId(UUID, UUID)}.
     *
     * <p>
     * Utilizado para validar dependências ativas em operações de exclusão.
     */
    @Test
    @DisplayName("existsByPessoaUsuarioIdAndPessoaId deve retornar verdadeiro se houver vínculo")
    void testeExistsByPessoaUsuarioIdAndPessoaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        boolean existe = contaRepository.existsByPessoaUsuarioIdAndPessoaId(usuario.getId(), pessoa.getId());

        assertTrue(existe, "Deveria retornar verdadeiro pois existe conta para a pessoa do usuário");
    }

    /**
     * Testa o isolamento no método
     * {@link ContaRepository#existsByPessoaUsuarioIdAndPessoaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByPessoaUsuarioIdAndPessoaId deve retornar falso para usuário incorreto")
    void testeExistsByPessoaUsuarioIdAndPessoaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Usuario outroUsuario = new Usuario("Outro", "outro@email.com", "123");
        entityManager.persist(outroUsuario);
        entityManager.flush();

        boolean existe = contaRepository.existsByPessoaUsuarioIdAndPessoaId(outroUsuario.getId(), pessoa.getId());

        assertFalse(existe, "Deveria retornar falso, simulando proteção contra acesso indevido");
    }
}
