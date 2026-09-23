import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/error.util';
import { Bodega, Comprobante, Producto } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

const CANTIDAD_MAXIMA = 999999;

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

  readonly cantidadMaxima = CANTIDAD_MAXIMA;

  private id!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly api: ApiService,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
    this.api.listarProductos(0, 100).subscribe({
      next: (p) => this.productos.set(p.content),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar los productos.')),
    });
    this.api.listarBodegas().subscribe({
      next: (b) => {
        this.bodegas.set(b);
        this.ncBodegaId = b[0]?.id;
      },
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar las bodegas.')),
    });
  }

  cargar(): void {
    this.api.obtenerComprobante(this.id).subscribe({
      next: (c) => this.comprobante.set(c),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudo cargar el comprobante.')),
    });
  }

  descargarRide(): void {
    this.api.descargarRide(this.id).subscribe({
      next: (blob) => this.descargarArchivo(blob, `ride-${this.id}.pdf`),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudo descargar el RIDE.')),
    });
  }

  descargarXml(): void {
    this.api.obtenerXml(this.id).subscribe({
      next: (xml) => {
        const blob = new Blob([xml], { type: 'application/xml' });
        this.descargarArchivo(blob, `comprobante-${this.id}.xml`);
      },
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudo descargar el XML.')),
    });
  }

  crearNotaCredito(): void {
    if (!this.ncProductoId) {
      this.errorNc.set('Seleccione un producto a acreditar.');
      return;
    }
    if (!this.ncBodegaId) {
      this.errorNc.set('Seleccione la bodega de devolución.');
      return;
    }
    if (!this.ncCantidad || this.ncCantidad <= 0 || this.ncCantidad > CANTIDAD_MAXIMA) {
      this.errorNc.set(`La cantidad debe estar entre 1 y ${CANTIDAD_MAXIMA}.`);
      return;
    }
    if (!this.ncMotivo.trim() || this.ncMotivo.trim().length < 5) {
      this.errorNc.set('El motivo debe tener al menos 5 caracteres.');
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
        next: (nota) => {
          this.guardandoNc.set(false);
          this.mostrarFormularioNc.set(false);
          this.ncMotivo = '';
          this.notifications.exito(`Nota de crédito ${nota.secuencial} emitida.`);
        },
        error: (err) => {
          this.guardandoNc.set(false);
          const mensaje = mensajeDeError(err, 'No se pudo crear la nota de crédito.');
          this.errorNc.set(mensaje);
          this.notifications.error(mensaje);
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
