import { createRouter, createWebHistory } from "vue-router"
import HomePage from "@/views/HomePage.vue"
import { useSessionStore } from "@/features/auth/store"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: HomePage,
    },
    {
      path: "/products/new",
      name: "new-products",
      component: () => import("@/views/NewProductsPage.vue"),
    },
    {
      path: "/products/best",
      name: "best-products",
      component: () => import("@/views/BestProductsPage.vue"),
    },
    {
      path: "/product/:id",
      name: "product-detail",
      component: () => import("@/views/ProductDetailPage.vue"),
    },
    {
      path: "/login",
      name: "login",
      component: () => import("@/views/LoginPage.vue"),
      meta: { guestOnly: true },
    },
    {
      path: "/signup",
      name: "signup",
      component: () => import("@/views/RegisterPage.vue"),
      meta: { guestOnly: true },
    },
    {
      path: "/cart",
      name: "cart",
      component: () => import("@/views/CartPage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/order/checkout",
      name: "order-checkout",
      component: () => import("@/views/OrderCheckoutPage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage",
      name: "my-page",
      component: () => import("@/views/MyPage.vue"),
      redirect: { name: "my-page-profile" },
      meta: { requiresAuth: true },
      children: [
        {
          path: "profile",
          name: "my-page-profile",
          component: () => import("@/views/mypage/MyProfilePage.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "orders",
          name: "my-page-orders",
          component: () => import("@/views/mypage/MyOrdersPage.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "orders/:orderNumber",
          name: "my-page-order-detail",
          component: () => import("@/views/mypage/MyOrderDetailPage.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "addresses",
          name: "my-page-addresses",
          component: () => import("@/views/mypage/MyAddressesPage.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "addresses/new",
          name: "my-page-addresses-new",
          component: () => import("@/views/mypage/MyAddressCreatePage.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "addresses/:id/edit",
          name: "my-page-addresses-edit",
          component: () => import("@/views/mypage/MyAddressEditPage.vue"),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: "/mypage/edit",
      name: "my-page-edit",
      component: () => import("@/views/UserEditPage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage/edit/name",
      name: "my-page-edit-name",
      component: () => import("@/views/account/ChangeNamePage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage/edit/phone",
      name: "my-page-edit-phone",
      component: () => import("@/views/account/ChangePhonePage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage/edit/password",
      name: "my-page-edit-password",
      component: () => import("@/views/account/ChangePasswordPage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/seller/apply",
      name: "seller-apply",
      component: () => import("@/views/SellerApplicationPage.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/seller/products",
      name: "seller-products",
      component: () => import("@/views/SellerProductRegisterPage.vue"),
      meta: { requiresAuth: true, role: "SELLER" },
    },
    {
      path: "/admin/seller-applications",
      name: "admin-seller-applications",
      component: () => import("@/views/AdminSellerApplicationsPage.vue"),
      meta: { requiresAuth: true, role: "ADMIN" },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const sessionStore = useSessionStore()
  const { isAuthenticated, isSeller, isAdmin } = sessionStore

  // 비로그인 전용 페이지: 이미 인증된 경우 홈으로 redirect
  if (to.meta.guestOnly && isAuthenticated) {
    return next({ name: "home" })
  }

  // 인증 필요 페이지: 비인증 시 로그인 페이지로 redirect (원래 경로 보존)
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ name: "login", query: { redirect: to.fullPath } })
  }

  // 역할 권한 체크
  if (to.meta.requiresAuth && to.meta.role) {
    if (to.meta.role === "ADMIN" && !isAdmin) {
      return next({ name: "home" })
    }
    if (to.meta.role === "SELLER" && !isSeller) {
      return next({ name: "home" })
    }
  }

  next()
})

export default router
