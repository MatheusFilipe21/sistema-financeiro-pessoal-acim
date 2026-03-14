import { describe, beforeEach, it, vi, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { of } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';

import { RecuperarSenha } from './recuperar-senha';
import { Autenticacao, Autenticacao as AutenticacaoService } from '../../../services/autenticacao';
import { Dialog as DialogService } from '../../../services/dialog';
import { Login } from '../login/login';

/**
 * Mock do serviço de autenticação.
 */
class AutenticacaoServiceMock {
  recuperarSenha = vi.fn().mockReturnValue(of(void 0));
}

/**
 * Mock do serviço de dialog.
 */
class DialogServiceMock {
  mostrarSucesso = vi.fn().mockReturnValue(of(true));
}

describe('RecuperarSenha', () => {
  let component: RecuperarSenha;
  let fixture: ComponentFixture<RecuperarSenha>;

  let autenticacaoService: AutenticacaoServiceMock;
  let dialogService: DialogServiceMock;
  let router: Router;

  /**
   * Configuração inicial do módulo de teste, carregando o componente standalone,
   * aplicando mocks às dependências e criando a instância do componente antes de cada teste.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [
        RecuperarSenha,
        ReactiveFormsModule,
        RouterModule.forRoot([
          { path: '', component: RecuperarSenha },
          { path: 'login', component: Login },
        ]),
      ],
      providers: [
        { provide: Autenticacao, useClass: AutenticacaoServiceMock },
        { provide: DialogService, useClass: DialogServiceMock },
        provideLocationMocks(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecuperarSenha);
    component = fixture.componentInstance;

    autenticacaoService = TestBed.inject(AutenticacaoService) as any;
    dialogService = TestBed.inject(DialogService) as any;
    router = TestBed.inject(Router);

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
  it('deve inicializar o formulário com campo de e-mail vazio', () => {
    expect(component.formulario.get('email')?.value).toBe('');
  });

  /**
   * Verifica se o formulário é considerado inválido quando campos obrigatórios não são preenchidos.
   */
  it('deve deixar o formulário inválido se o e-mail estiver vazio', () => {
    component.formulario.get('email')?.setValue('');
    expect(component.formulario.invalid).toBe(true);
  });

  /**
   * Testa a validação do campo de e-mail.
   */
  it('deve validar formato de e-mail', () => {
    component.formulario.get('email')?.setValue('email_invalido');
    expect(component.formulario.get('email')?.valid).toBe(false);

    component.formulario.get('email')?.setValue('valido@email.com');
    expect(component.formulario.get('email')?.valid).toBe(true);
  });

  /**
   * Garante que recuperarSenha() não é chamado quando o formulário está inválido.
   */
  it('não deve chamar o serviço recuperarSenha() se o formulário estiver inválido', () => {
    component.formulario.get('email')?.setValue('');
    component.aoEnviar();
    expect(autenticacaoService.recuperarSenha).not.toHaveBeenCalled();
  });

  /**
   * Verifica se recuperarSenha() é chamado com os dados corretos quando o formulário é válido.
   */
  it('deve chamar o serviço, exibir dialog e navegar para login ao enviar com sucesso', () => {
    const emailTeste = 'matheus@exemplo.com';
    component.formulario.get('email')?.setValue(emailTeste);

    vi.spyOn(router, 'navigate');

    component.aoEnviar();

    expect(autenticacaoService.recuperarSenha).toHaveBeenCalledWith(emailTeste);

    expect(dialogService.mostrarSucesso).toHaveBeenCalledWith(
      'E-mail Enviado',
      expect.stringContaining(emailTeste),
    );

    expect(router.navigate).toHaveBeenCalledWith(['/login']);
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
    const control = component.formulario.get('email');
    control?.setValue('');
    control?.markAsTouched();
    expect(component.obterMensagemErro('email')).toBe('Este campo é obrigatório.');
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
  it('deve ter um link que aponta para /login', () => {
    const linkElement: HTMLAnchorElement = fixture.nativeElement.querySelector('#link-login a');
    expect(linkElement).toBeTruthy();
    expect(linkElement.getAttribute('href')).toBe('/login');
  });
});
