import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/RateServiceStyles.css";

function RateService() {
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [rating, setRating] = useState(0);
  const [feedback, setFeedback] = useState("");
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showFeedbackSuccessModal, setShowFeedbackSuccessModal] = useState(false);
  const [feedbackSuccessMessage, setFeedbackSuccessMessage] = useState("");

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
}, [navigate]);

  /* LOGOUT */
  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const confirmLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });

      if (!response.ok) {
        alert("Logout failed");
        return;
      }

      setUser(null);
      setShowLogoutModal(false);
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
      alert("Logout failed");
    }
  };

  const cancelLogout = () => {
    setShowLogoutModal(false);
  };

  /* SUBMIT FEEDBACK */
  const handleSubmit = () => {
    if (rating === 0) {
      alert("Please select a rating.");
      return;
    }

    // In a real app, you would save to backend here
    setFeedbackSuccessMessage("Thank you for your feedback!");
    setShowFeedbackSuccessModal(true);
  };

  const closeFeedbackSuccessModal = () => {
    setShowFeedbackSuccessModal(false);
    setRating(0);
    setFeedback("");
    // Navigate back to dashboard after successful submission
    navigate("/dashboard");
  };

  return (
    <div className="dashboard-layout rate-service-page">
      {/* SIDEBAR */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">⚡</span>
            <span className="logo-text">Dashboard</span>
          </div>
          <button className="collapse-btn" onClick={() => navigate("/dashboard")}>
            ←
          </button>
        </div>

        <nav className="sidebar-nav">
          <button className="nav-item active" onClick={() => navigate("/dashboard")}>
            Services
          </button>
          <button className="nav-item" onClick={() => navigate("/my-ratings")}>
            My Ratings
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-sidebar-btn" onClick={handleLogoutClick}>
            Logout
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
              <button className="notifications-btn" onClick={() => navigate("/notifications")}>
                🔔
              </button>
              <div
                className="user-avatar"
                onClick={() => navigate("/profile")}
                style={{ cursor: "pointer" }}
              >
                👤
              </div>
            </div>
          </div>
        </header>

        {/* RATE CARD */}
        <section className="rate-card">
          <div className="service-image" />
          <div className="rate-info">
            <h2 className="service-name">Service X</h2>
            <p className="service-category">Service Category</p>

            {/* STARS */}
            <div className="stars">
              {[1, 2, 3, 4, 5].map((star) => (
                <span
                  key={star}
                  className={star <= rating ? "star active" : "star"}
                  onClick={() => setRating(star)}
                >
                  ★
                </span>
              ))}
            </div>

            {/* FEEDBACK */}
            <textarea
              placeholder="Write Feedback"
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              className="feedback-input"
            />

            <button className="submit-btn" onClick={handleSubmit}>
              Submit
            </button>
          </div>
        </section>

        {/* LOWER GRID */}
        <section className="lower-grid">
          {/* RATING STATS */}
          <div className="stats-card">
            <h3>Rating Statistics</h3>
            <div className="rating-value">
              ⭐⭐⭐⭐⭐
              <span>4.5</span>
            </div>
          </div>

          {/* DESCRIPTION */}
          <div className="description-card">
            <h3>Description</h3>
            <div className="description-box" />
          </div>

          {/* FEEDBACKS */}
          <div className="feedbacks-card">
            <h3>Feedbacks</h3>
            {[1, 2].map((item) => (
              <div key={item} className="feedback-item">
                <div className="avatar" />
                <div className="feedback-text">Written feedback</div>
              </div>
            ))}
          </div>
        </section>
      </main>

      {/* FEEDBACK SUCCESS MODAL */}
      {showFeedbackSuccessModal && (
        <div className="feedback-success-overlay">
          <div className="feedback-success-modal">
            <div className="feedback-success-icon">✓</div>
            <div className="feedback-success-message">{feedbackSuccessMessage}</div>
            <button className="feedback-success-btn" onClick={() => navigate("/My-Ratings")}>
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
              <button className="confirm-btn" onClick={confirmLogout}>
                Confirm
              </button>
              <button className="cancel-btn" onClick={cancelLogout}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default RateService;