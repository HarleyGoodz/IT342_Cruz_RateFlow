import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import NotificationBell from '../notifications/NotificationBell';
import "../../css/rateservice_css.css";

function RateService() {
  const navigate = useNavigate();
  const { serviceId } = useParams();
  const [service, setService] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [userRating, setUserRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [feedback, setFeedback] = useState("");
  const [feedbacks, setFeedbacks] = useState([]);
  const [ratingStats, setRatingStats] = useState({
    average: 0,
    total: 0,
    distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  });
  const [hasRated, setHasRated] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showFeedbackSuccessModal, setShowFeedbackSuccessModal] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Services");

  // Check authentication and fetch data
  useEffect(() => {
    const authenticateAndFetch = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/auth/me", {
          credentials: "include",
        });
        
        if (!res.ok) {
          throw new Error("Not authenticated");
        }
        
        const data = await res.json();
        
        if (data.role === "ADMIN") {
          navigate("/admindashboard");
          return;
        }
        
        setUser(data);
        await fetchServiceDetails(data.id);
        
      } catch (error) {
        console.error("Auth error:", error);
        navigate("/");
      }
    };
    
    authenticateAndFetch();
  }, [serviceId, navigate]);

  const fetchServiceDetails = async (userId) => {
    try {
      const serviceResponse = await fetch(`http://localhost:8080/api/services/${serviceId}`, {
        credentials: "include",
      });
      
      if (serviceResponse.ok) {
        const serviceData = await serviceResponse.json();
        setService(serviceData);
        
        await Promise.all([
          fetchRatingStats(),
          fetchFeedbacks(),
          checkUserRating(userId)
        ]);
      } else {
        navigate("/dashboard");
      }
    } catch (error) {
      console.error("Error fetching service:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchRatingStats = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/ratings/service/${serviceId}/stats`, {
        credentials: "include",
      });
      
      if (response.ok) {
        const data = await response.json();
        setRatingStats(data);
      }
    } catch (error) {
      console.error("Error fetching rating stats:", error);
    }
  };

  const fetchFeedbacks = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/ratings/service/${serviceId}/feedbacks`, {
        credentials: "include",
      });
      
      if (response.ok) {
        const data = await response.json();
        setFeedbacks(data);
      }
    } catch (error) {
      console.error("Error fetching feedbacks:", error);
    }
  };

  const checkUserRating = async (userId) => {
    if (!userId) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/ratings/check/user/${userId}/service/${serviceId}`, {
        credentials: "include",
      });
      
      if (response.ok) {
        const data = await response.json();
        if (data.hasRated) {
          setHasRated(true);
          setUserRating(data.rating);
          if (data.feedback) {
            setFeedback(data.feedback);
          }
        }
      }
    } catch (error) {
      console.error("Error checking user rating:", error);
    }
  };

  const handleRatingSubmit = async () => {
    if (userRating === 0) {
      alert("Please select a rating before submitting.");
      return;
    }

    if (hasRated) {
      alert("You have already rated this service!");
      return;
    }

    setSubmitting(true);
    
    try {
      const response = await fetch("http://localhost:8080/api/ratings/submit", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          userId: user.id,
          serviceId: parseInt(serviceId),
          starRate: userRating,
          feedbackText: feedback,
          userName: user.username
        }),
      });

      if (response.ok) {
        setShowFeedbackSuccessModal(true);
        setHasRated(true);
        await Promise.all([
          fetchRatingStats(),
          fetchFeedbacks()
        ]);
      } else {
        const error = await response.json();
        alert(error.error || "Failed to submit rating. Please try again.");
      }
    } catch (error) {
      console.error("Error submitting rating:", error);
      alert("Error submitting rating.");
    } finally {
      setSubmitting(false);
    }
  };

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

  const getImageUrl = () => {
    return `http://localhost:8080/api/services/${serviceId}/image`;
  };

  const renderStars = (rating, interactive = false) => {
    const stars = [];
    const displayRating = interactive ? (hoverRating || userRating) : rating;
    
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <span
          key={i}
          className={`star ${i <= displayRating ? "active" : ""}`}
          onClick={() => interactive && !hasRated && setUserRating(i)}
          onMouseEnter={() => interactive && !hasRated && setHoverRating(i)}
          onMouseLeave={() => interactive && !hasRated && setHoverRating(0)}
        >
          ★
        </span>
      );
    }
    return stars;
  };

  if (loading) {
    return (
      <div className="myrating-loading">
        <div className="loading-spinner"></div>
        <p>Loading your ratings...</p>
      </div>
    );
  }

  if (!service) {
    return <div className="error-container">Service not found</div>;
  }

  return (
    <div className="dashboard-layout rate-service-page">
      {/* SIDEBAR */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <div className="logo">
            
            {!sidebarCollapsed && <span className="logo-text">Dashboard</span>}

            <button
      className="dashboard-back-btn"
      onClick={() => navigate(-1)}
    >
      ←
    </button>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button 
            className={`nav-item ${activeTab === "Services" ? "active" : ""}`}
            onClick={() => {
              setActiveTab("Services");
              navigate("/dashboard");
            }}
          >
            {!sidebarCollapsed && <span className="nav-label">Services</span>}
          </button>
          <button 
            className={`nav-item ${activeTab === "My Ratings" ? "active" : ""}`}
            onClick={() => navigate("/my-ratings")}
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

      {/* MAIN */}
      <main className="main-content">
        {/* HEADER */}
        <header className="dashboard-header">
          <div className="header-content">
            <div className="header-left">
              <h1 className="page-title">Rate Service</h1>
            </div>
            <div className="header-actions">
              <NotificationBell />
              <div className="user-avatar" onClick={() => navigate("/profile")} style={{ cursor: "pointer" }}>
                {user?.username?.charAt(0).toUpperCase() || "U"}
              </div>
            </div>
          </div>
        </header>

        {/* RATE CARD */}
        <section className="rate-card">
          <div className="service-image">
            <img 
              src={getImageUrl()} 
              alt={service.serviceName}
              style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "10px" }}
              onError={(e) => {
                e.target.src = "https://via.placeholder.com/120x120?text=No+Image";
              }}
            />
          </div>
          <div className="rate-info">
            <h2 className="service-name">{service.serviceName}</h2>
            <p className="service-category">{service.serviceCategory}</p>

            {/* STARS */}
            <div className="stars">
              {hasRated ? (
                renderStars(userRating)
              ) : (
                renderStars(0, true)
              )}
            </div>

            {/* FEEDBACK */}
            <textarea
              placeholder="Write Feedback"
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              className="feedback-input"
              disabled={hasRated}
            />

            {!hasRated && (
              <button className="submit-btn" onClick={handleRatingSubmit} disabled={submitting}>
                {submitting ? "Submitting..." : "Submit"}
              </button>
            )}
            
            {hasRated && (
              <div style={{ marginTop: "12px", color: "#10b981", fontSize: "14px" }}>
                ✓ You have already rated this service
              </div>
            )}
          </div>
        </section>

        {/* LOWER GRID */}
        <section className="lower-grid">
          {/* RATING STATS */}
          <div className="stats-card">
            <h3>Rating Statistics</h3>
            <div className="rating-value">
              <div className="stars">
                {renderStars(Math.round(ratingStats.average))}
              </div>
              <span>{ratingStats.average.toFixed(1)}</span>
            </div>
            <div>Based on {ratingStats.total} ratings</div>
          </div>

          {/* DESCRIPTION */}
          <div className="description-card">
            <h3>Description</h3>
            <div className="description-box">
              {service.serviceDescription || "No description available for this service yet."}
            </div>
          </div>

          {/* FEEDBACKS */}
          <div className="feedbacks-card">
            <h3>Feedbacks ({feedbacks.length})</h3>
            {feedbacks.length === 0 ? (
              <p>No feedbacks yet. Be the first to share your experience!</p>
            ) : (
              feedbacks.map((fb, index) => (
                <div key={fb.ratingId || index} className="feedback-item">
                  <div className="avatar">
                    {fb.userName?.charAt(0).toUpperCase() || "U"}
                  </div>
                  <div className="feedback-text">
                    <strong>{fb.userName}</strong>
                    <div className="stars" style={{ fontSize: "14px" }}>
                      {renderStars(fb.starRate)}
                    </div>
                    <p>{fb.feedbackText || "No feedback provided."}</p>
                    <small style={{ color: "#64748b", fontSize: "11px" }}>
                      {fb.dateCreated ? new Date(fb.dateCreated).toLocaleDateString() : "Recently"}
                    </small>
                  </div>
                </div>
              ))
            )}
          </div>
        </section>
      </main>

      {/* FEEDBACK SUCCESS MODAL */}
      {showFeedbackSuccessModal && (
        <div className="feedback-success-overlay">
          <div className="feedback-success-modal">
            <div className="feedback-success-icon">✓</div>
            <div className="feedback-success-message">Thank you for your feedback!</div>
            <button className="feedback-success-btn" onClick={() => navigate("/dashboard")}>
              Continue
            </button>
          </div>
        </div>
      )}

      {/* LOGOUT MODAL */}
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

export default RateService;