import { describe, beforeEach, it, vi, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlternadorVisualizacao } from './alternador-visualizacao';

/**
 * Testes unitários para o componente AlternadorVisualizacao.
 *
 * @author Matheus F. N. Pereira
 */
describe('AlternadorVisualizacao', () => {
  let component: AlternadorVisualizacao;
  let fixture: ComponentFixture<AlternadorVisualizacao>;

  /**
   * Configuração inicial do módulo de teste.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [AlternadorVisualizacao],
    }).compileComponents();

    fixture = TestBed.createComponent(AlternadorVisualizacao);
    component = fixture.componentInstance;
  });

  /**
   * Verifica a criação correta do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Verifica o estado inicial padrão (Cards).
   * O botão deve sugerir ir para "Lista" (table_view).
   */
  it('deve iniciar no modo Cards (emCards = true) e exibir botão para Lista', () => {
    expect(component.emCards).toBe(true);

    const btn = fixture.nativeElement.querySelector('#btn-alternar-visao');
    const icone = btn.querySelector('mat-icon');

    fixture.detectChanges();

    expect(btn.textContent).toContain('Visualizar Lista');
    expect(icone.textContent).toContain('table_view');
  });

  /**
   * Verifica a renderização quando o Input é alterado para Lista.
   * O botão deve sugerir ir para "Cards" (grid_view).
   */
  it('deve atualizar visual ao receber modo Lista (emCards = false)', async () => {
    component.emCards = false;

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const btn = fixture.nativeElement.querySelector('#btn-alternar-visao');
    const icone = btn.querySelector('mat-icon');

    expect(btn.textContent).toContain('Visualizar Cards');
    expect(icone.textContent).toContain('grid_view');
  });

  /**
   * Verifica a lógica do método alternar().
   * Deve inverter o booleano e emitir o evento.
   */
  it('deve inverter o estado e emitir evento ao chamar alternar()', () => {
    vi.spyOn(component.emCardsChange, 'emit');

    component.emCards = true;

    component.alternar();

    expect(component.emCards).toBe(false);
    expect(component.emCardsChange.emit).toHaveBeenCalledWith(false);

    component.alternar();

    expect(component.emCards).toBe(true);
    expect(component.emCardsChange.emit).toHaveBeenCalledWith(true);
  });

  /**
   * Teste de Integração (DOM): Verifica o clique no botão.
   */
  it('deve chamar alternar() ao clicar no botão', () => {
    vi.spyOn(component, 'alternar');
    vi.spyOn(component.emCardsChange, 'emit');

    const btn = fixture.nativeElement.querySelector('#btn-alternar-visao');
    btn.click();
    fixture.detectChanges();

    expect(component.alternar).toHaveBeenCalled();
    expect(component.emCards).toBe(false);
  });
});
