package br.com.sfpacim.backend.services;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.repositories.ContaRepository;
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

    private final ContaRepository contaRepository;
    private final ContextoUsuarioService contextoUsuarioService;
    private final PessoaService pessoaService;
    private final TransacaoService transacaoService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param contaRepository        O repositório para acesso aos dados da conta.
     * @param contextoUsuarioService O serviço utilitário para recuperar o usuário
     *                               autenticado do contexto de segurança.
     * @param pessoaService          O serviço para buscar a pessoa titular.
     * @param transacaoService       O serviço para acesso as transações.
     */
    public ContaService(ContaRepository contaRepository, ContextoUsuarioService contextoUsuarioService,
            PessoaService pessoaService, @Lazy TransacaoService transacaoService) {
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
     * validando se a pessoa pertence ao usuário, se é titular e a unicidade
     * de nome.
     *
     * @param dto Os dados da nova conta (nome, instituição, saldo inicial, pessoa).
     * @return O {@link ContaDTO} representando a conta criada.
     * @throws ViolacaoDadosException  Se houver duplicidade de nome ou se a pessoa
     *                                 não for titular.
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
     * Lista todas as contas de todas as pessoas vinculadas ao usuário autenticado.
     *
     * <p>
     * Aplica uma ordenação composta em memória:
     * <ul>
     * <li><b>1º Nível:</b> Nome do Titular (alfabética).</li>
     * <li><b>2º Nível:</b> Nome da Conta (alfabética).</li>
     * </ul>
     *
     * @return Uma lista de {@link ContaDTO} ordenada por nome do titular e nome da
     *         conta.
     */
    @Transactional(readOnly = true)
    public List<ContaDTO> listar() {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Collator collator = MetodosUteis.collator();

        return contaRepository.findByPessoaUsuarioId(usuarioId)
                .stream()
                .sorted(Comparator
                        .comparing((Conta c) -> c.getPessoa().getNome(), collator)
                        .thenComparing(Conta::getNome, collator))
                .map(this::paraDTO)
                .toList();
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
     * @throws ViolacaoDadosException  Se o novo nome gerar duplicidade.
     * @throws EntityNotFoundException Se a conta não existir ou pertencer a
     *                                 outro usuário.
     */
    @Transactional
    public ContaDTO atualizar(UUID contaId, CriarAtualizarContaDTO dto) throws ViolacaoDadosException,
            EntityNotFoundException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Conta conta = obterEntidadeValidada(usuarioId, contaId);

        if (!conta.getPessoa().getId().equals(dto.pessoaId())) {
            Pessoa pessoa = pessoaService.obterEntidadeValidada(usuarioId, dto.pessoaId());
            pessoaService.validarTitularidade(pessoa);
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
     */
    @Transactional
    public void excluir(UUID contaId) throws EntityNotFoundException {
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
                        String.format("Conta com id %s não encontrada ou acesso negado.", contaId)));
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
     * Converte a entidade {@link Conta} (persistida) para o DTO de resposta.
     *
     * @param conta A entidade carregada do banco.
     * @return O {@link ContaDTO} contendo os dados formatados para o cliente.
     */
    private ContaDTO paraDTO(Conta conta) {
        return new ContaDTO(conta);
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
        try {
            validarUnicidadeNome(conta);

            return contaRepository.saveAndFlush(conta);
        } catch (DataIntegrityViolationException _) {
            MetodosUteis.validarUnicidade(true, Conta.class.getSimpleName(), conta.getNome(),
                    conta.getPessoa().getNome());
            return null;
        }
    }

    /**
     * Valida se já existe uma conta com o mesmo nome para a mesma pessoa.
     *
     * <p>
     * A validação é realizada em memória para garantir portabilidade entre
     * diferentes bancos de dados (H2, Postgres), utilizando normalização de strings
     * para ignorar acentos e diferenças de caixa.
     *
     * @param conta A entidade {@link Conta} a ser validada.
     * @throws ViolacaoDadosException Se duplicidade for detectada.
     */
    private void validarUnicidadeNome(Conta conta) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        UUID pessoaId = conta.getPessoa().getId();
        String nomeNovoNormalizado = MetodosUteis.normalizarParaBusca(conta.getNome());

        boolean existeDuplicado = contaRepository.findByPessoaUsuarioIdAndPessoaId(usuarioId, pessoaId)
                .stream()
                .filter(c -> conta.getId() == null || !c.getId().equals(conta.getId()))
                .filter(c -> c.getInstituicao() == conta.getInstituicao())
                .anyMatch(c -> MetodosUteis.normalizarParaBusca(c.getNome()).equals(nomeNovoNormalizado));

        MetodosUteis.validarUnicidade(existeDuplicado, Conta.class.getSimpleName(), conta.getNome(),
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
            dependencias.add("Transações");
        }

        MetodosUteis.validarDependenciasExclusao(conta.getNome(), dependencias);
    }
}
