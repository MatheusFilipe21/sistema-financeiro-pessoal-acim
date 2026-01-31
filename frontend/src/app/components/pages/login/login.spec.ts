import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { of } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';
import { Login } from './login';
import { Autenticacao } from '../../../services/autenticacao';
import { DadosTokenJWTDTO } from '../../../dtos/autenticacao/DadosTokenJWTDTO';
import { DadosAutenticacaoDTO } from '../../../dtos/autenticacao/DadosAutenticacaoDTO';
import { Cadastro } from '../cadastro/cadastro';

/**
 * Mock do serviço de autenticação.
 */
class AutenticacaoServiceMock {
  login = jasmine.createSpy('login').and.returnValue(
    of({
      token:
        'eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2MzMwNjE3NiwiZXhwIjoxNzYzMzM0OTc2fQ.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ',
    } as DadosTokenJWTDTO)
  );

  deslogar = jasmine.createSpy('deslogar');
}

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let autenticacaoService: AutenticacaoServiceMock;

  /**
   * Configuração inicial do módulo de teste, carregando o componente standalone,
   * aplicando mocks às dependências e criando a instância do componente antes de cada teste.
   */
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        Login,
        ReactiveFormsModule,
        RouterModule.forRoot([
          { path: '', component: Login },
          { path: 'cadastro', component: Cadastro },
        ]),
      ],
      providers: [
        { provide: Autenticacao, useClass: AutenticacaoServiceMock },
        provideLocationMocks(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;

    autenticacaoService = TestBed.inject(Autenticacao) as any;

    fixture.detectChanges();
  });

  /**
   * Verifica a criação correta do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Verifica se o formulário inicia com todos os campos vazios.
   */
  it('deve inicializar o formulário com campos vazios', () => {
    const form = component.formulario;

    expect(form.get('email')?.value).toBe('');
    expect(form.get('senha')?.value).toBe('');
  });

  /**
   * Verifica se o logout é forçado ao entrar na tela.
   */
  it('deve deslogar o usuário ao inicializar o componente', () => {
    expect(autenticacaoService.deslogar).toHaveBeenCalled();
  });

  /**
   * Verifica se o formulário é considerado inválido quando campos obrigatórios não são preenchidos.
   */
  it('deve deixar o formulário inválido quando campos obrigatórios estiverem vazios', () => {
    component.formulario.setValue({ email: '', senha: '' });
    expect(component.formulario.invalid).toBeTrue();
  });

  /**
   * Testa a validação do campo de e-mail.
   */
  it('deve validar formato de e-mail', () => {
    component.formulario.get('email')?.setValue('email_invalido');
    expect(component.formulario.get('email')?.valid).toBeFalse();

    component.formulario.get('email')?.setValue('valido@email.com');
    expect(component.formulario.get('email')?.valid).toBeTrue();
  });

  /**
   * Garante que login() não é chamado quando o formulário está inválido.
   */
  it('não deve chamar o serviço login() se o formulário estiver inválido', () => {
    component.formulario.setValue({
      email: '',
      senha: '',
    });

    component.aoEnviar();

    expect(autenticacaoService.login).not.toHaveBeenCalled();
  });

  /**
   * Verifica se login() é chamado com os dados corretos quando o formulário é válido.
   */
  it('deve chamar o serviço login() com dados válidos', () => {
    const dados = {
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
    } as DadosAutenticacaoDTO;

    component.formulario.setValue(dados);

    component.aoEnviar();

    expect(autenticacaoService.login).toHaveBeenCalledOnceWith(dados);
  });

  /**
   * Verifica a alternância de visibilidade da senha.
   */
  it('deve alternar a visibilidade da senha', () => {
    const valorInicial = component.esconderSenha();
    component.alternarVisibilidadeSenha();
    expect(component.esconderSenha()).toBe(!valorInicial);

    component.alternarVisibilidadeSenha();
    expect(component.esconderSenha()).toBe(valorInicial);
  });

  /**
   * Verifica retorno vazio para controles inexistentes.
   */
  it('deve retornar vazio se o controle não existir', () => {
    expect(component.obterMensagemErro('inexistente')).toBe('');
  });

  /**
   * Verifica mensagem de erro para campo obrigatório.
   */
  it('deve retornar erro de campo obrigatório', () => {
    const control = component.formulario.get('senha');
    control?.setValue('');
    control?.markAsTouched();
    expect(component.obterMensagemErro('senha')).toBe('Este campo é obrigatório.');
  });

  /**
   * Verifica mensagem de erro para e-mail inválido.
   */
  it('deve retornar erro de e-mail inválido', () => {
    const control = component.formulario.get('email');
    control?.setValue('invalido');
    control?.markAsTouched();
    expect(component.obterMensagemErro('email')).toBe('Formato de e-mail inválido.');
  });

  /**
   * Verifica retorno vazio quando o campo é válido.
   */
  it('deve retornar vazio quando o controle é válido', () => {
    const control = component.formulario.get('email');
    control?.setValue('teste@email.com');
    control?.markAsTouched();
    expect(component.obterMensagemErro('email')).toBe('');
  });

  /**
   * Verifica se o link de cadastro aponta para a rota correta.
   */
  it('deve ter um link que redireciona para /cadastro', () => {
    const linkElement: HTMLAnchorElement = fixture.nativeElement.querySelector('#link-cadastro a');

    expect(linkElement).toBeTruthy();

    expect(linkElement.getAttribute('href')).toBe('/cadastro');
  });
});
