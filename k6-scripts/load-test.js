import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN;

export const options = {
  vus: 10,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed:   ['rate<0.01'],
  },
};

export default function () {
  const authHeaders = {
    headers: {
      Authorization: `Bearer ${ACCESS_TOKEN}`,
      'Content-Type': 'application/json',
    },
  };

  const filterRes = http.get(
    `${BASE_URL}/api/home/filter?gender=All&city=All&style=All`,
    authHeaders,
  );

  check(filterRes, {
    'filter 200': (r) => r.status === 200,
  });

  sleep(1);
}
