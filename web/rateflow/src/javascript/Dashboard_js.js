import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/dashboard_css.css";

function Dashboard() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Services");
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [searchTerm, setSearchTerm] = useState("");

  // Sample services data
  const [services] = useState([
    { id: 1, name: "Gourmet Bistro", category: "Food & Hospitality", description: "Fine dining experience with signature dishes" },
    { id: 2, name: "Wellness Center", category: "Medical & Health", description: "Comprehensive healthcare services" },
    { id: 3, name: "Urban Mart", category: "Retail & Commercial", description: "One-stop shopping destination" },
    { id: 4, name: "Spa & Relaxation", category: "Personal & Lifestyle", description: "Rejuvenating spa treatments" },
    { id: 5, name: "Cafe Deluxe", category: "Food & Hospitality", description: "Artisan coffee and fresh pastries" },
    { id: 6, name: "PharmaCare", category: "Medical & Health", description: "24/7 pharmacy with delivery" },
    { id: 7, name: "Fashion Hub", category: "Retail & Commercial", description: "Latest trends in fashion" },
    { id: 8, name: "Fitness Studio", category: "Personal & Lifestyle", description: "Personal training and yoga" },
  ]);

  const categories = ["All", "Food & Hospitality", "Medical & Health", "Retail & Commercial", "Personal & Lifestyle"];

  // Filter services based on search term and category
  const filteredServices = services.filter(service => {
    const matchesSearch = service.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      service.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || service.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

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

  /* LOGOUT MODAL FUNCTIONS */
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

  return (
    <div className="dashboard-layout">
      {/* Sidebar */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <div className="logo">
            {!sidebarCollapsed && (
              <span className="logo-text">Dashboard</span>
            )}
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
        {/* Header */}
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
              <button className="notifications-btn" onClick={() => navigate("/notifications")}>
                🔔
              </button>
              <div className="user-avatar" onClick={() => navigate("/profile")} style={{ cursor: "pointer" }}>
                👤
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
          {filteredServices.map((service) => (
            <div key={service.id} className="service-card">
              <div className="service-image" />
              <div className="service-info">
                <h3 className="service-name">{service.name}</h3>
                <p className="service-category">{service.category}</p>
                <button className="rate-btn" onClick={() => navigate("/rate-service")}>
                  Rate Service
                </button>
              </div>
            </div>
          ))}
        </section>

        {filteredServices.length === 0 && (
          <div className="no-results">
            No services found matching "{searchTerm}" in category "{selectedCategory === "All" ? "all categories" : selectedCategory}"
          </div>
        )}
      </main>

      {/* LOGOUT MODAL */}
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