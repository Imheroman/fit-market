import { createRouter, createWebHistory } from "vue-router"
import HomePage from "@/views/HomePage.vue"
import ProductDetailPage from "@/views/ProductDetailPage.vue"
import CartPage from "@/views/CartPage.vue"
import MyPage from "@/views/MyPage.vue"
import UserEditPage from "@/views/UserEditPage.vue"
import OrderCompletePage from "@/views/OrderCompletePage.vue"
import OrderCheckoutPage from "@/views/OrderCheckoutPage.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: HomePage,
    },
    {
      path: "/product/:id",
      name: "product-detail",
      component: ProductDetailPage,
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
    },
    {
      path: "/mypage/edit",
      name: "my-page-edit",
      component: UserEditPage,
    },
  ],
})

export default router
