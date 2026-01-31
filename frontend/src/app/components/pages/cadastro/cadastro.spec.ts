import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';
import { Cadastro } from './cadastro';
import { Autenticacao as AutenticacaoService } from '../../../services/autenticacao';
import { ReactiveFormsModule } from '@angular/forms';
import { UsuarioDTO } from '../../../dtos/usuario/UsuarioDTO';
import { Router, RouterModule } from '@angular/router';
import { Dialog as DialogService } from '../../../services/dialog';
import { Login } from '../../pages/login/login';

/**
 * Mock do serviço de autenticação.
 */
class AutenticacaoServiceMock {
  registrar = jasmine.createSpy('registrar').and.returnValue(
    of({
      id: '123e4567-e89b-12d3-a456-426614174000',
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
    } as UsuarioDTO)
  );
}

/**
 * Mock do serviço de dialog.
 */
class DialogServiceMock {
  mostrarSucesso = jasmine.createSpy('mostrarSucesso').and.returnValue(of(true));
}

describe('Cadastro', () => {
  let component: Cadastro;
  let fixture: ComponentFixture<Cadastro>;
  let autenticacaoService: AutenticacaoServiceMock;
  let dialogService: DialogServiceMock;
  let router: Router;

  /**
   * Configuração inicial do módulo de teste, carregando o componente standalone,
   * aplicando mocks às dependências e criando a instância do componente antes de cada teste.
   */
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        Cadastro,
        ReactiveFormsModule,
        RouterModule.forRoot([
          { path: 'login', component: Login },
          { path: 'cadastro', component: Cadastro },
        ]),
      ],
      providers: [
        { provide: AutenticacaoService, useClass: AutenticacaoServiceMock },
        { provide: DialogService, useClass: DialogServiceMock },
        provideLocationMocks(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Cadastro);
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

    expect(form.get('nome')?.value).toBe('');
    expect(form.get('email')?.value).toBe('');
    expect(form.get('senha')?.value).toBe('');
    expect(form.get('confirmarSenha')?.value).toBe('');
  });

  /**
   * Verifica se o formulário é considerado inválido quando campos obrigatórios não são preenchidos.
   */
  it('deve deixar o formulário inválido quando campos obrigatórios estiverem vazios', () => {
    component.formulario.setValue({ nome: '', email: '', senha: '', confirmarSenha: '' });
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
   * Testa os requisitos mínimos da senha.
   */
  it('deve validar requisitos mínimos da senha', () => {
    component.formulario.get('senha')?.setValue('abc');
    expect(component.formulario.get('senha')?.valid).toBeFalse();

    component.formulario.get('senha')?.setValue('Senha123');
    expect(component.formulario.get('senha')?.valid).toBeTrue();
  });

  /**
   * Garante que registrar() não é chamado quando o formulário está inválido.
   */
  it('não deve chamar o serviço registrar() se o formulário estiver inválido', () => {
    component.formulario.setValue({
      nome: '',
      email: '',
      senha: '',
      confirmarSenha: '',
    });

    component.aoEnviar();

    expect(autenticacaoService.registrar).not.toHaveBeenCalled();
  });

  /**
   * Verifica se registrar() é chamado corretamente e se ocorre o redirecionamento
   * para login após o sucesso.
   */
  it('deve chamar o serviço registrar e navegar para login ao enviar com sucesso', () => {
    const dadosFormulario = {
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
      confirmarSenha: 'Ab123456',
    };

    component.formulario.setValue(dadosFormulario);

    spyOn(router, 'navigate');

    component.aoEnviar();

    const dadosEsperadosAPI = {
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
    };

    expect(autenticacaoService.registrar).toHaveBeenCalledWith(
      jasmine.objectContaining(dadosEsperadosAPI)
    );

    expect(dialogService.mostrarSucesso).toHaveBeenCalledWith(
      'Cadastro realizado com sucesso!',
      jasmine.stringMatching(dadosFormulario.nome)
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
    expect(component.senhaEstaEmFoco()).toBeTrue();
  });

  /**
   * Verifica alteração do signal ao desfocar o campo de senha.
   */
  it('deve definir senhaEstaEmFoco como false ao desfocar', () => {
    component.senhaEstaEmFoco.set(true);
    component.aoDesfocarSenha();
    expect(component.senhaEstaEmFoco()).toBeFalse();
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
    const control = component.formulario.get('nome');
    control?.setValue('');
    control?.markAsTouched();
    expect(component.obterMensagemErro('nome')).toBe('Este campo é obrigatório.');
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
    const control = component.formulario.get('email');
    control?.setValue('teste@email.com');
    control?.markAsTouched();
    expect(component.obterMensagemErro('email')).toBe('');
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
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
      confirmarSenha: 'Ab1234567',
    });

    expect(component.formulario.invalid).toBeTrue();
    expect(component.formulario.get('confirmarSenha')?.hasError('senhasNaoConferem')).toBeTrue();
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
   * Garante que o campo confirmarSenha não é enviado para o backend.
   */
  it('não deve enviar o campo confirmarSenha para o serviço', () => {
    const dados = {
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
      confirmarSenha: 'Ab123456',
    };

    const dadosEsperadosNoBackend = {
      nome: 'Matheus Filipe do Nascimento Pereira',
      email: 'matheusfnpereira@gmail.com',
      senha: 'Ab123456',
    };

    component.formulario.setValue(dados);
    component.aoEnviar();

    const argumentoChamada = autenticacaoService.registrar.calls.mostRecent().args[0];

    expect(argumentoChamada).toEqual(jasmine.objectContaining(dadosEsperadosNoBackend));

    expect(argumentoChamada.confirmarSenha).toBeUndefined();
  });
});
