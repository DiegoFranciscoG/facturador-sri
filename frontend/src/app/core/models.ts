export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  rol: string;
  expiraEnMinutos: number;
}

export interface CatalogoItem {
  codigo: string;
  descripcion: string;
  valorNumerico: number | null;
}

export interface Cliente {
  id: number;
  tipoIdentificacion: string;
  identificacion: string;
  razonSocial: string;
  direccion: string | null;
  email: string | null;
  telefono: string | null;
}

export interface ClienteRequest {
  tipoIdentificacion: string;
  identificacion: string;
  razonSocial: string;
  direccion?: string;
  email?: string;
  telefono?: string;
}

export interface Producto {
  id: number;
  codigoPrincipal: string;
  codigoAuxiliar: string | null;
  descripcion: string;
  unidadMedida: string;
  precioUnitario: number;
  codigoImpuesto: string;
  codigoPorcentajeIva: string;
  activo: boolean;
}

export interface ProductoRequest {
  codigoPrincipal: string;
  codigoAuxiliar?: string;
  descripcion: string;
  unidadMedida: string;
  precioUnitario: number;
  codigoImpuesto: string;
  codigoPorcentajeIva: string;
}

export interface Bodega {
  id: number;
  nombre: string;
  establecimientoId: number | null;
  activa: boolean;
}

export type EstadoComprobante =
  | 'GENERADO'
  | 'FIRMADO'
  | 'RECIBIDO'
  | 'DEVUELTA'
  | 'AUTORIZADO'
  | 'NO_AUTORIZADO';

export interface Comprobante {
  id: number;
  claveAcceso: string;
  tipoComprobante: string;
  secuencial: string;
  fechaEmision: string;
  estado: EstadoComprobante;
  totalSinImpuestos: number;
  totalDescuento: number;
  importeTotal: number;
  numeroAutorizacion: string | null;
  fechaAutorizacion: string | null;
  clienteRazonSocial: string;
}

export interface VentaLineaRequest {
  productoId: number;
  cantidad: number;
  descuento?: number;
}

export interface PagoRequest {
  formaPago: string;
  total: number;
  plazo?: number;
  unidadTiempo?: string;
}

export interface VentaRequest {
  establecimientoId: number;
  puntoEmisionId: number;
  clienteId: number;
  bodegaId: number;
  lineas: VentaLineaRequest[];
  pagos: PagoRequest[];
}

export interface NotaCreditoRequest {
  comprobanteOriginalId: number;
  bodegaId: number;
  motivo: string;
  lineas: VentaLineaRequest[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
