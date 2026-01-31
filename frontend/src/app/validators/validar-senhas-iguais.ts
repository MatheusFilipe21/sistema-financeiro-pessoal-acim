import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador que verifica se os campos 'senha' e 'confirmarSenha' são iguais.
 * Se forem diferentes, define o erro 'senhasNaoConferem' no campo confirmarSenha.
 *
 * @author Matheus F. N. Pereira
 */
export const validarSenhasIguais: ValidatorFn = (
  group: AbstractControl
): ValidationErrors | null => {
  const senha = group.get('senha');
  const confirmarSenha = group.get('confirmarSenha');

  if (!senha || !confirmarSenha) {
    return null;
  }

  if (senha.value === confirmarSenha.value) {
    if (confirmarSenha.hasError('senhasNaoConferem')) {
      const errors = { ...confirmarSenha.errors };
      delete errors['senhasNaoConferem'];
      confirmarSenha.setErrors(Object.keys(errors).length > 0 ? errors : null);
    }

    return null;
  } else {
    confirmarSenha.setErrors({ ...confirmarSenha.errors, senhasNaoConferem: true });

    return { senhasNaoConferem: true };
  }
};
