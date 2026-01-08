import {Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ToastComponent} from './shared/components/toast/toast.component';
import {ToastService} from './core/services/toast.service';
import {Observable} from 'rxjs';
import {Toast} from './core/@types/Toast';
import {AsyncPipe} from '@angular/common';
import {ConfirmModalComponent} from './shared/components/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastComponent, AsyncPipe, ConfirmModalComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private toastService = inject(ToastService);
  toasts$: Observable<Toast[]> = this.toastService.getToasts();
}
