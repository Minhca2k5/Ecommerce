import http from 'k6/http';
import { check, sleep } from 'k6';
import { getEnv } from './env.js';

const BASE_URL = getEnv('K6_BASE_URL', 'http://localhost:8080');
const USER_TOKEN = getEnv('K6_USER_TOKEN', '');
const PRODUCT_ID = getEnv('K6_PRODUCT_ID', '');
const ADDRESS_ID = getEnv('K6_ADDRESS_ID', '');

if (!USER_TOKEN) {
  throw new Error('K6_USER_TOKEN is required for user-mixed.js');
}

const headers = {
  Authorization: `Bearer ${USER_TOKEN}`,
  'Content-Type': 'application/json',
};

export const options = {
  vus: 10,
  duration: '2m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
  },
};

function getOrCreateCartId() {
  const res = http.get(`${BASE_URL}/api/users/me/carts`, { headers });
  if (res.status === 200) {
    const body = res.json();
    return body.id;
  }
  const createRes = http.post(`${BASE_URL}/api/users/me/carts`, null, { headers });
  check(createRes, { 'create cart 200': (r) => r.status === 200 });
  const created = createRes.json();
  return created.id;
}

export default function () {
  const cartId = getOrCreateCartId();

  if (PRODUCT_ID) {
    const addBody = JSON.stringify({
      cartId: Number(cartId),
      productId: Number(PRODUCT_ID),
      quantity: 1,
    });
    const addRes = http.post(`${BASE_URL}/api/users/me/carts/${cartId}/items`, addBody, { headers });
    check(addRes, { 'add cart item 201': (r) => r.status === 201 || r.status === 200 });
  }

  if (ADDRESS_ID) {
    const orderBody = JSON.stringify({
      cartId: Number(cartId),
      addressIdSnapshot: Number(ADDRESS_ID),
      shippingFee: 0,
      currency: 'VND',
      status: 'PENDING',
    });
    const orderRes = http.post(`${BASE_URL}/api/users/me/orders`, orderBody, { headers });
    check(orderRes, { 'create order 201': (r) => r.status === 201 });
  }

  const ordersRes = http.get(`${BASE_URL}/api/users/me/orders`, { headers });
  check(ordersRes, { 'orders 200': (r) => r.status === 200 });

  sleep(1);
}
