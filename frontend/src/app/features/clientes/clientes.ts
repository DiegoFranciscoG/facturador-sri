import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { ConfirmService } from '../../core/confirm.service';
import { mensajeDeError } from '../../core/error.util';
import { Cliente, ClienteRequest } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

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

  constructor(
    private readonly api: ApiService,
    private readonly notifications: NotificationService,
    private readonly confirmService: ConfirmService,
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.listarClientes().subscribe({
      next: (pagina) => this.clientes.set(pagina.content),
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudieron cargar los clientes.')),
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
    this.api.crearCliente(this.form).subscribe({
      next: () => {
        this.guardando.set(false);
        this.mostrarFormulario.set(false);
        this.notifications.exito('Cliente creado correctamente.');
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        const mensaje = mensajeDeError(err, 'No se pudo guardar el cliente.');
        this.error.set(mensaje);
        this.notifications.error(mensaje);
      },
    });
  }

  async eliminar(cliente: Cliente): Promise<void> {
    const confirmado = await this.confirmService.pedir(
      'Eliminar cliente',
      `¿Eliminar a ${cliente.razonSocial}? Esta acción no se puede deshacer.`,
    );
    if (!confirmado) {
      return;
    }
    this.api.eliminarCliente(cliente.id).subscribe({
      next: () => {
        this.notifications.exito('Cliente eliminado.');
        this.cargar();
      },
      error: (err) => this.notifications.error(mensajeDeError(err, 'No se pudo eliminar el cliente.')),
    });
  }

  private formularioVacio(): ClienteRequest {
    return { tipoIdentificacion: '05', identificacion: '', razonSocial: '', direccion: '', email: '', telefono: '' };
  }
}
