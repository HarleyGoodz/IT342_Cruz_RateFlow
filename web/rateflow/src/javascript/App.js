import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./auth/Login_js";
import Registration from "./auth/Registration_js";
import Dashboard from "./dashboard/Dashboard_js";
import Profile from "./auth/Profile";
import MyRatings from "./ratings/MyRatings";
import AdminDashboard from "./admin/AdminDashboard";
import CreateService from "./admin/CreateService";
import RateService from "./ratings/RateService";
import ManageServices from "./admin/ManageServices";
import AdminRateService from "./admin/AdminRateService";
import AccessControls from "./admin/AccessControls";
import AdminProfile from "./admin/AdminProfile";
import AdminNotifications from "./admin/AdminNotifications";
import UserNotifications from "./notifications/UserNotifications";
import ResetPassword from './auth/ResetPassword';
import '../css/App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/my-ratings" element={<MyRatings />} />
        <Route path="/notifications" element={<UserNotifications />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        <Route path="/admindashboard" element={<AdminDashboard />} />
        <Route path="/createservice" element={<CreateService />} />
        <Route path="/rate-service/:serviceId" element={<RateService />} />
        <Route path="/manageservices" element={<ManageServices />} />
        <Route path="/admin/rateservice/:serviceId" element={<AdminRateService />} />
        <Route path="/access-controls" element={<AccessControls />} />
        <Route path="/admin-profile" element={<AdminProfile />} />
        <Route path="/admin-notifications" element={<AdminNotifications />} />

      </Routes>
    </Router>
  );
}

export default App;