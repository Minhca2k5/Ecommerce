import { useCallback, useState } from "react";
import { useToast } from "@/app/ToastProvider";
import { addOrUpdateCartItem, getOrCreateCart } from "@/lib/cartApi";
import { getErrorMessage } from "@/lib/errors";

export function useCartActions() {
  const toast = useToast();
  const [isWorking, setIsWorking] = useState(false);

  const addToCart = useCallback(
    async (productId: number, deltaQuantity: number = 1) => {
      setIsWorking(true);
      try {
        const cart = await getOrCreateCart();
        const cartId = Number(cart.id ?? 0);
        if (!cartId) throw new Error("Missing cartId");
        await addOrUpdateCartItem({ cartId, productId, quantity: deltaQuantity });
        toast.push({ variant: "success", title: "Added to cart", message: "Item added to your cart." });
      } catch (e) {
        toast.push({ variant: "error", title: "Add failed", message: getErrorMessage(e, "Failed to add to cart.") });
      } finally {
        setIsWorking(false);
      }
    },
    [toast],
  );

  return { addToCart, isWorking };
}

