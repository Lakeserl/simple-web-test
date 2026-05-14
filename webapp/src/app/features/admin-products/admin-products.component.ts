import { Component, ChangeDetectionStrategy, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Product, ProductRequest } from '../../models/product.model';
import { ProductService } from '../../core/services/product.service';
import { ToastService } from '../../core/services/toast.service';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [FormsModule, CurrencyPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- Page Header -->
    <div class="page-header">
      <h1 class="page-title">📦 Quản Lí Sản Phẩm</h1>
      <div class="page-toolbar">
        <input class="form-input" style="width: 220px"
               placeholder="🔍 Tìm kiếm..."
               [ngModel]="searchQuery()"
               (ngModelChange)="searchQuery.set($event)"
               (keyup.enter)="loadProducts()">
        <button class="btn btn-ghost" (click)="searchQuery.set(''); loadProducts()">⟳ Làm mới</button>
        <button class="btn btn-primary" (click)="openForm()">+ Thêm sản phẩm</button>
      </div>
    </div>

    <!-- Product Table -->
    <div style="overflow-x: auto;">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên sản phẩm</th>
            <th class="text-right">Giá (₫)</th>
            <th>Danh mục</th>
            <th class="text-center">Tồn kho</th>
            <th class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          @for (p of products(); track p.id) {
            <tr>
              <td class="text-muted">{{ p.id }}</td>
              <td>{{ p.name }}</td>
              <td class="text-right price">{{ p.price | currency:'VND':'symbol':'1.0-0' }}</td>
              <td class="text-muted">{{ p.category || '—' }}</td>
              <td class="text-center">
                <span [style.color]="p.stock > 0 ? 'var(--success)' : 'var(--danger)'">{{ p.stock }}</span>
              </td>
              <td class="text-center">
                <div style="display: flex; gap: 4px; justify-content: center;">
                  <button class="btn btn-icon btn-info btn-sm" title="Xem" (click)="openDetail(p)">👁</button>
                  <button class="btn btn-icon btn-primary btn-sm" title="Sửa" (click)="openForm(p)">✏️</button>
                  <button class="btn btn-icon btn-danger btn-sm" title="Xóa" (click)="confirmDelete(p)">🗑️</button>
                </div>
              </td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="text-center text-muted" style="padding: 40px;">Không có sản phẩm nào</td></tr>
          }
        </tbody>
      </table>
    </div>

    <!-- Product Detail Modal -->
    @if (detailProduct()) {
      <div class="modal-overlay" (click)="detailProduct.set(null)">
        <div class="modal" (click)="$event.stopPropagation()">
          <h2 class="modal-title">📋 Chi tiết sản phẩm</h2>
          <div class="detail-grid">
            <div class="detail-row"><span class="detail-label">ID:</span> <span>{{ detailProduct()!.id }}</span></div>
            <div class="detail-row"><span class="detail-label">Tên:</span> <span>{{ detailProduct()!.name }}</span></div>
            <div class="detail-row"><span class="detail-label">Mô tả:</span> <span>{{ detailProduct()!.description || '—' }}</span></div>
            <div class="detail-row"><span class="detail-label">Giá:</span> <span class="price">{{ detailProduct()!.price | currency:'VND':'symbol':'1.0-0' }}</span></div>
            <div class="detail-row"><span class="detail-label">Danh mục:</span> <span>{{ detailProduct()!.category || '—' }}</span></div>
            <div class="detail-row"><span class="detail-label">Tồn kho:</span> <span>{{ detailProduct()!.stock }}</span></div>
            @if (detailProduct()!.imageUrl) {
              <div class="detail-row"><span class="detail-label">Ảnh:</span></div>
              <img [src]="detailProduct()!.imageUrl" style="width: 100%; max-height: 200px; object-fit: cover; border-radius: 8px; margin-top: 8px;">
            }
          </div>
          <div class="modal-actions">
            <button class="btn btn-ghost" (click)="detailProduct.set(null)">Đóng</button>
          </div>
        </div>
      </div>
    }

    <!-- Product Form Modal (Add / Edit) -->
    @if (showForm()) {
      <div class="modal-overlay" (click)="closeForm()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h2 class="modal-title">{{ editingProduct() ? '✏️ Sửa sản phẩm' : '➕ Thêm sản phẩm' }}</h2>
          <div class="form-group">
            <label class="form-label">Tên sản phẩm *</label>
            <input class="form-input" [(ngModel)]="formData.name" placeholder="Nhập tên sản phẩm">
          </div>
          <div class="form-group">
            <label class="form-label">Mô tả</label>
            <textarea class="form-textarea" [(ngModel)]="formData.description" placeholder="Nhập mô tả"></textarea>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="form-group">
              <label class="form-label">Giá (₫) *</label>
              <input class="form-input" type="number" [(ngModel)]="formData.price" placeholder="0" min="0">
            </div>
            <div class="form-group">
              <label class="form-label">Tồn kho</label>
              <input class="form-input" type="number" [(ngModel)]="formData.stock" placeholder="0" min="0">
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Danh mục</label>
            <input class="form-input" [(ngModel)]="formData.category" placeholder="VD: Điện thoại, Laptop...">
          </div>
          <div class="form-group">
            <label class="form-label">URL hình ảnh</label>
            <input class="form-input" [(ngModel)]="formData.imageUrl" placeholder="https://...">
          </div>
          <div class="modal-actions">
            <button class="btn btn-ghost" (click)="closeForm()">Hủy</button>
            <button class="btn btn-primary" (click)="saveProduct()" [disabled]="saving()">
              {{ saving() ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </div>
      </div>
    }

    <!-- Delete Confirm Modal -->
    @if (deleteTarget()) {
      <div class="modal-overlay" (click)="deleteTarget.set(null)">
        <div class="modal" (click)="$event.stopPropagation()">
          <h2 class="modal-title">⚠️ Xác nhận xóa</h2>
          <p>Bạn có chắc chắn muốn xóa sản phẩm <strong>"{{ deleteTarget()!.name }}"</strong>?</p>
          <div class="modal-actions">
            <button class="btn btn-ghost" (click)="deleteTarget.set(null)">Hủy</button>
            <button class="btn btn-danger" (click)="doDelete()">Xóa</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .detail-grid { display: flex; flex-direction: column; gap: 10px; }
    .detail-row { display: flex; gap: 12px; }
    .detail-label { color: var(--text-secondary); min-width: 80px; font-weight: 500; }
  `]
})
export class AdminProductsComponent implements OnInit {
  readonly products = signal<Product[]>([]);
  readonly searchQuery = signal('');
  readonly detailProduct = signal<Product | null>(null);
  readonly showForm = signal(false);
  readonly editingProduct = signal<Product | null>(null);
  readonly deleteTarget = signal<Product | null>(null);
  readonly saving = signal(false);

  formData: ProductRequest = { name: '', price: 0 };

  constructor(
    private readonly productService: ProductService,
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

  openDetail(product: Product): void {
    this.detailProduct.set(product);
  }

  openForm(product?: Product): void {
    this.editingProduct.set(product ?? null);
    this.formData = product
      ? { name: product.name, description: product.description ?? '', price: product.price, category: product.category ?? '', stock: product.stock, imageUrl: product.imageUrl ?? '' }
      : { name: '', description: '', price: 0, category: '', stock: 0, imageUrl: '' };
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingProduct.set(null);
  }

  saveProduct(): void {
    if (!this.formData.name.trim()) {
      this.toast.error('Vui lòng nhập tên sản phẩm');
      return;
    }
    if (this.formData.price < 0) {
      this.toast.error('Giá không được âm');
      return;
    }

    this.saving.set(true);
    const editing = this.editingProduct();
    const obs = editing
      ? this.productService.update(editing.id, this.formData)
      : this.productService.create(this.formData);

    obs.subscribe({
      next: () => {
        this.toast.success(editing ? 'Cập nhật thành công!' : 'Thêm sản phẩm thành công!');
        this.closeForm();
        this.loadProducts();
        this.saving.set(false);
      },
      error: err => {
        this.toast.error('Lỗi: ' + (err.error?.message || err.message));
        this.saving.set(false);
      },
    });
  }

  confirmDelete(product: Product): void {
    this.deleteTarget.set(product);
  }

  doDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.productService.delete(target.id).subscribe({
      next: () => {
        this.toast.success('Đã xóa sản phẩm "' + target.name + '"');
        this.deleteTarget.set(null);
        this.loadProducts();
      },
      error: err => this.toast.error('Lỗi xóa: ' + (err.error?.message || err.message)),
    });
  }
}
