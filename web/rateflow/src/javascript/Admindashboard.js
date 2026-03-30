import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/admin_dashboard.css";

function AdminDashboard() {
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
          credentials: "include",
        }
      );

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

  return (
    <div className="admin-layout">

      {/* Admin Sidebar */}

      <aside
        className={`admin-sidebar ${
          sidebarCollapsed ? "collapsed" : ""
        }`}
      >
        <div className="admin-sidebar-header">

          <div className="admin-logo">
            <span className="admin-logo-icon">
              🛡️
            </span>

            {!sidebarCollapsed && (
              <span className="admin-logo-text">
                Admin
              </span>
            )}
          </div>

          <button
            className="admin-collapse-btn"
            onClick={() =>
              setSidebarCollapsed(!sidebarCollapsed)
            }
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>

        </div>

        <nav className="admin-nav">

          <button
            className={`admin-nav-item ${
              activeTab === "Services" ? "active" : ""
            }`}
            onClick={() => setActiveTab("Services")}
          >
            {!sidebarCollapsed && (
              <span className="admin-nav-label">
                Services
              </span>
            )}
          </button>

          <button
            className={`admin-nav-item ${
              activeTab === "Create-Service" ? "active" : ""
            }`}
            onClick={() => setActiveTab("Create-Service")}
          >
            {!sidebarCollapsed && (
              <span className="admin-nav-label">
                Create Service
              </span>
            )}
          </button>

          <button
            className={`admin-nav-item ${
              activeTab === "Manage-Services" ? "active" : ""
            }`}
            onClick={() => setActiveTab("Manage-Services")}
          >

            {!sidebarCollapsed && (
              <span className="admin-nav-label">
                Manage Services
              </span>
            )}
          </button>

          <button
            className={`admin-nav-item ${
              activeTab === "Access-Control" ? "active" : ""
            }`}
            onClick={() => setActiveTab("Access-Control")}
          >
            
            {!sidebarCollapsed && (
              <span className="admin-nav-label">
                Access Control
              </span>
            )}
          </button>

        </nav>

        <div className="admin-sidebar-footer">

          <button
            className="admin-logout-btn"
            onClick={handleLogoutClick}
          >
            <span className="admin-nav-icon">🚪</span>
            {!sidebarCollapsed && (
              <span className="admin-nav-label">
                Logout
              </span>
            )}
          </button>

        </div>

      </aside>

      {/* Admin Panel (Main Content) */}

      <main className="admin-panel">

        {/* Admin Topbar */}

        <header className="admin-topbar">

          <div className="admin-topbar-content">

            <h1 className="admin-page-title">
              Services
            </h1>

            <div className="admin-search-wrapper">

              <input
                type="text"
                placeholder="Search services..."
                className="admin-search-input"
              />

            </div>

            <div className="admin-topbar-actions">

              <button
                className="admin-icon-btn"
                onClick={() =>
                  navigate("/admin/notifications")
                }
              >
                🔔
                <span className="admin-notification-badge">
                  3
                </span>
              </button>

              <div
                className="admin-avatar"
                onClick={() =>
                  navigate("/admin/profile")
                }
                style={{ cursor: "pointer" }}
              >
                👤
              </div>

            </div>

          </div>

        </header>

        {/* Filter / Category Bar */}

        <section className="admin-filter-bar">

          <h3 className="admin-filter-label">
            Filter Category
          </h3>

          <div className="admin-filter-group">

            <button className="admin-filter-chip">
              Food & Hospitality
            </button>

            <button className="admin-filter-chip">
              Medical & Health
            </button>

            <button className="admin-filter-chip">
              Retail & Commercial
            </button>

            <button className="admin-filter-chip">
              Personal & Lifestyle
            </button>

          </div>

        </section>

        {/* Admin Records Grid */}

        <section className="admin-records-grid">

          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(
            (record) => (

              <div
                key={record}
                className="admin-record-card"
              >

                <div className="admin-record-thumbnail" />

                <div className="admin-record-details">

                  <h3 className="admin-record-name">
                    Service {record}
                  </h3>

                  <p className="admin-record-category">
                    Service Category
                  </p>

                  <button
                    className="admin-record-action-btn"
                    onClick={() =>
                      navigate("/admin/manage-service")
                    }
                  >
                    Manage
                  </button>

                </div>

              </div>

            )
          )}

        </section>

      </main>

      {/* Logout Confirm Modal */}

      {showLogoutModal && (
        <div className="admin-logout-overlay">

          <div className="admin-logout-modal">

            <div className="admin-logout-modal-text">
              Are you sure you want to logout?
            </div>

            <div className="admin-logout-modal-actions">

              <button
                className="admin-confirm-btn"
                onClick={confirmLogout}
              >
                Confirm
              </button>

              <button
                className="admin-cancel-btn"
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

export default AdminDashboard;