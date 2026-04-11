import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/myratings_css.css";

// PREDEFINED CATEGORIES
const PREDEFINED_CATEGORIES = [
  "Food & Hospitality",
  "Medical & Health",
  "Retail & Commercial",
  "Personal & Lifestyle"
];

function MyRatings() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("My Ratings");
  const [user, setUser] = useState(null);
  const [myRatings, setMyRatings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Filter ratings based on search term and category
  const filteredRatings = myRatings.filter(rating => {
    const serviceName = rating.service?.serviceName || "";
    const serviceCategory = rating.service?.serviceCategory || "";
    const matchesSearch = serviceName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || serviceCategory === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Check authentication and fetch user's ratings
  useEffect(() => {
    const fetchUserRatings = async () => {
      try {
        const authRes = await fetch("http://localhost:8080/api/auth/me", {
          credentials: "include",
        });
        
        if (!authRes.ok) {
          navigate("/");
          return;
        }
        
        const userData = await authRes.json();
        
        if (userData.role === "ADMIN") {
          navigate("/admindashboard");
          return;
        }
        
        setUser(userData);
        
        const ratingsRes = await fetch(`http://localhost:8080/api/ratings/user/${userData.id}`, {
          credentials: "include",
        });
        
        if (ratingsRes.ok) {
          const ratingsData = await ratingsRes.json();
          
          const ratingsWithServices = await Promise.all(
            ratingsData.map(async (rating) => {
              const serviceRes = await fetch(`http://localhost:8080/api/services/${rating.serviceId}`, {
                credentials: "include",
              });
              if (serviceRes.ok) {
                const serviceData = await serviceRes.json();
                return {
                  ...rating,
                  service: serviceData
                };
              }
              return rating;
            })
          );
          
          setMyRatings(ratingsWithServices);
        }
      } catch (error) {
        console.error("Error fetching ratings:", error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchUserRatings();
  }, [navigate]);

  const handleLogoutClick = () => setShowLogoutModal(true);

  const confirmLogout = async () => {
    try {
      await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
      setShowLogoutModal(false);
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const cancelLogout = () => setShowLogoutModal(false);

  const getImageUrl = (serviceId) => {
    return `http://localhost:8080/api/services/${serviceId}/image`;
  };

  const renderStars = (rating) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <span
          key={i}
          className={`myratings-star ${i <= rating ? "active" : ""}`}
        >
          ★
        </span>
      );
    }
    return stars;
  };

  const formatDate = (dateString) => {
    if (!dateString) return "Recently";
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (loading) {
    return (
      <div className="myratings-loading">
        <div className="loading-spinner"></div>
        <p>Loading your ratings...</p>
      </div>
    );
  }

  return (
    <div className="myratings-layout">
      {/* Sidebar */}
      <aside className={`myratings-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="myratings-sidebar-header">
          <div className="myratings-logo">
            
            {!sidebarCollapsed && <span className="myratings-logo-text">Dashboard</span>}
          </div>
          <button 
            className="myratings-collapse-btn" 
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="myratings-nav">
          <button 
            className={`myratings-nav-item ${activeTab === "Services" ? "active" : ""}`}
            onClick={() => navigate("/dashboard")}
          >
            {!sidebarCollapsed && <span className="myratings-nav-label">Services</span>}
          </button>
          <button 
            className={`myratings-nav-item ${activeTab === "My Ratings" ? "active" : ""}`}
            onClick={() => setActiveTab("My Ratings")}
          >
            {!sidebarCollapsed && <span className="myratings-nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="myratings-sidebar-footer">
          <button className="myratings-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="myratings-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="myratings-main-content">
        <header className="myratings-header">
          <div className="myratings-header-content">
            <div>
              <h1 className="myratings-page-title">My Ratings</h1>
              
            </div>

            <div className="myratings-search-wrapper">
              <input
                type="text"
                placeholder="Search rated services..."
                className="myratings-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="myratings-header-actions">
              <div className="myratings-avatar" onClick={() => navigate("/profile")}>
                {user ? user.username.charAt(0).toUpperCase() : "U"}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Section */}
        <section className="myratings-filter-section">
          <h3 className="myratings-filter-title">Filter by Category</h3>
          <div className="myratings-filter-group">
            <button
              className={`myratings-filter-chip ${selectedCategory === "All" ? "active" : ""}`}
              onClick={() => setSelectedCategory("All")}
            >
              All
            </button>
            {PREDEFINED_CATEGORIES.map((category) => (
              <button
                key={category}
                className={`myratings-filter-chip ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Ratings Grid */}
        <section className="myratings-grid">
          {filteredRatings.map((rating) => (
            <div key={rating.ratingId} className="myratings-card">
              <div className="myratings-card-image">
                <img 
                  src={getImageUrl(rating.serviceId)} 
                  alt={rating.service?.serviceName || "Service"}
                  onError={(e) => {
                    e.target.src = "https://via.placeholder.com/100x100?text=No+Image";
                  }}
                />
              </div>
              <div className="myratings-card-info">
                <h3 className="myratings-service-name">
                  {rating.service?.serviceName || `Service #${rating.serviceId}`}
                </h3>
                <p className="myratings-service-category">
                  {rating.service?.serviceCategory || "Unknown Category"}
                </p>
                <div className="myratings-stars-container">
                  <div className="myratings-stars">
                    {renderStars(rating.starRate)}
                  </div>
                  <span className="myratings-rating-value">{rating.starRate}.0 / 5.0</span>
                </div>
                {rating.feedbackText && (
                  <div className="myratings-feedback">
                    <p>"{rating.feedbackText}"</p>
                  </div>
                )}
                <div className="myratings-date">
                  Rated on {formatDate(rating.dateCreated)}
                </div>
                <button 
                  className="myratings-view-btn"
                  onClick={() => navigate(`/rate-service/${rating.serviceId}`)}
                >
                  View Service
                </button>
              </div>
            </div>
          ))}
        </section>

        {filteredRatings.length === 0 && (
          <div className="myratings-no-results">
            No ratings found matching your criteria.
          </div>
        )}
      </main>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="myratings-logout-overlay">
          <div className="myratings-logout-modal">
            <div className="myratings-logout-modal-text">Are you sure you want to logout?</div>
            <div className="myratings-logout-modal-actions">
              <button className="myratings-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="myratings-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyRatings;
