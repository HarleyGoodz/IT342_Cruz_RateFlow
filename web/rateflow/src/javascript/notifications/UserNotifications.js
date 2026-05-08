import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../css/notifications/UserNotificationsStyles.css";

function UserNotifications() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notifications, setNotifications] = useState([]);
  const [totalCount, setTotalCount] = useState(0); // Changed from unreadCount
  const [showClearModal, setShowClearModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  // Check authentication
  useEffect(() => {
    checkAuth();
  }, []);

  // Poll for new notifications
  useEffect(() => {
    if (user && user.role !== "ADMIN") {
      fetchNotifications();
      fetchNotificationCount();
      
      const interval = setInterval(() => {
        fetchNotifications();
        fetchNotificationCount();
      }, 30000);
      
      return () => clearInterval(interval);
    }
  }, [user]);

  const checkAuth = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/me", {
        credentials: "include",
      });
      
      if (!response.ok) {
        throw new Error("Not authenticated");
      }
      
      const data = await response.json();
      setUser(data);
      
      if (data.role === "ADMIN") {
        navigate("/admin-notifications");
        return;
      }
      
      await fetchNotifications();
      await fetchNotificationCount();
    } catch (error) {
      navigate("/");
    } finally {
      setLoading(false);
    }
  };

  // Fetch all notifications
  const fetchNotifications = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/user-notifications", {
        credentials: "include",
      });

      if (response.ok) {
        const data = await response.json();
        setNotifications(data);
      } else if (response.status === 401) {
        navigate("/");
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    }
  };

  // Count total notifications (simple count)
  const fetchNotificationCount = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/user-notifications", {
        credentials: "include",
      });

      if (response.ok) {
        const data = await response.json();
        setTotalCount(data.length); // Just count the array length
      }
    } catch (error) {
      console.error("Error fetching notification count:", error);
    }
  };

  // Delete a single notification
  const handleDeleteNotification = async () => {
    if (!selectedNotification) return;

    try {
      const response = await fetch(`http://localhost:8080/api/user-notifications/delete/${selectedNotification.id}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        const updatedNotifications = notifications.filter(
          n => n.id !== selectedNotification.id
        );
        setNotifications(updatedNotifications);
        setTotalCount(updatedNotifications.length); // Update count
        setSuccessMessage("Notification deleted successfully!");
        setShowSuccessModal(true);
      }
    } catch (error) {
      console.error("Error deleting notification:", error);
    } finally {
      setShowDeleteModal(false);
      setSelectedNotification(null);
    }
  };

  // Clear all notifications
  const handleClearAllNotifications = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/user-notifications/clear-all", {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        setNotifications([]);
        setTotalCount(0);
        setSuccessMessage("All notifications cleared!");
        setShowSuccessModal(true);
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
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    return date.toLocaleDateString();
  };

  const getNotificationIcon = (type) => {
    switch(type) {
      case 'SERVICE_UPDATED':
        return '🔄';
      case 'SERVICE_DELETED':
        return '❌';
      case 'USERNAME_CHANGE':
        return '✏️';
      case 'SERVICE_RATING':
        return '⭐';
      case 'FEEDBACK_DELETED':
        return '🗑️';
      case 'ROLE_GRANTED':
        return '👑';
      case 'ROLE_DEMOTED':
        return '🔻';
      default:
        return '📢';
    }
  };

  const getNotificationColor = (type) => {
    switch(type) {
      case 'USERNAME_CHANGE':
        return '#38bdf8';
      case 'SERVICE_RATING':
        return '#fbbf24';
      case 'FEEDBACK_DELETED':
        return '#ef4444';
      case 'ROLE_GRANTED':
        return '#10b981';
      case 'ROLE_DEMOTED':
        return '#f97316';
      default:
        return '#94a3b8';
    }
  };

  const handleServicesNav = () => {
    navigate("/dashboard");
  };

  const handleMyRatingsNav = () => {
    navigate("/my-ratings");
  };

  const handleProfileNav = () => {
    navigate("/profile");
  };

  if (loading) {
    return (
      <div className="usernotif-loading">
        <div className="usernotif-loading-spinner"></div>
        <p>Loading notifications...</p>
      </div>
    );
  }

  return (
    <div className="usernotif-layout">
      {/* Sidebar */}
      <aside className={`usernotif-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="usernotif-sidebar-header">
          <div className="usernotif-logo">
            {!sidebarCollapsed && <span className="usernotif-logo-text">Dashboard</span>}

            <button
      className="dashboard-back-btn"
      onClick={() => navigate(-1)}
    >
      ←
    </button>
          </div>
        </div>

        <nav className="usernotif-nav">
          <button className="usernotif-nav-item" onClick={handleServicesNav}>
            {!sidebarCollapsed && <span className="usernotif-nav-label">Services</span>}
          </button>

          <button className="usernotif-nav-item" onClick={handleMyRatingsNav}>
            {!sidebarCollapsed && <span className="usernotif-nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="usernotif-sidebar-footer">
          <button className="usernotif-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="usernotif-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="usernotif-main">
        {/* Header */}
        <header className="usernotif-header">
          <div className="usernotif-header-content">
            <div>
              <h1 className="usernotif-page-title">Notifications</h1>
              <p className="usernotif-page-subtitle">Stay updated with your activity history</p>
            </div>
            <div className="usernotif-header-actions">
              <div className="usernotif-notification-bell">
                <button className="usernotif-bell-btn">
                  🔔
                  {totalCount > 0 && (
                    <span className="usernotif-badge">{totalCount}</span>
                  )}
                </button>
              </div>

              <div className="usernotif-avatar" onClick={handleProfileNav}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Notifications Actions Bar */}
        <div className="usernotif-actions-bar">
          {notifications.length > 0 && (
            <div className="usernotif-actions-group">
              <button className="usernotif-clear-all-btn" onClick={openClearModal}>
                Clear all
              </button>
            </div>
          )}
        </div>

        {/* Notifications List */}
        <section className="usernotif-list">
          {notifications.length === 0 ? (
            <div className="usernotif-empty">
              <div className="usernotif-empty-icon">🔔</div>
              <p>No notifications yet</p>
              <p className="usernotif-empty-sub">Your activity notifications will appear here</p>
            </div>
          ) : (
            notifications.map((notification) => (
              <div key={notification.id} className="usernotif-card">
                <div className="usernotif-icon" style={{ backgroundColor: `${getNotificationColor(notification.type)}20` }}>
                  <span style={{ color: getNotificationColor(notification.type) }}>
                    {getNotificationIcon(notification.type)}
                  </span>
                </div>
                <div className="usernotif-content">
                  <div className="usernotif-message">{notification.message}</div>
                  <div className="usernotif-time">{formatDate(notification.createdAt)}</div>
                  {notification.details && (
                    <div className="usernotif-details">{notification.details}</div>
                  )}
                </div>
                <button 
                  className="usernotif-delete-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    openDeleteModal(notification);
                  }}
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
        <div className="usernotif-modal-overlay">
          <div className="usernotif-modal">
            <div className="usernotif-modal-header">
              <h2>Clear All Notifications</h2>
              <button className="usernotif-modal-close" onClick={() => setShowClearModal(false)}>
                ×
              </button>
            </div>
            <div className="usernotif-modal-body">
              <p>Are you sure you want to clear all notifications?</p>
              <p className="usernotif-modal-warning">This action cannot be undone.</p>
            </div>
            <div className="usernotif-modal-footer">
              <button className="usernotif-btn-cancel" onClick={() => setShowClearModal(false)}>
                Cancel
              </button>
              <button className="usernotif-btn-confirm" onClick={handleClearAllNotifications}>
                Clear All
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Notification Modal */}
      {showDeleteModal && selectedNotification && (
        <div className="usernotif-modal-overlay">
          <div className="usernotif-modal">
            <div className="usernotif-modal-header">
              <h2>Delete Notification</h2>
              <button className="usernotif-modal-close" onClick={() => setShowDeleteModal(false)}>
                ×
              </button>
            </div>
            <div className="usernotif-modal-body">
              <p>Are you sure you want to delete this notification?</p>
              <p className="usernotif-modal-message">"{selectedNotification.message}"</p>
            </div>
            <div className="usernotif-modal-footer">
              <button className="usernotif-btn-cancel" onClick={() => setShowDeleteModal(false)}>
                Cancel
              </button>
              <button className="usernotif-btn-delete" onClick={handleDeleteNotification}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="usernotif-success-overlay">
          <div className="usernotif-success-modal">
            <div className="usernotif-success-icon">✓</div>
            <div className="usernotif-success-message">{successMessage}</div>
            <button 
              className="usernotif-success-btn"
              onClick={() => setShowSuccessModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="usernotif-logout-overlay">
          <div className="usernotif-logout-modal">
            <div className="usernotif-logout-modal-text">Are you sure you want to logout?</div>
            <div className="usernotif-logout-modal-actions">
              <button className="usernotif-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="usernotif-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default UserNotifications;