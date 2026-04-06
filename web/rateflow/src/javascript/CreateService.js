import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/CreateServiceStyles.css";

function CreateService() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] =
    useState(false);

  const [activeTab, setActiveTab] =
    useState("Create-Service");

  const [user, setUser] = useState(null);

  const [showLogoutModal, setShowLogoutModal] =
    useState(false);

  /* FORM STATE */

  const [serviceName, setServiceName] =
    useState("");

  const [serviceCategory, setServiceCategory] =
    useState("");

  const [serviceDescription, setServiceDescription] =
    useState("");

  const [serviceImage, setServiceImage] =
    useState(null);

  const [imagePreview, setImagePreview] =
    useState(null);

  useEffect(() => {
    let isMounted = true;

    const init = async () => {
      const {
        data: { session },
      } = await supabase.auth.getSession();

      if (!isMounted) return;

      if (session) {
        setUser({
          username:
            session.user.user_metadata
              ?.full_name ||
            session.user.email,
        });
      }
    };

    init();

    const { data: listener } =
      supabase.auth.onAuthStateChange(
        (event, session) => {
          console.log("Auth event:", event);

          if (event === "SIGNED_OUT") {
            navigate("/");
          }

          if (session) {
            setUser({
              username:
                session.user.user_metadata
                  ?.full_name ||
                session.user.email,
            });
          }
        }
      );

    return () => {
      isMounted = false;
      listener.subscription.unsubscribe();
    };
  }, [navigate]);

  /* IMAGE UPLOAD */

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setServiceImage(file);
      setImagePreview(URL.createObjectURL(file));
    }
  };

  /* FORM SUBMIT */

  const handleCreate = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/services/create",
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: serviceName,
            category: serviceCategory,
            description: serviceDescription,
          }),
        }
      );

      if (!response.ok) {
        alert("Failed to create service");
        return;
      }

      alert("Service created successfully!");
      navigate("/admin/services");
    } catch (error) {
      console.error("Create service error:", error);
      alert("Failed to create service");
    }
  };

  const handleCancel = () => {
    navigate("/admin");
  };

  /* LOGOUT MODAL FUNCTIONS */

  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const confirmLogout = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/logout",
        {
          method: "POST",
          credentials: "include",
        }
      );

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
    <div className="cs-layout">

      {/* Sidebar */}

      <aside
        className={`cs-sidebar ${
          sidebarCollapsed ? "collapsed" : ""
        }`}
      >
        <div className="cs-sidebar-header">

          <div className="cs-logo">
            <span className="cs-logo-icon">
              🛡️
            </span>

            {!sidebarCollapsed && (
              <span className="cs-logo-text">
                Admin
              </span>
            )}
          </div>

        </div>

        <nav className="cs-nav">

          <button
            className={`cs-nav-item ${
              activeTab === "Services" ? "active" : ""
            }`}
            onClick={() => {
              setActiveTab("Services");
              navigate("/admin");
            }}
          >
            {!sidebarCollapsed && (
              <span className="cs-nav-label">
                Services
              </span>
            )}
          </button>

          <button
            className={`cs-nav-item ${
              activeTab === "Create-Service"
                ? "active"
                : ""
            }`}
            onClick={() =>
              setActiveTab("Create-Service")
            }
          >
            {!sidebarCollapsed && (
              <span className="cs-nav-label">
                Create Service
              </span>
            )}
          </button>

          <button
            className={`cs-nav-item ${
              activeTab === "Manage-Services"
                ? "active"
                : ""
            }`}
            onClick={() => {
              setActiveTab("Manage-Services");
              navigate("/admin/manage-services");
            }}
          >
            {!sidebarCollapsed && (
              <span className="cs-nav-label">
                Manage Services
              </span>
            )}
          </button>

          <button
            className={`cs-nav-item ${
              activeTab === "Access-Control"
                ? "active"
                : ""
            }`}
            onClick={() => {
              setActiveTab("Access-Control");
              navigate("/admin/access-control");
            }}
          >
            {!sidebarCollapsed && (
              <span className="cs-nav-label">
                Access Control
              </span>
            )}
          </button>

        </nav>

        <div className="cs-sidebar-footer">

          <button
            className="cs-logout-btn"
            onClick={handleLogoutClick}
          >
            <span className="cs-nav-icon">🚪</span>
            {!sidebarCollapsed && (
              <span className="cs-nav-label">
                Logout
              </span>
            )}
          </button>

        </div>

      </aside>

      {/* Main Panel */}

      <main className="cs-panel">

        {/* Topbar */}

        <header className="cs-topbar">

          <div className="cs-topbar-content">

            <h1 className="cs-page-title">
              Create
            </h1>

            <div className="cs-search-wrapper">

            </div>

            <div className="cs-topbar-actions">

              <button
                className="cs-icon-btn"
                onClick={() =>
                  navigate("/admin/notifications")
                }
              >
                🔔
                <span className="cs-notification-badge">
                  3
                </span>
              </button>

              <div
                className="cs-avatar"
                onClick={() =>
                  navigate("/admin/profile")
                }
                style={{ cursor: "pointer" }}
              >
                👤
              </div>

            </div>

          </div>

        </header>

        {/* Form Area */}

        <section className="cs-form-area">

          <div className="cs-form-card">

            {/* Image Upload */}

            <label
              className="cs-image-upload"
              htmlFor="cs-image-file"
            >
              {imagePreview ? (
                <img
                  src={imagePreview}
                  alt="Service preview"
                />
              ) : null}

              <input
                id="cs-image-file"
                type="file"
                accept="image/*"
                style={{ display: "none" }}
                onChange={handleImageChange}
              />
            </label>

            {/* Form Fields */}

            <div className="cs-form-fields">

              <h2 className="cs-form-title">
                Create Service
              </h2>

              <div className="cs-form-row">

                <input
                  type="text"
                  placeholder="Service Name"
                  className="cs-field-input"
                  value={serviceName}
                  onChange={(e) =>
                    setServiceName(e.target.value)
                  }
                />

                <input
                  type="text"
                  placeholder="Category"
                  className="cs-field-input"
                  value={serviceCategory}
                  onChange={(e) =>
                    setServiceCategory(
                      e.target.value
                    )
                  }
                />

              </div>

              <textarea
                placeholder="Service Description"
                className="cs-textarea-input"
                value={serviceDescription}
                onChange={(e) =>
                  setServiceDescription(
                    e.target.value
                  )
                }
              />

              <div className="cs-form-actions">

                <button
                  className="cs-cancel-btn"
                  onClick={handleCancel}
                >
                  Cancel
                </button>

                <button
                  className="cs-create-btn"
                  onClick={handleCreate}
                >
                  Create
                </button>

              </div>

            </div>

          </div>

        </section>

      </main>

      {/* Logout Modal */}

      {showLogoutModal && (
        <div className="cs-logout-overlay">

          <div className="cs-logout-modal">

            <div className="cs-logout-modal-text">
              Are you sure you want to logout?
            </div>

            <div className="cs-logout-modal-actions">

              <button
                className="cs-logout-confirm-btn"
                onClick={confirmLogout}
              >
                Confirm
              </button>

              <button
                className="cs-logout-cancel-btn"
                onClick={cancelLogout}
              >
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