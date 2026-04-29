package br.com.sfpacim.backend.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.models.enums.TipoTransacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link TransacaoRepository}.
 *
 * <p>
 * Valida a persistência de movimentações financeiras, paginação, filtragem por
 * datas e o rigoroso isolamento de dados entre os usuários do sistema.
 *
 * @author Matheus F. N. Pereira
 */
class TransacaoRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME_USUARIO = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL_USUARIO = "matheusfnpereira@gmail.com";
    private static final String SENHA_USUARIO = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    private static final String DESC_TRANSACAO = "Compra no Mercado";
    private static final BigDecimal VALOR = new BigDecimal("250.00");
    private static final LocalDate DATA_HOJE = LocalDate.now();

    private Usuario usuario;
    private Pessoa pessoa;
    private Conta conta;
    private Categoria categoria;
    private Transacao transacao;

    /**
     * Configura o cenário de teste persistindo as entidades fundamentais e
     * a transação para evitar violações de chave estrangeira no banco em memória.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);
        pessoa = new Pessoa("Titular", usuario);
        conta = new Conta("Conta Principal", InstituicaoFinanceira.NUBANK, BigDecimal.ZERO, pessoa);
        categoria = new Categoria("Alimentação", TipoCategoria.DESPESA, "icon", "#000", usuario);

        transacao = Transacao.builder()
                .descricao(DESC_TRANSACAO)
                .valor(VALOR)
                .dataCompetencia(DATA_HOJE)
                .dataVencimento(DATA_HOJE)
                .tipo(TipoTransacao.DESPESA)
                .status(StatusTransacao.PENDENTE)
                .usuario(usuario)
                .pessoa(pessoa)
                .conta(conta)
                .categoria(categoria)
                .build();
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#findByUsuarioIdAndId(UUID, UUID)}.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId quando existir e pertencer ao usuário, deve retornar Optional com a transação")
    void testeFindByUsuarioIdAndId_QuandoExistir_DeveRetornarOptionalComTransacao() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        Optional<Transacao> resultado = transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(DESC_TRANSACAO, resultado.get().getDescricao());
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#findByUsuarioIdAndId(UUID, UUID)}.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId quando transação for de outro usuário, deve retornar Optional vazio")
    void testeFindByUsuarioIdAndId_QuandoOutroUsuario_DeveRetornarOptionalVazio() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        Optional<Transacao> resultado = transacaoRepository.findByUsuarioIdAndId(intruso.getId(), transacao.getId());

        assertFalse(resultado.isPresent(), "O Optional deveria estar vazio protegendo a transação de outro usuário");
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#findByUsuarioIdAndDataVencimentoBetween(UUID, LocalDate, LocalDate, org.springframework.data.domain.Pageable)}.
     *
     * <p>
     * Valida se a paginação e o filtro de período (data limite) retornam a
     * transação corretamente quando os parâmetros englobam a data de vencimento.
     */
    @Test
    @DisplayName("findByUsuarioIdAndDataVencimentoBetween quando estiver no período, deve retornar página preenchida")
    void testeFindByUsuarioIdAndDataVencimentoBetween_QuandoNoPeriodo_DeveRetornarPagina() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        LocalDate inicio = DATA_HOJE.minusDays(1);
        LocalDate fim = DATA_HOJE.plusDays(1);
        PageRequest pageable = PageRequest.of(0, 10);

        Page<Transacao> resultado = transacaoRepository.findByUsuarioIdAndDataVencimentoBetween(
                usuario.getId(), inicio, fim, pageable);

        assertFalse(resultado.isEmpty(), "A página deveria conter elementos");
        assertEquals(1, resultado.getTotalElements());
        assertEquals(DESC_TRANSACAO, resultado.getContent().get(0).getDescricao());
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#existsByUsuarioIdAndPessoaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndPessoaId deve retornar verdadeiro se houver vínculo")
    void testeExistsByUsuarioIdAndPessoaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndPessoaId(usuario.getId(), pessoa.getId());

        assertTrue(existe, "Deveria retornar verdadeiro devido ao vínculo da pessoa com a transação");
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#existsByUsuarioIdAndPessoaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndPessoaId com usuário incorreto, deve retornar falso")
    void testeExistsByUsuarioIdAndPessoaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndPessoaId(intruso.getId(), pessoa.getId());

        assertFalse(existe, "Deveria retornar falso garantindo o isolamento da pessoa");
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#existsByUsuarioIdAndContaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndContaId deve retornar verdadeiro se houver vínculo")
    void testeExistsByUsuarioIdAndContaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndContaId(usuario.getId(), conta.getId());

        assertTrue(existe, "Deveria retornar verdadeiro devido ao vínculo da conta com a transação");
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#existsByUsuarioIdAndContaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndContaId com usuário incorreto, deve retornar falso")
    void testeExistsByUsuarioIdAndContaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndContaId(intruso.getId(), conta.getId());

        assertFalse(existe, "Deveria retornar falso garantindo o isolamento da conta");
    }
}