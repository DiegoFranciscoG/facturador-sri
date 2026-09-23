import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
  },
  {
    path: '',
    loadComponent: () => import('./features/shell/shell').then((m) => m.Shell),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'pos', pathMatch: 'full' },
      {
        path: 'pos',
        loadComponent: () => import('./features/pos/pos').then((m) => m.Pos),
      },
      {
        path: 'comprobantes',
        loadComponent: () =>
          import('./features/comprobantes/comprobantes-list').then((m) => m.ComprobantesList),
      },
      {
        path: 'comprobantes/:id',
        loadComponent: () =>
          import('./features/comprobantes/comprobante-detalle').then((m) => m.ComprobanteDetalle),
      },
      {
        path: 'productos',
        loadComponent: () => import('./features/productos/productos').then((m) => m.Productos),
      },
      {
        path: 'clientes',
        loadComponent: () => import('./features/clientes/clientes').then((m) => m.Clientes),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
