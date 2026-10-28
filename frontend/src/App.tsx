import { useEffect, useState, type FormEvent } from 'react'
import axios from 'axios'
import { Link, Route, Routes, useNavigate } from 'react-router-dom'
import {
  ArrowDownRight,
  ArrowLeft,
  ArrowRight,
  ClipboardList,
  HeartHandshake,
  LogOut,
  MapPin,
  Minus,
  Plus,
  RefreshCw,
  Search,
  Settings2,
  ShieldCheck,
  ShoppingBag,
  Truck,
  X,
} from 'lucide-react'
import { api } from './api'
import { categories, products, type Product } from './products'
import './App.css'

type CartEntry = { product: Product; quantity: number }
type OrderForm = {
  customerName: string
  phone: string
  address: string
  latitude: string
  longitude: string
}

const initialOrder: OrderForm = {
  customerName: '',
  phone: '',
  address: '',
  latitude: '',
  longitude: '',
}

const money = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/admin" element={<AdminDashboard />} />
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route path="*" element={<HomePage />} />
    </Routes>
  )
}

function HomePage() {
  const [catalog, setCatalog] = useState(products)
  const [category, setCategory] = useState('All products')
  const [query, setQuery] = useState('')
  const [cart, setCart] = useState<CartEntry[]>([])
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)
  const [order, setOrder] = useState<OrderForm>(initialOrder)
  const [orderError, setOrderError] = useState('')
  const [orderSuccess, setOrderSuccess] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [locationStatus, setLocationStatus] = useState('')
  const [feedbackMessage, setFeedbackMessage] = useState('')
  const [feedbackError, setFeedbackError] = useState('')
  const [isSendingFeedback, setIsSendingFeedback] = useState(false)
  const [trackingNumber, setTrackingNumber] = useState('')
  const [trackingPhone, setTrackingPhone] = useState('')
  const [trackingMessage, setTrackingMessage] = useState('')
  const [trackingError, setTrackingError] = useState('')

  useEffect(() => {
    let active = true
    api.get<Array<Omit<Product, 'image' | 'tag'> & { imageUrl?: string }>>('/api/products')
      .then(({ data }) => {
        if (!active || !data.length) return
        setCatalog(data.map((product) => {
          const fallback = products.find((item) => item.id === product.id)
          return {
            ...product,
            price: Number(product.price),
            image: product.imageUrl || fallback?.image || '',
            tag: fallback?.tag ?? 'Selected with care',
          }
        }))
      })
      .catch(() => undefined)
    return () => { active = false }
  }, [])

  const filteredProducts = catalog.filter((product) => {
    const matchesCategory = category === 'All products' || product.category === category
    const matchesQuery = `${product.name} ${product.category}`.toLowerCase().includes(query.toLowerCase())
    return matchesCategory && matchesQuery
  })
  const itemCount = cart.reduce((total, entry) => total + entry.quantity, 0)

  function updateOrder(field: keyof OrderForm, value: string) {
    setOrder((current) => ({ ...current, [field]: value }))
  }

  function useCurrentLocation() {
    setLocationStatus('')
    if (!navigator.geolocation) {
      setLocationStatus('Location is not available in this browser. Enter coordinates manually.')
      return
    }
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setOrder((current) => ({ ...current, latitude: coords.latitude.toFixed(6), longitude: coords.longitude.toFixed(6) }))
        setLocationStatus('Location added. We will use it only to check branch service coverage.')
      },
      () => setLocationStatus('Location permission was not granted. You can enter coordinates manually.'),
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 60000 },
    )
  }

  function addToCart(product: Product) {
    setCart((current) => {
      const existing = current.find((entry) => entry.product.id === product.id)
      if (existing) {
        return current.map((entry) => entry.product.id === product.id
          ? { ...entry, quantity: entry.quantity + 1 }
          : entry)
      }
      return [...current, { product, quantity: 1 }]
    })
    setOrderError('')
    setOrderSuccess('')
    setIsDrawerOpen(true)
  }

  function changeQuantity(productId: string, delta: number) {
    setCart((current) => current
      .map((entry) => entry.product.id === productId
        ? { ...entry, quantity: entry.quantity + delta }
        : entry)
      .filter((entry) => entry.quantity > 0))
  }

  async function submitFeedback(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFeedbackError('')
    setFeedbackMessage('')
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    setIsSendingFeedback(true)
    try {
      await api.post('/api/feedback', {
        customerName: form.get('customerName'),
        email: form.get('email') || null,
        rating: Number(form.get('rating')),
        message: form.get('message'),
      })
      setFeedbackMessage('Thank you. Your feedback has been sent for review.')
      formElement.reset()
    } catch {
      setFeedbackError('Feedback could not be sent right now. Please try again later.')
    } finally {
      setIsSendingFeedback(false)
    }
  }

  async function trackOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setTrackingError('')
    setTrackingMessage('')
    try {
      const { data } = await api.get<{ orderNumber: string; status: string }>('/api/orders/track', {
        params: { orderNumber: trackingNumber.trim(), phone: trackingPhone.trim() },
      })
      setTrackingMessage(`${data.orderNumber} · ${data.status.replaceAll('_', ' ').toLowerCase()}`)
    } catch {
      setTrackingError('We could not find a matching request. Check the order number and phone number.')
    }
  }

  async function submitOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setOrderError('')
    setOrderSuccess('')
    if (!cart.length) {
      setOrderError('Add at least one product before sending your order request.')
      return
    }
    if (!order.latitude || !order.longitude) {
      setOrderError('Add your latitude and longitude so we can check the nearest service branch.')
      return
    }

    setIsSubmitting(true)
    try {
      const response = await api.post<{ orderNumber?: string }>('/api/orders', {
        customerName: order.customerName,
        customerPhone: order.phone,
        deliveryAddress: order.address,
        latitude: Number(order.latitude),
        longitude: Number(order.longitude),
        items: cart.map(({ product, quantity }) => ({ productId: product.id, quantity })),
      })
      const orderNumber = response.data.orderNumber ? ` #${response.data.orderNumber}` : ''
      setOrderSuccess(`Request${orderNumber} received. Our team will confirm availability and approval.`)
      setCart([])
      setOrder(initialOrder)
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data as { message?: string } | undefined)?.message
        : undefined
      setOrderError(message ?? 'We could not reach the order service. Please try again in a moment.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="site-shell">
      <div className="announcement">A little more care, delivered close to home <span aria-hidden="true">·</span> Local service, thoughtful essentials</div>
      <header className="site-header">
        <a className="brand" href="#top" aria-label="Sirji Homecare home">
          <span className="brand-mark"><HeartHandshake size={20} strokeWidth={1.7} /></span>
          <span className="brand-copy"><span className="brand-name">Sirji Homecare</span><span className="brand-caption">Care, close to home</span></span>
        </a>
        <nav className="main-nav" aria-label="Main navigation">
          <a href="#catalog">Shop essentials</a>
          <a href="#our-care">Our approach</a>
          <a href="#contact">Bulk & institutions</a>
        </nav>
        <div className="header-actions">
          <Link className="quiet-link" to="/admin/login">Admin</Link>
          <button className="bag-button" type="button" title="Open order request" aria-label={`Open order request, ${itemCount} items`} onClick={() => setIsDrawerOpen(true)}>
            <ShoppingBag size={17} strokeWidth={1.7} />
            {itemCount > 0 && <span className="bag-count">{itemCount}</span>}
          </button>
        </div>
      </header>

      <main id="top">
        <section className="hero" aria-labelledby="hero-title">
          <div className="hero-copy">
            <div className="eyebrow"><span className="eyebrow-mark" /> Homecare, made human</div>
            <h1 id="hero-title">Sirji Homecare</h1>
            <p>Everyday health essentials and thoughtful support, chosen for your home and brought a little closer.</p>
            <div className="hero-links">
              <a className="primary-button" href="#catalog">Explore the catalogue <ArrowRight size={15} /></a>
              <a className="text-button" href="#our-care">Our approach <ArrowDownRight size={15} /></a>
            </div>
          </div>
          <div className="hero-photo">
            <img src="https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=1400&q=88" alt="A care professional offering a thoughtful moment of support" />
            <div className="hero-note"><MapPin size={16} /><span>Neighbourhood care, thoughtfully delivered</span></div>
          </div>
        </section>

        <section className="benefit-strip" aria-label="Service values">
          <div className="benefit"><Truck size={18} /><span>Local delivery, within service areas</span></div>
          <div className="benefit"><ShieldCheck size={18} /><span>Products selected with care</span></div>
          <div className="benefit"><HeartHandshake size={18} /><span>Real people, ready to help</span></div>
        </section>

        <section className="catalog-section" id="catalog" aria-labelledby="catalog-title">
          <div className="section-heading">
            <div><div className="eyebrow"><span className="eyebrow-mark" /> The essentials shelf</div><h2 className="section-title" id="catalog-title">Care for everyday life</h2></div>
            <p className="section-intro">Browse practical home and wellness essentials. Send us an order request and our team will confirm the details with you.</p>
          </div>
          <div className="catalog-controls">
            <div className="category-list" aria-label="Filter by category">
              {categories.map((option) => <button key={option} type="button" className={`category-chip${category === option ? ' active' : ''}`} aria-pressed={category === option} onClick={() => setCategory(option)}>{option}</button>)}
            </div>
            <label className="search-wrap"><Search size={15} /><input className="search-input" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search essentials" aria-label="Search products" /></label>
          </div>
          <div className="product-grid">
            {filteredProducts.map((product, index) => (
              <article className="product-card" key={product.id} style={{ animationDelay: `${index * 55}ms` }}>
                <div className="product-image"><img src={product.image} alt={product.name} loading="lazy" /><span className="product-tag">{product.tag}</span><button className="product-add" type="button" title={`Add ${product.name} to order`} aria-label={`Add ${product.name} to order`} onClick={() => addToCart(product)}><Plus size={18} /></button></div>
                <div className="product-meta"><div><div className="product-category">{product.category} · {product.unit}</div><h3 className="product-name">{product.name}</h3></div><div className="product-price">{money.format(product.price)}</div></div>
              </article>
            ))}
            {filteredProducts.length === 0 && <div className="empty-state">No products match that search. Try another name or category.</div>}
          </div>
        </section>

        <section className="care-band" id="our-care">
          <div className="care-image"><img src="https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1100&q=85" alt="A homecare professional speaking with a patient" loading="lazy" /></div>
          <div className="care-copy"><div className="eyebrow"><span className="eyebrow-mark" /> Care beyond the catalogue</div><h2>Good care starts with listening.</h2><p>Looking for something in larger quantities or need help choosing? Tell us what your home, clinic or care team needs.</p><button className="text-button" type="button" onClick={() => setIsDrawerOpen(true)}>Start an enquiry <ArrowRight size={15} /></button></div>
        </section>

        <section className="community-section" id="community" aria-label="Customer feedback and order tracking">
          <div className="community-column">
            <div className="eyebrow"><span className="eyebrow-mark" /> A note from you</div>
            <h2 className="section-title">Help us care better.</h2>
            <p className="section-intro">Your feedback helps our team make everyday care more thoughtful.</p>
            <form className="feedback-form" onSubmit={submitFeedback}>
              <label className="field">Your name<input required name="customerName" maxLength={160} placeholder="Your name" /></label>
              <div className="feedback-row"><label className="field">Email, optional<input name="email" type="email" placeholder="you@example.com" /></label><label className="field">Rating<select name="rating" defaultValue="5"><option value="5">5 · Excellent</option><option value="4">4 · Good</option><option value="3">3 · Okay</option><option value="2">2 · Could improve</option><option value="1">1 · Needs attention</option></select></label></div>
              <label className="field">Your feedback<textarea required name="message" rows={3} maxLength={4000} placeholder="Tell us about your experience" /></label>
              {feedbackError && <div className="form-error" role="alert">{feedbackError}</div>}{feedbackMessage && <div className="form-success" role="status">{feedbackMessage}</div>}
              <button className="primary-button" type="submit" disabled={isSendingFeedback}>{isSendingFeedback ? 'Sending…' : <>Send feedback <ArrowRight size={15} /></>}</button>
            </form>
          </div>
          <div className="community-column tracking-column">
            <div className="eyebrow"><span className="eyebrow-mark" /> Keep in the loop</div>
            <h2 className="section-title">Track a request.</h2>
            <p className="section-intro">Already sent us an order request? Check its latest approval status.</p>
            <form className="tracking-form" onSubmit={trackOrder}>
              <label className="field">Order number<input required value={trackingNumber} onChange={(event) => setTrackingNumber(event.target.value)} placeholder="SH-XXXXXXXX" /></label>
              <label className="field">Phone number<input required type="tel" value={trackingPhone} onChange={(event) => setTrackingPhone(event.target.value)} placeholder="The number used for your request" /></label>
              {trackingError && <div className="form-error" role="alert">{trackingError}</div>}{trackingMessage && <div className="form-success" role="status">{trackingMessage}</div>}
              <button className="text-button" type="submit">Check request status <ArrowRight size={15} /></button>
            </form>
          </div>
        </section>
      </main>

      <footer className="site-footer" id="contact">
        <Link className="footer-brand" to="/">Sirji Homecare</Link>
        <span>Thoughtful essentials for the places we call home.</span>
        <div className="footer-links"><a href="#catalog">Catalogue</a><a href="#community">Feedback</a><Link to="/admin/login">Administration</Link></div>
      </footer>

      {isDrawerOpen && <div className="drawer-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setIsDrawerOpen(false) }}>
        <aside className="order-drawer" role="dialog" aria-modal="true" aria-labelledby="order-title">
          <div className="drawer-heading"><div><div className="eyebrow"><span className="eyebrow-mark" /> Your request</div><h2 id="order-title">A little more care</h2></div><button className="icon-button" type="button" title="Close order request" aria-label="Close order request" onClick={() => setIsDrawerOpen(false)}><X size={18} /></button></div>
          {cart.length === 0 ? <div className="drawer-empty">{orderSuccess ? <div className="form-success" role="status">{orderSuccess}</div> : <>Your request list is empty. Choose an essential from the catalogue and it will appear here.</>}<br /><button className="text-button" type="button" onClick={() => setIsDrawerOpen(false)}>Continue browsing <ArrowRight size={14} /></button></div> : <>
            <div className="cart-items">{cart.map(({ product, quantity }) => <div className="cart-row" key={product.id}><img className="cart-thumb" src={product.image} alt="" /><div><div className="cart-row-name">{product.name}</div><div className="cart-row-sub">{money.format(product.price)} · {product.unit}</div></div><div className="quantity-control"><button type="button" title={`Remove one ${product.name}`} aria-label={`Remove one ${product.name}`} onClick={() => changeQuantity(product.id, -1)}><Minus size={12} /></button><span>{quantity}</span><button type="button" title={`Add one ${product.name}`} aria-label={`Add one ${product.name}`} onClick={() => changeQuantity(product.id, 1)}><Plus size={12} /></button></div></div>)}</div>
            <form onSubmit={submitOrder}>
              <div className="form-section"><h3>Who should we contact?</h3><label className="field">Full name<input required autoComplete="name" value={order.customerName} onChange={(event) => updateOrder('customerName', event.target.value)} placeholder="Your name" /></label><label className="field">Phone number<input required autoComplete="tel" type="tel" pattern="[0-9+() -]{8,18}" value={order.phone} onChange={(event) => updateOrder('phone', event.target.value)} placeholder="e.g. +91 98765 43210" /></label><label className="field">Delivery address<input required autoComplete="street-address" value={order.address} onChange={(event) => updateOrder('address', event.target.value)} placeholder="House, street, locality" /></label></div>
              <div className="form-section"><h3>Check your service area</h3><button className="text-button location-button" type="button" onClick={useCurrentLocation}><MapPin size={14} /> Use my current location</button>{locationStatus && <span className="cart-row-sub" role="status">{locationStatus}</span>}<div className="coordinate-grid"><label className="field">Latitude<input required inputMode="decimal" type="number" min="-90" max="90" step="any" value={order.latitude} onChange={(event) => updateOrder('latitude', event.target.value)} placeholder="12.9716" /></label><label className="field">Longitude<input required inputMode="decimal" type="number" min="-180" max="180" step="any" value={order.longitude} onChange={(event) => updateOrder('longitude', event.target.value)} placeholder="77.5946" /></label></div><span className="cart-row-sub">Our team checks the nearest branch before confirming your request.</span></div>
              {orderError && <div className="form-error" role="alert">{orderError}</div>}{orderSuccess && <div className="form-success" role="status">{orderSuccess}</div>}
              <button className="primary-button submit-order" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Sending request…' : <>Send order request <ArrowRight size={15} /></>}</button>
            </form>
          </>}
        </aside>
      </div>}
    </div>
  )
}

