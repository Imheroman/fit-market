const resolveStatus = (error) => {
  if (error?.response?.status) return error.response.status;
  if (error?.cause?.response?.status) return error.cause.response.status;
  if (error?.status) return error.status;
  if (error?.cause?.status) return error.cause.status;
  return null;
};

const isAuthError = (error) => {
  if (error?.isAuthError) return true;
  if (error?.cause?.isAuthError) return true;
  return resolveStatus(error) === 401;
};

const shouldShowErrorAlert = (error) => !isAuthError(error);

export { isAuthError, shouldShowErrorAlert };
