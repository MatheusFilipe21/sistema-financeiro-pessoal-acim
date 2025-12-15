import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CriarAtualizarPessoaDTO } from '../dtos/pessoa/CriarAtualizarPessoaDTO';
import { PessoaDTO } from '../dtos/pessoa/PessoaDTO';

/**
 * Serviço responsável pela comunicação com os endpoints
 * de /pessoas no Backend.
 *
 * @author Matheus F. N. Pereira
 */
@Injectable({
  providedIn: 'root',
})
export class Pessoa {
  /**
   * URL base para os endpoints de pessoas (Plural no REST).
   * O 'PrefixoApiInterceptor' adicionará '/api' automaticamente.
   */
  private readonly API_URL = '/pessoas';

  private readonly http = inject(HttpClient);

  /**
   * Chama o endpoint GET /pessoas no backend (RF47).
   *
   * @returns Um Observable contendo a lista de PessoaDTO.
   */
  listar(): Observable<PessoaDTO[]> {
    return this.http.get<PessoaDTO[]>(this.API_URL);
  }

  /**
   * Chama o endpoint POST /pessoas no backend (RF46).
   *
   * @param dados O DTO contendo o nome da nova pessoa.
   * @returns Um Observable com o PessoaDTO criado.
   */
  cadastrar(dados: CriarAtualizarPessoaDTO): Observable<PessoaDTO> {
    return this.http.post<PessoaDTO>(this.API_URL, dados);
  }

  /**
   * Chama o endpoint PUT /pessoas/{id} no backend.
   *
   * @param id O UUID da pessoa a ser atualizada.
   * @param dados O DTO com o novo nome.
   * @returns Um Observable com o PessoaDTO atualizado.
   */
  atualizar(id: string, dados: CriarAtualizarPessoaDTO): Observable<PessoaDTO> {
    return this.http.put<PessoaDTO>(`${this.API_URL}/${id}`, dados);
  }

  /**
   * Chama o endpoint DELETE /pessoas/{id} no backend (RF49).
   *
   * @param id O UUID da pessoa a ser excluída.
   * @returns Um Observable vazio (void) indicando sucesso.
   */
  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
