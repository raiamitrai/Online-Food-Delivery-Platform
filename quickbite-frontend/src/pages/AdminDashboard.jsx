import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Badge, Button, Tabs, Tab, Spinner, Alert } from 'react-bootstrap';
import { LayoutDashboard, Store, Users, DollarSign, CheckCircle, XCircle, Trash2, UserPlus, TrendingUp, ClipboardList, Settings } from 'lucide-react';
import api from '../api';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [data, setData] = useState({
    orders: [],
    restaurants: [],
    users: [],
    agents: []
  });
  const [commissionRate, setCommissionRate] = useState(10);
  const [newCommissionRate, setNewCommissionRate] = useState(10);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [ordersRes, restRes, usersRes, agentsRes, commRes] = await Promise.all([
        api.get('/api/orders'),
        api.get('/api/restaurants'),
        api.get('/auth/users'),
        api.get('/api/delivery/agents'),
        api.get('/api/orders/settings/commission_rate')
      ]);

      setData({
        orders: ordersRes.data,
        restaurants: restRes.data,
        users: usersRes.data,
        agents: agentsRes.data
      });
      setCommissionRate(parseFloat(commRes.data) || 10);
      setNewCommissionRate(parseFloat(commRes.data) || 10);
      setError(null);
    } catch (err) {
      console.error("Error fetching admin data:", err);
      const errMsg = err.response?.data?.message || err.message;
      setError(`Dashboard data load failed: ${errMsg}. Please ensure all backend services (Order, Delivery, Auth, Restaurant) are running.`);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id, isApproved) => {
    try {
      await api.put(`/api/restaurants/${id}/approve?isApproved=${isApproved}`);
      fetchData(); // Refresh
    } catch (err) {
      alert("Error updating restaurant status");
    }
  };

  const handleDeleteRestaurant = async (id) => {
    if (!window.confirm("Are you sure you want to permanently remove this restaurant?")) return;
    try {
      await api.delete(`/api/restaurants/${id}`);
      fetchData();
    } catch (err) {
      alert("Error deleting restaurant");
    }
  };

  const handleUpdateCommission = async () => {
    setUpdating(true);
    try {
      await api.post(`/api/orders/settings/commission_rate?value=${newCommissionRate}`);
      setCommissionRate(newCommissionRate);
      alert("Platform commission updated successfully!");
    } catch (err) {
      alert("Failed to update commission");
    } finally {
      setUpdating(false);
    }
  };

  // Calculations
  const deliveredOrders = data.orders.filter(o => o.status === 'DELIVERED');
  const totalRevenue = deliveredOrders.reduce((sum, o) => sum + o.totalAmount, 0);
  const platformEarnings = totalRevenue * (commissionRate / 100);

  if (loading) return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <Spinner animation="border" variant="primary" />
    </div>
  );

  return (
    <Container className="py-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1">Super Admin Dashboard</h2>
          <p className="text-muted">Manage platform operations and view analytics</p>
        </div>
        <Button variant="outline-primary" className="rounded-pill" onClick={fetchData}>Refresh Data</Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-4 custom-tabs">
        <Tab eventKey="overview" title={<><LayoutDashboard size={18} className="me-2"/>Overview</>}>
          <Row className="g-4 mb-5">
            <Col md={3}>
              <Card className="border-0 shadow-sm rounded-4 h-100 p-3 bg-gradient-earnings text-white" style={{ background: 'linear-gradient(45deg, #11998e, #38ef7d)' }}>
                <div className="d-flex align-items-center gap-3">
                  <div className="p-3 bg-white bg-opacity-25 rounded-4">
                    <DollarSign size={24} />
                  </div>
                  <div>
                    <h6 className="opacity-75 mb-1">Total Earnings</h6>
                    <h4 className="fw-bold mb-0">₹{platformEarnings.toLocaleString()}</h4>
                    <small className="fw-medium"><TrendingUp size={14}/> {commissionRate}% Platform Fee</small>
                  </div>
                </div>
              </Card>
            </Col>
            <Col md={3}>
              <Card className="border-0 shadow-sm rounded-4 h-100 p-3">
                <div className="d-flex align-items-center gap-3">
                  <div className="p-3 bg-success bg-opacity-10 rounded-4 text-success">
                    <Store size={24} />
                  </div>
                  <div>
                    <h6 className="text-muted mb-1">Restaurants</h6>
                    <h4 className="fw-bold mb-0">{data.restaurants.length}</h4>
                    <small className="text-muted">{data.restaurants.filter(r => r.status === 'PENDING').length} Pending Approval</small>
                  </div>
                </div>
              </Card>
            </Col>
            <Col md={3}>
              <Card className="border-0 shadow-sm rounded-4 h-100 p-3">
                <div className="d-flex align-items-center gap-3">
                  <div className="p-3 bg-info bg-opacity-10 rounded-4 text-info">
                    <Users size={24} />
                  </div>
                  <div>
                    <h6 className="text-muted mb-1">Platform Users</h6>
                    <h4 className="fw-bold mb-0">{data.users.length}</h4>
                    <small className="text-muted">Customers & Agents</small>
                  </div>
                </div>
              </Card>
            </Col>
            <Col md={3}>
              <Card className="border-0 shadow-sm rounded-4 h-100 p-3">
                <div className="d-flex align-items-center gap-3">
                  <div className="p-3 bg-warning bg-opacity-10 rounded-4 text-warning">
                    <UserPlus size={24} />
                  </div>
                  <div>
                    <h6 className="text-muted mb-1">Active Agents</h6>
                    <h4 className="fw-bold mb-0">{data.agents.length}</h4>
                    <small className="text-muted">{data.agents.filter(a => a.isAvailable).length} Currently Online</small>
                  </div>
                </div>
              </Card>
            </Col>
          </Row>

          <Row>
            <Col md={8}>
              <Card className="border-0 shadow-sm rounded-4 overflow-hidden mb-4">
                <Card.Header className="bg-white border-0 py-3 d-flex justify-content-between align-items-center">
                  <h5 className="fw-bold mb-0">Recent Platform Orders</h5>
                  <Button variant="link" className="text-primary p-0" onClick={() => setActiveTab('orders')}>View All</Button>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive hover className="mb-0 align-middle">
                    <thead className="bg-light">
                      <tr>
                        <th className="px-4 border-0 text-muted small fw-bold text-uppercase">Order ID</th>
                        <th className="border-0 text-muted small fw-bold text-uppercase">Amount</th>
                        <th className="border-0 text-muted small fw-bold text-uppercase">Commission</th>
                        <th className="border-0 text-muted small fw-bold text-uppercase">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.orders.slice(-6).reverse().map(order => (
                        <tr key={order.id}>
                          <td className="px-4 fw-medium text-primary">#ORD-{order.id}</td>
                          <td>₹{order.totalAmount}</td>
                          <td className="text-success fw-bold">₹{(order.totalAmount * (commissionRate / 100)).toFixed(2)}</td>
                          <td>
                            <Badge bg={
                              order.status === 'DELIVERED' ? 'success' : 
                              order.status === 'CANCELLED' ? 'danger' : 'primary'
                            } className="rounded-pill px-3 py-1 bg-opacity-10 text-dark border-0" style={{ fontSize: '0.75rem' }}>
                              {order.status}
                            </Badge>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="border-0 shadow-sm rounded-4 p-4 h-100 bg-light">
                <h6 className="fw-bold mb-4">Earnings Breakdown</h6>
                <div className="d-flex flex-column gap-3">
                  <div className="p-3 bg-white rounded-3 shadow-sm d-flex justify-content-between align-items-center">
                    <span className="text-muted">Total Sales</span>
                    <span className="fw-bold">₹{totalRevenue.toLocaleString()}</span>
                  </div>
                  <div className="p-3 bg-white rounded-3 shadow-sm d-flex justify-content-between align-items-center">
                    <span className="text-muted">Platform Fee ({commissionRate}%)</span>
                    <span className="fw-bold text-success">₹{platformEarnings.toLocaleString()}</span>
                  </div>
                  <div className="p-3 bg-white rounded-3 shadow-sm d-flex justify-content-between align-items-center">
                    <span className="text-muted">Completed Orders</span>
                    <span className="fw-bold">{deliveredOrders.length}</span>
                  </div>
                  <hr />
                  <div className="text-center">
                    <p className="small text-muted mb-0">Total platform revenue is calculated from delivered orders only.</p>
                  </div>
                </div>
              </Card>
            </Col>
          </Row>
        </Tab>

        <Tab eventKey="restaurants" title={<><Store size={18} className="me-2"/>Restaurants</>}>
          {/* ... existing restaurants tab content ... */}
          <Card className="border-0 shadow-sm rounded-4 overflow-hidden">
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0 align-middle">
                <thead className="bg-light">
                  <tr>
                    <th className="px-4 border-0 text-muted small fw-bold text-uppercase">Restaurant Name</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Cuisine</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Status</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.restaurants.map(rest => (
                    <tr key={rest.id}>
                      <td className="px-4">
                        <div className="fw-bold">{rest.name}</div>
                        <small className="text-muted">{rest.location}</small>
                      </td>
                      <td>{rest.cuisine}</td>
                      <td>
                        <Badge bg={
                          rest.status === 'APPROVED' ? 'success' : 
                          rest.status === 'PENDING' ? 'warning' : 'danger'
                        } className="rounded-pill px-3">
                          {rest.status}
                        </Badge>
                      </td>
                      <td>
                        <div className="d-flex gap-2">
                          {rest.status === 'PENDING' && (
                            <>
                              <Button variant="success" size="sm" onClick={() => handleApprove(rest.id, true)}>
                                <CheckCircle size={14} className="me-1"/> Approve
                              </Button>
                              <Button variant="warning" size="sm" onClick={() => handleApprove(rest.id, false)}>
                                <XCircle size={14} className="me-1"/> Reject
                              </Button>
                            </>
                          )}
                          <Button variant="outline-danger" size="sm" onClick={() => handleDeleteRestaurant(rest.id)}>
                            <Trash2 size={14} />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="orders" title={<><ClipboardList size={18} className="me-2"/>All Orders</>}>
          <Card className="border-0 shadow-sm rounded-4 overflow-hidden">
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0 align-middle">
                <thead className="bg-light">
                  <tr>
                    <th className="px-4 border-0 text-muted small fw-bold text-uppercase">Order ID</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Restaurant</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Status</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Total Amount</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Platform Fee</th>
                    <th className="border-0 text-muted small fw-bold text-uppercase">Date</th>
                  </tr>
                </thead>
                <tbody>
                  {data.orders.slice().reverse().map(order => (
                    <tr key={order.id}>
                      <td className="px-4 fw-bold text-primary">#ORD-{order.id}</td>
                      <td>
                        <div className="fw-medium">ID: {order.restaurantId}</div>
                        <small className="text-muted">Customer ID: {order.customerId}</small>
                      </td>
                      <td>
                        <Badge bg={
                          order.status === 'DELIVERED' ? 'success' : 
                          order.status === 'CANCELLED' ? 'danger' : 'primary'
                        } className="rounded-pill px-3 py-1 bg-opacity-10 text-dark border-0">
                          {order.status}
                        </Badge>
                      </td>
                      <td className="fw-bold">₹{order.totalAmount}</td>
                      <td className="text-success fw-bold">₹{(order.totalAmount * (commissionRate / 100)).toFixed(2)}</td>
                      <td className="text-muted">{new Date(order.createdAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="users" title={<><Users size={18} className="me-2"/>User & Agents</>}>
          <Row>
            <Col md={7}>
              <Card className="border-0 shadow-sm rounded-4 overflow-hidden mb-4">
                <Card.Header className="bg-white py-3 border-0">
                  <h6 className="fw-bold mb-0">Platform Customers</h6>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive hover className="mb-0 align-middle">
                    <thead className="bg-light">
                      <tr>
                        <th className="px-4 border-0 text-muted small fw-bold text-uppercase">User</th>
                        <th className="border-0 text-muted small fw-bold text-uppercase">Email</th>
                        <th className="border-0 text-muted small fw-bold text-uppercase">Role</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.users.map(user => (
                        <tr key={user.id}>
                          <td className="px-4 fw-bold">{user.username}</td>
                          <td className="text-muted">{user.email}</td>
                          <td><Badge bg="light" className="text-dark border rounded-pill px-3">{user.role}</Badge></td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            </Col>
            <Col md={5}>
              <Card className="border-0 shadow-sm rounded-4 overflow-hidden">
                <Card.Header className="bg-white py-3 border-0">
                  <h6 className="fw-bold mb-0">Delivery Agents</h6>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive hover className="mb-0 align-middle">
                    <thead className="bg-light">
                      <tr>
                        <th className="px-4 border-0 text-muted small fw-bold text-uppercase">Agent</th>
                        <th className="border-0 text-muted small fw-bold text-uppercase">Availability</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.agents.map(agent => (
                        <tr key={agent.id}>
                          <td className="px-4">
                            <div className="fw-bold">{agent.name}</div>
                            <small className="text-muted">Agent ID: {agent.id}</small>
                          </td>
                          <td>
                            <Badge bg={agent.isAvailable ? 'success' : 'secondary'} className="rounded-pill px-3">
                              {agent.isAvailable ? 'Online' : 'Offline'}
                            </Badge>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Tab>
        <Tab eventKey="settings" title={<><Settings size={18} className="me-2"/>Settings</>}>
          <Card className="border-0 shadow-sm rounded-4 p-5 text-center">
            <div className="mx-auto bg-light p-4 rounded-circle mb-4" style={{ width: 'fit-content' }}>
              <Settings size={48} className="text-primary" />
            </div>
            <h3 className="fw-bold mb-3">Platform Configuration</h3>
            <p className="text-muted mb-5">Adjust the platform-wide commission rates and business settings.</p>
            
            <div className="mx-auto" style={{ maxWidth: '400px' }}>
              <Card className="bg-light border-0 p-4 rounded-4 text-start">
                <label className="fw-bold mb-2">Platform Commission Rate (%)</label>
                <div className="d-flex gap-2">
                  <input 
                    type="number" 
                    className="form-control form-control-lg rounded-pill"
                    value={newCommissionRate}
                    onChange={(e) => setNewCommissionRate(e.target.value)}
                    min="0"
                    max="100"
                  />
                  <Button 
                    className="btn-primary-custom px-4 rounded-pill"
                    onClick={handleUpdateCommission}
                    disabled={updating}
                  >
                    {updating ? 'Saving...' : 'Update'}
                  </Button>
                </div>
                <small className="text-muted mt-2">Current Rate: {commissionRate}% (Effective on all delivered orders)</small>
              </Card>
            </div>
          </Card>
        </Tab>
      </Tabs>
    </Container>
  );
};

export default AdminDashboard;
