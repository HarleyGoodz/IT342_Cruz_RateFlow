import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../css/admin_rateservice.css";

function AdminRateService() {
  const navigate = useNavigate();
  const { serviceId } = useParams();
  const [service, setService] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [feedbacks, setFeedbacks] = useState([]);
  const [deletingFeedback, setDeletingFeedback] = useState(false);
  const [ratingStats, setRatingStats] = useState({
    average: 0,
    total: 0,
    distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  });
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedFeedback, setSelectedFeedback] = useState(null);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Services");

  useEffect(() => {
    checkAuth();
  }, [serviceId]);

  const checkAuth = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/auth/me", {
        credentials: "include",
      });
      
      if (!res.ok) throw new Error("Not authenticated");
      
      const data = await res.json();
      
      if (data.role !== "ADMIN") {
        alert("Access denied. Admin only.");
        navigate("/dashboard");
        return;
      }
      
      setUser(data);
      await fetchServiceDetails();
      
    } catch (error) {
      console.error("Auth error:", error);
      navigate("/");
    }
  };

  const fetchServiceDetails = async () => {
    try {
      const serviceResponse = await fetch(`http://localhost:8080/api/services/${serviceId}`, {
        credentials: "include",
      });
      
      if (serviceResponse.ok) {
        const serviceData = await serviceResponse.json();
        setService(serviceData);
        
        await Promise.all([
          fetchRatingStats(),
          fetchFeedbacks()
        ]);
      } else {
        navigate("/admindashboard");
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

  const openDeleteModal = (feedback) => {
    setSelectedFeedback(feedback);
    setShowDeleteModal(true);
  };

  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setSelectedFeedback(null);
  };

  const confirmDelete = async () => {
    if (!selectedFeedback) return;
    
    setDeletingFeedback(true);
    
    try {
      const response = await fetch(`http://localhost:8080/api/ratings/delete/${selectedFeedback.ratingId}`, {
        method: "DELETE",
        credentials: "include",
      });
      
      if (response.ok) {
        await Promise.all([
          fetchFeedbacks(),
          fetchRatingStats()
        ]);
        closeDeleteModal();
      } else {
        const error = await response.json();
        alert(error.error || "Failed to delete feedback");
        closeDeleteModal();
      }
    } catch (error) {
      console.error("Error deleting feedback:", error);
      alert("Error deleting feedback");
      closeDeleteModal();
    } finally {
      setDeletingFeedback(false);
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

  const renderStars = (rating) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <span
          key={i}
          className={`admin-rate-star ${i <= rating ? "admin-rate-star-active" : ""}`}
        >
          ★
        </span>
      );
    }
    return stars;
  };

  if (loading) {
    return <div className="admin-rate-loading-container">Loading...</div>;
  }

  if (!service) {
    return <div className="admin-rate-error-container">Service not found</div>;
  }

  return (
    <div className="admin-rate-dashboard-layout">
      {/* SIDEBAR */}
      <aside className={`admin-rate-sidebar ${sidebarCollapsed ? "admin-rate-collapsed" : ""}`}>
        <div className="admin-rate-sidebar-header">
          <div className="admin-rate-logo">
            {!sidebarCollapsed && <span className="admin-rate-logo-text">Admin Panel</span>}
          </div>
          
        </div>

        <nav className="admin-rate-sidebar-nav">
          <button 
            className={`admin-rate-nav-item ${activeTab === "Services" ? "admin-rate-active" : ""}`}
            onClick={() => {
              setActiveTab("Services");
              navigate("/admindashboard");
            }}
          >
            {!sidebarCollapsed && <span className="admin-rate-nav-label">Back to Services</span>}
          </button>
        </nav>

        <div className="admin-rate-sidebar-footer">
          <button className="admin-rate-logout-sidebar-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="admin-rate-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* MAIN */}
      <main className="admin-rate-main-content">
        {/* HEADER */}
        <header className="admin-rate-dashboard-header">
          <div className="admin-rate-header-content">
            <div className="admin-rate-header-left">
              <h1 className="admin-rate-page-title">Manage Feedbacks</h1>
              <p className="admin-rate-badge">
                Admin Mode - You can delete inappropriate feedbacks
              </p>
            </div>
            <div className="admin-rate-header-actions">
              <div className="admin-rate-user-avatar" onClick={() => navigate("/profile")} style={{ cursor: "pointer" }}>
                {user?.username?.charAt(0).toUpperCase() || "A"}
              </div>
            </div>
          </div>
        </header>

        {/* RATE CARD */}
        <section className="admin-rate-card">
          <div className="admin-rate-service-image">
            <img 
              src={getImageUrl()} 
              alt={service.serviceName}
              onError={(e) => {
                e.target.src = "https://via.placeholder.com/120x120?text=No+Image";
              }}
            />
          </div>
          <div className="admin-rate-info">
            <h2 className="admin-rate-service-name">{service.serviceName}</h2>
            <p className="admin-rate-service-category">{service.serviceCategory}</p>
            <p className="admin-rate-service-provider">
              Created by: {service.createdBy}
            </p>
          </div>
        </section>

        {/* LOWER GRID */}
        <section className="admin-rate-lower-grid">
          {/* RATING STATS */}
          <div className="admin-rate-stats-card">
            <h3>Rating Statistics</h3>
            <div className="admin-rate-rating-value">
              <div className="admin-rate-stars">
                {renderStars(Math.round(ratingStats.average))}
              </div>
              <span>{ratingStats.average.toFixed(1)}</span>
            </div>
            <div>Based on {ratingStats.total} ratings</div>
            
            {/* Rating Distribution */}
            {ratingStats.total > 0 && (
              <div className="admin-rate-distribution">
                <h4>Distribution</h4>
                {[5,4,3,2,1].map(star => (
                  <div key={star} className="admin-rate-distribution-item">
                    <div className="admin-rate-distribution-label">
                      <span>{star} stars</span>
                      <span>{ratingStats.distribution[star] || 0}</span>
                    </div>
                    <div className="admin-rate-distribution-bar">
                      <div 
                        className="admin-rate-distribution-fill"
                        style={{ 
                          width: `${((ratingStats.distribution[star] || 0) / ratingStats.total) * 100}%`
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* DESCRIPTION */}
          <div className="admin-rate-description-card">
            <h3>Description</h3>
            <div className="admin-rate-description-box">
              {service.serviceDescription || "No description available for this service yet."}
            </div>
          </div>

          {/* FEEDBACKS */}
          <div className="admin-rate-feedbacks-card">
            <h3>All Feedbacks ({feedbacks.length})</h3>
            {feedbacks.length === 0 ? (
              <p>No feedbacks yet for this service.</p>
            ) : (
              feedbacks.map((fb, index) => (
                <div key={fb.ratingId || index} className="admin-rate-feedback-item">
                  <div className="admin-rate-avatar">
                    {fb.userName?.charAt(0).toUpperCase() || "U"}
                  </div>
                  <div className="admin-rate-feedback-text">
                    <div className="admin-rate-feedback-header">
                      <strong>{fb.userName}</strong>
                      <button 
                        className="admin-rate-delete-feedback-btn"
                        onClick={() => openDeleteModal(fb)}
                        disabled={deletingFeedback}
                      >
                        Delete
                      </button>
                    </div>
                    <div className="admin-rate-stars" style={{ fontSize: "14px" }}>
                      {renderStars(fb.starRate)}
                    </div>
                    <p>{fb.feedbackText || "No feedback provided."}</p>
                    <small>
                      {fb.dateCreated ? new Date(fb.dateCreated).toLocaleDateString() : "Recently"}
                    </small>
                  </div>
                </div>
              ))
            )}
          </div>
        </section>
      </main>

      {/* DELETE CONFIRMATION MODAL */}
      {showDeleteModal && selectedFeedback && (
        <div className="admin-rate-delete-modal-overlay">
          <div className="admin-rate-delete-modal">
            <div className="admin-rate-delete-modal-header">
              <h2>Confirm Delete</h2>
              <button className="admin-rate-delete-modal-close" onClick={closeDeleteModal}>
                ×
              </button>
            </div>
            <div className="admin-rate-delete-modal-body">
              <p>Are you sure you want to delete this feedback?</p>
              <div className="admin-rate-delete-modal-details">
                <p><strong>User:</strong> {selectedFeedback.userName}</p>
                <p><strong>Rating:</strong> {selectedFeedback.starRate} stars</p>
                <p><strong>Feedback:</strong> {selectedFeedback.feedbackText || "No feedback provided"}</p>
              </div>
              <p className="admin-rate-delete-modal-warning">This action cannot be undone!</p>
            </div>
            <div className="admin-rate-delete-modal-footer">
              <button className="admin-rate-delete-modal-cancel" onClick={closeDeleteModal}>
                Cancel
              </button>
              <button className="admin-rate-delete-modal-confirm" onClick={confirmDelete}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* LOGOUT MODAL */}
      {showLogoutModal && (
        <div className="admin-rate-logout-modal-overlay">
          <div className="admin-rate-logout-modal">
            <div className="admin-rate-logout-text">Are you sure you want to logout?</div>
            <div className="admin-rate-logout-buttons">
              <button className="admin-rate-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="admin-rate-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminRateService;