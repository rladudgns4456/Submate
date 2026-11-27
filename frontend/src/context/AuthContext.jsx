import { useCallback, useEffect, useRef, useState } from 'react'
import api, { setAuthorization } from '../api/axios'
import { authApi } from '../api/authApi'
import { AuthContext, isDemo } from './authState'

export function AuthProvider({ children, initialSession = null, onLogout }) {
  const [session, setSession] = useState(() => {
    setAuthorization(
      initialSession?.accessToken
        ? `Bearer ${initialSession.accessToken}`
        : null
    )
    return initialSession
  })
  const [authNotice, setAuthNotice] = useState('')
  const sessionRef = useRef(initialSession)
  const attemptVersion = useRef(0)
  const validation = useRef(null)
  const mounted = useRef(true)

  const logout = useCallback(
    (expired = false) => {
      attemptVersion.current += 1
      sessionRef.current = null
      setAuthorization(null)
      setSession(null)
      setAuthNotice(
        expired
          ? '로그인 시간이 만료되었거나 계정을 사용할 수 없습니다. 다시 로그인해 주세요.'
          : ''
      )
      onLogout?.()
    },
    [onLogout]
  )

  async function login(email, password) {
    const version = ++attemptVersion.current
    const result = await authApi.login(email, password)
    if (!result.token) throw new Error('로그인 응답에 토큰이 없습니다.')
    // 권한은 JWT 내용을 추측하지 않고 기존 /auth/me 응답으로 확인한다.
    const member = await authApi.me(result.token)
    if (!mounted.current || version !== attemptVersion.current) return
    const nextSession = { ...member, accessToken: result.token }
    setAuthorization(`Bearer ${result.token}`)
    sessionRef.current = nextSession
    setSession(nextSession)
    setAuthNotice('')
  }

  useEffect(() => {
    let active = true
    mounted.current = true
    const interceptor = api.interceptors.response.use(
      undefined,
      async (error) => {
        const current = sessionRef.current
        if (
          active &&
          error.response?.status === 401 &&
          current?.accessToken &&
          !error.config?.skipSessionCheck &&
          !error.config?.skipAuthorization &&
          error.config?.headers?.Authorization ===
            `Bearer ${current.accessToken}`
        ) {
          // 일부 기존 업무 오류도 401을 반환하므로 실제 세션을 확인한 뒤 만료 처리한다.
          if (validation.current?.token !== current.accessToken) {
            validation.current = {
              token: current.accessToken,
              promise: authApi
                .me(current.accessToken)
                .then(() => true)
                .catch((failure) => {
                  if (
                    active &&
                    failure.response?.status === 401 &&
                    sessionRef.current === current
                  )
                    logout(true)
                  return false
                })
                .finally(() => {
                  if (validation.current?.token === current.accessToken)
                    validation.current = null
                }),
            }
          }
          await validation.current.promise
        }
        return Promise.reject(error)
      }
    )
    return () => {
      active = false
      mounted.current = false
      api.interceptors.response.eject(interceptor)
    }
  }, [logout])
  async function demoLogin(email, password) {
    if (!isDemo) throw new Error('데모 모드가 아닙니다.')
    const version = ++attemptVersion.current
    setAuthorization(`Basic ${btoa(`${email}:${password}`)}`)
    try {
      await api.get('/subscriptions', { params: { size: 1 } })
      if (!mounted.current || version !== attemptVersion.current) return
      const nextSession = {
        email,
        role: email === 'admin@submate.test' ? 'ADMIN' : 'USER',
      }
      sessionRef.current = nextSession
      setSession(nextSession)
      setAuthNotice('')
    } catch (error) {
      if (mounted.current && version === attemptVersion.current)
        setAuthorization(null)
      throw error
    }
  }
  return (
    <AuthContext.Provider
      value={{ session, login, demoLogin, logout, authNotice }}
    >
      {children}
    </AuthContext.Provider>
  )
}
