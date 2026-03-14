import { describe, beforeEach, it, vi, expect } from 'vitest';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { FormatacaoDecimal } from './formatacao-decimal';

/**
 * Componente Dummy (Falso) para testar a diretiva.
 * Possui dois inputs:
 * 1. Com FormControl (Caminho feliz).
 * 2. Sem FormControl (Para testar a proteção contra nulos).
 */
@Component({
  template: `
    <input id="input-com-form" type="number" [formControl]="controle" appFormatacaoDecimal />

    <input id="input-sem-form" type="number" appFormatacaoDecimal />
  `,
  imports: [ReactiveFormsModule, FormatacaoDecimal],
  standalone: true,
})
class TestComponent {
  controle = new FormControl('');
}

/**
 * Suite de testes para a diretiva FormatacaoDecimalDirective.
 * Verifica a formatação de valores numéricos e a robustez do código.
 * @author Matheus F. N. Pereira
 */
describe('FormatacaoDecimalDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let component: TestComponent;
  let inputComForm: HTMLInputElement;
  let inputSemForm: HTMLInputElement;

  /**
   * Configuração inicial do ambiente de teste.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [TestComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;

    inputComForm = fixture.debugElement.query(By.css('#input-com-form')).nativeElement;
    inputSemForm = fixture.debugElement.query(By.css('#input-sem-form')).nativeElement;

    fixture.detectChanges();
  });

  /**
   * Auxiliar para disparar evento de teclado e permitir verificar o preventDefault
   */
  function dispararKeyDown(key: string): KeyboardEvent {
    const event = new KeyboardEvent('keydown', { key: key, cancelable: true });
    vi.spyOn(event, 'preventDefault');
    inputComForm.dispatchEvent(event);
    return event;
  }

  /**
   * Teste de criação básica.
   */
  it('deve criar uma instância do componente de teste', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Cenário Principal: Verifica se formata '100' para '100.00'.
   */
  it('deve formatar o valor para 2 casas decimais ao perder o foco (blur)', () => {
    inputComForm.value = '100';
    inputComForm.dispatchEvent(new Event('input'));
    component.controle.setValue('100');

    inputComForm.dispatchEvent(new Event('blur'));
    fixture.detectChanges();

    expect(component.controle.value).toBe('100.00');
    expect(inputComForm.value).toBe('100.00');
  });

  /**
   * Cenário: Verifica arredondamento ou preenchimento de zeros (ex: 10.5 -> 10.50).
   */
  it('deve formatar valores com 1 casa decimal para 2 casas (ex: 10.5 -> 10.50)', () => {
    component.controle.setValue('10.5');
    fixture.detectChanges();

    inputComForm.dispatchEvent(new Event('blur'));
    fixture.detectChanges();

    expect(component.controle.value).toBe('10.50');
  });

  /**
   * Cenário: Valor vazio não deve quebrar nem adicionar zeros (ex: '' -> '').
   */
  it('não deve alterar o valor se ele estiver vazio', () => {
    component.controle.setValue('');
    inputComForm.dispatchEvent(new Event('blur'));
    expect(component.controle.value).toBe('');
  });

  /**
   * Cenário de Borda (A linha que faltava):
   * Verifica se o código aborta silenciosamente quando não há FormControl vinculado.
   * Testa a linha: if (!this.ngControl?.control) return;
   */
  it('não deve lançar erro se a diretiva for usada em um input sem FormControl', () => {
    inputSemForm.value = '123';

    const acaoBlur = () => inputSemForm.dispatchEvent(new Event('blur'));

    expect(acaoBlur).not.toThrow();
  });

  /**
   * Cenário de Truncamento.
   */
  it('deve TRUNCAR (cortar) casas decimais extras em vez de arredondar', () => {
    component.controle.setValue('10.559');

    inputComForm.dispatchEvent(new Event('blur'));
    fixture.detectChanges();

    expect(component.controle.value).toBe('10.55');
    expect(inputComForm.value).toBe('10.55');
  });

  /**
   * Cenário: Limite em tempo real (Input).
   */
  it('deve impedir a digitação da terceira casa decimal no evento input', () => {
    inputComForm.value = '10.123';

    inputComForm.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(inputComForm.value).toBe('10.12');
  });

  it('deve adicionar zeros à direita se faltar casas decimais (10.5 -> 10.50)', () => {
    component.controle.setValue('10.5');
    inputComForm.dispatchEvent(new Event('blur'));
    expect(component.controle.value).toBe('10.50');
  });

  /**
   * Cenário: Números Negativos
   */
  it('deve impedir a digitação de números negativos', () => {
    inputComForm.value = '-150';

    inputComForm.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(inputComForm.value).toBe('150');
  });

  describe('Bloqueio de Teclas (Keydown)', () => {
    it('deve bloquear (preventDefault) a tecla de menos "-"', () => {
      const event = dispararKeyDown('-');
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('deve bloquear (preventDefault) a tecla "Minus" (teclados antigos)', () => {
      const event = dispararKeyDown('Minus');
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('deve bloquear (preventDefault) a tecla "e" (exponencial)', () => {
      const event = dispararKeyDown('e');
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('não deve bloquear teclas numéricas permitidas (ex: "5")', () => {
      const event = dispararKeyDown('5');
      expect(event.preventDefault).not.toHaveBeenCalled();
    });

    it('não deve bloquear teclas de navegação ou controle (ex: "Backspace")', () => {
      const event = dispararKeyDown('Backspace');
      expect(event.preventDefault).not.toHaveBeenCalled();
    });
  });
});
