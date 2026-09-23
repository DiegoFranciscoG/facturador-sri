import { HttpErrorResponse } from '@angular/common/http';

/** Extrae un mensaje legible del ApiError del backend (ver GlobalExceptionHandler). */
export function mensajeDeError(error: unknown, porDefecto = 'Ocurrió un error inesperado.'): string {
  if (error instanceof HttpErrorResponse) {
    const cuerpo = error.error as { mensaje?: string; detalles?: string[] } | null;
    if (cuerpo?.detalles?.length) {
      return cuerpo.detalles.join(' · ');
    }
    if (cuerpo?.mensaje) {
      return cuerpo.mensaje;
    }
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor.';
    }
  }
  return porDefecto;
}
