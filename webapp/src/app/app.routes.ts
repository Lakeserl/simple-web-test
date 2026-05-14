import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'admin/products', pathMatch: 'full' },
  {
    path: 'admin/products',
    loadComponent: () =>
      import('./features/admin-products/admin-products.component').then(m => m.AdminProductsComponent),
  },
  {
    path: 'shop',
    loadComponent: () =>
      import('./features/shop/shop.component').then(m => m.ShopComponent),
  },
  {
    path: 'admin/orders',
    loadComponent: () =>
      import('./features/admin-orders/admin-orders.component').then(m => m.AdminOrdersComponent),
  },
];
