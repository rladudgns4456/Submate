import { AuthProvider } from './context/AuthContext.jsx'
import AppRouter from './routes/AppRouter'
import './styles/subscriptions.css'

function App({ session = null, onLogout }) {
  return (
    <AuthProvider
      key={session?.accessToken || 'standalone'}
      initialSession={session}
      onLogout={onLogout}
    >
      <AppRouter />
    </AuthProvider>
  )
}

export default App
