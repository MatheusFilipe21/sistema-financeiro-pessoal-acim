import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { CardBase } from './card-base';

/**
 * Componente Host auxiliar para testar a projeção de conteúdo (ng-content).
 * Simula um componente pai utilizando o CardBase.
 */
@Component({
  template: `
    <app-card-base [identificador]="99" titulo="Título Host">
      <div id="conteudo-teste-projecao">Conteúdo Projetado</div>
    </app-card-base>
  `,
  standalone: true,
  imports: [CardBase],
})
class TestHostComponent {}

describe('CardBase', () => {
  let component: CardBase;
  let fixture: ComponentFixture<CardBase>;

  /**
   * Configuração inicial do módulo de teste.
   */
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardBase, TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CardBase);
    component = fixture.componentInstance;

    component.identificador = 1;
    component.titulo = 'Título Inicial';

    fixture.detectChanges();
  });

  /**
   * Verifica a criação correta do componente.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Teste de Renderização:
   * Verifica se os IDs são gerados corretamente concatenando o identificador.
   */
  it('deve gerar IDs dinâmicos baseados no input "identificador"', () => {
    component.identificador = 50;
    fixture.detectChanges();

    const cardContainer = fixture.nativeElement.querySelector('#card-base-50');
    const cabecalho = fixture.nativeElement.querySelector('#cabecalho-card-50');

    expect(cardContainer).toBeTruthy();
    expect(cabecalho).toBeTruthy();
  });

  /**
   * Teste de Renderização:
   * Verifica se Título e Subtítulo são exibidos conforme os Inputs.
   */
  it('deve exibir o título e subtítulo corretamente', () => {
    component.identificador = 10;
    component.titulo = 'João Silva';
    component.subtitulo = 'Desenvolvedor';
    fixture.detectChanges();

    const tituloEl = fixture.nativeElement.querySelector('#titulo-card-10');
    const subtituloEl = fixture.nativeElement.querySelector('#subtitulo-card-10');

    expect(tituloEl.textContent).toContain('João Silva');
    expect(subtituloEl.textContent).toContain('Desenvolvedor');
  });

  /**
   * Regra de Negócio Visual:
   * Se 'avatarUrl' for fornecido, deve renderizar a IMAGEM e não o ÍCONE.
   */
  it('deve renderizar a imagem (img) quando avatarUrl for fornecido', () => {
    component.identificador = 20;
    component.avatarUrl = 'http://fake-url.com/foto.jpg';
    component.icone = 'person';
    fixture.detectChanges();

    const imgAvatar = fixture.nativeElement.querySelector('#img-avatar-20');
    const iconeAvatar = fixture.nativeElement.querySelector('#icone-avatar-20');

    expect(imgAvatar).toBeTruthy();
    expect(imgAvatar.src).toContain('foto.jpg');

    expect(iconeAvatar).toBeNull();
  });

  /**
   * Regra de Negócio Visual:
   * Se apenas 'icone' for fornecido, deve renderizar o MAT-ICON e não a IMAGEM.
   */
  it('deve renderizar o ícone (mat-icon) quando apenas icone for fornecido', () => {
    component.identificador = 30;
    component.icone = 'account_balance';
    component.avatarUrl = undefined;
    fixture.detectChanges();

    const iconeAvatar = fixture.nativeElement.querySelector('#icone-avatar-30');
    const imgAvatar = fixture.nativeElement.querySelector('#img-avatar-30');

    expect(iconeAvatar).toBeTruthy();
    expect(iconeAvatar.textContent).toContain('account_balance');

    expect(imgAvatar).toBeNull();
  });

  /**
   * Teste de Integração (Menu Material):
   * Verifica o fluxo de abrir o menu e clicar em Editar.
   */
  it('deve emitir evento "editar" ao clicar na opção do menu', () => {
    spyOn(component.editar, 'emit');
    component.identificador = 100;
    fixture.detectChanges();

    const btnMenu = fixture.nativeElement.querySelector('#btn-abrir-acoes-100');
    btnMenu.click();
    fixture.detectChanges();

    const btnEditar = document.getElementById('btn-acao-editar-100');

    expect(btnEditar).toBeTruthy();
    btnEditar?.click();

    expect(component.editar.emit).toHaveBeenCalled();
  });

  /**
   * Teste de Integração (Menu Material):
   * Verifica o fluxo de abrir o menu e clicar em Excluir.
   */
  it('deve emitir evento "excluir" ao clicar na opção do menu', () => {
    spyOn(component.excluir, 'emit');
    component.identificador = 200;
    fixture.detectChanges();

    const btnMenu = fixture.nativeElement.querySelector('#btn-abrir-acoes-200');
    btnMenu.click();
    fixture.detectChanges();

    const btnExcluir = document.getElementById('btn-acao-excluir-200');

    expect(btnExcluir).toBeTruthy();
    btnExcluir?.click();

    expect(component.excluir.emit).toHaveBeenCalled();
  });

  /**
   * Teste de Projeção de Conteúdo (Content Projection):
   * Verifica se o conteúdo HTML inserido pelo pai é renderizado dentro do slot ng-content.
   */
  it('deve projetar o conteúdo interno (ng-content) corretamente', () => {
    const hostFixture = TestBed.createComponent(TestHostComponent);
    hostFixture.detectChanges();

    const conteudoProjetado = hostFixture.debugElement.query(By.css('#conteudo-teste-projecao'));

    expect(conteudoProjetado).toBeTruthy();
    expect(conteudoProjetado.nativeElement.textContent).toContain('Conteúdo Projetado');
  });
});
