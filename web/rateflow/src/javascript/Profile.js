import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NotificationBell from './NotificationBell';
import "../css/Profile_css.css";

function Profile() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [notificationCount, setNotificationCount] = useState(0);
  const [showNotificationToast, setShowNotificationToast] = useState(false);
  const [latestNotification, setLatestNotification] = useState(null);
  
  // Edit form state
  const [editFormData, setEditFormData] = useState({
    username: "",
    email: ""
  });

  // Fetch notification count
  const fetchNotificationCount = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/notifications", {
        credentials: "include",
      });
      if (response.ok) {
        const data = await response.json();
        setNotificationCount(data.length);
      }
    } catch (error) {
      console.error("Error fetching notification count:", error);
    }
  };

  const showNotification = (message) => {
    setLatestNotification({ message, timestamp: new Date() });
    setShowNotificationToast(true);
    setTimeout(() => {
      setShowNotificationToast(false);
    }, 3000);
    fetchNotificationCount();
  };

  // Check session
  useEffect(() => {
    checkAuth();
    fetchNotificationCount();
    const interval = setInterval(fetchNotificationCount, 30000);
    return () => clearInterval(interval);
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
      setUser(data);
      setEditFormData({
        username: data.username,
        email: data.email
      });
    } catch (error) {
      navigate("/");
    } finally {
      setLoading(false);
    }
  };

  const handleEditProfile = () => {
    setShowEditModal(true);
  };

  const handleEditInputChange = (e) => {
    const { name, value } = e.target;
    setEditFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const submitEditProfile = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/update-profile", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          username: editFormData.username
        }),
      });

      if (response.ok) {
        const updatedUser = await response.json();
        setUser(updatedUser);
        setShowEditModal(false);
        setSuccessMessage("Username updated successfully!");
        setShowSuccessModal(true);
        showNotification(`Username changed to ${editFormData.username}`);
      } else {
        const error = await response.json();
        setErrorMessage(error.error || "Failed to update username");
        setShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      setErrorMessage("Error updating username");
      setShowErrorModal(true);
    }
  };

  const handleLogout = () => setShowLogoutModal(true);

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
      <div className="profile-loading">
        <div className="profile-loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="profile-layout">
      {/* Sidebar */}
      <aside className={`profile-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="profile-sidebar-header">
          <div className="profile-logo">

            {!sidebarCollapsed && <span className="profile-logo-text">Dashboard</span>}
          </div>
        </div>

        <nav className="profile-nav">
          <button 
            className="dashboard-nav-item"
            onClick={() => navigate("/dashboard")}
          >
            {!sidebarCollapsed && <span className="dashboard-nav-label">Services</span>}
          </button>
          <button 
            className="dashboard-nav-item"
            onClick={() => navigate("/my-ratings")}
          >
            {!sidebarCollapsed && <span className="dashboard-nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="profile-sidebar-footer">
          <button className="profile-logout-btn" onClick={handleLogout}>
            {!sidebarCollapsed && <span className="profile-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="profile-main">
        <header className="profile-header">
          <div className="profile-header-content">
            <div>
              <h1 className="profile-page-title">Profile</h1>
              <p className="profile-page-subtitle">Manage your account settings</p>
            </div>
            <div className="profile-header-actions">
              <NotificationBell />
              <div className="profile-avatar" onClick={() => navigate("/profile")}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Profile Card */}
        <section className="profile-card">
          <div className="profile-avatar-large">
            {user?.username?.charAt(0).toUpperCase()}
          </div>

          <div className="profile-info">
            <div className="profile-info-row">
              <span className="profile-label">Username</span>
              <span className="profile-value">{user?.username}</span>
            </div>
            
            <div className="profile-info-row">
              <span className="profile-label">Email</span>
              <span className="profile-value">{user?.email}</span>
            </div>
            
            <div className="profile-info-row">
              <span className="profile-label">Role</span>
              <span className="profile-role-badge admin">
                {user?.role === "ADMIN" ? "Administrator" : "User"}
              </span>
            </div>

            <div className="profile-actions">
              <button className="profile-edit-btn" onClick={handleEditProfile}>
                Edit Username
              </button>
            </div>
          </div>
        </section>
      </main>

      {/* Edit Username Modal */}
      {showEditModal && (
        <div className="profile-modal-overlay">
          <div className="profile-modal">
            <div className="profile-modal-header">
              <h2>Edit Username</h2>
              <button className="profile-modal-close" onClick={() => setShowEditModal(false)}>
                ×
              </button>
            </div>
            <div className="profile-modal-body">
              <div className="profile-form-group">
                <label>Username</label>
                <input
                  type="text"
                  name="username"
                  value={editFormData.username}
                  onChange={handleEditInputChange}
                  className="profile-input-field"
                  placeholder="Enter username"
                />
              </div>
              <div className="profile-form-group">
                <label>Email (Cannot be changed)</label>
                <input
                  type="email"
                  value={editFormData.email}
                  disabled
                  className="profile-input-field disabled"
                />
              </div>
            </div>
            <div className="profile-modal-footer">
              <button className="profile-btn-cancel" onClick={() => setShowEditModal(false)}>
                Cancel
              </button>
              <button className="profile-btn-submit" onClick={submitEditProfile}>
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Notification Toast */}
      {showNotificationToast && latestNotification && (
        <div className="profile-notification-toast">
          <div className="profile-notification-toast-icon">🔔</div>
          <div className="profile-notification-toast-message">{latestNotification.message}</div>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="profile-success-overlay">
          <div className="profile-success-modal">
            <div className="profile-success-icon">✓</div>
            <div className="profile-success-message">{successMessage}</div>
            <button 
              className="profile-success-btn"
              onClick={() => setShowSuccessModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Error Modal */}
      {showErrorModal && (
        <div className="profile-error-overlay">
          <div className="profile-error-modal">
            <div className="profile-error-icon">!</div>
            <div className="profile-error-message">{errorMessage}</div>
            <button 
              className="profile-error-btn"
              onClick={() => setShowErrorModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="profile-logout-overlay">
          <div className="profile-logout-modal">
            <div className="profile-logout-modal-text">Are you sure you want to logout?</div>
            <div className="profile-logout-modal-actions">
              <button className="profile-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="profile-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;