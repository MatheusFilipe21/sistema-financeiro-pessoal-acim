package br.com.sfpacim.backend.services;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.FiltroTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.ListagemTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.TransacaoDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import br.com.sfpacim.backend.repositories.TransacaoRepository;
import br.com.sfpacim.backend.repositories.specifications.TransacaoSpec;
import br.com.sfpacim.backend.utils.MetodosUteis;

/**
 * Serviço responsável pela lógica de negócio relacionada à entidade
 * {@link Transacao}.
 *
 * <p>
 * Gerencia o ciclo de vida das receitas e despesas. Aplica Defense in Depth
 * delegando a validação de dependências (Conta, Pessoa, Categoria) para os
 * seus respectivos serviços de domínio.
 *
 * @author Matheus F. N. Pereira
 */
@Service
public class TransacaoService {

    private final MessageSource messageSource;
    private final ContextoUsuarioService contextoUsuarioService;
    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;
    private final ContaService contaService;
    private final PessoaService pessoaService;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * @param messageSource          A instância do MessageSource.
     * @param transacaoRepository    Repositório principal de transações.
     * @param contextoUsuarioService Serviço de contexto do usuário.
     * @param categoriaService       Serviço para validar categorias.
     * @param contaService           Serviço para validar contas.
     * @param pessoaService          Serviço para validar pessoas.
     */
    public TransacaoService(MessageSource messageSource,
            TransacaoRepository transacaoRepository,
            ContextoUsuarioService contextoUsuarioService,
            CategoriaService categoriaService,
            ContaService contaService,
            PessoaService pessoaService) {
        this.messageSource = messageSource;
        this.transacaoRepository = transacaoRepository;
        this.categoriaService = categoriaService;
        this.contaService = contaService;
        this.pessoaService = pessoaService;
        this.contextoUsuarioService = contextoUsuarioService;
    }

    /**
     * Cadastra uma nova transação financeira.
     *
     * <p>
     * Valida as entidades via services de domínio, monta a transação e, se ela
     * já nascer com status PAGO, efetiva imediatamente o impacto no saldo da conta.
     *
     * @param dto Os dados da nova transação.
     * @return O {@link TransacaoDTO} representando a transação criada.
     */
    @Transactional
    public TransacaoDTO cadastrar(CriarAtualizarTransacaoDTO dto) {
        Usuario usuario = contextoUsuarioService.getUsuarioAutenticado();
        UUID usuarioId = usuario.getId();

        Categoria categoria = categoriaService.obterEntidadeValidada(usuarioId, dto.categoriaId());
        Conta conta = contaService.obterEntidadeValidada(usuarioId, dto.contaId());

        Pessoa pessoa = null;
        if (dto.pessoaId() != null) {
            pessoa = pessoaService.obterEntidadeValidada(usuarioId, dto.pessoaId());
        }

        Transacao transacao = Transacao.builder()
                .descricao(dto.descricao().trim())
                .valor(dto.valor())
                .dataCompetencia(dto.dataCompetencia())
                .dataVencimento(dto.dataVencimento())
                .dataPagamento(dto.dataPagamento())
                .tipo(dto.tipo())
                .status(dto.status())
                .observacao(dto.observacao())
                .usuario(usuario)
                .categoria(categoria)
                .conta(conta)
                .pessoa(pessoa)
                .build();

        if (transacao.isPago()) {
            efetivarTransacao(transacao, conta);
        }

        transacao = salvarEntidade(transacao);

        return paraDTO(transacao);
    }

    /**
     * Lista as transações do usuário de forma paginada e filtrada.
     *
     * <p>
     * Utilizado para alimentar dashboards, extratos avançados e tabelas de
     * "Contas a Pagar/Receber". Delega a complexidade da busca e a paginação ao
     * SGBD. Retorna o DTO otimizado para exibição visual no frontend.
     *
     * @param filtro   DTO contendo todos os critérios dinâmicos de busca
     *                 (opcionais).
     * @param pageable Configuração de paginação e ordenação injetadas pelo Spring.
     * @return Página de {@link ListagemTransacaoDTO}.
     * @throws RegraDeNegocioException Se houver incoerência nas datas informadas
     *                                 (início maior que fim).
     */
    @Transactional(readOnly = true)
    public Page<ListagemTransacaoDTO> listar(FiltroTransacaoDTO filtro, Pageable pageable) {
        validarCoerenciaCronologica(filtro.dataVencimentoInicio(), filtro.dataVencimentoFim());

        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        Specification<Transacao> spec = TransacaoSpec.comFiltros(usuarioId, filtro);

        return transacaoRepository.findAll(spec, pageable).map(this::paraListagemDTO);
    }

