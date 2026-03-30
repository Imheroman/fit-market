import { fitmarket } from '@/lib/axios';

export async function applySeller(data) {
  const response = await fitmarket.post('/seller', data);
  return response.data;
}

export async function fetchMySellerApplication() {
  const response = await fitmarket.get('/seller/me');
  return response.data;
}

export async function fetchSellerApplicationsByStatus(status = 'pending') {
  const params = status ? { status } : {};
  const response = await fitmarket.get('/seller', { params });
  return response.data;
}

export async function reviewSellerApplication(id, decision, reviewNote = '') {
  const response = await fitmarket.patch(`/seller/${id}/review`, { decision, reviewNote });
  return response.data;
}

/**
 * 이미지 파일 업로드.
 *
 * @param {File} file - 업로드할 이미지 파일
 * @returns {Promise<string>} 업로드된 파일의 URL
 */
export async function uploadImage(file) {
  if (!file) {
    throw new Error('업로드할 파일이 없습니다.');
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await fitmarket.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    const fileUrl = response.data?.fileUrl;
    if (!fileUrl) {
      throw new Error('업로드된 파일 URL을 받지 못했습니다.');
    }
    return fileUrl;
  } catch (error) {
    if (error.message && !error.response) throw error;
    const message = error?.response?.data?.message ?? '이미지 업로드에 실패했습니다.';
    throw new Error(message);
  }
}
