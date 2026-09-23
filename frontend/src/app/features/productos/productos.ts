import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { ConfirmService } from '../../core/confirm.service';
import { mensajeDeError } from '../../core/error.util';
import { CatalogoItem, Producto, ProductoRequest } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-productos',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './productos.html',
})
export class Productos implements OnInit {
  productos = signal<Producto[]>([]);
  tarifasIva = signal<CatalogoItem[]>([]);
  mostrarFormulario = signal(false);
  guardando = signal(false);
  error = signal<string | null>(null);

  form: ProductoRequest = this.formularioVacio();

  constructor(
    private readonly api: ApiService,
    private readonly notifications: NotificationService,
    private readonly confirmService: ConfirmService,
  ) {}

  ngOnInit(): void {
    this.cargar();
    this.api.catalogo('CODIGO_PORCENTAJE_IVA').subscribe({
      next: (items) => this.tarifasIva.set(items),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar las tarifas de IVA.')),
    });
  }

  cargar(): void {
    this.api.listarProductos().subscribe({
      next: (pagina) => this.productos.set(pagina.content),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar los productos.')),
    });
  }

  nuevo(): void {
    this.form = this.formularioVacio();
    this.error.set(null);
    this.mostrarFormulario.set(true);
  }

  guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    this.api.crearProducto(this.form).subscribe({
      next: () => {
        this.guardando.set(false);
        this.mostrarFormulario.set(false);
        this.notifications.exito('Producto creado correctamente.');
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        const mensaje = mensajeDeError(err, 'No se pudo guardar el producto.');
        this.error.set(mensaje);
        this.notifications.error(mensaje);
      },
    });
  }

  async desactivar(producto: Producto): Promise<void> {
    const confirmado = await this.confirmService.pedir(
      'Desactivar producto',
      `¿Desactivar "${producto.descripcion}"? Ya no podrá usarse en nuevas ventas.`,
    );
    if (!confirmado) {
      return;
    }
    this.api.desactivarProducto(producto.id).subscribe({
      next: () => {
        this.notifications.exito('Producto desactivado.');
        this.cargar();
      },
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudo desactivar el producto.')),
    });
  }

  private formularioVacio(): ProductoRequest {
    return {
      codigoPrincipal: '',
      codigoAuxiliar: '',
      descripcion: '',
      unidadMedida: 'UNIDAD',
      precioUnitario: 0,
      codigoImpuesto: '2',
      codigoPorcentajeIva: '4',
    };
  }
}
