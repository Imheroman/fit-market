import { defineStore } from 'pinia';

const ensureRoleArray = (roles, role) => {
  if (Array.isArray(roles) && roles.length > 0) return roles;
  if (role) return [role];
  return [];
};

const normalizeCartCount = (cartCount) => {
  const parsed = Number(cartCount);
  if (Number.isNaN(parsed) || parsed < 0) return 0;
  return Math.floor(parsed);
};

const buildUserSession = (payload) => {
  if (!payload) return null;

  const roles = ensureRoleArray(payload.roles, payload.role);
  const name = typeof payload.name === 'string' ? payload.name.trim() : '';
  const nickname = typeof payload.nickname === 'string' ? payload.nickname.trim() : '';
  const role = payload.role ?? roles[0] ?? 'USER';

  return {
    ...payload,
    name,
    nickname,
    role,
    roles,
    cartCount: normalizeCartCount(payload.cartCount),
  };
};

export const useSessionStore = defineStore('session', {
  state: () => ({
    user: null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.user),
    userName: (state) => {
      const nickname = state.user?.nickname?.trim?.();
      const name = state.user?.name?.trim?.();
      if (nickname) return nickname;
      if (name) return name;
      return '게스트';
    },
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isSeller: (state) => state.user?.role === 'SELLER' || state.user?.role === 'ADMIN',
    roles: (state) => ensureRoleArray(state.user?.roles, state.user?.role),
    cartCount: (state) => normalizeCartCount(state.user?.cartCount ?? 0),
  },
  actions: {
    login(sessionPayload) {
      this.user = buildUserSession(sessionPayload);
      return this.user;
    },
    logout() {
      this.user = null;
    },
    deleteAccount() {
      this.user = null;
    },
    hydrateProfile(profile) {
      if (!profile) return;
      const snapshot = this.user ? buildUserSession(this.user) : null;
      const merged = {
        ...(snapshot ?? {}),
        ...profile,
      };
      this.user = buildUserSession({
        ...merged,
        cartCount: normalizeCartCount(profile.cartCount ?? snapshot?.cartCount),
        roles: ensureRoleArray(profile.roles ?? snapshot?.roles, profile.role ?? snapshot?.role),
        role: profile.role ?? snapshot?.role ?? merged.role,
      });
    },
  },
  persist: {
    paths: ['user'],
  },
});

export { buildUserSession, ensureRoleArray };
