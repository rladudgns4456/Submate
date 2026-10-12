import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { isDemo } from '../context/authState'

export default function SubscriptionLayout() {
  const { session, logout } = useAuth()
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink className="brand" to="/subscriptions">
          <span className="brand-symbol">s</span>SubMate
          <span className="brand-dot">.</span>
        </NavLink>
        <p className="nav-label">MY SPACE</p>
        <nav aria-label="사용자 메뉴">
          <NavLink to="/subscriptions">
            <span>▦</span>내 구독
          </NavLink>
          <NavLink to="/payments">
            <span>▤</span>결제 내역
          </NavLink>
          <NavLink to="/refunds">
            <span>↩</span>환불 내역
          </NavLink>
          <NavLink to="/notices">
          <span>📢</span>공지사항
          </NavLink>
        </nav>
        {session?.role === 'ADMIN' && (
          <>
            <p className="nav-label">ADMIN</p>
            <nav aria-label="관리자 메뉴">
              <NavLink to="/admin/subscriptions">구독 관리</NavLink>
              <NavLink to="/admin/payments">결제 관리</NavLink>
              <NavLink to="/admin/refunds">환불 관리</NavLink>
            </nav>
          </>
        )}
        <div className="sidebar-note">
          <strong>내 구독, 내 페이스대로.</strong>
          <p>
            이용 현황을 확인하고
            <br />
            필요할 때 관리하세요.
          </p>
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <span>{isDemo ? 'LOCAL DEMO · 가상 결제' : '구독 관리'}</span>
          <div>
            <span className="account">{session?.email}</span>
            {session && (
              <button className="text-button" onClick={logout}>
                로그아웃
              </button>
            )}
          </div>
        </header>
        <main>
          <Outlet />
        </main>
        <footer>
          SubMate <span>구독 · 결제 · 환불</span>
        </footer>
      </div>
    </div>
  )
}
