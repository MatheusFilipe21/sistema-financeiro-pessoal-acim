package br.com.sfpacim.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.FiltroCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.SelecaoCategoriaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.CategoriaRepository;
import br.com.sfpacim.backend.repositories.specifications.CategoriaSpec;
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

    private final MessageSource messageSource;
    private final CategoriaRepository categoriaRepository;
    private final ContextoUsuarioService contextoUsuarioService;
    private final TransacaoService transacaoService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param messageSource          A instância do MessageSource.
     * @param categoriaRepository    O repositório de categorias.
     * @param contextoUsuarioService O serviço para recuperar o usuário autenticado.
     * @param transacaoService       O serviço para acesso as transações.
     */
    public CategoriaService(MessageSource messageSource, CategoriaRepository categoriaRepository,
            ContextoUsuarioService contextoUsuarioService, @Lazy TransacaoService transacaoService) {
        this.messageSource = messageSource;
        this.categoriaRepository = categoriaRepository;
        this.contextoUsuarioService = contextoUsuarioService;
        this.transacaoService = transacaoService;
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
     * Lista as categorias visíveis para o usuário de forma paginada e filtrada.
     *
     * <p>
     * A busca delega a ordenação, filtros e paginação ao SGBD. Inclui
     * automaticamente tanto as categorias pessoais do usuário quanto as
     * categorias globais do sistema.
     *
     * @param filtro   Objeto {@link FiltroCategoriaDTO} contendo os parâmetros de
     *                 busca.
     * @param pageable Configurações de paginação e ordenação injetadas pelo Spring.
     * @return Uma {@link Page} de {@link CategoriaDTO}.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaDTO> listar(FiltroCategoriaDTO filtro, Pageable pageable) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        Specification<Categoria> spec = CategoriaSpec.comFiltros(usuarioId, filtro);

        return categoriaRepository.findAll(spec, pageable).map(this::paraDTO);
    }

    /**
     * Lista as opções de categorias visíveis para o usuário em componentes de
     * seleção.
     *
     * <p>
     * Retorna uma lista leve contendo dados de identificação e apresentação visual
     * (ícone/cor). Traz todas as categorias (Receita, Despesa e Ambos) para
     * otimizar o payload.
     *
     * @return Uma lista não paginada de {@link SelecaoCategoriaDTO}.
     */
    @Transactional(readOnly = true)
    public List<SelecaoCategoriaDTO> listarOpcoes() {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        return categoriaRepository.buscarOpcoesParaSelecao(usuarioId);
    }

    /**
     * Busca os detalhes de uma categoria pelo seu identificador.
     *
     * @param categoriaId O identificador da categoria.
     * @return O {@link CategoriaDTO} contendo os dados da categoria.
     * @throws EntityNotFoundException Se a categoria não for encontrada ou não for
     *                                 visível ao usuário.
     */
    @Transactional(readOnly = true)
    public CategoriaDTO buscarPorId(UUID categoriaId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        return paraDTO(obterEntidadeValidada(usuarioId, categoriaId));
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
            throw new RegraDeNegocioException(
                    MetodosUteis.obterMensagem(messageSource, "erro.categoria.sistema.alteracao"));
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

        validarDependenciasParaExclusao(categoria);

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
        return categoriaRepository.buscarPorIdEUsuarioOuSistema(usuarioId, categoriaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MetodosUteis.obterMensagem(messageSource, "erro.recurso.nao-encontrado",
                                MetodosUteis.obterMensagem(messageSource, "categoria.nome.singular"), categoriaId)));
    }

    /**
     * Valida se já existe uma categoria com o mesmo nome (sistema ou do usuário).
     *
     * <p>
     * A validação é delegada ao banco de dados utilizando a função {@code unaccent}
     * do PostgreSQL, garantindo integridade semântica e prevenindo colisões entre
     * categorias pessoais e globais. O tipo (Receita/Despesa/Ambos) é ignorado na
     * validação para forçar o uso da categoria genérica caso os nomes coincidam.
     *
     * @param categoria A entidade a ser validada.
     * @throws ViolacaoDadosException Se duplicidade for detectada.
     */
    private void validarUnicidadeNome(Categoria categoria) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        boolean existeDuplicado = categoriaRepository.existeCategoriaDuplicada(usuarioId, categoria.getNome(),
                categoria.getId());

        MetodosUteis.validarUnicidade(messageSource, existeDuplicado, Categoria.class, "erro.unicidade.padrao",
                categoria.getNome());
    }

    /**
     * Valida se a categoria possui dependências ativas ou restrições de sistema
     * que impeçam sua exclusão.
     *
     * <p>
     * O método aplica duas travas de segurança:
     * 1. Impede a remoção de categorias globais (padrão do sistema).
     * 2. Consulta o serviço de transações para verificar se existem registros
     * vinculados a esta categoria personalizada.
     * Caso existam vínculos, delega a formatação da mensagem e o lançamento
     * da exceção de violação para a classe utilitária.
     *
     * @param categoria A entidade {@link Categoria} avaliada para exclusão.
     * @throws RegraDeNegocioException Caso a categoria seja do sistema.
     * @throws ViolacaoDadosException  Caso existam transações vinculadas à
     *                                 categoria.
     */
    private void validarDependenciasParaExclusao(Categoria categoria) throws ViolacaoDadosException {
        List<String> dependencias = new ArrayList<>();

        if (categoria.isDoSistema()) {
            throw new RegraDeNegocioException(
                    MetodosUteis.obterMensagem(messageSource, "erro.categoria.sistema.exclusao"));
        }

        if (transacaoService.existeTransacaoVinculadaACategoria(categoria.getId())) {
            dependencias.add(MetodosUteis.obterMensagem(messageSource, "transacao.nome.plural"));
        }

        MetodosUteis.validarDependenciasExclusao(messageSource, categoria.getNome(), dependencias);
    }

    /**
     * Tenta salvar a categoria no banco de dados.
     *
     * <p>
     * Força a sincronização com o banco (flush) para capturar imediatamente
     * a {@link DataIntegrityViolationException} como uma camada extra de segurança
     * para a constraint de unicidade.
     *
     * @param categoria Entidade a ser salva.
     * @return Entidade salva.
     */
    private Categoria salvarEntidade(Categoria categoria) {
        try {
            return categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException _) {
            throw MetodosUteis.gerarExcecaoUnicidade(messageSource, Categoria.class, "erro.unicidade.padrao",
                    categoria.getNome());
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
