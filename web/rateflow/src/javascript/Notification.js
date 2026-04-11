import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "./supabaseClient";
import "../css/notifications_css.css";

function Notification() {
  const navigate = useNavigate();

  const [sidebarCollapsed, setSidebarCollapsed] =
    useState(false);

  const [user, setUser] = useState(null);

  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const [notifications, setNotifications] =
    useState([
      {
        id: 1,
        message:
          "This is a notification message",
      },
      {
        id: 2,
        message:
          "This is a notification message",
      },
      {
        id: 3,
        message:
          "This is a notification message",
      },
    ]);

  /* SESSION CHECK */

  useEffect(() => {
    let isMounted = true;

    const init = async () => {
      const {
        data: { session },
      } =
        await supabase.auth.getSession();

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

  /* LOGOUT */
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

  /* DELETE ONE */

  const deleteNotification = (id) => {
    setNotifications(
      notifications.filter(
        (n) => n.id !== id
      )
    );
  };

  /* CLEAR ALL */

  const clearAll = () => {
    setNotifications([]);
  };

  return (
    <div className="dashboard-layout">

      {/* SIDEBAR */}

      <aside
        className={`sidebar ${
          sidebarCollapsed
            ? "collapsed"
            : ""
        }`}
      >
        <div className="sidebar-header">

          <div className="logo">
          

            {!sidebarCollapsed && (
              <span className="logo-text">
                Dashboard
              </span>
            )}
          </div>

          <button
            className="collapse-btn"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            ←
          </button>

        </div>

        <nav className="sidebar-nav">

          <button
            className="nav-item"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            <span className="nav-label">
              Services
            </span>
          </button>

          <button
            className="nav-item"
            onClick={() =>
              navigate("/my-ratings")
            }
          >
            <span className="nav-label">
              My Ratings
            </span>
          </button>

        </nav>

        <div className="sidebar-footer">

          <button
            className="logout-sidebar-btn"
            onClick={handleLogoutClick}
          >
            Logout
          </button>

        </div>

      </aside>

      {/* MAIN */}

      <main className="main-content">

        {/* HEADER */}

        <header className="dashboard-header">

          <div className="header-content">

            <div className="notification-header-left">

              <h1 className="page-title">
                Notifications
              </h1>

            </div>

            <div className="header-actions">

              <button className="icon-btn">
                🔔
              </button>

              <div
                className="user-avatar"
                onClick={() =>
                  navigate("/profile")
                }
              >
                👤
              </div>

            </div>

          </div>

        </header>

        {/* CONTENT */}

        <section className="notification-container">

          <div className="notification-top">

            <button
              className="clear-btn"
              onClick={clearAll}
            >
              Clear All
            </button>

          </div>

          {notifications.map(
            (notification) => (

              <div
                key={notification.id}
                className="notification-card"
              >

                <div className="notification-icon">
                  !
                </div>

                <div className="notification-text">
                  {
                    notification.message
                  }
                </div>

                <button
                  className="delete-btn"
                  onClick={() =>
                    deleteNotification(
                      notification.id
                    )
                  }
                >
                  🗑
                </button>

              </div>

            )
          )}

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

export default Notification;