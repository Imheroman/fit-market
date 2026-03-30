import { createRouter, createWebHistory } from "vue-router"
import HomePage from "@/views/HomePage.vue"
import ProductDetailPage from "@/views/ProductDetailPage.vue"
import CartPage from "@/views/CartPage.vue"
import MyPage from "@/views/MyPage.vue"
import MyProfilePage from "@/views/mypage/MyProfilePage.vue"
import MyOrdersPage from "@/views/mypage/MyOrdersPage.vue"
import MyOrderDetailPage from "@/views/mypage/MyOrderDetailPage.vue"
import MyAddressesPage from "@/views/mypage/MyAddressesPage.vue"
import MyAddressCreatePage from "@/views/mypage/MyAddressCreatePage.vue"
import MyAddressEditPage from "@/views/mypage/MyAddressEditPage.vue"
import UserEditPage from "@/views/UserEditPage.vue"
import ChangeNamePage from "@/views/account/ChangeNamePage.vue"
import ChangePhonePage from "@/views/account/ChangePhonePage.vue"
import ChangePasswordPage from "@/views/account/ChangePasswordPage.vue"
import OrderCheckoutPage from "@/views/OrderCheckoutPage.vue"
import SellerApplicationPage from "@/views/SellerApplicationPage.vue"
import SellerProductRegisterPage from "@/views/SellerProductRegisterPage.vue"
import AdminSellerApplicationsPage from "@/views/AdminSellerApplicationsPage.vue"
import LoginPage from "@/views/LoginPage.vue"
import RegisterPage from "@/views/RegisterPage.vue"
import NewProductsPage from "@/views/NewProductsPage.vue"
import BestProductsPage from "@/views/BestProductsPage.vue"
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
      component: NewProductsPage,
    },
    {
      path: "/products/best",
      name: "best-products",
      component: BestProductsPage,
    },
    {
      path: "/product/:id",
      name: "product-detail",
      component: ProductDetailPage,
    },
    {
      path: "/login",
      name: "login",
      component: LoginPage,
      meta: { guestOnly: true },
    },
    {
      path: "/signup",
      name: "signup",
      component: RegisterPage,
      meta: { guestOnly: true },
    },
    {
      path: "/cart",
      name: "cart",
      component: CartPage,
      meta: { requiresAuth: true },
    },
    {
      path: "/order/checkout",
      name: "order-checkout",
      component: OrderCheckoutPage,
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage",
      name: "my-page",
      component: MyPage,
      redirect: { name: "my-page-profile" },
      meta: { requiresAuth: true },
      children: [
        {
          path: "profile",
          name: "my-page-profile",
          component: MyProfilePage,
          meta: { requiresAuth: true },
        },
        {
          path: "orders",
          name: "my-page-orders",
          component: MyOrdersPage,
          meta: { requiresAuth: true },
        },
        {
          path: "orders/:orderNumber",
          name: "my-page-order-detail",
          component: MyOrderDetailPage,
          meta: { requiresAuth: true },
        },
        {
          path: "addresses",
          name: "my-page-addresses",
          component: MyAddressesPage,
          meta: { requiresAuth: true },
        },
        {
          path: "addresses/new",
          name: "my-page-addresses-new",
          component: MyAddressCreatePage,
          meta: { requiresAuth: true },
        },
        {
          path: "addresses/:id/edit",
          name: "my-page-addresses-edit",
          component: MyAddressEditPage,
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: "/mypage/edit",
      name: "my-page-edit",
      component: UserEditPage,
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage/edit/name",
      name: "my-page-edit-name",
      component: ChangeNamePage,
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage/edit/phone",
      name: "my-page-edit-phone",
      component: ChangePhonePage,
      meta: { requiresAuth: true },
    },
    {
      path: "/mypage/edit/password",
      name: "my-page-edit-password",
      component: ChangePasswordPage,
      meta: { requiresAuth: true },
    },
    {
      path: "/seller/apply",
      name: "seller-apply",
      component: SellerApplicationPage,
      meta: { requiresAuth: true },
    },
    {
      path: "/seller/products",
      name: "seller-products",
      component: SellerProductRegisterPage,
      meta: { requiresAuth: true, role: "SELLER" },
    },
    {
      path: "/admin/seller-applications",
      name: "admin-seller-applications",
      component: AdminSellerApplicationsPage,
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
