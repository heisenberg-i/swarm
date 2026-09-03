const APP_ORIGIN = import.meta.env.VITE_APP_ORIGIN || 'https://app.swarm.co.in';
const CONTROL_ORIGIN = import.meta.env.VITE_CONTROL_CENTER_ORIGIN || 'https://control.swarm.co.in';

export function isLocalHost() {
  const host = window.location.hostname.toLowerCase();
  return host === 'localhost' || host === '127.0.0.1';
}

export function isControlCenterHost() {
  if (import.meta.env.VITE_CONTROL_CENTER === 'true') return true;
  const host = window.location.hostname.toLowerCase();
  return host === 'control.swarm.co.in' || host.startsWith('control.');
}

export function getAppOrigin() {
  if (isLocalHost()) return window.location.origin;
  return APP_ORIGIN;
}

export function getControlCenterOrigin() {
  if (isLocalHost()) return window.location.origin;
  return CONTROL_ORIGIN;
}

/** Control Center is role-based. Do not require control.swarm.co.in (that DNS record is not live). */
export function shouldShowControlCenter(user) {
  return Boolean(user && user.role === 'CONTROL_CENTER_ADMIN');
}

export function getHomeRoute(role) {
  if (role === 'CONTROL_CENTER_ADMIN') return '/overview';
  return '/dashboard';
}
