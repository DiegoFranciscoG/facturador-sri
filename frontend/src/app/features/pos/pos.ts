import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Bodega, CatalogoItem, Cliente, Comprobante, Producto } from '../../core/models';

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
  ) {}

  ngOnInit(): void {
    this.api.listarClientes(0, 100).subscribe((p) => this.clientes.set(p.content));
    this.api.listarProductos(0, 100).subscribe((p) => {
      this.productos.set(p.content.filter((prod) => prod.activo));
    });
    this.api.listarBodegas().subscribe((b) => {
      this.bodegas.set(b);
      this.bodegaId = b[0]?.id;
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
    if (!producto || this.cantidad <= 0) {
      return;
    }
    this.lineas.update((actuales) => [...actuales, { producto, cantidad: this.cantidad }]);
    this.cantidad = 1;
  }

  quitarLinea(index: number): void {
    this.lineas.update((actuales) => actuales.filter((_, i) => i !== index));
  }

  confirmarVenta(): void {
    if (!this.clienteId || !this.bodegaId || this.lineas().length === 0) {
      this.error.set('Seleccione cliente, bodega y al menos un producto.');
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
        },
        error: (err) => {
          this.enviando.set(false);
          this.error.set(err.error?.mensaje ?? 'No se pudo registrar la venta.');
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
