# P5 M4.2 - Checkout Fraud/Abuse Baseline

## Goal
Add a lightweight protection layer for checkout abuse bursts without changing current checkout contract.

## What was implemented
- Added Redis-backed failure counter per checkout scope:
  - `user:{userId}:ip:{clientIp}` for authenticated checkout
  - `guest:{guestId}:ip:{clientIp}` for guest checkout
- Added block rule:
  - if failed attempts in active window reach threshold, return `429 Too Many Requests`
- Added reset behavior:
  - successful checkout clears current scope failure counter
- Added config:
  - `checkout.abuse.enabled`
  - `checkout.abuse.max-failures`
  - `checkout.abuse.window-minutes`

## Code references
- `backend/src/main/java/com/minzetsu/ecommerce/order/service/CheckoutAbuseService.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/CheckoutAbuseServiceImpl.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/controller/user/UserOrderController.java`
- `backend/src/main/java/com/minzetsu/ecommerce/order/controller/pub/PublicGuestCheckoutController.java`
- `backend/src/main/resources/application.properties`

## Verification (manual)
1. Send invalid checkout request (wrong cart/invalid payload) for same scope repeatedly.
2. After threshold (default 5), next request should return HTTP `429`.
3. Wait for window timeout (default 10 minutes) or use a successful checkout in same scope.
4. Verify request is accepted again (not blocked by abuse guard).

## Notes
- This is baseline anti-abuse; it complements existing rate limit filter.
- If Redis is unavailable, guard degrades gracefully (warn log, no hard block) to avoid checkout outage.
