package br.com.sfpacim.backend.services;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.ContaRepository;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço responsável pela lógica de negócio relacionada à entidade
 * {@link Conta}.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final PessoaRepository pessoaRepository;
    private final ContextoUsuarioService contextoUsuarioService;
    private final Collator collator;

    /**
     * Construtor para Injeção de Dependências.
     *
     *
     * @param contaRepository        O repositório para persistência de contas.
     * @param pessoaRepository       O repositório para buscar a pessoa titular.
     * @param contextoUsuarioService O serviço para recuperar o usuário autenticado.
     */
    public ContaService(ContaRepository contaRepository, PessoaRepository pessoaRepository,
            ContextoUsuarioService contextoUsuarioService) {
        this.contaRepository = contaRepository;
        this.pessoaRepository = pessoaRepository;
        this.contextoUsuarioService = contextoUsuarioService;

        this.collator = Collator.getInstance(Locale.of("pt", "BR"));
        this.collator.setStrength(Collator.PRIMARY);
    }

    /**
     * Cadastra uma nova conta bancária vinculada a uma pessoa.
     *
     * <p>
     * Recupera o usuário do contexto de segurança e persiste a nova conta,
     * validando se a pessoa pertence ao usuário, se é titular e a unicidade
     * de nome (RF - Unicidade).
     *
     * @param dto Os dados da nova conta (nome, instituição, saldo inicial, pessoa).
     * @return O {@link ContaDTO} representando a conta criada.
     * @throws ViolacaoDadosException  Se houver duplicidade de nome ou se a pessoa
     *                                 não for titular.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    @Transactional
    public ContaDTO cadastrar(CriarAtualizarContaDTO dto) throws ViolacaoDadosException {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();
        Pessoa pessoa = buscarPessoaDoUsuario(dto.pessoaId(), usuario);

        validarTitularidade(pessoa);
        validarUnicidadeNome(dto.nome(), pessoa, null);

        Conta conta = paraEntidade(dto, pessoa);

        return paraDTO(salvarEntidade(conta));
    }

    /**
     * Lista todas as contas de todas as pessoas vinculadas ao usuário autenticado.
     *
     * <p>
     * O método percorre todas as pessoas do usuário, busca suas respectivas contas,
     * agrupa tudo em uma única lista e ordena alfabeticamente pelo nome da conta.
     *
     * @return Uma lista de {@link ContaDTO} ordenada por nome.
     */
    public List<ContaDTO> listar() {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        return pessoaRepository.findByUsuario(usuario)
                .stream()
                .map(contaRepository::findByPessoa)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Conta::getNome))
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
     * @param id  O identificador da conta a ser atualizada.
     * @param dto Os novos dados da conta.
     * @return O {@link ContaDTO} atualizado.
     * @throws EntityNotFoundException Se a conta não existir ou pertencer a outro
     *                                 usuário.
     * @throws ViolacaoDadosException  Se o novo nome gerar duplicidade.
     */
    @Transactional
    public ContaDTO atualizar(UUID id, CriarAtualizarContaDTO dto) throws ViolacaoDadosException {
        Conta conta = buscarContaValidada(id);

        if (!conta.getPessoa().getId().equals(dto.pessoaId())) {
            Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();
            Pessoa novaPessoa = buscarPessoaDoUsuario(dto.pessoaId(), usuario);
            validarTitularidade(novaPessoa);
            conta.setPessoa(novaPessoa);
        }

        validarUnicidadeNome(dto.nome(), conta.getPessoa(), id);

        if (!conta.getSaldoInicial().equals(dto.saldoInicial())) {
            recalcularSaldoAtual(conta, dto.saldoInicial());
        }

        conta.setNome(dto.nome());
        conta.setInstituicao(dto.instituicao());
        conta.setSaldoInicial(dto.saldoInicial());

        return paraDTO(salvarEntidade(conta));
    }

    /**
     * Remove uma conta do sistema.
     *
     * <p>
     * Verifica se a conta existe e pertence ao usuário autenticado antes da
     * exclusão.
     * (Futuramente validará se existem transações vinculadas que impeçam a
     * exclusão).
     *
     * @param id O identificador da conta a ser excluída.
     * @throws EntityNotFoundException Se a conta não for encontrada.
     */
    @SuppressWarnings("null")
    public void excluir(UUID id) {
        Conta conta = buscarContaValidada(id);
        contaRepository.delete(conta);
    }

    /**
     * Aplica a lógica de correção do Saldo Atual quando o Saldo Inicial é editado.
     *
     * <p>
     * Fórmula: {@code SaldoAtual = SaldoAtual + (NovoInicial - AntigoInicial)}
     *
     * @param conta            A entidade conta a ser ajustada.
     * @param novoSaldoInicial O novo valor informado pelo usuário.
     */
    private void recalcularSaldoAtual(Conta conta, BigDecimal novoSaldoInicial) {
        BigDecimal diferenca = novoSaldoInicial.subtract(conta.getSaldoInicial());
        conta.setSaldoAtual(conta.getSaldoAtual().add(diferenca));
    }

    /**
     * Busca uma Pessoa pelo ID e garante que ela pertence ao Usuário autenticado.
     * Garante o isolamento dos dados (Multi-tenancy).
     *
     * @param pessoaId ID da pessoa.
     * @param usuario  Usuário autenticado.
     * @return A entidade Pessoa validada.
     * @throws EntityNotFoundException Se não encontrada ou acesso negado.
     */
    @SuppressWarnings("null")
    private Pessoa buscarPessoaDoUsuario(UUID pessoaId, Usuario usuario) {
        return pessoaRepository.findById(pessoaId)
                .filter(p -> p.getUsuario().equals(usuario))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Pessoa com id %s não encontrada ou acesso negado.", pessoaId)));
    }

    /**
     * Busca uma Conta pelo ID e valida a cadeia de propriedade:
     * Conta -> Pessoa -> Usuário.
     *
     * @param id ID da conta.
     * @return A entidade Conta validada.
     * @throws EntityNotFoundException Se não encontrada ou acesso negado.
     */
    @SuppressWarnings("null")
    private Conta buscarContaValidada(UUID id) {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        return contaRepository.findById(id)
                .filter(c -> c.getPessoa().getUsuario().equals(usuario))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Conta com id %s não encontrada ou acesso negado.", id)));
    }

    /**
     * Valida a regra de negócio que impede a criação de contas para dependentes
     * ou pessoas não marcadas como titulares.
     *
     * @param pessoa A pessoa a ser validada.
     * @throws ViolacaoDadosException Se a flag titular for false.
     */
    private void validarTitularidade(Pessoa pessoa) {
        if (!pessoa.isTitular()) {
            throw new ViolacaoDadosException(
                    String.format("A pessoa '%s' não é um titular habilitado para ter contas.", pessoa.getNome()));
        }
    }

    /**
     * Valida se já existe uma conta com o mesmo nome para a mesma pessoa.
     *
     * <p>
     * Utiliza o {@link Collator} para ignorar diferenças de caixa e acentuação
     * (ex: "Itaú" == "itau").
     *
     * @param nome         O nome da conta.
     * @param pessoa       A pessoa titular.
     * @param idContaAtual ID da conta sendo editada (null se for cadastro novo)
     *                     para ignorar a si mesma na validação.
     * @throws ViolacaoDadosException Se duplicidade for detectada.
     */
    private void validarUnicidadeNome(String nome, Pessoa pessoa, UUID idContaAtual) {
        List<Conta> contasDaPessoa = contaRepository.findByPessoa(pessoa);

        boolean existeDuplicado = contasDaPessoa.stream()
                .filter(c -> !Objects.equals(c.getId(), idContaAtual))
                .anyMatch(c -> collator.equals(c.getNome().trim(), nome.trim()));

        if (existeDuplicado) {
            throw excecaoNomeDuplicado(nome, pessoa.getNome());
        }
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
        return new Conta(dto.nome(), dto.instituicao(), dto.saldoInicial(), pessoa);
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
    @SuppressWarnings("null")
    private Conta salvarEntidade(Conta conta) {
        try {
            return contaRepository.save(conta);
        } catch (DataIntegrityViolationException e) {
            throw excecaoNomeDuplicado(conta.getNome(), conta.getPessoa().getNome());
        }
    }

    /**
     * Cria a instância da exceção de regra de negócio para nome duplicado.
     * <p>
     * Centraliza a formatação da mensagem de erro para garantir consistência
     * nas respostas da API.
     *
     * @param nomeConta  O nome da conta que causou o conflito.
     * @param nomePessoa O nome do titular da conta.
     * @return A exceção {@link ViolacaoDadosException} pronta para ser lançada.
     */
    private ViolacaoDadosException excecaoNomeDuplicado(String nomeConta, String nomePessoa) {
        return new ViolacaoDadosException(
                String.format("Já existe uma conta '%s' cadastrada para %s.", nomeConta, nomePessoa));
    }
}
