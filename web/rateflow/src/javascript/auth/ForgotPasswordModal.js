import React, { useState } from 'react';
import '../../css/ForgotPasswordModal.css';

function ForgotPasswordModal({ isOpen, onClose }) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });
      
      const data = await response.json();
      
      if (response.ok && data.success) {
        setSubmitted(true);
        setSuccessMessage(data.message);
        setTimeout(() => {
          onClose();
          setSubmitted(false);
          setEmail('');
        }, 3000);
      } else {
        setError(data.message || 'Something went wrong. Please try again.');
      }
    } catch (error) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="forgot-modal-overlay" onClick={onClose}>
      <div className="forgot-modal-container" onClick={(e) => e.stopPropagation()}>
        <button className="forgot-modal-close" onClick={onClose}>×</button>
        
        {!submitted ? (
          <>
            <div className="forgot-modal-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                <path d="M12 8V12M12 16H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" 
                  stroke="#38bdf8" strokeWidth="2" strokeLinecap="round"/>
              </svg>
            </div>
            <h2 className="forgot-modal-title">Forgot Password?</h2>
            <p className="forgot-modal-subtitle">
              Enter your email address and we'll send you a link to reset your password.
            </p>
            
            <form onSubmit={handleSubmit} className="forgot-modal-form">
              <div className="forgot-form-group">
                <label className="forgot-form-label">Email Address</label>
                <div className="forgot-input-wrapper">
                  <svg className="forgot-input-icon" viewBox="0 0 16 16" fill="none">
                    <rect x="1" y="3" width="14" height="10" rx="2"/>
                    <path d="M1 5.5l7 4.5 7-4.5"/>
                  </svg>
                  <input
                    type="email"
                    className="forgot-form-input"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>
              
              {error && <div className="forgot-error-message">{error}</div>}
              
              <button 
                type="submit" 
                className="forgot-submit-btn"
                disabled={loading}
              >
                {loading ? 'Sending...' : 'Send Reset Link'}
              </button>
            </form>
          </>
        ) : (
          <div className="forgot-success-content">
            <div className="forgot-success-icon">✓</div>
            <h2 className="forgot-success-title">Check Your Email</h2>
            <p className="forgot-success-message">{successMessage}</p>
            <p className="forgot-success-hint">Redirecting to login...</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default ForgotPasswordModal;