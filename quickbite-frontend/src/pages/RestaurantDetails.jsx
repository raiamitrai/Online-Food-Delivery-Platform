import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Badge } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import api from '../api';

const RestaurantDetails = () => {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menus, setMenus] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    try {
      const restRes = await api.get(`/api/restaurants/${id}`);
      let restaurantData = restRes.data;

      try {
        const reviewsRes = await api.get(`/api/reviews/restaurant/${id}`);
        const reviews = reviewsRes.data || [];
        if (reviews.length > 0) {
          const avg = reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length;
          restaurantData.rating = parseFloat(avg.toFixed(1));
          restaurantData.reviewCount = reviews.length;
        }
      } catch (e) {
        console.error("Could not fetch reviews");
      }

      setRestaurant(restaurantData);

      const menuRes = await api.get(`/api/menus/restaurant/${id}`);
      setMenus(menuRes.data);
    } catch (error) {
      console.error("Error fetching details", error);
      // Fallback dummy data
      setRestaurant({ id, name: 'Spice Route', location: 'Downtown', cuisine: 'Indian', rating: 4.5, status: 'OPEN' });
      setMenus([
        {
          id: 1, name: 'Starters', items: [
            { id: 101, name: 'Paneer Tikka', description: 'Spicy cottage cheese', price: 250, isVegetarian: true }
          ]
        },
        {
          id: 2, name: 'Main Course', items: [
            { id: 102, name: 'Chicken Biryani', description: 'Aromatic rice with chicken', price: 350, isVegetarian: false }
          ]
        }
      ]);
    }
  };

  const handleAddToCart = async (menuItem) => {
    const customerId = sessionStorage.getItem('customerId');
    if (!customerId) {
        navigate('/login');
        return;
    }
    
    try {
        await api.post(`/api/cart/${customerId}/items`, {
            menuItemId: menuItem.id,
            menuItemName: menuItem.name,
            quantity: 1,
            price: menuItem.price,
            restaurantId: restaurant.id
        });
        alert('Added to cart!');
    } catch (err) {
        console.error("Add to cart error:", err);
        const backendMsg = err.response?.data?.message;
        if (backendMsg && backendMsg.includes("multiple restaurants")) {
            alert("⚠️ You already have items from another restaurant in your cart. Please clear your cart first!");
        } else {
            alert(backendMsg || 'Failed to add item. Please try again.');
        }
    }
  };

  if (!restaurant) return <div className="text-center py-5">Loading...</div>;

  return (
    <>
      <div className="bg-dark text-white py-5 mb-5">
        <Container>
          <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-end">
            <div>
              <h1 className="fw-bold display-4">{restaurant.name}</h1>
              <p className="fs-5 text-light opacity-75 mb-0">
                {restaurant.cuisine || 'Multi-Cuisine'} • {restaurant.location} • <Badge bg="success" className="ms-1">{restaurant.status === 'APPROVED' ? 'OPEN' : restaurant.status}</Badge>
              </p>
            </div>
            <div className="mt-3 mt-md-0 text-md-end">
              <div className="bg-success text-white px-3 py-2 rounded-3 d-inline-flex align-items-center gap-2">
                <span className="fw-bold fs-4">{restaurant.rating > 0 ? restaurant.rating : 4.5}</span>
                <span className="fs-5">★</span>
              </div>
              {restaurant.reviewCount > 0 && <div className="text-light opacity-75 small mt-1">{restaurant.reviewCount} Reviews</div>}
            </div>
          </div>
        </Container>
      </div>

      <Container>
        {menus.map((category) => (
          <div key={category.id} className="mb-5">
            <h4 className="fw-bold mb-4 pb-2 border-bottom">{category.name}</h4>
            <Row className="g-4">
              {category.items?.map((item) => (
                <Col md={6} key={item.id}>
                  <Card className="card-premium h-100 border-0 shadow-sm hover-shadow transition-all">
                    <Card.Body className="p-4 d-flex justify-content-between align-items-center">
                      <div>
                        <div className="d-flex align-items-center gap-2 mb-2">
                          <Card.Title className="fw-bold mb-0">{item.name}</Card.Title>
                          {item.isVegetarian ? 
                            <Badge bg="success" pill>Veg</Badge> : 
                            <Badge bg="danger" pill>Non-Veg</Badge>
                          }
                        </div>
                        <Card.Text className="text-muted small mb-3">{item.description}</Card.Text>
                        <div className="fw-bold fs-5 text-dark">₹{item.price}</div>
                      </div>
                      <div>
                        {(item.isAvailable !== undefined ? item.isAvailable : item.available) !== false ? (
                          <Button 
                            variant="outline-danger" 
                            className="d-flex align-items-center gap-1 rounded-pill px-4 py-2 fw-bold"
                            onClick={() => handleAddToCart(item)}
                          >
                            <Plus size={16} /> ADD
                          </Button>
                        ) : (
                          <Badge bg="secondary" className="px-3 py-2 rounded-pill fs-6 text-uppercase fw-bold opacity-75">Out of Stock</Badge>
                        )}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </div>
        ))}
      </Container>
    </>
  );
};

export default RestaurantDetails;
