import { describe, beforeEach, it, vi, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BreakpointObserver, BreakpointState } from '@angular/cdk/layout';
import { of } from 'rxjs';

import { ContainerFiltro } from './container-filtro';

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

describe('ContainerFiltro', () => {
  let component: ContainerFiltro;
  let fixture: ComponentFixture<ContainerFiltro>;
  let breakpointObserver: BreakpointObserverMock;

  /**
   * Configuração inicial do módulo de teste.
   */
  beforeEach(async () => {
    breakpointObserver = new BreakpointObserverMock();
    breakpointObserver.setMobile(false);

    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ContainerFiltro],
      providers: [{ provide: BreakpointObserver, useValue: breakpointObserver }],
    }).compileComponents();

    fixture = TestBed.createComponent(ContainerFiltro);
    component = fixture.componentInstance;

    component.titulo = 'Teste de Filtros';
  });

  /**
   * Verifica a criação correta do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Verifica se o título passado via @Input é renderizado no cabeçalho.
   */
  it('deve exibir o título correto configurado via @Input', () => {
    const tituloEl = fixture.nativeElement.querySelector('#titulo-pagina');
    fixture.detectChanges();
    expect(tituloEl.textContent).toContain('Teste de Filtros');
  });

  /**
   * Verifica se o layout detecta Desktop corretamente.
   * O signal 'dispositivoMovel' deve ser false.
   */
  it('deve identificar ambiente Desktop (dispositivoMovel = false)', () => {
    expect(component.dispositivoMovel()).toBe(false);
  });

  /**
   * Verifica se o botão de abrir filtros (hambúrguer/funil) NÃO é renderizado no Desktop.
   */
  it('não deve exibir o botão de toggle (abrir filtros) no modo desktop', () => {
    const btnAbrir = fixture.nativeElement.querySelector('#btn-abrir-filtros');
    expect(btnAbrir).toBeNull();
  });

  /**
   * Teste de Integração (Output): Verifica se o botão "Limpar" emite o evento.
   */
  it('deve emitir o evento "limpar" ao clicar no botão Limpar', () => {
    vi.spyOn(component.limpar, 'emit');

    const btnLimpar = fixture.nativeElement.querySelector('#btn-acao-limpar');
    expect(btnLimpar).toBeTruthy();

    btnLimpar.click();

    expect(component.limpar.emit).toHaveBeenCalled();
  });

  /**
   * Teste de Integração (Output): Verifica se o botão "Buscar" emite o evento.
   */
  it('deve emitir o evento "buscar" ao clicar no botão Buscar', async () => {
    vi.spyOn(component.buscar, 'emit');

    fixture.detectChanges();
    await fixture.whenStable();

    const btnBuscar = fixture.nativeElement.querySelector('#btn-acao-buscar');
    expect(btnBuscar).toBeTruthy();

    btnBuscar.click();

    expect(component.buscar.emit).toHaveBeenCalled();
  });

  /**
   * Cenário Mobile:
   * Verifica responsividade, gaveta e backdrop.
   */
  describe('Responsividade (Mobile)', () => {
    beforeEach(() => {
      breakpointObserver.setMobile(true);

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [ContainerFiltro],
        providers: [{ provide: BreakpointObserver, useValue: breakpointObserver }],
      });

      fixture = TestBed.createComponent(ContainerFiltro);
      component = fixture.componentInstance;
      component.titulo = 'Teste Mobile';
    });

    /**
     * Verifica se o signal reflete o estado Mobile.
     */
    it('deve identificar ambiente Mobile (dispositivoMovel = true)', () => {
      fixture.detectChanges();
      expect(component.dispositivoMovel()).toBe(true);
    });

    /**
     * Verifica se o botão de toggle aparece no mobile.
     */
    it('deve exibir o botão de abrir filtros no modo mobile', () => {
      fixture.detectChanges();
      const btnAbrir = fixture.nativeElement.querySelector('#btn-abrir-filtros');
      expect(btnAbrir).toBeTruthy();
    });

    /**
     * Teste de Interface: Verifica a abertura da gaveta lateral.
     */
    it('deve abrir a gaveta (adicionar classe .aberto) ao clicar no toggle', async () => {
      fixture.detectChanges();

      const btnAbrir = fixture.nativeElement.querySelector('#btn-abrir-filtros');

      expect(component.filtroAberto).toBe(false);

      btnAbrir.click();

      fixture.componentRef.changeDetectorRef.markForCheck();
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      expect(component.filtroAberto).toBe(true);

      const areaFiltros = fixture.nativeElement.querySelector('#area-conteudo-filtros');
      expect(areaFiltros.classList).toContain('aberto');
    });

    /**
     * Teste de Interface: Verifica o fechamento pelo botão "X" interno.
     */
    it('deve fechar a gaveta ao clicar no botão de fechar interno', async () => {
      fixture.detectChanges();
      component.alternarFiltro();

      fixture.componentRef.changeDetectorRef.markForCheck();
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const btnFechar = fixture.nativeElement.querySelector('#btn-fechar-painel');
      expect(btnFechar).toBeTruthy();

      btnFechar.click();

      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      expect(component.filtroAberto).toBe(false);
    });

    /**
     * Teste de Interface: Verifica o fechamento pelo Backdrop (fundo escuro).
     */
    it('deve fechar a gaveta ao clicar no backdrop (fundo escuro)', async () => {
      fixture.detectChanges();
      component.alternarFiltro();

      fixture.componentRef.changeDetectorRef.markForCheck();
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const backdrop = fixture.nativeElement.querySelector('#fundo-escuro-modal');
      expect(backdrop).toBeTruthy();

      backdrop.click();

      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      expect(component.filtroAberto).toBe(false);
    });

    /**
     * Regra de Negócio Visual:
     * Ao buscar no mobile, a gaveta deve fechar automaticamente para exibir os resultados.
     */
    it('deve fechar a gaveta automaticamente após clicar em Buscar no mobile', async () => {
      vi.spyOn(component.buscar, 'emit');
      fixture.detectChanges();

      component.alternarFiltro();

      fixture.componentRef.changeDetectorRef.markForCheck();
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const btnBuscar = fixture.nativeElement.querySelector('#btn-acao-buscar');
      btnBuscar.click();

      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      expect(component.buscar.emit).toHaveBeenCalled();
      expect(component.filtroAberto).toBe(false);
    });
  });
});
