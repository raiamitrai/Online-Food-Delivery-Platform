import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button } from 'react-bootstrap';
import { Search, Star, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const Home = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const role = sessionStorage.getItem('role');
    if (role === 'OWNER') {
      const restaurantId = sessionStorage.getItem('restaurantId') || sessionStorage.getItem('customerId');
      navigate(`/restaurant-dashboard/${restaurantId}`);
    } else if (role === 'AGENT') {
      const agentId = sessionStorage.getItem('agentId') || sessionStorage.getItem('customerId');
      navigate(`/agent-dashboard/${agentId}`);
    } else {
      fetchRestaurants();
    }
  }, [navigate]);

  const fetchRestaurants = async () => {
    try {
      const response = await api.get('/api/restaurants');
      // Filter only APPROVED restaurants for the public home page
      let restaurantsData = (response.data || []).filter(r => r.status === 'APPROVED');
      
      try {
        const reviewsRes = await api.get('/api/reviews');
        const reviews = reviewsRes.data || [];
        
        // Calculate average rating per restaurant
        const ratingsMap = {};
        reviews.forEach(review => {
          if (review.entityType === 'RESTAURANT' && review.restaurantId) {
            if (!ratingsMap[review.restaurantId]) ratingsMap[review.restaurantId] = { sum: 0, count: 0 };
            ratingsMap[review.restaurantId].sum += review.rating;
            ratingsMap[review.restaurantId].count += 1;
          }
        });
        
        restaurantsData = restaurantsData.map(r => ({
          ...r,
          rating: ratingsMap[r.id] ? (ratingsMap[r.id].sum / ratingsMap[r.id].count).toFixed(1) : (r.rating > 0 ? r.rating : 4.5)
        }));
      } catch (reviewErr) {
        console.error("Could not fetch reviews, using default ratings", reviewErr);
      }
      
      setRestaurants(restaurantsData);
    } catch (error) {
      console.error("Failed to fetch restaurants", error);
      setRestaurants([]); // No dummy data
    }
  };

  const handleRestaurantClick = (id) => {
    const token = sessionStorage.getItem('token');
    if (!token) {
      alert("Please login to view restaurant details and menu!");
      navigate('/login');
    } else {
      navigate(`/restaurant/${id}`);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    // We filter the visible list instead of navigating
  };

  const filteredRestaurants = restaurants.filter(restaurant => 
    restaurant.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    (restaurant.cuisine && restaurant.cuisine.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const getImageForRestaurant = (name) => {
    if (name.includes('Spice')) return '/images/spice_route.png';
    if (name.includes('Burger')) return '/images/burger_joint.png';
    if (name.includes('Sushi')) return '/images/sushi_zen.png';
    return null; // Return null to use gradient fallback
  };

  const getGradientForRestaurant = (name) => {
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

  const isAdmin = sessionStorage.getItem('role') === 'ADMIN';

  if (isAdmin) {
    return (
      <Container className="py-5">
        <Card className="border-0 shadow-lg rounded-4 overflow-hidden bg-dark text-white position-relative" style={{ minHeight: '400px' }}>
          <div 
            className="position-absolute w-100 h-100" 
            style={{ 
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              opacity: 0.9,
              zIndex: 1
            }}
          ></div>
          <Card.Body className="d-flex flex-column align-items-center justify-content-center text-center p-5" style={{ zIndex: 2 }}>
            <h1 className="display-3 fw-bold mb-3">Welcome to Admin Dashboard</h1>
            <p className="lead mb-4 opacity-75" style={{ maxWidth: '600px' }}>
              Manage your platform's restaurants, monitor real-time orders, and track platform earnings from one central command center.
            </p>
            <div className="d-flex gap-3">
              <Button 
                onClick={() => navigate('/admin-dashboard')} 
                className="btn-light px-5 py-3 rounded-pill fw-bold text-primary shadow-sm"
              >
                Go to Platform Dashboard
              </Button>
            </div>
          </Card.Body>
        </Card>
      </Container>
    );
  }

  return (
    <>
      {/* Hero Section */}
      <section className="hero-section">
        <Container>
          <Row className="justify-content-center text-center">
            <Col md={8}>
              <h1 className="hero-title">Hungry? You're in the right place.</h1>
              <p className="hero-subtitle">Discover the best food & drinks in your city</p>
              <Form className="search-wrapper" onSubmit={handleSearch}>
                <Form.Control
                  type="text"
                  placeholder="Search for restaurants, cuisines, or dishes..."
                  className="search-input"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
                <Button type="submit" className="btn-primary-custom search-btn d-flex align-items-center gap-2">
                  <Search size={18} /> Search
                </Button>
              </Form>
            </Col>
          </Row>
        </Container>
      </section>

      {/* Restaurants Section */}
      <section className="py-5">
        <Container>
          <h3 className="fw-bold mb-4">Top Restaurants in your city</h3>
          <Row className="g-4">
            {filteredRestaurants.map((restaurant) => (
              <Col md={4} key={restaurant.id}>
                <Card 
                  className="card-premium h-100 cursor-pointer" 
                  onClick={() => handleRestaurantClick(restaurant.id)}
                  style={{ cursor: 'pointer' }}
                >
                  {getImageForRestaurant(restaurant.name) ? (
                    <div 
                      style={{ 
                        height: '200px', 
                        backgroundImage: `url(${getImageForRestaurant(restaurant.name)})`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                        borderTopLeftRadius: 'calc(.375rem - 1px)',
                        borderTopRightRadius: 'calc(.375rem - 1px)'
                      }} 
                    ></div>
                  ) : (
                    <div 
                      className="d-flex align-items-center justify-content-center text-center p-3"
                      style={{ 
                        height: '200px', 
                        background: getGradientForRestaurant(restaurant.name),
                        borderTopLeftRadius: 'calc(.375rem - 1px)',
                        borderTopRightRadius: 'calc(.375rem - 1px)'
                      }} 
                    >
                      <h3 className="text-white fw-bold m-0" style={{ textShadow: '1px 2px 4px rgba(0,0,0,0.3)', letterSpacing: '1px', fontSize: '1.8rem' }}>
                        {restaurant.name.toUpperCase()}
                      </h3>
                    </div>
                  )}
                  <Card.Body className="p-4">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <Card.Title className="fw-bold mb-0">{restaurant.name}</Card.Title>
                      <div className="bg-success text-white px-2 py-1 rounded d-flex align-items-center gap-1" style={{ fontSize: '0.85rem', fontWeight: 600 }}>
                        {restaurant.rating || 4.5} <Star size={12} fill="white" />
                      </div>
                    </div>
                    <Card.Text className="text-muted mb-3 d-flex align-items-center gap-2">
                       {restaurant.cuisine || 'Multi-Cuisine'} • {restaurant.location}
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>
    </>
  );
};

export default Home;
