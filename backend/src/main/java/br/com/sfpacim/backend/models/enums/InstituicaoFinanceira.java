package br.com.sfpacim.backend.models.enums;

import lombok.Getter;

/**
 * Enum que representa as instituições financeiras suportadas pelo sistema.
 *
 * <p>
 * A lista inclui os maiores bancos do Brasil por número de clientes e as
 * principais fintechs/carteiras digitais.
 * Utilizado para padronizar a seleção e o mapeamento de ícones.
 *
 * @author Matheus F. N. Pereira
 */
@Getter
public enum InstituicaoFinanceira {

    CAIXA("Caixa Econômica Federal"),
    BB("Banco do Brasil"),
    ITAU("Itaú"),
    BRADESCO("Bradesco"),
    SANTANDER("Santander"),
    SAFRA("Banco Safra"),
    BANRISUL("Banrisul"),
    VOTORANTIM("Banco BV"),
    BANCO_NORDESTE("Banco do Nordeste"),
    BRB("BRB - Banco de Brasília"),
    NUBANK("Nubank"),
    INTER("Banco Inter"),
    C6("C6 Bank"),
    PAGBANK("PagBank"),
    MERCADO_PAGO("Mercado Pago"),
    PICPAY("PicPay"),
    NEON("Neon"),
    NEXT("Next"),
    PAN("Banco Pan"),
    AGIBANK("Agibank"),
    BTG("BTG Pactual"),
    XP("XP Investimentos"),
    RICO("Rico"),
    DINHEIRO("Dinheiro em Espécie"),
    OUTROS("Outros");

    private final String descricao;

    /**
     * Construtor do enum.
     * 
     * @param descricao O nome amigável da instituição para exibição.
     */
    InstituicaoFinanceira(String descricao) {
        this.descricao = descricao;
    }
}
