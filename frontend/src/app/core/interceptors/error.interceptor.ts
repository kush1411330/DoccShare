import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Do not redirect on failed login/register attempts or password checks on public download page
      const isAuthEndpoint = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/register');
      const isPublicEndpoint = req.url.includes('/api/public/');

      if (error.status === 401 && !isAuthEndpoint && !isPublicEndpoint) {
        authService.logout(true);
      } else if (error.status === 429) {
        const retryAfter = error.headers.get('Retry-After');
        const msg = retryAfter ? `Please retry in ${retryAfter} seconds.` : 'Too many requests. Please wait a moment.';
        toast.error(msg, 'Rate Limit Reached');
      } else if (!isAuthEndpoint && !isPublicEndpoint) {
        const errorMessage = error.error?.message || error.statusText || 'An unexpected error occurred';
        toast.error(errorMessage, `Error (${error.status || 'Network'})`);
      }

      return throwError(() => error);
    })
  );
};
