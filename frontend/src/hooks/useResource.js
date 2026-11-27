import { useCallback, useEffect, useState } from 'react'
import { errorMessage } from '../api/axios'

export function useResource(loader) {
  const [state, setState] = useState(null)
  const [revision, setRevision] = useState(0)
  const reload = useCallback(() => setRevision((value) => value + 1), [])
  useEffect(() => {
    let active = true
    loader()
      .then((data) => {
        if (active) setState({ data, error: '', loader, revision })
      })
      .catch((error) => {
        if (active)
          setState({ data: null, error: errorMessage(error), loader, revision })
      })
    return () => {
      active = false
    }
  }, [loader, revision])
  const current = state?.loader === loader && state?.revision === revision
  return {
    data: current ? state.data : null,
    error: current ? state.error : '',
    loading: !current,
    reload,
  }
}
