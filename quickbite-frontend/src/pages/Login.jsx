import React, { useState } from 'react';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    sessionStorage.clear(); // Hard reset for new login

    try {
      const response = await api.post('/auth/login', { email, password });
      if (response.data.accessToken) {
        sessionStorage.setItem('token', response.data.accessToken);
        sessionStorage.setItem('customerId', response.data.userId); 
        sessionStorage.setItem('role', response.data.role);
        
        // Redirect based on role
        if (response.data.role === 'OWNER') {
          try {
            // Fetch the specific restaurant owned by this user
            const myRestRes = await api.get(`/api/restaurants/owner/${response.data.userId}`);
            const myRestaurant = myRestRes.data;
            
            if (myRestaurant) {
              sessionStorage.setItem('restaurantId', myRestaurant.id);
              navigate(`/restaurant-dashboard/${myRestaurant.id}`);
            }
          } catch (err) {
            console.error("No restaurant profile found for this owner", err);
            // If no restaurant yet, maybe redirect to a "Register Restaurant" page or home
            navigate('/');
          }
        } else if (response.data.role === 'AGENT') {
          try {
            // Check if agent profile exists
            const agentRes = await api.get(`/api/delivery/agents/user/${response.data.userId}`);
            sessionStorage.setItem('agentId', agentRes.data.id);
            navigate(`/agent-dashboard/${agentRes.data.id}`);
          } catch (err) {
            // Auto register agent profile
            if (err.response && err.response.status === 404 || err.response && err.response.status === 500) {
              try {
                const newAgentRes = await api.post('/api/delivery/agents', {
                  userId: response.data.userId,
                  name: `Agent ${response.data.userId}`,
                  phone: `00000${response.data.userId}`,
                  initialLocation: 'City Center'
                });
                sessionStorage.setItem('agentId', newAgentRes.data.id);
                navigate(`/agent-dashboard/${newAgentRes.data.id}`);
              } catch (createErr) {
                console.error("Failed to create agent profile", createErr);
                navigate('/');
              }
            } else {
              console.error("Error fetching agent profile", err);
              navigate('/');
            }
          }
        } else {
          navigate('/');
        }
      }
    } catch (err) {
      console.error("Login error:", err);
      const errorMsg = err.response?.data?.message || err.response?.data || "Invalid email or password";
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="text-center mb-4">
          <h2 className="fw-bold text-dark">Welcome Back</h2>
          <p className="text-muted">Login to continue your delicious journey</p>
        </div>

        {error && <Alert variant="danger">{error}</Alert>}

        <Form onSubmit={handleLogin}>
          <Form.Group className="mb-3">
            <Form.Label className="fw-medium">Email Address</Form.Label>
            <Form.Control 
              type="email" 
              className="auth-input" 
              placeholder="Enter your email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required 
            />
          </Form.Group>

          <Form.Group className="mb-4">
            <Form.Label className="fw-medium">Password</Form.Label>
            <Form.Control 
              type="password" 
              className="auth-input" 
              placeholder="Enter your password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required 
            />
          </Form.Group>

          <Button type="submit" className="btn-primary-custom w-100 py-2 mb-3" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </Button>

          <div className="text-center mt-3">
            <span className="text-muted">Don't have an account? </span>
            <Link to="/register" className="text-primary-custom text-decoration-none fw-medium">Sign Up</Link>
          </div>
        </Form>
      </div>
    </div>
  );
};

export default Login;
