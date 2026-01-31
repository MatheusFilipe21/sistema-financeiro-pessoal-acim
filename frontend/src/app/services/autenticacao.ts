import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { DadosCadastroUsuarioDTO } from '../dtos/usuario/DadosCadastroUsuarioDTO';
import { UsuarioDTO } from '../dtos/usuario/UsuarioDTO';
import { DadosAutenticacaoDTO } from '../dtos/autenticacao/DadosAutenticacaoDTO';
import { DadosTokenJWTDTO } from '../dtos/autenticacao/DadosTokenJWTDTO';
import { DadosRedefinicaoSenhaDTO } from '../dtos/autenticacao/DadosRedefinicaoSenhaDTO';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { Token as TokenService } from './token';

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

  /**
   * Chave utilizada para persistir o token JWT no `LocalStorage` do navegador.
   */
  private readonly CHAVE_TOKEN = 'sfp-acim-token-jwt';

  /**
   * Signal que guarda o estado reativo da sessão.
   * - `true`: Usuário autenticado e token válido.
   * - `false`: Usuário deslogado ou token expirado.
   */
  public usuarioEstaLogado = signal(false);

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);

  constructor() {
    this.usuarioEstaLogado.set(this.possuiTokenValido());
  }

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
   * Utiliza o operador `tap` para, em caso de sucesso, persistir automaticamente
   * a sessão do usuário antes de devolver a resposta ao componente.
   *
   * @param dados Os dados (DTO) do formulário de login (email e senha).
   * @returns Um Observable com o DadosTokenJWTDTO (contendo o token gerado).
   */
  login(dados: DadosAutenticacaoDTO): Observable<DadosTokenJWTDTO> {
    return this.http.post<DadosTokenJWTDTO>(`${this.API_URL}/login`, dados).pipe(
      tap((resposta) => {
        this.logar(resposta.token);
      })
    );
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

  /**
   * Persiste a sessão do usuário no navegador e atualiza o estado da aplicação.
   *
   * 1. Armazena o token JWT no LocalStorage.
   * 2. Atualiza o signal `usuarioEstaLogado` para refletir o novo estado na UI.
   * 3. Navega imperativamente para a rota segura (`/dashboard`).
   *
   * @param token O token JWT recebido da API após um login bem-sucedido.
   */
  public logar(token: string): void {
    this.tokenService.salvar(token);
    this.usuarioEstaLogado.set(true);
    this.router.navigate(['/dashboard']);
  }

  /**
   * Encerra a sessão do usuário (RF26).
   *
   * Realiza a limpeza completa das credenciais locais, atualiza o estado
   * de autenticação para falso e força o redirecionamento para a tela de Login
   * para prevenir acesso não autorizado via histórico do navegador.
   */
  public deslogar(): void {
    this.tokenService.remover();
    this.usuarioEstaLogado.set(false);
    this.router.navigate(['/login']);
  }

  /**
   * Recupera o token JWT armazenado cru (raw string).
   *
   * Utilizado principalmente por Interceptors para injetar o cabeçalho
   * `Authorization: Bearer ...` em requisições HTTP autenticadas.
   *
   * @returns O token JWT em formato string ou `null` se não houver sessão.
   */
  public obterToken(): string | null {
    return this.tokenService.obter();
  }

  /**
   * Valida a integridade e validade temporal do token armazenado.
   *
   * Utiliza a biblioteca `jwt-decode` para ler a claim `exp` (expiration) do payload.
   *
   * Regra de Validação:
   * - Retorna `false` se não houver token.
   * - Retorna `false` se o token estiver malformado (erro no decode).
   * - Retorna `true` APENAS se a data de expiração for maior que o timestamp atual.
   *
   * @returns Booleano indicando se o usuário possui uma sessão ativa e válida.
   */
  public possuiTokenValido(): boolean {
    const token = this.obterToken();

    if (!token) {
      return false;
    }

    try {
      const tokenDecodificado: any = jwtDecode(token);
      // O JWT exp é em segundos, o Date.now() é em milissegundos.
      const dataExpiracao = tokenDecodificado.exp * 1000;
      const agora = Date.now();

      return dataExpiracao > agora;
    } catch (error_) {
      console.warn('Token inválido ou malformado encontrado ao verificar sessão:', error_);
      return false;
    }
  }
}
