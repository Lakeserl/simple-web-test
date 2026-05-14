export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  category: string | null;
  stock: number;
  imageUrl: string | null;
  createdAt: string;
}

export interface ProductRequest {
  name: string;
  description?: string;
  price: number;
  category?: string;
  stock?: number;
  imageUrl?: string;
}
