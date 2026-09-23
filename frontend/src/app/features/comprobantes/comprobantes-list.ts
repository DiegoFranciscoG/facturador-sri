import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeDeError } from '../../core/error.util';
import { Comprobante, EstadoComprobante } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-comprobantes-list',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './comprobantes-list.html',
})
export class ComprobantesList implements OnInit {
  comprobantes = signal<Comprobante[]>([]);
  filtroEstado = signal<EstadoComprobante | ''>('');

  readonly estados: EstadoComprobante[] = [
    'GENERADO',
    'FIRMADO',
    'RECIBIDO',
    'DEVUELTA',
    'AUTORIZADO',
    'NO_AUTORIZADO',
  ];

  constructor(
    private readonly api: ApiService,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.listarComprobantes(0, 50, this.filtroEstado() || undefined).subscribe({
      next: (pagina) => this.comprobantes.set(pagina.content),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar los comprobantes.')),
    });
  }

  cambiarFiltro(estado: string): void {
    this.filtroEstado.set(estado as EstadoComprobante | '');
    this.cargar();
  }
}
