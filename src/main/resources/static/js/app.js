// Smart Web Notification Intelligence Hub - Frontend JavaScript

// Global state
let currentUser = null;
let authToken = null;

// DOM Elements
const landingSection = document.getElementById('landing-section');
const authSection = document.getElementById('auth-section');
const dashboardSection = document.getElementById('dashboard-section');
const navActions = document.getElementById('nav-actions');
const navUser = document.getElementById('nav-user');
const loadingOverlay = document.getElementById('loading-overlay');
const toastContainer = document.getElementById('toast-container');

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    // Check for OAuth callback parameters
    const urlParams = new URLSearchParams(window.location.search);
    
    // Handle authorization code from OAuth callback
    if (urlParams.has('gmail_code')) {
        const code = urlParams.get('gmail_code');
        const state = urlParams.get('gmail_state');
        
        console.log('Received authorization code from OAuth callback');
        
        // Clean up URL first
        window.history.replaceState({}, document.title, window.location.pathname);
        
        // Check if user is logged in
        const savedToken = localStorage.getItem('swnih_token');
        const savedUser = localStorage.getItem('swnih_user');
        
        if (savedToken && savedUser) {
            authToken = savedToken;
            currentUser = JSON.parse(savedUser);
            
            // Exchange the authorization code for tokens
            exchangeAuthorizationCode(code, state);
            showDashboard();
        } else {
            showToast('Please login first to complete Gmail connection', 'warning');
            showLanding();
        }
        return;
    }
    
    // Handle other OAuth callback parameters
    if (urlParams.has('gmail_connected')) {
        const connected = urlParams.get('gmail_connected') === 'true';
        const message = urlParams.get('message') || urlParams.get('error');
        
        if (connected) {
            showToast('Gmail connected successfully!', 'success');
        } else {
            showToast('Gmail connection failed: ' + message, 'error');
        }
        
        // Clean up URL
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    if (urlParams.has('gmail_error')) {
        const error = urlParams.get('gmail_error');
        showToast('Gmail connection error: ' + error, 'error');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    // Check for existing auth token
    const savedToken = localStorage.getItem('swnih_token');
    const savedUser = localStorage.getItem('swnih_user');
    
    if (savedToken && savedUser) {
        authToken = savedToken;
        currentUser = JSON.parse(savedUser);
        showDashboard();
    } else {
        showLanding();
    }
});

// Function to exchange authorization code for tokens
async function exchangeAuthorizationCode(code, state) {
    try {
        showLoading(true);
        console.log('Exchanging authorization code for tokens...');
        
        const response = await apiCall('/api/gmail/exchange-code', {
            method: 'POST',
            body: JSON.stringify({
                code: code,
                state: state
            })
        });
        
        console.log('Code exchange response:', response);
        
        if (response.success) {
            showToast('Gmail connected successfully!', 'success');
            // Reload messages to show real Gmail data
            loadMessages();
        } else {
            showToast('Failed to connect Gmail: ' + response.message, 'error');
        }
        
    } catch (error) {
        console.error('Error exchanging authorization code:', error);
        showToast('Error connecting Gmail: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

// New functions for professional UI
function showProfile() {
    showToast('Profile settings coming soon!', 'info');
}

function showSettings() {
    showToast('Settings panel coming soon!', 'info');
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const button = input.parentElement.querySelector('.input-action i');
    
    if (input.type === 'password') {
        input.type = 'text';
        button.className = 'fas fa-eye-slash';
    } else {
        input.type = 'password';
        button.className = 'fas fa-eye';
    }
}

function filterMessages(filter) {
    showToast(`Filtering messages by: ${filter}`, 'info');
    // Implementation for filtering messages
}

function markAllRead(priority) {
    showToast(`Marked all ${priority} priority messages as read`, 'success');
    // Implementation for marking messages as read
}

// Mobile menu toggle functionality
function toggleMobileMenu() {
    const navMenu = document.querySelector('.nav-menu');
    const mobileToggle = document.querySelector('.mobile-menu-toggle');
    
    navMenu.classList.toggle('active');
    mobileToggle.classList.toggle('active');
    
    // Toggle hamburger animation
    const spans = mobileToggle.querySelectorAll('span');
    spans.forEach((span, index) => {
        if (navMenu.classList.contains('active')) {
            if (index === 0) span.style.transform = 'rotate(45deg) translate(5px, 5px)';
            if (index === 1) span.style.opacity = '0';
            if (index === 2) span.style.transform = 'rotate(-45deg) translate(7px, -6px)';
        } else {
            span.style.transform = '';
            span.style.opacity = '';
        }
    });
}

// Close mobile menu when clicking outside
document.addEventListener('click', function(event) {
    const navMenu = document.querySelector('.nav-menu');
    const mobileToggle = document.querySelector('.mobile-menu-toggle');
    
    if (navMenu && navMenu.classList.contains('active') && 
        !navMenu.contains(event.target) && 
        !mobileToggle.contains(event.target)) {
        toggleMobileMenu();
    }
});

// Enhanced password strength checker
function checkPasswordStrength(password) {
    const strengthBar = document.querySelector('.strength-fill');
    const strengthText = document.querySelector('.strength-text');
    
    if (!strengthBar || !strengthText) return;
    
    let strength = 0;
    let strengthLabel = 'Very Weak';
    let color = '#ef4444';
    
    // Length check
    if (password.length >= 8) strength += 25;
    if (password.length >= 12) strength += 10;
    
    // Character variety checks
    if (/[a-z]/.test(password)) strength += 15;
    if (/[A-Z]/.test(password)) strength += 15;
    if (/[0-9]/.test(password)) strength += 15;
    if (/[^A-Za-z0-9]/.test(password)) strength += 20;
    
    // Determine strength label and color
    if (strength >= 80) {
        strengthLabel = 'Very Strong';
        color = '#10b981';
    } else if (strength >= 60) {
        strengthLabel = 'Strong';
        color = '#059669';
    } else if (strength >= 40) {
        strengthLabel = 'Medium';
        color = '#f59e0b';
    } else if (strength >= 20) {
        strengthLabel = 'Weak';
        color = '#f97316';
    }
    
    strengthBar.style.width = `${Math.min(strength, 100)}%`;
    strengthBar.style.background = color;
    strengthText.textContent = `Password strength: ${strengthLabel}`;
    strengthText.style.color = color;
}

// Add event listener for password strength checking
document.addEventListener('DOMContentLoaded', function() {
    const passwordInput = document.getElementById('register-password');
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            checkPasswordStrength(this.value);
        });
    }
});

