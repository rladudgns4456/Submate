import assert from 'node:assert/strict'
import { afterEach, test } from 'node:test'
import api, { setAuthorization } from '../src/api/axios.js'
import { authApi } from '../src/api/authApi.js'
import { afterLoginPath } from '../src/utils/authNavigation.js'

const originalAdapter = api.defaults.adapter
afterEach(() => {
  api.defaults.adapter = originalAdapter
  setAuthorization(null)
})

function respondWith(data, inspect) {
  api.defaults.adapter = async (config) => {
    inspect(config)
    return { data, status: 200, statusText: 'OK', headers: {}, config }
  }
}

test('새 로그인은 이전 회원의 Authorization을 전송하지 않고 기존 token 응답을 받는다', async () => {
  setAuthorization('Bearer previous-member')
  respondWith({ token: 'new-member-token' }, (config) => {
    assert.equal(config.url, '/auth/login')
    assert.equal(config.method, 'post')
    assert.equal(config.headers.get('Authorization'), undefined)
    assert.deepEqual(JSON.parse(config.data), {
      email: 'student@example.test',
      password: 'test-password',
    })
  })
  assert.deepEqual(
    await authApi.login('student@example.test', 'test-password'),
    { token: 'new-member-token' }
  )
})

test('회원가입은 권한/회원 ID를 보내지 않고 이전 로그인 헤더도 제거한다', async () => {
  setAuthorization('Bearer previous-member')
  respondWith({ memberId: 7 }, (config) => {
    assert.equal(config.url, '/auth/signup')
    assert.equal(config.headers.get('Authorization'), undefined)
    assert.deepEqual(JSON.parse(config.data), {
      email: 'new@example.test',
      password: 'test-password',
    })
  })
  assert.deepEqual(await authApi.signup('new@example.test', 'test-password'), {
    memberId: 7,
  })
})

test('본인 정보 검증은 새 토큰을 사용하며 만료 검사 재귀를 방지한다', async () => {
  setAuthorization('Bearer previous-member')
  const member = { memberId: 7, email: 'new@example.test', role: 'USER' }
  respondWith(member, (config) => {
    assert.equal(config.url, '/auth/me')
    assert.equal(config.headers.get('Authorization'), 'Bearer current-member')
    assert.equal(config.skipSessionCheck, true)
  })
  assert.deepEqual(await authApi.me('current-member'), member)
})

test('로그아웃 후 일반 요청에는 이전 인증 헤더가 남지 않는다', async () => {
  setAuthorization('Bearer previous-member')
  setAuthorization(null)
  respondWith([], (config) =>
    assert.equal(config.headers.get('Authorization'), undefined)
  )
  await api.get('/products')
})

test('로그인 복귀는 앱 내부 경로만 허용하고 로그인/가입 순환을 막는다', () => {
  for (const value of [
    undefined,
    '',
    'https://outside.example',
    '//outside.example',
    '/\\outside.example',
    '/login',
    '/login?next=1',
    '/signup#form',
  ]) {
    assert.equal(afterLoginPath(value), '/products')
  }
  assert.equal(
    afterLoginPath('/subscribe/2?source=products'),
    '/subscribe/2?source=products'
  )
  assert.equal(afterLoginPath('/refunds'), '/refunds')
})
