import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import "../css/login_css.css";

function LoginContent() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [errorModalMessage, setErrorModalMessage] = useState("");
  const [successModalMessage, setSuccessModalMessage] = useState("");
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);

  const navigate = useNavigate();

  // Email/Password Login
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    try {
      const response = await fetch("http://localhost:8080/api/auth/admin/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          email: email,
          password: password,
        }),
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
        if (data.role === "ADMIN") {
          navigate("/admindashboard");
        } else {
          navigate("/dashboard");
        }
      }, 2000);

    } catch (err) {
      setErrorMessage("Invalid email or password.");
    }
  };

  // Google Login Handler
  const handleGoogleSuccess = async (credentialResponse) => {
    setIsGoogleLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/auth/google", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          idToken: credentialResponse.credential
        })
      });

      const data = await response.json();

      if (response.status === 403) {
        setErrorModalMessage(data.message || "This email is not registered. Please create an account first.");
        setShowErrorModal(true);
        setIsGoogleLoading(false);
        return;
      }

      if (!response.ok) {
        throw new Error(data.error || "Google login failed");
      }

      setSuccessModalMessage(`Welcome ${data.name || 'User'}! Successfully signed in with Google.`);
      setShowSuccessModal(true);
      
      setTimeout(() => {
        if (data.role === "ADMIN") {
          navigate("/admindashboard");
        } else {
          navigate("/dashboard");
        }
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

  const closeErrorModal = () => {
    setShowErrorModal(false);
    setErrorModalMessage("");
  };

  const closeSuccessModal = () => {
    setShowSuccessModal(false);
    setSuccessModalMessage("");
  };

  return (
    <div className="login-container">
      <form className="login-card" onSubmit={handleSubmit}>
        <h2>Welcome!</h2>
        <p className="subtitle">Sign in to your account</p>

        {successMessage && (
          <div className="success-message">{successMessage}</div>
        )}

        {errorMessage && (
          <div className="error-message">{errorMessage}</div>
        )}

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit">Login</button>

        <div className="divider">
          <span>or</span>
        </div>

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
          <Link to="/register" className="signup-link">
            Sign Up
          </Link>
        </p>
      </form>

      {/* Error Modal */}
      {showErrorModal && (
        <div className="modal-overlay" onClick={closeErrorModal}>
          <div className="modal-content error-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-close-row">
              <button className="modal-x" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-icon-circle error">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 8V12M12 16H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" 
                  stroke="#f87171" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h3 className="modal-title error">Authentication Failed</h3>
            <p className="modal-desc">{errorModalMessage}</p>
            <button onClick={closeErrorModal} className="modal-action-btn error">
              Try Again
            </button>
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
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" 
                  stroke="#10b981" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h3 className="modal-title success">Welcome!</h3>
            <p className="modal-desc">{successModalMessage}</p>
            <button onClick={closeSuccessModal} className="modal-action-btn success">
              Continue
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

// Wrap with GoogleOAuthProvider
function Login() {
  const googleClientId = "253632602120-fh2ff76jia74rc9m62kpvagcn2pmlrf2.apps.googleusercontent.com";
  
  return (
    <GoogleOAuthProvider clientId={googleClientId}>
      <LoginContent />
    </GoogleOAuthProvider>
  );
}

export default Login;