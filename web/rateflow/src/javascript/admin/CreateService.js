import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../css/admin/CreateServiceStyles.css";

// Predefined categories
const PREDEFINED_CATEGORIES = [
  "Food & Hospitality",
  "Medical & Health",
  "Retail & Commercial",
  "Personal & Lifestyle"
];

function CreateService() {
  const navigate = useNavigate();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Create-Service");
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [notificationCount, setNotificationCount] = useState(0);
  const [showNotificationToast, setShowNotificationToast] = useState(false);
  const [latestNotification, setLatestNotification] = useState(null);
  
  // Form state
  const [formData, setFormData] = useState({
    serviceName: "",
    serviceCategory: PREDEFINED_CATEGORIES[0],
    serviceDescription: "",
    createdBy: "",
    image: null
  });
  const [imagePreview, setImagePreview] = useState(null);
  
  // Validation error states
  const [errors, setErrors] = useState({
    serviceName: false,
    serviceDescription: false,
    image: false
  });

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

  const showNotification = (message, type = "error") => {
    setLatestNotification({ message, type, timestamp: new Date() });
    setShowNotificationToast(true);
    setTimeout(() => {
      setShowNotificationToast(false);
    }, 4000);
    fetchNotificationCount();
  };

  // Sample services for display
  const [sampleServices] = useState([
    { id: 1, name: "Gourmet Bistro", category: "Food & Hospitality", description: "Experience fine dining with our signature dishes and exceptional service." },
    { id: 2, name: "Wellness Center", category: "Medical & Health", description: "Comprehensive healthcare services including checkups and therapy." },
    { id: 3, name: "Urban Mart", category: "Retail & Commercial", description: "One-stop shopping destination for daily needs and luxury goods." },
    { id: 4, name: "Spa & Relaxation", category: "Personal & Lifestyle", description: "Rejuvenating spa treatments and personalized wellness programs." },
  ]);

  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");

  const categories = ["All", ...PREDEFINED_CATEGORIES];

  // Filter sample services based on search term and category
  const filteredSampleServices = sampleServices.filter(service => {
    const matchesSearch = service.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      service.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || service.category === selectedCategory;
    return matchesSearch && matchesCategory;
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
      
      if (data.role !== "ADMIN") {
        alert("Access denied. Admin only.");
        navigate("/dashboard");
        return;
      }
      
      setUser(data);
      setFormData(prev => ({
        ...prev,
        createdBy: data.username
      }));
    } catch (error) {
      navigate("/");
    } finally {
      setLoading(false);
    }
  };

  const handleManageServices = () => {
    navigate("/manageservices");
  };

  const handleAccessControls = () => {
    navigate("/access-controls");
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: false
      }));
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setFormData(prev => ({
        ...prev,
        image: file
      }));
      setImagePreview(URL.createObjectURL(file));
      // Clear image error
      if (errors.image) {
        setErrors(prev => ({
          ...prev,
          image: false
        }));
      }
    }
  };

  // Validation function
  const validateForm = () => {
    const newErrors = {
      serviceName: !formData.serviceName.trim(),
      serviceDescription: !formData.serviceDescription.trim(),
      image: !formData.image
    };
    
    setErrors(newErrors);
    
    // Build error message
    const missingFields = [];
    if (newErrors.serviceName) missingFields.push("Service Name");
    if (newErrors.serviceDescription) missingFields.push("Service Description");
    if (newErrors.image) missingFields.push("Service Image");
    
    if (missingFields.length > 0) {
      const errorMessage = `Please fill in the following required fields: ${missingFields.join(", ")}`;
      showNotification(errorMessage, "error");
      
      // Scroll to first error field
      if (newErrors.serviceName) {
        document.querySelector('input[name="serviceName"]')?.scrollIntoView({ behavior: "smooth", block: "center" });
      } else if (newErrors.serviceDescription) {
        document.querySelector('textarea[name="serviceDescription"]')?.scrollIntoView({ behavior: "smooth", block: "center" });
      } else if (newErrors.image) {
        document.querySelector('.cs-image-upload')?.scrollIntoView({ behavior: "smooth", block: "center" });
      }
      
      return false;
    }
    
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate all fields first
    if (!validateForm()) {
      return;
    }
    
    const formDataToSend = new FormData();
    formDataToSend.append("serviceName", formData.serviceName);
    formDataToSend.append("serviceCategory", formData.serviceCategory);
    formDataToSend.append("serviceDescription", formData.serviceDescription);
    formDataToSend.append("createdBy", formData.createdBy);
    formDataToSend.append("image", formData.image);

    try {
      const response = await fetch("http://localhost:8080/api/services/create", {
        method: "POST",
        body: formDataToSend,
        credentials: "include",
      });

      if (response.ok) {
        showNotification(`Service "${formData.serviceName}" was created successfully!`, "success");
        setShowSuccessModal(true);
      } else {
        const errorData = await response.text();
        showNotification(`Failed to create service: ${errorData}`, "error");
      }
    } catch (error) {
      console.error("Error creating service:", error);
      showNotification("Error creating service. Please try again.", "error");
    }
  };

  const handleCancel = () => {
    navigate("/admindashboard");
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

  if (loading) {
    return (
      <div className="cs-loading">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="cs-layout">
      {/* Notification Toast */}
      {showNotificationToast && latestNotification && (
        <div className={`cs-notification-toast ${latestNotification.type === "success" ? "success" : "error"}`}>
          <div className="cs-notification-icon">🔔</div>
          <div className="cs-notification-message">{latestNotification.message}</div>
        </div>
      )}

      {/* Sidebar */}
      <aside className={`cs-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="cs-sidebar-header">
          <div className="cs-logo">
          
            {!sidebarCollapsed && <span className="cs-logo-text">Admin Dashboard</span>}
          </div>
        </div>

        <nav className="cs-nav">
          <button 
            className={`cs-nav-item ${activeTab === "Services" ? "active" : ""}`} 
            onClick={() => {
              setActiveTab("Services");
              navigate("/admindashboard");
            }}
          >
            {!sidebarCollapsed && <span className="cs-nav-label">Services</span>}
          </button>

          <button 
            className={`cs-nav-item ${activeTab === "Create-Service" ? "active" : ""}`}
            onClick={() => {
              setActiveTab("Create-Service");
              navigate("/createservice");
            }}
          >
            {!sidebarCollapsed && <span className="cs-nav-label">Create Service</span>}
          </button>

          <button 
            className={`cs-nav-item ${activeTab === "Manage-Services" ? "active" : ""}`}
            onClick={() => {
              setActiveTab("Manage-Services");
              navigate("/manageservices");
            }}
          >
            {!sidebarCollapsed && <span className="cs-nav-label">Manage Services</span>}
          </button>

          <button 
            className={`cs-nav-item ${activeTab === "Access-Controls" ? "active" : ""}`}
            onClick={() => {
              setActiveTab("Access-Controls");
              handleAccessControls();
            }}
          >
            {!sidebarCollapsed && <span className="cs-nav-label">Access Controls</span>}
          </button>
        </nav>

        <div className="cs-sidebar-footer">
          <button className="cs-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="cs-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="cs-panel">
        {/* Topbar */}
        <header className="cs-topbar">
          <div className="cs-topbar-content">
            <h1 className="cs-page-title">Create Service</h1>
            <div className="cs-search-wrapper">
              <input
                type="text"
                placeholder="Search sample services..."
                className="cs-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div className="cs-topbar-actions">
              <button className="admin-notification-btn" onClick={() => navigate("/admin-notifications")} style={{ position: "relative" }}>
                🔔
                {notificationCount > 0 && (
                  <span className="notification-badge">{notificationCount}</span>
                )}
              </button>
              <div className="cs-avatar" onClick={() => navigate("/admin-profile")}>
                {user?.username?.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Form Area */}
        <section className="cs-form-area">
          <div className="cs-form-card">
            {/* Image Upload */}
            <label className={`cs-image-upload ${errors.image ? "error" : ""}`} htmlFor="cs-image-file">
              {imagePreview ? (
                <img src={imagePreview} alt="Service preview" />
              ) : (
                <span>Upload Image {errors.image && "(Required)"}</span>
              )}
              <input
                id="cs-image-file"
                type="file"
                accept="image/*"
                style={{ display: "none" }}
                onChange={handleImageChange}
                required
              />
            </label>

            {/* Form Fields */}
            <div className="cs-form-fields">
              <h2 className="cs-form-title">Create New Service</h2>

              <div className="cs-form-row">
                <input
                  type="text"
                  name="serviceName"
                  placeholder="Service Name *"
                  className={`cs-field-input ${errors.serviceName ? "error" : ""}`}
                  value={formData.serviceName}
                  onChange={handleInputChange}
                  required
                />
                {errors.serviceName && (
                  <div className="cs-field-error">Service Name is required</div>
                )}
              </div>

              <div className="cs-form-group">
                <label className="cs-form-label">Service Category</label>
                <select
                  className="cs-field-input cs-category-select"
                  value={formData.serviceCategory}
                  onChange={(e) => setFormData(prev => ({ ...prev, serviceCategory: e.target.value }))}
                >
                  {PREDEFINED_CATEGORIES.map(category => (
                    <option key={category} value={category}>
                      {category}
                    </option>
                  ))}
                </select>
              </div>

              <div className="cs-form-group">
                <label className="cs-form-label">Service Description *</label>
                <textarea
                  name="serviceDescription"
                  placeholder="Enter service description... *"
                  className={`cs-textarea-input ${errors.serviceDescription ? "error" : ""}`}
                  value={formData.serviceDescription}
                  onChange={handleInputChange}
                  rows="4"
                  required
                />
                {errors.serviceDescription && (
                  <div className="cs-field-error">Service Description is required</div>
                )}
              </div>

              <div className="cs-form-actions">
                <button className="cs-cancel-btn" onClick={handleCancel}>
                  Cancel
                </button>
                <button className="cs-create-btn" onClick={handleSubmit}>
                  Create Service
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="cs-success-overlay">
          <div className="cs-success-modal">
            <div className="cs-success-icon">✓</div>
            <div className="cs-success-message">Service Successfully Created</div>
            <button className="cs-success-btn" onClick={() => navigate("/admindashboard")}>
              Continue
            </button>
          </div>
        </div>
      )}

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="cs-logout-overlay">
          <div className="cs-logout-modal">
            <div className="cs-logout-modal-text">Are you sure you want to logout?</div>
            <div className="cs-logout-modal-actions">
              <button className="cs-logout-confirm-btn" onClick={confirmLogout}>
                Confirm
              </button>
              <button className="cs-logout-cancel-btn" onClick={cancelLogout}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CreateService;