package com.swnih.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Dashboard - SWNIH</title>
                
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #f8fafc; color: #1e293b; line-height: 1.6;
                    }
                    
                    /* Navigation */
                    .navbar {
                        background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        padding: 1rem 0; position: fixed; top: 0; left: 0; right: 0; z-index: 1000;
                    }
                    .nav-container {
                        max-width: 1200px; margin: 0 auto; padding: 0 2rem;
                        display: flex; justify-content: space-between; align-items: center;
                    }
                    .brand-logo { display: flex; align-items: center; gap: 0.75rem; color: #1e293b; }
                    .logo-wrapper {
                        width: 40px; height: 40px; background: linear-gradient(135deg, #3b82f6, #1d4ed8);
                        border-radius: 8px; display: flex; align-items: center; justify-content: center;
                        color: white; font-size: 1.25rem; font-weight: bold;
                    }
                    .brand-name { font-size: 1.5rem; font-weight: 700; }
                    .nav-user { display: flex; align-items: center; gap: 1rem; }
                    .user-info { display: flex; align-items: center; gap: 0.5rem; }
                    .user-avatar {
                        width: 36px; height: 36px; background: linear-gradient(135deg, #3b82f6, #1d4ed8);
                        border-radius: 50%; display: flex; align-items: center; justify-content: center;
                        color: white; font-size: 0.875rem; font-weight: bold;
                    }
                    .btn {
                        padding: 0.5rem 1rem; border: none; border-radius: 6px; font-weight: 600;
                        cursor: pointer; transition: all 0.2s; display: inline-flex;
                        align-items: center; gap: 0.5rem; font-size: 0.875rem;
                    }
                    .btn-primary { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; }
                    .btn-outline { background: transparent; border: 2px solid #e2e8f0; color: #64748b; }
                    .btn-ghost { background: transparent; color: #64748b; }
                    .btn:hover { transform: translateY(-1px); }
                    
                    /* Main Content */
                    .main-content { padding-top: 80px; min-height: 100vh; }
                    .dashboard-container { max-width: 1400px; margin: 0 auto; padding: 2rem; }
                    
                    /* Dashboard Header */
                    .dashboard-header {
                        background: white; border-radius: 16px; padding: 2rem; margin-bottom: 2rem;
                        box-shadow: 0 1px 3px rgba(0,0,0,0.1); border: 1px solid #e2e8f0;
                    }
                    .header-content {
                        display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;
                    }
                    .header-info h1 { font-size: 2rem; font-weight: 800; margin-bottom: 0.5rem; }
                    .header-info p { color: #64748b; font-size: 1.125rem; }
                    .username { color: #3b82f6; font-weight: 700; }
                    .header-actions { display: flex; gap: 1rem; }
                    
                    /* Stats Grid */
                    .header-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
                    .quick-stat {
                        display: flex; align-items: center; gap: 1rem; padding: 1rem;
                        background: #f8fafc; border-radius: 12px; border: 1px solid #e2e8f0;
                    }
                    .stat-icon {
                        width: 48px; height: 48px; background: linear-gradient(135deg, #3b82f6, #1d4ed8);
                        border-radius: 12px; display: flex; align-items: center; justify-content: center;
                        color: white; font-size: 1.25rem;
                    }
                    .stat-data { flex: 1; }
                    .stat-number { font-size: 1.5rem; font-weight: 800; line-height: 1; }
                    .stat-label { font-size: 0.875rem; color: #64748b; font-weight: 500; }
                    
                    /* Priority Dashboard */
                    .priority-dashboard {
                        display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
                        gap: 1.5rem; margin-top: 2rem;
                    }
                    .priority-column {
                        background: white; border-radius: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        overflow: hidden; border: 1px solid #e2e8f0;
                    }
                    .column-header {
                        padding: 1.5rem; display: flex; align-items: center; gap: 1rem;
                        color: white; font-weight: 700;
                    }
                    .high-priority .column-header { background: linear-gradient(135deg, #ef4444, #dc2626); }
                    .medium-priority .column-header { background: linear-gradient(135deg, #f59e0b, #d97706); }
                    .low-priority .column-header { background: linear-gradient(135deg, #10b981, #059669); }
                    .header-icon {
                        width: 40px; height: 40px; background: rgba(255, 255, 255, 0.2);
                        border-radius: 8px; display: flex; align-items: center; justify-content: center;
                    }
                    .column-title { font-size: 1.125rem; font-weight: 800; }
                    .column-count {
                        display: inline-flex; align-items: center; justify-content: center;
                        min-width: 24px; height: 24px; background: rgba(255, 255, 255, 0.2);
                        border-radius: 50%; font-size: 0.875rem; font-weight: 800; margin-left: auto;
                    }
                    .message-list { max-height: 400px; overflow-y: auto; padding: 1rem; }
                    .empty-state {
                        display: flex; flex-direction: column; align-items: center;
                        justify-content: center; padding: 3rem 2rem; text-align: center;
                    }
                    .empty-icon {
                        width: 64px; height: 64px; background: #f1f5f9; border-radius: 50%;
                        display: flex; align-items: center; justify-content: center;
                        font-size: 1.5rem; color: #94a3b8; margin-bottom: 1rem;
                    }
                    .message-item {
                        background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;
                        padding: 1rem; margin-bottom: 0.75rem; cursor: pointer; transition: all 0.2s;
                    }
                    .message-item:hover { background: white; border-color: #cbd5e1; }
                    .message-header {
                        display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;
                    }
                    .message-sender { font-size: 0.875rem; font-weight: 700; color: #374151; }
                    .message-time { font-size: 0.75rem; color: #94a3b8; font-weight: 500; }
                    .message-subject { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 0.5rem; }
                    .message-preview { font-size: 0.875rem; color: #64748b; line-height: 1.4; }
                    
                    /* Responsive */
                    @media (max-width: 768px) {
                        .header-content { flex-direction: column; gap: 1.5rem; text-align: center; }
                        .header-stats { grid-template-columns: 1fr; }
                        .priority-dashboard { grid-template-columns: 1fr; }
                        .nav-container { padding: 0 1rem; }
                    }
                </style>
            </head>
            <body>
                <!-- Navigation -->
                <nav class="navbar">
                    <div class="nav-container">
                        <div class="brand-logo">
                            <div class="logo-wrapper">🧠</div>
                            <div>
                                <div class="brand-name">SWNIH</div>
                                <div style="font-size: 0.75rem; color: #64748b;">Smart Intelligence Hub</div>
                            </div>
                        </div>
                        
                        <div class="nav-user">
                            <div class="user-info">
                                <div class="user-avatar" id="user-initial">U</div>
                                <div>
                                    <div style="font-weight: 600;" id="user-display-name">User</div>
                                    <div style="font-size: 0.75rem; color: #10b981;">Online</div>
                                </div>
                            </div>
                            <button class="btn btn-ghost" onclick="logout()">🚪 Logout</button>
                        </div>
                    </div>
                </nav>

                <!-- Main Content -->
                <main class="main-content">
                    <div class="dashboard-container">
                        <!-- Dashboard Header -->
                        <div class="dashboard-header">
                            <div class="header-content">
                                <div class="header-info">
                                    <h1>Welcome back, <span class="username" id="dashboard-username">User</span>!</h1>
                                    <p>Here's your intelligent email overview</p>
                                </div>
                                <div class="header-actions">
                                    <button class="btn btn-outline" onclick="connectGmail()">
                                        📧 Connect Gmail
                                    </button>
                                    <button class="btn btn-primary" onclick="refreshMessages()">
                                        🔄 Refresh
                                    </button>
                                </div>
                            </div>
                            
                            <!-- Quick Stats -->
                            <div class="header-stats">
                                <div class="quick-stat">
                                    <div class="stat-icon">📧</div>
                                    <div class="stat-data">
                                        <div class="stat-number" id="total-count">0</div>
                                        <div class="stat-label">Total Messages</div>
                                    </div>
                                </div>
                                <div class="quick-stat">
                                    <div class="stat-icon">⏰</div>
                                    <div class="stat-data">
                                        <div class="stat-number">2.5h</div>
                                        <div class="stat-label">Time Saved</div>
                                    </div>
                                </div>
                                <div class="quick-stat">
                                    <div class="stat-icon">📊</div>
                                    <div class="stat-data">
                                        <div class="stat-number">94%</div>
                                        <div class="stat-label">Accuracy</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Priority Dashboard -->
                        <div class="priority-dashboard">
                            <!-- High Priority Column -->
                            <div class="priority-column high-priority">
                                <div class="column-header">
                                    <div class="header-icon">⚠️</div>
                                    <div class="column-title">High Priority</div>
                                    <div class="column-count" id="high-priority-count">0</div>
                                </div>
                                <div class="message-list" id="high-messages">
                                    <div class="empty-state">
                                        <div class="empty-icon">📥</div>
                                        <div>
                                            <h4>No High Priority Messages</h4>
                                            <p>You're all caught up!</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Medium Priority Column -->
                            <div class="priority-column medium-priority">
                                <div class="column-header">
                                    <div class="header-icon">⏰</div>
                                    <div class="column-title">Medium Priority</div>
                                    <div class="column-count" id="medium-priority-count">0</div>
                                </div>
                                <div class="message-list" id="medium-messages">
                                    <div class="empty-state">
                                        <div class="empty-icon">📥</div>
                                        <div>
                                            <h4>No Medium Priority Messages</h4>
                                            <p>Nothing urgent to review</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Low Priority Column -->
                            <div class="priority-column low-priority">
                                <div class="column-header">
                                    <div class="header-icon">ℹ️</div>
                                    <div class="column-title">Low Priority</div>
                                    <div class="column-count" id="low-priority-count">0</div>
                                </div>
                                <div class="message-list" id="low-messages">
                                    <div class="empty-state">
                                        <div class="empty-icon">📥</div>
                                        <div>
                                            <h4>No Low Priority Messages</h4>
                                            <p>Clean inbox!</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
                
                <script>
                    console.log('Dashboard loaded');
                    
                    // Check authentication
                    const token = localStorage.getItem('swnih_token');
                    const user = localStorage.getItem('swnih_user');
                    
                    if (!token || !user) {
                        alert('Please login first');
                        window.location.href = '/login';
                    } else {
                        const userData = JSON.parse(user);
                        document.getElementById('dashboard-username').textContent = userData.username;
                        document.getElementById('user-display-name').textContent = userData.username;
                        document.getElementById('user-initial').textContent = userData.username.charAt(0).toUpperCase();
                        
                        // Check for OAuth callback parameters
                        handleOAuthCallback();
                        
                        // Load messages on page load
                        loadMessages();
                    }
                    
                    function handleOAuthCallback() {
                        const urlParams = new URLSearchParams(window.location.search);
                        const gmailCode = urlParams.get('gmail_code');
                        const gmailError = urlParams.get('gmail_error');
                        
                        if (gmailError) {
                            alert('Gmail connection failed: ' + gmailError);
                            window.history.replaceState({}, document.title, '/dashboard');
                            return;
                        }
                        
                        if (gmailCode) {
                            console.log('Processing Gmail OAuth callback...');
                            exchangeGmailCode(gmailCode);
                        }
                    }
                    
                    async function exchangeGmailCode(code) {
                        try {
                            const response = await fetch('/api/gmail/exchange-code', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Authorization': 'Bearer ' + token
                                },
                                body: JSON.stringify({ code: code })
                            });
                            
                            const data = await response.json();
                            
                            if (data.success) {
                                alert('Gmail connected successfully! Loading your messages...');
                                window.history.replaceState({}, document.title, '/dashboard');
                                loadMessages();
                            } else {
                                alert('Failed to connect Gmail: ' + data.message);
                                window.history.replaceState({}, document.title, '/dashboard');
                            }
                        } catch (error) {
                            console.error('Error exchanging Gmail code:', error);
                            alert('Error connecting Gmail: ' + error.message);
                            window.history.replaceState({}, document.title, '/dashboard');
                        }
                    }
                    
                    function logout() {
                        localStorage.removeItem('swnih_token');
                        localStorage.removeItem('swnih_user');
                        alert('Logged out successfully');
                        window.location.href = '/';
                    }
                    
                    async function connectGmail() {
                        try {
                            const response = await fetch('/api/gmail/connect', {
                                headers: { 'Authorization': 'Bearer ' + token }
                            });
                            
                            if (response.status === 401) {
                                localStorage.removeItem('swnih_token');
                                localStorage.removeItem('swnih_user');
                                alert('Session expired. Please login again.');
                                window.location.href = '/login';
                                return;
                            }
                            
                            const data = await response.json();
                            
                            if (data.success && data.authorizationUrl) {
                                window.location.href = data.authorizationUrl;
                            } else if (data.alreadyConnected) {
                                alert('Gmail is already connected!');
                                loadMessages();
                            } else {
                                alert('Error: ' + (data.message || 'Failed to connect Gmail'));
                            }
                        } catch (error) {
                            alert('Error connecting Gmail: ' + error.message);
                        }
                    }
                    
                    async function refreshMessages() {
                        await loadMessages();
                        alert('Messages refreshed!');
                    }
                    
                    async function loadMessages() {
                        try {
                            showLoadingState();
                            
                            const statusResponse = await fetch('/api/gmail/status', {
                                headers: { 'Authorization': 'Bearer ' + token }
                            });
                            
                            if (statusResponse.status === 401) {
                                localStorage.removeItem('swnih_token');
                                localStorage.removeItem('swnih_user');
                                alert('Session expired. Please login again.');
                                window.location.href = '/login';
                                return;
                            }
                            
                            const statusData = await statusResponse.json();
                            
                            if (statusData.connected) {
                                console.log('Gmail is connected, fetching real messages...');
                                const response = await fetch('/api/gmail/messages', {
                                    headers: { 'Authorization': 'Bearer ' + token }
                                });
                                
                                if (response.status === 401) {
                                    localStorage.removeItem('swnih_token');
                                    localStorage.removeItem('swnih_user');
                                    alert('Session expired. Please login again.');
                                    window.location.href = '/login';
                                    return;
                                }
                                
                                const data = await response.json();
                                
                                if (data.success && data.messages) {
                                    console.log('Loaded', data.messages.length, 'Gmail messages');
                                    displayMessages(data.messages);
                                } else {
                                    console.log('No Gmail messages, showing demo data');
                                    showDemoMessages();
                                }
                            } else {
                                console.log('Gmail not connected, showing demo data');
                                showDemoMessages();
                            }
                        } catch (error) {
                            console.error('Error loading messages:', error);
                            showDemoMessages();
                        }
                    }
                    
                    function showLoadingState() {
                        ['high', 'medium', 'low'].forEach(priority => {
                            const container = document.getElementById(priority + '-messages');
                            container.innerHTML = '<div class="empty-state"><div class="empty-icon">⏳</div><div><h4>Loading Messages...</h4><p>Fetching your emails</p></div></div>';
                        });
                    }
                    
                    function showDemoMessages() {
                        const now = Date.now();
                        const demoMessages = [
                            {
                                id: 1, priority: 'HIGH', sender: 'hr@techcorp.com',
                                subject: 'Interview Invitation - Senior Developer Position',
                                body: 'We would like to invite you for a technical interview tomorrow at 2 PM...',
                                timestamp: new Date(now - 2 * 60 * 60 * 1000)
                            },
                            {
                                id: 2, priority: 'MEDIUM', sender: 'team@company.com',
                                subject: 'Weekly Team Meeting Reminder',
                                body: 'Don\\'t forget about our weekly standup meeting tomorrow at 10 AM...',
                                timestamp: new Date(now - 6 * 60 * 60 * 1000)
                            },
                            {
                                id: 3, priority: 'LOW', sender: 'newsletter@techblog.com',
                                subject: 'Weekly Tech Newsletter - Latest Trends',
                                body: 'Check out the latest trends in web development and AI...',
                                timestamp: new Date(now - 12 * 60 * 60 * 1000)
                            }
                        ];
                        
                        displayMessages(demoMessages);
                    }
                    
                    function displayMessages(messages) {
                        const highMessages = messages.filter(m => m.priority === 'HIGH');
                        const mediumMessages = messages.filter(m => m.priority === 'MEDIUM');
                        const lowMessages = messages.filter(m => m.priority === 'LOW');
                        
                        renderMessageList('high', highMessages);
                        renderMessageList('medium', mediumMessages);
                        renderMessageList('low', lowMessages);
                        
                        document.getElementById('high-priority-count').textContent = highMessages.length;
                        document.getElementById('medium-priority-count').textContent = mediumMessages.length;
                        document.getElementById('low-priority-count').textContent = lowMessages.length;
                        document.getElementById('total-count').textContent = messages.length;
                    }
                    
                    function renderMessageList(priority, messages) {
                        const container = document.getElementById(priority + '-messages');
                        
                        if (messages.length === 0) {
                            container.innerHTML = '<div class="empty-state"><div class="empty-icon">📥</div><div><h4>No ' + priority.charAt(0).toUpperCase() + priority.slice(1) + ' Priority Messages</h4><p>You\\'re all caught up!</p></div></div>';
                            return;
                        }
                        
                        container.innerHTML = messages.map(message => 
                            '<div class="message-item" onclick="showMessageDetail(' + message.id + ')"><div class="message-header"><div class="message-sender">' + escapeHtml(message.sender) + '</div><div class="message-time">' + formatTime(message.timestamp) + '</div></div><div class="message-subject">' + escapeHtml(message.subject) + '</div><div class="message-preview">' + escapeHtml(message.body) + '</div></div>'
                        ).join('');
                    }
                    
                    function showMessageDetail(messageId) {
                        alert('Message detail view - ID: ' + messageId);
                    }
                    
                    function formatTime(timestamp) {
                        const now = new Date();
                        const date = new Date(timestamp);
                        const diff = now - date;
                        const minutes = Math.floor(diff / (1000 * 60));
                        const hours = Math.floor(diff / (1000 * 60 * 60));
                        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
                        
                        if (minutes < 1) return 'Just now';
                        if (minutes < 60) return minutes + 'm ago';
                        if (hours < 24) return hours + 'h ago';
                        if (days < 7) return days + 'd ago';
                        
                        return date.toLocaleDateString();
                    }
                    
                    function escapeHtml(text) {
                        const div = document.createElement('div');
                        div.textContent = text;
                        return div.innerHTML;
                    }
                </script>
            </body>
            </html>
            """;
    }
}