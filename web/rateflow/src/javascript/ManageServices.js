import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/ManageServicesStyles.css";

// PREDEFINED CATEGORIES FOR SERVICES
const PREDEFINED_SERVICE_CATEGORIES = [
  "Food & Hospitality",
  "Medical & Health", 
  "Retail & Commercial",
  "Personal & Lifestyle"
];

function ManageServices() {
  const navigate = useNavigate();
  const [manageSidebarCollapsed, setManageSidebarCollapsed] = useState(false);
  const [manageActiveTab, setManageActiveTab] = useState("Services");
  const [manageServicesList, setManageServicesList] = useState([]);
  const [manageCurrentUser, setManageCurrentUser] = useState(null);
  const [manageLoading, setManageLoading] = useState(true);
  const [manageShowCreateModal, setManageShowCreateModal] = useState(false);
  const [manageShowLogoutModal, setManageShowLogoutModal] = useState(false);
  const [manageEditingServiceItem, setManageEditingServiceItem] = useState(null);
  const [manageSelectedCategoryFilter, setManageSelectedCategoryFilter] = useState("All");
  const [manageSearchTerm, setManageSearchTerm] = useState("");
  
  // Notification states
  const [manageShowSuccessModal, setManageShowSuccessModal] = useState(false);
  const [manageShowErrorModal, setManageShowErrorModal] = useState(false);
  const [manageSuccessMessage, setManageSuccessMessage] = useState("");
  const [manageErrorMessage, setManageErrorMessage] = useState("");
  const [manageShowDeleteConfirmModal, setManageShowDeleteConfirmModal] = useState(false);
  const [manageServiceToDelete, setManageServiceToDelete] = useState(null);
  
  const [manageServiceFormData, setManageServiceFormData] = useState({
    serviceName: "",
    serviceCategory: PREDEFINED_SERVICE_CATEGORIES[0],
    serviceDescription: "",
    createdBy: "",
    image: null
  });

  // Filter services based on search and category
  const manageFilteredServices = manageServicesList.filter(service => {
    const matchesSearch = service.serviceName.toLowerCase().includes(manageSearchTerm.toLowerCase());
    const matchesCategory = manageSelectedCategoryFilter === "All" || service.serviceCategory === manageSelectedCategoryFilter;
    return matchesSearch && matchesCategory;
  });

  // Check authentication
  useEffect(() => {
    checkManageAuth();
    fetchManageServices();
  }, []);

  const checkManageAuth = async () => {
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
      
      setManageCurrentUser(data);
      setManageServiceFormData(prev => ({ ...prev, createdBy: data.username }));
    } catch (error) {
      navigate("/");
    }
  };

  const fetchManageServices = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/services", {
        credentials: "include",
      });
      
      if (response.ok) {
        const data = await response.json();
        setManageServicesList(data);
      }
    } catch (error) {
      console.error("Error fetching services:", error);
    } finally {
      setManageLoading(false);
    }
  };

  const handleManageLogoutClick = () => setManageShowLogoutModal(true);

  const confirmManageLogout = async () => {
    try {
      await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
      setManageShowLogoutModal(false);
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const cancelManageLogout = () => setManageShowLogoutModal(false);

  const handleManageInputChange = (e) => {
    const { name, value } = e.target;
    setManageServiceFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleManageImageChange = (e) => {
    setManageServiceFormData(prev => ({
      ...prev,
      image: e.target.files[0]
    }));
  };

  const handleManageCreateService = async (e) => {
    e.preventDefault();
    
    const formDataToSend = new FormData();
    formDataToSend.append("serviceName", manageServiceFormData.serviceName);
    formDataToSend.append("serviceCategory", manageServiceFormData.serviceCategory);
    formDataToSend.append("serviceDescription", manageServiceFormData.serviceDescription);
    formDataToSend.append("createdBy", manageServiceFormData.createdBy);
    formDataToSend.append("image", manageServiceFormData.image);

    try {
      const response = await fetch("http://localhost:8080/api/services/create", {
        method: "POST",
        body: formDataToSend,
        credentials: "include",
      });

      if (response.ok) {
        setManageShowCreateModal(false);
        resetManageForm();
        fetchManageServices();
        setManageSuccessMessage("Service created successfully!");
        setManageShowSuccessModal(true);
      } else {
        setManageErrorMessage("Failed to create service");
        setManageShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error creating service:", error);
      setManageErrorMessage("Error creating service");
      setManageShowErrorModal(true);
    }
  };

  const handleManageUpdateService = async (e) => {
    e.preventDefault();
    
    const formDataToSend = new FormData();
    formDataToSend.append("serviceName", manageServiceFormData.serviceName);
    formDataToSend.append("serviceCategory", manageServiceFormData.serviceCategory);
    formDataToSend.append("serviceDescription", manageServiceFormData.serviceDescription);
    formDataToSend.append("createdBy", manageServiceFormData.createdBy);
    if (manageServiceFormData.image) {
      formDataToSend.append("image", manageServiceFormData.image);
    }

    try {
      const response = await fetch(`http://localhost:8080/api/services/update/${manageEditingServiceItem.serviceId}`, {
        method: "PUT",
        body: formDataToSend,
        credentials: "include",
      });

      if (response.ok) {
        setManageEditingServiceItem(null);
        resetManageForm();
        fetchManageServices();
        setManageSuccessMessage("Service updated successfully!");
        setManageShowSuccessModal(true);
      } else {
        setManageErrorMessage("Failed to update service");
        setManageShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error updating service:", error);
      setManageErrorMessage("Error updating service");
      setManageShowErrorModal(true);
    }
  };

  const handleManageDeleteService = async () => {
    if (!manageServiceToDelete) return;

    try {
      const response = await fetch(`http://localhost:8080/api/services/delete/${manageServiceToDelete}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        setManageShowDeleteConfirmModal(false);
        fetchManageServices();
        setManageServiceToDelete(null);
        setManageSuccessMessage("Service deleted successfully!");
        setManageShowSuccessModal(true);
      } else {
        setManageErrorMessage("Failed to delete service");
        setManageShowErrorModal(true);
      }
    } catch (error) {
      console.error("Error deleting service:", error);
      setManageErrorMessage("Error deleting service");
      setManageShowErrorModal(true);
    }
  };

  const openDeleteConfirmModal = (serviceId) => {
    setManageServiceToDelete(serviceId);
    setManageShowDeleteConfirmModal(true);
  };

  const resetManageForm = () => {
    setManageServiceFormData({
      serviceName: "",
      serviceCategory: PREDEFINED_SERVICE_CATEGORIES[0],
      serviceDescription: "",
      createdBy: manageCurrentUser?.username || "",
      image: null
    });
  };

  const openManageEditModal = (service) => {
    setManageEditingServiceItem(service);
    setManageServiceFormData({
      serviceName: service.serviceName,
      serviceCategory: service.serviceCategory,
      serviceDescription: service.serviceDescription || "",
      createdBy: service.createdBy,
      image: null
    });
  };

  const getManageImageUrl = (serviceId) => {
    return `http://localhost:8080/api/services/${serviceId}/image`;
  };

  // Handle navigation functions
  const handleManageServicesNav = () => {
    navigate("/manageservices");
  };

  const handleAccessControls = () => {
    alert("Access Controls feature is coming soon!");
  };

  const toggleSidebar = () => {
    setManageSidebarCollapsed(!manageSidebarCollapsed);
  };

  if (manageLoading) {
    return (
      <div className="manage-loading">
        <div className="manage-loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="manage-layout">
      {/* Sidebar */}
      <aside className={`manage-sidebar ${manageSidebarCollapsed ? "collapsed" : ""}`}>
        <div className="manage-sidebar-header">
          <div className="manage-logo">
            <span className="manage-logo-icon">🛡️</span>
            {!manageSidebarCollapsed && <span className="manage-logo-text">Admin Panel</span>}
          </div>
        </div>

        <nav className="manage-nav">
          <button 
            className={`manage-nav-item ${manageActiveTab === "Services" ? "active" : ""}`} 
            onClick={() => {
              setManageActiveTab("Services");
              navigate("/admindashboard");
            }}
          >
            {!manageSidebarCollapsed && <span className="manage-nav-label">Services</span>}
          </button>

          <button 
            className={`manage-nav-item ${manageActiveTab === "Create Service" ? "active" : ""}`}
            onClick={() => {
              setManageActiveTab("Create Service");
              navigate("/createservice");
            }}
          >
            
            {!manageSidebarCollapsed && <span className="manage-nav-label">Create Service</span>}
          </button>
          
          <button 
            className={`manage-nav-item ${manageActiveTab === "Manage Services" ? "active" : ""}`}
            onClick={handleManageServicesNav}
          >
           
            {!manageSidebarCollapsed && <span className="manage-nav-label">Manage Services</span>}
          </button>

          <button 
            className="manage-nav-item"
            onClick={handleAccessControls}
          >
            
            {!manageSidebarCollapsed && <span className="manage-nav-label">Access Controls</span>}
          </button>
        </nav>

        <div className="manage-sidebar-footer">
          <button className="manage-logout-btn" onClick={handleManageLogoutClick}>
            
            {!manageSidebarCollapsed && <span className="manage-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="manage-panel">
        {/* Topbar */}
        <header className="manage-topbar">
          <div className="manage-topbar-content">
            <div>
              <h1 className="manage-page-title">Manage</h1>
            </div>

            <div className="manage-search-wrapper">
              <input
                type="text"
                placeholder="Search services..."
                className="manage-search-input"
                value={manageSearchTerm}
                onChange={(e) => setManageSearchTerm(e.target.value)}
              />
            </div>

            <div className="manage-topbar-actions">
              
              <div className="manage-avatar" onClick={() => navigate("/profile")}>
                {manageCurrentUser?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Filter Bar */}
        <section className="manage-filter-bar">
          <h3 className="manage-filter-label">Filter by Category</h3>
          <div className="manage-filter-group">
            <button
              className={`manage-filter-chip ${manageSelectedCategoryFilter === "All" ? "active" : ""}`}
              onClick={() => setManageSelectedCategoryFilter("All")}
            >
              All
            </button>
            {PREDEFINED_SERVICE_CATEGORIES.map(category => (
              <button
                key={category}
                className={`manage-filter-chip ${manageSelectedCategoryFilter === category ? "active" : ""}`}
                onClick={() => setManageSelectedCategoryFilter(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Services Grid */}
        <section className="manage-records-grid">
          {manageFilteredServices.map((service) => (
            <div key={service.serviceId} className="manage-record-card">
              {service.image && (
                <div className="manage-record-thumbnail">
                  <img 
                    src={getManageImageUrl(service.serviceId)} 
                    alt={service.serviceName}
                    style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "10px" }}
                    onError={(e) => {
                      e.target.src = "https://via.placeholder.com/70x70?text=No+Image";
                    }}
                  />
                </div>
              )}
              <div className="manage-record-details">
                <h3 className="manage-record-name">{service.serviceName}</h3>
                <p className="manage-record-category">{service.serviceCategory}</p>
                <p className="manage-record-provider">by {service.createdBy}</p>
                <div className="manage-record-actions">
                  <button 
                    className="manage-record-action-btn edit"
                    onClick={() => openManageEditModal(service)}
                  >
                    Edit
                  </button>
                  <button 
                    className="manage-record-action-btn delete"
                    onClick={() => openDeleteConfirmModal(service.serviceId)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </section>

        {manageFilteredServices.length === 0 && (
          <div className="manage-no-results">
            No services found matching your criteria.
          </div>
        )}
      </main>

            {/* Create/Edit Modal - Redesigned to match CreateService form */}
      {(manageShowCreateModal || manageEditingServiceItem) && (
        <div className="manage-modal-overlay">
          <div className="manage-modal manage-service-modal">
            <div className="manage-modal-header">
              <h2>{manageEditingServiceItem ? "Edit Service" : "Create New Service"}</h2>
              <button 
                className="manage-modal-close"
                onClick={() => {
                  setManageShowCreateModal(false);
                  setManageEditingServiceItem(null);
                  resetManageForm();
                }}
              >
                ×
              </button>
            </div>
            <form onSubmit={manageEditingServiceItem ? handleManageUpdateService : handleManageCreateService}>
              <div className="manage-modal-body manage-service-modal-body">
                {/* Image Upload Section */}
                <div className="manage-service-image-section">
                  <label className="manage-service-image-upload" htmlFor="manage-service-image-file">
                    {manageServiceFormData.image ? (
                      <img 
                        src={URL.createObjectURL(manageServiceFormData.image)} 
                        alt="Service preview" 
                      />
                    ) : manageEditingServiceItem && !manageServiceFormData.image ? (
                      <img 
                        src={getManageImageUrl(manageEditingServiceItem.serviceId)} 
                        alt={manageEditingServiceItem.serviceName}
                      />
                    ) : (
                      <span>Upload Image</span>
                    )}
                    <input
                      id="manage-service-image-file"
                      type="file"
                      accept="image/*"
                      style={{ display: "none" }}
                      onChange={handleManageImageChange}
                    />
                  </label>
                  {manageEditingServiceItem && (
                    <p className="manage-form-hint">Leave empty to keep current image</p>
                  )}
                </div>

                {/* Form Fields Section */}
                <div className="manage-service-form-fields">
                  <div className="manage-form-group">
                    <label>Service Name</label>
                    <input
                      type="text"
                      name="serviceName"
                      value={manageServiceFormData.serviceName}
                      onChange={handleManageInputChange}
                      required
                      placeholder="Enter service name"
                      className="manage-field-input"
                    />
                  </div>

                  <div className="manage-form-group">
                    <label>Service Category</label>
                    <select
                      className="manage-field-input manage-category-select"
                      value={manageServiceFormData.serviceCategory}
                      onChange={(e) => setManageServiceFormData(prev => ({ ...prev, serviceCategory: e.target.value }))}
                    >
                      {PREDEFINED_SERVICE_CATEGORIES.map(category => (
                        <option key={category} value={category}>
                          {category}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="manage-form-group">
                    <label>Service Description</label>
                    <textarea
                      name="serviceDescription"
                      value={manageServiceFormData.serviceDescription}
                      onChange={handleManageInputChange}
                      placeholder="Enter service description..."
                      className="manage-textarea-input"
                      rows="4"
                      required
                    />
                  </div>

                  <div className="manage-form-group">
                    <label>Created By</label>
                    <input
                      type="text"
                      name="createdBy"
                      value={manageServiceFormData.createdBy}
                      onChange={handleManageInputChange}
                      required
                      placeholder="Enter creator name"
                      className="manage-field-input"
                    />
                  </div>

                  <div className="manage-form-actions">
                    <button
                      type="button"
                      className="manage-btn-cancel"
                      onClick={() => {
                        setManageShowCreateModal(false);
                        setManageEditingServiceItem(null);
                        resetManageForm();
                      }}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="manage-btn-submit"
                    >
                      {manageEditingServiceItem ? "Update" : "Create"}
                    </button>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {manageShowDeleteConfirmModal && (
        <div className="manage-modal-overlay">
          <div className="manage-modal manage-confirm-modal">
            <div className="manage-modal-header">
              <h2>Confirm Delete</h2>
              <button 
                className="manage-modal-close"
                onClick={() => {
                  setManageShowDeleteConfirmModal(false);
                  setManageServiceToDelete(null);
                }}
              >
                ×
              </button>
            </div>
            <div className="manage-modal-body">
              <p className="manage-confirm-text">Are you sure you want to delete this service? This action cannot be undone.</p>
            </div>
            <div className="manage-modal-footer">
              <button
                type="button"
                className="manage-btn-cancel"
                onClick={() => {
                  setManageShowDeleteConfirmModal(false);
                  setManageServiceToDelete(null);
                }}
              >
                Cancel
              </button>
              <button
                type="button"
                className="manage-btn-delete"
                onClick={handleManageDeleteService}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {manageShowSuccessModal && (
        <div className="manage-success-overlay">
          <div className="manage-success-modal">
            <div className="manage-success-icon">✓</div>
            <div className="manage-success-message">{manageSuccessMessage}</div>
            <button 
              className="manage-success-btn"
              onClick={() => setManageShowSuccessModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Error Modal */}
      {manageShowErrorModal && (
        <div className="manage-error-overlay">
          <div className="manage-error-modal">
            <div className="manage-error-icon">!</div>
            <div className="manage-error-message">{manageErrorMessage}</div>
            <button 
              className="manage-error-btn"
              onClick={() => setManageShowErrorModal(false)}
            >
              OK
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {manageShowLogoutModal && (
        <div className="manage-logout-overlay">
          <div className="manage-logout-modal">
            <div className="manage-logout-modal-text">Are you sure you want to logout?</div>
            <div className="manage-logout-modal-actions">
              <button className="manage-confirm-btn" onClick={confirmManageLogout}>Confirm</button>
              <button className="manage-cancel-btn" onClick={cancelManageLogout}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ManageServices;