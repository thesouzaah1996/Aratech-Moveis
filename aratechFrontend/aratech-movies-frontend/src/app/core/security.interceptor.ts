import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { CsrfService } from './services/csrf.service';
import { SessaoState } from './services/sessao-state.service';

export const securityInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith('/aratech/')) return next(req);
  const csrf = inject(CsrfService), state = inject(SessaoState), router = inject(Router);
  const enviar = () => ['GET', 'HEAD', 'OPTIONS'].includes(req.method) ? next(req) : csrf.obter().pipe(
    switchMap(token => next(req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } })))
  );
  return enviar().pipe(catchError((erro: HttpErrorResponse) => {
    if (erro.error?.codigo === 'CSRF_INVALIDO') csrf.limpar();
    if (erro.status === 401 && !req.url.startsWith('/aratech/auth/')) {
      state.usuario.set(null); csrf.limpar();
      void router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
    }
    return throwError(() => erro);
  }));
};
