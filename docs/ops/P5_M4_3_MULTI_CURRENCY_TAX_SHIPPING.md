# P5 M4.3 - Multi-Currency + Tax/Shipping Rules

## Goal
Make checkout total calculation deterministic with explicit pricing breakdown: subtotal, discount, shipping fee, tax, total.

## What was implemented
- Added configurable pricing policy (`checkout.pricing.*`) with:
  - exchange rates
  - tax rates
  - flat shipping fees
  - default currency
- Added pricing breakdown fields on `orders`:
  - `subtotal_amount`
  - `shipping_fee`
  - `tax_amount`
- Checkout calculation pipeline now:
  1. Calculate subtotal from cart items (base VND prices)
  2. Apply voucher discount
  3. Convert to target currency by configured exchange rate
  4. Apply shipping fee (request override or configured default)
  5. Apply tax rate
  6. Persist and return full breakdown
- `OrderResponse` now returns:
  - `subtotalAmount`
  - `discountAmount`
  - `shippingFee`
  - `taxAmount`
  - `totalAmount`
  - `currency`

## Code references
- `backend/src/main/java/com/minzetsu/ecommerce/order/config/CheckoutPricingProperties.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/OrderServiceImpl.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/entity/Order.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/dto/response/OrderResponse.java`
- `backend/src/main/resources/db/changelog/v14__order_pricing_breakdown.xml`
- `backend/src/main/resources/application.properties`

## Verification (manual)
1. Create two orders with same cart/items but different `currency` (e.g., `VND` and `USD`).
2. Confirm response includes breakdown fields and deterministic formula:
   - `totalAmount = max(subtotalAmount - discountAmount, 0) + shippingFee + taxAmount`
3. Omit `shippingFee` in request and confirm configured flat fee is applied.
4. Check DB row in `orders` has `subtotal_amount`, `shipping_fee`, `tax_amount`.
