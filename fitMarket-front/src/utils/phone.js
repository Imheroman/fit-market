export const sanitizePhoneDigits = (value = '') => value.replace(/[^0-9]/g, '').slice(0, 11);

export const formatPhoneNumber = (value = '') => {
  const digits = sanitizePhoneDigits(value);
  if (digits.length < 4) return digits;
  if (digits.length < 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  if (digits.length < 11) return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
};
