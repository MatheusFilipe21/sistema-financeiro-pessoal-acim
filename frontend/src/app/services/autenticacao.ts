import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DadosCadastroUsuarioDTO } from '../dtos/usuario/DadosCadastroUsuarioDTO';
import { UsuarioDTO } from '../dtos/usuario/UsuarioDTO';
import { DadosAutenticacaoDTO } from '../dtos/autenticacao/DadosAutenticacaoDTO';
import { DadosTokenJWTDTO } from '../dtos/autenticacao/DadosTokenJWTDTO';
import { DadosRedefinicaoSenhaDTO } from '../dtos/autenticacao/DadosRedefinicaoSenhaDTO';

/**
 * Serviço responsável pela comunicação com os endpoints
 * de /autenticacao no Backend.
 *
 * @author Matheus F. N. Pereira
 */
@Injectable({
  providedIn: 'root',
})
export class Autenticacao {
  /**
   * URL base para os endpoints de autenticação.
   * O 'PrefixoApiInterceptor' (do Sprint 0) adicionará o
   * prefixo '/api' (ex: http://localhost:8080/api) automaticamente.
   */
  private readonly API_URL = '/autenticacao';

  constructor(private readonly http: HttpClient) {}

  /**
   * Chama o endpoint POST /autenticacao/cadastro no backend (RF01).
   *
   * @param dados Os dados (DTO) do formulário de cadastro.
   * @returns Um Observable com o UsuarioDTO (resposta do backend).
   */
  registrar(dados: DadosCadastroUsuarioDTO): Observable<UsuarioDTO> {
    return this.http.post<UsuarioDTO>(`${this.API_URL}/cadastro`, dados);
  }

  /**
   * Chama o endpoint POST /autenticacao/login no backend (RF08).
   *
   * @param dados Os dados (DTO) do formulário de login (email e senha).
   * @returns Um Observable com o DadosTokenJWTDTO (contendo o token).
   */
  login(dados: DadosAutenticacaoDTO): Observable<DadosTokenJWTDTO> {
    return this.http.post<DadosTokenJWTDTO>(`${this.API_URL}/login`, dados);
  }

  /**
   * Chama o endpoint POST /autenticacao/recuperar-senha no backend (RF16).
   * Envia o e-mail do usuário para iniciar o processo de recuperação de senha.
   *
   * @param email O e-mail informado pelo usuário.
   * @returns Um Observable vazio (void) indicando sucesso na solicitação.
   */
  recuperarSenha(email: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/recuperar-senha`, { email });
  }

  /**
   * Envia a solicitação de redefinição de senha (RF17).
   *
   * @param dados Objeto contendo o token de recuperação e a nova senha.
   * @returns Observable vazio indicando sucesso na alteração.
   */
  redefinirSenha(dados: DadosRedefinicaoSenhaDTO): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/redefinir-senha`, dados);
  }
}
