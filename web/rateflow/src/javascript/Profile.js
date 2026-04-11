import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/Profile_css.css";
import Dashboard from "./Dashboard_js";

function Profile() {
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
        username: data.username || "user",
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


  const handleEditProfile = () => {
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
    <div className="profile-layout">
      {/* Sidebar */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <div className="logo">
            
            {!sidebarCollapsed && <span className="logo-text">Dashboard</span>}
          </div>
          <button
            className="collapse-btn"
            onClick={() =>  navigate("/dashboard")}
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="sidebar-nav">
          <button className="nav-item" onClick={() => navigate("/dashboard")}>
            {!sidebarCollapsed && <span className="nav-label">Services</span>}
          </button>

          <button className="nav-item" onClick={() =>
              navigate("/my-ratings")
            }>
            {!sidebarCollapsed && <span className="nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-sidebar-btn" onClick={handleLogoutClick}>
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="profile-main">
        <header className="profile-header">
          <h1 className="page-title">Profile</h1>

          <div className="header-actions">
            <button
            className="icon-btn"
            onClick={() => navigate("/notifications")}
          >
            🔔
          </button>
            <div className="user-avatar">
              👤
            </div>
          </div>
        </header>

        {/* Profile Card */}
        <section className="profile-card">
          <div className="profile-avatar-large">
            {user?.username?.charAt(0)?.toUpperCase() || "U"}
          </div>

        <div className="profile-info">

        {isEditing ? (
          <input
            type="text"
            value={newUsername}
            onChange={(e) => setNewUsername(e.target.value)}
            className="profile-input"
            maxLength={100}
          />
        ) : (
          <h2>{user?.username || "user"}</h2>
        )}

        {/* EMAIL FIELD — TINTED */}
        <input
          type="text"
          value={user?.email || ""}
          readOnly
          className="profile-input email-tint"
        />

        {isEditing ? (
          <div className="edit-action-buttons">

            <button
              className="confirm-edit-btn"
              onClick={confirmEditProfile}
            >
              Confirm
            </button>

            <button
              className="cancel-edit-btn"
              onClick={cancelEditProfile}
            >
              Cancel
            </button>

          </div>
        ) : (
          <button
            className="edit-profile-btn"
            onClick={handleEditProfile}
          >
            Edit Profile
          </button>
        )}

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

      {/* SUCCESS MODAL */}

      {showSuccessModal && (
        <div className="logout-modal-overlay">

          <div className="logout-modal">

            <div className="logout-text">
              Successfully changed username!
            </div>

            <div className="logout-buttons">

              <button
                className="confirm-btn"
                onClick={() => setShowSuccessModal(false)}
              >
                OK
              </button>

            </div>

          </div>

        </div>
      )}
    </div>

    
  );
}

export default Profile;