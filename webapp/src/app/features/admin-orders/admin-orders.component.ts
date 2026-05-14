import { Component, ChangeDetectionStrategy, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Order, OrderStatus } from '../../models/order.model';
import { OrderService } from '../../core/services/order.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-header">
      <h1 class="page-title">📋 Quản Lí Đơn Hàng</h1>
      <button class="btn btn-ghost" (click)="loadOrders()">⟳ Làm mới</button>
    </div>

    <div style="overflow-x: auto;">
      <table class="data-table">
        <thead>
          <tr>
            <th>Mã ĐH</th>
            <th>Khách hàng</th>
            <th>SĐT</th>
            <th class="text-right">Tổng (₫)</th>
            <th class="text-center">Trạng thái</th>
            <th>Ngày tạo</th>
            <th class="text-center">Chi tiết</th>
          </tr>
        </thead>
        <tbody>
          @for (o of orders(); track o.id) {
            <tr>
              <td class="text-muted">#{{ o.id }}</td>
              <td>{{ o.customerName }}</td>
              <td class="text-muted">{{ o.customerPhone || '—' }}</td>
              <td class="text-right price">{{ o.total | currency:'VND':'symbol':'1.0-0' }}</td>
              <td class="text-center">
                <select class="status-select"
                        [class]="'status-select status-' + o.status.toLowerCase()"
                        [ngModel]="o.status"
                        (ngModelChange)="onStatusChange(o, $event)">
                  <option value="PENDING">⏳ Chờ xử lý</option>
                  <option value="CONFIRMED">✓ Đã xác nhận</option>
                  <option value="SHIPPED">🚚 Đang giao</option>
                  <option value="DELIVERED">✅ Đã giao</option>
                  <option value="CANCELLED">✗ Đã hủy</option>
                </select>
              </td>
              <td class="text-muted text-sm">{{ o.createdAt | date:'dd/MM/yyyy HH:mm' }}</td>
              <td class="text-center">
                <button class="btn btn-icon btn-info btn-sm" (click)="openDetail(o)">👁</button>
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="text-center text-muted" style="padding: 40px;">Chưa có đơn hàng nào</td></tr>
          }
        </tbody>
      </table>
    </div>

    <!-- Order Detail Modal -->
    @if (detailOrder()) {
      <div class="modal-overlay" (click)="detailOrder.set(null)">
        <div class="modal" (click)="$event.stopPropagation()" style="min-width: 500px;">
          <h2 class="modal-title">📋 Chi tiết đơn hàng #{{ detailOrder()!.id }}</h2>

          <div class="detail-section">
            <div class="detail-row">
              <span class="detail-label">Khách hàng:</span>
              <span>{{ detailOrder()!.customerName }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">SĐT:</span>
              <span>{{ detailOrder()!.customerPhone || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Trạng thái:</span>
              <span [class]="'badge badge-' + detailOrder()!.status.toLowerCase()">
                {{ statusLabel(detailOrder()!.status) }}
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Ngày tạo:</span>
              <span>{{ detailOrder()!.createdAt | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
          </div>

          <div class="items-section">
            <div class="text-sm text-muted" style="margin-bottom: 8px; font-weight: 600;">Sản phẩm trong đơn:</div>
            <table class="data-table" style="font-size: 0.85rem;">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th class="text-center">SL</th>
                  <th class="text-right">Đơn giá</th>
                  <th class="text-right">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                @for (item of detailOrder()!.items; track item.id) {
                  <tr>
                    <td>{{ item.product.name }}</td>
                    <td class="text-center">{{ item.quantity }}</td>
                    <td class="text-right">{{ item.price | currency:'VND':'symbol':'1.0-0' }}</td>
                    <td class="text-right price">{{ item.price * item.quantity | currency:'VND':'symbol':'1.0-0' }}</td>
                  </tr>
                }
              </tbody>
              <tfoot>
                <tr>
                  <td colspan="3" class="text-right" style="font-weight: 700;">Tổng cộng:</td>
                  <td class="text-right price" style="font-weight: 700;">{{ detailOrder()!.total | currency:'VND':'symbol':'1.0-0' }}</td>
                </tr>
              </tfoot>
            </table>
          </div>

          <div class="modal-actions">
            <button class="btn btn-ghost" (click)="detailOrder.set(null)">Đóng</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .status-select {
      background: var(--bg-input);
      border: 1px solid var(--border);
      border-radius: 20px;
      color: var(--text-primary);
      padding: 4px 10px;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      outline: none;
      font-family: inherit;
    }
    .status-pending { color: var(--warning); }
    .status-confirmed { color: var(--info); }
    .status-shipped { color: var(--accent); }
    .status-delivered { color: var(--success); }
    .status-cancelled { color: var(--danger); }
    .detail-section { display: flex; flex-direction: column; gap: 8px; margin-bottom: 18px; }
    .detail-row { display: flex; gap: 12px; align-items: center; }
    .detail-label { color: var(--text-secondary); min-width: 90px; font-weight: 500; font-size: 0.85rem; }
    .items-section { margin-top: 4px; }
    tfoot td { border-top: 1px solid var(--border); }
  `]
})
export class AdminOrdersComponent implements OnInit {
  readonly orders = signal<Order[]>([]);
  readonly detailOrder = signal<Order | null>(null);

  constructor(
    private readonly orderService: OrderService,
    private readonly toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.findAll().subscribe({
      next: data => this.orders.set(data),
      error: err => this.toast.error('Lỗi tải đơn hàng: ' + (err.error?.message || err.message)),
    });
  }

  onStatusChange(order: Order, newStatus: OrderStatus): void {
    this.orderService.updateStatus(order.id, newStatus).subscribe({
      next: () => {
        this.toast.success('Cập nhật trạng thái đơn #' + order.id + ' thành công');
        this.loadOrders();
      },
      error: err => this.toast.error('Lỗi: ' + (err.error?.message || err.message)),
    });
  }

  openDetail(order: Order): void {
    this.orderService.findById(order.id).subscribe({
      next: data => this.detailOrder.set(data),
      error: err => this.toast.error('Lỗi: ' + (err.error?.message || err.message)),
    });
  }

  statusLabel(status: OrderStatus): string {
    const labels: Record<OrderStatus, string> = {
      PENDING: '⏳ Chờ xử lý',
      CONFIRMED: '✓ Đã xác nhận',
      SHIPPED: '🚚 Đang giao',
      DELIVERED: '✅ Đã giao',
      CANCELLED: '✗ Đã hủy',
    };
    return labels[status] ?? status;
  }
}