// Navigation functions
function showLanding() {
    hideAllSections();
    landingSection.style.display = 'block';
    navActions.style.display = 'flex';
    navUser.style.display = 'none';
}

function showLogin() {
    hideAllSections();
    authSection.style.display = 'block';
    switchTab('login');
    navActions.style.display = 'none';
    navUser.style.display = 'none';
}

function showRegister() {
    hideAllSections();
    authSection.style.display = 'block';
    switchTab('register');
    navActions.style.display = 'none';
    navUser.style.display = 'none';
}

function showDashboard() {
    hideAllSections();
    dashboardSection.style.display = 'block';
    navActions.style.display = 'none';
    navUser.style.display = 'flex';
    
    if (currentUser) {
        document.getElementById('user-display-name').textContent = currentUser.username;
        document.getElementById('dashboard-username').textContent = currentUser.username;
    }
    
    // Load dashboard data
    loadMessages();
}

function showDemo() {
    showToast('Demo feature coming soon!', 'info');
}

function hideAllSections() {
    landingSection.style.display = 'none';
    authSection.style.display = 'none';
    dashboardSection.style.display = 'none';
}

// Tab switching
function switchTab(tab) {
    const loginTab = document.getElementById('login-tab');
    const registerTab = document.getElementById('register-tab');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    
    if (tab === 'login') {
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
    } else {
        registerTab.classList.add('active');
        loginTab.classList.remove('active');
        registerForm.style.display = 'block';
        loginForm.style.display = 'none';
    }
}

// Authentication functions
async function handleLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    
    if (!email || !password) {
        showToast('Please fill in all fields', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
            authToken = data.token;
            currentUser = {
                id: data.userId,
                username: data.username,
                email: data.email
            };
            
            // Save to localStorage
            localStorage.setItem('swnih_token', authToken);
            localStorage.setItem('swnih_user', JSON.stringify(currentUser));
            
            showToast('Login successful!', 'success');
            showDashboard();
        } else {
            showToast(data.message || 'Login failed', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showToast('Network error. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    
    if (!username || !email || !password) {
        showToast('Please fill in all fields', 'error');
        return;
    }
    
    // Basic password validation
    if (password.length < 8) {
        showToast('Password must be at least 8 characters long', 'error');
        return;
    }
    
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password)) {
        showToast('Password must contain uppercase, lowercase, and number', 'error');
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('Registration successful! Please login.', 'success');
            switchTab('login');
            
            // Pre-fill login form
            document.getElementById('login-email').value = email;
        } else {
            showToast(data.message || 'Registration failed', 'error');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showToast('Network error. Please try again.', 'error');
    } finally {
        showLoading(false);
    }
}

function logout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('swnih_token');
    localStorage.removeItem('swnih_user');
    
    showToast('Logged out successfully', 'success');
    showLanding();
}

