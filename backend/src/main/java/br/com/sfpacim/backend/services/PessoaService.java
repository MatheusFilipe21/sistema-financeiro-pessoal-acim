package br.com.sfpacim.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import br.com.sfpacim.backend.utils.MetodosUteis;

/**
 * Serviço responsável pela lógica de negócio relacionada à {@link Pessoa}.
 *
 * <p>
 * Aplica rigorosamente o conceito de Defense in Depth, garantindo que
 * todas as operações validem a titularidade do usuário sobre a entidade.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final ContextoUsuarioService contextoUsuarioService;
    private final ContaService contaService;
    private final TransacaoService transacaoService;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * @param pessoaRepository       O repositório para acesso aos dados da pessoa.
     * @param contextoUsuarioService O serviço utilitário para recuperar o usuário
     *                               autenticado do contexto de segurança.
     * @param contaService           O serviço para acesso as contas.
     * @param transacaoService       O serviço para acesso as transações.
     */
    public PessoaService(PessoaRepository pessoaRepository, ContextoUsuarioService contextoUsuarioService,
            @Lazy ContaService contaService, @Lazy TransacaoService transacaoService) {
        this.pessoaRepository = pessoaRepository;
        this.contextoUsuarioService = contextoUsuarioService;
        this.contaService = contaService;
        this.transacaoService = transacaoService;
    }

    /**
     * Cadastra uma nova pessoa vinculada ao usuário autenticado.
     *
     * <p>
     * Recupera o usuário do contexto de segurança e persiste a nova pessoa,
     * validando unicidade de nome por usuário.
     *
     * @param dto Os dados da nova pessoa.
     * @return O {@link PessoaDTO} contendo os dados da pessoa criada.
     * @throws ViolacaoDadosException Se já existir uma pessoa com esse nome para o
     *                                usuário.
     */
    @Transactional
    public PessoaDTO cadastrar(CriarAtualizarPessoaDTO dto) throws ViolacaoDadosException {
        Pessoa entidade = paraEntidade(dto, contextoUsuarioService.getUsuarioAutenticado());

        return paraDTO(salvarEntidade(entidade));
    }

    /**
     * Lista todas as pessoas vinculadas ao usuário autenticado.
     * 
     * <p>
     * Aplica uma ordenação em memória pelo nome da pessoa (alfabética).
     *
     * @return Uma lista de {@link PessoaDTO}.
     */
    @Transactional(readOnly = true)
    public List<PessoaDTO> listar() {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        return pessoaRepository.findByUsuarioId(usuarioId)
                .stream()
                .sorted((p1, p2) -> MetodosUteis.collator().compare(p1.getNome(), p2.getNome()))
                .map(this::paraDTO)
                .toList();
    }

    /**
     * Atualiza os dados de uma pessoa existente.
     *
     * <p>
     * Verifica se a pessoa existe e se pertence ao usuário autenticado
     * antes de permitir a alteração.
     *
     * @param pessoaId O identificador da pessoa a ser atualizada.
     * @param dto      Os novos dados (nome).
     * @return O {@link PessoaDTO} atualizado.
     * @throws ViolacaoDadosException  Se o novo nome causar duplicidade.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    @Transactional
    public PessoaDTO atualizar(UUID pessoaId, CriarAtualizarPessoaDTO dto) throws ViolacaoDadosException,
            EntityNotFoundException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Pessoa pessoa = obterEntidadeValidada(usuarioId, pessoaId);

        pessoa.setNome(dto.nome().trim());

        if (dto.titular() != null) {
            pessoa.setTitular(dto.titular());
        }

        return paraDTO(salvarEntidade(pessoa));
    }

    /**
     * Remove uma pessoa do sistema.
     *
     * <p>
     * Verifica se a pessoa existe e pertence ao usuário autenticado.
     *
     * @param pessoaId O identificador da pessoa a ser excluída.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    @Transactional
    public void excluir(UUID pessoaId) throws EntityNotFoundException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Pessoa pessoa = obterEntidadeValidada(usuarioId, pessoaId);

        validarDependenciasParaExclusao(pessoa);

        pessoaRepository.delete(pessoa);
    }

    /**
     * Busca a entidade Pessoa validando a propriedade do usuário no banco de dados.
     *
     * @param usuarioId O identificador único do usuário proprietário.
     * @param pessoaId  O identificador único da pessoa.
     * @return A entidade {@link Pessoa} validada.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    Pessoa obterEntidadeValidada(UUID usuarioId, UUID pessoaId) throws EntityNotFoundException {
        return pessoaRepository.findByUsuarioIdAndId(usuarioId, pessoaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Pessoa com id %s não encontrada ou acesso negado.", pessoaId)));
    }

    /**
     * Valida se a pessoa possui permissão de titularidade para operações
     * financeiras.
     *
     * <p>
     * Regra de Negócio: Impede que dependentes ou pessoas não marcadas como
     * titulares sejam vinculadas a entidades que exigem titularidade (ex: Contas).
     *
     * @param pessoa A entidade {@link Pessoa} a ser validada.
     * @throws ViolacaoDadosException Se a pessoa não possuir a flag de titular
     *                                ativa.
     */
    void validarTitularidade(Pessoa pessoa) throws ViolacaoDadosException {
        if (!pessoa.isTitular()) {
            throw new ViolacaoDadosException(
                    String.format("A pessoa '%s' não é um titular habilitado para esta operação.", pessoa.getNome()));
        }
    }

    /**
     * Converte o DTO para a entidade {@link Pessoa}, vinculando ao usuário.
     *
     * @param dto     Os dados de entrada.
     * @param usuario O usuário autenticado (pai do registro).
     * @return A entidade pronta para persistência.
     */
    private Pessoa paraEntidade(CriarAtualizarPessoaDTO dto, Usuario usuario) {
        Pessoa pessoa = new Pessoa(dto.nome().trim(), usuario);
        pessoa.setTitular(Boolean.TRUE.equals(dto.titular()));

        return pessoa;
    }

    /**
     * Converte a entidade {@link Pessoa} para um {@link PessoaDTO}.
     *
     * @param pessoa A entidade vinda do banco.
     * @return O DTO correspondente.
     */
    private PessoaDTO paraDTO(Pessoa pessoa) {
        return new PessoaDTO(pessoa);
    }

    /**
     * Tenta salvar uma entidade {@link Pessoa} no repositório.
     *
     * <p>
     * Trata a exceção de violação de integridade (nome duplicado para o usuário),
     * lançando uma exceção de negócio legível.
     *
     * @param pessoa Entidade {@link Pessoa} a ser salva.
     * @return A entidade salva.
     * @throws ViolacaoDadosException Caso o nome já esteja cadastrado para este
     *                                usuário.
     */
    private Pessoa salvarEntidade(Pessoa pessoa) throws ViolacaoDadosException {
        try {
            validarUnicidadeNome(pessoa);

            return pessoaRepository.saveAndFlush(pessoa);
        } catch (DataIntegrityViolationException _) {
            MetodosUteis.validarUnicidade(true, Pessoa.class.getSimpleName(), pessoa.getNome());
            return null;
        }
    }

    /**
     * Valida se já existe uma pessoa com o mesmo nome para o usuário autenticado.
     *
     * <p>
     * A validação é realizada em memória para garantir portabilidade entre
     * diferentes bancos de dados (H2, Postgres), utilizando normalização de strings
     * para ignorar acentos e diferenças de caixa.
     *
     * @param pessoa A entidade {@link Pessoa} a ser validada.
     * @throws ViolacaoDadosException Caso o nome já esteja cadastrado.
     */
    private void validarUnicidadeNome(Pessoa pessoa) throws ViolacaoDadosException {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        String nomeNovoNormalizado = MetodosUteis.normalizarParaBusca(pessoa.getNome());

        boolean existeDuplicado = pessoaRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(p -> pessoa.getId() == null || !p.getId().equals(pessoa.getId()))
                .anyMatch(p -> MetodosUteis.normalizarParaBusca(p.getNome()).equals(nomeNovoNormalizado));

        MetodosUteis.validarUnicidade(existeDuplicado, Pessoa.class.getSimpleName(), pessoa.getNome());
    }

    /**
     * Valida se a pessoa possui dependências ativas que impeçam sua exclusão.
     *
     * <p>
     * Consulta os serviços de domínio dependentes (Contas e Transações) para
     * verificar se existem registros vinculados a esta pessoa. Delega a
     * formatação da mensagem e o lançamento da exceção para a classe utilitária.
     *
     * @param pessoa A entidade {@link Pessoa} que está sendo avaliada para
     *               exclusão.
     * @throws ViolacaoDadosException Caso existam contas ou transações vinculadas.
     */
    private void validarDependenciasParaExclusao(Pessoa pessoa) throws ViolacaoDadosException {
        List<String> dependencias = new ArrayList<>();
        UUID pessoaId = pessoa.getId();

        if (contaService.existeContaVinculadaAPessoa(pessoaId)) {
            dependencias.add("Contas");
        }

        if (transacaoService.existeTransacaoVinculadaAPessoa(pessoaId)) {
            dependencias.add("Transações");
        }

        MetodosUteis.validarDependenciasExclusao(pessoa.getNome(), dependencias);
    }
}
