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
          className={`myrating-star ${i <= rating ? "active" : ""}`}
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
      <div className="myrating-loading">
        <div className="loading-spinner"></div>
        <p>Loading your ratings...</p>
      </div>
    );
  }

  return (
    <div className="myrating-layout">
      {/* Sidebar */}
      <aside className={`myrating-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="myrating-sidebar-header">
          <div className="myrating-logo">
            
            {!sidebarCollapsed && <span className="myrating-logo-text">Dashboard</span>}
          </div>
          <button 
            className="myrating-collapse-btn" 
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="myrating-nav">
          <button 
            className={`myrating-nav-item ${activeTab === "Services" ? "active" : ""}`}
            onClick={() => navigate("/dashboard")}
          >
            {!sidebarCollapsed && <span className="myrating-nav-label">Services</span>}
          </button>
          <button 
            className={`myrating-nav-item ${activeTab === "My Ratings" ? "active" : ""}`}
            onClick={() => setActiveTab("My Ratings")}
          >
            {!sidebarCollapsed && <span className="myrating-nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="myrating-sidebar-footer">
          <button className="myrating-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="myrating-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="myrating-main-content">
        <header className="myrating-header">
          <div className="myrating-header-content">
            <div>
              <h1 className="myrating-page-title">My Ratings</h1>
              <p className="myrating-page-subtitle">View and manage your service ratings</p>
            </div>

            <div className="myrating-search-wrapper">
              <input
                type="text"
                placeholder="Search rated services..."
                className="myrating-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="myrating-header-actions">
              <div className="myrating-avatar" onClick={() => navigate("/profile")}>
                {user ? user.username.charAt(0).toUpperCase() : "U"}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Bar */}
        <section className="myrating-filter-bar">
          <h3 className="myrating-filter-label">Filter by Category</h3>
          <div className="myrating-filter-group">
            <button
              className={`myrating-filter-chip ${selectedCategory === "All" ? "active" : ""}`}
              onClick={() => setSelectedCategory("All")}
            >
              All
            </button>
            {PREDEFINED_CATEGORIES.map(category => (
              <button
                key={category}
                className={`myrating-filter-chip ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Ratings Grid */}
        <section className="myrating-records-grid">
          {filteredRatings.map((rating) => (
            <div key={rating.ratingId} className="myrating-record-card">
              <div className="myrating-record-thumbnail">
                <img 
                  src={getImageUrl(rating.serviceId)} 
                  alt={rating.service?.serviceName || "Service"}
                  onError={(e) => {
                    e.target.src = "https://via.placeholder.com/70x70?text=No+Image";
                  }}
                />
              </div>
              <div className="myrating-record-details">
                <h3 className="myrating-record-name">
                  {rating.service?.serviceName || `Service #${rating.serviceId}`}
                </h3>
                <p className="myrating-record-category">
                  {rating.service?.serviceCategory || "Unknown Category"}
                </p>
                <div className="myrating-stars-container">
                  {renderStars(rating.starRate)}
                  <span className="myrating-rating-value">{rating.starRate}.0 / 5.0</span>
                </div>
                {rating.feedbackText && (
                  <div className="myrating-feedback">
                    "{rating.feedbackText}"
                  </div>
                )}
                <div className="myrating-date">
                  Rated on {formatDate(rating.dateCreated)}
                </div>
                <div className="myrating-record-actions">
                  <button 
                    className="myrating-record-action-btn view"
                    onClick={() => navigate(`/rate-service/${rating.serviceId}`)}
                  >
                    View Service
                  </button>
                </div>
              </div>
            </div>
          ))}
        </section>

        {filteredRatings.length === 0 && (
          <div className="myrating-no-results">
            No ratings found matching your criteria.
          </div>
        )}
      </main>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="myrating-logout-overlay">
          <div className="myrating-logout-modal">
            <div className="myrating-logout-modal-text">Are you sure you want to logout?</div>
            <div className="myrating-logout-modal-actions">
              <button className="myrating-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="myrating-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyRatings;
