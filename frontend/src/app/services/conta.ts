import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CriarAtualizarContaDTO } from '../dtos/conta/CriarAtualizarContaDTO';
import { ContaDTO } from '../dtos/conta/ContaDTO';

/**
 * Serviço responsável pela comunicação com os endpoints
 * de /contas no Backend.
 *
 * @author Matheus F. N. Pereira
 */
@Injectable({
  providedIn: 'root',
})
export class Conta {
  /**
   * URL base para os endpoints de contas (Plural no REST).
   * O 'PrefixoApiInterceptor' adicionará '/api' automaticamente.
   */
  private readonly API_URL = '/contas';

  private readonly http = inject(HttpClient);

  /**
   * Chama o endpoint GET /contas no backend.
   *
   * @returns Um Observable contendo a lista de ContaDTO.
   */
  listar(): Observable<ContaDTO[]> {
    return this.http.get<ContaDTO[]>(this.API_URL);
  }

  /**
   * Chama o endpoint POST /contas no backend.
   *
   * @param dados O DTO contendo os dados da nova conta (incluindo pessoaId).
   * @returns Um Observable com o ContaDTO criado.
   */
  cadastrar(dados: CriarAtualizarContaDTO): Observable<ContaDTO> {
    return this.http.post<ContaDTO>(this.API_URL, dados);
  }

  /**
   * Chama o endpoint PUT /contas/{id} no backend.
   *
   * @param id O UUID da conta a ser atualizada.
   * @param dados O DTO com os novos dados.
   * @returns Um Observable com o ContaDTO atualizado.
   */
  atualizar(id: string, dados: CriarAtualizarContaDTO): Observable<ContaDTO> {
    return this.http.put<ContaDTO>(`${this.API_URL}/${id}`, dados);
  }

  /**
   * Chama o endpoint DELETE /contas/{id} no backend.
   *
   * @param id O UUID da conta a ser excluída.
   * @returns Um Observable vazio (void) indicando sucesso.
   */
  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
