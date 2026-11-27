import { createContext } from 'react'
export const AuthContext = createContext(null)
export const isDemo =
  import.meta.env.DEV && import.meta.env.VITE_SUBMATE_DEMO === 'true'
