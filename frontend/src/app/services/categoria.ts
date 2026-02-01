import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CategoriaDTO } from '../dtos/categoria/CategoriaDTO';
import { CriarAtualizarCategoriaDTO } from '../dtos/categoria/CriarAtualizarCategoriaDTO';

/**
 * Serviço responsável pela comunicação com os endpoints
 * de /categorias no Backend.
 *
 * @author Matheus F. N. Pereira
 */
@Injectable({
  providedIn: 'root',
})
export class Categoria {
  /**
   * URL base para os endpoints de categorias.
   * O 'PrefixoApiInterceptor' adicionará '/api' automaticamente.
   */
  private readonly API_URL = '/categorias';

  private readonly http = inject(HttpClient);

  /**
   * Chama o endpoint GET /categorias no backend.
   *
   * @returns Um Observable contendo a lista de CategoriaDTO.
   */
  listar(): Observable<CategoriaDTO[]> {
    return this.http.get<CategoriaDTO[]>(this.API_URL);
  }

  /**
   * Chama o endpoint POST /categorias no backend.
   *
   * @param dados O DTO contendo os dados da nova categoria.
   * @returns Um Observable com o CategoriaDTO criado.
   */
  cadastrar(dados: CriarAtualizarCategoriaDTO): Observable<CategoriaDTO> {
    return this.http.post<CategoriaDTO>(this.API_URL, dados);
  }

  /**
   * Chama o endpoint PUT /categorias/{id} no backend.
   *
   * @param id O UUID da categoria a ser atualizada.
   * @param dados O DTO com os novos dados.
   * @returns Um Observable com o CategoriaDTO atualizado.
   */
  atualizar(id: string, dados: CriarAtualizarCategoriaDTO): Observable<CategoriaDTO> {
    return this.http.put<CategoriaDTO>(`${this.API_URL}/${id}`, dados);
  }

  /**
   * Chama o endpoint DELETE /categorias/{id} no backend.
   *
   * @param id O UUID da categoria a ser excluída.
   * @returns Um Observable vazio (void) indicando sucesso.
   */
  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
