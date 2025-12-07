// /src/restapi/index.js
import axios from 'axios';

// 전역 로딩 카운터 : useApi 등에서 개별적으로 관리
//const loadingCount = ref(0);
//const isLoading = computed(() => loadingCount.value > 0);

// axios 인스턴스 생성: 동적으로 memberStore에서 accessToken을 가져올 수 없으므로 인터셉터에서 처리
const fitmarket = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 1000 * 3,
  withCredentials: true,
  // headers: {
    // "x-api-key": "reqres-free-v1",
  // },
});

// 요청 인터셉터 설정
fitmarket.interceptors.request.use(
  (config) => {
    console.log('[요청]', config);
    //loadingCount.value++;
    config.withCredentials = true;

    // END
    return config;
  },
  (error) => {
    console.error('[요청]', error);
    //loadingCount.value--;
    return Promise.reject(error);
  }
);
// 응답 인터셉터 설정
fitmarket.interceptors.response.use(
  (response) => {
    console.log('[응답]', response);
    return response;
  },
  async (error) => {
    console.error('[응답 에러]', error);
    //  error 메시지가 TOKEN_ERROR인 경우 refresh token으로 access token 재발급 시도, 아니면 로그아웃 처리
     return Promise.reject(error);

    // END
  }
);

export { fitmarket };
