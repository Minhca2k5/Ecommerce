# P5-M4.1: Guest Checkout

## Implemented
- New public endpoint:
  - `POST /api/public/checkout/guest/{guestId}`
- Creates order from guest cart without requiring login.
- Uses idempotency key support and existing order pipeline.
- Enforces cart ownership checks:
  - guest flow requires matching `guestId` and guest cart.
  - user flow now rejects carts not owned by current user.

## Key files
- `backend/src/main/java/com/minzetsu/ecommerce/order/controller/pub/PublicGuestCheckoutController.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/OrderServiceImpl.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/service/GuestCheckoutIdentityService.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/config/GuestCheckoutProperties.java`

## Verify quickly
1. Create guest cart:
   - `POST /api/public/carts/guest`
2. Add items to guest cart.
3. Checkout:
   - `POST /api/public/checkout/guest/{guestId}`
4. Expected:
   - `201` with order response.
   - no login required.
