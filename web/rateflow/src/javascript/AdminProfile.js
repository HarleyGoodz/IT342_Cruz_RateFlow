import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

import "../css/AdminProfileStyles.css";

function AdminProfile() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [newUsername, setNewUsername] = useState("");

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
      username: data.username || "Admin",
      email: data.email
    });

    console.log("Loaded from HTTP session");

  } catch (err) {
    console.error("Session check failed:", err);
    navigate("/login");
  }
};

    loadUser();

    

    

    return () => {
      isMounted = false;
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

  const handleEditProfile = () => {
    setNewUsername(user?.username || "");
    setIsEditing(true);
  };

  const confirmEditProfile = async () => {
    if (!newUsername.trim()) {
      alert("Username cannot be empty");
      return;
    }

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/update-username",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          credentials: "include",
          body: JSON.stringify({
            username: newUsername
          })
        }
      );

      if (!response.ok) {
        alert("Failed to update username");
        return;
      }

      const data = await response.json();

      setUser({
        username: data.username,
        email: data.email
      });

      setIsEditing(false);
      setShowSuccessModal(true);
    } catch (error) {
      console.error(error);
      alert("Failed to update username");
    }
  };

  const cancelEditProfile = () => {
    setNewUsername(user ? user.username : "");
    setIsEditing(false);
  };

  return (
    <div className="ap-layout">
      {/* Sidebar */}
      <aside className={`ap-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="ap-sidebar-header">
          <div className="ap-logo">
            <span className="ap-logo-icon">🛡️</span>
            {!sidebarCollapsed && <span className="ap-logo-text">Admin</span>}
          </div>
          <button
            className="ap-collapse-btn"
            onClick={() => navigate("/admin")}
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="ap-sidebar-nav">
          <button className="ap-nav-item" onClick={() => navigate("/admin")}>
            {!sidebarCollapsed && <span className="ap-nav-label">Services</span>}
          </button>

          <button className="ap-nav-item" onClick={() => navigate("/admin/create-service")}>
            {!sidebarCollapsed && <span className="ap-nav-label">Create Service</span>}
          </button>

          <button className="ap-nav-item" onClick={() => navigate("/admin/manage-services")}>
            {!sidebarCollapsed && <span className="ap-nav-label">Manage Services</span>}
          </button>

          <button className="ap-nav-item" onClick={() => navigate("/admin/access-control")}>
            {!sidebarCollapsed && <span className="ap-nav-label">Access Control</span>}
          </button>
        </nav>

        <div className="ap-sidebar-footer">
          <button className="ap-logout-sidebar-btn" onClick={handleLogoutClick}>
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ap-main">
        <header className="ap-header">
          <h1 className="ap-page-title">Profile</h1>

          <div className="ap-header-actions">
            <button
              className="ap-icon-btn"
              onClick={() => navigate("/admin/notifications")}
            >
              🔔
              <span className="ap-notification-badge">3</span>
            </button>
            <div className="ap-user-avatar">
              👤
            </div>
          </div>
        </header>

        {/* Profile Card */}
        <section className="ap-profile-card">
          <div className="ap-profile-avatar-large">
            {user?.username?.charAt(0)?.toUpperCase() || "A"}
          </div>

          <div className="ap-profile-info">
            {isEditing ? (
              <input
                type="text"
                value={newUsername}
                onChange={(e) => setNewUsername(e.target.value)}
                className="ap-profile-input"
                maxLength={100}
              />
            ) : (
              <h2>{user?.username || "Admin"}</h2>
            )}

            {/* EMAIL FIELD — TINTED */}
            <input
              type="text"
              value={user?.email || ""}
              readOnly
              className="ap-profile-input ap-email-tint"
            />

            {isEditing ? (
              <div className="ap-edit-action-buttons">
                <button
                  className="ap-confirm-edit-btn"
                  onClick={confirmEditProfile}
                >
                  Confirm
                </button>
                <button
                  className="ap-cancel-edit-btn"
                  onClick={cancelEditProfile}
                >
                  Cancel
                </button>
              </div>
            ) : (
              <button
                className="ap-edit-profile-btn"
                onClick={handleEditProfile}
              >
                Edit Profile
              </button>
            )}
          </div>
        </section>
      </main>

      {/* LOGOUT MODAL - Styled like CreateService */}
      {showLogoutModal && (
        <div className="ap-logout-overlay">
          <div className="ap-logout-modal">
            <div className="ap-logout-modal-text">
              Are you sure you want to logout?
            </div>
            <div className="ap-logout-modal-actions">
              <button className="ap-logout-confirm-btn" onClick={confirmLogout}>
                Confirm
              </button>
              <button className="ap-logout-cancel-btn" onClick={cancelLogout}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* SUCCESS MODAL */}
      {showSuccessModal && (
        <div className="ap-success-overlay">
          <div className="ap-success-modal">
            <div className="ap-success-icon">✓</div>
            <div className="ap-success-message">
              Successfully changed username!
            </div>
            <button
              className="ap-success-btn"
              onClick={() => setShowSuccessModal(false)}
            >
              Continue
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminProfile;