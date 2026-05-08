import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import ForgotPasswordModal from "./ForgotPasswordModal";
import "../../css/auth/login_css.css";

// Clean SVG eye icons — same as registration
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

function LoginContent() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [errorModalMessage, setErrorModalMessage] = useState("");
  const [successModalMessage, setSuccessModalMessage] = useState("");
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);
  
  // Add state for password visibility
  const [showPassword, setShowPassword] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const response = await fetch("http://localhost:8080/api/auth/admin/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error);
      }

      const data = await response.json();
      console.log(data);

      setSuccessModalMessage("Welcome back! You've successfully logged in.");
      setShowSuccessModal(true);

      setTimeout(() => {
        if (data.role === "ADMIN") navigate("/admindashboard");
        else navigate("/dashboard");
      }, 2000);

    } catch (err) {
      setErrorMessage("Invalid email or password.");
    }
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    setIsGoogleLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/auth/google", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ idToken: credentialResponse.credential }),
      });

      const data = await response.json();

      if (response.status === 403) {
        setErrorModalMessage(data.message || "This email is not registered. Please create an account first.");
        setShowErrorModal(true);
        setIsGoogleLoading(false);
        return;
      }

      if (!response.ok) throw new Error(data.error || "Google login failed");

      setSuccessModalMessage(`Welcome ${data.name || 'User'}! Successfully signed in with Google.`);
      setShowSuccessModal(true);

      setTimeout(() => {
        if (data.role === "ADMIN") navigate("/admindashboard");
        else navigate("/dashboard");
      }, 2000);

    } catch (error) {
      console.error("Google login error:", error);
      setErrorModalMessage("Google login failed. Please try again.");
      setShowErrorModal(true);
      setIsGoogleLoading(false);
    }
  };

  const handleGoogleError = () => {
    setErrorModalMessage("Google login failed. Please try again or use email login.");
    setShowErrorModal(true);
  };

  const closeErrorModal = () => { setShowErrorModal(false); setErrorModalMessage(""); };
  const closeSuccessModal = () => { setShowSuccessModal(false); setSuccessModalMessage(""); };
  

  return (
    <div className="login-container">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="App-Title">
          <h2>Welcome!</h2>
          <label className="login-field-label">Sign in to your account</label>
        </div>

        {successMessage && <div className="success-message">{successMessage}</div>}
        {errorMessage && <div className="error-message">{errorMessage}</div>}

        {/* Email field */}
        <div className="login-field-group">
          <label className="login-field-label">Email address</label>
          <div className="login-field-wrap">
            <svg className="login-field-icon" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.3">
              <rect x="1" y="3" width="14" height="10" rx="2"/><path d="M1 5.5l7 4.5 7-4.5"/>
            </svg>
            <input
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
        </div>

        {/* Password field with toggle */}
        <div className="login-field-group">
          <label className="login-field-label">Password</label>
          <div className="login-field-wrap password-group">
            <svg className="login-field-icon" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.3">
              <rect x="3" y="7" width="10" height="7" rx="1.5"/><path d="M5 7V5a3 3 0 016 0v2"/>
              <circle cx="8" cy="10.5" r="1" fill="currentColor"/>
            </svg>
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <PasswordToggle
              visible={showPassword}
              onToggle={() => setShowPassword(v => !v)}
              label="password"
            />
          </div>
        </div>

        <a 
          href="#" 
          className="login-forgot" 
          onClick={(e) => {
            e.preventDefault();
            setShowForgotPassword(true);
          }}
        >
          Forgot password?
        </a>

        <button type="submit">Sign in</button>

        <div className="divider"><span>or continue with</span></div>

        <div className={`google-login-wrapper ${isGoogleLoading ? 'loading' : ''}`}>
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={handleGoogleError}
            useOneTap
            size="large"
            width="100%"
            text="signin_with"
            shape="rectangular"
            logo_alignment="center"
            locale="en"
          />
        </div>

        <p className="signup-text">
          Don't have an account?{" "}
          <Link to="/register" className="signup-link">Sign up</Link>
        </p>
      </form>

      {/* Forgot Password Modal */}
      <ForgotPasswordModal 
        isOpen={showForgotPassword}
        onClose={() => setShowForgotPassword(false)}
      />

      {/* Error Modal */}
      {showErrorModal && (
        <div className="modal-overlay" onClick={closeErrorModal}>
          <div className="modal-content error-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-close-row">
              <button className="modal-x" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-icon-circle error">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <path d="M12 8V12M12 16H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z"
                  stroke="#f87171" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h3 className="modal-title error">Authentication Failed</h3>
            <p className="modal-desc">{errorModalMessage}</p>
            <button onClick={closeErrorModal} className="modal-action-btn error">Try Again</button>
          </div>
        </div>
      )}

      {/* Success Modal */}
      {showSuccessModal && (
        <div className="modal-overlay" onClick={closeSuccessModal}>
          <div className="modal-content success-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-close-row">
              <button className="modal-x" onClick={closeSuccessModal}>×</button>
            </div>
            <div className="modal-icon-circle success">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z"
                  stroke="#10b981" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h3 className="modal-title success">Welcome!</h3>
            <p className="modal-desc">{successModalMessage}</p>
            <button onClick={closeSuccessModal} className="modal-action-btn success">Continue</button>
          </div>
        </div>
      )}
    </div>
  );
}

function Login() {
  const googleClientId = "253632602120-fh2ff76jia74rc9m62kpvagcn2pmlrf2.apps.googleusercontent.com";
  return (
    <GoogleOAuthProvider clientId={googleClientId}>
      <LoginContent />
    </GoogleOAuthProvider>
  );
}

export default Login;