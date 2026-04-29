package br.com.sfpacim.backend.repositories;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe base para testes de persistência.
 * Configura o Spring para usar o banco real (Postgres) em vez do H2,
 * e ativa o perfil de teste (application-test.yml).
 * 
 * @author Matheus F. N. Pereira
 */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

}
