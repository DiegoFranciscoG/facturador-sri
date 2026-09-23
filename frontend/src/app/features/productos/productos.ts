import { DecimalPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { CatalogoItem, Producto, ProductoRequest } from '../../core/models';

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

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
    this.api.catalogo('CODIGO_PORCENTAJE_IVA').subscribe((items) => this.tarifasIva.set(items));
  }

  cargar(): void {
    this.api.listarProductos().subscribe((pagina) => this.productos.set(pagina.content));
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
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el producto.');
      },
    });
  }

  desactivar(producto: Producto): void {
    if (!confirm(`¿Desactivar ${producto.descripcion}?`)) {
      return;
    }
    this.api.desactivarProducto(producto.id).subscribe(() => this.cargar());
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
