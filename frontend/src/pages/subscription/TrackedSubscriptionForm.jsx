const defaults = {
  name: '',
  amount: '',
  billingCycle: 'MONTHLY',
  billingDate: '',
  active: true,
  reminderDays: 3,
}

export default function TrackedSubscriptionForm({
  initial,
  busy,
  onSave,
  onClose,
}) {
  const values = { ...defaults, ...initial }

  function submit(event) {
    event.preventDefault()
    if (busy) return
    const form = Object.fromEntries(new FormData(event.currentTarget))
    onSave({
      name: form.name.trim(),
      amount: Number(form.amount),
      billingCycle: form.billingCycle,
      billingDate: form.billingDate,
      active: form.active === 'true',
      reminderDays: form.reminderDays === '' ? null : Number(form.reminderDays),
    })
  }

  return (
    <form className="tracker-form" onSubmit={submit}>
      <fieldset disabled={busy}>
        <div className="tracker-fields">
          <label>
            서비스 이름
            <input
              required
              maxLength={100}
              name="name"
              defaultValue={values.name}
            />
          </label>
          <label>
            결제 금액 (원)
            <input
              required
              type="number"
              min={1}
              max={999999999}
              step={1}
              name="amount"
              defaultValue={values.amount}
            />
          </label>
          <label>
            결제 주기
            <select
              required
              name="billingCycle"
              defaultValue={values.billingCycle}
            >
              <option value="">주기를 선택하세요</option>
              <option value="MONTHLY">매월</option>
              <option value="YEARLY">매년</option>
            </select>
          </label>
          <label>
            기준 결제일
            <input
              required
              type="date"
              min="1900-01-01"
              max="9998-12-31"
              name="billingDate"
              defaultValue={values.billingDate}
            />
          </label>
          <label>
            앱 알림 (결제 며칠 전)
            <input
              type="number"
              min={0}
              max={30}
              step={1}
              name="reminderDays"
              defaultValue={values.reminderDays ?? ''}
              placeholder="비워 두면 알림 끔"
            />
          </label>
          <label>
            구독 상태
            <select name="active" defaultValue={String(values.active)}>
              <option value="true">이용 중</option>
              <option value="false">중지</option>
            </select>
          </label>
        </div>
        <p className="muted">
          기준 결제일을 기준으로 이후 결제일을 계산합니다. 알림에 0을 입력하면
          결제 당일에 표시됩니다.
        </p>
        <div className="tracker-actions">
          <button className="primary" type="submit">
            {busy ? '저장 중…' : '저장'}
          </button>
          <button type="button" onClick={onClose}>
            닫기
          </button>
        </div>
      </fieldset>
    </form>
  )
}
