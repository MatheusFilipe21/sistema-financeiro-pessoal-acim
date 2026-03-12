package br.com.sfpacim.backend.services;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.ContaRepository;
import br.com.sfpacim.backend.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço responsável pela lógica de negócio relacionada à {@link Pessoa}.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final ContaRepository contaRepository;
    private final ContextoUsuarioService contextoUsuarioService;
    private final Collator collator;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * @param pessoaRepository       O repositório para acesso aos dados da pessoa.
     * @param pessoaRepository       O repositório para acesso aos dados das contas.
     * @param contextoUsuarioService O serviço utilitário para recuperar o usuário
     *                               autenticado do contexto de segurança.
     */
    public PessoaService(PessoaRepository pessoaRepository, ContaRepository contaRepository,
            ContextoUsuarioService contextoUsuarioService) {
        this.pessoaRepository = pessoaRepository;
        this.contaRepository = contaRepository;
        this.contextoUsuarioService = contextoUsuarioService;

        this.collator = Collator.getInstance(Locale.of("pt", "BR"));
        this.collator.setStrength(Collator.PRIMARY);
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
    public PessoaDTO cadastrar(CriarAtualizarPessoaDTO dto) throws ViolacaoDadosException {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        Pessoa entidade = paraEntidade(dto, usuario);

        return paraDTO(this.salvarEntidade(entidade));
    }

    /**
     * Lista todas as pessoas vinculadas ao usuário autenticado.
     *
     * <p>
     * Garante o isolamento dos dados (Multi-tenancy), retornando apenas
     * registros que pertencem ao token informado.
     *
     * @return Uma lista de {@link PessoaDTO}.
     */
    public List<PessoaDTO> listar() {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        return pessoaRepository.findByUsuario(usuario)
                .stream()
                .sorted((p1, p2) -> collator.compare(p1.getNome(), p2.getNome()))
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
     * @param id  O identificador da pessoa a ser atualizada.
     * @param dto Os novos dados (nome).
     * @return O {@link PessoaDTO} atualizado.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     * @throws ViolacaoDadosException  Se o novo nome causar duplicidade.
     */
    public PessoaDTO atualizar(UUID id, CriarAtualizarPessoaDTO dto) throws ViolacaoDadosException {
        Pessoa pessoa = buscarPessoaValidada(id);

        pessoa.setNome(dto.nome().trim());

        if (dto.titular() != null) {
            pessoa.setTitular(dto.titular());
        }

        return paraDTO(this.salvarEntidade(pessoa));
    }

    /**
     * Remove uma pessoa do sistema.
     *
     * <p>
     * Verifica se a pessoa existe e pertence ao usuário autenticado.
     *
     * @param id O identificador da pessoa a ser excluída.
     * @throws EntityNotFoundException Se a pessoa não for encontrada ou não
     *                                 pertencer ao usuário.
     */
    public void excluir(UUID id) {
        Pessoa pessoa = buscarPessoaValidada(id);

        validarDependenciasParaExclusao(pessoa);

        pessoaRepository.delete(pessoa);
    }

    /**
     * Verifica se a pessoa possui vínculos que impedem a exclusão (Contas, Cartões,
     * etc).
     */
    private void validarDependenciasParaExclusao(Pessoa pessoa) {
        boolean possuiContas = !contaRepository.findByPessoa(pessoa).isEmpty();

        if (possuiContas) {
            throw new ViolacaoDadosException(
                    String.format(
                            "Não é possível excluir '%s' pois existem Contas vinculadas. Exclua as contas primeiro.",
                            pessoa.getNome()));
        }
    }

    private Pessoa buscarPessoaValidada(UUID id) {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();

        return pessoaRepository.findById(id)
                .filter(p -> p.getUsuario().equals(usuario))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Pessoa com id %s não encontrada ou acesso negado.", id)));
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
            throw excecaoNomeDuplicado(pessoa.getNome());
        }
    }

    /**
     * Valida se já existe uma pessoa com o mesmo nome para o usuário autenticado.
     *
     * <p>
     * A validação segue as seguintes regras de normalização:
     * <ul>
     * <li>Ignora diferenças de acentuação e caixa (via {@link Collator}).</li>
     * <li>Ignora espaços em branco no início e fim (trim).</li>
     * </ul>
     * Exemplo: " joao " será considerado duplicado de "João".
     *
     * @param pessoa A entidade {@link Pessoa} contendo o nome e o ID (se houver) a
     *               ser validada.
     * @throws ViolacaoDadosException Caso o nome já esteja cadastrado para este
     *                                usuário.
     */
    private void validarUnicidadeNome(Pessoa pessoa) {
        List<Pessoa> pessoasDoUsuario = pessoaRepository.findByUsuario(pessoa.getUsuario());

        boolean existeDuplicado = pessoasDoUsuario.stream()
                .filter(p -> !Objects.equals(p.getId(), pessoa.getId()))
                .anyMatch(p -> collator.equals(p.getNome().trim(), pessoa.getNome().trim()));

        if (existeDuplicado) {
            throw excecaoNomeDuplicado(pessoa.getNome());
        }
    }

    /**
     * Cria a instância da exceção de regra de negócio para nome duplicado.
     * Centraliza a mensagem de erro para garantir consistência.
     *
     * @param nome O nome que causou o conflito de duplicidade.
     * @return A exceção {@link ViolacaoDadosException} pronta para ser lançada.
     */
    private ViolacaoDadosException excecaoNomeDuplicado(String nome) {
        return new ViolacaoDadosException(
                String.format("Já existe uma pessoa cadastrada com o nome '%s'.", nome));
    }
}
