import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/myratings_css.css";

function MyRatings() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("My Ratings");
  const [user, setUser] = useState(null);
  const [myRatings, setMyRatings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [showLogoutModal, setShowLogoutModal] = useState(false);

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

  // Filter ratings based on search term
  const filteredRatings = myRatings.filter(rating => {
    const serviceName = rating.service?.serviceName || "";
    return serviceName.toLowerCase().includes(searchTerm.toLowerCase());
  });

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
          className={`star ${i <= rating ? "active" : ""}`}
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
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading your ratings...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-layout">
      {/* Sidebar */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <div className="logo">
            {!sidebarCollapsed && <span className="logo-text">Dashboard</span>}
          </div>
        </div>

        <nav className="sidebar-nav">
          <button 
            className={`nav-item ${activeTab === "Services" ? "active" : ""}`}
            onClick={() => navigate("/dashboard")}
          >
            {!sidebarCollapsed && <span className="nav-label">Services</span>}
          </button>
          <button 
            className={`nav-item ${activeTab === "My Ratings" ? "active" : ""}`}
            onClick={() => setActiveTab("My Ratings")}
          >
            {!sidebarCollapsed && <span className="nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-sidebar-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="dashboard-header">
          <div className="header-content">
            <h1 className="page-title">My Ratings</h1>
            <div className="header-search">
              <input
                type="text"
                placeholder="Search ratings..."
                className="search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div className="header-actions">
              <div className="user-avatar" onClick={() => navigate("/profile")}>
                {user ? user.username.charAt(0).toUpperCase() : "U"}
              </div>
            </div>
          </div>
        </header>

        {/* Ratings Grid */}
        <section className="services-grid">
          {!loading && filteredRatings.map((rating) => (
            <div key={rating.ratingId} className="service-card">
              <div className="service-image">
                <img 
                  src={getImageUrl(rating.serviceId)} 
                  alt={rating.service?.serviceName || "Service"}
                  style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "10px" }}
                  onError={(e) => {
                    e.target.src = "https://via.placeholder.com/70x70?text=No+Image";
                  }}
                />
              </div>
              <div className="service-info">
                <h3 className="service-name">
                  {rating.service?.serviceName || `Service #${rating.serviceId}`}
                </h3>
                <p className="service-category">
                  {rating.service?.serviceCategory || "Unknown Category"}
                </p>
                <div className="rating-stars">
                  {renderStars(rating.starRate)}
                  <span className="rating-value">{rating.starRate}.0 / 5.0</span>
                </div>
                {rating.feedbackText && (
                  <div className="rating-feedback">
                    <p>"{rating.feedbackText}"</p>
                  </div>
                )}
                <div className="rating-date">
                  Rated on {formatDate(rating.dateCreated)}
                </div>
                <button className="rate-btn" onClick={() => navigate(`/rate-service/${rating.serviceId}`)}>
                  View Service
                </button>
              </div>
            </div>
          ))}
        </section>

        {!loading && filteredRatings.length === 0 && (
          <div className="no-results">
            No ratings found matching "{searchTerm}"
          </div>
        )}

        {loading && (
          <div className="loading-state">Loading your ratings...</div>
        )}
      </main>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="logout-modal-overlay">
          <div className="logout-modal">
            <div className="logout-text">Are you sure you want to logout?</div>
            <div className="logout-buttons">
              <button className="confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyRatings;