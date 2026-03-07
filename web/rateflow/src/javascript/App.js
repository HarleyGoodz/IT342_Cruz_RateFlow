import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./Login_js";
import Registration from "./Registration_js";
import Dashboard from "./Dashboard_js";
import Profile from "./Profile";
import '../css/App.css';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Registration />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
      </Routes>
    </Router>
  );
}

export default App;