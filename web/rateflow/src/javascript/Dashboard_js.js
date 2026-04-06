import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/dashboard_css.css";

function Dashboard() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] =
    useState(false);

  const [activeTab, setActiveTab] =
    useState("Services");

  const [user, setUser] = useState(null);

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
          console.log("Auth event:", event);

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

  /* LOGOUT MODAL FUNCTIONS */

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

  return (
    <div className="dashboard-layout">

      {/* Sidebar */}

      <aside
        className={`sidebar ${
          sidebarCollapsed ? "collapsed" : ""
        }`}
      >
        <div className="sidebar-header">

          <div className="logo">

            {!sidebarCollapsed && (
              <span className="logo-text">
                Dashboard
              </span>
            )}
          </div>

        </div>

        <nav className="sidebar-nav">

          <button
            className={`nav-item ${
              activeTab === "Services"
                ? "active"
                : ""
            }`}
            onClick={() =>
              setActiveTab("Services")
            }
          >
            {!sidebarCollapsed && (
              <span className="nav-label">
                Services
              </span>
            )}
          </button>

          <button
            className={`nav-item ${
              activeTab === "My Ratings"
                ? "active"
                : ""
            }`}
            onClick={() =>
              navigate("/my-ratings")
            }
          >
            {!sidebarCollapsed && (
              <span className="nav-label">
                My Ratings
              </span>
            )}
          </button>

        </nav>

        <div className="sidebar-footer">

          <button
            className="logout-sidebar-btn"
            onClick={handleLogoutClick}
          >

            {!sidebarCollapsed && (
              <span className="nav-label">
                Logout
              </span>
            )}
          </button>

        </div>

      </aside>

      {/* Main Content */}

      <main className="main-content">

        {/* Header */}

        <header className="dashboard-header">

          <div className="header-content">

            <h1 className="page-title">
              Services
            </h1>

            <div className="header-search">

              <input
                type="text"
                placeholder="Search services..."
                className="search-input"
              />

            </div>

            <div className="header-actions">

              <button
                className="notifications-btn"
                onClick={() =>
                  navigate("/notifications")
                }
              >
                🔔
              </button>

              <div
                className="user-avatar"
                onClick={() =>
                  navigate("/profile")
                }
                style={{
                  cursor: "pointer",
                }}
              >
                👤
              </div>

            </div>

          </div>

        </header>

        {/* Filter Section */}

        <section className="filter-section">

          <h3 className="filter-title">
            Filter Category
          </h3>

          <div className="filter-buttons">

            <button className="filter-btn">
              Food & Hospitality
            </button>

            <button className="filter-btn">
              Medical & Health
            </button>

            <button className="filter-btn">
              Retail & Commercial
            </button>

            <button className="filter-btn">
              Personal & Lifestyle
            </button>

          </div>

        </section>

        {/* Services Grid */}

        <section className="services-grid">

          {[1,2,3,4,5,6,7,8,9].map(
            (service) => (

              <div
                key={service}
                className="service-card"
              >

                <div className="service-image" />

                <div className="service-info">

                  <h3 className="service-name">
                    Service X
                  </h3>

                  <p className="service-category">
                    Service Category
                  </p>

                  <button
                    className="rate-btn"
                    onClick={() =>
                      navigate("/rate-service")
                    }
                  >
                    Rate Service
                  </button>

                </div>

              </div>

            )
          )}

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

export default Dashboard;