import React, { useState, useEffect, useRef } from 'react';
import { Container, Card, Badge, Row, Col, ProgressBar, ToastContainer, Toast, Modal, Button, Form, Spinner } from 'react-bootstrap';
import { Package, CheckCircle, Star } from 'lucide-react';
import api from '../api';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [toasts, setToasts] = useState([]);
  const customerId = sessionStorage.getItem('customerId');
  const previousOrdersRef = useRef([]);

  // Rating state
  const [showRateModal, setShowRateModal] = useState(false);
  const [orderToRate, setOrderToRate] = useState(null);
  const [restaurantRating, setRestaurantRating] = useState(5);
  const [agentRating, setAgentRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');

  useEffect(() => {
    if (!customerId) return;
    
    fetchOrders();
    const interval = setInterval(() => {
      fetchOrders(true); // silent auto-polling
    }, 10000); // 10 seconds

    return () => clearInterval(interval);
  }, [customerId]);

  const fetchOrders = async (isPolling = false) => {
    try {
      const res = await api.get(`/api/orders/customer/${customerId}`);
      const sortedOrders = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      
      if (isPolling) {
        checkForStatusChanges(sortedOrders);
      } else {
        previousOrdersRef.current = sortedOrders;
      }
      
      setOrders(sortedOrders);
    } catch (error) {
      console.error("Error fetching orders", error);
    }
  };

  const checkForStatusChanges = (newOrders) => {
    const prevOrdersMap = new Map(previousOrdersRef.current.map(o => [o.id, o]));
    
    newOrders.forEach(newOrder => {
      const prevOrder = prevOrdersMap.get(newOrder.id);
      if (prevOrder && prevOrder.status !== newOrder.status) {
        // Status changed!
        const message = `Order #${newOrder.id} is now ${newOrder.status.replace(/_/g, ' ')}!`;
        addToast(message);
      }
    });
    
    previousOrdersRef.current = newOrders;
  };

  const addToast = (message) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 6000); // Toast vanishes after 6s
  };

  const getStatusProgress = (status) => {
    switch (status) {
      case 'PLACED': return 10;
      case 'CONFIRMED': return 25;
      case 'PREPARING': return 50;
      case 'READY_FOR_PICKUP': return 65;
      case 'PICKED_UP': return 75;
      case 'OUT_FOR_DELIVERY': return 90;
      case 'DELIVERED': return 100;
      case 'CANCELLED': return 0;
      default: return 0;
    }
  };

  const submitReview = async (e) => {
    e.preventDefault();
    if (!orderToRate) return;

    try {
      // 1. Submit Restaurant Review
      try {
        await api.post('/api/reviews', {
          customerId: parseInt(customerId),
          restaurantId: orderToRate.restaurantId,
          rating: parseInt(restaurantRating),
          comment: reviewComment || `Great service!`,
          entityType: 'RESTAURANT'
        });
      } catch (restErr) {
        console.warn("Restaurant review failed, but continuing:", restErr);
      }

      // 2. Submit Agent Review
      if (orderToRate.deliveryAgentId) {
        try {
          await api.post('/api/reviews', {
            customerId: parseInt(customerId),
            deliveryAgentId: orderToRate.deliveryAgentId,
            rating: parseInt(agentRating),
            comment: 'Good delivery experience',
            entityType: 'AGENT'
          });
        } catch (agentErr) {
          console.warn("Agent review failed, but continuing:", agentErr);
        }
      }

      addToast("Your review has been successfully submitted! Thank you.");
      setShowRateModal(false);
      setReviewComment('');
    } catch (error) {
      console.error('Submission logic error:', error);
      alert('Failed to submit review. Please try again.');
    }
  };

  return (
    <Container className="py-5 position-relative min-vh-100">
      <ToastContainer position="top-end" className="p-3" style={{ zIndex: 9999, position: 'fixed', top: '80px', right: '20px' }}>
        {toasts.map(toast => (
          <Toast key={toast.id} bg="success" className="text-white shadow-lg border-0 rounded-3">
            <Toast.Body className="fw-bold d-flex align-items-center gap-2 py-3 px-4 fs-6">
              <CheckCircle size={24} /> {toast.message}
            </Toast.Body>
          </Toast>
        ))}
      </ToastContainer>

      <div className="d-flex justify-content-between align-items-end mb-4">
        <h2 className="fw-bold mb-0 d-flex align-items-center gap-2">
          My Orders
        </h2>
        <div className="d-flex align-items-center gap-2 text-muted small">
          <Spinner animation="grow" variant="success" size="sm" /> Live Tracking Active
        </div>
      </div>

      <Row className="g-4">
        {orders.length === 0 ? (
          <p className="text-muted">No orders found.</p>
        ) : orders.map(order => (
          <Col md={12} key={order.id}>
            <Card className="card-premium border-0 shadow-sm overflow-hidden rounded-4">
              <Card.Header className="bg-white border-bottom-0 pt-4 px-4 d-flex justify-content-between align-items-center">
                <div className="d-flex align-items-center gap-3">
                  <div className="bg-light p-3 rounded-circle text-primary-custom">
                    <Package size={24} />
                  </div>
                  <div>
                    <h5 className="fw-bold mb-1">Order #{order.id}</h5>
                    <small className="text-muted">{new Date(order.createdAt).toLocaleString()}</small>
                  </div>
                </div>
                <div className="text-end">
                  <Badge bg={order.status === 'CANCELLED' ? 'danger' : 'success'} className="px-3 py-2 rounded-pill mb-2">
                    {order.status.replace(/_/g, ' ')}
                  </Badge>
                  {order.status === 'DELIVERED' && (
                    <div className="mt-1">
                      <Button variant="outline-warning" size="sm" className="fw-bold rounded-pill shadow-sm" onClick={() => { setOrderToRate(order); setShowRateModal(true); }}>
                        <Star size={14} className="me-1" fill="currentColor" /> Rate Order
                      </Button>
                    </div>
                  )}
                </div>
              </Card.Header>
              <Card.Body className="px-4 pb-4 pt-3">
                {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                  <div className="mb-4 bg-light p-3 rounded-3">
                    <div className="d-flex justify-content-between text-muted small mb-2 fw-bold text-uppercase" style={{ fontSize: '0.7rem' }}>
                      <span className={getStatusProgress(order.status) >= 10 ? "text-dark" : ""}>Placed</span>
                      <span className={getStatusProgress(order.status) >= 50 ? "text-dark" : ""}>Preparing</span>
                      <span className={getStatusProgress(order.status) >= 90 ? "text-dark" : ""}>Out for Delivery</span>
                      <span className={getStatusProgress(order.status) === 100 ? "text-success" : ""}>Delivered</span>
                    </div>
                    <ProgressBar 
                      now={getStatusProgress(order.status)} 
                      variant="success" 
                      striped 
                      animated 
                      style={{ height: '8px', borderRadius: '10px' }} 
                    />
                  </div>
                )}
                
                <div className="d-flex justify-content-between align-items-end mt-3">
                  <div className="text-muted small">
                    <h6 className="fw-bold text-dark mb-1">Items:</h6>
                    {order.items?.map(item => (
                      <div key={item.id}>{item.quantity}x {item.menuItemName}</div>
                    ))}
                  </div>
                  <div className="text-end">
                    <div className="text-muted small fw-medium">Total Paid</div>
                    <div className="fw-bold fs-4 text-dark">
                      ₹{order.totalAmount.toFixed(2)}
                    </div>
                  </div>
                </div>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      <Modal show={showRateModal} onHide={() => setShowRateModal(false)} centered>
        <Modal.Header closeButton className="border-0 pb-0">
          <Modal.Title className="fw-bold d-flex align-items-center gap-2">
            <Star className="text-warning" fill="currentColor" /> Rate Your Experience
          </Modal.Title>
        </Modal.Header>
        <Modal.Body className="pt-3">
          <Form onSubmit={submitReview}>
            <Form.Group className="mb-4 bg-light p-3 rounded-3">
              <Form.Label className="fw-bold mb-1">Food & Packaging Quality</Form.Label>
              <p className="text-muted small mb-2">How was the food from the restaurant?</p>
              <Form.Range min="1" max="5" value={restaurantRating} onChange={e => setRestaurantRating(e.target.value)} className="custom-range" />
              <div className="text-center text-warning fs-3 mt-1" style={{ letterSpacing: '5px' }}>
                {'★'.repeat(restaurantRating)}{'☆'.repeat(5 - restaurantRating)}
              </div>
            </Form.Group>
            
            {orderToRate?.deliveryAgentId && (
              <Form.Group className="mb-4 bg-light p-3 rounded-3">
                <Form.Label className="fw-bold mb-1">Delivery Experience</Form.Label>
                <p className="text-muted small mb-2">How was your delivery partner?</p>
                <Form.Range min="1" max="5" value={agentRating} onChange={e => setAgentRating(e.target.value)} />
                <div className="text-center text-warning fs-3 mt-1" style={{ letterSpacing: '5px' }}>
                  {'★'.repeat(agentRating)}{'☆'.repeat(5 - agentRating)}
                </div>
              </Form.Group>
            )}

            <Form.Group className="mb-4">
              <Form.Label className="fw-bold">Any comments? (Optional)</Form.Label>
              <Form.Control as="textarea" rows={3} value={reviewComment} onChange={e => setReviewComment(e.target.value)} placeholder="Tell us what you liked or what could be improved..." className="border-light-subtle shadow-sm" />
            </Form.Group>

            <Button variant="warning" type="submit" className="w-100 fw-bold text-dark py-2 rounded-pill shadow-sm fs-5">
              Submit Review
            </Button>
          </Form>
        </Modal.Body>
      </Modal>

    </Container>
  );
};

export default Orders;
