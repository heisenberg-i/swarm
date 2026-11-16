// PURPOSE: Frontend helper used by SWARM pages (networkError).
import { Capacitor } from '@capacitor/core';

export function isNativeApp() {
  try {
    return Capacitor.isNativePlatform();
  } catch {
    return false;
  }
}

export function networkErrorMessage(err) {
  if (err?.response) return null;
  if (isNativeApp()) {
    const api = import.meta.env.VITE_API_URL || 'https://api.swarm.co.in/api';
    return 'Cannot reach the server. Check mobile internet (Wi-Fi or mobile data) and try again. '
      + `API: ${api}`;
  }
  return 'Cannot reach the server. Check that you are using this site\'s address and that the API is running.';
}

export function responseMessage(err) {
  const data = err?.response?.data;
  if (typeof data === 'string' && data.trim()) {
    return data;
  }
  if (data && typeof data === 'object' && data.message) {
    return data.message;
  }
  return null;
}

export function loginErrorMessage(err) {
  const networkMsg = networkErrorMessage(err);
  if (networkMsg) {
    return networkMsg;
  }
  const status = err.response?.status;
  const apiMessage = responseMessage(err);
  if (status === 401) {
    return apiMessage && !/^unauthorized/i.test(apiMessage) ? apiMessage : 'Invalid email or password';
  }
  if (status === 403) {
    const text = (apiMessage || '').toLowerCase();
    if (text.includes('cors')) {
      return 'This browser origin is blocked by CORS. Open SWARM at the same host the API allows, or set CORS_ORIGINS / SWARM_CORS_ALLOW_ANY_HTTP.';
    }
    return apiMessage || 'Access denied.';
  }
  if (status === 429) {
    return apiMessage || 'Too many login attempts. Wait a minute and try again.';
  }
  if (status === 400) {
    return apiMessage || 'Login request was rejected.';
  }
  if (status >= 500) {
    return 'The API is reachable but reported a server error. Try again, or check backend logs.';
  }
  return apiMessage || `Request failed (${status}).`;
}
