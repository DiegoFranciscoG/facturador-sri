import { Injectable, signal } from '@angular/core';

export interface SolicitudConfirmacion {
  titulo: string;
  mensaje: string;
  resolver: (confirmado: boolean) => void;
}

/** Modal de confirmación propio, en vez del confirm() nativo del navegador. */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly solicitud = signal<SolicitudConfirmacion | null>(null);

  pedir(titulo: string, mensaje: string): Promise<boolean> {
    return new Promise((resolve) => {
      this.solicitud.set({ titulo, mensaje, resolver: resolve });
    });
  }

  responder(confirmado: boolean): void {
    this.solicitud()?.resolver(confirmado);
    this.solicitud.set(null);
  }
}
