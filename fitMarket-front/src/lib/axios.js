// /src/lib/axios.js
import axios from 'axios';
import router from '@/router';
import { useSessionStore } from '@/features/auth/store';

const isDev = import.meta.env.DEV;

// axios 인스턴스 생성: 동적으로 memberStore에서 accessToken을 가져올 수 없으므로 인터셉터에서 처리
const fitmarket = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 1000 * 10,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

const AUTH_REQUIRED_MESSAGE = '로그인이 필요해요. 로그인 후 이용할 수 있어요.';

const clearAuthErrorState = () => {};

// 동시에 여러 401 응답이 들어올 때 중복 처리를 방지하는 플래그
let isHandling401 = false;

// 요청 인터셉터 설정
fitmarket.interceptors.request.use(
  (config) => {
    if (isDev) console.log('[요청]', config);
    config.withCredentials = true;
    return config;
  },
  (error) => {
    if (isDev) console.error('[요청 에러]', error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터 설정
fitmarket.interceptors.response.use(
  (response) => {
    if (isDev) console.log('[응답]', response);
    return response;
  },
  async (error) => {
    if (isDev) console.error('[응답 에러]', error);

    const status = error?.response?.status;

    if (status === 401 && !isHandling401) {
      isHandling401 = true;

      error.isAuthError = true;
      const sessionStore = useSessionStore();
      sessionStore.logout();

      const isOnLogin = router.currentRoute.value?.name === 'login';
      if (!isOnLogin) {
        alert(AUTH_REQUIRED_MESSAGE);
        const redirect = router.currentRoute.value?.fullPath;
        router.push({ name: 'login', query: redirect ? { redirect } : undefined });
      }

      isHandling401 = false;
    }

    return Promise.reject(error);
  }
);

export { fitmarket, clearAuthErrorState };
