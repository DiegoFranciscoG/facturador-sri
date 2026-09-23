import { Injectable, signal } from '@angular/core';

export type TipoNotificacion = 'exito' | 'error' | 'info';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  mensaje: string;
}

/**
 * Notificaciones en pantalla (no bloqueantes) para reemplazar alert()/confirm()
 * nativos: cada accion importante (crear, eliminar, emitir, error de API)
 * muestra un mensaje temporal en la esquina de la pantalla.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private contador = 0;
  readonly notificaciones = signal<Notificacion[]>([]);

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.mostrar('error', mensaje);
  }

  info(mensaje: string): void {
    this.mostrar('info', mensaje);
  }

  descartar(id: number): void {
    this.notificaciones.update((actuales) => actuales.filter((n) => n.id !== id));
  }

  private mostrar(tipo: TipoNotificacion, mensaje: string): void {
    const id = ++this.contador;
    this.notificaciones.update((actuales) => [...actuales, { id, tipo, mensaje }]);
    setTimeout(() => this.descartar(id), 5000);
  }
}
