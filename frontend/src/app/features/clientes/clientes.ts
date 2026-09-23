import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Cliente, ClienteRequest } from '../../core/models';

@Component({
  selector: 'app-clientes',
  imports: [FormsModule],
  templateUrl: './clientes.html',
})
export class Clientes implements OnInit {
  clientes = signal<Cliente[]>([]);
  mostrarFormulario = signal(false);
  guardando = signal(false);
  error = signal<string | null>(null);

  form: ClienteRequest = this.formularioVacio();

  readonly tiposIdentificacion = [
    { codigo: '04', nombre: 'RUC' },
    { codigo: '05', nombre: 'Cédula' },
    { codigo: '06', nombre: 'Pasaporte' },
    { codigo: '07', nombre: 'Consumidor final' },
    { codigo: '08', nombre: 'Identificación del exterior' },
  ];

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.listarClientes().subscribe((pagina) => this.clientes.set(pagina.content));
  }

  nuevo(): void {
    this.form = this.formularioVacio();
    this.error.set(null);
    this.mostrarFormulario.set(true);
  }

  guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    this.api.crearCliente(this.form).subscribe({
      next: () => {
        this.guardando.set(false);
        this.mostrarFormulario.set(false);
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar el cliente.');
      },
    });
  }

  eliminar(cliente: Cliente): void {
    if (!confirm(`¿Eliminar a ${cliente.razonSocial}?`)) {
      return;
    }
    this.api.eliminarCliente(cliente.id).subscribe(() => this.cargar());
  }

  private formularioVacio(): ClienteRequest {
    return { tipoIdentificacion: '05', identificacion: '', razonSocial: '', direccion: '', email: '', telefono: '' };
  }
}
