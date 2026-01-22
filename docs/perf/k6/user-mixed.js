import http from 'k6/http';
import { check, sleep } from 'k6';
import { getEnv } from './env.js';

const BASE_URL = getEnv('K6_BASE_URL', 'http://localhost:8080');
const USER_TOKEN = getEnv('K6_USER_TOKEN', '');
const USER_EMAIL = getEnv('K6_USER_EMAIL', '');
const USER_PASSWORD = getEnv('K6_USER_PASSWORD', '');
const PRODUCT_ID = getEnv('K6_PRODUCT_ID', '');
const ADDRESS_ID = getEnv('K6_ADDRESS_ID', '');

if (!USER_TOKEN && (!USER_EMAIL || !USER_PASSWORD)) {
  throw new Error('K6_USER_TOKEN or K6_USER_EMAIL/K6_USER_PASSWORD is required for user-mixed.js');
}

let cachedToken = null;

function resolveToken() {
  if (cachedToken) return cachedToken;
  if (USER_TOKEN) {
    cachedToken = USER_TOKEN.startsWith('Bearer ') ? USER_TOKEN : `Bearer ${USER_TOKEN}`;
    return cachedToken;
  }
  const loginBody = JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD });
  const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginBody, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(loginRes, { 'login 200': (r) => r.status === 200 });
  if (!loginRes.body || loginRes.body.length === 0) {
    throw new Error(`Login failed (status ${loginRes.status})`);
  }
  const payload = loginRes.json();
  if (!payload || !payload.accessToken) {
    throw new Error('Login response missing accessToken');
  }
  const tokenType = payload.tokenType || 'Bearer';
  cachedToken = `${tokenType} ${payload.accessToken}`;
  return cachedToken;
}

function buildHeaders() {
  return {
    Authorization: resolveToken(),
    'Content-Type': 'application/json',
  };
}

export const options = {
  vus: 10,
  duration: '2m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
  },
};

function getOrCreateCartId() {
  const headers = buildHeaders();
  const res = http.get(`${BASE_URL}/api/users/me/carts`, { headers });
  if (res.status === 200) {
    if (res.body && res.body.length > 0) {
      const body = res.json();
      if (body && body.id) return body.id;
    }
  }
  const createRes = http.post(`${BASE_URL}/api/users/me/carts`, null, { headers });
  check(createRes, { 'create cart 200/201': (r) => r.status === 200 || r.status === 201 });
  if (createRes.body && createRes.body.length > 0) {
    const created = createRes.json();
    if (created && created.id) return created.id;
  }
  throw new Error(`Unable to resolve cart id (status ${createRes.status})`);
}

export default function () {
  const cartId = getOrCreateCartId();
  const headers = buildHeaders();

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
