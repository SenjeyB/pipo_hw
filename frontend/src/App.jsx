import React, { useState, createContext, useContext, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate, Link, useNavigate } from 'react-router-dom'
import * as api from './api'

// --- Auth Context ---
const AuthContext = createContext(null)

function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (token) {
      api.getMe()
        .then(res => setUser(res.data))
        .catch(() => localStorage.removeItem('token'))
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [])

  const loginUser = async (username, password) => {
    const res = await api.login({ username, password })
    localStorage.setItem('token', res.data.token)
    setUser(res.data.user)
  }

  const registerUser = async (username, email, password) => {
    const res = await api.register({ username, email, password })
    localStorage.setItem('token', res.data.token)
    setUser(res.data.user)
  }

  const logout = () => {
    localStorage.removeItem('token')
    setUser(null)
  }

  if (loading) return <div className="container"><p>Loading...</p></div>

  return (
    <AuthContext.Provider value={{ user, loginUser, registerUser, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

function useAuth() {
  return useContext(AuthContext)
}

// --- Login Page ---
function LoginPage() {
  const { loginUser } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await loginUser(username, password)
      navigate('/')
    } catch {
      setError('Invalid credentials')
    }
  }

  return (
    <div className="container">
      <h2>Login</h2>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input value={username} onChange={e => setUsername(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          {error && <p className="error">{error}</p>}
          <button className="btn" type="submit">Login</button>
        </form>
        <p style={{marginTop:12}}>
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  )
}

// --- Register Page ---
function RegisterPage() {
  const { registerUser } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await registerUser(username, email, password)
      navigate('/')
    } catch {
      setError('Registration failed (username or email may be taken)')
    }
  }

  return (
    <div className="container">
      <h2>Register</h2>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input value={username} onChange={e => setUsername(e.target.value)} required minLength={3} />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={6} />
          </div>
          {error && <p className="error">{error}</p>}
          <button className="btn" type="submit">Register</button>
        </form>
        <p style={{marginTop:12}}>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  )
}

// --- My Intervals Page ---
function MyIntervalsPage() {
  const [intervals, setIntervals] = useState([])
  const [title, setTitle] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [error, setError] = useState('')

  const load = () => {
    api.getMyIntervals().then(r => setIntervals(r.data || []))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.createInterval({
        title,
        start_time: new Date(startTime).toISOString(),
        end_time: new Date(endTime).toISOString(),
      })
      setTitle(''); setStartTime(''); setEndTime('')
      load()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create interval')
    }
  }

  const handleDelete = async (id) => {
    await api.deleteInterval(id)
    load()
  }

  return (
    <div className="container">
      <h2>My Time Slots</h2>
      <div className="card">
        <h3 style={{marginBottom:12}}>Create New Slot</h3>
        <form onSubmit={handleCreate}>
          <div className="form-group">
            <label>Title</label>
            <input value={title} onChange={e => setTitle(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Start Time</label>
            <input type="datetime-local" value={startTime} onChange={e => setStartTime(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>End Time</label>
            <input type="datetime-local" value={endTime} onChange={e => setEndTime(e.target.value)} required />
          </div>
          {error && <p className="error">{error}</p>}
          <button className="btn" type="submit">Create</button>
        </form>
      </div>

      {intervals.map(iv => (
        <div className="card" key={iv.id}>
          <strong>{iv.title}</strong>
          <span className={`tag ${iv.is_booked ? 'tag-booked' : 'tag-available'}`} style={{marginLeft:10}}>
            {iv.is_booked ? 'Booked' : 'Available'}
          </span>
          <p style={{marginTop:8, fontSize:14, color:'#666'}}>
            {new Date(iv.start_time).toLocaleString()} — {new Date(iv.end_time).toLocaleString()}
          </p>
          {iv.booked_by && <p style={{fontSize:13, color:'#888'}}>Booked by: {iv.booked_by}</p>}
          <button className="btn btn-danger" style={{marginTop:8}} onClick={() => handleDelete(iv.id)}>
            Delete
          </button>
        </div>
      ))}
      {intervals.length === 0 && <p>No intervals yet. Create one above!</p>}
    </div>
  )
}

// --- Available Intervals Page ---
function AvailablePage() {
  const { user } = useAuth()
  const [intervals, setIntervals] = useState([])

  const load = () => {
    api.getAvailableIntervals().then(r => setIntervals(r.data || []))
  }

  useEffect(() => { load() }, [])

  const handleBook = async (id) => {
    await api.bookInterval(id)
    load()
  }

  return (
    <div className="container">
      <h2>Available Time Slots</h2>
      {intervals.map(iv => (
        <div className="card" key={iv.id}>
          <strong>{iv.title}</strong>
          <span className="tag tag-available" style={{marginLeft:10}}>Available</span>
          <p style={{marginTop:8, fontSize:14, color:'#666'}}>
            {new Date(iv.start_time).toLocaleString()} — {new Date(iv.end_time).toLocaleString()}
          </p>
          <p style={{fontSize:13, color:'#888'}}>Owner: {iv.owner_id}</p>
          {iv.owner_id !== user?.id && (
            <button className="btn btn-success" style={{marginTop:8}} onClick={() => handleBook(iv.id)}>
              Book This Slot
            </button>
          )}
        </div>
      ))}
      {intervals.length === 0 && <p>No available slots at the moment.</p>}
    </div>
  )
}

// --- Navigation ---
function Navbar() {
  const { user, logout } = useAuth()
  return (
    <nav>
      <div>
        <Link to="/">🕐 Pipo-Go</Link>
        {user && <Link to="/my">My Slots</Link>}
        {user && <Link to="/available">Browse Slots</Link>}
      </div>
      <div>
        {user ? (
          <>
            <span style={{marginRight:12}}>Hi, {user.username}</span>
            <button className="btn" style={{background:'#1e40af'}} onClick={logout}>Logout</button>
          </>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </div>
    </nav>
  )
}

function PrivateRoute({ children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" />
  return children
}

// --- App ---
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/my" element={<PrivateRoute><MyIntervalsPage /></PrivateRoute>} />
          <Route path="/available" element={<PrivateRoute><AvailablePage /></PrivateRoute>} />
          <Route path="/" element={<PrivateRoute><MyIntervalsPage /></PrivateRoute>} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
