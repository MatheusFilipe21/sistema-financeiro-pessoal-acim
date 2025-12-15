import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Pessoas } from './pessoas';
import { Pessoa as PessoaService } from '../../../services/pessoa';
import { Dialog as DialogService } from '../../../services/dialog';
import { PessoaDTO } from '../../../dtos/pessoa/PessoaDTO';

/**
 * Testes unitários para o componente de página {@link Pessoas}.
 *
 * Cobre a lógica de listagem, filtragem local, alternância de visualização
 * e orquestração das ações de CRUD via DialogService.
 *
 * @author Matheus F. N. Pereira
 */
describe('Pessoas', () => {
  let component: Pessoas;
  let fixture: ComponentFixture<Pessoas>;
  let pessoaServiceSpy: jasmine.SpyObj<PessoaService>;
  let dialogServiceSpy: jasmine.SpyObj<DialogService>;

  const listaPessoasMock: PessoaDTO[] = [
    { id: '1', nome: 'Ana Silva' },
    { id: '2', nome: 'Bruno Costa' },
    { id: '3', nome: 'Carlos Souza' },
  ];

  beforeEach(async () => {
    pessoaServiceSpy = jasmine.createSpyObj('PessoaService', ['listar']);
    dialogServiceSpy = jasmine.createSpyObj('DialogService', [
      'abrirFormularioPessoa',
      'mostrarSucesso',
    ]);

    pessoaServiceSpy.listar.and.returnValue(of(listaPessoasMock));

    await TestBed.configureTestingModule({
      imports: [Pessoas, FormsModule],
      providers: [
        { provide: PessoaService, useValue: pessoaServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Pessoas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  /**
   * Verifica se o componente foi criado com sucesso.
   */
  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  /**
   * Teste de Inicialização e Carregamento de Dados (RF47).
   */
  it('deve carregar dados ao iniciar (ngOnInit)', () => {
    expect(pessoaServiceSpy.listar).toHaveBeenCalled();
    expect(component.pessoas).toEqual(listaPessoasMock);
    expect(component.pessoasFiltradas).toEqual(listaPessoasMock);
  });

  /**
   * Teste de Filtragem Local.
   */
  it('deve filtrar a lista localmente pelo nome', () => {
    component.filtro.nome = 'ana';
    component.filtrar();

    expect(component.pessoasFiltradas.length).toBe(1);
    expect(component.pessoasFiltradas[0].nome).toBe('Ana Silva');
  });

  it('deve restaurar a lista completa ao limpar o filtro', () => {
    component.filtro.nome = 'ana';
    component.filtrar();
    expect(component.pessoasFiltradas.length).toBe(1);

    component.limpar();

    expect(component.filtro.nome).toBe('');
    expect(component.pessoasFiltradas.length).toBe(3);
  });

  /**
   * Teste de Alternância de Visualização.
   */
  it('deve alternar entre visualização de Cards e Tabela', () => {
    expect(component.visualizacaoEmCards).toBeTrue();

    component.visualizacaoEmCards = false;
    fixture.detectChanges();

    expect(component.visualizacaoEmCards).toBeFalse();
  });

  /**
   * Teste do Fluxo de Adicionar (RF46).
   */
  it('deve abrir dialog de cadastro e atualizar lista ao confirmar', () => {
    const novaPessoa: PessoaDTO = { id: '4', nome: 'Nova Pessoa' };

    dialogServiceSpy.abrirFormularioPessoa.and.returnValue(of(novaPessoa));

    component.aoAdicionar();

    expect(dialogServiceSpy.abrirFormularioPessoa).toHaveBeenCalledWith('cadastrar');

    expect(pessoaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  it('não deve atualizar lista se o cadastro for cancelado', () => {
    dialogServiceSpy.abrirFormularioPessoa.and.returnValue(of(undefined));

    component.aoAdicionar();

    expect(dialogServiceSpy.abrirFormularioPessoa).toHaveBeenCalledWith('cadastrar');
    expect(pessoaServiceSpy.listar).toHaveBeenCalledTimes(1);
  });

  /**
   * Teste do Fluxo de Editar.
   */
  it('deve abrir dialog de edição e atualizar lista ao confirmar', () => {
    const pessoaAlvo = listaPessoasMock[0];
    const pessoaEditada = { ...pessoaAlvo, nome: 'Ana Editada' };

    dialogServiceSpy.abrirFormularioPessoa.and.returnValue(of(pessoaEditada));

    component.aoEditar(pessoaAlvo);

    expect(dialogServiceSpy.abrirFormularioPessoa).toHaveBeenCalledWith('editar', pessoaAlvo);
    expect(pessoaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });

  /**
   * Teste do Fluxo de Excluir (RF49).
   */
  it('deve abrir dialog de exclusão e atualizar lista ao confirmar', () => {
    const pessoaAlvo = listaPessoasMock[1];

    dialogServiceSpy.abrirFormularioPessoa.and.returnValue(of(true));

    component.aoExcluir(pessoaAlvo);

    expect(dialogServiceSpy.abrirFormularioPessoa).toHaveBeenCalledWith('excluir', pessoaAlvo);
    expect(pessoaServiceSpy.listar).toHaveBeenCalledTimes(2);
  });
});
