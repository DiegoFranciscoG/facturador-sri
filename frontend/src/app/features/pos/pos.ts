import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/error.util';
import { Bodega, CatalogoItem, Cliente, Comprobante, Producto } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

const CANTIDAD_MAXIMA = 999999;

interface LineaVenta {
  producto: Producto;
  cantidad: number;
}

@Component({
  selector: 'app-pos',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './pos.html',
})
export class Pos implements OnInit {
  clientes = signal<Cliente[]>([]);
  productos = signal<Producto[]>([]);
  bodegas = signal<Bodega[]>([]);
  formasPago = signal<CatalogoItem[]>([]);

  clienteId?: number;
  bodegaId?: number;
  productoSeleccionadoId?: number;
  cantidad = 1;
  formaPago = '01';

  lineas = signal<LineaVenta[]>([]);
  enviando = signal(false);
  error = signal<string | null>(null);
  resultado = signal<Comprobante | null>(null);

  readonly cantidadMaxima = CANTIDAD_MAXIMA;

  readonly totalSinImpuestos = computed(() =>
    this.lineas().reduce((suma, l) => suma + l.producto.precioUnitario * l.cantidad, 0),
  );

  readonly totalIva = computed(() =>
    this.lineas().reduce((suma, l) => {
      const tarifa = this.tarifaDe(l.producto.codigoPorcentajeIva);
      return suma + (l.producto.precioUnitario * l.cantidad * tarifa) / 100;
    }, 0),
  );

  readonly total = computed(() => this.totalSinImpuestos() + this.totalIva());

  constructor(
    private readonly api: ApiService,
    private readonly router: Router,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.api.listarClientes(0, 100).subscribe({
      next: (p) => this.clientes.set(p.content),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar los clientes.')),
    });
    this.api.listarProductos(0, 100).subscribe({
      next: (p) => this.productos.set(p.content.filter((prod) => prod.activo)),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar los productos.')),
    });
    this.api.listarBodegas().subscribe({
      next: (b) => {
        this.bodegas.set(b);
        this.bodegaId = b[0]?.id;
      },
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar las bodegas.')),
    });
    this.api.catalogo('FORMA_PAGO').subscribe((items) => this.formasPago.set(items));
    this.api.catalogo('CODIGO_PORCENTAJE_IVA').subscribe((items) => (this.tarifasIva = items));
  }

  private tarifasIva: CatalogoItem[] = [];

  private tarifaDe(codigo: string): number {
    return this.tarifasIva.find((t) => t.codigo === codigo)?.valorNumerico ?? 0;
  }

  agregarLinea(): void {
    const producto = this.productos().find((p) => p.id === this.productoSeleccionadoId);
    if (!producto) {
      this.notifications.error('Seleccione un producto.');
      return;
    }
    if (!this.cantidad || this.cantidad <= 0) {
      this.notifications.error('La cantidad debe ser mayor a cero.');
      return;
    }
    if (this.cantidad > CANTIDAD_MAXIMA) {
      this.notifications.error(`La cantidad no puede superar ${CANTIDAD_MAXIMA}.`);
      return;
    }
    this.lineas.update((actuales) => [...actuales, { producto, cantidad: this.cantidad }]);
    this.cantidad = 1;
    this.productoSeleccionadoId = undefined;
  }

  quitarLinea(index: number): void {
    this.lineas.update((actuales) => actuales.filter((_, i) => i !== index));
  }

  confirmarVenta(): void {
    if (!this.clienteId) {
      this.notifications.error('Seleccione un cliente.');
      return;
    }
    if (!this.bodegaId) {
      this.notifications.error('Seleccione una bodega.');
      return;
    }
    if (this.lineas().length === 0) {
      this.notifications.error('Agregue al menos un producto a la venta.');
      return;
    }
    this.error.set(null);
    this.enviando.set(true);

    this.api
      .crearFactura({
        establecimientoId: 1,
        puntoEmisionId: 1,
        clienteId: this.clienteId,
        bodegaId: this.bodegaId,
        lineas: this.lineas().map((l) => ({ productoId: l.producto.id, cantidad: l.cantidad })),
        pagos: [{ formaPago: this.formaPago, total: Number(this.total().toFixed(2)) }],
      })
      .subscribe({
        next: (comprobante) => {
          this.enviando.set(false);
          this.resultado.set(comprobante);
          this.lineas.set([]);
          this.notifications.exito(`Factura ${comprobante.secuencial} registrada.`);
        },
        error: (err) => {
          this.enviando.set(false);
          const mensaje = mensajeDeError(err, 'No se pudo registrar la venta.');
          this.error.set(mensaje);
          this.notifications.error(mensaje);
        },
      });
  }

  verComprobante(): void {
    const id = this.resultado()?.id;
    if (id) {
      this.router.navigate(['/comprobantes', id]);
    }
  }
}
