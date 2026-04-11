import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./Login_js";
import Registration from "./Registration_js";
import Dashboard from "./Dashboard_js";
import Profile from "./Profile";
import MyRatings from "./MyRatings";
import AdminDashboard from "./AdminDashboard";
import CreateService from "./CreateService";
import RateService from "./RateService";
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

        <Route path="/admindashboard" element={<AdminDashboard />} />
        <Route path="/createservice" element={<CreateService />} />
        <Route path="/rate-service/:serviceId" element={<RateService />} />

      </Routes>
    </Router>
  );
}

export default App;