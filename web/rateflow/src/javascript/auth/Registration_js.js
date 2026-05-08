import { useState } from "react";
import "../../css/auth/registration_css.css";
import { useNavigate } from "react-router-dom";

function Registration() {
  const navigate = useNavigate();

  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setErrorMessage("");
    setSuccessMessage("");

    if (formData.password !== formData.confirmPassword) {
      setErrorMessage("Passwords do not match.");
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          fName: formData.firstName,
          lName: formData.lastName,
          username: formData.username,
          email: formData.email,
          password: formData.password,
        }),
      });

      if (!response.ok) {
        throw new Error("Email already created!");
      }

      const data = await response.json();
      console.log(data);

      setSuccessMessage("Registration successful! Redirecting to login...");

      setTimeout(() => {
        navigate("/");
      }, 2000);

    } catch (err) {
      setErrorMessage("Email has already been created!");
    }
  };
  

  return (
    <div className="register-container">
      {/* Back button - positioned at upper left */}
    <div className="back-button-container">
      <button 
        className="back-button" 
        onClick={() => navigate('/')}
        aria-label="Go back to login"
      >
        <span className="back-arrow">←</span>
        <span>Back</span>
      </button>
    </div>

      <form className="register-card" onSubmit={handleSubmit}>
        <h2>Create Account</h2>
        <p className="subtitle">Join us and get started</p>

        {successMessage && (
          <div className="success-message">{successMessage}</div>
        )}

        {errorMessage && (
          <div className="error-message">{errorMessage}</div>
        )}

        <div className="input-group">
          <input
            type="text"
            name="username"
            placeholder="Username"
            value={formData.username}
            onChange={handleChange}
            required
          />
        </div>

        <div className="input-group">
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>

        <div className="input-group">
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>

        <div className="input-group">
          <input
            type="password"
            name="confirmPassword"
            placeholder="Confirm Password"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
          />
        </div>

        <button type="submit">Register</button>
      </form>
    </div>
  );
}

export default Registration;