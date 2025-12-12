import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { BreakpointObserver, BreakpointState } from '@angular/cdk/layout';
import { of } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';

import { Dashboard } from './dashboard';
import { Autenticacao as AutenticacaoService } from '../../services/autenticacao';
import { Login } from '../login/login';

/**
 * Mock do serviço de autenticação.
 */
class AutenticacaoServiceMock {
  deslogar = jasmine.createSpy('deslogar');
}

/**
 * Mock do BreakpointObserver para simular tamanhos de tela.
 */
class BreakpointObserverMock {
  private matches = false;

  /**
   * Configura se o mock deve simular um dispositivo móvel ou desktop.
   * @param isMobile Se true, simula Handset/XSmall. Se false, simula Desktop.
   */
  setMobile(isMobile: boolean) {
    this.matches = isMobile;
  }

  observe() {
    return of({ matches: this.matches } as BreakpointState);
  }
}

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let autenticacaoService: AutenticacaoServiceMock;
  let breakpointObserver: BreakpointObserverMock;

  /**
   * Configuração inicial do módulo de teste, carregando o componente standalone,
   * aplicando mocks às dependências e criando a instância do componente antes de cada teste.
   */
  beforeEach(async () => {
    autenticacaoService = new AutenticacaoServiceMock();
    breakpointObserver = new BreakpointObserverMock();

    breakpointObserver.setMobile(false);

    await TestBed.configureTestingModule({
      imports: [
        Dashboard,
        RouterModule.forRoot([
          { path: '', component: Login },
          { path: 'dashboard', component: Dashboard },
        ]),
      ],
      providers: [
        { provide: AutenticacaoService, useValue: autenticacaoService },
        { provide: BreakpointObserver, useValue: breakpointObserver },
        provideLocationMocks(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  /**
   * Verifica a criação correta do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Verifica se o layout detecta Desktop corretamente.
   * O signal 'dispositivoMovel' deve ser false.
   */
  it('deve identificar ambiente Desktop (dispositivoMovel = false)', () => {
    expect(component.dispositivoMovel()).toBeFalse();
  });

  /**
   * Verifica se o método deslogar() chama o serviço de autenticação corretamente.
   */
  it('deve chamar o serviço deslogar() ao acionar o logout', () => {
    component.deslogar();
    expect(autenticacaoService.deslogar).toHaveBeenCalled();
  });

  /**
   * Teste de Integração (DOM): Verifica se o botão de logout no template aciona o método.
   */
  it('deve chamar deslogar() ao clicar no botão de sair na toolbar', () => {
    const btnSair = fixture.nativeElement.querySelector('#btn-sair');

    expect(btnSair).toBeTruthy();

    btnSair.click();

    expect(autenticacaoService.deslogar).toHaveBeenCalled();
  });

  /**
   * Teste de Integração (DOM): Verifica se o título do sistema está correto.
   */
  it('deve exibir o título correto do sistema na toolbar', () => {
    const titulo = fixture.nativeElement.querySelector('#titulo-sistema');
    expect(titulo.textContent).toContain('Sistema Financeiro Pessoal - ACIM');
  });

  /**
   * Teste de Integração (DOM): Verifica se o menu lateral contém o link "Início".
   */
  it('deve renderizar o link de "Início" no menu lateral', () => {
    const linkInicio = fixture.nativeElement.querySelector('#link-inicio');
    const textoInicio = fixture.nativeElement.querySelector('#txt-inicio');

    expect(linkInicio).toBeTruthy();
    expect(textoInicio.textContent).toContain('Início');
  });

  /**
   * Cenário Mobile:
   * Verifica se o layout se adapta quando o BreakpointObserver emite true.
   */
  describe('Responsividade (Mobile)', () => {
    beforeEach(() => {
      breakpointObserver.setMobile(true);

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [Dashboard, RouterModule.forRoot([])],
        providers: [
          { provide: AutenticacaoService, useValue: autenticacaoService },
          { provide: BreakpointObserver, useValue: breakpointObserver },
        ],
      });
      fixture = TestBed.createComponent(Dashboard);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    /**
     * Verifica se o signal reflete o estado Mobile.
     */
    it('deve identificar ambiente Mobile (dispositivoMovel = true)', () => {
      expect(component.dispositivoMovel()).toBeTrue();
    });

    /**
     * Verifica se o botão de toggle (hambúrguer) aparece apenas no mobile.
     */
    it('deve exibir o botão de toggle menu no modo mobile', () => {
      const btnToggle = fixture.nativeElement.querySelector('#btn-alternar-menu');
      expect(btnToggle).toBeTruthy();
    });
  });
});
