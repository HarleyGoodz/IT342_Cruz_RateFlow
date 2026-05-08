import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../css/admin_dashboard.css";

// PREDEFINED CATEGORIES - Add this at the top
const PREDEFINED_CATEGORIES = [
  "Food & Hospitality",
  "Medical & Health",
  "Retail & Commercial",
  "Personal & Lifestyle"
];

function AdminDashboard() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0); 
  const [activeTab, setActiveTab] = useState("Services");
  const [services, setServices] = useState([]);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [editingService, setEditingService] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [searchTerm, setSearchTerm] = useState("");
  const [showNotificationModal, setShowNotificationModal] = useState(false);
  const [latestNotification, setLatestNotification] = useState(null);
  const [formData, setFormData] = useState({
    serviceName: "",
    serviceCategory: PREDEFINED_CATEGORIES[0],
    createdBy: "",
    image: null
  });

  const showNotification = (message, type) => {
  setLatestNotification({ message, type, timestamp: new Date() });
  setShowNotificationModal(true);
  setTimeout(() => {
    setShowNotificationModal(false);
  }, 3000);
  fetchNotificationCount();
};

  // Filter services based on search and category
  const filteredServices = services.filter(service => {
    const matchesSearch = service.serviceName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || service.serviceCategory === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Check authentication
  useEffect(() => {
    checkAuth();
    fetchServices();
    fetchNotificationCount();
  }, []);

 const fetchNotificationCount = async () => {
  try {
    const response = await fetch("http://localhost:8080/api/notifications", {
      credentials: "include",
    });
    if (response.ok) {
      const data = await response.json();
      setUnreadCount(data.length); // Count total notifications
      console.log("Total admin notifications:", data.length);
    }
  } catch (error) {
    console.error("Error fetching notification count:", error);
  }
};

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
      setFormData(prev => ({ ...prev, createdBy: data.username }));
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

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleImageChange = (e) => {
    setFormData(prev => ({
      ...prev,
      image: e.target.files[0]
    }));
  };

  const handleCreateService = async (e) => {
    e.preventDefault();
    
    const formDataToSend = new FormData();
    formDataToSend.append("serviceName", formData.serviceName);
    formDataToSend.append("serviceCategory", formData.serviceCategory);
    formDataToSend.append("createdBy", formData.createdBy);
    formDataToSend.append("image", formData.image);

    try {
      const response = await fetch("http://localhost:8080/api/services/create", {
        method: "POST",
        body: formDataToSend,
        credentials: "include",
      });

      if (response.ok) {
        alert("Service created successfully!");
        setShowCreateModal(false);
        resetForm();
        fetchServices();
      } else {
        alert("Failed to create service");
      }
    } catch (error) {
      console.error("Error creating service:", error);
      alert("Error creating service");
    }
  };

  const handleUpdateService = async (e) => {
    e.preventDefault();
    
    const formDataToSend = new FormData();
    formDataToSend.append("serviceName", formData.serviceName);
    formDataToSend.append("serviceCategory", formData.serviceCategory);
    formDataToSend.append("createdBy", formData.createdBy);
    if (formData.image) {
      formDataToSend.append("image", formData.image);
    }

    try {
      const response = await fetch(`http://localhost:8080/api/services/update/${editingService.serviceId}`, {
        method: "PUT",
        body: formDataToSend,
        credentials: "include",
      });

      if (response.ok) {
        alert("Service updated successfully!");
        setEditingService(null);
        resetForm();
        fetchServices();
      } else {
        alert("Failed to update service");
      }
    } catch (error) {
      console.error("Error updating service:", error);
      alert("Error updating service");
    }
  };

  const handleDeleteService = async (serviceId) => {
    const confirmed = window.confirm("Are you sure you want to delete this service?");
    if (!confirmed) return;

    try {
      const response = await fetch(`http://localhost:8080/api/services/delete/${serviceId}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        alert("Service deleted successfully!");
        fetchServices();
      } else {
        alert("Failed to delete service");
      }
    } catch (error) {
      console.error("Error deleting service:", error);
      alert("Error deleting service");
    }
  };

  const resetForm = () => {
    setFormData({
      serviceName: "",
      serviceCategory: PREDEFINED_CATEGORIES[0],
      createdBy: user?.username || "",
      image: null
    });
  };

  const openEditModal = (service) => {
    setEditingService(service);
    setFormData({
      serviceName: service.serviceName,
      serviceCategory: service.serviceCategory,
      createdBy: service.createdBy,
      image: null
    });
  };

  const getImageUrl = (serviceId) => {
    return `http://localhost:8080/api/services/${serviceId}/image`;
  };

  // Handle navigation to Manage Services
  const handleManageServices = () => {
    navigate("/manageservices");
  };

  // Handle navigation to Access Controls 
  const handleAccessControls = () => {
    navigate("/access-controls");
  };

  // Handle view ratings (not functional yet)
  const handleViewRatings = (serviceId) => {
  navigate(`/admin/rateservice/${serviceId}`);
};
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
            {!sidebarCollapsed && <span className="admin-logo-text">Admin Dashboard</span>}
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
            className="admin-nav-item"
            onClick={() => navigate("/createservice")}
          >
            
            {!sidebarCollapsed && <span className="admin-nav-label">Create Service</span>}
          </button>
          
          <button 
            className="admin-nav-item"
            onClick={handleManageServices}
          >
            
            {!sidebarCollapsed && <span className="admin-nav-label">Manage Services</span>}
          </button>

          <button 
            className="admin-nav-item"
            onClick={handleAccessControls}
          >
            
            {!sidebarCollapsed && <span className="admin-nav-label">Access Controls</span>}
          </button>
        
        </nav>

        <div className="admin-sidebar-footer">
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
              <p className="admin-page-subtitle">View all services and their ratings</p>
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

              <button className="admin-notification-btn" onClick={() => navigate("/admin-notifications")} style={{ position: "relative" }}>
                🔔
                {unreadCount > 0 && (
                  <span className="notification-badge">{unreadCount}</span>
                )}
              </button>
              
              <div className="admin-avatar" onClick={() => navigate("/admin-profile")}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Bar */}
        <section className="admin-filter-bar">
          <h3 className="admin-filter-label">Filter by Category</h3>
          <div className="admin-filter-group">
            <button
              className={`admin-filter-chip ${selectedCategory === "All" ? "active" : ""}`}
              onClick={() => setSelectedCategory("All")}
            >
              All
            </button>
            {PREDEFINED_CATEGORIES.map(category => (
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
            <div key={service.serviceId} className="admin-record-card">
              {service.image && (
                <div className="admin-record-thumbnail">
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
              <div className="admin-record-details">
                <h3 className="admin-record-name">{service.serviceName}</h3>
                <p className="admin-record-category">{service.serviceCategory}</p>
                <p className="admin-record-provider">by {service.createdBy}</p>
                <div className="admin-record-actions">
                  <button 
                    className="admin-record-action-btn view-ratings"
                    onClick={() => handleViewRatings(service.serviceId)}
                  >
                    View Ratings
                  </button>
                </div>
              </div>
            </div>
          ))}
        </section>

        {filteredServices.length === 0 && (
          <div className="admin-no-results">
            No services found matching your criteria.
          </div>
        )}
      </main>

      {showNotificationModal && latestNotification && (
        <div className="admin-notification-toast">
          <div className="admin-notification-toast-icon">🔔</div>
          <div className="admin-notification-toast-message">{latestNotification.message}</div>
        </div>
      )}

      {/* Create/Edit Modal */}
      {(showCreateModal || editingService) && (
        <div className="admin-modal-overlay">
          <div className="admin-modal">
            <div className="admin-modal-header">
              <h2>{editingService ? "Edit Service" : "Create New Service"}</h2>
              <button 
                className="admin-modal-close"
                onClick={() => {
                  setShowCreateModal(false);
                  setEditingService(null);
                  resetForm();
                }}
              >
                ×
              </button>
            </div>
            <form onSubmit={editingService ? handleUpdateService : handleCreateService}>
              <div className="admin-modal-body">
                <div className="admin-form-group">
                  <label>Service Name</label>
                  <input
                    type="text"
                    name="serviceName"
                    value={formData.serviceName}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter service name"
                  />
                </div>

                <div className="admin-form-group">
                  <label>Service Category</label>
                  <div className="admin-category-chips">
                    {PREDEFINED_CATEGORIES.map(category => (
                      <button
                        key={category}
                        type="button"
                        className={`admin-category-chip ${formData.serviceCategory === category ? "active" : ""}`}
                        onClick={() => setFormData(prev => ({ ...prev, serviceCategory: category }))}
                      >
                        {category}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="admin-form-group">
                  <label>Created By</label>
                  <input
                    type="text"
                    name="createdBy"
                    value={formData.createdBy}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter creator name"
                  />
                </div>

                <div className="admin-form-group">
                  <label>Service Image</label>
                  <input
                    type="file"
                    name="image"
                    onChange={handleImageChange}
                    accept="image/*"
                    required={!editingService}
                  />
                  {editingService && (
                    <p className="admin-form-hint">Leave empty to keep current image</p>
                  )}
                </div>
              </div>
              <div className="admin-modal-footer">
                <button
                  type="button"
                  className="admin-btn-cancel"
                  onClick={() => {
                    setShowCreateModal(false);
                    setEditingService(null);
                    resetForm();
                  }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="admin-btn-submit"
                >
                  {editingService ? "Update" : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

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