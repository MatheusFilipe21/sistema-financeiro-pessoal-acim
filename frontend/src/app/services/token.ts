import { Injectable } from '@angular/core';

/**
 * Serviço de infraestrutura responsável exclusivamente pelo gerenciamento
 * da persistência do Token JWT no LocalStorage do navegador.
 *
 * @author Matheus F. N. Pereira
 */
@Injectable({
  providedIn: 'root',
})
export class Token {
  /**
   * Chave constante utilizada para gravar o token no armazenamento local.
   */
  private readonly CHAVE_TOKEN = 'sfp-acim-token-jwt';

  /**
   * Salva o token JWT no LocalStorage.
   *
   * @param token A string do token JWT recebida do backend.
   */
  salvar(token: string): void {
    localStorage.setItem(this.CHAVE_TOKEN, token);
  }

  /**
   * Recupera o token JWT armazenado (raw string).
   *
   * @returns A string do token JWT se existir no LocalStorage, ou `null` caso contrário.
   */
  obter(): string | null {
    return localStorage.getItem(this.CHAVE_TOKEN);
  }

  /**
   * Remove o token do armazenamento, efetivando o logout local.
   */
  remover(): void {
    localStorage.removeItem(this.CHAVE_TOKEN);
  }

  /**
   * Verifica a existência de um token armazenado.
   *
   * Nota: Este método verifica apenas se a string existe,
   * não valida a expiração ou assinatura (isso é feito no AutenticacaoService).
   *
   * @returns `true` se houver uma string de token armazenada; `false` caso contrário.
   */
  possuiToken(): boolean {
    return !!this.obter();
  }
}
