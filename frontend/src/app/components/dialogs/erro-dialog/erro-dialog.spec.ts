import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ErroDialog, DadosErroDialog } from './erro-dialog';

/**
 * Testes unitários para o componente {@link ErroDialog}.
 *
 * @author Matheus F. N. Pereira
 */
describe('ErroDialog', () => {
  let component: ErroDialog;
  let fixture: ComponentFixture<ErroDialog>;
  let el: HTMLElement;

  /**
   * Mock de um Erro Padrão (ex: 400 E-mail Duplicado, 500 Erro Interno).
   * Este mock NÃO tem a lista de erros de campo.
   */
  const mockErroPadrao: DadosErroDialog = {
    erroPadrao: {
      status: 400,
      titulo: 'Violação de Dados',
      mensagem: 'O e-mail já está cadastrado.',
      dataHora: '15/11/2025 10:00',
      rota: '/api/autenticacao/cadastro',
    },
    listaErros: [],
  };

  /**
   * Mock de um Erro de Validação (ex: 422 Senha Fraca).
   * Este mock TEM a lista de erros de campo.
   */
  const mockErroValidacao: DadosErroDialog = {
    erroPadrao: {
      status: 422,
      titulo: 'Erro de Validação',
      mensagem: 'Um ou mais campos estão inválidos.',
      dataHora: '15/11/2025 10:01',
      rota: '/api/autenticacao/cadastro',
    },
    listaErros: [
      { campo: 'senha', mensagem: 'A senha é obrigatória.' },
      { campo: 'email', mensagem: 'O formato do e-mail é inválido.' },
    ],
  };

  /**
   * Configura o TestBed (ambiente de teste) antes de cada 'it'.
   */
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErroDialog, MatDialogModule, MatButtonModule, MatIconModule],
      providers: [{ provide: MAT_DIALOG_DATA, useValue: mockErroPadrao }],
    }).compileComponents();

    fixture = TestBed.createComponent(ErroDialog);
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
   * Testa a exibição de um erro padrão (ex: 400, 500).
   * Verifica se o título e a mensagem principal são exibidos,
   * mas a lista de erros (<ul>) não é renderizada.
   */
  it('deve exibir erro padrão e ocultar lista de validação (Fluxo Erro Padrão)', () => {
    component.dados = mockErroPadrao;

    fixture.detectChanges();

    const titulo = el.querySelector('#titulo-dialog span')?.textContent;
    const mensagem = el.querySelector('#conteudo-dialog p')?.textContent;
    const listaErros = el.querySelector('#lista-erros-validacao');

    expect(titulo).toContain('Violação de Dados');
    expect(mensagem).toContain('O e-mail já está cadastrado.');
    expect(listaErros).toBeNull();
  });

  /**
   * Testa a exibição de um erro de validação (ex: 422).
   * Verifica se o título, a mensagem principal e a lista de erros (<ul>)
   * são renderizados corretamente.
   */
  it('deve exibir a lista de erros de campo (Fluxo Erro de Validação)', () => {
    component.dados = mockErroValidacao;

    fixture.detectChanges();

    const titulo = el.querySelector('#titulo-dialog span')?.textContent;
    const mensagem = el.querySelector('#conteudo-dialog p')?.textContent;
    const listaErros = el.querySelector('#lista-erros-validacao');
    const itensErro = el.querySelectorAll('#lista-erros-validacao li');

    expect(titulo).toContain('Erro de Validação');
    expect(mensagem).toContain('Um ou mais campos estão inválidos.');
    expect(listaErros).not.toBeNull();
    expect(itensErro.length).toBe(2);
    expect(itensErro[0].textContent).toContain('senha: A senha é obrigatória.');
    expect(itensErro[1].textContent).toContain('email: O formato do e-mail é inválido.');
  });
});
