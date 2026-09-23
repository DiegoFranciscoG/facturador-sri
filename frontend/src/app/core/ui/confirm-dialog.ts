import { Component } from '@angular/core';
import { ConfirmService } from '../confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  imports: [],
  templateUrl: './confirm-dialog.html',
})
export class ConfirmDialog {
  constructor(readonly confirmService: ConfirmService) {}
}
