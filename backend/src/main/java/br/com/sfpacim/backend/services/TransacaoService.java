package br.com.sfpacim.backend.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.TransacaoDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import br.com.sfpacim.backend.repositories.TransacaoRepository;

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

    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;
    private final ContaService contaService;
    private final PessoaService pessoaService;
    private final ContextoUsuarioService contextoUsuarioService;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * @param transacaoRepository    Repositório principal de transações.
     * @param categoriaService       Serviço para validar categorias.
     * @param contaService           Serviço para validar contas.
     * @param pessoaService          Serviço para validar pessoas.
     * @param contextoUsuarioService Serviço de contexto do usuário.
     */
    public TransacaoService(TransacaoRepository transacaoRepository,
            CategoriaService categoriaService,
            ContaService contaService,
            PessoaService pessoaService,
            ContextoUsuarioService contextoUsuarioService) {
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
     * Lista transações paginadas e filtradas por período de vencimento.
     *
     * <p>
     * Utilizado principalmente para alimentar a tabela de "Contas a Pagar/Receber".
     *
     * @param inicio   Data inicial do vencimento.
     * @param fim      Data final do vencimento.
     * @param pageable Configuração de paginação e ordenação.
     * @return Página de {@link TransacaoDTO}.
     * @throws RegraDeNegocioException Se a data de início for posterior à data
     *                                 final.
     * @throws RegraDeNegocioException Se o período de consulta ultrapassar 90 dias.
     */
    @Transactional(readOnly = true)
    public Page<TransacaoDTO> listarPorPeriodo(LocalDate inicio, LocalDate fim, Pageable pageable) {
        if (inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("A data de início não pode ser posterior à data final.");
        }

        long diferencaEmDias = ChronoUnit.DAYS.between(inicio, fim);

        if (diferencaEmDias > 90) {
            throw new RegraDeNegocioException("O período de consulta não pode ultrapassar 90 dias.");
        }

        UUID usuarioId = contextoUsuarioService.getUsuarioAutenticado().getId();

        Page<Transacao> transacoes = transacaoRepository.findByUsuarioIdAndDataVencimentoBetween(
                usuarioId, inicio, fim, pageable);

        return transacoes.map(this::paraDTO);
    }

    /**
     * Busca os detalhes de uma transação específica.
     *
     * @param transacaoId ID da transação.
     * @return DTO com os dados completos.
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
                        String.format("Transação com id %s não encontrada ou acesso negado.", transacaoId)));
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
                    "Erro de integridade ao salvar transação. Verifique os dados fornecidos.");
        }
    }

    /**
     * Converte a entidade para DTO.
     * 
     * @param transacao Entidade persistida.
     * @return DTO de leitura.
     */
    private TransacaoDTO paraDTO(Transacao transacao) {
        return new TransacaoDTO(transacao);
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
}
