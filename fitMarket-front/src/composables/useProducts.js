import { ref } from 'vue'

const products = ref([
  {
    id: 1,
    name: '그린 샐러드 도시락',
    category: '도시락',
    price: '8,500원',
    image: '/fresh-green-salad-bowl.png',
    rating: 4.8,
    reviews: 124,
    calories: 320,
    protein: 18,
    carbs: 35,
    fat: 12,
    isFavorite: true,
  },
  {
    id: 2,
    name: '고단백 치킨 밀키트',
    category: '밀키트',
    price: '12,900원',
    image: '/healthy-chicken-meal-prep.png',
    rating: 4.9,
    reviews: 89,
    calories: 450,
    protein: 42,
    carbs: 28,
    fat: 18,
    isFavorite: false,
  },
  {
    id: 3,
    name: '퀴노아 닭가슴살 볼',
    category: '단백질 보충식',
    price: '9,800원',
    image: '/quinoa-chicken-bowl-healthy.png',
    rating: 4.7,
    reviews: 156,
    calories: 380,
    protein: 35,
    carbs: 42,
    fat: 8,
    isFavorite: true,
  },
  {
    id: 4,
    name: '베리 그린 스무디',
    category: '스무디',
    price: '6,500원',
    image: '/green-berry-smoothie.png',
    rating: 4.6,
    reviews: 203,
    calories: 180,
    protein: 8,
    carbs: 32,
    fat: 4,
    isFavorite: false,
  },
  {
    id: 5,
    name: '지중해식 샐러드',
    category: '샐러드',
    price: '11,200원',
    image: '/mediterranean-salad.png',
    rating: 4.8,
    reviews: 178,
    calories: 290,
    protein: 15,
    carbs: 25,
    fat: 16,
    isFavorite: false,
  },
  {
    id: 6,
    name: '새우 아보카도 볼',
    category: '도시락',
    price: '13,500원',
    image: '/shrimp-avocado-bowl.png',
    rating: 4.9,
    reviews: 92,
    calories: 410,
    protein: 28,
    carbs: 30,
    fat: 20,
    isFavorite: true,
  },
])

export function useProducts() {
  const toggleFavorite = (productId) => {
    const product = products.value.find(p => p.id === productId)
    if (product) {
      product.isFavorite = !product.isFavorite
    }
  }

  const getProductById = (id) => {
    return products.value.find(p => p.id === id)
  }

  return {
    products,
    toggleFavorite,
    getProductById,
  }
}
