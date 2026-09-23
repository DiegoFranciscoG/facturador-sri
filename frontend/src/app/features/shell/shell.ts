import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.html',
})
export class Shell {
  constructor(readonly authService: AuthService) {}

  cerrarSesion(): void {
    this.authService.logout();
  }
}
