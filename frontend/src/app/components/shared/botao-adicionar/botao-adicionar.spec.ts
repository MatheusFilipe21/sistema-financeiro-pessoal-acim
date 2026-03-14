import { describe, beforeEach, it, vi, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BotaoAdicionar } from './botao-adicionar';

describe('BotaoAdicionar', () => {
  let component: BotaoAdicionar;
  let fixture: ComponentFixture<BotaoAdicionar>;

  /**
   * Configuração inicial do módulo de teste.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [BotaoAdicionar, MatButtonModule, MatIconModule, MatTooltipModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BotaoAdicionar);
    component = fixture.componentInstance;
  });

  /**
   * Verifica a criação correta do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Verifica o estado inicial (Padrão Material Design).
   * Não deve ter a classe de ajuste de rodapé.
   */
  it('deve iniciar na posição padrão (temRodape = false) e sem a classe .com-rodape', () => {
    expect(component.temRodape).toBe(false);

    const botaoEl = fixture.nativeElement.querySelector('#btn-adicionar');

    expect(botaoEl).toBeTruthy();
    expect(botaoEl.classList).not.toContain('com-rodape');
  });

  /**
   * Verifica a mudança visual quando o Input informa que existe rodapé.
   * O botão deve receber a classe CSS que altera sua posição "bottom".
   */
  it('deve aplicar classe de ajuste (.com-rodape) quando temRodape for true', async () => {
    component.temRodape = true;

    fixture.componentRef.changeDetectorRef.markForCheck();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const botaoEl = fixture.nativeElement.querySelector('#btn-adicionar');

    expect(botaoEl.classList).toContain('com-rodape');
  });

  /**
   * Teste de Integração (Output): Verifica se o clique emite o evento para o pai.
   */
  it('deve emitir o evento "adicionar" ao clicar no botão', () => {
    vi.spyOn(component.adicionar, 'emit');

    const botaoEl = fixture.nativeElement.querySelector('button');
    botaoEl.click();

    expect(component.adicionar.emit).toHaveBeenCalled();
  });

  /**
   * Teste de Acessibilidade (Teclado):
   * Verifica se a tecla INSERT, pressionada em qualquer lugar da janela (window),
   * dispara o evento de adicionar.
   */
  it('deve emitir o evento "adicionar" ao pressionar a tecla INSERT globalmente', () => {
    vi.spyOn(component.adicionar, 'emit');

    const evento = new KeyboardEvent('keydown', { key: 'Insert' });
    globalThis.dispatchEvent(evento);

    expect(component.adicionar.emit).toHaveBeenCalled();
  });

  /**
   * Teste de Segurança (Teclado):
   * Garante que outras teclas não disparem a ação.
   */
  it('NÃO deve emitir evento ao pressionar outras teclas (ex: Enter)', () => {
    vi.spyOn(component.adicionar, 'emit');

    const evento = new KeyboardEvent('keydown', { key: 'Enter' });
    globalThis.dispatchEvent(evento);

    expect(component.adicionar.emit).not.toHaveBeenCalled();
  });
});
