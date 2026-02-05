package com.swnih.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthPageController {

    @GetMapping("/login")
    @ResponseBody
    public String login() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Login - SWNIH</title>
                
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white; min-height: 100vh; display: flex;
                        align-items: center; justify-content: center; padding: 20px;
                    }
                    .auth-container {
                        background: rgba(255,255,255,0.1); backdrop-filter: blur(15px);
                        border-radius: 20px; padding: 40px; width: 100%; max-width: 400px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.3);
                    }
                    .auth-header { text-align: center; margin-bottom: 30px; }
                    .auth-header h1 { font-size: 2.5rem; margin-bottom: 10px; }
                    .form-group { margin-bottom: 20px; }
                    .form-label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 0.9rem; }
                    .form-input {
                        width: 100%; padding: 12px 16px; border: 2px solid rgba(255,255,255,0.3);
                        border-radius: 10px; background: rgba(255,255,255,0.1); color: white;
                        font-size: 16px; transition: all 0.3s;
                    }
                    .form-input:focus { outline: none; border-color: #ff6b6b; background: rgba(255,255,255,0.2); }
                    .form-input::placeholder { color: rgba(255,255,255,0.7); }
                    .btn {
                        width: 100%; padding: 15px; border: none; border-radius: 10px;
                        background: linear-gradient(45deg, #ff6b6b, #ee5a24); color: white;
                        font-size: 16px; font-weight: bold; cursor: pointer; transition: all 0.3s;
                    }
                    .btn:hover { transform: translateY(-2px); box-shadow: 0 10px 20px rgba(0,0,0,0.3); }
                    .auth-links { text-align: center; margin-top: 20px; }
                    .auth-links a { color: #ffeb3b; text-decoration: none; font-weight: 500; }
                    .back-btn {
                        position: absolute; top: 20px; left: 20px; background: rgba(255,255,255,0.2);
                        border: none; color: white; padding: 10px 15px; border-radius: 10px;
                        cursor: pointer; font-size: 14px;
                    }
                    .error-message, .success-message {
                        padding: 10px; border-radius: 8px; margin-bottom: 15px; display: none;
                    }
                    .error-message { background: rgba(255,0,0,0.2); border: 1px solid rgba(255,0,0,0.5); color: #ffcccb; }
                    .success-message { background: rgba(0,255,0,0.2); border: 1px solid rgba(0,255,0,0.5); color: #90ee90; }
                </style>
            </head>
            <body>
                <button class="back-btn" onclick="window.location.href='/'">← Back</button>
                
                <div class="auth-container">
                    <div class="auth-header">
                        <h1>🧠 Login</h1>
                        <p>Welcome back to SWNIH</p>
                    </div>
                    
                    <div id="error-message" class="error-message"></div>
                    <div id="success-message" class="success-message"></div>
                    
                    <form onsubmit="handleLogin(event)">
                        <div class="form-group">
                            <label class="form-label">📧 Email</label>
                            <input type="email" id="email" class="form-input" placeholder="Enter email" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">🔒 Password</label>
                            <input type="password" id="password" class="form-input" placeholder="Enter password" required>
                        </div>
                        <button type="submit" class="btn" id="login-btn">🚀 Sign In</button>
                    </form>
                    
                    <div class="auth-links">
                        <p>No account? <a href="/register">Create one</a></p>
                    </div>
                </div>
                
                <script>
                    function showError(msg) {
                        document.getElementById('error-message').textContent = msg;
                        document.getElementById('error-message').style.display = 'block';
                        document.getElementById('success-message').style.display = 'none';
                    }
                    function showSuccess(msg) {
                        document.getElementById('success-message').textContent = msg;
                        document.getElementById('success-message').style.display = 'block';
                        document.getElementById('error-message').style.display = 'none';
                    }
                    async function handleLogin(event) {
                        event.preventDefault();
                        const email = document.getElementById('email').value;
                        const password = document.getElementById('password').value;
                        const btn = document.getElementById('login-btn');
                        
                        btn.textContent = '⏳ Signing in...';
                        btn.disabled = true;
                        
                        try {
                            const response = await fetch('/api/auth/login', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ email, password })
                            });
                            const data = await response.json();
                            
                            if (data.success) {
                                localStorage.setItem('swnih_token', data.token);
                                localStorage.setItem('swnih_user', JSON.stringify({
                                    id: data.userId, username: data.username, email: data.email
                                }));
                                showSuccess('Login successful! Redirecting...');
                                setTimeout(() => window.location.href = '/dashboard', 1500);
                            } else {
                                showError(data.message || 'Login failed');
                            }
                        } catch (error) {
                            showError('Network error. Try again.');
                        } finally {
                            btn.textContent = '🚀 Sign In';
                            btn.disabled = false;
                        }
                    }
                </script>
            </body>
            </html>
            """;
    }

    @GetMapping("/register")
    @ResponseBody
    public String register() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Register - SWNIH</title>
                
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white; min-height: 100vh; display: flex;
                        align-items: center; justify-content: center; padding: 20px;
                    }
                    .auth-container {
                        background: rgba(255,255,255,0.1); backdrop-filter: blur(15px);
                        border-radius: 20px; padding: 40px; width: 100%; max-width: 400px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.3);
                    }
                    .auth-header { text-align: center; margin-bottom: 30px; }
                    .auth-header h1 { font-size: 2.5rem; margin-bottom: 10px; }
                    .form-group { margin-bottom: 20px; }
                    .form-label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 0.9rem; }
                    .form-input {
                        width: 100%; padding: 12px 16px; border: 2px solid rgba(255,255,255,0.3);
                        border-radius: 10px; background: rgba(255,255,255,0.1); color: white;
                        font-size: 16px; transition: all 0.3s;
                    }
                    .form-input:focus { outline: none; border-color: #ff6b6b; background: rgba(255,255,255,0.2); }
                    .form-input::placeholder { color: rgba(255,255,255,0.7); }
                    .btn {
                        width: 100%; padding: 15px; border: none; border-radius: 10px;
                        background: linear-gradient(45deg, #ff6b6b, #ee5a24); color: white;
                        font-size: 16px; font-weight: bold; cursor: pointer; transition: all 0.3s;
                    }
                    .btn:hover { transform: translateY(-2px); box-shadow: 0 10px 20px rgba(0,0,0,0.3); }
                    .auth-links { text-align: center; margin-top: 20px; }
                    .auth-links a { color: #ffeb3b; text-decoration: none; font-weight: 500; }
                    .back-btn {
                        position: absolute; top: 20px; left: 20px; background: rgba(255,255,255,0.2);
                        border: none; color: white; padding: 10px 15px; border-radius: 10px;
                        cursor: pointer; font-size: 14px;
                    }
                    .error-message, .success-message {
                        padding: 10px; border-radius: 8px; margin-bottom: 15px; display: none;
                    }
                    .error-message { background: rgba(255,0,0,0.2); border: 1px solid rgba(255,0,0,0.5); color: #ffcccb; }
                    .success-message { background: rgba(0,255,0,0.2); border: 1px solid rgba(0,255,0,0.5); color: #90ee90; }
                </style>
            </head>
            <body>
                <button class="back-btn" onclick="window.location.href='/'">← Back</button>
                
                <div class="auth-container">
                    <div class="auth-header">
                        <h1>🚀 Register</h1>
                        <p>Join SWNIH today</p>
                    </div>
                    
                    <div id="error-message" class="error-message"></div>
                    <div id="success-message" class="success-message"></div>
                    
                    <form onsubmit="handleRegister(event)">
                        <div class="form-group">
                            <label class="form-label">👤 Username</label>
                            <input type="text" id="username" class="form-input" placeholder="Choose username" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">📧 Email</label>
                            <input type="email" id="email" class="form-input" placeholder="Enter email" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">🔒 Password</label>
                            <input type="password" id="password" class="form-input" placeholder="Create password" required>
                        </div>
                        <button type="submit" class="btn" id="register-btn">🎉 Create Account</button>
                    </form>
                    
                    <div class="auth-links">
                        <p>Have account? <a href="/login">Sign in</a></p>
                    </div>
                </div>
                
                <script>
                    function showError(msg) {
                        document.getElementById('error-message').textContent = msg;
                        document.getElementById('error-message').style.display = 'block';
                        document.getElementById('success-message').style.display = 'none';
                    }
                    function showSuccess(msg) {
                        document.getElementById('success-message').textContent = msg;
                        document.getElementById('success-message').style.display = 'block';
                        document.getElementById('error-message').style.display = 'none';
                    }
                    async function handleRegister(event) {
                        event.preventDefault();
                        const username = document.getElementById('username').value;
                        const email = document.getElementById('email').value;
                        const password = document.getElementById('password').value;
                        const btn = document.getElementById('register-btn');
                        
                        if (password.length < 8) {
                            showError('Password must be at least 8 characters');
                            return;
                        }
                        
                        btn.textContent = '⏳ Creating...';
                        btn.disabled = true;
                        
                        try {
                            const response = await fetch('/api/auth/register', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ username, email, password })
                            });
                            const data = await response.json();
                            
                            if (data.success) {
                                showSuccess('Account created! Redirecting...');
                                setTimeout(() => window.location.href = '/login', 2000);
                            } else {
                                showError(data.message || 'Registration failed');
                            }
                        } catch (error) {
                            showError('Network error. Try again.');
                        } finally {
                            btn.textContent = '🎉 Create Account';
                            btn.disabled = false;
                        }
                    }
                </script>
            </body>
            </html>
            """;
    }
}