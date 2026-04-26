package br.com.sfpacim.backend.services;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

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
import br.com.sfpacim.backend.utils.MetodosUteis;

/**
 * Serviço responsável pela lógica de negócio relacionada à entidade
 * {@link Categoria}.
 *
 * <p>
 * Gerencia o ciclo de vida das categorias (pessoais e globais), aplicando
 * o conceito de Defense in Depth para garantir isolamento de dados.
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
     * @param contextoUsuarioService O serviço para recuperar o usuário autenticado.
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

        Categoria categoria = new Categoria(dto.nome().trim(), dto.tipo(), dto.icone(), dto.cor(), usuario);

        validarUnicidadeNome(categoria);

        return paraDTO(salvarEntidade(categoria));
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
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        List<Categoria> categorias = categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuarioId);

        return categorias.stream()
                .map(this::paraDTO)
                .toList();
    }

    /**
     * Atualiza uma categoria existente.
     *
     * <p>
     * Categorias do sistema são imutáveis e não podem ser alteradas pelo usuário.
     *
     * @param categoriaId O identificador da categoria.
     * @param dto         Os novos dados.
     * @return O {@link CategoriaDTO} atualizado.
     * @throws RegraDeNegocioException Se tentar alterar uma categoria do sistema.
     * @throws ViolacaoDadosException  Se o novo nome gerar duplicidade.
     */
    @Transactional
    public CategoriaDTO atualizar(UUID categoriaId, CriarAtualizarCategoriaDTO dto) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Categoria categoria = obterEntidadeValidada(usuarioId, categoriaId);

        if (categoria.isDoSistema()) {
            throw new RegraDeNegocioException("Categorias padrão do sistema não podem ser alteradas.");
        }

        categoria.setNome(dto.nome().trim());
        categoria.setTipo(dto.tipo());
        categoria.setIcone(dto.icone());
        categoria.setCor(dto.cor());

        validarUnicidadeNome(categoria);

        return paraDTO(salvarEntidade(categoria));
    }

    /**
     * Remove uma categoria personalizada.
     *
     * <p>
     * Categorias do sistema não podem ser excluídas.
     *
     * @param categoriaId O identificador da categoria.
     * @throws RegraDeNegocioException Se a categoria for padrão do sistema.
     */
    @Transactional
    public void excluir(UUID categoriaId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Categoria categoria = obterEntidadeValidada(usuarioId, categoriaId);

        if (categoria.isDoSistema()) {
            throw new RegraDeNegocioException("Não é possível excluir uma categoria padrão do sistema.");
        }

        categoriaRepository.delete(categoria);
    }

    /**
     * Busca uma categoria e valida se o usuário tem permissão de visualização
     * direto no banco de dados.
     *
     * @param usuarioId   ID do usuário autenticado.
     * @param categoriaId ID da categoria.
     * @return Entidade Categoria.
     * @throws EntityNotFoundException Se a categoria não existir ou não for
     *                                 visível.
     */
    Categoria obterEntidadeValidada(UUID usuarioId, UUID categoriaId) {
        return categoriaRepository.findByUsuarioIdOrSistemaAndId(usuarioId, categoriaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Categoria com id %s não encontrada ou acesso negado.", categoriaId)));
    }

    /**
     * Valida se já existe uma categoria com o mesmo nome (sistema ou do usuário).
     *
     * <p>
     * A validação é realizada em memória buscando as categorias visíveis ao
     * usuário,
     * garantindo portabilidade (H2, Postgres) e ignorando case/acentos.
     *
     * @param categoria A entidade a ser validada.
     * @throws ViolacaoDadosException Se duplicidade for detectada.
     */
    private void validarUnicidadeNome(Categoria categoria) {
        UUID usuarioId = categoria.getUsuario().getId();
        String nomeNovoNormalizado = MetodosUteis.normalizarParaBusca(categoria.getNome());

        boolean existeDuplicado = categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuarioId)
                .stream()
                .filter(c -> categoria.getId() == null || !c.getId().equals(categoria.getId()))
                .anyMatch(c -> MetodosUteis.normalizarParaBusca(c.getNome()).equals(nomeNovoNormalizado));

        MetodosUteis.validarUnicidade(existeDuplicado, Categoria.class.getSimpleName(), categoria.getNome());
    }

    /**
     * Tenta salvar a categoria no banco de dados.
     *
     * <p>
     * Captura {@link DataIntegrityViolationException} como uma camada extra
     * de segurança para a constraint de unicidade.
     *
     * @param categoria Entidade a ser salva.
     * @return Entidade salva.
     */
    private Categoria salvarEntidade(Categoria categoria) {
        try {
            return categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException _) {
            MetodosUteis.validarUnicidade(true, Categoria.class.getSimpleName(), categoria.getNome());
            return null;
        }
    }

    /**
     * Converte a entidade {@link Categoria} para o DTO de resposta.
     *
     * @param categoria A entidade carregada do banco.
     * @return O {@link CategoriaDTO} correspondente.
     */
    private CategoriaDTO paraDTO(Categoria categoria) {
        return new CategoriaDTO(categoria);
    }
}
