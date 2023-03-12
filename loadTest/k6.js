import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 100,
    duration: '30s',
};

export default function () {
    http.get('http://discovery-client:8080/test');
    sleep(Math.floor(Math.random() * 4));
}
