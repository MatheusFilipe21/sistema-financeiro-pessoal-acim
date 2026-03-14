import { describe, beforeEach, it, vi, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';
import { RedefinirSenha } from './redefinir-senha';
import { Autenticacao as AutenticacaoService } from '../../../services/autenticacao';
import { Dialog as DialogService } from '../../../services/dialog';
import { Login } from '../login/login';
import { DadosRedefinicaoSenhaDTO } from '../../../dtos/autenticacao/DadosRedefinicaoSenhaDTO';

/**
 * Mock do serviço de autenticação.
 */
class AutenticacaoServiceMock {
  redefinirSenha = vi.fn().mockReturnValue(of(void 0));
}

/**
 * Mock do serviço de dialog.
 */
class DialogServiceMock {
  mostrarSucesso = vi.fn().mockReturnValue(of(true));
}

/**
 * Mock para ActivatedRoute simulando o token na URL.
 */
const activatedRouteMock = {
  snapshot: {
    queryParamMap: {
      get: (key: string) => (key === 'token' ? 'token-jwt-valido-mock' : null),
    },
  },
};

describe('RedefinirSenha', () => {
  let component: RedefinirSenha;
  let fixture: ComponentFixture<RedefinirSenha>;
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
        RedefinirSenha,
        ReactiveFormsModule,
        RouterModule.forRoot([
          { path: '', component: RedefinirSenha },
          { path: 'login', component: Login },
        ]),
      ],
      providers: [
        { provide: AutenticacaoService, useClass: AutenticacaoServiceMock },
        { provide: DialogService, useClass: DialogServiceMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        provideLocationMocks(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RedefinirSenha);
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
  it('deve inicializar o formulário com campos vazios', () => {
    const form = component.formulario;
    expect(form.get('senha')?.value).toBe('');
    expect(form.get('confirmarSenha')?.value).toBe('');
  });

  /**
   * Verifica se o formulário é considerado inválido quando campos obrigatórios não são preenchidos.
   */
  it('deve deixar o formulário inválido quando campos obrigatórios estiverem vazios', () => {
    component.formulario.setValue({ senha: '', confirmarSenha: '' });
    expect(component.formulario.invalid).toBe(true);
  });

  /**
   * Testa os requisitos mínimos da senha.
   */
  it('deve validar requisitos mínimos da senha', () => {
    component.formulario.get('senha')?.setValue('abc');
    expect(component.formulario.get('senha')?.valid).toBe(false);

    component.formulario.get('senha')?.setValue('Senha123');
    expect(component.formulario.get('senha')?.valid).toBe(true);
  });

  /**
   * Garante que redefinirSenha() não é chamado quando o formulário está inválido.
   */
  it('não deve chamar o serviço redefinirSenha() se o formulário estiver inválido', () => {
    component.formulario.setValue({
      senha: '',
      confirmarSenha: '',
    });

    component.aoEnviar();

    expect(autenticacaoService.redefinirSenha).not.toHaveBeenCalled();
  });

  /**
   * Verifica se o serviço é chamado corretamente e o fluxo de sucesso é executado.
   */
  it('deve chamar o serviço, exibir dialog e navegar para login ao enviar com sucesso', () => {
    const novaSenha = 'NovaSenha123';

    component.formulario.setValue({
      senha: novaSenha,
      confirmarSenha: novaSenha,
    });

    vi.spyOn(router, 'navigate');

    component.aoEnviar();

    const dtoEsperado: DadosRedefinicaoSenhaDTO = {
      token: 'token-jwt-valido-mock',
      senha: novaSenha,
    };

    expect(autenticacaoService.redefinirSenha).toHaveBeenCalledWith(dtoEsperado);

    expect(dialogService.mostrarSucesso).toHaveBeenCalledWith(
      'Senha Alterada',
      expect.stringContaining('sucesso'),
    );

    expect(router.navigate).toHaveBeenCalledWith(['/login']);
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
   * Verifica alteração do signal ao focar no campo de senha.
   */
  it('deve definir senhaEstaEmFoco como true ao focar', () => {
    component.senhaEstaEmFoco.set(false);
    component.aoFocarSenha();
    expect(component.senhaEstaEmFoco()).toBe(true);
  });

  /**
   * Verifica alteração do signal ao desfocar o campo de senha.
   */
  it('deve definir senhaEstaEmFoco como false ao desfocar', () => {
    component.senhaEstaEmFoco.set(true);
    component.aoDesfocarSenha();
    expect(component.senhaEstaEmFoco()).toBe(false);
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
   * Verifica mensagem de erro para senha inválida.
   */
  it('deve retornar erro de senha fraca', () => {
    const control = component.formulario.get('senha');
    control?.setValue('123');
    control?.markAsTouched();
    expect(component.obterMensagemErro('senha')).toBe('A senha não atende aos requisitos mínimos.');
  });

  /**
   * Verifica retorno vazio quando o campo é válido.
   */
  it('deve retornar vazio quando o controle é válido', () => {
    const control = component.formulario.get('senha');
    control?.setValue('SenhaForte1');
    control?.markAsTouched();
    expect(component.obterMensagemErro('senha')).toBe('');
  });

  /**
   * Verifica se o link de login aponta para a rota correta.
   */
  it('deve ter um link que redireciona para /login', () => {
    const linkElement: HTMLAnchorElement = fixture.nativeElement.querySelector('#link-login a');

    expect(linkElement).toBeTruthy();

    expect(linkElement.getAttribute('href')).toBe('/login');
  });

  /**
   * Verifica se o formulário é inválido quando as senhas não coincidem.
   */
  it('deve invalidar o formulário se a confirmação de senha for diferente', () => {
    component.formulario.setValue({
      senha: 'Senha123',
      confirmarSenha: 'Senha1234',
    });

    expect(component.formulario.invalid).toBe(true);
    expect(component.formulario.get('confirmarSenha')?.hasError('senhasNaoConferem')).toBe(true);
    expect(component.obterMensagemErro('confirmarSenha')).toBe('As senhas não conferem.');
  });

  /**
   * Verifica a alternância de visibilidade da confirmação de senha.
   */
  it('deve alternar a visibilidade da confirmação de senha', () => {
    const valorInicial = component.esconderConfirmarSenha();
    component.alternarVisibilidadeConfirmarSenha();
    expect(component.esconderConfirmarSenha()).toBe(!valorInicial);

    component.alternarVisibilidadeConfirmarSenha();
    expect(component.esconderConfirmarSenha()).toBe(valorInicial);
  });

  /**
   * Verifica se o usuário é redirecionado para o login caso o token não esteja presente na URL.
   */
  it('deve redirecionar para o login se o token estiver ausente no ngOnInit', () => {
    vi.spyOn(router, 'navigate');

    vi.spyOn(TestBed.inject(ActivatedRoute).snapshot.queryParamMap, 'get').mockReturnValue(null);

    component.ngOnInit();

    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
