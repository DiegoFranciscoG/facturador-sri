import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../environments/environment';
import { instalarLocalStorageFalso } from './testing/fake-local-storage';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    instalarLocalStorageFalso();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', children: [] }]),
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('no esta autenticado si no hay token guardado', () => {
    expect(service.estaAutenticado()).toBe(false);
  });

  it('guarda el token y el usuario tras un login exitoso', () => {
    service.login({ username: 'admin', password: 'secreto' }).subscribe();

    const peticion = httpMock.expectOne(`${environment.apiBaseUrl}/api/auth/login`);
    expect(peticion.request.method).toBe('POST');
    peticion.flush({ token: 'jwt-de-prueba', username: 'admin', rol: 'ADMIN', expiraEnMinutos: 30 });

    expect(service.estaAutenticado()).toBe(true);
    expect(service.obtenerToken()).toBe('jwt-de-prueba');
    expect(service.usuario()).toEqual({ username: 'admin', rol: 'ADMIN' });
  });

  it('logout limpia el token y el usuario', () => {
    window.localStorage.setItem('facturador.token', 'x');
    window.localStorage.setItem('facturador.usuario', JSON.stringify({ username: 'admin', rol: 'ADMIN' }));

    service.logout();

    expect(service.estaAutenticado()).toBe(false);
    expect(service.usuario()).toBeNull();
  });
});
