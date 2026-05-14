import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ToastComponent } from './shared/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <nav class="sidebar">
        <div class="sidebar-logo">🛍️ ShopManager</div>
        <div class="sidebar-nav">
          <a routerLink="/admin/products" routerLinkActive="active" class="nav-btn">
            📦 Quản Lí Sản Phẩm
          </a>
          <a routerLink="/shop" routerLinkActive="active" class="nav-btn">
            🛒 Cửa Hàng
          </a>
          <a routerLink="/admin/orders" routerLinkActive="active" class="nav-btn">
            📋 Đơn Hàng
          </a>
        </div>
      </nav>
      <main class="content">
        <router-outlet />
      </main>
    </div>
    <app-toast />
  `,
  styles: [`
    .app-layout {
      display: flex;
      height: 100vh;
      background: var(--bg-dark);
    }
    .sidebar {
      width: 220px;
      min-width: 220px;
      background: var(--bg-panel);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
    }
    .sidebar-logo {
      font-size: 1.15rem;
      font-weight: 700;
      color: var(--accent);
      padding: 20px 16px;
    }
    .sidebar-nav {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 4px 10px;
    }
    .nav-btn {
      display: block;
      padding: 10px 14px;
      border-radius: 6px;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.9rem;
      font-weight: 500;
      transition: background 0.15s, color 0.15s;
    }
    .nav-btn:hover {
      background: var(--bg-card);
      color: var(--text-primary);
    }
    .nav-btn.active {
      background: var(--accent);
      color: #fff;
    }
    .content {
      flex: 1;
      overflow-y: auto;
      padding: 24px;
    }
  `]
})
export class AppComponent {}
