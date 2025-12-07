import { ref } from 'vue';
import { storeToRefs } from 'pinia';
import { fetchUserProfile, deleteUserAccount } from '@/api/userApi';
import { useSessionStore, buildUserSession, ensureRoleArray } from '@/stores/sessionStore';

export function useAuth() {
  const sessionStore = useSessionStore();
  const { user, isAuthenticated, userName, isSeller, isAdmin, cartCount } = storeToRefs(sessionStore);
  const isProfileLoading = ref(false);
  const profileError = ref('');

  const login = (sessionPayload) => sessionStore.login(sessionPayload);
  const logout = () => sessionStore.logout();
  const deleteAccount = async () => {
    await deleteUserAccount();
    sessionStore.deleteAccount();
  };
  const hydrateProfile = (profile) => sessionStore.hydrateProfile(profile);

  const loadUserProfile = async () => {
    isProfileLoading.value = true;
    profileError.value = '';

    try {
      const profile = await fetchUserProfile();
      hydrateProfile(profile);
      return profile;
    } catch (error) {
      profileError.value = error?.message ?? '회원 정보를 불러오지 못했어요.';
      throw error;
    } finally {
      isProfileLoading.value = false;
    }
  };

  return {
    user,
    isAuthenticated,
    userName,
    isSeller,
    isAdmin,
    cartCount,
    login,
    logout,
    deleteAccount,
    hydrateProfile,
    loadUserProfile,
    isProfileLoading,
    profileError,
  };
}

export { buildUserSession, ensureRoleArray };
