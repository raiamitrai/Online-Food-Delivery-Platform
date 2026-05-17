import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Table, Tabs, Tab, Spinner, Modal, Form } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import { Plus, Trash2, Star } from 'lucide-react';
import api from '../api';

const RestaurantDashboard = () => {
  const { id } = useParams();
  const storedRestaurantId = sessionStorage.getItem('restaurantId');
  const restaurantId = id || storedRestaurantId || '1';

  const [orders, setOrders] = useState([]);
  const [menuCategories, setMenuCategories] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('menu'); // Start on menu or orders, but remember it

  // Modals state
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');

  const [showItemModal, setShowItemModal] = useState(false);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [newItem, setNewItem] = useState({ name: '', description: '', price: '', isVegetarian: true });

  const [restaurantInfo, setRestaurantInfo] = useState(null);

  const [showEditItemModal, setShowEditItemModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);

  const [showEditProfileModal, setShowEditProfileModal] = useState(false);
  const [editingProfile, setEditingProfile] = useState({ name: '', cuisine: '', location: '' });

  const [orderFilter, setOrderFilter] = useState('ALL'); // ALL, ACTIVE, PAST

  useEffect(() => {
    fetchDashboardData(true);
  }, [restaurantId]);

  const fetchDashboardData = async (showSpinner = false) => {
    if (showSpinner) setLoading(true);
    try {
      const [ordersRes, menuRes, restRes, reviewsRes] = await Promise.all([
        api.get(`/api/orders/restaurant/${restaurantId}`),
        api.get(`/api/menus/restaurant/${restaurantId}`),
        api.get(`/api/restaurants/${restaurantId}`).catch(() => ({ data: null })),
        api.get(`/api/reviews/restaurant/${restaurantId}`).catch(() => ({ data: [] }))
      ]);
      const sortedOrders = ordersRes.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setOrders(sortedOrders);
      setMenuCategories(menuRes.data);

      const reviewsData = reviewsRes.data || [];
      setReviews(reviewsData);
      console.log("DEBUG: Restaurant ID: " + restaurantId + " | Reviews found in DB: " + reviewsData.length);

      if (restRes.data) setRestaurantInfo(restRes.data);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      if (showSpinner) setLoading(false);
    }
  };

  const getFilteredOrders = () => {
    if (orderFilter === 'ACTIVE') {
      return orders.filter(o => ['PLACED', 'CONFIRMED', 'PREPARING'].includes(o.status));
    }
    if (orderFilter === 'PAST') {
      return orders.filter(o => ['READY_FOR_PICKUP', 'PICKED_UP', 'DELIVERED', 'CANCELLED'].includes(o.status));
    }
    return orders;
  };

  const totalEarnings = orders
    .filter(o => o.status !== 'CANCELLED')
    .reduce((sum, order) => sum + order.totalAmount, 0);

  const totalCompletedOrders = orders.filter(o => o.status !== 'CANCELLED').length;
  const pendingOrders = orders.filter(o => ['PLACED', 'CONFIRMED', 'PREPARING'].includes(o.status)).length;

  const handleUpdateItem = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/api/menus/items/${editingItem.id}`, {
        ...editingItem,
        price: parseFloat(editingItem.price)
      });
      setShowEditItemModal(false);
      fetchDashboardData(false);
    } catch (error) {
      console.error('Error updating item:', error);
      alert('Failed to update item');
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/api/restaurants/${restaurantId}`, editingProfile);
      setShowEditProfileModal(false);
      fetchDashboardData(false);
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile');
    }
  };

  const getImageForRestaurant = (name) => {
    if (!name) return null;
    if (name.includes('Spice')) return '/images/spice_route.png';
    if (name.includes('Burger')) return '/images/burger_joint.png';
    if (name.includes('Sushi')) return '/images/sushi_zen.png';
    return null;
  };

  const getGradientForRestaurant = (name) => {
    if (!name) return 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)';
    const gradients = [
      'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
      'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)',
      'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
      'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
      'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
      'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
    ];
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return gradients[Math.abs(hash) % gradients.length];
  };

  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      if (newStatus === 'READY_FOR_PICKUP') {
        try {
          // 1. Assign delivery agent FIRST
          const assignRes = await api.post(`/api/delivery/assign/${orderId}`);
          const assignedAgentId = assignRes.data.id;

          // 2. Link agent to order in order-service
          await api.put(`/api/orders/${orderId}/assign/${assignedAgentId}`);

          // 3. Update status
          await api.put(`/api/orders/${orderId}/status?status=${newStatus}`);
          alert(`Order is ready! Assigned to Delivery Agent: ${assignRes.data.name}`);
        } catch (assignErr) {
          console.error('Error assigning delivery agent:', assignErr);
          alert('Cannot mark as Ready for Pickup: No Delivery Agents are currently ONLINE. Please wait for an agent to come online.');
          return; // Stop the flow, do not update status!
        }
      } else {
        // Normal status update for others
        await api.put(`/api/orders/${orderId}/status?status=${newStatus}`);
      }

      fetchDashboardData(false);
    } catch (error) {
      console.error('Error updating order status:', error);
      alert('Failed to update status.');
    }
  };

  const toggleItemAvailability = async (itemId, currentStatus) => {
    try {
      await api.put(`/api/menus/items/${itemId}/availability?isAvailable=${!currentStatus}`);
      fetchDashboardData(false);
    } catch (error) {
      console.error('Error toggling availability:', error);
    }
  };

  const handleAddCategory = async (e) => {
    e.preventDefault();
    if (!newCategoryName.trim()) return;
    try {
      await api.post('/api/menus/categories', { name: newCategoryName, restaurantId });
      setShowCategoryModal(false);
      setNewCategoryName('');
      fetchDashboardData(false);
    } catch (error) {
      console.error('Error adding category:', error);
      alert('Failed to add category');
    }
  };

  const handleAddItem = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/menus/items', {
        ...newItem,
        price: parseFloat(newItem.price),
        categoryId: selectedCategoryId
      });
      setShowItemModal(false);
      setNewItem({ name: '', description: '', price: '', isVegetarian: true });
      fetchDashboardData(false);
    } catch (error) {
      console.error('Error adding item:', error);
      alert('Failed to add item');
    }
  };

  const fetchReviews = async () => {
    try {
      console.log("Fetching reviews for Restaurant ID:", restaurantId);
      const response = await api.get(`/api/reviews/restaurant/${restaurantId}`);
      console.log("Reviews fetched:", response.data);
      alert("Dashboard showing data for Restaurant ID: " + restaurantId + "\nReviews found: " + response.data.length);
      setReviews(response.data);
    } catch (err) {
      console.error("Error fetching reviews", err);
    }
  };

  const handleDeleteItem = async (itemId) => {
    if (window.confirm('Are you sure you want to delete this item?')) {
      try {
        await api.delete(`/api/menus/items/${itemId}`);
        fetchDashboardData(false);
      } catch (error) {
        console.error('Error deleting item:', error);
        alert('Failed to delete item');
      }
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    if (window.confirm('Are you sure you want to delete this category? All items inside it will also be deleted.')) {
      try {
        await api.delete(`/api/menus/categories/${categoryId}`);
        fetchDashboardData(false);
      } catch (error) {
        console.error('Error deleting category:', error);
        alert('Failed to delete category');
      }
    }
  };

  const getStatusBadgeVariant = (status) => {
    switch (status) {
      case 'PLACED': return 'warning';
      case 'CONFIRMED': return 'info';
      case 'PREPARING': return 'primary';
      case 'READY_FOR_PICKUP': return 'success';
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

  return (
    <Container className="py-5">
      {restaurantInfo ? (
        <div className="mb-4 rounded-4 overflow-hidden shadow-sm position-relative" style={{ height: '220px' }}>
          {getImageForRestaurant(restaurantInfo.name) ? (
            <div
              style={{
                position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                backgroundImage: `url(${getImageForRestaurant(restaurantInfo.name)})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                filter: 'brightness(0.6)'
              }}
            />
          ) : (
            <div
              style={{
                position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                background: getGradientForRestaurant(restaurantInfo.name)
              }}
            />
          )}

          <div className="position-absolute bottom-0 start-0 p-4 w-100 text-white d-flex justify-content-between align-items-end" style={{ background: 'linear-gradient(transparent, rgba(0,0,0,0.85))' }}>
            <div>
              <h1 className="fw-bold display-5 mb-1" style={{ textShadow: '1px 2px 4px rgba(0,0,0,0.5)', letterSpacing: '-0.5px' }}>
                {restaurantInfo.name}
              </h1>
              <p className="fs-6 text-light opacity-75 mb-0 d-flex align-items-center gap-2" style={{ textShadow: '1px 1px 2px rgba(0,0,0,0.5)' }}>
                {restaurantInfo.cuisine || 'Multi-Cuisine'} • {restaurantInfo.location}
                <Badge bg="success" className="ms-2">{restaurantInfo.status === 'APPROVED' ? 'OPEN' : restaurantInfo.status}</Badge>
              </p>
            </div>
            <Button variant="light" size="sm" className="fw-bold px-3 py-2 text-dark shadow-sm" onClick={() => {
              setEditingProfile({ name: restaurantInfo.name, cuisine: restaurantInfo.cuisine, location: restaurantInfo.location });
              setShowEditProfileModal(true);
            }}>
              Edit Profile
            </Button>
          </div>
        </div>
      ) : (
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h2 className="fw-bold mb-0">Vendor Dashboard <Badge bg="light" text="dark" className="fs-6 border">ID: {restaurantId}</Badge></h2>
        </div>
      )}

      <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-4">
        {/* ANALYTICS TAB */}
        <Tab eventKey="analytics" title="Analytics">
          <Row className="mt-4">
            <Col md={4}>
              <Card className="text-center shadow-sm border-0 py-4 mb-3" style={{ background: 'linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%)' }}>
                <h5 className="text-dark opacity-75 fw-bold">Total Earnings</h5>
                <h2 className="text-dark fw-bold mb-0">₹{totalEarnings.toFixed(2)}</h2>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="text-center shadow-sm border-0 py-4 mb-3" style={{ background: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)' }}>
                <h5 className="text-dark opacity-75 fw-bold">Total Orders</h5>
                <h2 className="text-dark fw-bold mb-0">{totalCompletedOrders}</h2>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="text-center shadow-sm border-0 py-4 mb-3" style={{ background: 'linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%)' }}>
                <h5 className="text-dark opacity-75 fw-bold">Pending Orders</h5>
                <h2 className="text-dark fw-bold mb-0">{pendingOrders}</h2>
              </Card>
            </Col>
          </Row>
        </Tab>

        {/* ORDERS TAB */}
        <Tab eventKey="orders" title="Incoming Orders">
          <div className="d-flex gap-2 mt-3 mb-4">
            <Button variant={orderFilter === 'ALL' ? 'primary' : 'outline-secondary'} size="sm" onClick={() => setOrderFilter('ALL')}>All Orders</Button>
            <Button variant={orderFilter === 'ACTIVE' ? 'primary' : 'outline-secondary'} size="sm" onClick={() => setOrderFilter('ACTIVE')}>Active</Button>
            <Button variant={orderFilter === 'PAST' ? 'primary' : 'outline-secondary'} size="sm" onClick={() => setOrderFilter('PAST')}>Past Orders</Button>
          </div>
          {getFilteredOrders().length === 0 ? (
            <p className="text-muted mt-3">No orders found.</p>
          ) : (
            <Row className="mt-3">
              {getFilteredOrders().map(order => (
                <Col md={12} key={order.id}>
                  <Card className="mb-3 shadow-sm border-0">
                    <Card.Header className="bg-white border-bottom-0 pt-3 pb-0 d-flex justify-content-between align-items-center">
                      <h5 className="mb-0 fw-bold">Order #{order.id}</h5>
                      <Badge bg={getStatusBadgeVariant(order.status)} className="px-3 py-2 rounded-pill">
                        {order.status.replace(/_/g, ' ')}
                      </Badge>
                    </Card.Header>
                    <Card.Body>
                      <div className="d-flex justify-content-between mb-3 text-muted small">
                        <span>Placed at: {new Date(order.createdAt).toLocaleString()}</span>
                        <span className="fw-bold text-dark fs-5">₹{order.totalAmount.toFixed(2)}</span>
                      </div>

                      <div className="mb-4">
                        <h6 className="fw-medium">Items:</h6>
                        <ul className="list-unstyled mb-0">
                          {order.items.map(item => (
                            <li key={item.id} className="d-flex justify-content-between border-bottom py-2">
                              <span>{item.quantity}x {item.menuItemName}</span>
                              <span>₹{(item.price * item.quantity).toFixed(2)}</span>
                            </li>
                          ))}
                        </ul>
                      </div>

                      <div className="d-flex gap-2 flex-wrap">
                        {order.status === 'PLACED' && (
                          <Button variant="info" size="sm" onClick={() => updateOrderStatus(order.id, 'CONFIRMED')}>
                            Confirm Order
                          </Button>
                        )}
                        {order.status === 'CONFIRMED' && (
                          <Button variant="primary" size="sm" onClick={() => updateOrderStatus(order.id, 'PREPARING')}>
                            Start Preparing
                          </Button>
                        )}
                        {order.status === 'PREPARING' && (
                          <Button variant="success" size="sm" onClick={() => updateOrderStatus(order.id, 'READY_FOR_PICKUP')}>
                            Ready for Pickup
                          </Button>
                        )}
                        {(order.status === 'PLACED' || order.status === 'CONFIRMED') && (
                          <Button variant="outline-danger" size="sm" onClick={() => updateOrderStatus(order.id, 'CANCELLED')}>
                            Cancel Order
                          </Button>
                        )}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}
        </Tab>

        {/* MENU MANAGEMENT TAB */}
        <Tab eventKey="menu" title="Menu Management">
          <div className="d-flex justify-content-between align-items-center mt-3 mb-4">
            <h4 className="fw-bold mb-0">Your Menu</h4>
            <Button variant="primary" className="d-flex align-items-center gap-1" onClick={() => setShowCategoryModal(true)}>
              <Plus size={18} /> Add Category
            </Button>
          </div>

          {menuCategories.length === 0 ? (
            <p className="text-muted">No menu categories found. Add one to get started.</p>
          ) : (
            menuCategories.map(category => (
              <Card key={category.id} className="mb-4 shadow-sm border-0">
                <Card.Header className="bg-light fw-bold py-3 d-flex justify-content-between align-items-center">
                  <span className="fs-5">{category.name}</span>
                  <div className="d-flex gap-2">
                    <Button
                      variant="outline-danger"
                      size="sm"
                      title="Delete Category"
                      onClick={() => handleDeleteCategory(category.id)}
                    >
                      <Trash2 size={16} />
                    </Button>
                    <Button
                      variant="outline-primary"
                      size="sm"
                      className="d-flex align-items-center gap-1"
                      onClick={() => { setSelectedCategoryId(category.id); setShowItemModal(true); }}
                    >
                      <Plus size={16} /> Add Item
                    </Button>
                  </div>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table hover responsive className="mb-0">
                    <thead className="bg-white">
                      <tr>
                        <th className="ps-4">Item Name</th>
                        <th>Price</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th className="text-end pe-4">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(!category.items || category.items.length === 0) ? (
                        <tr>
                          <td colSpan="5" className="text-center py-4 text-muted">No items in this category.</td>
                        </tr>
                      ) : (
                        category.items.map(item => {
                          const isVeg = item.isVegetarian !== undefined ? item.isVegetarian : item.vegetarian;
                          const isAvail = item.isAvailable !== undefined ? item.isAvailable : item.available;

                          return (
                            <tr key={item.id} className="align-middle">
                              <td className="ps-4 fw-medium">{item.name}</td>
                              <td>₹{item.price.toFixed(2)}</td>
                              <td>
                                <Badge bg={isVeg ? 'success' : 'danger'} className="opacity-75">
                                  {isVeg ? 'Veg' : 'Non-Veg'}
                                </Badge>
                              </td>
                              <td>
                                <Badge bg={isAvail ? 'success' : 'secondary'}>
                                  {isAvail ? 'Available' : 'Out of Stock'}
                                </Badge>
                              </td>
                              <td className="text-end pe-4">
                                <div className="d-flex justify-content-end gap-2">
                                  <Button
                                    variant={isAvail ? "outline-secondary" : "outline-success"}
                                    size="sm"
                                    onClick={() => toggleItemAvailability(item.id, isAvail)}
                                  >
                                    {isAvail ? 'Mark Out of Stock' : 'Mark Available'}
                                  </Button>
                                  <Button
                                    variant="outline-primary"
                                    size="sm"
                                    onClick={() => {
                                      setEditingItem({
                                        id: item.id,
                                        name: item.name,
                                        description: item.description || '',
                                        price: item.price,
                                        isVegetarian: isVeg,
                                        categoryId: category.id
                                      });
                                      setShowEditItemModal(true);
                                    }}
                                  >
                                    Edit
                                  </Button>
                                  <Button
                                    variant="outline-danger"
                                    size="sm"
                                    onClick={() => handleDeleteItem(item.id)}
                                  >
                                    <Trash2 size={16} />
                                  </Button>
                                </div>
                              </td>
                            </tr>
                          );
                        })
                      )}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            ))
          )}
        </Tab>

        {/* REVIEWS TAB */}
        <Tab eventKey="reviews" title={<span><Star size={18} className="me-1" /> Reviews</span>}>
          <div className="mt-4">
            <h4 className="fw-bold mb-4">Customer Reviews</h4>
            {reviews.length === 0 ? (
              <Card className="text-center py-5 border-0 shadow-sm rounded-4">
                <p className="text-muted mb-0">No reviews yet. Ratings will appear here once customers share their experience.</p>
              </Card>
            ) : (
              <Row>
                {Array.isArray(reviews) && reviews.slice().reverse().map(review => (
                  <Col md={6} key={review.id} className="mb-3">
                    <Card className="border-0 shadow-sm rounded-4 h-100">
                      <Card.Body className="p-4">
                        <div className="d-flex justify-content-between align-items-start mb-2">
                          <div className="text-warning fs-5">
                            {'★'.repeat(Math.max(0, Math.min(5, review.rating || 0)))}
                            {'☆'.repeat(Math.max(0, Math.min(5, 5 - (review.rating || 0))))}
                          </div>
                          <small className="text-muted">
                            {review.createdAt ? new Date(review.createdAt).toLocaleDateString() : 'Recent'}
                          </small>
                        </div>
                        <p className="mb-0 text-dark">"{review.comment}"</p>
                        <div className="mt-3 pt-3 border-top small text-muted">
                          Customer ID: {review.customerId}
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
                ))}
              </Row>
            )}
          </div>
        </Tab>
      </Tabs>

      {/* Category Modal */}
      <Modal show={showCategoryModal} onHide={() => setShowCategoryModal(false)} centered>
        <Modal.Header closeButton className="border-0">
          <Modal.Title className="fw-bold">Add New Category</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleAddCategory}>
            <Form.Group className="mb-3">
              <Form.Label>Category Name</Form.Label>
              <Form.Control
                type="text"
                placeholder="e.g. Starters, Main Course"
                value={newCategoryName}
                onChange={(e) => setNewCategoryName(e.target.value)}
                required
                autoFocus
              />
            </Form.Group>
            <Button variant="primary" type="submit" className="w-100 fw-bold py-2">
              Add Category
            </Button>
          </Form>
        </Modal.Body>
      </Modal>

      {/* Item Modal */}
      <Modal show={showItemModal} onHide={() => setShowItemModal(false)} centered>
        <Modal.Header closeButton className="border-0">
          <Modal.Title className="fw-bold">Add Menu Item</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleAddItem}>
            <Form.Group className="mb-3">
              <Form.Label>Item Name</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter item name"
                value={newItem.name}
                onChange={(e) => setNewItem({ ...newItem, name: e.target.value })}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Description (Optional)</Form.Label>
              <Form.Control
                as="textarea"
                rows={2}
                placeholder="Item description"
                value={newItem.description}
                onChange={(e) => setNewItem({ ...newItem, description: e.target.value })}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Price (₹)</Form.Label>
              <Form.Control
                type="number"
                min="0"
                step="0.01"
                placeholder="Enter price"
                value={newItem.price}
                onChange={(e) => setNewItem({ ...newItem, price: e.target.value })}
                required
              />
            </Form.Group>

            <Form.Group className="mb-4">
              <Form.Check
                type="switch"
                id="veg-switch"
                label={newItem.isVegetarian ? "Vegetarian (Veg)" : "Non-Vegetarian"}
                checked={newItem.isVegetarian}
                onChange={(e) => setNewItem({ ...newItem, isVegetarian: e.target.checked })}
              />
            </Form.Group>

            <Button variant="primary" type="submit" className="w-100 fw-bold py-2">
              Add Item
            </Button>
          </Form>
        </Modal.Body>
      </Modal>

      {/* Edit Item Modal */}
      <Modal show={showEditItemModal} onHide={() => setShowEditItemModal(false)} centered>
        <Modal.Header closeButton className="border-0">
          <Modal.Title className="fw-bold">Edit Menu Item</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {editingItem && (
            <Form onSubmit={handleUpdateItem}>
              <Form.Group className="mb-3">
                <Form.Label>Item Name</Form.Label>
                <Form.Control
                  type="text"
                  value={editingItem.name}
                  onChange={(e) => setEditingItem({ ...editingItem, name: e.target.value })}
                  required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Description</Form.Label>
                <Form.Control
                  as="textarea" rows={2}
                  value={editingItem.description}
                  onChange={(e) => setEditingItem({ ...editingItem, description: e.target.value })}
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Price (₹)</Form.Label>
                <Form.Control
                  type="number" min="0" step="0.01"
                  value={editingItem.price}
                  onChange={(e) => setEditingItem({ ...editingItem, price: e.target.value })}
                  required
                />
              </Form.Group>
              <Form.Group className="mb-4">
                <Form.Check
                  type="switch" id="edit-veg-switch"
                  label={editingItem.isVegetarian ? "Vegetarian (Veg)" : "Non-Vegetarian"}
                  checked={editingItem.isVegetarian}
                  onChange={(e) => setEditingItem({ ...editingItem, isVegetarian: e.target.checked })}
                />
              </Form.Group>
              <Button variant="primary" type="submit" className="w-100 fw-bold py-2">Save Changes</Button>
            </Form>
          )}
        </Modal.Body>
      </Modal>

      {/* Edit Profile Modal */}
      <Modal show={showEditProfileModal} onHide={() => setShowEditProfileModal(false)} centered>
        <Modal.Header closeButton className="border-0">
          <Modal.Title className="fw-bold">Edit Restaurant Profile</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleUpdateProfile}>
            <Form.Group className="mb-3">
              <Form.Label>Restaurant Name</Form.Label>
              <Form.Control
                type="text"
                value={editingProfile.name}
                onChange={(e) => setEditingProfile({ ...editingProfile, name: e.target.value })}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Cuisine</Form.Label>
              <Form.Control
                type="text"
                value={editingProfile.cuisine}
                onChange={(e) => setEditingProfile({ ...editingProfile, cuisine: e.target.value })}
                required
              />
            </Form.Group>
            <Form.Group className="mb-4">
              <Form.Label>Location</Form.Label>
              <Form.Control
                type="text"
                value={editingProfile.location}
                onChange={(e) => setEditingProfile({ ...editingProfile, location: e.target.value })}
                required
              />
            </Form.Group>
            <Button variant="primary" type="submit" className="w-100 fw-bold py-2">Save Profile</Button>
          </Form>
        </Modal.Body>
      </Modal>

    </Container>
  );
};

export default RestaurantDashboard;
