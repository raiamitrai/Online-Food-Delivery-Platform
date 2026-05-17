import React, { useState } from 'react';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api';

const Register = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('CUSTOMER');
  
  // Restaurant Owner Specific Fields
  const [restaurantName, setRestaurantName] = useState('');
  const [cuisine, setCuisine] = useState('');
  const [location, setLocation] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Step 1: Register User
      const authRes = await api.post('/auth/register', { name, email, password, role });
      
      // Step 2: If Owner, Create Restaurant using the returned token
      if (role === 'OWNER') {
        const token = authRes.data.accessToken;
        const ownerId = authRes.data.userId;
        
        await api.post(
          '/api/restaurants', 
          { 
            name: restaurantName, 
            cuisine: cuisine, 
            location: location, 
            ownerId: ownerId 
          },
          {
            headers: { Authorization: `Bearer ${token}` }
          }
        );
      }

      // Step 3: Navigate to login
      navigate('/login');
    } catch (err) {
      console.error("Registration error:", err);
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || 'Registration failed';
      setError(`Registration failed: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ maxHeight: '90vh', overflowY: 'auto' }}>
        <div className="text-center mb-4">
          <h2 className="fw-bold text-dark">Create Account</h2>
          <p className="text-muted">Join QuickBite today</p>
        </div>

        {error && <Alert variant="danger">{error}</Alert>}

        <Form onSubmit={handleRegister}>
          <Form.Group className="mb-3">
            <Form.Label className="fw-medium">Full Name</Form.Label>
            <Form.Control 
              type="text" 
              className="auth-input" 
              placeholder="Enter your name" 
              value={name}
              onChange={(e) => setName(e.target.value)}
              required 
            />
          </Form.Group>

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

          <Form.Group className="mb-3">
            <Form.Label className="fw-medium">Password</Form.Label>
            <Form.Control 
              type="password" 
              className="auth-input" 
              placeholder="Create a password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required 
            />
          </Form.Group>
          
          <Form.Group className="mb-4">
            <Form.Label className="fw-medium">I am a</Form.Label>
            <Form.Select className="auth-input" value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="CUSTOMER">Customer</option>
                <option value="OWNER">Restaurant Owner</option>
                <option value="AGENT">Delivery Agent</option>
            </Form.Select>
          </Form.Group>

          {/* Conditional Restaurant Fields */}
          {role === 'OWNER' && (
            <div className="bg-light p-3 rounded mb-4 border">
              <h6 className="fw-bold mb-3 text-primary-custom">Restaurant Details</h6>
              <Form.Group className="mb-3">
                <Form.Label className="fw-medium">Restaurant Name</Form.Label>
                <Form.Control 
                  type="text" 
                  className="auth-input" 
                  placeholder="e.g. Spice Route" 
                  value={restaurantName}
                  onChange={(e) => setRestaurantName(e.target.value)}
                  required 
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label className="fw-medium">Cuisine</Form.Label>
                <Form.Control 
                  type="text" 
                  className="auth-input" 
                  placeholder="e.g. North Indian, Fast Food" 
                  value={cuisine}
                  onChange={(e) => setCuisine(e.target.value)}
                  required 
                />
              </Form.Group>
              <Form.Group className="mb-2">
                <Form.Label className="fw-medium">Location</Form.Label>
                <Form.Control 
                  type="text" 
                  className="auth-input" 
                  placeholder="e.g. Downtown, Phase 1" 
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  required 
                />
              </Form.Group>
            </div>
          )}

          <Button type="submit" className="btn-primary-custom w-100 py-2 mb-3" disabled={loading}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </Button>

          <div className="text-center mt-3">
            <span className="text-muted">Already have an account? </span>
            <Link to="/login" className="text-primary-custom text-decoration-none fw-medium">Login</Link>
          </div>
        </Form>
      </div>
    </div>
  );
};

export default Register;
