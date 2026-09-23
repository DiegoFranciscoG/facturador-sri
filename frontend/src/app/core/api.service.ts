import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  Bodega,
  CatalogoItem,
  Cliente,
  ClienteRequest,
  Comprobante,
  NotaCreditoRequest,
  Page,
  Producto,
  ProductoRequest,
  VentaRequest,
} from './models';

const BASE = environment.apiBaseUrl;

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private readonly http: HttpClient) {}

  // Catalogos
  catalogo(nombre: string) {
    return this.http.get<CatalogoItem[]>(`${BASE}/api/catalogos/${nombre}`);
  }

  // Clientes
  listarClientes(page = 0, size = 20) {
    return this.http.get<Page<Cliente>>(`${BASE}/api/clientes`, {
      params: { page, size },
    });
  }

  crearCliente(request: ClienteRequest) {
    return this.http.post<Cliente>(`${BASE}/api/clientes`, request);
  }

  actualizarCliente(id: number, request: ClienteRequest) {
    return this.http.put<Cliente>(`${BASE}/api/clientes/${id}`, request);
  }

  eliminarCliente(id: number) {
    return this.http.delete<void>(`${BASE}/api/clientes/${id}`);
  }

  // Productos
  listarProductos(page = 0, size = 20) {
    return this.http.get<Page<Producto>>(`${BASE}/api/productos`, {
      params: { page, size },
    });
  }

  crearProducto(request: ProductoRequest) {
    return this.http.post<Producto>(`${BASE}/api/productos`, request);
  }

  actualizarProducto(id: number, request: ProductoRequest) {
    return this.http.put<Producto>(`${BASE}/api/productos/${id}`, request);
  }

  desactivarProducto(id: number) {
    return this.http.delete<void>(`${BASE}/api/productos/${id}`);
  }

  // Inventario
  listarBodegas() {
    return this.http.get<Bodega[]>(`${BASE}/api/inventario/bodegas`);
  }

  saldoProducto(productoId: number, bodegaId: number) {
    return this.http.get<{ saldo: number }>(`${BASE}/api/inventario/saldo`, {
      params: { productoId, bodegaId },
    });
  }

  // Comprobantes
  listarComprobantes(page = 0, size = 20, estado?: string) {
    const params: Record<string, string | number> = { page, size };
    if (estado) {
      params['estado'] = estado;
    }
    return this.http.get<Page<Comprobante>>(`${BASE}/api/comprobantes`, { params });
  }

  obtenerComprobante(id: number) {
    return this.http.get<Comprobante>(`${BASE}/api/comprobantes/${id}`);
  }

  crearFactura(request: VentaRequest) {
    return this.http.post<Comprobante>(`${BASE}/api/comprobantes/facturas`, request);
  }

  crearNotaCredito(request: NotaCreditoRequest) {
    return this.http.post<Comprobante>(`${BASE}/api/comprobantes/notas-credito`, request);
  }

  obtenerXml(id: number) {
    return this.http.get(`${BASE}/api/comprobantes/${id}/xml`, { responseType: 'text' });
  }

  descargarRide(id: number) {
    return this.http.get(`${BASE}/api/comprobantes/${id}/ride`, { responseType: 'blob' });
  }
}
