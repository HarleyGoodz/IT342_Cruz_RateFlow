import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/admin_dashboard.css";

function AdminDashboard() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Services");
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);

  // Sample services data
  const [services] = useState([
    { id: 1, name: "Gourmet Bistro", category: "Food & Hospitality", description: "Fine dining experience" },
    { id: 2, name: "Wellness Center", category: "Medical & Health", description: "Comprehensive healthcare" },
    { id: 3, name: "Urban Mart", category: "Retail & Commercial", description: "Shopping destination" },
    { id: 4, name: "Spa & Relaxation", category: "Personal & Lifestyle", description: "Rejuvenating treatments" },
    { id: 5, name: "Cafe Deluxe", category: "Food & Hospitality", description: "Artisan coffee" },
    { id: 6, name: "PharmaCare", category: "Medical & Health", description: "24/7 pharmacy" },
    { id: 7, name: "Fashion Hub", category: "Retail & Commercial", description: "Latest trends" },
    { id: 8, name: "Fitness Studio", category: "Personal & Lifestyle", description: "Personal training" },
  ]);

  const categories = ["All", "Food & Hospitality", "Medical & Health", "Retail & Commercial", "Personal & Lifestyle"];

  // Filter services
  const filteredServices = services.filter(service => {
    const matchesSearch = service.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      service.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || service.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Check authentication status on mount
  useEffect(() => {
    let isMounted = true;

    const checkAuth = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/auth/me", {
          method: "GET",
          credentials: "include", // Important: sends HTTP cookies
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!isMounted) return;

        if (response.ok) {
          const userData = await response.json();
          setUser(userData);
        } else {
          // Not authenticated, redirect to login
          navigate("/login");
        }
      } catch (error) {
        console.error("Auth check error:", error);
        navigate("/login");
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    checkAuth();

    return () => {
      isMounted = false;
    };
  }, [navigate]);

  const handleLogoutClick = () => setShowLogoutModal(true);

  const confirmLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Logout failed");
      }

      // Clear user state
      setUser(null);
      setShowLogoutModal(false);
      
      // Redirect to login page
      navigate("/login");
    } catch (error) {
      console.error("Logout error:", error);
      alert(error.message || "Logout failed. Please try again.");
    }
  };

  const cancelLogout = () => setShowLogoutModal(false);

  // Show loading state while checking authentication
  if (loading) {
    return (
      <div className="admin-loading">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="admin-layout">
      {/* Sidebar */}
      <aside className={`admin-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="admin-sidebar-header">
          <div className="admin-logo">
            <span className="admin-logo-icon">🛡️</span>
            {!sidebarCollapsed && <span className="admin-logo-text">Admin</span>}
          </div>
        </div>

        <nav className="admin-nav">
          <button 
            className={`admin-nav-item ${activeTab === "Services" ? "active" : ""}`} 
            onClick={() => setActiveTab("Services")}
          >
            {!sidebarCollapsed && <span className="admin-nav-label">Services</span>}
          </button>
          <button 
            className={`admin-nav-item ${activeTab === "Create-Service" ? "active" : ""}`} 
            onClick={() => navigate("/admin/create-service")}
          >
            {!sidebarCollapsed && <span className="admin-nav-label">Create Service</span>}
          </button>
          <button 
            className={`admin-nav-item ${activeTab === "Manage-Services" ? "active" : ""}`} 
            onClick={() => navigate("/admin/manage-services")}
          >
            {!sidebarCollapsed && <span className="admin-nav-label">Manage Services</span>}
          </button>
          <button 
            className={`admin-nav-item ${activeTab === "Access-Control" ? "active" : ""}`} 
            onClick={() => navigate("/admin/access-control")}
          >
            {!sidebarCollapsed && <span className="admin-nav-label">Access Control</span>}
          </button>
        </nav>

        <div className="admin-sidebar-footer">
          {user && !sidebarCollapsed && (
            <div className="admin-user-info">
              <span className="admin-user-name">{user.username || user.email}</span>
            </div>
          )}
          <button className="admin-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="admin-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="admin-panel">
        {/* Topbar */}
        <header className="admin-topbar">
          <div className="admin-topbar-content">
            <div>
              <h1 className="admin-page-title">Services</h1>
            </div>

            <div className="admin-search-wrapper">
              <input
                type="text"
                placeholder="Search services..."
                className="admin-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="admin-topbar-actions">
              <button className="admin-icon-btn" onClick={() => navigate("/admin/notifications")}>
                🔔<span className="admin-notification-badge">3</span>
              </button>
              <div className="admin-avatar" onClick={() => navigate("/admin/profile")}>👤</div>
            </div>
          </div>
        </header>

        {/* Filter Bar */}
        <section className="admin-filter-bar">
          <h3 className="admin-filter-label">Filter by Category</h3>
          <div className="admin-filter-group">
            {categories.map(category => (
              <button
                key={category}
                className={`admin-filter-chip ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Services Grid */}
        <section className="admin-records-grid">
          {filteredServices.map((service) => (
            <div key={service.id} className="admin-record-card">
              <div className="admin-record-thumbnail" />
              <div className="admin-record-details">
                <h3 className="admin-record-name">{service.name}</h3>
                <p className="admin-record-category">{service.category}</p>
                <button className="admin-record-action-btn">
                  View Ratings
                </button>
              </div>
            </div>
          ))}
        </section>

        {filteredServices.length === 0 && (
          <div className="admin-no-results">No services found matching your criteria.</div>
        )}
      </main>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="admin-logout-overlay">
          <div className="admin-logout-modal">
            <div className="admin-logout-modal-text">Are you sure you want to logout?</div>
            <div className="admin-logout-modal-actions">
              <button className="admin-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="admin-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;