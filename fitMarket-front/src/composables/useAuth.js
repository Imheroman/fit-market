import { computed, ref } from 'vue'

const mockUser = {
  id: 1,
  email: 'kim.youngwoong@example.com',
  name: '김영웅',
  roles: ['USER'],
  phone: '010-1234-5678',
  address: '서울시 강남구 테헤란로 123',
  joinedAt: '2024-01-15',
  token: 'mock-jwt-token',
}

const user = ref(mockUser)

export function useAuth() {
  const isAuthenticated = computed(() => Boolean(user.value))
  const userName = computed(() => user.value?.name ?? '게스트')

  const login = () => {
    // TODO: 로그인 처리
    user.value = mockUser
  }

  const logout = () => {
    // TODO: 로그아웃 처리
    user.value = null
  }

  const deleteAccount = () => {
    // TODO: 회원 탈퇴
    user.value = null
  }

  const hydrateProfile = (profile) => {
    if (!profile) return
    user.value = {
      ...user.value,
      ...profile,
    }
  }

  return {
    user,
    isAuthenticated,
    userName,
    login,
    logout,
    deleteAccount,
    hydrateProfile,
  }
}

export { mockUser }
