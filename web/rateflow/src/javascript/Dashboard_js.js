import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NotificationBell from './NotificationBell';
import "../css/dashboard_css.css";

// PREDEFINED CATEGORIES
const PREDEFINED_CATEGORIES = [
  "Food & Hospitality",
  "Medical & Health",
  "Retail & Commercial",
  "Personal & Lifestyle"
];

function Dashboard() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Services");
  const [user, setUser] = useState(null);
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Filter services based on search term and category
  const filteredServices = services.filter(service => {
    const matchesSearch = service.serviceName?.toLowerCase().includes(searchTerm.toLowerCase()) || false;
    const matchesCategory = selectedCategory === "All" || service.serviceCategory === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Check authentication and fetch services
  useEffect(() => {
    checkAuth();
    fetchServices();
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
      
      if (data.role === "ADMIN") {
        navigate("/admindashboard");
        return;
      }
      
      setUser(data);
    } catch (error) {
      navigate("/");
    }
  };

  const fetchServices = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/services", {
        credentials: "include",
      });
      
      if (response.ok) {
        const data = await response.json();
        setServices(data);
      }
    } catch (error) {
      console.error("Error fetching services:", error);
    } finally {
      setLoading(false);
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

  const handleServiceClick = (serviceId) => {
    navigate(`/rate-service/${serviceId}`);
  };

  const getImageUrl = (serviceId) => {
    return `http://localhost:8080/api/services/${serviceId}/image`;
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-layout">
      {/* Sidebar */}
      <aside className={`dashboard-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="dashboard-sidebar-header">
          <div className="dashboard-logo">
            
            {!sidebarCollapsed && <span className="dashboard-logo-text">Dashboard</span>}
          </div>
        </div>

        <nav className="dashboard-nav">
          <button 
            className={`dashboard-nav-item ${activeTab === "Services" ? "active" : ""}`} 
            onClick={() => setActiveTab("Services")}
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

        <div className="dashboard-sidebar-footer">
          <button className="dashboard-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="dashboard-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="dashboard-panel">
        {/* Topbar */}
        <header className="dashboard-topbar">
          <div className="dashboard-topbar-content">
            <div>
              <h1 className="dashboard-page-title">Services</h1>
            </div>

            <div className="dashboard-search-wrapper">
              <input
                type="text"
                placeholder="Search services..."
                className="dashboard-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="dashboard-topbar-actions">
              <NotificationBell />
              <div className="dashboard-avatar" onClick={() => navigate("/profile")}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Bar */}
        <section className="dashboard-filter-bar">
          <h3 className="dashboard-filter-label">Filter by Category</h3>
          <div className="dashboard-filter-group">
            <button
              className={`dashboard-filter-chip ${selectedCategory === "All" ? "active" : ""}`}
              onClick={() => setSelectedCategory("All")}
            >
              All
            </button>
            {PREDEFINED_CATEGORIES.map(category => (
              <button
                key={category}
                className={`dashboard-filter-chip ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Services Grid */}
        <section className="dashboard-records-grid">
          {filteredServices.map((service) => (
            <div key={service.serviceId} className="dashboard-record-card">
              {service.image && (
                <div className="dashboard-record-thumbnail">
                  <img 
                    src={getImageUrl(service.serviceId)} 
                    alt={service.serviceName}
                    style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "10px" }}
                    onError={(e) => {
                      e.target.src = "https://via.placeholder.com/70x70?text=No+Image";
                    }}
                  />
                </div>
              )}
              <div className="dashboard-record-details">
                <h3 className="dashboard-record-name">{service.serviceName}</h3>
                <p className="dashboard-record-category">{service.serviceCategory}</p>
                <p className="dashboard-record-provider">by {service.createdBy || "Provider"}</p>
                <div className="dashboard-record-actions">
                  <button 
                    className="dashboard-record-action-btn rate"
                    onClick={() => handleServiceClick(service.serviceId)}
                  >
                    Rate Service
                  </button>
                </div>
              </div>
            </div>
          ))}
        </section>

        {filteredServices.length === 0 && (
          <div className="dashboard-no-results">
            No services found matching your criteria.
          </div>
        )}
      </main>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="dashboard-logout-overlay">
          <div className="dashboard-logout-modal">
            <div className="dashboard-logout-modal-text">Are you sure you want to logout?</div>
            <div className="dashboard-logout-modal-actions">
              <button className="dashboard-confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="dashboard-cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;