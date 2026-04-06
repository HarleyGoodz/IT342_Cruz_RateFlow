import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/AccessControlStyles.css";

function AccessControl() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Access-Control");
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  // Sample users data
  const [users, setUsers] = useState([
    { id: 1, name: "John Doe", email: "john.doe@example.com", role: "User", isAdmin: false },
    { id: 2, name: "Jane Smith", email: "jane.smith@example.com", role: "User", isAdmin: false },
    { id: 3, name: "Michael Johnson", email: "michael.j@example.com", role: "User", isAdmin: false },
    { id: 4, name: "Emily Brown", email: "emily.brown@example.com", role: "User", isAdmin: false },
    { id: 5, name: "David Wilson", email: "david.wilson@example.com", role: "User", isAdmin: false },
    { id: 6, name: "Sarah Martinez", email: "sarah.m@example.com", role: "User", isAdmin: false },
  ]);

  const [searchTerm, setSearchTerm] = useState("");

  // Filter users based on search
  const filteredUsers = users.filter(user =>
    user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  useEffect(() => {
    let isMounted = true;

    const init = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      if (!isMounted) return;
      if (session) {
        setUser({ username: session.user.user_metadata?.full_name || session.user.email });
      }
    };

    init();

    const { data: listener } = supabase.auth.onAuthStateChange((event, session) => {
      if (event === "SIGNED_OUT") navigate("/");
      if (session) {
        setUser({ username: session.user.user_metadata?.full_name || session.user.email });
      }
    });

    return () => {
      isMounted = false;
      listener.subscription.unsubscribe();
    };
  }, [navigate]);

  // Handle grant admin access
  const handleGrantAdmin = (userId) => {
    const updatedUsers = users.map(user =>
      user.id === userId ? { ...user, isAdmin: true, role: "Admin" } : user
    );
    setUsers(updatedUsers);
    setSuccessMessage("Admin access granted successfully");
    setShowSuccessModal(true);
  };

  // Handle revoke admin access
  const handleRevokeAdmin = (userId) => {
    const updatedUsers = users.map(user =>
      user.id === userId ? { ...user, isAdmin: false, role: "User" } : user
    );
    setUsers(updatedUsers);
    setSuccessMessage("Admin access revoked successfully");
    setShowSuccessModal(true);
  };

  const handleLogoutClick = () => setShowLogoutModal(true);

  const confirmLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
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

  const cancelLogout = () => setShowLogoutModal(false);

  return (
    <div className="ac-layout">
      {/* Sidebar */}
      <aside className={`ac-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="ac-sidebar-header">
          <div className="ac-logo">
            <span className="ac-logo-icon">🛡️</span>
            {!sidebarCollapsed && <span className="ac-logo-text">Admin</span>}
          </div>
        </div>

        <nav className="ac-nav">
          <button className={`ac-nav-item ${activeTab === "Services" ? "active" : ""}`} onClick={() => { setActiveTab("Services"); navigate("/admin"); }}>
            {!sidebarCollapsed && <span className="ac-nav-label">Services</span>}
          </button>
          <button className={`ac-nav-item ${activeTab === "Create-Service" ? "active" : ""}`} onClick={() => navigate("/admin/create-service")}>
            {!sidebarCollapsed && <span className="ac-nav-label">Create Service</span>}
          </button>
          <button className={`ac-nav-item ${activeTab === "Manage-Services" ? "active" : ""}`} onClick={() => navigate("/admin/manage-services")}>
            {!sidebarCollapsed && <span className="ac-nav-label">Manage Services</span>}
          </button>
          <button className={`ac-nav-item ${activeTab === "Access-Control" ? "active" : ""}`} onClick={() => setActiveTab("Access-Control")}>
            {!sidebarCollapsed && <span className="ac-nav-label">Access Control</span>}
          </button>
        </nav>

        <div className="ac-sidebar-footer">
          <button className="ac-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="ac-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="ac-panel">
        {/* Topbar */}
        <header className="ac-topbar">
          <div className="ac-topbar-content">
            <h1 className="ac-page-title">Access Control</h1>
            <div className="ac-search-wrapper">
              <input
                type="text"
                placeholder="Search users..."
                className="ac-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
        
            </div>
            <div className="ac-topbar-actions">
              <button className="ac-icon-btn" onClick={() => navigate("/admin/notifications")}>
                🔔<span className="ac-notification-badge">3</span>
              </button>
              <div className="ac-avatar" onClick={() => navigate("/admin/profile")}>👤</div>
            </div>
          </div>
        </header>

        {/* Users Content Area */}
        <section className="ac-content-area">
          <div className="ac-users-container">
            {/* Users Header */}
            <div className="ac-users-header">
              <h2 className="ac-users-title">Users</h2>
              <div className="ac-users-count">{filteredUsers.length} total</div>
            </div>

            {/* Users List */}
            <div className="ac-users-list">
              {filteredUsers.map((userItem) => (
                <div key={userItem.id} className="ac-user-card">
                  <div className="ac-user-avatar">
                    {userItem.name.charAt(0)}
                  </div>
                  <div className="ac-user-info">
                    <h3 className="ac-user-name">{userItem.name}</h3>
                    <p className="ac-user-email">{userItem.email}</p>
                  </div>
                  <div className="ac-user-actions">

                    <span className={`ac-user-role ${userItem.isAdmin ? "admin" : "user"}`}>
                      {userItem.role}
                    </span>
                    
                    {!userItem.isAdmin ? (
                      <button 
                        className="ac-grant-btn"
                        onClick={() => handleGrantAdmin(userItem.id)}
                      >
                        Grant Admin Access
                      </button>
                    ) : (
                      <button 
                        className="ac-revoke-btn"
                        onClick={() => handleRevokeAdmin(userItem.id)}
                      >
                        Revoke Admin Access
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {filteredUsers.length === 0 && (
              <div className="ac-no-results">No users found matching "{searchTerm}"</div>
            )}
          </div>
        </section>
      </main>

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="ac-success-overlay">
          <div className="ac-success-modal">
            <div className="ac-success-icon">✓</div>
            <div className="ac-success-message">{successMessage}</div>
            <button className="ac-success-btn" onClick={() => setShowSuccessModal(false)}>
              Continue
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="ac-logout-overlay">
          <div className="ac-logout-modal">
            <div className="ac-logout-modal-text">
              Are you sure you want to logout?
            </div>
            <div className="ac-logout-modal-actions">
              <button className="ac-logout-confirm-btn" onClick={confirmLogout}>
                Confirm
              </button>
              <button className="ac-logout-cancel-btn" onClick={cancelLogout}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AccessControl;