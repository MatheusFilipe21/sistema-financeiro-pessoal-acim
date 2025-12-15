package br.com.sfpacim.backend.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para a entidade {@link Pessoa}.
 * 
 * @author Matheus F. N. Pereira
 */
class PessoaTest {

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final Usuario USUARIO = new Usuario("Matheus Filipe do Nascimento Pereira",
            "matheusfnpereira@gmail.com", "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");

    private Pessoa pessoa;

    /**
     * Configura um objeto {@link Pessoa} padrão antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        USUARIO.setId(UUID.randomUUID());
        pessoa = new Pessoa(NOME, USUARIO);
    }

    /**
     * Testa o construtor customizado da classe
     * {@link Pessoa#Pessoa(String, Usuario)}.
     *
     * <p>
     * Verifica se os atributos são inicializados corretamente.
     */
    @Test
    @DisplayName("Construtor customizado deve inicializar atributos corretamente")
    void testConstrutorCustomizado_QuandoChamado_DeveInicializarAtributos() {
        assertNull(pessoa.getId(), "O id da Pessoa deveria ser nulo antes da persistência");

        assertEquals(NOME, pessoa.getNome(), "O nome da Pessoa deveria ser o esperado");
        assertEquals(USUARIO, pessoa.getUsuario(), "O usuário deveria ser o esperado");
    }
}
