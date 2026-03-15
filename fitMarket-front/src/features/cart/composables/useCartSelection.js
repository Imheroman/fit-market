import { computed, ref, watch } from 'vue';

export const useCartSelection = (cartItems) => {
  const selectedItemKeys = ref([]);
  const hasAutoSelected = ref(false);

  const getItemKey = (item) => item?.cartItemId ?? item?.productId ?? null;
  const allItemKeys = computed(() => cartItems.value.map(getItemKey).filter((key) => key !== null));
  const selectedKeySet = computed(() => new Set(selectedItemKeys.value));

  const selectedItems = computed(() =>
    cartItems.value.filter((item) => selectedKeySet.value.has(getItemKey(item))),
  );
  const isItemSelected = (item) => selectedKeySet.value.has(getItemKey(item));
  const allSelected = computed(
    () => allItemKeys.value.length > 0 && allItemKeys.value.every((key) => selectedKeySet.value.has(key)),
  );

  const toggleItemSelection = (item) => {
    const key = getItemKey(item);
    if (key === null) return;
    if (selectedKeySet.value.has(key)) {
      selectedItemKeys.value = selectedItemKeys.value.filter((entry) => entry !== key);
    } else {
      selectedItemKeys.value = [...selectedItemKeys.value, key];
    }
  };

  const toggleAllSelection = () => {
    if (allSelected.value) {
      selectedItemKeys.value = [];
      return;
    }
    selectedItemKeys.value = [...allItemKeys.value];
  };

  watch(
    allItemKeys,
    (keys) => {
      if (!keys.length) {
        selectedItemKeys.value = [];
        hasAutoSelected.value = false;
        return;
      }

      const keySet = new Set(keys);
      const filteredSelection = selectedItemKeys.value.filter((key) => keySet.has(key));

      if (!hasAutoSelected.value) {
        selectedItemKeys.value = [...keys];
        hasAutoSelected.value = true;
        return;
      }

      if (filteredSelection.length !== selectedItemKeys.value.length) {
        selectedItemKeys.value = filteredSelection;
      }
    },
    { immediate: true },
  );

  return {
    selectedItemKeys,
    selectedItems,
    allSelected,
    isItemSelected,
    toggleItemSelection,
    toggleAllSelection,
  };
};
