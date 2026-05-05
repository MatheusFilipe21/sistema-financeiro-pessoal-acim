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

import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.dtos.conta.FiltroContaDTO;
import br.com.sfpacim.backend.dtos.conta.ListagemContaDTO;
import br.com.sfpacim.backend.dtos.conta.SelecaoContaDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.repositories.ContaRepository;
import br.com.sfpacim.backend.repositories.specifications.ContaSpec;
import br.com.sfpacim.backend.utils.MetodosUteis;

/**
 * Serviço responsável pela lógica de negócio relacionada à entidade
 * {@link Conta}.
 *
 * <p>
 * Aplica o conceito de Defense in Depth, garantindo que todas as operações
 * validem a titularidade do usuário sobre a entidade e suas dependências.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class ContaService {

    private final MessageSource messageSource;
    private final ContaRepository contaRepository;
    private final ContextoUsuarioService contextoUsuarioService;
    private final PessoaService pessoaService;
    private final TransacaoService transacaoService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param messageSource          A instância do MessageSource.
     * @param contaRepository        O repositório para acesso aos dados da conta.
     * @param contextoUsuarioService O serviço utilitário para recuperar o usuário
     *                               autenticado do contexto de segurança.
     * @param pessoaService          O serviço para buscar a pessoa titular.
     * @param transacaoService       O serviço para acesso as transações.
     */
    public ContaService(MessageSource messageSource, ContaRepository contaRepository,
            ContextoUsuarioService contextoUsuarioService, PessoaService pessoaService,
            @Lazy TransacaoService transacaoService) {
        this.messageSource = messageSource;
        this.contaRepository = contaRepository;
        this.pessoaService = pessoaService;
        this.contextoUsuarioService = contextoUsuarioService;
        this.transacaoService = transacaoService;
    }

    /**
     * Cadastra uma nova conta bancária vinculada a uma pessoa.
     *
     * <p>
     * Recupera o usuário do contexto de segurança e persiste a nova conta,
     * validando se a pessoa pertence ao usuário, se possui titularidade válida
     * e a unicidade dos dados da conta.
     *
     * @param dto Os dados da nova conta (nome, instituição, saldo inicial, pessoa).
     * @return O {@link ContaDTO} representando a conta criada.
     * @throws ViolacaoDadosException  Se houver duplicidade (mesmo nome e
     *                                 instituição para a mesma pessoa).
     * @throws RegraDeNegocioException Se a pessoa selecionada não for um titular
     *                                 habilitado.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    @Transactional
    public ContaDTO cadastrar(CriarAtualizarContaDTO dto) throws ViolacaoDadosException, EntityNotFoundException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        Pessoa pessoa = pessoaService.obterEntidadeValidada(usuarioId, dto.pessoaId());

        pessoaService.validarTitularidade(pessoa);

        Conta conta = paraEntidade(dto, pessoa);

        return paraDTO(salvarEntidade(conta));
    }

    /**
     * Lista as contas vinculadas ao usuário de forma paginada e filtrada.
     *
     * <p>
     * A ordenação, os filtros dinâmicos e a paginação são delegados ao SGBD através
     * de Specifications e do objeto Pageable.
     *
     * @param filtro   Objeto contendo os parâmetros de busca (nome, instituições,
     *                 pessoas).
     * @param pageable Configurações de página, tamanho e ordenação.
     * @return Uma {@link Page} de {@link ListagemContaDTO} otimizada para tabelas.
     */
    @Transactional(readOnly = true)
    public Page<ListagemContaDTO> listar(FiltroContaDTO filtro, Pageable pageable) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        Specification<Conta> spec = ContaSpec.comFiltros(usuarioId, filtro);

        return contaRepository.findAll(spec, pageable).map(this::paraListagemDTO);
    }

    /**
     * Lista as opções de contas visíveis para o usuário em componentes de seleção.
     *
     * <p>
     * Retorna uma lista leve contendo dados de identificação, instituição
     * financeira e o titular da conta para facilitar a distinção visual no
     * frontend.
     *
     * @return Uma lista não paginada de {@link SelecaoContaDTO}.
     */
    @Transactional(readOnly = true)
    public List<SelecaoContaDTO> listarOpcoes() {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        return contaRepository.buscarOpcoesParaSelecao(usuarioId);
    }

    /**
     * Busca os detalhes de uma conta pelo seu identificador.
     *
     * @param contaId O identificador da conta.
     * @return O {@link ContaDTO} completo contendo os dados para edição.
     * @throws EntityNotFoundException Se a conta não for encontrada ou não for
     *                                 visível ao usuário.
     */
    @Transactional(readOnly = true)
    public ContaDTO buscarPorId(UUID contaId) throws EntityNotFoundException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        return paraDTO(obterEntidadeValidada(usuarioId, contaId));
    }

    /**
     * Atualiza os dados de uma conta existente.
     *
     * <p>
     * Regra de Saldo: Se o saldo inicial for alterado, o {@code saldoAtual} é
     * recalculado aplicando a diferença, preservando assim o histórico
     * de lançamentos (receitas/despesas) que já afetaram o saldo atual.
     *
     * @param contaId O identificador da conta a ser atualizada.
     * @param dto     Os novos dados da conta.
     * @return O {@link ContaDTO} atualizado.
     * @throws ViolacaoDadosException  Se a alteração gerar duplicidade (mesmo nome
     *                                 e instituição para a mesma pessoa).
     * @throws RegraDeNegocioException Se a pessoa for alterada e o novo dono não
     *                                 for um titular habilitado.
     * @throws EntityNotFoundException Se a conta ou a pessoa não existirem ou
     *                                 pertencerem a outro usuário.
     */
    @Transactional
    public ContaDTO atualizar(UUID contaId, CriarAtualizarContaDTO dto) throws ViolacaoDadosException,
            EntityNotFoundException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        Conta conta = obterEntidadeValidada(usuarioId, contaId);
        Pessoa pessoa = pessoaService.obterEntidadeValidada(usuarioId, dto.pessoaId());

        boolean isPessoaAlterada = !conta.getPessoa().getId().equals(dto.pessoaId());

        if (isPessoaAlterada) {
            pessoaService.validarTitularidade(pessoa);
        }

        validarUnicidade(dto.pessoaId(), dto.instituicao(), dto.nome(), contaId, pessoa.getNome());

        if (isPessoaAlterada) {
            conta.setPessoa(pessoa);
        }

        if (conta.getSaldoInicial().compareTo(dto.saldoInicial()) != 0) {
            conta.alterarSaldoInicial(dto.saldoInicial());
        }

        conta.setNome(dto.nome().trim());
        conta.setInstituicao(dto.instituicao());

        return paraDTO(salvarEntidade(conta));
    }

    /**
     * Remove uma conta do sistema.
     *
     * <p>
     * Verifica se a conta existe e pertence ao usuário autenticado antes da
     * exclusão.
     *
     * @param contaId O identificador da conta a ser excluída.
     * @throws EntityNotFoundException Se a conta não for encontrada.
     * @throws ViolacaoDadosException  Se a conta possuir transações vinculadas.
     */
    @Transactional
    public void excluir(UUID contaId) throws EntityNotFoundException, ViolacaoDadosException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Conta conta = obterEntidadeValidada(usuarioId, contaId);

        validarDependenciasParaExclusao(conta);

        contaRepository.delete(conta);
    }

    /**
     * Busca a entidade Conta validando a cadeia de propriedade até o usuário.
     *
     * @param usuarioId O identificador único do usuário proprietário.
     * @param contaId   O identificador único da conta.
     * @return A entidade {@link Conta} validada.
     * @throws EntityNotFoundException Se a conta não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    Conta obterEntidadeValidada(UUID usuarioId, UUID contaId) throws EntityNotFoundException {
        return contaRepository.findByPessoaUsuarioIdAndId(usuarioId, contaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MetodosUteis.obterMensagem(messageSource, "erro.recurso.nao-encontrado",
                                MetodosUteis.obterMensagem(messageSource, "conta.nome.singular"), contaId)));
    }

    /**
     * Verifica se existe alguma conta vinculada a uma pessoa específica.
     * Utilizado pelo PessoaService para impedir a exclusão de pessoas com contas
     * ativas.
     *
     * @param pessoaId O identificador da pessoa.
     * @return {@code true} se possuir contas.
     */
    boolean existeContaVinculadaAPessoa(UUID pessoaId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        return contaRepository.existsByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId);
    }

    /**
     * Converte o DTO de entrada (dados da requisição) para a entidade
     * {@link Conta}.
     *
     * @param dto    Os dados recebidos da API.
     * @param pessoa A entidade {@link Pessoa} já validada e carregada do banco.
     * @return A entidade {@link Conta} instanciada (sem ID).
     */
    private Conta paraEntidade(CriarAtualizarContaDTO dto, Pessoa pessoa) {
        return new Conta(dto.nome().trim(), dto.instituicao(), dto.saldoInicial(), pessoa);
    }

    /**
     * Converte a entidade {@link Conta} (persistida) para o DTO de resposta
     * completo.
     *
     * @param conta A entidade carregada do banco.
     * @return O {@link ContaDTO} contendo os dados formatados para o cliente.
     */
    private ContaDTO paraDTO(Conta conta) {
        return new ContaDTO(conta);
    }

    /**
     * Converte a entidade {@link Conta} para o DTO otimizado de listagem.
     *
     * @param conta A entidade carregada do banco.
     * @return O {@link ListagemContaDTO} contendo dados simplificados para tabelas.
     */
    private ListagemContaDTO paraListagemDTO(Conta conta) {
        return new ListagemContaDTO(conta);
    }

    /**
     * Tenta salvar a conta no banco de dados.
     *
     * <p>
     * Captura {@link DataIntegrityViolationException} como uma segunda camada de
     * segurança para a constraint de unicidade do banco.
     *
     * @param conta Entidade a ser salva.
     * @return Entidade salva.
     */
    private Conta salvarEntidade(Conta conta) {
        String nomePessoaTitular = conta.getPessoa().getNome();

        try {
            validarUnicidade(conta);

            return contaRepository.saveAndFlush(conta);
        } catch (DataIntegrityViolationException _) {
            throw MetodosUteis.gerarExcecaoUnicidade(messageSource, Conta.class, "erro.unicidade.conta.detalhada",
                    conta.getNome(), conta.getInstituicao().getDescricao(), nomePessoaTitular);
        }
    }

    /**
     * Valida se já existe uma conta com o mesmo nome e instituição para a mesma
     * pessoa.
     *
     * <p>
     * A validação é delegada diretamente ao banco de dados, utilizando a função
     * {@code unaccent} do PostgreSQL. Isso garante performance e integridade
     * semântica, ignorando acentos e diferenças entre maiúsculas e minúsculas.
     *
     * @param pessoaId        ID da Pessoa (Titular).
     * @param instituicao     Instituição financeira.
     * @param nomeConta       Nome digitado pelo usuário.
     * @param contaIdAIgnorar ID da própria conta (em caso de atualização) ou null
     *                        (no cadastro).
     * @param nomePessoa      Nome da pessoa para montar a mensagem de erro
     *                        formatada.
     * @throws ViolacaoDadosException Se duplicidade for detectada.
     */
    private void validarUnicidade(UUID pessoaId, InstituicaoFinanceira instituicao, String nomeConta,
            UUID contaIdAIgnorar, String nomePessoa) throws ViolacaoDadosException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        boolean existeDuplicado = contaRepository.existeContaDuplicada(
                usuarioId,
                pessoaId,
                instituicao,
                nomeConta,
                contaIdAIgnorar);

        MetodosUteis.validarUnicidade(messageSource, existeDuplicado, Conta.class, "erro.unicidade.conta.detalhada",
                nomeConta, instituicao.getDescricao(), nomePessoa);
    }

    /**
     * Valida a unicidade da conta delegando os dados da Entidade para a sobrecarga
     * principal. Ideal para cenários de Cadastro (onde a entidade é nova).
     *
     * @param conta A entidade {@link Conta} a ser validada.
     * @throws ViolacaoDadosException Se duplicidade for detectada.
     */
    private void validarUnicidade(Conta conta) throws ViolacaoDadosException {
        validarUnicidade(
                conta.getPessoa().getId(),
                conta.getInstituicao(),
                conta.getNome(),
                conta.getId(),
                conta.getPessoa().getNome());
    }

    /**
     * Valida se a conta possui dependências ativas que impeçam sua exclusão.
     *
     * <p>
     * Consulta o serviço de transações para verificar se existem registros
     * vinculados a esta conta. Caso existam, delega a formatação da mensagem
     * e o lançamento da exceção para a classe utilitária.
     *
     * @param conta A entidade {@link Conta} que está sendo avaliada para exclusão.
     * @throws ViolacaoDadosException Caso existam transações vinculadas à conta.
     */
    private void validarDependenciasParaExclusao(Conta conta) throws ViolacaoDadosException {
        List<String> dependencias = new ArrayList<>();

        if (transacaoService.existeTransacaoVinculadaAConta(conta.getId())) {
            dependencias.add(MetodosUteis.obterMensagem(messageSource, "transacao.nome.plural"));
        }

        MetodosUteis.validarDependenciasExclusao(messageSource, conta.getNome(), dependencias);
    }
}
