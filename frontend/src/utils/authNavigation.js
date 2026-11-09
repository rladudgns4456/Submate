export function afterLoginPath(path) {
  if (
    typeof path !== 'string' ||
    !path.startsWith('/') ||
    path.startsWith('//') ||
    path.includes('\\') ||
    /^\/(login|signup)(?:[/?#]|$)/.test(path)
  )
    return '/tracker'
  return path
}