type AdminOrder = {
  id: string
  orderNumber: string
  customerName: string
  customerPhone: string
  deliveryAddress: string
  status: string
  bulkOrder: boolean
  branchName: string
  adminNotes?: string
  createdAt: string
  items: Array<{ productId: string; productName: string; quantity: number; unitPrice: number }>
}

function AdminDashboard() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState<AdminOrder[]>([])
  const [statusFilter, setStatusFilter] = useState('PENDING_APPROVAL')
  const [refreshKey, setRefreshKey] = useState(0)
  const [bulkThreshold, setBulkThreshold] = useState('50')
  const [isLoading, setIsLoading] = useState(true)
  const [savingSetting, setSavingSetting] = useState(false)
  const [busyOrder, setBusyOrder] = useState('')
  const [adminError, setAdminError] = useState('')
  const [settingsMessage, setSettingsMessage] = useState('')

  useEffect(() => {
    if (!localStorage.getItem('sirji-admin-token')) {
      navigate('/admin/login', { replace: true })
      return
    }
    let active = true
    const orderConfig = statusFilter === 'ALL' ? {} : { params: { status: statusFilter } }
    Promise.all([
      api.get<AdminOrder[]>('/api/admin/orders', orderConfig),
      api.get<Record<string, string>>('/api/admin/settings'),
    ]).then(([ordersResponse, settingsResponse]) => {
      if (!active) return
      setOrders(ordersResponse.data)
      setBulkThreshold(settingsResponse.data['bulk.quantity.threshold'] ?? '50')
      setAdminError('')
    }).catch(() => {
      if (active) setAdminError('The admin service could not be reached. Sign in again or retry when the API is available.')
    }).finally(() => {
      if (active) setIsLoading(false)
    })
    return () => { active = false }
  }, [navigate, refreshKey, statusFilter])

  async function reviewOrder(orderId: string, approved: boolean) {
    setBusyOrder(orderId)
    setAdminError('')
    try {
      const { data } = await api.patch<AdminOrder>(`/api/admin/orders/${orderId}/approval`, { approved })
      setOrders((current) => current.map((order) => order.id === orderId ? data : order))
    } catch {
      setAdminError('That order could not be updated. Refresh and try again.')
    } finally {
      setBusyOrder('')
    }
  }

  async function fulfillOrder(orderId: string) {
    setBusyOrder(orderId)
    setAdminError('')
    try {
      const { data } = await api.patch<AdminOrder>(`/api/admin/orders/${orderId}/fulfillment`)
      setOrders((current) => current.map((order) => order.id === orderId ? data : order))
    } catch {
      setAdminError('That order could not be updated. Refresh and try again.')
    } finally {
      setBusyOrder('')
    }
  }

  async function saveThreshold(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSettingsMessage('')
    setAdminError('')
    setSavingSetting(true)
    try {
      await api.patch('/api/admin/settings/bulk.quantity.threshold', { value: bulkThreshold })
      setSettingsMessage('Bulk quantity threshold saved.')
    } catch {
      setAdminError('The setting could not be saved. Enter a whole number from 1 to 100000.')
    } finally {
      setSavingSetting(false)
    }
  }

  function signOut() {
    localStorage.removeItem('sirji-admin-token')
    navigate('/admin/login', { replace: true })
  }

  return <main className="admin-workspace">
    <header className="admin-header"><Link className="brand" to="/"><span className="brand-mark"><HeartHandshake size={20} /></span><span className="brand-copy"><span className="brand-name">Sirji Homecare</span><span className="brand-caption">Administration</span></span></Link><button className="text-button" type="button" onClick={signOut}><LogOut size={15} /> Sign out</button></header>
    <div className="admin-content">
      <div className="admin-title-row"><div><div className="eyebrow"><span className="eyebrow-mark" /> Operations</div><h1>Order requests</h1></div><button className="icon-button" type="button" title="Refresh orders" aria-label="Refresh orders" onClick={() => { setIsLoading(true); setRefreshKey((current) => current + 1) }}><RefreshCw size={16} /></button></div>
      {adminError && <div className="form-error" role="alert">{adminError}</div>}
      <section className="admin-settings"><div className="settings-icon"><Settings2 size={18} /></div><div className="settings-copy"><strong>Bulk order threshold</strong><span>Requests at or above this total quantity are flagged for bulk review.</span></div><form className="threshold-form" onSubmit={saveThreshold}><label className="field"><span className="sr-only">Bulk quantity threshold</span><input type="number" min="1" max="100000" step="1" required value={bulkThreshold} onChange={(event) => setBulkThreshold(event.target.value)} /></label><button className="primary-button" type="submit" disabled={savingSetting}>{savingSetting ? 'Saving…' : 'Save'}</button></form>{settingsMessage && <span className="settings-message" role="status">{settingsMessage}</span>}</section>
      <section className="admin-orders-section"><div className="admin-orders-heading"><div><h2>Order queue</h2><span>{orders.length} {orders.length === 1 ? 'request' : 'requests'}</span></div><label className="field status-filter">Status<select value={statusFilter} onChange={(event) => { setIsLoading(true); setStatusFilter(event.target.value) }}><option value="PENDING_APPROVAL">Awaiting review</option><option value="APPROVED">Approved</option><option value="REJECTED">Rejected</option><option value="FULFILLED">Fulfilled</option><option value="ALL">All requests</option></select></label></div>
        {isLoading ? <div className="admin-empty">Loading requests…</div> : orders.length === 0 ? <div className="admin-empty"><ClipboardList size={23} /><span>No requests in this view.</span></div> : <div className="admin-order-list">{orders.map((order) => <article className="admin-order" key={order.id}><div className="admin-order-top"><div><span className="order-number">{order.orderNumber}</span>{order.bulkOrder && <span className="bulk-label">Bulk</span>}</div><span className={`status-label status-${order.status.toLowerCase()}`}>{order.status.replaceAll('_', ' ')}</span></div><div className="admin-order-details"><div><span>Customer</span><strong>{order.customerName}</strong><a href={`tel:${order.customerPhone}`}>{order.customerPhone}</a></div><div><span>Delivery</span><strong>{order.branchName}</strong><p>{order.deliveryAddress}</p></div><div><span>Items</span><strong>{order.items.reduce((sum, item) => sum + item.quantity, 0)} units</strong><p>{order.items.map((item) => `${item.productName} × ${item.quantity}`).join(', ')}</p></div></div>{order.status === 'PENDING_APPROVAL' && <div className="order-actions"><button className="approve-button" type="button" disabled={busyOrder === order.id} onClick={() => reviewOrder(order.id, true)}>Approve</button><button className="reject-button" type="button" disabled={busyOrder === order.id} onClick={() => reviewOrder(order.id, false)}>Reject</button></div>}{order.status === 'APPROVED' && <div className="order-actions"><button className="approve-button" type="button" disabled={busyOrder === order.id} onClick={() => fulfillOrder(order.id)}>Mark fulfilled</button></div>}</article>)}</div>}
      </section>
    </div>
  </main>
}

function AdminLogin() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function signIn(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      const response = await api.post<{ token: string }>('/api/auth/login', { email, password })
      localStorage.setItem('sirji-admin-token', response.data.token)
      navigate('/admin')
    } catch {
      setError('Sign-in is unavailable. Check your details and confirm the API is running.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return <main className="admin-page"><section className="admin-panel"><Link className="brand" to="/"><span className="brand-mark"><HeartHandshake size={20} /></span><span className="brand-copy"><span className="brand-name">Sirji Homecare</span><span className="brand-caption">Administration</span></span></Link><h1>Welcome back.</h1><p>Sign in to review order requests and manage the catalogue.</p><form className="admin-form" onSubmit={signIn}><label className="field">Work email<input type="email" autoComplete="username" required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="admin@example.com" /></label><label className="field">Password<input type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Your password" /></label>{error && <div className="form-error" role="alert">{error}</div>}<button className="primary-button" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Signing in…' : <>Sign in <ArrowRight size={15} /></>}</button></form><Link className="back-link" to="/"><ArrowLeft size={14} /> Back to the catalogue</Link></section></main>
}

export default App
