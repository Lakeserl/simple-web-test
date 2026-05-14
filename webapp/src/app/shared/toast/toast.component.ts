import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast" [class]="'toast-' + toast.type" (click)="toastService.dismiss(toast.id)">
          {{ toast.type === 'success' ? '✅' : '❌' }} {{ toast.message }}
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  constructor(readonly toastService: ToastService) {}
}
