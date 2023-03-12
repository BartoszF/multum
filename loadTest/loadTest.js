import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '2m', target: 500 },
        { duration: '2m', target: 200 },
        { duration: '5m', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(99)<500'],
    }
};

export default function () {
    const res = http.get('http://discovery-client:8080/test').json();
    check(res, { 'msg' : (obj) => obj.msg === 'OK'})
    sleep(Math.floor(Math.random() * 4));
}
