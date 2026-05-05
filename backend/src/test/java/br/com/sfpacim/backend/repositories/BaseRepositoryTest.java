package br.com.sfpacim.backend.repositories;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe base para testes de persistência.
 * 
 * <p>
 * Ativa o perfil de teste e impede a autoconfiguração de bancos
 * em memória, garantindo que os testes sejam executados diretamente
 * no banco de dados real configurado para testes.
 * 
 * @author Matheus F. N. Pereira
 */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

}
