export function getOS() {
  const userAgent = window.navigator.userAgent;
  if (userAgent.indexOf("Win") !== -1) return "Windows";
  if (userAgent.indexOf("Mac") !== -1) return "MacOS";
  if (userAgent.indexOf("X11") !== -1) return "Unix";
  if (userAgent.indexOf("Linux") !== -1) return "Linux";
  return "Unknown OS";
}

export function getUserAgent() {
  return navigator.userAgent;
}