import { Component } from '@angular/core';
import { NotificationService } from '../notification.service';

@Component({
  selector: 'app-toast-host',
  imports: [],
  templateUrl: './toast-host.html',
})
export class ToastHost {
  constructor(readonly notifications: NotificationService) {}
}
