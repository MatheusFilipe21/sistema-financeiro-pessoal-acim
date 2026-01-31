package br.com.sfpacim.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;

/**
 * Testes unitários para a classe {@link UserDetailsServiceImpl}.
 * <p>
 * Utiliza Mockito para isolar o serviço da dependência externa
 * (o UsuarioRepository).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String EMAIL_INEXISTENTE = "naoexiste@email.com";

    /**
     * Testa o método {@link UserDetailsServiceImpl#loadUserByUsername(String)}.
     * Valida o cenário de sucesso.
     * 
     * <p>
     * Verifica se o serviço retorna o UserDetails (Usuario) correto
     * quando o repositório o encontra.
     */
    @Test
    @DisplayName("loadUserByUsername: Quando e-mail existir, deve retornar UserDetails")
    void testeLoadUserByUsername_QuandoEmailExistir_DeveRetornarUserDetails() {
        Usuario usuarioMock = new Usuario(UUID.randomUUID(), "Matheus Filipe do Nascimento Pereira", EMAIL,
                "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMock));

        UserDetails userDetails = userDetailsService.loadUserByUsername(EMAIL);

        assertNotNull(userDetails, "UserDetails não deve ser nulo");
        assertEquals(EMAIL, userDetails.getUsername(), "O username (e-mail) deve ser o esperado");
        verify(usuarioRepository, times(1)).findByEmail(EMAIL);
    }

    /**
     * Testa o método {@link UserDetailsServiceImpl#loadUserByUsername(String)}.
     * Valida o cenário de falha (usuário não encontrado - RF13).
     * 
     * <p>
     * Verifica se o serviço lança {@link UsernameNotFoundException}
     * quando o repositório retorna um Optional vazio.
     */
    @Test
    @DisplayName("loadUserByUsername: Quando e-mail não existir, deve lançar UsernameNotFoundException")
    void testeLoadUserByUsername_QuandoEmailNaoExistir_DeveLancarExcecao() {
        when(usuarioRepository.findByEmail(EMAIL_INEXISTENTE)).thenReturn(Optional.empty());

        UsernameNotFoundException excecao = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(EMAIL_INEXISTENTE);
        });

        assertTrue(excecao.getMessage().contains("Usuário não encontrado com o e-mail: " + EMAIL_INEXISTENTE));
    }
}
