import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/dashboard_css.css";

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

  const categories = ["All", "Food & Hospitality", "Medical & Health", "Retail & Commercial", "Personal & Lifestyle"];

  // Filter services based on search term and category
  const filteredServices = services.filter(service => {
    const matchesSearch = service.serviceName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || service.serviceCategory === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Check authentication and fetch services
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
        if (data.role === "ADMIN") {
          navigate("/admindashboard");
          return;
        }
        setUser(data);
        fetchServices();
      })
      .catch(() => {
        navigate("/");
      });
  }, [navigate]);

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

  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

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

  const cancelLogout = () => {
    setShowLogoutModal(false);
  };

  const handleServiceClick = (serviceId) => {
    navigate(`/rate-service/${serviceId}`);
  };

  const getImageUrl = (serviceId) => {
    return `http://localhost:8080/api/services/${serviceId}/image`;
  };

  function getUniqueCategories() {
    const cats = services.map(service => service.serviceCategory);
    return [...new Set(cats)];
  }

  return (
    <div className="dashboard-layout">
      {/* Sidebar */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <div className="logo">
            {!sidebarCollapsed && <span className="logo-text">Dashboard</span>}
          </div>
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
            onClick={() => navigate("/my-ratings")}
          >
            {!sidebarCollapsed && <span className="nav-label">My Ratings</span>}
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-sidebar-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="dashboard-header">
          <div className="header-content">
            <h1 className="page-title">Services</h1>
            <div className="header-search">
              <input
                type="text"
                placeholder="Search services..."
                className="search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div className="header-actions">
              <div className="user-avatar" onClick={() => navigate("/profile")}>
                {user ? user.username.charAt(0).toUpperCase() : "U"}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Section */}
        <section className="filter-section">
          <h3 className="filter-title">Filter Category</h3>
          <div className="filter-buttons">
            {categories.map((category) => (
              <button
                key={category}
                className={`filter-btn ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Services Grid */}
        <section className="services-grid">
          {!loading && filteredServices.map((service) => (
            <div key={service.serviceId} className="service-card">
              {service.image && (
                <div className="service-image">
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
              <div className="service-info">
                <h3 className="service-name">{service.serviceName}</h3>
                <p className="service-category">{service.serviceCategory}</p>
                <button className="rate-btn" onClick={() => handleServiceClick(service.serviceId)}>
                  Rate Service
                </button>
              </div>
            </div>
          ))}
        </section>

        {!loading && filteredServices.length === 0 && (
          <div className="no-results">
            No services found matching "{searchTerm}" in category "{selectedCategory === "All" ? "all categories" : selectedCategory}"
          </div>
        )}

        {loading && (
          <div className="loading-state">Loading services...</div>
        )}
      </main>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="logout-modal-overlay">
          <div className="logout-modal">
            <div className="logout-text">Are you sure you want to logout?</div>
            <div className="logout-buttons">
              <button className="confirm-btn" onClick={confirmLogout}>Confirm</button>
              <button className="cancel-btn" onClick={cancelLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;