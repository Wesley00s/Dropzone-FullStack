import {Component, inject} from '@angular/core';
import {ConfirmService} from '../../../core/services/confirm.service';

@Component({
  selector: 'app-confirm-modal',
  imports: [],
  templateUrl: './confirm-modal.component.html',
  styleUrl: './confirm-modal.component.css',
})
export class ConfirmModalComponent {
  confirmService = inject(ConfirmService);

  get state() {
    return this.confirmService.state();
  }

  onBackdropClick(event: MouseEvent) {
    if (event.target === event.currentTarget) {
      this.confirmService.cancel();
    }
  }
}
