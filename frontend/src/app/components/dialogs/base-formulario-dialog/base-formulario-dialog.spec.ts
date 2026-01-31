import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';

import { BaseFormularioDialog } from './base-formulario-dialog';

/**
 * Testes unitários para o componente {@link BaseFormularioDialog}.
 *
 * Verifica a renderização correta baseada no tipo de operação (cadastrar/editar/excluir),
 * o estado dos botões (loading/invalid) e a emissão de eventos, utilizando
 * seletores de ID para garantir a integridade da estrutura HTML.
 *
 * @author Matheus F. N. Pereira
 */
describe('BaseFormularioDialog', () => {
  let component: BaseFormularioDialog;
  let fixture: ComponentFixture<BaseFormularioDialog>;

  /**
   * Configuração inicial do ambiente de teste.
   * Compila os componentes e define inputs iniciais obrigatórios.
   */
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BaseFormularioDialog,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BaseFormularioDialog);
    component = fixture.componentInstance;

    // Define valores iniciais para evitar erros de inputs obrigatórios
    component.titulo = 'Título de Teste';
    component.operacao = 'cadastrar';

    fixture.detectChanges();
  });

  /**
   * Verifica se a instância do componente foi criada com sucesso.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Verifica se o título passado via Input é renderizado corretamente no cabeçalho.
   */
  it('deve exibir o título corretamente', () => {
    const tituloEl = fixture.debugElement.query(By.css('h2[mat-dialog-title]'));
    expect(tituloEl.nativeElement.textContent).toContain('Título de Teste');
  });

  /**
   * Cenário: Operação "Cadastrar".
   * Verifica se o botão principal exibe o texto "Salvar" e usa a cor primária.
   */
  it('deve exibir botão "Salvar" e cor primary para operação "cadastrar"', () => {
    component.operacao = 'cadastrar';
    fixture.detectChanges();

    const btnConfirmar = fixture.debugElement.query(By.css('#btn-confirmar'));

    expect(btnConfirmar.nativeElement.textContent.trim()).toBe('Salvar');
    expect(component.corBotaoConfirmar).toBe('primary');
  });

  /**
   * Cenário: Operação "Excluir".
   * Verifica se o botão principal exibe o texto "Excluir", usa a cor de alerta (warn)
   * e se a mensagem de aviso visual é renderizada.
   */
  it('deve exibir botão "Excluir", cor warn e alerta para operação "excluir"', () => {
    component.operacao = 'excluir';
    fixture.detectChanges();

    const btnConfirmar = fixture.debugElement.query(By.css('#btn-confirmar'));
    const alerta = fixture.debugElement.query(By.css('.alerta-exclusao'));

    expect(btnConfirmar.nativeElement.textContent.trim()).toBe('Excluir');
    expect(component.corBotaoConfirmar).toBe('warn');
    expect(alerta).toBeTruthy();
  });

  /**
   * Verifica a interatividade do botão de confirmação.
   * Deve disparar o Output `confirmar` ao ser clicado.
   */
  it('deve emitir evento "confirmar" ao clicar no botão de ação (#btn-confirmar)', () => {
    spyOn(component.confirmar, 'emit');

    const btnConfirmar = fixture.debugElement.query(By.css('#btn-confirmar'));
    btnConfirmar.nativeElement.click();

    expect(component.confirmar.emit).toHaveBeenCalled();
  });

  /**
   * Verifica a interatividade do botão de cancelar.
   * Deve disparar o Output `cancelar` ao ser clicado.
   */
  it('deve emitir evento "cancelar" ao clicar no botão Cancelar (#btn-cancelar)', () => {
    spyOn(component.cancelar, 'emit');

    const btnCancelar = fixture.debugElement.query(By.css('#btn-cancelar'));
    btnCancelar.nativeElement.click();

    expect(component.cancelar.emit).toHaveBeenCalled();
  });

  /**
   * Verifica a interatividade do botão de fechar (X) no cabeçalho.
   * Deve disparar o Output `cancelar` ao ser clicado.
   */
  it('deve emitir evento "cancelar" ao clicar no botão de fechar (#btn-fechar)', () => {
    spyOn(component.cancelar, 'emit');

    const btnFechar = fixture.debugElement.query(By.css('#btn-fechar'));
    btnFechar.nativeElement.click();

    expect(component.cancelar.emit).toHaveBeenCalled();
  });

  /**
   * Verifica o estado desabilitado do botão principal quando o formulário é inválido.
   */
  it('deve desabilitar botão de confirmação se formularioInvalido for true', () => {
    component.formularioInvalido = true;
    fixture.detectChanges();

    const btnConfirmar = fixture.debugElement.query(By.css('#btn-confirmar'));
    expect(btnConfirmar.nativeElement.disabled).toBeTrue();
  });

  /**
   * Verifica o estado de carregamento (Processing).
   * O botão principal deve estar desabilitado e o Spinner deve estar visível.
   */
  it('deve exibir spinner e desabilitar botões quando estiver processando', () => {
    component.processando = true;
    fixture.detectChanges();

    const btnConfirmar = fixture.debugElement.query(By.css('#btn-confirmar'));
    const spinner = fixture.debugElement.query(By.css('mat-spinner'));

    expect(btnConfirmar.nativeElement.disabled).toBeTrue();
    expect(spinner).toBeTruthy();
  });
});
