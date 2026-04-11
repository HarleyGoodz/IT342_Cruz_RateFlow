    import React, { useState, useEffect } from "react";
    import { useNavigate } from "react-router-dom";
    import "../css/myRatings_css.css";

    function MyRatings() {
    const navigate = useNavigate();

    const [sidebarCollapsed, setSidebarCollapsed] =
        useState(false);

    /* FIXED */
    const [activeTab, setActiveTab] =
        useState("My Ratings");

    const [user, setUser] = useState(null);

    const [showLogoutModal, setShowLogoutModal] =
    useState(false);

useEffect(() => {
  let isMounted = true;

  const loadUser = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/me",
        {
          method: "GET",
          credentials: "include"
        }
      );

      if (!response.ok) {
        navigate("/login");
        return;
      }

      const data = await response.json();

      if (!isMounted) return;

      setUser({
        username: data.username,
        email: data.email
      });

      console.log("Loaded from HTTP session");

    } catch (error) {
      console.error("Session check failed:", error);
      navigate("/login");
    }
  };

  loadUser();

  return () => {
    isMounted = false;
  };
}, []);

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
            
            {!sidebarCollapsed && <span className="myratings-logo-text">My Ratings</span>}
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

      {/* Main Panel */}
      <main className="myratings-panel">
        {/* Topbar */}
        <header className="myratings-topbar">
          <div className="myratings-topbar-content">
            <div>
              <h1 className="myratings-page-title">My Ratings</h1>
            </div>

            <div className="myratings-search-wrapper">
              <input
                type="text"
                placeholder="Search ratings..."
                className="myratings-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="myratings-topbar-actions">
              <div className="myratings-avatar" onClick={() => navigate("/profile")}>
                {user ? user.username.charAt(0).toUpperCase() : "U"}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Bar */}
        <section className="myratings-filter-bar">
          <h3 className="myratings-filter-label">Filter by Category</h3>
          <div className="myratings-filter-group">
            <button
              className={`myratings-filter-chip ${selectedCategory === "All" ? "active" : ""}`}
              onClick={() => setSelectedCategory("All")}
            >
              All
            </button>
            {PREDEFINED_CATEGORIES.map(category => (
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
        <section className="myratings-records-grid">
          {filteredRatings.map((rating) => (
            <div key={rating.ratingId} className="myratings-record-card">
              <div className="myratings-record-thumbnail">
                <img 
                  src={getImageUrl(rating.serviceId)} 
                  alt={rating.service?.serviceName || "Service"}
                  onError={(e) => {
                    e.target.src = "https://via.placeholder.com/100x100?text=No+Image";
                  }}
                />
              </div>
              <div className="myratings-record-details">
                <h3 className="myratings-record-name">
                  {rating.service?.serviceName || `Service #${rating.serviceId}`}
                </h3>
                <p className="myratings-record-category">
                  {rating.service?.serviceCategory || "Unknown Category"}
                </p>
                <div className="myratings-rating-stars">
                  {renderStars(rating.starRate)}
                  <span className="myratings-rating-value">{rating.starRate}.0 / 5.0</span>
                </div>
                {rating.feedbackText && (
                  <div className="myratings-rating-feedback">
                    <p>"{rating.feedbackText}"</p>
                  </div>
                )}
                <div className="myratings-rating-date">
                  Rated on {formatDate(rating.dateCreated)}
                </div>
                <div className="myratings-record-actions">
                  <button 
                    className="myratings-record-action-btn view"
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
