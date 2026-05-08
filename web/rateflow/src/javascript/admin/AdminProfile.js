import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../css/AdminProfileStyles.css";

function AdminProfile() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showChangePasswordModal, setShowChangePasswordModal] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [notificationCount, setNotificationCount] = useState(0);
const [showNotificationToast, setShowNotificationToast] = useState(false);
const [latestNotification, setLatestNotification] = useState(null);

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
  
  // Edit profile form state
  const [editFormData, setEditFormData] = useState({
    username: "",
    email: ""
  });
  
  // Change password form state
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  });

  // Check authentication
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

  const handlePasswordInputChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
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
          username: editFormData.username,
          
        }),
      });

      if (response.ok) {
        const updatedUser = await response.json();
        setUser(updatedUser);
        setShowEditModal(false);
        setSuccessMessage("Profile updated successfully!");
        setShowSuccessModal(true);
        showNotification(`Username changed to ${editFormData.username}`);
      } else {
        const error = await response.json();
        setErrorMessage(error.error || "Failed to update profile");
        setShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      setErrorMessage("Error updating profile");
      setShowErrorModal(true);
    }
  };

  const submitChangePassword = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setErrorMessage("New passwords do not match");
      setShowErrorModal(true);
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setErrorMessage("Password must be at least 6 characters");
      setShowErrorModal(true);
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/auth/change-password", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword
        }),
      });

      if (response.ok) {
        setShowChangePasswordModal(false);
        setPasswordData({
          currentPassword: "",
          newPassword: "",
          confirmPassword: ""
        });
        setSuccessMessage("Password changed successfully!");
        setShowSuccessModal(true);
      } else {
        const error = await response.json();
        setErrorMessage(error.error || "Failed to change password");
        setShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error changing password:", error);
      setErrorMessage("Error changing password");
      setShowErrorModal(true);
    }
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
      <div className="admin-profile-loading">
        <div className="admin-profile-loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="admin-profile-layout">
      {/* Sidebar */}
      <aside className={`admin-profile-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="admin-profile-sidebar-header">
          <div className="admin-profile-logo">
            
            {!sidebarCollapsed && <span className="admin-profile-logo-text">Admin Dashboard</span>}
          </div>
        </div>

        <nav className="admin-profile-nav">
          <button 
            className="admin-profile-nav-item" 
            onClick={handleServicesNav}
          >
            {!sidebarCollapsed && <span className="admin-profile-nav-label">Services</span>}
          </button>

          <button 
            className="admin-profile-nav-item"
            onClick={handleCreateServiceNav}
          >
            {!sidebarCollapsed && <span className="admin-profile-nav-label">Create Service</span>}
          </button>
          
          <button 
            className="admin-profile-nav-item"
            onClick={handleManageServicesNav}
          >
            {!sidebarCollapsed && <span className="admin-profile-nav-label">Manage Services</span>}
          </button>

          <button 
            className="admin-profile-nav-item"
            onClick={handleAccessControlsNav}
          >
            {!sidebarCollapsed && <span className="admin-profile-nav-label">Access Controls</span>}
          </button>
        </nav>

        <div className="admin-profile-sidebar-footer">
          <button className="admin-profile-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="admin-profile-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="admin-profile-main">
        {/* Header */}
        <header className="admin-profile-header">
          <div className="admin-profile-header-content">
            <div>
              <h1 className="admin-profile-page-title">Profile</h1>
              <p className="admin-profile-page-subtitle">Manage your account settings</p>
            </div>
            <div className="admin-profile-header-actions">
                <button className="admin-notification-btn" onClick={() => navigate("/admin-notifications")} style={{ position: "relative" }}>
                🔔
                {notificationCount > 0 && (
                    <span className="adminprofile-notification-badge">{notificationCount}</span>
                )}
                </button>
              <div className="admin-profile-avatar" onClick={() => navigate("/profile")}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Profile Card */}
        <section className="admin-profile-card">
          <div className="admin-profile-avatar-large">
            {user?.username?.charAt(0).toUpperCase()}
          </div>

          <div className="admin-profile-info">
            <div className="admin-profile-info-row">
              <span className="admin-profile-label">Username</span>
              <span className="admin-profile-value">{user?.username}</span>
            </div>
            
            <div className="admin-profile-info-row">
              <span className="admin-profile-label">Email</span>
              <span className="admin-profile-value">{user?.email}</span>
            </div>
            
            <div className="admin-profile-info-row">
              <span className="admin-profile-label">Role</span>
              <span className="admin-profile-role-badge admin">
                {user?.role === "ADMIN" ? "Administrator" : "User"}
              </span>
            </div>

            <div className="admin-profile-actions">
                <button className="admin-profile-edit-btn" onClick={handleEditProfile}>
                Edit Username
            </button>
              
            </div>
          </div>
        </section>
      </main>

      {showNotificationToast && latestNotification && (
  <div className="adminprofile-notification-toast">
    <div className="adminprofile-notification-toast-icon">🔔</div>
    <div className="adminprofile-notification-toast-message">{latestNotification.message}</div>
  </div>
)}

      {/* Edit Profile Modal */}
      {showEditModal && (
        <div className="admin-profile-modal-overlay">
          <div className="admin-profile-modal">
            <div className="admin-profile-modal-header">
              <h2>Edit Profile</h2>
              <button className="admin-profile-modal-close" onClick={() => setShowEditModal(false)}>
                ×
              </button>
            </div>
            <div className="admin-profile-modal-body">
              <div className="admin-profile-form-group">
                <label>Username</label>
                <input
                  type="text"
                  name="username"
                  value={editFormData.username}
                  onChange={handleEditInputChange}
                  className="admin-profile-input"
                  placeholder="Enter username"
                />
              </div>
              <div className="admin-profile-form-group">
                <label>Email</label>
                <input
                  type="email"
                  name="email"
                  value={editFormData.email}
                  className="admin-profile-input"
                  disabled
                />
              </div>
            </div>
            <div className="admin-profile-modal-footer">
              <button className="admin-profile-btn-cancel" onClick={() => setShowEditModal(false)}>
                Cancel
              </button>
              <button className="admin-profile-btn-submit" onClick={submitEditProfile}>
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Change Password Modal */}
      {showChangePasswordModal && (
        <div className="admin-profile-modal-overlay">
          <div className="admin-profile-modal">
            <div className="admin-profile-modal-header">
              <h2>Change Password</h2>
              <button className="admin-profile-modal-close" onClick={() => setShowChangePasswordModal(false)}>
                ×
              </button>
            </div>
            <div className="admin-profile-modal-body">
              <div className="admin-profile-form-group">
                <label>Current Password</label>
                <input
                  type="password"
                  name="currentPassword"
                  value={passwordData.currentPassword}
                  onChange={handlePasswordInputChange}
                  className="admin-profile-input"
                  placeholder="Enter current password"
                />
              </div>
              <div className="admin-profile-form-group">
                <label>New Password</label>
                <input
                  type="password"
                  name="newPassword"
                  value={passwordData.newPassword}
                  onChange={handlePasswordInputChange}
                  className="admin-profile-input"
                  placeholder="Enter new password (min 6 characters)"
                />
              </div>
              <div className="admin-profile-form-group">
                <label>Confirm New Password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  value={passwordData.confirmPassword}
                  onChange={handlePasswordInputChange}
                  className="admin-profile-input"
                  placeholder="Confirm new password"
                />
              </div>
            </div>
            <div className="admin-profile-modal-footer">
              <button className="admin-profile-btn-cancel" onClick={() => setShowChangePasswordModal(false)}>
                Cancel
              </button>
              <button className="admin-profile-btn-submit" onClick={submitChangePassword}>
                Change Password
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="admin-profile-success-overlay">
          <div className="admin-profile-success-modal">
            <div className="admin-profile-success-icon">✓</div>
            <div className="admin-profile-success-message">{successMessage}</div>
            <button 
              className="admin-profile-success-btn"
              onClick={() => setShowSuccessModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Error Modal */}
      {showErrorModal && (
        <div className="admin-profile-error-overlay">
          <div className="admin-profile-error-modal">
            <div className="admin-profile-error-icon">!</div>
            <div className="admin-profile-error-message">{errorMessage}</div>
            <button 
              className="admin-profile-error-btn"
              onClick={() => setShowErrorModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal - Styled exactly like ManageServices */}
      {showLogoutModal && (
        <div className="admin-profile-logout-overlay">
          <div className="admin-profile-logout-modal">
            <div className="admin-profile-logout-modal-text">Are you sure you want to logout?</div>
            <div className="admin-profile-logout-modal-actions">
              <button className="admin-profile-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="admin-profile-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminProfile;