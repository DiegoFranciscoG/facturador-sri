import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Comprobante, EstadoComprobante } from '../../core/models';

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

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api
      .listarComprobantes(0, 50, this.filtroEstado() || undefined)
      .subscribe((pagina) => this.comprobantes.set(pagina.content));
  }

  cambiarFiltro(estado: string): void {
    this.filtroEstado.set(estado as EstadoComprobante | '');
    this.cargar();
  }
}
