import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/dashboard_css.css";

function Dashboard() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Services");
  const [user, setUser] = useState(null);

  // 🔐 Check session when dashboard loads
  useEffect(() => {
    fetch("http://localhost:8080/api/auth/me", {
      credentials: "include",
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("Not authenticated");
        }
        return res.json();
      })
      .then((data) => {
        setUser(data);
      })
      .catch(() => {
        navigate("/");
      });
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

  const stats = [
    { label: "Total Revenue", value: "$45,231", change: "+20.1%", positive: true, icon: "💰" },
    { label: "Active Users", value: "2,543", change: "+12.5%", positive: true, icon: "👥" },
    { label: "Projects", value: "42", change: "-3.2%", positive: false, icon: "📊" },
    { label: "Tasks Completed", value: "156", change: "+8.4%", positive: true, icon: "✓" },
  ];

  const recentActivities = [
    { action: "New project created", time: "2 minutes ago", icon: "📁" },
    { action: "Report generated", time: "1 hour ago", icon: "📄" },
    { action: "Team member added", time: "3 hours ago", icon: "👤" },
    { action: "Payment processed", time: "5 hours ago", icon: "💳" },
  ];

  return (
    <div className="dashboard-layout">
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
            aria-label="Toggle sidebar"
          >
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="sidebar-nav">
          <button 
            className={`nav-item ${activeTab === "Services" ? "active" : ""}`}
            onClick={() => setActiveTab("Services")}
          >
            {!sidebarCollapsed && <span className="nav-label">Services</span>}
          </button>
          <button 
            className={`nav-item ${activeTab === "My Ratings" ? "active" : ""}`}
            onClick={() => setActiveTab("My Ratings")}
          >
  
            {!sidebarCollapsed && <span className="nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-sidebar-btn" onClick={handleLogout}>
            <span className="nav-icon">🚪</span>
            {!sidebarCollapsed && <span className="nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="dashboard-header">
          <div className="header-content">
            <div>
              <h1 className="page-title">
                Welcome back, {user ? user.username : "User"}
              </h1>
              <p className="page-subtitle">
                Here's what's happening with your projects today
              </p>
            </div>
            <div className="header-actions">
              <button className="icon-btn notification-btn">
                <span className="notification-badge">3</span>
                🔔
              </button>
              <div className="user-avatar"
                onClick={() => navigate("/profile")}
                style={{ cursor: "pointer" }}
                >
                  {user ? user.username.charAt(0).toUpperCase() : "U"}
                </div>
            </div>
          </div>
        </header>

        <section className="stats-grid">
          {stats.map((stat, index) => (
            <div key={index} className="stat-card">
              <div className="stat-icon">{stat.icon}</div>
              <div className="stat-content">
                <p className="stat-label">{stat.label}</p>
                <h3 className="stat-value">{stat.value}</h3>
                <p className={`stat-change ${stat.positive ? "positive" : "negative"}`}>
                  {stat.change} from last month
                </p>
              </div>
            </div>
          ))}
        </section>

        <section className="content-grid">
          <div className="chart-card">
            <div className="card-header">
              <h3 className="card-title">Revenue Services</h3>
              <select className="time-selector">
                <option>Last 7 days</option>
                <option>Last 30 days</option>
                <option>Last 90 days</option>
              </select>
            </div>
            <div className="chart-placeholder"></div>
          </div>

          <div className="activity-card">
            <div className="card-header">
              <h3 className="card-title">Recent Activity</h3>
              <button className="view-all-btn">View All →</button>
            </div>
            <div className="activity-list">
              {recentActivities.map((activity, index) => (
                <div key={index} className="activity-item">
                  <div className="activity-icon">{activity.icon}</div>
                  <div className="activity-content">
                    <p className="activity-action">{activity.action}</p>
                    <p className="activity-time">{activity.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default Dashboard;
