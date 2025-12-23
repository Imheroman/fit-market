const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 이미지 파일 업로드.
 *
 * @param {File} file - 업로드할 이미지 파일
 * @returns {Promise<string>} 업로드된 파일의 URL
 */
export async function uploadImage(file) {
  if (!file) {
    throw new Error('업로드할 파일이 없습니다.')
  }

  // FormData 생성
  const formData = new FormData()
  formData.append('file', file)

  let response
  try {
    response = await fetch(`${API_BASE_URL}/files/upload`, {
      method: 'POST',
      credentials: 'include',
      body: formData, // Content-Type을 자동으로 multipart/form-data로 설정
    })
  } catch (error) {
    console.error('네트워크 에러:', error)
    throw new Error('이미지 업로드 서버에 연결할 수 없습니다. 네트워크를 확인해주세요.')
  }

  // 응답 처리
  if (!response.ok) {
    let errorMessage = '이미지 업로드에 실패했습니다.'

    try {
      const errorData = await response.json()
      errorMessage = errorData?.message ?? errorMessage
    } catch {
      // JSON 파싱 실패 시 기본 에러 메시지 사용
    }

    throw new Error(errorMessage)
  }

  // 성공 응답
  const data = await response.json()
  const fileUrl = data?.fileUrl

  if (!fileUrl) {
    throw new Error('업로드된 파일 URL을 받지 못했습니다.')
  }

  return fileUrl
}