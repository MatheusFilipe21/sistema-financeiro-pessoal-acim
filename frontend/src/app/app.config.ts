import {
  ApplicationConfig,
  ErrorHandler,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import localePt from '@angular/common/locales/pt';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { prefixoApiInterceptor } from './interceptors/prefixo-api-interceptor';
import { TratadorDeErrosGlobal } from './exceptions/tratador-de-erros-global';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { PaginatorPtBrIntl } from './utils/paginator-ptbr';
import { registerLocaleData } from '@angular/common';

/**
 * Sobrescreve as configurações padrão do locale en-US, como virgulas por pontos na exibição de números.
 */
registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([prefixoApiInterceptor])),
    { provide: ErrorHandler, useClass: TratadorDeErrosGlobal },
    { provide: MatPaginatorIntl, useClass: PaginatorPtBrIntl },
    { provide: LOCALE_ID, useValue: 'pt-BR' },
  ],
};
