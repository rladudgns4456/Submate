import {
  BrowserRouter,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import SubscriptionLayout from '../layouts/SubscriptionLayout'
import ConnectionPage from '../pages/subscription/ConnectionPage'
import SubscriptionsPage from '../pages/subscription/SubscriptionsPage'
import SubscriptionDetailPage from '../pages/subscription/SubscriptionDetailPage'
import PaymentPage from '../pages/payment/PaymentPage'
import PaymentResultPage from '../pages/payment/PaymentResultPage'
import PaymentsPage from '../pages/payment/PaymentsPage'
import RefundsPage from '../pages/admin/RefundsPage'
import NoticesPage from '../pages/notice/NoticesPage'
import NoticeDetailPage from '../pages/notice/NoticeDetailPage'

function PrivateRoute({ admin = false }) {
  const { session } = useAuth()
  const location = useLocation()
  if (!session)
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  if (admin && session.role !== 'ADMIN')
    return (
      <section className="panel">
        <h1>접근 권한이 없습니다</h1>
        <p>관리자 계정으로 로그인해 주세요.</p>
      </section>
    )
  return <Outlet />
}
export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<SubscriptionLayout />}>
          <Route path="/login" element={<ConnectionPage />} />

          <Route path="/notices" element={<NoticesPage />} />
          <Route path="/notices/:id" element={<NoticeDetailPage />} />
          <Route element={<PrivateRoute />}>
            <Route index element={<Navigate to="/subscriptions" replace />} />
            <Route
              path="/subscriptions"
              element={<SubscriptionsPage key="user" />}
            />
            <Route
              path="/subscriptions/:id"
              element={<SubscriptionDetailPage key="user-detail" />}
            />
            <Route path="/subscribe/:productId" element={<PaymentPage />} />
            <Route path="/payment/result" element={<PaymentResultPage />} />
            <Route
              path="/payments"
              element={<PaymentsPage key="user-payments" />}
            />
            <Route
              path="/refunds"
              element={<RefundsPage key="user-refunds" />}
            />
          </Route>
          <Route element={<PrivateRoute admin />}>
            <Route
              path="/admin/subscriptions"
              element={<SubscriptionsPage key="admin" admin />}
            />
            <Route
              path="/admin/subscriptions/:id"
              element={<SubscriptionDetailPage key="admin-detail" admin />}
            />
            <Route
              path="/admin/payments"
              element={<PaymentsPage key="admin-payments" admin />}
            />
            <Route
              path="/admin/refunds"
              element={<RefundsPage key="admin-refunds" admin />}
            />
          </Route>
          <Route
            path="*"
            element={
              <section className="empty">
                <h1>페이지를 찾을 수 없습니다</h1>
                <a href="/subscriptions">내 구독으로 이동</a>
              </section>
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
