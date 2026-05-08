import { useState } from "react";
import "../../css/auth/registration_css.css";
import { useNavigate } from "react-router-dom";

// Clean SVG eye icons — no emojis, fully styled via CSS
const EyeIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
);

const EyeOffIcon = () => (
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
    <line x1="1" y1="1" x2="23" y2="23" className="eye-slash-line" />
  </svg>
);

function PasswordToggle({ visible, onToggle, label }) {
  return (
    <button
      type="button"
      className={`password-toggle${visible ? " visible" : ""}`}
      onClick={onToggle}
      aria-label={visible ? `Hide ${label}` : `Show ${label}`}
    >
      {visible ? <EyeIcon /> : <EyeOffIcon />}
    </button>
  );
}

function Registration() {
  const navigate = useNavigate();

  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

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
      {/* Back button */}
      <div className="back-button-container">
        <button
          className="back-button"
          onClick={() => navigate("/")}
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

        {/* Password field */}
        <div className="input-group password-group">
          <input
            type={showPassword ? "text" : "password"}
            name="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
          />
          <PasswordToggle
            visible={showPassword}
            onToggle={() => setShowPassword((v) => !v)}
            label="password"
          />
        </div>

        {/* Confirm Password field */}
        <div className="input-group password-group">
          <input
            type={showConfirmPassword ? "text" : "password"}
            name="confirmPassword"
            placeholder="Confirm Password"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
          />
          <PasswordToggle
            visible={showConfirmPassword}
            onToggle={() => setShowConfirmPassword((v) => !v)}
            label="confirm password"
          />
        </div>

        <button type="submit">Register</button>
      </form>
    </div>
  );
}

export default Registration;