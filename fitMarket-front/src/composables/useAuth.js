import { computed, ref } from 'vue'

const mockUser = {
  id: 1,
  email: 'kim.youngwoong@example.com',
  name: '김영웅',
  role: 'ADMIN', // 'USER' | 'SELLER' | 'ADMIN'
  phone: '010-1234-5678',
  address: '서울시 강남구 테헤란로 123',
  joinedAt: '2024-01-15',
  token: 'mock-jwt-token',
}

const ensureRoleArray = (roles, role) => {
  if (Array.isArray(roles) && roles.length > 0) return roles
  if (role) return [role]
  return ['USER']
}

const buildUserSession = (payload) => {
  const base = payload ?? mockUser
  return {
    ...mockUser,
    ...base,
    roles: ensureRoleArray(base.role, base.role),
  }
}

const user = ref(null)

export function useAuth() {
  const isAuthenticated = computed(() => Boolean(user.value))
  const userName = computed(() => user.value?.name ?? '게스트')

  // 권한 계층: ADMIN > SELLER > USER
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isSeller = computed(() => user.value?.role === 'SELLER' || user.value?.role === 'ADMIN')

  const login = (sessionPayload) => {
    user.value = buildUserSession(sessionPayload)
    return user.value
  }

  const logout = () => {
    user.value = null
  }

  const deleteAccount = () => {
    user.value = null
  }

  const hydrateProfile = (profile) => {
    if (!profile) return
    const currentSnapshot = user.value ? buildUserSession(user.value) : buildUserSession()
    user.value = {
      ...currentSnapshot,
      ...profile,
      roles: ensureRoleArray(profile.roles ?? currentSnapshot.roles, profile.role ?? currentSnapshot.role),
    }
  }

  return {
    user,
    isAuthenticated,
    userName,
    isSeller,
    isAdmin,
    login,
    logout,
    deleteAccount,
    hydrateProfile,
  }
}

export {mockUser}
