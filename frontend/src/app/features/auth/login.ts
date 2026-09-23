import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
})
export class Login {
  username = '';
  password = '';
  cargando = signal(false);
  error = signal<string | null>(null);

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  ingresar(): void {
    this.error.set(null);
    this.cargando.set(true);
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(
          err.status === 429
            ? 'Demasiados intentos fallidos. Espere unos minutos.'
            : 'Usuario o contraseña incorrectos.',
        );
      },
    });
  }
}
