import {
  ApplicationConfig,
  ErrorHandler,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { prefixoApiInterceptor } from './interceptors/prefixo-api-interceptor';
import { TratadorDeErrosGlobal } from './exceptions/tratador-de-erros-global';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { PaginatorPtBrIntl } from './utils/paginator-ptbr';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([prefixoApiInterceptor])),
    { provide: ErrorHandler, useClass: TratadorDeErrosGlobal },
    { provide: MatPaginatorIntl, useClass: PaginatorPtBrIntl },
  ],
};