// Dashboard functions
async function loadMessages() {
    if (!authToken) return;
    
    try {
        // First check if Gmail is connected
        const statusResponse = await apiCall('/api/gmail/status');
        
        if (statusResponse.connected) {
            // Load real messages from Gmail
            await loadRealMessages();
        } else {
            // Show demo data and prompt to connect Gmail
            showDemoMessages();
            showToast('Connect Gmail to see your real messages', 'info');
        }
        
        updateStats();
    } catch (error) {
        console.error('Error loading messages:', error);
        showToast('Error loading messages', 'error');
        // Fallback to demo data
        showDemoMessages();
        updateStats();
    }
}

async function loadRealMessages() {
    try {
        showLoading(true);
        
        // This endpoint will be implemented to fetch real Gmail messages
        const response = await apiCall('/api/gmail/messages');
        
        if (response.success && response.messages) {
            renderRealMessages(response.messages);
            showToast('Messages loaded from Gmail!', 'success');
        } else {
            throw new Error('Failed to load Gmail messages');
        }
    } catch (error) {
        console.error('Error loading real messages:', error);
        showToast('Error loading Gmail messages, showing demo data', 'warning');
        showDemoMessages();
    } finally {
        showLoading(false);
    }
}

function renderRealMessages(messages) {
    const priorityGroups = {
        high: messages.filter(m => m.priority === 'HIGH'),
        medium: messages.filter(m => m.priority === 'MEDIUM'),
        low: messages.filter(m => m.priority === 'LOW')
    };
    
    // Render messages for each priority
    renderMessages('high', priorityGroups.high);
    renderMessages('medium', priorityGroups.medium);
    renderMessages('low', priorityGroups.low);
}

function showDemoMessages() {
    const demoMessages = {
        high: [
            {
                id: 1,
                sender: 'hr@techcorp.com',
                subject: 'Interview Invitation - Senior Developer Position',
                body: 'We would like to invite you for a technical interview tomorrow at 2 PM...',
                timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000) // 2 hours ago
            },
            {
                id: 2,
                sender: 'admissions@university.edu',
                subject: 'Urgent: Final Exam Schedule Change',
                body: 'Important update regarding your final examination schedule...',
                timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000) // 4 hours ago
            }
        ],
        medium: [
            {
                id: 3,
                sender: 'team@company.com',
                subject: 'Weekly Team Meeting Reminder',
                body: 'Don\'t forget about our weekly standup meeting tomorrow at 10 AM...',
                timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000) // 6 hours ago
            },
            {
                id: 4,
                sender: 'calendar@google.com',
                subject: 'Event Reminder: Project Deadline',
                body: 'Your project deadline is approaching in 2 days...',
                timestamp: new Date(Date.now() - 8 * 60 * 60 * 1000) // 8 hours ago
            }
        ],
        low: [
            {
                id: 5,
                sender: 'newsletter@techblog.com',
                subject: 'Weekly Tech Newsletter - Latest Trends',
                body: 'Check out the latest trends in web development and AI...',
                timestamp: new Date(Date.now() - 12 * 60 * 60 * 1000) // 12 hours ago
            },
            {
                id: 6,
                sender: 'deals@shopping.com',
                subject: '50% Off Sale - Limited Time Offer',
                body: 'Don\'t miss out on our biggest sale of the year...',
                timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000) // 1 day ago
            }
        ]
    };
    
    // Render messages for each priority
    renderMessages('high', demoMessages.high);
    renderMessages('medium', demoMessages.medium);
    renderMessages('low', demoMessages.low);
}

