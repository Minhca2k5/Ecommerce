import { create } from "zustand";

interface CartItem {
    productId: string;
    name: string;
    price: number;
    quantity: number;
    image?: string;
}

interface CartState {
    items: CartItem[];
    count: number;
    addItem: (item: CartItem) => void;
    removeItem: (productId: string) => void;
    updateQuantity: (productId: string, quantity: number) => void;
    clearCart: () => void;
}

export const useCartStore = create<CartState>((set) => ({
    items: [],
    count: 0,
    addItem: (item) =>
        set((state) => {
            const existing = state.items.find((i) => i.productId === item.productId);
            if (existing) {
                const updated = state.items.map((i) =>
                    i.productId === item.productId
                        ? { ...i, quantity: i.quantity + item.quantity }
                        : i,
                );
                return {
                    items: updated,
                    count: updated.reduce((sum, i) => sum + i.quantity, 0),
                };
            }
            const items = [...state.items, item];
            return {
                items,
                count: items.reduce((sum, i) => sum + i.quantity, 0),
            };
        }),
    removeItem: (productId) =>
        set((state) => {
            const items = state.items.filter((i) => i.productId !== productId);
            return {
                items,
                count: items.reduce((sum, i) => sum + i.quantity, 0),
            };
        }),
    updateQuantity: (productId, quantity) =>
        set((state) => {
            const items = state.items.map((i) =>
                i.productId === productId ? { ...i, quantity } : i,
            );
            return {
                items,
                count: items.reduce((sum, i) => sum + i.quantity, 0),
            };
        }),
    clearCart: () => set({ items: [], count: 0 }),
}));
