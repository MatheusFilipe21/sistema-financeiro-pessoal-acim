/**
 * Arquivo de configuração global para os testes unitários no Angular.
 * Este script é executado uma única vez pelo executor de testes (Vitest)
 * antes de rodar qualquer suíte de testes (arquivos .spec.ts).
 */
import '@angular/compiler';
import 'zone.js';
import 'zone.js/testing';

import { getTestBed } from '@angular/core/testing';
import { BrowserTestingModule, platformBrowserTesting } from '@angular/platform-browser/testing';

getTestBed().initTestEnvironment(BrowserTestingModule, platformBrowserTesting(), {
  teardown: { destroyAfterEach: true },
});