    /**
     * Busca os detalhes de uma transação específica.
     *
     * @param transacaoId ID da transação.
     * @return DTO com os dados completos (para formulários de edição).
     */
    @Transactional(readOnly = true)
    public TransacaoDTO buscarPorId(UUID transacaoId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Transacao transacao = obterEntidadeValidada(usuarioId, transacaoId);

        return paraDTO(transacao);
    }

    /**
     * Atualiza uma transação existente.
     *
     * <p>
     * Utiliza o padrão de "Estorno e Efetivação":
     * 1. Se a transação original estava PAGA, o valor antigo é estornado da conta.
     * 2. Os dados da transação são atualizados com as informações recebidas.
     * 3. Se a nova configuração da transação estiver PAGA, o novo valor é
     * efetivado.
     * Isso garante consistência mesmo que o usuário troque de conta, mude o valor
     * ou altere o status do lançamento.
     *
     * @param transacaoId O identificador da transação a ser atualizada.
     * @param dto         Os novos dados fornecidos.
     * @return O {@link TransacaoDTO} atualizado.
     */
    @Transactional
    public TransacaoDTO atualizar(UUID transacaoId, CriarAtualizarTransacaoDTO dto) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Transacao transacao = obterEntidadeValidada(usuarioId, transacaoId);

        if (transacao.isPago()) {
            estornarTransacao(transacao, transacao.getConta());
        }

        if (!transacao.getCategoria().getId().equals(dto.categoriaId())) {
            transacao.setCategoria(categoriaService.obterEntidadeValidada(usuarioId, dto.categoriaId()));
        }

        if (!transacao.getConta().getId().equals(dto.contaId())) {
            transacao.setConta(contaService.obterEntidadeValidada(usuarioId, dto.contaId()));
        }

        if (dto.pessoaId() != null
                && (transacao.getPessoa() == null || !transacao.getPessoa().getId().equals(dto.pessoaId()))) {
            transacao.setPessoa(pessoaService.obterEntidadeValidada(usuarioId, dto.pessoaId()));
        } else if (dto.pessoaId() == null) {
            transacao.setPessoa(null);
        }

        transacao.setDescricao(dto.descricao().trim());
        transacao.setValor(dto.valor());
        transacao.setDataCompetencia(dto.dataCompetencia());
        transacao.setDataVencimento(dto.dataVencimento());
        transacao.setDataPagamento(dto.dataPagamento());
        transacao.setTipo(dto.tipo());
        transacao.setStatus(dto.status());
        transacao.setObservacao(dto.observacao());

        if (transacao.isPago()) {
            efetivarTransacao(transacao, transacao.getConta());
        }

        transacao = salvarEntidade(transacao);

