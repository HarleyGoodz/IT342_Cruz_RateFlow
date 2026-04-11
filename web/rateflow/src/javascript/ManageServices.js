import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/ManageServicesStyles.css";

function ManageServices() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState("Manage-Services");
  const [user, setUser] = useState(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showDeleteSuccessModal, setShowDeleteSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [serviceToDelete, setServiceToDelete] = useState(null);

  // Sample services data for visual demonstration
  const [services, setServices] = useState([
    { id: 1, name: "Gourmet Bistro", category: "Food & Hospitality", description: "Experience fine dining with our signature dishes and exceptional service." },
    { id: 2, name: "Wellness Center", category: "Medical & Health", description: "Comprehensive healthcare services including checkups and therapy." },
    { id: 3, name: "Urban Mart", category: "Retail & Commercial", description: "One-stop shopping destination for daily needs and luxury goods." },
    { id: 4, name: "Spa & Relaxation", category: "Personal & Lifestyle", description: "Rejuvenating spa treatments and personalized wellness programs." },
    { id: 5, name: "Cafe Deluxe", category: "Food & Hospitality", description: "Artisan coffee and fresh pastries in a cozy atmosphere." },
    { id: 6, name: "PharmaCare", category: "Medical & Health", description: "24/7 pharmacy with prescription delivery and health consultations." },
    { id: 7, name: "Fashion Hub", category: "Retail & Commercial", description: "Latest trends in fashion, accessories, and footwear." },
    { id: 8, name: "Fitness Studio", category: "Personal & Lifestyle", description: "Personal training, yoga, and group fitness classes." },
  ]);

  const [searchTerm, setSearchTerm] = useState("");
  const [selectedService, setSelectedService] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState(false);

  // Edit form state
  const [editName, setEditName] = useState("");
  const [editCategory, setEditCategory] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editImage, setEditImage] = useState(null);
  const [editImagePreview, setEditImagePreview] = useState(null);

  // Category options for filter
  const categories = ["All", "Food & Hospitality", "Medical & Health", "Retail & Commercial", "Personal & Lifestyle"];

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

  // Filter services based on search term and category
  const filteredServices = services.filter(service => {
    const matchesSearch = service.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      service.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "All" || service.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  // Handle edit click
  const handleEditClick = (service) => {
    setSelectedService(service);
    setEditName(service.name);
    setEditCategory(service.category);
    setEditDescription(service.description);
    setEditImagePreview(service.imagePreview || null);
    setEditImage(null);
    setIsEditing(true);
  };

  // Handle image change for edit form
  const handleEditImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setEditImage(file);
      setEditImagePreview(URL.createObjectURL(file));
    }
  };

  // Handle update service
  const handleUpdateService = () => {
    if (!selectedService) return;
    
    const updatedServices = services.map(service =>
      service.id === selectedService.id
        ? { 
            ...service, 
            name: editName, 
            category: editCategory, 
            description: editDescription, 
            image: editImage,
            imagePreview: editImagePreview
        }
        : service
    );
    setServices(updatedServices);
    setIsEditing(false);
    setSelectedService(null);
    setEditImage(null);
    setEditImagePreview(null);
    setSuccessMessage("Service Successfully Updated");
    setShowSuccessModal(true);
  };

  // Handle delete click - show confirmation modal
  const handleDeleteClick = (service) => {
    setServiceToDelete(service);
    setShowDeleteConfirmModal(true);
  };

  // Confirm delete service
  const confirmDelete = () => {
    if (serviceToDelete) {
      setServices(services.filter(service => service.id !== serviceToDelete.id));
      setShowDeleteConfirmModal(false);
      setServiceToDelete(null);
      setSuccessMessage("Service Successfully Deleted");
      setShowDeleteSuccessModal(true);
    }
  };

  // Cancel delete
  const cancelDelete = () => {
    setShowDeleteConfirmModal(false);
    setServiceToDelete(null);
  };

  // Handle cancel edit
  const handleCancelEdit = () => {
    setIsEditing(false);
    setSelectedService(null);
    setEditImage(null);
    setEditImagePreview(null);
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
    <div className="ms-layout">
      {/* Sidebar */}
      <aside className={`ms-sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="ms-sidebar-header">
          <div className="ms-logo">
            <span className="ms-logo-icon">🛡️</span>
            {!sidebarCollapsed && <span className="ms-logo-text">Admin</span>}
          </div>
        </div>

        <nav className="ms-nav">
          <button className={`ms-nav-item ${activeTab === "Services" ? "active" : ""}`} onClick={() => { setActiveTab("Services"); navigate("/admin"); }}>
            {!sidebarCollapsed && <span className="ms-nav-label">Services</span>}
          </button>
          <button className={`ms-nav-item ${activeTab === "Create-Service" ? "active" : ""}`} onClick={() => navigate("/admin/create-service")}>
            {!sidebarCollapsed && <span className="ms-nav-label">Create Service</span>}
          </button>
          <button className={`ms-nav-item ${activeTab === "Manage-Services" ? "active" : ""}`} onClick={() => setActiveTab("Manage-Services")}>
            {!sidebarCollapsed && <span className="ms-nav-label">Manage Services</span>}
          </button>
          <button className={`ms-nav-item ${activeTab === "Access-Control" ? "active" : ""}`} onClick={() => navigate("/admin/access-control")}>
            {!sidebarCollapsed && <span className="ms-nav-label">Access Control</span>}
          </button>
        </nav>

        <div className="ms-sidebar-footer">
          <button className="ms-logout-btn" onClick={handleLogoutClick}>
            {!sidebarCollapsed && <span className="ms-nav-label">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="ms-panel">
        {/* Topbar */}
        <header className="ms-topbar">
          <div className="ms-topbar-content">
            <h1 className="ms-page-title">Manage Services</h1>
            <div className="ms-search-wrapper">
              <input
                type="text"
                placeholder="Search services..."
                className="ms-search-input"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              
            </div>
            <div className="ms-topbar-actions">
              <button className="ms-icon-btn" onClick={() => navigate("/admin/notifications")}>
                🔔<span className="ms-notification-badge">3</span>
              </button>
              <div className="ms-avatar" onClick={() => navigate("/admin/profile")}>👤</div>
            </div>
          </div>
        </header>

        {/* Filter / Category Bar */}
        <section className="ms-filter-bar">
          <h3 className="ms-filter-label">Filter by Category</h3>
          <div className="ms-filter-group">
            {categories.map(category => (
              <button
                key={category}
                className={`ms-filter-chip ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </section>

        {/* Services Content Area */}
        <section className="ms-content-area">
          <div className="ms-services-container">
            {/* Created Services Header */}
            <div className="ms-services-header">
              <h2 className="ms-services-title">Created Services</h2>
              <div className="ms-services-count">{filteredServices.length} total</div>
            </div>

            {/* Services Grid */}
            <div className="ms-services-grid">
              {filteredServices.map(service => (
                <div key={service.id} className="ms-service-card">
                  <div className="ms-service-card-header">
                    <h3 className="ms-service-name">{service.name}</h3>
                    <span className="ms-service-category">{service.category}</span>
                  </div>
                  <p className="ms-service-description">{service.description}</p>
                  <div className="ms-service-actions">
                    <button className="ms-edit-btn" onClick={() => handleEditClick(service)}>Edit Service</button>
                    <button className="ms-delete-btn" onClick={() => handleDeleteClick(service)}>Delete</button>
                  </div>
                </div>
              ))}
            </div>

            {filteredServices.length === 0 && (
              <div className="ms-no-results">No services found matching "{searchTerm}"</div>
            )}
          </div>
        </section>
      </main>

      {/* Edit Modal - Styled like Create Service form */}
      {isEditing && selectedService && (
        <div className="ms-modal-overlay">
          <div className="ms-edit-form-card">
            {/* Image Upload */}
            <label className="ms-image-upload" htmlFor="ms-edit-image-file">
              {editImagePreview ? (
                <img src={editImagePreview} alt="Service preview" />
              ) : (
                <span>Upload Image</span>
              )}
              <input
                id="ms-edit-image-file"
                type="file"
                accept="image/*"
                style={{ display: "none" }}
                onChange={handleEditImageChange}
              />
            </label>

            {/* Form Fields */}
            <div className="ms-form-fields">
              <h2 className="ms-form-title">Edit Service</h2>

              <div className="ms-form-row">
                <input
                  type="text"
                  placeholder="Service Name"
                  className="ms-field-input"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                />
                <select 
                  className="ms-field-input ms-category-select"
                  value={editCategory}
                  onChange={(e) => setEditCategory(e.target.value)}
                >
                  <option value="Food & Hospitality">Food & Hospitality</option>
                  <option value="Medical & Health">Medical & Health</option>
                  <option value="Retail & Commercial">Retail & Commercial</option>
                  <option value="Personal & Lifestyle">Personal & Lifestyle</option>
                </select>
              </div>

              <textarea
                placeholder="Service Description"
                className="ms-textarea-input"
                value={editDescription}
                onChange={(e) => setEditDescription(e.target.value)}
              />

              <div className="ms-form-actions">
                <button className="ms-cancel-btn" onClick={handleCancelEdit}>
                  Cancel
                </button>
                <button className="ms-update-btn" onClick={handleUpdateService}>
                  Update Service
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteConfirmModal && serviceToDelete && (
        <div className="ms-delete-confirm-overlay">
          <div className="ms-delete-confirm-modal">
            <div className="ms-delete-confirm-icon">⚠️</div>
            <div className="ms-delete-confirm-text">
              Are you sure you want to delete "{serviceToDelete.name}"?
            </div>
            <div className="ms-delete-confirm-actions">
              <button className="ms-delete-cancel-btn" onClick={cancelDelete}>
                Cancel
              </button>
              <button className="ms-delete-confirm-btn" onClick={confirmDelete}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Success Modal for Update */}
      {showSuccessModal && (
        <div className="ms-success-overlay">
          <div className="ms-success-modal">
            <div className="ms-success-icon">✓</div>
            <div className="ms-success-message">{successMessage}</div>
            <button className="ms-success-btn" onClick={() => setShowSuccessModal(false)}>
              Continue
            </button>
          </div>
        </div>
      )}

      {/* Success Modal for Delete */}
      {showDeleteSuccessModal && (
        <div className="ms-success-overlay">
          <div className="ms-success-modal">
            <div className="ms-success-icon">✓</div>
            <div className="ms-success-message">{successMessage}</div>
            <button className="ms-success-btn" onClick={() => setShowDeleteSuccessModal(false)}>
              Continue
            </button>
          </div>
        </div>
      )}

       {/* Logout Modal - Styled exactly like CreateService */}
      {showLogoutModal && (
        <div className="ms-logout-overlay">
          <div className="ms-logout-modal">
            <div className="ms-logout-modal-text">
              Are you sure you want to logout?
            </div>
            <div className="ms-logout-modal-actions">
              <button className="ms-logout-confirm-btn" onClick={confirmLogout}>
                Confirm
              </button>
              <button className="ms-logout-cancel-btn" onClick={cancelLogout}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ManageServices;