function renderMessages(priority, messages) {
    const container = document.getElementById(`${priority}-messages`);
    const countElement = document.getElementById(`${priority}-priority-count`);
    
    if (messages.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>No ${priority} priority messages</p>
            </div>
        `;
        countElement.textContent = '0';
        return;
    }
    
    countElement.textContent = messages.length;
    
    container.innerHTML = messages.map(message => `
        <div class="message-item" onclick="showMessageDetail(${message.id})">
            <div class="message-header">
                <div class="message-sender">${message.sender}</div>
                <div class="message-time">${formatTime(message.timestamp)}</div>
            </div>
            <div class="message-subject">${message.subject}</div>
            <div class="message-preview">${message.body}</div>
        </div>
    `).join('');
}

function updateStats() {
    const highCount = document.querySelectorAll('#high-messages .message-item').length;
    const mediumCount = document.querySelectorAll('#medium-messages .message-item').length;
    const lowCount = document.querySelectorAll('#low-messages .message-item').length;
    const totalCount = highCount + mediumCount + lowCount;
    
    // Update main stats
    document.getElementById('high-count').textContent = highCount;
    document.getElementById('medium-count').textContent = mediumCount;
    document.getElementById('low-count').textContent = lowCount;
    document.getElementById('total-count').textContent = totalCount;
    
    // Update additional stats for professional UI
    const totalMessagesCount = document.getElementById('total-messages-count');
    if (totalMessagesCount) {
        totalMessagesCount.textContent = totalCount;
    }
}

function showMessageDetail(messageId) {
    showToast('Message detail view coming soon!', 'info');
}

async function connectGmail() {
    if (!authToken) {
        showToast('Please login first', 'error');
        return;
    }
    
    try {
        showLoading(true);
        console.log('Starting Gmail connection with token:', authToken ? 'present' : 'missing');
        
        // Check current status first
        console.log('Checking Gmail status...');
        const statusResponse = await apiCall('/api/gmail/status');
        console.log('Gmail status response:', statusResponse);
        
        if (statusResponse.error === 'UNAUTHORIZED') {
            console.error('Unauthorized error during status check');
            return; // Already handled by apiCall
        }
        
        if (statusResponse.connected) {
            showToast('Gmail is already connected!', 'info');
            loadMessages(); // Refresh messages
            return;
        }
        
        // Initiate OAuth flow
        console.log('Initiating OAuth flow...');
        const response = await apiCall('/api/gmail/connect');
        console.log('Gmail connect response:', response);
        
        if (response.success && response.authorizationUrl) {
            console.log('Got authorization URL, redirecting...');
            showToast('Redirecting to Google for authorization...', 'info');
            
            // Redirect to Google OAuth in the same window
            window.location.href = response.authorizationUrl;
            
        } else if (response.alreadyConnected) {
            showToast('Gmail is already connected!', 'info');
            loadMessages();
        } else {
            console.error('Failed to get authorization URL:', response);
            // Check if it's a configuration issue
            if (response.message && response.message.includes('client')) {
                showToast('Gmail OAuth is not configured. Please check GMAIL_SETUP.md for setup instructions.', 'info');
            } else {
                throw new Error(response.message || 'Failed to initiate Gmail connection');
            }
        }
        
    } catch (error) {
        console.error('Gmail connection error:', error);
        showToast('Gmail connection error: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

function refreshMessages() {
    showLoading(true);
    setTimeout(() => {
        loadMessages();
        showLoading(false);
        showToast('Messages refreshed!', 'success');
    }, 1000);
}

// Utility functions
function showLoading(show) {
    loadingOverlay.style.display = show ? 'flex' : 'none';
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = getToastIcon(type);
    toast.innerHTML = `
        <i class="${icon}"></i>
        <span>${message}</span>
    `;
    
    toastContainer.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 5000);
    
    // Remove on click
    toast.addEventListener('click', () => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    });
}

function getToastIcon(type) {
    switch (type) {
        case 'success': return 'fas fa-check-circle';
        case 'error': return 'fas fa-exclamation-circle';
        case 'warning': return 'fas fa-exclamation-triangle';
        default: return 'fas fa-info-circle';
    }
}

function formatTime(timestamp) {
    const now = new Date();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (minutes < 60) {
        return `${minutes}m ago`;
    } else if (hours < 24) {
        return `${hours}h ago`;
    } else {
        return `${days}d ago`;
    }
}

// API helper function
async function apiCall(endpoint, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...(authToken && { 'Authorization': `Bearer ${authToken}` })
        }
    };
    
    try {
        console.log(`Making API call to ${endpoint} with token:`, authToken ? 'present' : 'missing');
        
        const response = await fetch(endpoint, { ...defaultOptions, ...options });
        
        console.log(`API response status for ${endpoint}:`, response.status);
        
        // Handle authentication errors
        if (response.status === 401) {
            console.error('Authentication failed for endpoint:', endpoint);
            showToast('Session expired. Please login again.', 'warning');
            logout();
            return { success: false, error: 'UNAUTHORIZED' };
        }
        
        const data = await response.json();
        console.log(`API response data for ${endpoint}:`, data);
        
        return data;
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}