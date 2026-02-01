import { useCallback, useState } from "react";
import { useToast } from "@/app/ToastProvider";
import { useAuth } from "@/app/AuthProvider";
import {
  addOrUpdateCartItem,
  addOrUpdateGuestCartItem,
  getOrCreateCart,
  getOrCreateGuestCart,
  getStoredGuestId,
  setStoredGuestId,
} from "@/lib/cartApi";
import { getErrorMessage } from "@/lib/errors";

export function useCartActions() {
  const toast = useToast();
  const auth = useAuth();
  const [isWorking, setIsWorking] = useState(false);

  const addToCart = useCallback(
    async (productId: number, deltaQuantity: number = 1) => {
      setIsWorking(true);
      try {
        if (auth.isAuthenticated) {
          const cart = await getOrCreateCart();
          const cartId = Number(cart.id ?? 0);
          if (!cartId) throw new Error("Missing cartId");
          await addOrUpdateCartItem({ cartId, productId, quantity: deltaQuantity });
        } else {
          const guestCart = await getOrCreateGuestCart();
          const guestId = guestCart.guestId ?? getStoredGuestId();
          if (!guestId) throw new Error("Missing guestId");
          setStoredGuestId(guestId);
          await addOrUpdateGuestCartItem(guestId, productId, deltaQuantity);
        }
        toast.push({ variant: "success", title: "Added to cart", message: "Item added to your cart." });
        window.dispatchEvent(new CustomEvent("cart:changed"));
      } catch (e) {
        toast.push({ variant: "error", title: "Add failed", message: getErrorMessage(e, "Failed to add to cart.") });
      } finally {
        setIsWorking(false);
      }
    },
    [auth.isAuthenticated, toast],
  );

  return { addToCart, isWorking };
}
