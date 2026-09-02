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

export function shouldShowControlCenter(user) {
  if (!user || user.role !== 'CONTROL_CENTER_ADMIN') return false;
  if (isLocalHost()) return true;
  return isControlCenterHost();
}

export function getHomeRoute(role) {
  if (role === 'CONTROL_CENTER_ADMIN') {
    if (isLocalHost() || isControlCenterHost()) return '/overview';
    window.location.replace(`${CONTROL_ORIGIN}/overview`);
    return '/overview';
  }
  if (isControlCenterHost() && !isLocalHost()) {
    window.location.replace(`${APP_ORIGIN}/dashboard`);
    return '/dashboard';
  }
  return '/dashboard';
}
