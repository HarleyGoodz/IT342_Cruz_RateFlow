import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/AdminNotificationsStyles.css";

function AdminNotifications() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notifications, setNotifications] = useState([]);
  const [showClearModal, setShowClearModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  // Check authentication and fetch notifications
  useEffect(() => {
    checkAuth();
    fetchNotifications();
  }, []);

  const checkAuth = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/me", {
        credentials: "include",
      });
      
      if (!response.ok) {
        throw new Error("Not authenticated");
      }
      
      const data = await response.json();
      
      if (data.role !== "ADMIN") {
        alert("Access denied. Admin only.");
        navigate("/dashboard");
        return;
      }
      
      setUser(data);
    } catch (error) {
      navigate("/");
    }
  };

  const fetchNotifications = async () => {
    console.log("Fetching notifications...");
    try {
      const response = await fetch("http://localhost:8080/api/notifications", {
        credentials: "include",
      });

      console.log("Response status:", response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log("Notifications data:", data);
        setNotifications(data);
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteNotification = async () => {
    if (!selectedNotification) return;

    try {
      const response = await fetch(`http://localhost:8080/api/notifications/delete/${selectedNotification.id}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        setSuccessMessage("Notification deleted successfully!");
        setShowSuccessModal(true);
        fetchNotifications();
      } else {
        console.error("Failed to delete notification");
      }
    } catch (error) {
      console.error("Error deleting notification:", error);
    } finally {
      setShowDeleteModal(false);
      setSelectedNotification(null);
    }
  };

  const handleClearAllNotifications = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/notifications/clear-all", {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        setSuccessMessage("All notifications cleared!");
        setShowSuccessModal(true);
        fetchNotifications();
      } else {
        console.error("Failed to clear notifications");
      }
    } catch (error) {
      console.error("Error clearing notifications:", error);
    } finally {
      setShowClearModal(false);
    }
  };

  const openDeleteModal = (notification) => {
    setSelectedNotification(notification);
    setShowDeleteModal(true);
  };

  const openClearModal = () => {
    setShowClearModal(true);
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

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return "Just now";
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  };

  const getNotificationIcon = (type) => {
    switch(type) {
      case 'CREATE':
        return '➕';
      case 'UPDATE':
        return '✏️';
      case 'DELETE':
        return '🗑️';
      case 'GRANT_ADMIN':
        return '👑';
      case 'REMOVE_ADMIN':
        return '🔻';
    case 'DELETE_FEEDBACK':  
        return '⭐';
      default:
        return '📢';
    }
  };

  // Handle navigation functions
  const handleServicesNav = () => {
    navigate("/admindashboard");
  };

  const handleCreateServiceNav = () => {
    navigate("/createservice");
  };

  const handleManageServicesNav = () => {
    navigate("/manageservices");
  };

  const handleAccessControlsNav = () => {
    navigate("/access-controls");
  };

  if (loading) {
    return (
      <div className="notif-loading">
        <div className="notif-loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="notif-layout">
      {/* Sidebar */}
      <aside className={`notif-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="notif-sidebar-header">
          <div className="notif-logo">
            
            {!sidebarCollapsed && <span className="notif-logo-text">Admin Dashboard</span>}
          </div>
        </div>

        <nav className="notif-nav">
          <button 
            className="notif-nav-item" 
            onClick={handleServicesNav}
          >
            {!sidebarCollapsed && <span className="notif-nav-label">Services</span>}
          </button>

          <button 
            className="notif-nav-item"
            onClick={handleCreateServiceNav}
          >
            {!sidebarCollapsed && <span className="notif-nav-label">Create Service</span>}
          </button>
          
          <button 
            className="notif-nav-item"
            onClick={handleManageServicesNav}
          >
            {!sidebarCollapsed && <span className="notif-nav-label">Manage Services</span>}
          </button>

          <button 
            className="notif-nav-item"
            onClick={handleAccessControlsNav}
          >
            {!sidebarCollapsed && <span className="notif-nav-label">Access Controls</span>}
          </button>
        </nav>

        <div className="notif-sidebar-footer">
          <button className="notif-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="notif-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="notif-main">
        {/* Header */}
        <header className="notif-header">
          <div className="notif-header-content">
            <div>
              <h1 className="notif-page-title">Notifications</h1>
              <p className="notif-page-subtitle">Track all your admin activities</p>
            </div>
            <div className="notif-header-actions">
              {notifications.length > 0 && (
                <button className="notif-clear-all-btn" onClick={openClearModal}>
                  Clear All
                </button>
              )}

              <button className="admin-notification-btn">
                🔔
              </button>

              <div className="notif-avatar" onClick={() => navigate("/admin-profile")}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Notifications List */}
        <section className="notif-list">
          {notifications.length === 0 ? (
            <div className="notif-empty">
              <div className="notif-empty-icon">🔔</div>
              <p>No notifications yet</p>
              <p className="notif-empty-sub">Your admin activities will appear here</p>
            </div>
          ) : (
            notifications.map((notification) => (
              <div key={notification.id} className="notif-card">
                <div className="notif-icon">
                  {getNotificationIcon(notification.type)}
                </div>
                <div className="notif-content">
                  <div className="notif-message">{notification.message}</div>
                  <div className="notif-time">{formatDate(notification.createdAt)}</div>
                </div>
                <button 
                  className="notif-delete-btn"
                  onClick={() => openDeleteModal(notification)}
                  title="Delete notification"
                >
                  ✕
                </button>
              </div>
            ))
          )}
        </section>
      </main>

      {/* Clear All Confirmation Modal */}
      {showClearModal && (
        <div className="notif-modal-overlay">
          <div className="notif-modal">
            <div className="notif-modal-header">
              <h2>Clear All Notifications</h2>
              <button className="notif-modal-close" onClick={() => setShowClearModal(false)}>
                ×
              </button>
            </div>
            <div className="notif-modal-body">
              <p>Are you sure you want to clear all notifications?</p>
              <p className="notif-modal-warning">This action cannot be undone.</p>
            </div>
            <div className="notif-modal-footer">
              <button className="notif-btn-cancel" onClick={() => setShowClearModal(false)}>
                Cancel
              </button>
              <button className="notif-btn-confirm" onClick={handleClearAllNotifications}>
                Clear All
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Notification Modal */}
      {showDeleteModal && selectedNotification && (
        <div className="notif-modal-overlay">
          <div className="notif-modal">
            <div className="notif-modal-header">
              <h2>Delete Notification</h2>
              <button className="notif-modal-close" onClick={() => setShowDeleteModal(false)}>
                ×
              </button>
            </div>
            <div className="notif-modal-body">
              <p>Are you sure you want to delete this notification?</p>
              <p className="notif-modal-message">"{selectedNotification.message}"</p>
            </div>
            <div className="notif-modal-footer">
              <button className="notif-btn-cancel" onClick={() => setShowDeleteModal(false)}>
                Cancel
              </button>
              <button className="notif-btn-delete" onClick={handleDeleteNotification}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="notif-success-overlay">
          <div className="notif-success-modal">
            <div className="notif-success-icon">✓</div>
            <div className="notif-success-message">{successMessage}</div>
            <button 
              className="notif-success-btn"
              onClick={() => setShowSuccessModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="notif-logout-overlay">
          <div className="notif-logout-modal">
            <div className="notif-logout-modal-text">Are you sure you want to logout?</div>
            <div className="notif-logout-modal-actions">
              <button className="notif-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="notif-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminNotifications;