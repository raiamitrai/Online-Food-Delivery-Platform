import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, ListGroup, Modal } from 'react-bootstrap';
import { Trash2, CreditCard } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState(null); // 'COD' or 'QR'
  const [promoCode, setPromoCode] = useState('');
  const [discount, setDiscount] = useState(0);
  const navigate = useNavigate();
  const customerId = sessionStorage.getItem('customerId');

  useEffect(() => {
    if (!customerId) {
      navigate('/login');
      return;
    }
    fetchCart();
  }, [customerId]);

  const fetchCart = async () => {
    try {
      const res = await api.get(`/api/cart/${customerId}`);
      setCart(res.data);
    } catch (error) {
      console.error("Error fetching cart", error);
      // Fallback dummy data
      setCart({
        id: 1,
        restaurantId: 1,
        totalAmount: 600,
        items: [
            { id: 1, menuItemName: 'Paneer Tikka', quantity: 1, price: 250 },
            { id: 2, menuItemName: 'Chicken Biryani', quantity: 1, price: 350 }
        ]
      });
    }
  };

  const subTotal = cart?.totalAmount || 0;
  const deliveryFee = 40;
  const gst = subTotal * 0.05; // 5% GST
  const finalTotal = subTotal + deliveryFee + gst - discount;

  const handleApplyPromo = () => {
    if (promoCode.toUpperCase() === 'WELCOME50') {
      setDiscount(50);
      alert('Promo code applied! You saved ₹50.');
    } else {
      setDiscount(0);
      alert('Invalid Promo Code');
    }
  };

  const handleCheckout = async () => {
    try {
      // Step 1: Place Order
      const orderRes = await api.post(`/api/orders/${customerId}`, {
          restaurantId: cart.restaurantId,
          totalAmount: finalTotal,
          items: cart.items.map(item => ({
              menuItemId: item.menuItemId || 0, // Fallback for dummy
              menuItemName: item.menuItemName,
              quantity: item.quantity,
              price: item.price
          }))
      });
      
      const orderId = orderRes.data.id;

      // Step 2: Clear Cart
      await api.delete(`/api/cart/${customerId}`);
      
      setShowPaymentModal(false);
      alert('Order Placed Successfully!');
      navigate('/orders'); 

    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || 'Checkout failed!');
    }
  };

  const handleRemoveItem = async (itemId) => {
    try {
      await api.delete(`/api/cart/${customerId}/items/${itemId}`);
      fetchCart();
    } catch (error) {
      console.error("Error removing item", error);
      alert(error.response?.data?.message || 'Failed to remove item');
    }
  };

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <Container className="py-5 text-center">
        <h2 className="fw-bold">Your cart is empty</h2>
        <p className="text-muted mb-4">Looks like you haven't added anything to your cart yet.</p>
        <Button variant="primary" className="btn-primary-custom" onClick={() => navigate('/')}>
          Browse Restaurants
        </Button>
      </Container>
    );
  }

  return (
    <Container className="py-5">
      <h2 className="fw-bold mb-4">Checkout</h2>
      <Row className="g-4">
        <Col md={8}>
          <Card className="card-premium mb-4">
            <Card.Header className="bg-white border-bottom-0 pt-4 pb-0">
              <h5 className="fw-bold mb-0">Cart Items</h5>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                {cart.items.map(item => (
                  <ListGroup.Item key={item.id} className="d-flex justify-content-between align-items-center py-3 border-light">
                    <div>
                      <h6 className="fw-bold mb-1">{item.menuItemName}</h6>
                      <small className="text-muted">Qty: {item.quantity}</small>
                    </div>
                    <div className="d-flex align-items-center gap-3">
                      <span className="fw-bold">₹{item.price * item.quantity}</span>
                      <Button variant="link" className="text-danger p-0" onClick={() => handleRemoveItem(item.id)}>
                        <Trash2 size={18} />
                      </Button>
                    </div>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="card-premium">
            <Card.Body className="p-4">
              <h5 className="fw-bold mb-4">Order Summary</h5>
              <div className="d-flex justify-content-between mb-2">
                <span className="text-muted">Item Total</span>
                <span className="fw-medium">₹{subTotal.toFixed(2)}</span>
              </div>
              <div className="d-flex justify-content-between mb-2">
                <span className="text-muted">Delivery Fee</span>
                <span className="fw-medium">₹{deliveryFee.toFixed(2)}</span>
              </div>
              <div className="d-flex justify-content-between mb-3">
                <span className="text-muted">GST (5%)</span>
                <span className="fw-medium">₹{gst.toFixed(2)}</span>
              </div>
              
              {/* Promo Code Section */}
              <div className="d-flex gap-2 mb-3">
                <input 
                  type="text" 
                  className="form-control form-control-sm text-uppercase" 
                  placeholder="Promo Code (e.g. WELCOME50)"
                  value={promoCode}
                  onChange={(e) => setPromoCode(e.target.value)}
                />
                <Button variant="dark" size="sm" onClick={handleApplyPromo}>Apply</Button>
              </div>
              
              {discount > 0 && (
                <div className="d-flex justify-content-between mb-3 text-success fw-bold">
                  <span>Discount ({promoCode})</span>
                  <span>-₹{discount.toFixed(2)}</span>
                </div>
              )}

              <hr />
              <div className="d-flex justify-content-between mb-4">
                <span className="fw-bold fs-5">To Pay</span>
                <span className="fw-bold fs-5 text-primary-custom">₹{finalTotal.toFixed(2)}</span>
              </div>
              <Button 
                className="btn-primary-custom w-100 py-3 d-flex justify-content-center align-items-center gap-2 text-uppercase fw-bold" 
                onClick={() => setShowPaymentModal(true)}
              >
                <CreditCard size={20} /> Proceed to Pay
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Payment Modal */}
      <Modal show={showPaymentModal} onHide={() => { setShowPaymentModal(false); setPaymentMethod(null); }} centered>
        <Modal.Header closeButton className="border-0 pb-0">
          <Modal.Title className="fw-bold">Payment Options</Modal.Title>
        </Modal.Header>
        <Modal.Body className="px-4 pb-4">
          {!paymentMethod ? (
            <div className="d-flex flex-column gap-3 mt-3">
              <Button 
                variant="outline-dark" 
                className="p-3 text-start fw-bold d-flex justify-content-between align-items-center"
                onClick={() => setPaymentMethod('COD')}
              >
                Cash on Delivery (COD) <span>💵</span>
              </Button>
              <Button 
                variant="outline-primary" 
                className="p-3 text-start fw-bold d-flex justify-content-between align-items-center"
                onClick={() => setPaymentMethod('QR')}
              >
                Pay Now (UPI / QR) <span>📱</span>
              </Button>
            </div>
          ) : paymentMethod === 'COD' ? (
            <div className="text-center mt-3">
              <h5 className="mb-4">Pay ₹{finalTotal.toFixed(2)} in cash to our delivery partner.</h5>
              <Button className="btn-primary-custom w-100 py-3 fw-bold" onClick={handleCheckout}>
                Place Order
              </Button>
            </div>
          ) : (
            <div className="text-center mt-3">
              <div className="bg-light p-3 rounded mb-3 d-inline-block border">
                <img src="/images/qr_code.png" alt="PhonePe QR Code" style={{ width: '250px', height: '250px', objectFit: 'contain' }} />
              </div>
              <h5 className="fw-bold mb-1">Scan & Pay: ₹{finalTotal.toFixed(2)}</h5>
              <p className="text-muted mb-4">Paying to: <strong>AMIT KUMAR RAI</strong> (PhonePe)</p>
              <Button className="btn-primary-custom w-100 py-3 fw-bold" onClick={handleCheckout}>
                I have Paid
              </Button>
            </div>
          )}
        </Modal.Body>
      </Modal>

    </Container>
  );
};

export default Cart;
