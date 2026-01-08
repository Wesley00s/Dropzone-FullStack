import { Component, inject, input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Toast } from '../../../core/@types/Toast';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css'
})
export class ToastComponent implements OnInit {
  toast = input.required<Toast>();
  isVisible = signal(false);

  private toastService = inject(ToastService);

  ngOnInit() {
    requestAnimationFrame(() => {
      this.isVisible.set(true);
    });
  }

  public toastClose(): void {
    this.isVisible.set(false);

    setTimeout(() => {
      this.toastService.remove(this.toast().id);
    }, 300);
  }
}
