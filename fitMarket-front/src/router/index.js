import { createRouter, createWebHistory } from "vue-router"
import HomePage from "@/views/HomePage.vue"
import ProductDetailPage from "@/views/ProductDetailPage.vue"
import CartPage from "@/views/CartPage.vue"
import MyPage from "@/views/MyPage.vue"
import MyProfilePage from "@/views/mypage/MyProfilePage.vue"
import MyOrdersPage from "@/views/mypage/MyOrdersPage.vue"
import MyAddressesPage from "@/views/mypage/MyAddressesPage.vue"
import MyAddressCreatePage from "@/views/mypage/MyAddressCreatePage.vue"
import MyAddressEditPage from "@/views/mypage/MyAddressEditPage.vue"
import UserEditPage from "@/views/UserEditPage.vue"
import ChangeNamePage from "@/views/account/ChangeNamePage.vue"
import ChangePhonePage from "@/views/account/ChangePhonePage.vue"
import ChangePasswordPage from "@/views/account/ChangePasswordPage.vue"
import OrderCompletePage from "@/views/OrderCompletePage.vue"
import OrderCheckoutPage from "@/views/OrderCheckoutPage.vue"
import SellerApplicationPage from "@/views/SellerApplicationPage.vue"
import SellerProductRegisterPage from "@/views/SellerProductRegisterPage.vue"
import AdminSellerApplicationsPage from "@/views/AdminSellerApplicationsPage.vue"
import LoginPage from "@/views/LoginPage.vue"
import RegisterPage from "@/views/RegisterPage.vue"
import NewProductsPage from "@/views/NewProductsPage.vue"
import BestProductsPage from "@/views/BestProductsPage.vue"

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
    },
    {
      path: "/signup",
      name: "signup",
      component: RegisterPage,
    },
    {
      path: "/cart",
      name: "cart",
      component: CartPage,
    },
    {
      path: "/order/checkout",
      name: "order-checkout",
      component: OrderCheckoutPage,
    },
    {
      path: "/order/complete",
      name: "order-complete",
      component: OrderCompletePage,
    },
    {
      path: "/mypage",
      name: "my-page",
      component: MyPage,
      redirect: { name: "my-page-profile" },
      children: [
        {
          path: "profile",
          name: "my-page-profile",
          component: MyProfilePage,
        },
        {
          path: "orders",
          name: "my-page-orders",
          component: MyOrdersPage,
        },
        {
          path: "addresses",
          name: "my-page-addresses",
          component: MyAddressesPage,
        },
        {
          path: "addresses/new",
          name: "my-page-addresses-new",
          component: MyAddressCreatePage,
        },
        {
          path: "addresses/:id/edit",
          name: "my-page-addresses-edit",
          component: MyAddressEditPage,
        },
      ],
    },
    {
      path: "/mypage/edit",
      name: "my-page-edit",
      component: UserEditPage,
    },
    {
      path: "/mypage/edit/name",
      name: "my-page-edit-name",
      component: ChangeNamePage,
    },
    {
      path: "/mypage/edit/phone",
      name: "my-page-edit-phone",
      component: ChangePhonePage,
    },
    {
      path: "/mypage/edit/password",
      name: "my-page-edit-password",
      component: ChangePasswordPage,
    },
    {
      path: "/seller/apply",
      name: "seller-apply",
      component: SellerApplicationPage,
    },
    {
      path: "/seller/products",
      name: "seller-products",
      component: SellerProductRegisterPage,
    },
    {
      path: "/admin/seller-applications",
      name: "admin-seller-applications",
      component: AdminSellerApplicationsPage,
    },
  ],
})

export default router
