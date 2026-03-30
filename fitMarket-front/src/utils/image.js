/**
 * 백엔드 이미지 경로를 전체 URL로 변환한다.
 *
 * @param {string|null|undefined} imageUrl - 백엔드에서 반환한 이미지 경로 (예: '/files/abc.jpg')
 * @returns {string} 전체 이미지 URL 또는 빈 문자열
 */
export function getImageUrl(imageUrl) {
  if (!imageUrl) return '';
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  return `${import.meta.env.VITE_API_BASE_URL}${imageUrl}`;
}

/**
 * 전체 이미지 URL에서 백엔드 경로만 추출한다.
 * (상품 수정 시 기존 이미지 URL을 백엔드 경로로 복원할 때 사용)
 *
 * @param {string} fullUrl - 전체 이미지 URL
 * @returns {string} 백엔드 이미지 경로
 */
export function stripImageBaseUrl(fullUrl) {
  if (!fullUrl) return '';
  const baseUrl = import.meta.env.VITE_API_BASE_URL;
  if (fullUrl.startsWith(baseUrl)) {
    return fullUrl.slice(baseUrl.length);
  }
  return fullUrl;
}
