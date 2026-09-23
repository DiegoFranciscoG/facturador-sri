import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastHost } from './core/ui/toast-host';
import { ConfirmDialog } from './core/ui/confirm-dialog';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastHost, ConfirmDialog],
  template: `
    <router-outlet />
    <app-toast-host />
    <app-confirm-dialog />
  `,
})
export class App {}
