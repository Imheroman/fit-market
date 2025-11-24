const mockDelay = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

let mockProfile = {
  id: 1,
  name: '김영웅',
  email: 'kim.youngwoong@example.com',
  phone: '010-1234-5678',
  address: '서울시 강남구 테헤란로 123',
  marketingConsent: true,
  notificationEmail: true,
  notificationSms: false,
  updatedAt: '2024-03-20T09:00:00+09:00',
}

export async function fetchUserProfile() {
  await mockDelay()
  return { ...mockProfile }
}

export async function updateUserProfile(payload) {
  await mockDelay()
  mockProfile = {
    ...mockProfile,
    ...payload,
    updatedAt: new Date().toISOString(),
  }
  return { ...mockProfile }
}
