const DEFAULT_PRODUCT_IMAGE = `data:image/svg+xml,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect width="200" height="200" fill="#f0fdf4"/><rect x="60" y="50" width="80" height="60" rx="8" fill="#bbf7d0"/><circle cx="80" cy="72" r="6" fill="#4ade80"/><path d="M60 95 L85 78 L105 92 L120 82 L140 100 L140 110 L60 110Z" fill="#86efac"/><text x="100" y="140" text-anchor="middle" fill="#16a34a" font-size="12" font-family="sans-serif">이미지 준비중</text></svg>')}`

export function useImageFallback() {
  const onImageError = (e) => {
    if (e.target.src !== DEFAULT_PRODUCT_IMAGE) {
      e.target.src = DEFAULT_PRODUCT_IMAGE
    }
  }

  return { onImageError, DEFAULT_PRODUCT_IMAGE }
}
