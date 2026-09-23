import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Bodega, Comprobante, Producto } from '../../core/models';

@Component({
  selector: 'app-comprobante-detalle',
  imports: [RouterLink, FormsModule, DecimalPipe],
  templateUrl: './comprobante-detalle.html',
})
export class ComprobanteDetalle implements OnInit {
  comprobante = signal<Comprobante | null>(null);
  productos = signal<Producto[]>([]);
  bodegas = signal<Bodega[]>([]);

  mostrarFormularioNc = signal(false);
  ncProductoId?: number;
  ncCantidad = 1;
  ncBodegaId?: number;
  ncMotivo = '';
  guardandoNc = signal(false);
  errorNc = signal<string | null>(null);

  private id!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly api: ApiService,
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
    this.api.listarProductos(0, 100).subscribe((p) => this.productos.set(p.content));
    this.api.listarBodegas().subscribe((b) => {
      this.bodegas.set(b);
      this.ncBodegaId = b[0]?.id;
    });
  }

  cargar(): void {
    this.api.obtenerComprobante(this.id).subscribe((c) => this.comprobante.set(c));
  }

  descargarRide(): void {
    this.api.descargarRide(this.id).subscribe((blob) => this.descargarArchivo(blob, `ride-${this.id}.pdf`));
  }

  descargarXml(): void {
    this.api.obtenerXml(this.id).subscribe((xml) => {
      const blob = new Blob([xml], { type: 'application/xml' });
      this.descargarArchivo(blob, `comprobante-${this.id}.xml`);
    });
  }

  crearNotaCredito(): void {
    if (!this.ncProductoId || !this.ncBodegaId || !this.ncMotivo.trim()) {
      this.errorNc.set('Complete producto, bodega y motivo.');
      return;
    }
    this.guardandoNc.set(true);
    this.errorNc.set(null);
    this.api
      .crearNotaCredito({
        comprobanteOriginalId: this.id,
        bodegaId: this.ncBodegaId,
        motivo: this.ncMotivo,
        lineas: [{ productoId: this.ncProductoId, cantidad: this.ncCantidad }],
      })
      .subscribe({
        next: () => {
          this.guardandoNc.set(false);
          this.mostrarFormularioNc.set(false);
          this.ncMotivo = '';
        },
        error: (err) => {
          this.guardandoNc.set(false);
          this.errorNc.set(err.error?.mensaje ?? 'No se pudo crear la nota de crédito.');
        },
      });
  }

  private descargarArchivo(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    enlace.click();
    window.URL.revokeObjectURL(url);
  }
}
