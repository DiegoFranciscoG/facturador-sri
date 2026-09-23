import { vi } from 'vitest';

/**
 * Node 22+ expone un `localStorage` global experimental que no funciona sin
 * `--localstorage-file`, y ese global pisa el `window.localStorage` real de
 * jsdom en algunos entornos de Vitest. Para no depender de ese detalle del
 * entorno de pruebas, se reemplaza por una implementacion en memoria.
 */
export function instalarLocalStorageFalso(): Storage {
  const almacen = new Map<string, string>();

  const falso: Storage = {
    get length() {
      return almacen.size;
    },
    clear: () => almacen.clear(),
    getItem: (clave: string) => (almacen.has(clave) ? almacen.get(clave)! : null),
    key: (index: number) => Array.from(almacen.keys())[index] ?? null,
    removeItem: (clave: string) => {
      almacen.delete(clave);
    },
    setItem: (clave: string, valor: string) => {
      almacen.set(clave, valor);
    },
  };

  vi.stubGlobal('localStorage', falso);
  Object.defineProperty(window, 'localStorage', { value: falso, configurable: true });
  return falso;
}
