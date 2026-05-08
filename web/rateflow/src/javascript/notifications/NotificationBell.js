// NotificationBell.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function NotificationBell() {
  const [notificationCount, setNotificationCount] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    fetchNotificationCount();
    const interval = setInterval(fetchNotificationCount, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchNotificationCount = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/user-notifications", {
        credentials: "include",
      });
      if (response.ok) {
        const data = await response.json();
        setNotificationCount(data.length);
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    }
  };

  return (
    <div className="usernotif-notification-bell">
      <button 
        className="usernotif-bell-btn"
        onClick={() => navigate('/notifications')}
      >
        🔔
        {notificationCount > 0 && (
          <span className="usernotif-badge">
            {notificationCount > 99 ? '99+' : notificationCount}
          </span>
        )}
      </button>
    </div>
  );
}

export default NotificationBell;