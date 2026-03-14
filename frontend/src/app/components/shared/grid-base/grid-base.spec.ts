import { describe, beforeEach, it, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { GridBase } from './grid-base';

/**
 * Componente de teste para validar a projeção de conteúdo (ng-content).
 */
@Component({
  template: `
    <app-grid-base [identificador]="'grid-teste'">
      <div id="item-teste">Conteúdo Projetado</div>
    </app-grid-base>
  `,
  standalone: true,
  imports: [GridBase],
})
class TestHostComponent {}

describe('GridBase', () => {
  let component: GridBase;
  let fixture: ComponentFixture<GridBase>;

  /**
   * Configuração inicial do módulo de teste.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [GridBase, TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(GridBase);
    component = fixture.componentInstance;
  });

  /**
   * Verifica a criação do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Teste de Input: Verifica se o ID padrão é aplicado quando nenhum é fornecido.
   */
  it('deve usar o ID padrão "grid-principal" se nenhum for fornecido', async () => {
    const elementoGrid = fixture.nativeElement.querySelector('.grid-base');

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(elementoGrid.id).toBe('grid-principal');
  });

  /**
   * Teste de Input: Verifica se o ID personalizado é aplicado corretamente.
   */
  it('deve aplicar o ID personalizado fornecido via @Input', () => {
    component.identificador = 'minha-grid-customizada';
    fixture.detectChanges();

    const elementoGrid = fixture.nativeElement.querySelector('.grid-base');
    expect(elementoGrid.id).toBe('minha-grid-customizada');
  });

  /**
   * Teste de Integração (Content Projection):
   * Verifica se o conteúdo inserido dentro do componente é renderizado corretamente.
   */
  it('deve projetar o conteúdo (ng-content) corretamente', () => {
    const hostFixture = TestBed.createComponent(TestHostComponent);
    hostFixture.detectChanges();

    const itemProjetado = hostFixture.debugElement.query(By.css('#item-teste'));

    expect(itemProjetado).toBeTruthy();
    expect(itemProjetado.nativeElement.textContent).toContain('Conteúdo Projetado');
  });
});
