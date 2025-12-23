<template>
  <footer class="bg-green-900 text-green-50 py-12">
    <div class="container mx-auto px-4">
      <div class="grid md:grid-cols-4 gap-8">
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div class="flex items-center justify-center w-10 h-10 bg-white/10 rounded-xl">
              <Leaf class="w-6 h-6 text-green-300" />
            </div>
            <span class="text-xl font-bold text-white">초록초록</span>
          </div>
          <p class="text-sm text-green-200 leading-relaxed">건강한 식단, 맛있는 하루</p>
        </div>

        <div>
          <h4 class="font-semibold mb-3 text-white">상품</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">도시락</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">밀키트</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">샐러드</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">단백질 보충식</a></li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold mb-3 text-white">고객지원</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">공지사항</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">자주 묻는 질문</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">배송 안내</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">환불 정책</a></li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold mb-3 text-white">판매자</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">판매자 등록</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">상품 관리</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">주문 관리</a></li>
            <li><a href="#" class="text-green-200 hover:text-white transition-colors">정산 안내</a></li>
          </ul>
        </div>
      </div>

      <div class="border-t border-green-800 mt-8 pt-8 text-center text-sm text-green-300">
        <p>© 2025 초록초록. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { Leaf } from 'lucide-vue-next'
</script>
