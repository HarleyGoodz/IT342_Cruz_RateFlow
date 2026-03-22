import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./Login_js";
import Registration from "./Registration_js";
import Dashboard from "./Dashboard_js";
import Profile from "./Profile";
import MyRatings from "./MyRatings";
import Notification from "./Notification";
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
        <Route path="/notifications" element={<Notification />} />
        <Route path="/rate-service" element={<RateService />} />
      </Routes>
    </Router>
  );
}

export default App;