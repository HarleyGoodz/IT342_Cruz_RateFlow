import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/Profile_css.css";

function Profile() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);

  // 🔐 Check session
  useEffect(() => {
    fetch("http://localhost:8080/api/auth/me", {
      credentials: "include",
    })
      .then((res) => {
        if (!res.ok) throw new Error("Not authenticated");
        return res.json();
      })
      .then((data) => setUser(data))
      .catch(() => navigate("/"));
  }, [navigate]);

  const handleLogout = async () => {
    const confirmed = window.confirm("Are you sure you want to logout?");
    if (!confirmed) return;

    try {
      await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
    } finally {
      navigate("/");
    }
  };

  return (
    <div className="profile-layout">
      {/* Sidebar */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">⚡</span>
            {!sidebarCollapsed && <span className="logo-text">Dashboard</span>}
          </div>
          <button
            className="collapse-btn"
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="sidebar-nav">
          <button className="nav-item" onClick={() => navigate("/dashboard")}>
            {!sidebarCollapsed && <span className="nav-label">Services</span>}
          </button>

          <button className="nav-item">
            {!sidebarCollapsed && <span className="nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-sidebar-btn" onClick={handleLogout}>
            🚪 {!sidebarCollapsed && "Logout"}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="profile-main">
        <header className="profile-header">
          <h1 className="page-title">Profile</h1>

          <div className="header-actions">
            <button className="icon-btn">🔔</button>
            <div className="user-avatar">
              {user ? user.username.charAt(0).toUpperCase() : "U"}
            </div>
          </div>
        </header>

        {/* Profile Card */}
        <section className="profile-card">
          <div className="profile-avatar-large">
            {user ? user.username.charAt(0).toUpperCase() : "U"}
          </div>

          <div className="profile-info">
            <h2>{user ? user.username : "Username"}</h2>

            <input
              type="text"
              value={user ? user.email : ""}
              disabled
              className="profile-input"
            />

            <button
              className="edit-profile-btn"
              onClick={() => navigate("/edit-profile")}
            >
              Edit Profile
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}

export default Profile;