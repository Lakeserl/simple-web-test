import { Component, ChangeDetectionStrategy, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { Product } from '../../models/product.model';
import { ProductService } from '../../core/services/product.service';
import { OrderService } from '../../core/services/order.service';
import { ToastService } from '../../core/services/toast.service';
import { OrderItemRequest } from '../../models/order.model';

interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
}

@Component({
  selector: 'app-shop',
  standalone: true,
  imports: [FormsModule, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="shop-layout">
      <!-- Left: Products -->
      <div class="shop-products">
        <div class="page-header">
          <h1 class="page-title">🛒 Cửa Hàng</h1>
          <input class="form-input" style="width: 240px"
                 placeholder="🔍 Tìm sản phẩm..."
                 [ngModel]="searchQuery()"
                 (ngModelChange)="searchQuery.set($event)"
                 (keyup.enter)="loadProducts()">
        </div>

        <div class="products-grid">
          @for (p of products(); track p.id) {
            <div class="card product-card">
              <div class="product-info">
                <div class="product-name">{{ p.name }}</div>
                @if (p.category) {
                  <div class="text-sm text-muted">{{ p.category }}</div>
                }
                <div class="price" style="margin-top: 6px;">{{ p.price | currency:'VND':'symbol':'1.0-0' }}</div>
                <div class="text-sm mt-1" [style.color]="p.stock > 0 ? 'var(--success)' : 'var(--danger)'">
                  Kho: {{ p.stock }}
                </div>
              </div>
              <button class="btn btn-primary btn-icon add-btn"
                      [disabled]="p.stock <= 0"
                      (click)="addToCart(p)"
                      title="Thêm vào giỏ">+</button>
            </div>
          } @empty {
            <div class="text-muted" style="padding: 40px; text-align: center; grid-column: 1/-1;">
              Không tìm thấy sản phẩm
            </div>
          }
        </div>
      </div>

      <!-- Right: Cart -->
      <div class="shop-cart">
        <h2 class="cart-title">🧺 Giỏ hàng</h2>

        <div class="cart-items">
          @for (item of cart(); track item.productId) {
            <div class="cart-item">
              <div class="cart-item-info">
                <div class="text-sm">{{ item.name }}</div>
                <div class="text-sm text-muted">
                  {{ item.price | currency:'VND':'symbol':'1.0-0' }} × {{ item.quantity }}
                </div>
              </div>
              <div class="cart-item-actions">
                <button class="btn-qty" (click)="updateQty(item, -1)">−</button>
                <span class="qty-display">{{ item.quantity }}</span>
                <button class="btn-qty" (click)="updateQty(item, 1)">+</button>
                <button class="btn-remove" (click)="removeFromCart(item)">✕</button>
              </div>
            </div>
          } @empty {
            <div class="text-muted text-sm" style="padding: 20px; text-align: center;">Giỏ hàng trống</div>
          }
        </div>

        <div class="cart-footer">
          <div class="cart-total">Tổng: {{ cartTotal() | currency:'VND':'symbol':'1.0-0' }}</div>
          <button class="btn btn-primary" style="width: 100%" (click)="openCheckout()" [disabled]="cart().length === 0">
            📝 Đặt hàng
          </button>
        </div>
      </div>
    </div>

    <!-- Checkout Modal -->
    @if (showCheckout()) {
      <div class="modal-overlay" (click)="showCheckout.set(false)">
        <div class="modal" (click)="$event.stopPropagation()">
          <h2 class="modal-title">📝 Thông tin đặt hàng</h2>
          <div class="form-group">
            <label class="form-label">Tên khách hàng *</label>
            <input class="form-input" [(ngModel)]="customerName" placeholder="Nhập tên khách hàng">
          </div>
          <div class="form-group">
            <label class="form-label">Số điện thoại</label>
            <input class="form-input" [(ngModel)]="customerPhone" placeholder="Nhập SĐT">
          </div>
          <div class="order-summary">
            <div class="text-sm text-muted" style="margin-bottom: 8px;">Đơn hàng:</div>
            @for (item of cart(); track item.productId) {
              <div class="summary-row">
                <span>{{ item.name }} × {{ item.quantity }}</span>
                <span class="price">{{ item.price * item.quantity | currency:'VND':'symbol':'1.0-0' }}</span>
              </div>
            }
            <div class="summary-row" style="border-top: 1px solid var(--border); padding-top: 8px; margin-top: 8px;">
              <strong>Tổng cộng</strong>
              <strong class="price">{{ cartTotal() | currency:'VND':'symbol':'1.0-0' }}</strong>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn btn-ghost" (click)="showCheckout.set(false)">Hủy</button>
            <button class="btn btn-primary" (click)="placeOrder()" [disabled]="submitting()">
              {{ submitting() ? 'Đang xử lý...' : '✅ Xác nhận đặt hàng' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .shop-layout {
      display: flex;
      gap: 0;
      height: calc(100vh - 48px);
      margin: -24px;
    }
    .shop-products {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
    }
    .shop-cart {
      width: 300px;
      min-width: 300px;
      background: var(--bg-panel);
      border-left: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      padding: 20px 16px;
    }
    .cart-title {
      font-size: 1.1rem;
      font-weight: 700;
      margin-bottom: 12px;
    }
    .products-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
    }
    .product-card {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .product-name { font-weight: 600; font-size: 0.95rem; }
    .add-btn { flex-shrink: 0; width: 40px; height: 40px; font-size: 1.2rem; }
    .cart-items { flex: 1; overflow-y: auto; }
    .cart-item {
      background: var(--bg-card);
      border-radius: 6px;
      padding: 8px 10px;
      margin-bottom: 6px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }
    .cart-item-info { flex: 1; min-width: 0; }
    .cart-item-actions { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
    .btn-qty {
      width: 24px; height: 24px; border: none; border-radius: 4px;
      background: var(--bg-input); color: var(--text-primary);
      cursor: pointer; font-size: 0.85rem; display: flex; align-items: center; justify-content: center;
    }
    .btn-qty:hover { background: var(--accent); }
    .qty-display { font-size: 0.85rem; min-width: 20px; text-align: center; }
    .btn-remove {
      background: none; border: none; color: var(--danger);
      cursor: pointer; font-size: 0.85rem; padding: 2px 4px;
    }
    .cart-footer {
      border-top: 1px solid var(--border);
      padding-top: 12px;
    }
    .cart-total {
      font-size: 1.05rem;
      font-weight: 700;
      margin-bottom: 10px;
    }
    .order-summary {
      background: var(--bg-card);
      border-radius: 8px;
      padding: 12px;
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.85rem;
      margin-bottom: 4px;
    }
  `]
})
export class ShopComponent implements OnInit {
  readonly products = signal<Product[]>([]);
  readonly searchQuery = signal('');
  readonly cart = signal<CartItem[]>([]);
  readonly showCheckout = signal(false);
  readonly submitting = signal(false);

  customerName = '';
  customerPhone = '';

  readonly cartTotal = () => this.cart().reduce((sum, item) => sum + item.price * item.quantity, 0);

  constructor(
    private readonly productService: ProductService,
    private readonly orderService: OrderService,
    private readonly toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    const search = this.searchQuery().trim();
    this.productService.findAll(search || undefined).subscribe({
      next: data => this.products.set(data),
      error: err => this.toast.error('Lỗi tải sản phẩm: ' + (err.error?.message || err.message)),
    });
  }

  addToCart(product: Product): void {
    this.cart.update(items => {
      const existing = items.find(i => i.productId === product.id);
      if (existing) {
        return items.map(i =>
          i.productId === product.id ? { ...i, quantity: i.quantity + 1 } : i
        );
      }
      return [...items, { productId: product.id, name: product.name, price: product.price, quantity: 1 }];
    });
  }

  updateQty(item: CartItem, delta: number): void {
    const newQty = item.quantity + delta;
    if (newQty <= 0) {
      this.removeFromCart(item);
      return;
    }
    this.cart.update(items =>
      items.map(i => i.productId === item.productId ? { ...i, quantity: newQty } : i)
    );
  }

  removeFromCart(item: CartItem): void {
    this.cart.update(items => items.filter(i => i.productId !== item.productId));
  }

  openCheckout(): void {
    this.customerName = '';
    this.customerPhone = '';
    this.showCheckout.set(true);
  }

  placeOrder(): void {
    if (!this.customerName.trim()) {
      this.toast.error('Vui lòng nhập tên khách hàng');
      return;
    }

    this.submitting.set(true);
    const items: OrderItemRequest[] = this.cart().map(i => ({
      productId: i.productId,
      quantity: i.quantity,
    }));

    this.orderService.create({
      customerName: this.customerName.trim(),
      customerPhone: this.customerPhone.trim() || undefined,
      items,
    }).subscribe({
      next: order => {
        this.toast.success('✅ Đặt hàng thành công! Mã đơn: #' + order.id);
        this.cart.set([]);
        this.showCheckout.set(false);
        this.submitting.set(false);
        this.loadProducts();
      },
      error: err => {
        this.toast.error('Lỗi đặt hàng: ' + (err.error?.message || err.message));
        this.submitting.set(false);
      },
    });
  }
}
