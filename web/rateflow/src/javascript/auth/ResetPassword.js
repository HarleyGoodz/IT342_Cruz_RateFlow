import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../../css/auth/ResetPassword.css';

function ResetPassword() {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [tokenValid, setTokenValid] = useState(true);
  const [validating, setValidating] = useState(true);
  
  const navigate = useNavigate();
  const location = useLocation();
  const token = new URLSearchParams(location.search).get('token');
  
  useEffect(() => {
    validateToken();
  }, [token]);
  
  const validateToken = async () => {
    if (!token) {
      setTokenValid(false);
      setValidating(false);
      return;
    }
    
    try {
      const response = await fetch(`http://localhost:8080/api/auth/validate-reset-token?token=${token}`);
      const data = await response.json();
      
      if (!data.valid) {
        setTokenValid(false);
      }
    } catch (error) {
      setTokenValid(false);
    } finally {
      setValidating(false);
    }
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword: password }),
      });
      
      const data = await response.json();
      
      if (response.ok && data.success) {
        setSuccess(true);
        setTimeout(() => {
          navigate('/');
        }, 3000);
      } else {
        setError(data.message || 'Failed to reset password');
      }
    } catch (error) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  if (validating) {
    return (
      <div className="reset-loading">
        <div className="loading-spinner"></div>
        <p>Validating reset link...</p>
      </div>
    );
  }
  
  if (!tokenValid) {
    return (
      <div className="reset-error-container">
        <div className="reset-error-card">
          <div className="reset-error-icon">⚠️</div>
          <h2>Invalid or Expired Link</h2>
          <p>The password reset link is invalid or has expired.</p>
          <button onClick={() => navigate('/')} className="reset-error-btn">
            Back to Login
          </button>
        </div>
      </div>
    );
  }
  
  if (success) {
    return (
      <div className="reset-success-container">
        <div className="reset-success-card">
          <div className="reset-success-icon">✓</div>
          <h2>Password Reset Successful!</h2>
          <p>Your password has been changed. Redirecting to login...</p>
          <button onClick={() => navigate('/')} className="reset-success-btn">
            Go to Login
          </button>
        </div>
      </div>
    );
  }
  
  return (
    <div className="reset-container">
      <form className="reset-card" onSubmit={handleSubmit}>
        <h2 className="reset-title">Create New Password</h2>
        <p className="reset-subtitle">Enter your new password below</p>
        
        <div className="reset-field-group">
          <label className="reset-field-label">New Password</label>
          <div className="reset-input-wrapper">
            <input
              type="password"
              className="reset-input"
              placeholder="Enter new password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
        </div>
        
        <div className="reset-field-group">
          <label className="reset-field-label">Confirm Password</label>
          <div className="reset-input-wrapper">
            <input
              type="password"
              className="reset-input"
              placeholder="Confirm new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>
        </div>
        
        {error && <div className="reset-error">{error}</div>}
        
        <button type="submit" className="reset-btn" disabled={loading}>
          {loading ? 'Resetting...' : 'Reset Password'}
        </button>
        
        <button type="button" onClick={() => navigate('/')} className="reset-back-btn">
          Back to Login
        </button>
      </form>
    </div>
  );
}

export default ResetPassword;