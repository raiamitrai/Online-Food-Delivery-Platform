import React, { useState, useEffect, useRef } from 'react';
import { Container, Row, Col, Card, Badge, Button, Tabs, Tab, Spinner, Form, Alert, ToastContainer, Toast } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { MapPin, CheckCircle, Navigation, Package, Truck, Info } from 'lucide-react';
import api from '../api';

const AgentDashboard = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const agentId = id;
  
  const [agent, setAgent] = useState(null);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [orderHistory, setOrderHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('current');
  const [updating, setUpdating] = useState(false);
  const [toasts, setToasts] = useState([]);
  const previousOrderIdRef = useRef(null);

  useEffect(() => {
    fetchDashboardData(true);
    const interval = setInterval(() => {
      fetchDashboardData(false, true); // silent polling
    }, 10000); // Poll every 10 seconds

    return () => clearInterval(interval);
  }, [agentId]);

  const addToast = (message) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 6000);
  };

  const fetchDashboardData = async (showSpinner = false, isPolling = false) => {
    if (showSpinner) setLoading(true);
    try {
      // 1. Fetch Agent Profile
      const agentRes = await api.get(`/api/delivery/agents/${agentId}`);
      setAgent(agentRes.data);

      // 2. Fetch Current Order if assigned
      if (agentRes.data.currentOrderId) {
        // If an order was just assigned and it wasn't there before, notify!
        if (isPolling && previousOrderIdRef.current !== agentRes.data.currentOrderId && previousOrderIdRef.current === null) {
          addToast(`New Delivery Assigned: Order #${agentRes.data.currentOrderId}!`);
        }
        previousOrderIdRef.current = agentRes.data.currentOrderId;

        try {
          const orderRes = await api.get(`/api/orders/${agentRes.data.currentOrderId}`);
          setCurrentOrder(orderRes.data);
        } catch (err) {
          console.error('Failed to fetch current order details', err);
          setCurrentOrder(null);
        }
      } else {
        if (isPolling && previousOrderIdRef.current !== null) {
          // Order was delivered or unassigned
          previousOrderIdRef.current = null;
        }
        setCurrentOrder(null);
      }

      // 3. Fetch Order History
      const historyRes = await api.get(`/api/orders/agent/${agentId}`);
      // Sort by newest first and filter only delivered or cancelled if needed, but we'll show all
      const sortedHistory = historyRes.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setOrderHistory(sortedHistory);
      
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      if (error.response?.status === 401) {
          navigate('/login');
      }
    } finally {
      if (showSpinner) setLoading(false);
    }
  };

  const toggleAvailability = async () => {
    setUpdating(true);
    try {
      await api.put(`/api/delivery/agents/${agentId}/availability?isAvailable=${!agent.isAvailable}`);
      fetchDashboardData(false);
    } catch (err) {
      console.error("Failed to update availability", err);
      alert("Could not update availability.");
    } finally {
      setUpdating(false);
    }
  };

  const updateOrderStatus = async (status) => {
    setUpdating(true);
    try {
      await api.put(`/api/orders/${currentOrder.id}/status?status=${status}`);
      
      // If marking as delivered, also notify delivery service to free up the agent
      if (status === 'DELIVERED') {
        await api.put(`/api/delivery/agents/${agentId}/deliver/${currentOrder.id}`);
      }
      
      fetchDashboardData(false);
    } catch (err) {
      console.error("Failed to update order status", err);
      alert("Failed to update order status.");
    } finally {
      setUpdating(false);
    }
  };

  const getStatusBadgeVariant = (status) => {
    switch (status) {
      case 'PLACED': return 'warning';
      case 'CONFIRMED': return 'info';
      case 'PREPARING': return 'primary';
      case 'READY_FOR_PICKUP': return 'success';
      case 'PICKED_UP': return 'info';
      case 'OUT_FOR_DELIVERY': return 'info';
      case 'DELIVERED': return 'success';
      case 'CANCELLED': return 'danger';
      default: return 'secondary';
    }
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <Spinner animation="border" variant="primary" />
      </Container>
    );
  }

  if (!agent) {
    return (
      <Container className="py-5 text-center">
        <h2>Agent Profile Not Found</h2>
        <p className="text-muted">Could not load your delivery profile.</p>
        <Button onClick={() => navigate('/login')}>Return to Login</Button>
      </Container>
    );
  }

  const totalDeliveries = orderHistory.filter(o => o.status === 'DELIVERED').length;
  // Let's assume a flat delivery fee per order of ₹40 for UI purposes
  const totalEarnings = totalDeliveries * 40;

  return (
    <div className="bg-light min-vh-100 position-relative">
      {/* Toast Notification Container */}
      <ToastContainer position="top-center" className="p-3" style={{ zIndex: 9999, position: 'fixed', top: '80px' }}>
        {toasts.map(toast => (
          <Toast key={toast.id} bg="primary" className="text-white shadow-lg border-0 rounded-3">
            <Toast.Body className="fw-bold d-flex align-items-center gap-2 py-3 px-4 fs-6">
              <Package size={24} /> {toast.message}
            </Toast.Body>
          </Toast>
        ))}
      </ToastContainer>

      {/* Premium Welcome Banner */}
      <div className="text-white py-5 mb-4" style={{ 
        background: 'linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)',
        borderRadius: '0 0 40px 40px',
        boxShadow: '0 10px 30px rgba(0,0,0,0.1)'
      }}>
        <Container>
          <Row className="align-items-center">
            <Col md={7}>
              <div className="d-flex align-items-center gap-3 mb-3">
                <div className="bg-white bg-opacity-20 p-3 rounded-circle shadow-sm">
                  <Truck size={40} className="text-white" />
                </div>
                <div>
                  <h6 className="text-uppercase mb-1 opacity-75 fw-bold" style={{ letterSpacing: '2px', fontSize: '0.8rem' }}>Delivery Partner</h6>
                  <h1 className="display-5 fw-bold mb-0">Welcome Back, {agent.name}!</h1>
                </div>
              </div>
              <div className="d-flex flex-wrap gap-4 mt-4">
                <div className="d-flex align-items-center gap-2">
                  <Badge bg="light" text="dark" className="p-2 rounded-circle"><Info size={16} /></Badge>
                  <div>
                    <small className="d-block opacity-75">Agent ID</small>
                    <span className="fw-bold">#AGT-{agent.id}</span>
                  </div>
                </div>
                <div className="d-flex align-items-center gap-2">
                  <Badge bg="light" text="dark" className="p-2 rounded-circle"><MapPin size={16} /></Badge>
                  <div>
                    <small className="d-block opacity-75">Location</small>
                    <span className="fw-bold">{agent.currentLocation || 'City Center'}</span>
                  </div>
                </div>
                {agent.phone && (
                  <div className="d-flex align-items-center gap-2">
                    <Badge bg="light" text="dark" className="p-2 rounded-circle"><Truck size={16} /></Badge>
                    <div>
                      <small className="d-block opacity-75">Contact</small>
                      <span className="fw-bold">{agent.phone}</span>
                    </div>
                  </div>
                )}
              </div>
            </Col>
            <Col md={5} className="mt-4 mt-md-0">
              <Card className="bg-white bg-opacity-10 border-0 shadow-lg" style={{ backdropFilter: 'blur(15px)', borderRadius: '20px' }}>
                <Card.Body className="p-4">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="mb-0 fw-bold">Duty Status</h5>
                    <Badge bg={agent.isAvailable ? "success" : "danger"} pill className="px-3 py-2">
                      {agent.isAvailable ? "ONLINE" : "OFFLINE"}
                    </Badge>
                  </div>
                  <Form.Check 
                    type="switch"
                    id="availability-switch"
                    label={agent.isAvailable ? "Ready to accept orders" : "Unavailable for delivery"}
                    checked={agent.isAvailable}
                    onChange={toggleAvailability}
                    disabled={updating || agent.currentOrderId !== null}
                    className={`fw-medium fs-5 ${agent.isAvailable ? 'text-success' : 'text-white opacity-75'}`}
                  />
                  {agent.currentOrderId && (
                    <Alert variant="warning" className="mt-3 mb-0 py-2 border-0 small fw-bold">
                      Cannot go offline while on delivery!
                    </Alert>
                  )}
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Container>
      </div>

      {/* Main Content */}
      <Container className="py-4" style={{ marginTop: '-30px' }}>
        <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-4 bg-white rounded-top px-3 pt-3 shadow-sm border-bottom-0">
          <Tab eventKey="current" title={<span><Navigation size={18} className="me-1"/> Current Task</span>}>
            {agent.currentOrderId ? (
              currentOrder ? (
                <Card className="shadow-sm border-0 rounded-4 overflow-hidden mb-4">
                  <div className="bg-primary bg-opacity-10 p-3 border-bottom d-flex justify-content-between align-items-center">
                    <h5 className="fw-bold mb-0 text-primary">Active Delivery - Order #{currentOrder.id}</h5>
                    <Badge bg={getStatusBadgeVariant(currentOrder.status)} className="px-3 py-2 rounded-pill">
                      {currentOrder.status.replace(/_/g, ' ')}
                    </Badge>
                  </div>
                  <Card.Body className="p-4">
                    <Row className="g-4">
                      <Col md={6}>
                        <div className="d-flex flex-column h-100">
                          <h6 className="fw-bold text-muted mb-3 text-uppercase" style={{ fontSize: '0.85rem', letterSpacing: '1px' }}>Delivery Details</h6>
                          
                          <div className="mb-4 position-relative">
                            <div className="d-flex gap-3 align-items-start mb-4">
                              <div className="bg-light rounded-circle p-2 text-danger">
                                <Package size={20} />
                              </div>
                              <div>
                                <p className="mb-1 fw-bold">Pickup from Restaurant</p>
                                <p className="text-muted small mb-0">Restaurant ID: {currentOrder.restaurantId}</p>
                              </div>
                            </div>
                            
                            <div className="position-absolute border-start border-2 ms-4" style={{ top: '35px', bottom: '35px', left: '1px', borderColor: '#dee2e6' }}></div>

                            <div className="d-flex gap-3 align-items-start">
                              <div className="bg-light rounded-circle p-2 text-success">
                                <MapPin size={20} />
                              </div>
                              <div>
                                <p className="mb-1 fw-bold">Deliver to Customer</p>
                                <p className="text-muted small mb-0">Customer ID: {currentOrder.customerId}</p>
                              </div>
                            </div>
                          </div>

                          <div className="bg-light p-3 rounded-3 mt-auto">
                            <h6 className="fw-bold mb-2">Order Items:</h6>
                            <ul className="mb-0 ps-3 small text-muted">
                              {currentOrder.items?.map(item => (
                                <li key={item.id}>{item.quantity}x {item.menuItemName}</li>
                              ))}
                            </ul>
                            <div className="mt-2 pt-2 border-top fw-bold text-dark d-flex justify-content-between">
                              <span>Collect Cash:</span>
                              <span>₹{currentOrder.totalAmount.toFixed(2)}</span>
                            </div>
                          </div>
                        </div>
                      </Col>
                      <Col md={6} className="d-flex flex-column justify-content-center border-start-md">
                        <div className="text-center p-3">
                          <h6 className="fw-bold text-muted mb-4">Actions</h6>
                          
                          <div className="d-grid gap-3">
                            {['CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP'].includes(currentOrder.status) && (
                              <Button 
                                variant="primary" 
                                size="lg" 
                                className="fw-bold rounded-pill py-3 shadow-sm d-flex justify-content-center align-items-center gap-2"
                                onClick={() => updateOrderStatus('PICKED_UP')}
                                disabled={updating}
                              >
                                <Package size={20} /> Mark as Picked Up
                              </Button>
                            )}
                            
                            {['PICKED_UP', 'OUT_FOR_DELIVERY'].includes(currentOrder.status) && (
                              <Button 
                                variant="success" 
                                size="lg" 
                                className="fw-bold rounded-pill py-3 shadow-sm d-flex justify-content-center align-items-center gap-2"
                                onClick={() => updateOrderStatus('DELIVERED')}
                                disabled={updating}
                              >
                                <CheckCircle size={20} /> Mark as Delivered
                              </Button>
                            )}

                            {currentOrder.status === 'PLACED' && (
                              <Alert variant="info" className="mb-0 d-flex align-items-center gap-2">
                                <Info size={16}/> Waiting for restaurant to confirm.
                              </Alert>
                            )}
                          </div>
                        </div>
                      </Col>
                    </Row>
                  </Card.Body>
                </Card>
              ) : (
                <Card className="text-center py-5 shadow-sm border-0 rounded-4 mb-4">
                  <Spinner animation="border" className="mx-auto mb-3" />
                  <p className="text-muted">Loading assigned order details...</p>
                </Card>
              )
            ) : (
              <Card className="text-center py-5 shadow-sm border-0 rounded-4 mb-4" style={{ backgroundColor: '#f8f9fa' }}>
                <Card.Body className="py-5">
                  <div className="bg-white rounded-circle d-inline-flex p-4 mb-4 shadow-sm">
                    <Truck size={48} className={agent.isAvailable ? "text-success" : "text-muted"} opacity={agent.isAvailable ? 1 : 0.5} />
                  </div>
                  <h3 className="fw-bold text-dark">No Active Deliveries</h3>
                  <p className="text-muted mb-0 mx-auto" style={{ maxWidth: '400px' }}>
                    {agent.isAvailable 
                      ? "You are online and ready to receive orders. Keep this window open, we will notify you when an order arrives."
                      : "You are currently offline. Toggle your status to Online to start receiving delivery assignments."}
                  </p>
                </Card.Body>
              </Card>
            )}
          </Tab>

          <Tab eventKey="history" title={<span><CheckCircle size={18} className="me-1"/> Delivery History</span>}>
            <Row className="mb-4">
              <Col md={6}>
                <Card className="bg-primary text-white border-0 shadow-sm rounded-4 overflow-hidden">
                  <Card.Body className="p-4 position-relative">
                    <h6 className="opacity-75 fw-medium mb-1">Total Deliveries</h6>
                    <h2 className="fw-bold mb-0 display-5">{totalDeliveries}</h2>
                    <Truck size={80} className="position-absolute opacity-25" style={{ right: '-10px', bottom: '-10px' }} />
                  </Card.Body>
                </Card>
              </Col>
              <Col md={6}>
                <Card className="bg-success text-white border-0 shadow-sm rounded-4 overflow-hidden mt-3 mt-md-0">
                  <Card.Body className="p-4 position-relative">
                    <h6 className="opacity-75 fw-medium mb-1">Estimated Earnings</h6>
                    <h2 className="fw-bold mb-0 display-5">₹{totalEarnings}</h2>
                    <div className="position-absolute opacity-25 fw-bold" style={{ right: '10px', bottom: '-10px', fontSize: '80px', lineHeight: 1 }}>₹</div>
                  </Card.Body>
                </Card>
              </Col>
            </Row>

            <Card className="shadow-sm border-0 rounded-4 mb-4">
              <Card.Header className="bg-white border-bottom-0 pt-4 pb-2 px-4">
                <h5 className="fw-bold mb-0">Past Orders</h5>
              </Card.Header>
              <Card.Body className="p-0">
                {orderHistory.filter(o => o.id !== agent.currentOrderId).length === 0 ? (
                  <div className="text-center py-5 text-muted">
                    No past deliveries found.
                  </div>
                ) : (
                  <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                      <thead className="table-light">
                        <tr>
                          <th className="ps-4">Order ID</th>
                          <th>Date</th>
                          <th>Restaurant</th>
                          <th>Amount</th>
                          <th className="pe-4 text-end">Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {orderHistory.filter(o => o.id !== agent.currentOrderId).map(order => (
                          <tr key={order.id}>
                            <td className="ps-4 fw-medium">#{order.id}</td>
                            <td className="text-muted small">{new Date(order.createdAt).toLocaleDateString()}</td>
                            <td>ID: {order.restaurantId}</td>
                            <td className="fw-medium">₹{order.totalAmount.toFixed(2)}</td>
                            <td className="pe-4 text-end">
                              <Badge bg={getStatusBadgeVariant(order.status)}>
                                {order.status.replace(/_/g, ' ')}
                              </Badge>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </Card.Body>
            </Card>
          </Tab>
        </Tabs>
      </Container>
    </div>
  );
};

export default AgentDashboard;
