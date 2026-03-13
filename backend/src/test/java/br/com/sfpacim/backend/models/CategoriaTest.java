package br.com.sfpacim.backend.models;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sfpacim.backend.models.enums.TipoCategoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para a entidade {@link Categoria}.
 * 
 * @author Matheus F. N. Pereira
 */
class CategoriaTest {

    private static final String NOME_CATEGORIA = "Alimentação";
    private static final TipoCategoria TIPO = TipoCategoria.DESPESA;
    private static final String ICONE = "restaurant";
    private static final String COR = "#FF0000";
    private static final Usuario USUARIO = new Usuario("Matheus Filipe do Nascimento Pereira",
            "matheusfnpereira@gmail.com", "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");

    private Categoria categoria;

    /**
     * Configura um objeto {@link Categoria} padrão antes de cada teste.
     *
     * <p>
     * Garante que as dependências (Usuario) tenham IDs para simular
     * objetos persistidos, embora não seja estritamente necessário para teste
     * de unidade pura, ajuda na consistência.
     */
    @BeforeEach
    void setUp() {
        USUARIO.setId(UUID.randomUUID());

        categoria = new Categoria(NOME_CATEGORIA, TIPO, ICONE, COR, USUARIO);
    }

    /**
     * Testa o construtor customizado da classe
     * {@link Categoria#Categoria(String, TipoCategoria, String, String, Usuario)}.
     *
     * <p>
     * Verifica se os atributos são inicializados corretamente.
     */
    @Test
    @DisplayName("Construtor customizado deve inicializar atributos corretamente")
    void testConstrutorCustomizado_QuandoChamado_DeveInicializarAtributos() {
        assertNull(categoria.getId(), "O id da Categoria deveria ser nulo antes da persistência");
        assertEquals(NOME_CATEGORIA, categoria.getNome(), "O nome da Categoria deveria ser o esperado");
        assertEquals(TIPO, categoria.getTipo(), "O tipo deveria ser o esperado");
        assertEquals(ICONE, categoria.getIcone(), "O ícone deveria ser o esperado");
        assertEquals(COR, categoria.getCor(), "A cor deveria ser a esperada");
        assertEquals(USUARIO, categoria.getUsuario(), "O usuário deveria ser o esperado");
    }

    /**
     * Testa o método {@link Categoria#isDoSistema()}.
     *
     * <p>
     * Valida o cenário onde a categoria possui um usuário vinculado,
     * portanto não deve ser considerada do sistema.
     */
    @Test
    @DisplayName("isDoSistema quando possuir usuário, deve retornar falso")
    void testIsDoSistema_QuandoPossuirUsuario_DeveRetornarFalso() {
        assertFalse(categoria.isDoSistema(), "Deveria retornar falso, pois a categoria pertence a um usuário");
    }

    /**
     * Testa o método {@link Categoria#isDoSistema()}.
     *
     * <p>
     * Valida o cenário onde a categoria não possui usuário (null),
     * caracterizando uma categoria global do sistema.
     */
    @Test
    @DisplayName("isDoSistema quando não possuir usuário, deve retornar verdadeiro")
    void testIsDoSistema_QuandoUsuarioForNulo_DeveRetornarVerdadeiro() {
        Categoria categoriaSistema = new Categoria(NOME_CATEGORIA, TIPO, ICONE, COR, null);

        assertTrue(categoriaSistema.isDoSistema(), "Deveria retornar verdadeiro, pois é uma categoria global");
    }
}
