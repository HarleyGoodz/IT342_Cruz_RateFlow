import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/AccessControlsStyles.css";

function AccessControls() {
  const navigate = useNavigate();
  const [accessSidebarCollapsed, setAccessSidebarCollapsed] = useState(false);
  const [accessActiveTab, setAccessActiveTab] = useState("Access Controls");
  const [accessUsersList, setAccessUsersList] = useState([]);
  const [accessCurrentUser, setAccessCurrentUser] = useState(null);
  const [accessLoading, setAccessLoading] = useState(true);
  const [accessSearchTerm, setAccessSearchTerm] = useState("");
  const [accessShowLogoutModal, setAccessShowLogoutModal] = useState(false);
  const [accessShowSuccessModal, setAccessShowSuccessModal] = useState(false);
  const [accessShowErrorModal, setAccessShowErrorModal] = useState(false);
  const [accessSuccessMessage, setAccessSuccessMessage] = useState("");
  const [accessErrorMessage, setAccessErrorMessage] = useState("");
  const [accessShowConfirmModal, setAccessShowConfirmModal] = useState(false);
  const [accessSelectedUser, setAccessSelectedUser] = useState(null);
  const [accessNotificationCount, setAccessNotificationCount] = useState(0);
const [accessShowNotificationToast, setAccessShowNotificationToast] = useState(false);
const [accessLatestNotification, setAccessLatestNotification] = useState(null);
  const [accessActionType, setAccessActionType] = useState(null);

  const fetchAccessNotificationCount = async () => {
  try {
    const response = await fetch("http://localhost:8080/api/notifications", {
      credentials: "include",
    });
    if (response.ok) {
      const data = await response.json();
      setAccessNotificationCount(data.length);
    }
  } catch (error) {
    console.error("Error fetching notification count:", error);
  }
};

const showAccessNotification = (message) => {
  setAccessLatestNotification({ message, timestamp: new Date() });
  setAccessShowNotificationToast(true);
  setTimeout(() => {
    setAccessShowNotificationToast(false);
  }, 3000);
  fetchAccessNotificationCount();
};

  // Filter users based on search
  const accessFilteredUsers = accessUsersList.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(accessSearchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(accessSearchTerm.toLowerCase());
    return matchesSearch;
  });

  // Check authentication and fetch users
  useEffect(() => {
    checkAccessAuth();
    fetchAccessUsers();

    fetchAccessNotificationCount();
  const interval = setInterval(fetchAccessNotificationCount, 30000);
  return () => clearInterval(interval);
  }, []);

  const checkAccessAuth = async () => {
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
      
      setAccessCurrentUser(data);
    } catch (error) {
      navigate("/");
    }
  };

  const fetchAccessUsers = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/users", {
        credentials: "include",
      });
      
      if (response.ok) {
        const data = await response.json();
        setAccessUsersList(data);
      } else {
        console.error("Failed to fetch users");
      }
    } catch (error) {
      console.error("Error fetching users:", error);
    } finally {
      setAccessLoading(false);
    }
  };

  const handleGrantAdminAccess = async (user) => {
    setAccessSelectedUser(user);
    setAccessActionType("grant");
    setAccessShowConfirmModal(true);
  };

  const handleRemoveAdminAccess = async (user) => {
    setAccessSelectedUser(user);
    setAccessActionType("remove");
    setAccessShowConfirmModal(true);
  };

  const confirmAction = async () => {
    if (!accessSelectedUser) return;

    try {
      const endpoint = accessActionType === "grant" 
        ? `http://localhost:8080/api/auth/grant-admin/${accessSelectedUser.id}`
        : `http://localhost:8080/api/auth/remove-admin/${accessSelectedUser.id}`;
      
      const response = await fetch(endpoint, {
        method: "PUT",
        credentials: "include",
      });

      if (response.ok) {
        const message = accessActionType === "grant" 
        ? `${accessSelectedUser.username} is now an Admin!`
        : `Admin access removed from ${accessSelectedUser.username}`;
    setAccessSuccessMessage(message);
    setAccessShowSuccessModal(true);
    fetchAccessUsers();
    showAccessNotification(message);
      } else {
        const error = await response.json();
        setAccessErrorMessage(error.error || "Operation failed");
        setAccessShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error updating user role:", error);
      setAccessErrorMessage("Error updating user role");
      setAccessShowErrorModal(true);
    } finally {
      setAccessShowConfirmModal(false);
      setAccessSelectedUser(null);
      setAccessActionType(null);
    }
  };

  const cancelConfirm = () => {
    setAccessShowConfirmModal(false);
    setAccessSelectedUser(null);
    setAccessActionType(null);
  };

  const handleAccessLogoutClick = () => setAccessShowLogoutModal(true);

  const confirmAccessLogout = async () => {
    try {
      await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
      setAccessShowLogoutModal(false);
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const cancelAccessLogout = () => setAccessShowLogoutModal(false);

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

  if (accessLoading) {
    return (
      <div className="access-loading">
        <div className="access-loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="access-layout">
      {/* Sidebar */}
      <aside className={`access-sidebar ${accessSidebarCollapsed ? "collapsed" : ""}`}>
        <div className="access-sidebar-header">
          <div className="access-logo">
            <span className="access-logo-icon">🛡️</span>
            {!accessSidebarCollapsed && <span className="access-logo-text">Admin Panel</span>}
          </div>
        </div>

        <nav className="access-nav">
          <button 
            className={`access-nav-item ${accessActiveTab === "Services" ? "active" : ""}`} 
            onClick={() => {
              setAccessActiveTab("Services");
              handleServicesNav();
            }}
          >
            {!accessSidebarCollapsed && <span className="access-nav-label">Services</span>}
          </button>

          <button 
            className={`access-nav-item ${accessActiveTab === "Create Service" ? "active" : ""}`}
            onClick={() => {
              setAccessActiveTab("Create Service");
              handleCreateServiceNav();
            }}
          >
            {!accessSidebarCollapsed && <span className="access-nav-label">Create Service</span>}
          </button>
          
          <button 
            className={`access-nav-item ${accessActiveTab === "Manage Services" ? "active" : ""}`}
            onClick={() => {
              setAccessActiveTab("Manage Services");
              handleManageServicesNav();
            }}
          >
            {!accessSidebarCollapsed && <span className="access-nav-label">Manage Services</span>}
          </button>

          <button 
            className={`access-nav-item ${accessActiveTab === "Access Controls" ? "active" : ""}`}
            onClick={() => {
              setAccessActiveTab("Access Controls");
            }}
          >
            {!accessSidebarCollapsed && <span className="access-nav-label">Access Controls</span>}
          </button>
        </nav>

        <div className="access-sidebar-footer">
          <button className="access-logout-btn" onClick={handleAccessLogoutClick}>
            {!accessSidebarCollapsed && <span className="access-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="access-panel">
        {/* Topbar */}
        <header className="access-topbar">
          <div className="access-topbar-content">
            <div>
              <h1 className="access-page-title">Access</h1>
            </div>

            <div className="access-search-wrapper">
              <input
                type="text"
                placeholder="Search users..."
                className="access-search-input"
                value={accessSearchTerm}
                onChange={(e) => setAccessSearchTerm(e.target.value)}
              />
            </div>

            <div className="access-topbar-actions">
            <button className="admin-notification-btn" onClick={() => navigate("/admin-notifications")} style={{ position: "relative" }}>
                    🔔
                    {accessNotificationCount > 0 && (
                        <span className="access-notification-badge">{accessNotificationCount}</span>
                    )}
                    </button>
              <div className="access-avatar" onClick={() => navigate("/admin-profile")}>
                {accessCurrentUser?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Users Grid */}
        <section className="access-users-grid">
        {accessFilteredUsers.map((user) => (
            <div key={user.id} className="access-user-card">
            <div className="access-user-info">
                <div className="access-user-avatar">
                {user.username?.charAt(0).toUpperCase()}
                </div>
                <div className="access-user-details">
                <h3 className="access-user-name">{user.username}</h3>
                <p className="access-user-email">{user.email}</p>
                <div className="access-user-role">
                    <span className={`access-role-badge ${user.role === "ADMIN" ? "admin" : "user"}`}>
                    {user.role === "ADMIN" ? "Admin" : "User"}
                    </span>
                </div>
                </div>
            </div>
            <div className="access-user-actions">
                {user.role === "ADMIN" ? (
                <button 
                    className="access-action-btn remove"
                    onClick={() => handleRemoveAdminAccess(user)}
                    disabled={user.id === accessCurrentUser?.id}
                    title={user.id === accessCurrentUser?.id ? "Cannot remove your own admin access" : ""}
                >
                    Remove Admin Access
                </button>
                ) : (
                <button 
                    className="access-action-btn grant"
                    onClick={() => handleGrantAdminAccess(user)}
                >
                    Grant Admin Access
                </button>
                )}
            </div>
            </div>
        ))}
        </section>

        {accessFilteredUsers.length === 0 && (
          <div className="access-no-results">
            No users found matching your search.
          </div>
        )}
      </main>

      {accessShowNotificationToast && accessLatestNotification && (
        <div className="access-notification-toast">
            <div className="access-notification-toast-icon">🔔</div>
            <div className="access-notification-toast-message">{accessLatestNotification.message}</div>
        </div>
        )}

      {/* Confirm Action Modal */}
      {accessShowConfirmModal && accessSelectedUser && (
        <div className="access-modal-overlay">
          <div className="access-modal access-confirm-modal">
            <div className="access-modal-header">
              <h2>Confirm Action</h2>
              <button className="access-modal-close" onClick={cancelConfirm}>
                ×
              </button>
            </div>
            <div className="access-modal-body">
              <p className="access-confirm-text">
                Are you sure you want to {accessActionType === "grant" ? "grant admin access to" : "remove admin access from"} 
                <strong> {accessSelectedUser.username}</strong>?
              </p>
              <p className="access-confirm-warning">
                This action will change the user's permissions immediately.
              </p>
            </div>
            <div className="access-modal-footer">
              <button type="button" className="access-btn-cancel" onClick={cancelConfirm}>
                Cancel
              </button>
              <button type="button" className={`access-btn-confirm ${accessActionType === "grant" ? "grant" : "remove"}`} onClick={confirmAction}>
                {accessActionType === "grant" ? "Grant Access" : "Remove Access"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {accessShowSuccessModal && (
        <div className="access-success-overlay">
          <div className="access-success-modal">
            <div className="access-success-icon">✓</div>
            <div className="access-success-message">{accessSuccessMessage}</div>
            <button 
              className="access-success-btn"
              onClick={() => setAccessShowSuccessModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Error Modal */}
      {accessShowErrorModal && (
        <div className="access-error-overlay">
          <div className="access-error-modal">
            <div className="access-error-icon">!</div>
            <div className="access-error-message">{accessErrorMessage}</div>
            <button 
              className="access-error-btn"
              onClick={() => setAccessShowErrorModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {accessShowLogoutModal && (
        <div className="access-logout-overlay">
          <div className="access-logout-modal">
            <div className="access-logout-modal-text">Are you sure you want to logout?</div>
            <div className="access-logout-modal-actions">
              <button className="access-confirm-btn" onClick={confirmAccessLogout}>Confirm</button>
              <button className="access-cancel-btn" onClick={cancelAccessLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AccessControls;