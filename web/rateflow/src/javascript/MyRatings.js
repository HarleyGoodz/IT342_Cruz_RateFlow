    import React, { useState, useEffect } from "react";
    import { useNavigate } from "react-router-dom";
    import { supabase } from "./supabaseClient";
    import "../css/myRatings_css.css";

    function MyRatings() {
    const navigate = useNavigate();

    const [sidebarCollapsed, setSidebarCollapsed] =
        useState(false);

    /* FIXED */
    const [activeTab, setActiveTab] =
        useState("My Ratings");

    const [user, setUser] = useState(null);

    const [showLogoutModal, setShowLogoutModal] =
    useState(false);

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
                session.user.user_metadata?.full_name ||
                session.user.email,
            });
        }
        };

        init();

        const { data: listener } =
        supabase.auth.onAuthStateChange(
            (event, session) => {
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
        credentials: "include"
      }
    );

    if (!response.ok) {
      alert("Logout failed");
      return;
    }

    // Clear local state
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

        {/* SIDEBAR */}

        <aside
            className={`sidebar ${
            sidebarCollapsed ? "collapsed" : ""
            }`}
        >
            <div className="sidebar-header">

            <div className="logo">
                <span className="logo-icon">
                ⚡
                </span>

                {!sidebarCollapsed && (
                <span className="logo-text">
                    Dashboard
                </span>
                )}
            </div>
            

            </div>

            {/* NAVIGATION */}

            <nav className="sidebar-nav">

            {/* SERVICES */}

            <button
                className={`nav-item ${
                activeTab === "Services"
                    ? "active"
                    : ""
                }`}
                onClick={() => {
                setActiveTab("Services");
                navigate("/dashboard");
                }}
            >
                {!sidebarCollapsed && (
                <span className="nav-label">
                    Services
                </span>
                )}
            </button>

            {/* MY RATINGS */}

            <button
                className={`nav-item ${
                activeTab === "My Ratings"
                    ? "active"
                    : ""
                }`}
                onClick={() => {
                setActiveTab("My Ratings");
                }}
            >
                {!sidebarCollapsed && (
                <span className="nav-label">
                    My Ratings
                </span>
                )}
            </button>

            </nav>

            {/* LOGOUT */}

            <div className="sidebar-footer">

            <button
                className="logout-sidebar-btn"
                onClick={handleLogoutClick}
            >


                {!sidebarCollapsed && (
                <span className="nav-label">
                    Logout
                </span>
                )}
            </button>

            </div>

        </aside>

        {/* MAIN CONTENT */}

        <main className="main-content">

            {/* HEADER */}

            <header className="dashboard-header">

            <div className="header-content">

                {/* LEFT */}

                <h1 className="page-title">
                My Ratings
                </h1>

                {/* CENTER SEARCH */}

                <div className="header-search">

                <input
                    type="text"
                    placeholder="Search ratings..."
                    className="search-input"
                />

                </div>

                {/* RIGHT */}

                <div className="header-actions">

                <button
                className="notifications-btn"
                onClick={() => navigate("/notifications")}
                >
    🔔
                </button>

                <div
                    className="user-avatar"
                    onClick={() =>
                    navigate("/profile")
                    }
                    style={{
                    cursor: "pointer",
                    }}
                >
                👤
                </div>

                </div>

            </div>

            </header>

            {/* CONTENT */}

            <section
            style={{
                padding: "40px",
                fontSize: "18px",
            }}
            >
            Your ratings will appear here.
            </section>

        </main>

        {/* LOGOUT MODAL */}

      {showLogoutModal && (
        <div className="logout-modal-overlay">

          <div className="logout-modal">

            <div className="logout-text">
              Are you sure you want to logout?
            </div>

            <div className="logout-buttons">

              <button
                className="confirm-btn"
                onClick={confirmLogout}
              >
                Confirm
              </button>

              <button
                className="cancel-btn"
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

    export default MyRatings;