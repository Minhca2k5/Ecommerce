import http from 'k6/http';
import { check, sleep } from 'k6';
import { getEnv } from './env.js';

const BASE_URL = getEnv('K6_BASE_URL', 'http://localhost:8080');
const PRODUCT_SLUG = getEnv('K6_PRODUCT_SLUG', 'sample-product');

export const options = {
  vus: 20,
  duration: '2m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  const homeRes = http.get(`${BASE_URL}/api/public/home`);
  check(homeRes, { 'home 200': (r) => r.status === 200 });

  const listRes = http.get(`${BASE_URL}/api/public/products?page=0&size=20`);
  check(listRes, { 'list 200': (r) => r.status === 200 });

  const detailRes = http.get(`${BASE_URL}/api/public/products/slug/${PRODUCT_SLUG}`);
  check(detailRes, { 'detail 200': (r) => r.status === 200 });

  sleep(1);
}
