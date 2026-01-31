package br.com.sfpacim.backend.bdd.contexts;

import org.springframework.beans.factory.annotation.Autowired;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import br.com.sfpacim.backend.repositories.UsuarioRepository;
import io.cucumber.java.After;

/**
 * Contexto auxiliar para manipulação direta do Banco de Dados nos testes.
 * 
 * <p>
 * Responsável por criar pré-condições (Massa de dados) e realizar a limpeza
 * do ambiente após a execução dos cenários.
 *
 * @author Matheus F. N. Pereira
 */
public class BancoDadosContext {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    /**
     * Remove todos os registros de usuários do banco de dados.
     * <p>
     * Este Hook é executado automaticamente ao final de qualquer cenário
     * que possua a tag <b>@limparUsuarios</b>.
     */
    @After("@limparUsuarios")
    public void limparTodosOsUsuarios() {
        pessoaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    /**
     * Cria um usuário com Nome e Email explícitos, usando a senha padrão.
     * 
     * @param nome  Nome completo do usuário.
     * @param email Email do usuário.
     */
    public void criarUsuarioComSenhaPadrao(String nome, String email) {
        final Usuario usuario = new Usuario(
                nome,
                email,
                "$2a$10$dBAKrIKGqf2KmyRnMh3C4.WiJ9vl..jUNTF2GIoVms32PZIjNPNY2" // Ab123456
        );

        usuarioRepository.save(usuario);
    }
}
