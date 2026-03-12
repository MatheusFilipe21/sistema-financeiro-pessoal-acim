package br.com.sfpacim.backend.services;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço responsável pela lógica de negócio relacionada à entidade
 * {@link Categoria}.
 *
 * <p>
 * Gerencia o ciclo de vida das categorias (pessoais e globais).
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ContextoUsuarioService contextoUsuarioService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param categoriaRepository    O repositório de categorias.
     * @param contextoUsuarioService O serviço para recuperar o usuário
     *                               autenticado.
     */
    public CategoriaService(CategoriaRepository categoriaRepository,
            ContextoUsuarioService contextoUsuarioService) {
        this.categoriaRepository = categoriaRepository;
        this.contextoUsuarioService = contextoUsuarioService;
    }

    /**
     * Cadastra uma nova categoria personalizada para o usuário.
     *
     * <p>
     * Valida unicidade global (não pode repetir nome de categoria do sistema
     * ou do próprio usuário).
     *
     * @param dto Os dados da nova categoria.
     * @return O {@link CategoriaDTO} representando a categoria criada.
     * @throws ViolacaoDadosException Se houver duplicidade de nome.
     */
    @Transactional
    public CategoriaDTO cadastrar(CriarAtualizarCategoriaDTO dto) throws ViolacaoDadosException {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        String nomeSanitizado = dto.nome().trim();

        if (categoriaRepository.existsByNomeAndUsuarioConflitoCadastro(nomeSanitizado, usuario)) {
            throw excecaoNomeDuplicado(nomeSanitizado);
        }

        Categoria categoria = new Categoria(nomeSanitizado, dto.tipo(), dto.icone(), dto.cor(), usuario);
        categoria = salvarEntidade(categoria);

        return paraDTO(categoria);
    }

    /**
     * Lista todas as categorias visíveis para o usuário.
     *
     * <p>
     * Inclui categorias pessoais e globais, ordenadas por nome.
     *
     * @return Lista de {@link CategoriaDTO}.
     */
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        List<Categoria> categorias = categoriaRepository.findByUsuarioOrUsuarioIsNullOrderByNomeAsc(usuario);

        return categorias.stream()
                .map(CategoriaDTO::new)
                .toList();
    }

    /**
     * Atualiza uma categoria existente.
     *
     * <p>
     * Categorias do sistema são imutáveis e não podem ser alteradas pelo usuário.
     *
     * @param id  O identificador da categoria.
     * @param dto Os novos dados.
     * @return O {@link CategoriaDTO} atualizado.
     * @throws RegraDeNegocioException Se tentar alterar uma categoria do sistema.
     */
    @Transactional
    public CategoriaDTO atualizar(UUID id, CriarAtualizarCategoriaDTO dto) {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();
        Categoria categoria = buscarCategoriaValidada(id, usuario);

        if (categoria.isDoSistema()) {
            throw new RegraDeNegocioException("Categorias padrão do sistema não podem ser alteradas.");
        }

        String novoNome = dto.nome().trim();

        if (categoriaRepository.existsByNomeAndUsuarioConflito(novoNome, usuario, id)) {
            throw excecaoNomeDuplicado(novoNome);
        }

        categoria.setNome(novoNome);
        categoria.setTipo(dto.tipo());
        categoria.setIcone(dto.icone());
        categoria.setCor(dto.cor());

        salvarEntidade(categoria);

        return paraDTO(categoria);
    }

    /**
     * Remove uma categoria personalizada.
     *
     * <p>
     * Categorias do sistema não podem ser excluídas.
     *
     * @param id O identificador da categoria.
     * @throws RegraDeNegocioException Se a categoria for padrão do sistema.
     */
    @Transactional
    public void excluir(UUID id) {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();
        Categoria categoria = buscarCategoriaValidada(id, usuario);

        if (categoria.isDoSistema()) {
            throw new RegraDeNegocioException("Não é possível excluir uma categoria padrão do sistema.");
        }

        categoriaRepository.delete(categoria);
    }

    /**
     * Busca uma categoria e valida se o usuário tem permissão de visualização.
     *
     * @param id      ID da categoria.
     * @param usuario Usuário autenticado.
     * @return Entidade Categoria.
     */
    private Categoria buscarCategoriaValidada(UUID id, Usuario usuario) {
        return categoriaRepository.findById(id)
                .filter(c -> c.getUsuario() == null || c.getUsuario().equals(usuario))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Categoria com id %s não encontrada ou acesso negado.", id)));
    }

    /**
     * Tenta salvar a categoria no banco de dados.
     *
     * @param categoria Entidade a ser salva.
     * @return Entidade salva.
     */
    private Categoria salvarEntidade(Categoria categoria) {
        try {
            return categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException _) {
            throw excecaoNomeDuplicado(categoria.getNome());
        }
    }

    private ViolacaoDadosException excecaoNomeDuplicado(String nome) {
        return new ViolacaoDadosException(
                String.format("Já existe uma categoria '%s' cadastrada.", nome));
    }

    private CategoriaDTO paraDTO(Categoria categoria) {
        return new CategoriaDTO(categoria);
    }
}
