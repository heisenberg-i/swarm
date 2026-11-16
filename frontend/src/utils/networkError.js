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
