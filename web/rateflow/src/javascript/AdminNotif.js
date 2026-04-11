import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/AdminNotifStyles.css";

function AdminNotif() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  const [notifications, setNotifications] = useState([
    {
      id: 1,
      message: "New service 'Gourmet Bistro' was created",
      time: "2 minutes ago",
      type: "service"
    },
    {
      id: 2,
      message: "User 'John Doe' was granted admin access",
      time: "1 hour ago",
      type: "access"
    },
    {
      id: 3,
      message: "Service 'Wellness Center' was updated",
      time: "3 hours ago",
      type: "service"
    },
    {
      id: 4,
      message: "New user registration: jane.smith@example.com",
      time: "5 hours ago",
      type: "user"
    },
    {
      id: 5,
      message: "Service 'Urban Mart' was deleted",
      time: "1 day ago",
      type: "service"
    },
  ]);

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
}, []);

  /* LOGOUT FUNCTIONS */
  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const confirmLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include"
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

  /* DELETE ONE NOTIFICATION */
  const deleteNotification = (id) => {
    setNotifications(notifications.filter((n) => n.id !== id));
    setSuccessMessage("Notification removed");
    setShowSuccessModal(true);
  };

  /* CLEAR ALL NOTIFICATIONS */
  const clearAll = () => {
    setNotifications([]);
    setSuccessMessage("All notifications cleared");
    setShowSuccessModal(true);
  };

  /* MARK AS READ (for individual notification) */
  const markAsRead = (id) => {
    // In a real app, you would update the backend
    setSuccessMessage("Notification marked as read");
    setShowSuccessModal(true);
  };

  return (
    <div className="an-layout">
      {/* SIDEBAR */}
      <aside className={`an-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="an-sidebar-header">
          <div className="an-logo">
            <span className="an-logo-icon">🛡️</span>
            {!sidebarCollapsed && <span className="an-logo-text">Admin</span>}
          </div>
          <button className="an-collapse-btn" onClick={() => navigate("/admin")}>
            {sidebarCollapsed ? "→" : "←"}
          </button>
        </div>

        <nav className="an-sidebar-nav">
          <button className="an-nav-item" onClick={() => navigate("/admin")}>
            {!sidebarCollapsed && <span className="an-nav-label">Services</span>}
          </button>
          <button className="an-nav-item" onClick={() => navigate("/admin/create-service")}>
            {!sidebarCollapsed && <span className="an-nav-label">Create Service</span>}
          </button>
          <button className="an-nav-item" onClick={() => navigate("/admin/manage-services")}>
            {!sidebarCollapsed && <span className="an-nav-label">Manage Services</span>}
          </button>
          <button className="an-nav-item" onClick={() => navigate("/admin/access-control")}>
            {!sidebarCollapsed && <span className="an-nav-label">Access Control</span>}
          </button>
        </nav>

        <div className="an-sidebar-footer">
          <button className="an-logout-sidebar-btn" onClick={handleLogoutClick}>
            Logout
          </button>
        </div>
      </aside>

      {/* MAIN CONTENT */}
      <main className="an-main-content">
        {/* HEADER */}
        <header className="an-header">
          <div className="an-header-content">
            <div className="an-notification-header-left">
              <h1 className="an-page-title">Notifications</h1>
            </div>
            <div className="an-header-actions">
              <button className="an-icon-btn" onClick={() => navigate("/admin/notifications")}>
                🔔
                <span className="an-notification-badge">{notifications.length}</span>
              </button>
              <div className="an-user-avatar" onClick={() => navigate("/admin/profile")}>
                👤
              </div>
            </div>
          </div>
        </header>

        {/* NOTIFICATIONS CONTENT */}
        <section className="an-notification-container">
          <div className="an-notification-top">
            {notifications.length > 0 && (
              <button className="an-clear-btn" onClick={clearAll}>
                Clear All
              </button>
            )}
          </div>

          {notifications.length === 0 ? (
            <div className="an-empty-state">
              <div className="an-empty-icon">🔔</div>
              <div className="an-empty-text">No notifications yet</div>
              <div className="an-empty-subtext">When you have notifications, they will appear here</div>
            </div>
          ) : (
            notifications.map((notification) => (
              <div key={notification.id} className="an-notification-card">
                <div className="an-notification-icon">
                  {notification.type === "service" && "📦"}
                  {notification.type === "access" && "👑"}
                  {notification.type === "user" && "👤"}
                  {!notification.type && "!"}
                </div>
                <div className="an-notification-content">
                  <div className="an-notification-text">{notification.message}</div>
                  <div className="an-notification-time">{notification.time}</div>
                </div>
                <div className="an-notification-actions">
                  <button className="an-delete-btn" onClick={() => deleteNotification(notification.id)}>
                    🗑
                  </button>
                </div>
              </div>
            ))
          )}
        </section>
      </main>

      {/* LOGOUT MODAL */}
      {showLogoutModal && (
        <div className="an-logout-overlay">
          <div className="an-logout-modal">
            <div className="an-logout-modal-text">
              Are you sure you want to logout?
            </div>
            <div className="an-logout-modal-actions">
              <button className="an-logout-confirm-btn" onClick={confirmLogout}>
                Confirm
              </button>
              <button className="an-logout-cancel-btn" onClick={cancelLogout}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* SUCCESS MODAL */}
      {showSuccessModal && (
        <div className="an-success-overlay">
          <div className="an-success-modal">
            <div className="an-success-icon">✓</div>
            <div className="an-success-message">{successMessage}</div>
            <button className="an-success-btn" onClick={() => setShowSuccessModal(false)}>
              Continue
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminNotif;