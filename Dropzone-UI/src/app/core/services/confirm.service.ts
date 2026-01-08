import { Injectable, signal } from '@angular/core';

export interface ConfirmState {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmService {
  state = signal<ConfirmState>({
    isOpen: false,
    title: '',
    message: '',
    confirmText: 'Confirmar',
    cancelText: 'Cancelar'
  });

  private resolveRef: ((value: boolean) => void) | null = null;

  ask(title: string, message: string, confirmText = 'Confirmar', cancelText = 'Cancelar'): Promise<boolean> {
    this.state.set({
      isOpen: true,
      title,
      message,
      confirmText,
      cancelText
    });

    return new Promise((resolve) => {
      this.resolveRef = resolve;
    });
  }

  confirm() {
    this.close(true);
  }

  cancel() {
    this.close(false);
  }

  private close(result: boolean) {
    this.state.update(s => ({ ...s, isOpen: false }));

    if (this.resolveRef) {
      this.resolveRef(result);
      this.resolveRef = null;
    }
  }
}
