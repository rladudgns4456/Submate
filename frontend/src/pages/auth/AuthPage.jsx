import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { authApi } from '../../api/authApi'
import { errorMessage } from '../../api/axios'
import { useAuth } from '../../hooks/useAuth'
import { afterLoginPath } from '../../utils/authNavigation'

export default function AuthPage({ signup = false }) {
  const { session, login, authNotice } = useAuth()
  const { state } = useLocation()
  const navigate = useNavigate()
  const [email, setEmail] = useState(state?.email || '')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const destination = afterLoginPath(state?.from)
  if (session) return <Navigate to={destination} replace />

  async function handleSubmit(event) {
    event.preventDefault()
    if (busy) return
    setError('')
    if (signup && password !== confirmation) {
      setError('비밀번호 확인이 일치하지 않습니다.')
      return
    }
    if (signup && new TextEncoder().encode(password).length > 72) {
      setError('비밀번호는 UTF-8 기준 72바이트 이하로 입력해 주세요.')
      return
    }
    setBusy(true)
    try {
      if (signup) {
        await authApi.signup(email.trim(), password)
        navigate('/login', {
          replace: true,
          state: { from: destination, email: email.trim(), registered: true },
        })
      } else {
        await login(email.trim(), password)
      }
      setPassword('')
      setConfirmation('')
    } catch (failure) {
      setError(
        failure.response?.status === 401
          ? signup
            ? '가입할 수 없습니다. 이메일 중복 여부와 비밀번호를 확인해 주세요.'
            : '이메일·비밀번호와 계정 상태를 확인해 주세요.'
          : errorMessage(failure)
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="panel login">
      <p className="eyebrow">SUBMATE</p>
      <h1>{signup ? '회원가입' : '다시 만나서 반가워요'}</h1>
      <p className="muted">
        {signup
          ? '계정을 만들고 원하는 상품을 구독하세요.'
          : '로그인하고 내 구독과 이용 내역을 확인하세요.'}
      </p>
      {!signup && state?.registered && (
        <p className="message success" role="status">
          회원가입이 완료되었습니다. 로그인해 주세요.
        </p>
      )}
      {!signup && authNotice && (
        <p className="message error" role="status">
          {authNotice}
        </p>
      )}
      <form onSubmit={handleSubmit}>
        <label htmlFor="auth-email">이메일</label>
        <input
          id="auth-email"
          type="email"
          autoComplete="username"
          required
          maxLength={255}
          value={email}
          disabled={busy}
          onChange={(event) => setEmail(event.target.value)}
        />
        <label htmlFor="auth-password">비밀번호</label>
        <input
          id="auth-password"
          type="password"
          autoComplete={signup ? 'new-password' : 'current-password'}
          required
          minLength={signup ? 8 : undefined}
          aria-describedby={signup ? 'password-help' : undefined}
          value={password}
          disabled={busy}
          onChange={(event) => setPassword(event.target.value)}
        />
        {signup && (
          <>
            <p id="password-help" className="muted auth-help">
              8자 이상, UTF-8 기준 72바이트 이하로 입력해 주세요.
            </p>
            <label htmlFor="auth-confirmation">비밀번호 확인</label>
            <input
              id="auth-confirmation"
              type="password"
              autoComplete="new-password"
              required
              value={confirmation}
              disabled={busy}
              onChange={(event) => setConfirmation(event.target.value)}
            />
          </>
        )}
        {error && (
          <p className="message error" role="alert">
            {error}
          </p>
        )}
        <button type="submit" className="primary full" disabled={busy}>
          {busy ? '확인 중…' : signup ? '회원가입' : '로그인'}
        </button>
      </form>
      <p className="auth-switch">
        {signup ? '이미 계정이 있나요? ' : '아직 계정이 없나요? '}
        <Link
          className="auth-link"
          to={signup ? '/login' : '/signup'}
          state={{ from: destination, email }}
        >
          {signup ? '로그인' : '회원가입'}
        </Link>
      </p>
      <p className="fine">새로고침하면 로그인이 해제됩니다.</p>
    </section>
  )
}
