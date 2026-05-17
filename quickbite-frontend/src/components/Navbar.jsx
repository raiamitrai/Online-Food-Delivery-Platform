import React, { useState, useEffect } from 'react';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingBag, LogOut } from 'lucide-react';
import api from '../api';

const CustomNavbar = () => {
  const navigate = useNavigate();
  const token = sessionStorage.getItem('token');
  const role = sessionStorage.getItem('role');
  const customerId = sessionStorage.getItem('customerId');
  const handleLogout = () => {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('role');
    sessionStorage.removeItem('customerId');
    sessionStorage.removeItem('restaurantId');
    sessionStorage.removeItem('agentId');
    navigate('/login');
  };

  return (
    <Navbar className="navbar-premium" sticky="top" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/" className="text-primary-custom d-flex align-items-center gap-2">
          <ShoppingBag size={28} />
          QuickBite
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto align-items-center gap-3">
            {token ? (
              <>

                {role === 'OWNER' ? (
                  <Nav.Link as={Link} to={`/restaurant-dashboard/${sessionStorage.getItem('restaurantId') || customerId}`} className="fw-medium text-dark">Vendor Dashboard</Nav.Link>
                ) : role === 'ADMIN' ? (
                  <Nav.Link as={Link} to="/admin-dashboard" className="fw-medium text-dark">Platform Dashboard</Nav.Link>
                ) : role === 'AGENT' ? (
                  <Nav.Link as={Link} to={`/agent-dashboard/${sessionStorage.getItem('agentId') || customerId}`} className="fw-medium text-dark">Delivery Dashboard</Nav.Link>
                ) : (
                  <>
                    <Nav.Link as={Link} to="/orders" className="fw-medium text-dark">Orders</Nav.Link>
                    <div className="position-relative">
                      <Nav.Link as={Link} to="/cart" className="fw-medium text-dark d-flex align-items-center gap-1">
                        <ShoppingBag size={20} />
                        Cart
                      </Nav.Link>
                    </div>
                  </>
                )}
                <Button variant="outline-danger" className="d-flex align-items-center gap-2 rounded-pill px-3 py-2 border-0 bg-light" onClick={handleLogout}>
                  <LogOut size={18} />
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/login" className="fw-medium text-dark">Login</Nav.Link>
                <Button as={Link} to="/register" className="btn-primary-custom rounded-pill">Sign Up</Button>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default CustomNavbar;