        return paraDTO(transacao);
    }

    /**
     * Remove uma transação.
     * 
     * <p>
     * Aplica o estorno de saldo na conta caso a transação estivesse efetivada.
     *
     * @param transacaoId O identificador da transação.
     */
    @Transactional
    public void excluir(UUID transacaoId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        Transacao transacao = obterEntidadeValidada(usuarioId, transacaoId);

        if (transacao.isPago()) {
            estornarTransacao(transacao, transacao.getConta());
        }

        transacaoRepository.delete(transacao);
    }

    /**
     * Verifica se existe alguma transação vinculada a uma pessoa.
     * Utilizado pelo PessoaService para bloqueio de exclusão.
     *
     * @param pessoaId ID da pessoa.
     * @return {@code true} se existir.
     */
    boolean existeTransacaoVinculadaAPessoa(UUID pessoaId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        return transacaoRepository.existsByUsuarioIdAndPessoaId(usuarioId, pessoaId);
    }

    /**
     * Verifica se existe alguma transação vinculada a uma conta.
     * Utilizado pelo ContaService para bloqueio de exclusão.
     * 
     * @param contaId ID da conta.
     * @return {@code true} se existir.
     */
    boolean existeTransacaoVinculadaAConta(UUID contaId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        return transacaoRepository.existsByUsuarioIdAndContaId(usuarioId, contaId);
    }

    /**
     * Verifica se existe alguma transação vinculada a uma categoria.
     * Utilizado pelo CategoriaService para bloqueio de exclusão de categorias
     * personalizadas.
     *
     * @param categoriaId ID da categoria.
     * @return {@code true} se existir.
     */
    boolean existeTransacaoVinculadaACategoria(UUID categoriaId) {
        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();
        return transacaoRepository.existsByUsuarioIdAndCategoriaId(usuarioId, categoriaId);
    }

    /**
     * Busca uma transação garantindo que ela pertence ao usuário informado.
     *
     * <p>
     * Utiliza {@link EntityNotFoundException} tanto para "não encontrado" quanto
     * para "acesso negado" por segurança, evitando vazar a existência de IDs de
     * outros usuários (Defense in Depth).
     *
     * @param usuarioId   O identificador do usuário autenticado.
     * @param transacaoId O identificador da transação.
     * @return A entidade {@link Transacao} validada.
     * @throws EntityNotFoundException Se não existir ou não pertencer ao usuário.
     */
    Transacao obterEntidadeValidada(UUID usuarioId, UUID transacaoId) {
        return transacaoRepository.findByUsuarioIdAndId(usuarioId, transacaoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MetodosUteis.obterMensagem(messageSource, "erro.recurso.nao-encontrado",
                                MetodosUteis.obterMensagem(messageSource, "transacao.nome.singular"), transacaoId)));
    }

    /**
     * Encapsula a persistência tratando exceções de banco de dados.
     *
     * <p>
     * Converte erros técnicos (DataIntegrityViolation) em erros de negócio
     * amigáveis para a API.
     *
     * @param transacao A entidade a ser salva.
     * @return A entidade persistida e sincronizada (flush).
     */
    private Transacao salvarEntidade(Transacao transacao) {
        try {
            return transacaoRepository.saveAndFlush(transacao);
        } catch (DataIntegrityViolationException _) {
            throw new RegraDeNegocioException(
                    MetodosUteis.obterMensagem(messageSource, "erro.transacao.integridade.salvar"));
        }
    }

    /**
     * Converte a entidade para o DTO de leitura completo.
     * 
     * @param transacao Entidade persistida.
     * @return DTO contendo todos os dados (útil para edição).
     */
    private TransacaoDTO paraDTO(Transacao transacao) {
        return new TransacaoDTO(transacao);
    }

    /**
     * Converte a entidade para o DTO de leitura otimizado (Extrato).
     * 
     * @param transacao Entidade persistida.
     * @return DTO contendo dados amigáveis para tabelas.
     */
    private ListagemTransacaoDTO paraListagemDTO(Transacao transacao) {
        return new ListagemTransacaoDTO(transacao);
    }

    /**
     * Efetiva o impacto financeiro da transação no saldo da conta vinculada.
     *
     * <p>
     * A operação matemática depende da natureza do lançamento:
     * Se a transação for uma {@link TipoTransacao#RECEITA}, o valor é creditado.
     * Se for uma {@link TipoTransacao#DESPESA}, o valor é debitado.
     *
     * @param transacao A entidade contendo o valor e o tipo da movimentação.
     * @param conta     A entidade conta que sofrerá a alteração de saldo.
     */
    private void efetivarTransacao(Transacao transacao, Conta conta) {
        if (TipoTransacao.RECEITA.equals(transacao.getTipo())) {
            conta.creditar(transacao.getValor());
        } else {
            conta.debitar(transacao.getValor());
        }
    }

    /**
     * Reverte (estorna) o impacto financeiro de uma transação no saldo da conta.
     *
     * <p>
     * Utilizado para desfazer operações (ex: exclusão ou alteração de status).
     * A lógica aplica a operação matemática inversa à natureza do lançamento:
     * Estornar uma {@link TipoTransacao#RECEITA} resulta em débito.
     * Estornar uma {@link TipoTransacao#DESPESA} resulta em crédito.
     *
     * @param transacao A entidade original cujo valor será estornado.
     * @param conta     A entidade conta que receberá a reversão do saldo.
     */
    private void estornarTransacao(Transacao transacao, Conta conta) {
        if (TipoTransacao.RECEITA.equals(transacao.getTipo())) {
            conta.debitar(transacao.getValor());
        } else {
            conta.creditar(transacao.getValor());
        }
    }

    /**
     * Valida se o intervalo temporal é logicamente coerente.
     *
     * @param inicio Data inicial do período.
     * @param fim    Data final do período.
     * @throws RegraDeNegocioException Caso a data inicial seja posterior à final.
     */
    private void validarCoerenciaCronologica(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraDeNegocioException(
                    MetodosUteis.obterMensagem(messageSource, "erro.filtro.data.invalida"));
        }
    }
}
