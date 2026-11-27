import { useContext } from 'react'
import { AuthContext } from '../context/authState'
export const useAuth = () => useContext(AuthContext)
