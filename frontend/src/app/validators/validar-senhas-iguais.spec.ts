import { FormControl, FormGroup } from '@angular/forms';
import { validarSenhasIguais } from './validar-senhas-iguais';

/**
 * Testes unitários para o validador customizado {@link validarSenhasIguais}.
 *
 * @author Matheus F. N. Pereira
 */
describe('validarSenhasIguais', () => {
  /**
   * Testa se o validador ignora caso os controles
   * ainda não existam no grupo.
   */
  it('deve retornar null se os controles não existirem no grupo', () => {
    const group = new FormGroup({});
    const resultado = validarSenhasIguais(group);
    expect(resultado).toBeNull();
  });

  /**
   * Testa o cenário de erro básico: Senhas diferentes.
   */
  it('deve retornar erro "senhasNaoConferem" se os valores forem diferentes', () => {
    const group = new FormGroup({
      senha: new FormControl('Ab123456'),
      confirmarSenha: new FormControl('Ab1234567'),
    });

    const resultado = validarSenhasIguais(group);

    expect(resultado).toEqual({ senhasNaoConferem: true });

    const confirmControl = group.get('confirmarSenha');
    expect(confirmControl?.hasError('senhasNaoConferem')).toBeTrue();
  });

  /**
   * Testa se o validador preserva outros erros que já existiam no campo.
   */
  it('deve manter outros erros existentes ao adicionar "senhasNaoConferem"', () => {
    const confirmControl = new FormControl('curta');
    confirmControl.setErrors({ minlength: true });

    const group = new FormGroup({
      senha: new FormControl('senhaLonga123'),
      confirmarSenha: confirmControl,
    });

    validarSenhasIguais(group);

    expect(confirmControl.hasError('minlength')).toBeTrue();
    expect(confirmControl.hasError('senhasNaoConferem')).toBeTrue();
    expect(Object.keys(confirmControl.errors || {}).length).toBe(2);
  });

  /**
   * Testa o cenário de sucesso básico: Senhas iguais.
   */
  it('deve retornar null se as senhas forem iguais', () => {
    const group = new FormGroup({
      senha: new FormControl('Ab123456'),
      confirmarSenha: new FormControl('Ab123456'),
    });

    const resultado = validarSenhasIguais(group);

    expect(resultado).toBeNull();
    expect(group.get('confirmarSenha')?.errors).toBeNull();
  });

  /**
   * Testa a lógica de limpeza.
   * Se o campo tinha o erro 'senhasNaoConferem' e o usuário corrigiu,
   * o erro deve ser removido.
   */
  it('deve remover o erro "senhasNaoConferem" quando o usuário corrigir a senha', () => {
    const confirmControl = new FormControl('Ab123456');
    confirmControl.setErrors({ senhasNaoConferem: true });

    const group = new FormGroup({
      senha: new FormControl('Ab123456'),
      confirmarSenha: confirmControl,
    });

    const resultado = validarSenhasIguais(group);

    expect(resultado).toBeNull();
    expect(confirmControl.errors).toBeNull();
  });

  /**
   * Testa a lógica de limpeza parcial.
   * Se o campo tinha 'senhasNaoConferem' E 'required', e o usuário corrigiu a igualdade,
   * apenas 'senhasNaoConferem' deve sair. O 'required' deve ficar.
   */
  it('deve remover APENAS "senhasNaoConferem" e manter outros erros ao corrigir', () => {
    const confirmControl = new FormControl('Ab123456');
    confirmControl.setErrors({ senhasNaoConferem: true, outroErro: true });

    const group = new FormGroup({
      senha: new FormControl('Ab123456'),
      confirmarSenha: confirmControl,
    });

    validarSenhasIguais(group);

    expect(confirmControl.hasError('senhasNaoConferem')).toBeFalse();
    expect(confirmControl.hasError('outroErro')).toBeTrue();
    expect(confirmControl.errors).toEqual({ outroErro: true });
  });
});
