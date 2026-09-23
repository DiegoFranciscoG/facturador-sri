import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse } from './models';

const CLAVE_TOKEN = 'facturador.token';
const CLAVE_USUARIO = 'facturador.usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly usuarioActual = signal<{ username: string; rol: string } | null>(
    this.leerUsuarioGuardado(),
  );
  readonly usuario = this.usuarioActual.asReadonly();

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(request: LoginRequest) {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/api/auth/login`, request)
      .pipe(
        tap((respuesta) => {
          window.localStorage.setItem(CLAVE_TOKEN, respuesta.token);
          const usuario = { username: respuesta.username, rol: respuesta.rol };
          window.localStorage.setItem(CLAVE_USUARIO, JSON.stringify(usuario));
          this.usuarioActual.set(usuario);
        }),
      );
  }

  logout(): void {
    window.localStorage.removeItem(CLAVE_TOKEN);
    window.localStorage.removeItem(CLAVE_USUARIO);
    this.usuarioActual.set(null);
    this.router.navigateByUrl('/login');
  }

  obtenerToken(): string | null {
    return window.localStorage.getItem(CLAVE_TOKEN);
  }

  estaAutenticado(): boolean {
    return this.obtenerToken() !== null;
  }

  private leerUsuarioGuardado(): { username: string; rol: string } | null {
    const crudo = window.localStorage.getItem(CLAVE_USUARIO);
    return crudo ? JSON.parse(crudo) : null;
  }
}
