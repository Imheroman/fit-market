const mockDelay = (ms = 400) => new Promise((resolve) => setTimeout(resolve, ms))

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const formatPhoneNumber = (value) => {
  const digits = value.replace(/[^0-9]/g, '').slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

const normalizeEmail = (email) => email.trim().toLowerCase()

const sanitizeUser = (user) => {
  const { password, ...rest } = user
  return rest
}

const seedUsers = [
  {
    id: 1,
    email: 'kim.youngwoong@example.com',
    password: 'password123',
    name: '김영웅',
    phone: '010-1234-5678',
    role: 'USER',
    createdAt: '2024-01-15T09:00:00+09:00',
    updatedAt: '2024-03-20T09:00:00+09:00',
  },
]

const users = [...seedUsers]

export async function loginUser(payload) {
  await mockDelay()

  if (!payload) {
    throw new Error('로그인 정보를 전달받지 못했어요.')
  }

  const { email, password } = payload

  if (!email?.trim() || !emailPattern.test(email)) {
    throw new Error('등록된 이메일을 다시 확인해 주세요.')
  }

  if (!password?.trim() || password.length < 8) {
    throw new Error('비밀번호는 8자 이상 입력해주세요.')
  }

  const matched = users.find((user) => normalizeEmail(user.email) === normalizeEmail(email))
  if (!matched || matched.password !== password) {
    throw new Error('이메일 또는 비밀번호가 올바르지 않아요.')
  }

  return sanitizeUser({ ...matched, token: `mock-token-${Date.now()}` })
}

export async function registerUser(payload) {
  await mockDelay()

  if (!payload) {
    throw new Error('회원가입 정보를 확인할 수 없어요.')
  }

  const { email, password, name, phone } = payload

  if (!email?.trim() || !emailPattern.test(email)) {
    throw new Error('올바른 이메일을 입력해주세요.')
  }

  if (!password?.trim() || password.length < 8) {
    throw new Error('비밀번호는 8자 이상 입력해주세요.')
  }

  if (!name?.trim()) {
    throw new Error('이름을 입력해주세요.')
  }

  const normalizedPhone = phone?.replace(/[^0-9]/g, '') ?? ''
  if (normalizedPhone.length < 10) {
    throw new Error('휴대폰 번호를 정확하게 입력해주세요.')
  }

  const duplicated = users.some((user) => normalizeEmail(user.email) === normalizeEmail(email))
  if (duplicated) {
    throw new Error('이미 가입된 이메일이에요.')
  }

  const newUser = {
    id: Date.now(),
    email: email.trim(),
    password,
    name: name.trim(),
    phone: formatPhoneNumber(phone),
    role: 'USER',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  }

  users.push(newUser)
  return sanitizeUser(newUser)
}

export { formatPhoneNumber }
