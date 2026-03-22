import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/RateServiceStyles.css";

function RateService() {
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [rating, setRating] = useState(0);
  const [feedback, setFeedback] = useState("");
  const [showLogoutModal, setShowLogoutModal] =
    useState(false);

  useEffect(() => {
    let isMounted = true;

    const init = async () => {
      const {
        data: { session },
      } = await supabase.auth.getSession();

      if (!isMounted) return;

      if (session) {
        setUser({
          username:
            session.user.user_metadata
              ?.full_name ||
            session.user.email,
        });
      }
    };

    init();

    const { data: listener } =
      supabase.auth.onAuthStateChange(
        (event, session) => {
          if (event === "SIGNED_OUT") {
            navigate("/");
          }

          if (session) {
            setUser({
              username:
                session.user.user_metadata
                  ?.full_name ||
                session.user.email,
            });
          }
        }
      );

    return () => {
      isMounted = false;
      listener.subscription.unsubscribe();
    };
  }, [navigate]);

  /* LOGOUT */

  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const confirmLogout = async () => {
  try {
    const response = await fetch(
      "http://localhost:8080/api/auth/logout",
      {
        method: "POST",
        credentials: "include"
      }
    );

    if (!response.ok) {
      alert("Logout failed");
      return;
    }

    // Clear local state
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

    alert("Feedback submitted!");
    setRating(0);
    setFeedback("");
  };

  return (
    <div className="dashboard-layout rate-service-page">

      {/* SIDEBAR */}

      <aside className="sidebar">

        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">
              ⚡
            </span>
            <span className="logo-text">
              Dashboard
            </span>
          </div>

          <button
            className="collapse-btn"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            ←
          </button>
        </div>
        

        <nav className="sidebar-nav">

          <button
            className="nav-item active"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            Services
          </button>

          <button
            className="nav-item"
            onClick={() =>
              navigate("/my-ratings")
            }
          >
            My Ratings
          </button>

        </nav>

        <div className="sidebar-footer">
          <button
            className="logout-sidebar-btn"
            onClick={handleLogoutClick}
          >
            Logout
          </button>
        </div>

      </aside>

      {/* MAIN */}

      <main className="main-content">

        {/* HEADER */}

        <header className="dashboard-header">

  <div className="header-content">

    {/* LEFT SIDE */}

    <div className="header-left">
      <h1 className="page-title">
        Rate Service
      </h1>

    </div>

    {/* RIGHT SIDE */}

    <div className="header-actions">

      <button
        className="notifications-btn"
        onClick={() => navigate("/notifications")}
      >
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

            <h2 className="service-name">
              Service X
            </h2>

            <p className="service-category">
              Service Category
            </p>

            {/* STARS */}

            <div className="stars">

              {[1, 2, 3, 4, 5].map((star) => (

                <span
                  key={star}
                  className={
                    star <= rating
                      ? "star active"
                      : "star"
                  }
                  onClick={() =>
                    setRating(star)
                  }
                >
                  ★
                </span>

              ))}

            </div>

            {/* FEEDBACK */}

            <textarea
              placeholder="Write Feedback"
              value={feedback}
              onChange={(e) =>
                setFeedback(
                  e.target.value
                )
              }
              className="feedback-input"
            />

            <button
              className="submit-btn"
              onClick={handleSubmit}
            >
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

              <div
                key={item}
                className="feedback-item"
              >

                <div className="avatar" />

                <div className="feedback-text">
                  Written feedback
                </div>

              </div>

            ))}

          </div>

        </section>

      </main>

      {/* LOGOUT MODAL */}

      {showLogoutModal && (
        <div className="logout-modal-overlay">

          <div className="logout-modal">

            <div className="logout-text">
              Are you sure you want to logout?
            </div>

            <div className="logout-buttons">

              <button
                className="confirm-btn"
                onClick={confirmLogout}
              >
                Confirm
              </button>

              <button
                className="cancel-btn"
                onClick={cancelLogout}
              >
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