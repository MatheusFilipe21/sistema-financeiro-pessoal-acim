import { describe, beforeEach, it, expect } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MensagemDialog, DadosMensagem } from './mensagem-dialog';

/**
 * Testes unitários para o componente {@link MensagemDialog}.
 *
 * @author Matheus F. N. Pereira
 */
describe('MensagemDialog', () => {
  let component: MensagemDialog;
  let fixture: ComponentFixture<MensagemDialog>;
  let el: HTMLElement;

  /**
   * Mock de mensagem de sucesso.
   */
  const mockDadosSucesso: DadosMensagem = {
    tipo: 'sucesso',
    titulo: 'Sucesso',
    mensagem: 'Operação OK',
    textoBotao: 'OK',
  };

  /**
   * Mock de mensagem de aviso.
   */
  const mockDadosAviso: DadosMensagem = {
    tipo: 'aviso',
    titulo: 'Atenção',
    mensagem: 'Cuidado!',
    textoBotao: 'Entendi',
  };

  /**
   * Mock de mensagem de informação.
   */
  const mockDadosInfo: DadosMensagem = {
    tipo: 'info',
    titulo: 'Informação',
    mensagem: 'Saiba mais.',
    textoBotao: 'Fechar',
  };

  /**
   * Configura o TestBed (ambiente de teste) antes de cada 'it'.
   */
  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [MensagemDialog, MatDialogModule, MatButtonModule, MatIconModule],
      providers: [{ provide: MAT_DIALOG_DATA, useValue: mockDadosSucesso }],
    }).compileComponents();

    fixture = TestBed.createComponent(MensagemDialog);
    component = fixture.componentInstance;
    el = fixture.nativeElement;
  });

  /**
   * Testa se o componente é criado com sucesso.
   */
  it('deve criar o componente', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  /**
   * Testa a renderização visual para mensagens de sucesso.
   * Verifica classe CSS, ícone e cor do botão.
   */
  it('deve exibir estilo e ícone de sucesso', () => {
    component.dados = mockDadosSucesso;
    fixture.detectChanges();

    const container = el.querySelector('#mensagem-dialog');
    const icone = el.querySelector('mat-icon')?.textContent;
    const botao = el.querySelector('button');

    expect(container?.classList).toContain('tipo-sucesso');
    expect(icone).toContain('check_circle');
    expect(botao?.classList).toContain('mat-primary');
  });

  /**
   * Testa a renderização visual para mensagens de aviso.
   * Verifica classe CSS, ícone e a mudança de cor do botão para 'warn'.
   */
  it('deve exibir estilo e ícone de aviso', () => {
    component.dados = mockDadosAviso;
    fixture.detectChanges();

    const container = el.querySelector('#mensagem-dialog');
    const icone = el.querySelector('mat-icon')?.textContent;
    const botao = el.querySelector('button');

    expect(container?.classList).toContain('tipo-aviso');
    expect(icone).toContain('warning');
    expect(botao?.classList).toContain('mat-warn');
  });

  /**
   * Testa a renderização visual para mensagens de informação.
   * Verifica classe CSS e ícone.
   */
  it('deve exibir estilo e ícone de informação', () => {
    component.dados = mockDadosInfo;
    fixture.detectChanges();

    const container = el.querySelector('#mensagem-dialog');
    const icone = el.querySelector('mat-icon')?.textContent;

    expect(container?.classList).toContain('tipo-info');
    expect(icone).toContain('info');
  });

  /**
   * Testa a renderização do conteúdo textual.
   * Verifica se título e mensagem (HTML) são injetados corretamente.
   */
  it('deve exibir título e mensagem corretamente', () => {
    const dadosHtml: DadosMensagem = {
      ...mockDadosInfo,
      titulo: 'Teste Título',
      mensagem: 'Conteúdo <strong>HTML</strong>',
    };
    component.dados = dadosHtml;
    fixture.detectChanges();

    const titulo = el.querySelector('#titulo-mensagem')?.textContent;
    const mensagem = el.querySelector('#mensagem-mensagem')?.innerHTML;

    expect(titulo).toBe('Teste Título');
    expect(mensagem).toContain('Conteúdo <strong>HTML</strong>');
  });

  /**
   * Testa a renderização do botão de ação.
   * Verifica texto customizado e texto padrão.
   */
  it('deve exibir o texto do botão customizado ou padrão', async () => {
    component.dados = { ...mockDadosInfo, textoBotao: 'Confirmar' };
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    let botao = el.querySelector('button')?.textContent;
    expect(botao).toContain('Confirmar');

    component.dados = { ...mockDadosInfo, textoBotao: undefined };

    fixture.componentRef.changeDetectorRef.markForCheck();

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    botao = el.querySelector('button')?.textContent;
    expect(botao).toContain('Fechar');
  });
});
