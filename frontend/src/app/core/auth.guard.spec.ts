import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import { authGuard } from './auth.guard';
import { instalarLocalStorageFalso } from './testing/fake-local-storage';

describe('authGuard', () => {
  beforeEach(() => {
    instalarLocalStorageFalso();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient()],
    });
  });

  it('permite el acceso si hay un token guardado', () => {
    window.localStorage.setItem('facturador.token', 'jwt-de-prueba');

    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never),
    );

    expect(resultado).toBe(true);
  });

  it('redirige a /login si no hay token', () => {
    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never),
    );

    expect(resultado).toBeInstanceOf(UrlTree);
    const router = TestBed.inject(Router);
    expect(router.serializeUrl(resultado as UrlTree)).toBe('/login');
  });
});
