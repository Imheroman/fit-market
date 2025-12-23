const approvalStatusMeta = {
  pending_approval: { label: '승인 대기', badgeClass: 'bg-yellow-100 text-yellow-700' },
  approved: { label: '승인 완료', badgeClass: 'bg-green-100 text-green-700' },
  rejected: { label: '승인 거절', badgeClass: 'bg-red-100 text-red-600' },
  cancelled: { label: '주문 취소', badgeClass: 'bg-red-100 text-red-600' },
  shipping: { label: '배송 중', badgeClass: 'bg-blue-100 text-blue-700' },
  delivered: { label: '배송 완료', badgeClass: 'bg-green-100 text-green-700' },
};

const claimStatusMeta = {
  REFUND: {
    PENDING: { label: '환불 진행 중', badgeClass: 'bg-blue-100 text-blue-700' },
    APPROVED: { label: '환불 승인', badgeClass: 'bg-blue-100 text-blue-700' },
    REJECTED: { label: '환불 거절', badgeClass: 'bg-red-100 text-red-600' },
    COMPLETED: { label: '환불 완료', badgeClass: 'bg-green-100 text-green-700' },
  },
  RETURN: {
    PENDING: { label: '반품 진행 중', badgeClass: 'bg-blue-100 text-blue-700' },
    APPROVED: { label: '반품 승인', badgeClass: 'bg-blue-100 text-blue-700' },
    REJECTED: { label: '반품 거절', badgeClass: 'bg-red-100 text-red-600' },
    COMPLETED: { label: '반품 완료', badgeClass: 'bg-green-100 text-green-700' },
  },
  EXCHANGE: {
    PENDING: { label: '교환 진행 중', badgeClass: 'bg-blue-100 text-blue-700' },
    APPROVED: { label: '교환 승인', badgeClass: 'bg-blue-100 text-blue-700' },
    REJECTED: { label: '교환 거절', badgeClass: 'bg-red-100 text-red-600' },
    COMPLETED: { label: '교환 완료', badgeClass: 'bg-green-100 text-green-700' },
  },
};

const defaultMeta = { label: '확인 필요', badgeClass: 'bg-gray-100 text-gray-600' };

const normalizeUpper = (value) => (value ? String(value).toUpperCase() : '');

const resolveClaimMeta = (returnExchange) => {
  if (!returnExchange || typeof returnExchange !== 'object') return null;
  const type = normalizeUpper(returnExchange.type);
  const status = normalizeUpper(returnExchange.status);
  if (!type || !status) return null;
  return claimStatusMeta[type]?.[status] ?? null;
};

export const getOrderStatusMeta = (order) => {
  const claimMeta = resolveClaimMeta(order?.returnExchange);
  if (claimMeta) return claimMeta;
  const approvalStatus = order?.approvalStatus ?? '';
  return approvalStatusMeta[approvalStatus] ?? defaultMeta;
};
