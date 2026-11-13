// Authentication Module
class AuthManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.tokenKey = 'jwt_token';
        this.userKey = 'exam_user';
        this.init();
    }

    init() {
        const form = document.getElementById('loginForm');
        if (form) {
            form.addEventListener('submit', (e) => this.handleLogin(e));
        }
    }

    async handleLogin(e) {
        e.preventDefault();
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const submitBtn = e.target.querySelector('button[type="submit"]');
        const errorDiv = document.getElementById('errorMessage');

        console.log('Attempting login for:', username);

        if (!username || !password) {
            this.showError('Please enter both username and password');
            return;
        }

        submitBtn.classList.add('loading');
        errorDiv.classList.add('hidden');

        try {
            const response = await fetch(`${this.apiBaseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            console.log('Login response status:', response.status);

            if (!response.ok) {
                const errorBody = await response.text();
                console.error('Login failed:', errorBody);
                throw new Error('Invalid credentials (Status: ' + response.status + ')');
            }

            const data = await response.json();
            console.log('Login successful, token received:', data.token ? 'YES' : 'NO');
            
            this.setToken(data.token);
            this.setUser(data.user);
            
            window.location.href = 'dashboard.html';
        } catch (error) {
            console.error('Login error:', error);
            this.showError(error.message || 'Login failed. Please try again.');
            submitBtn.classList.remove('loading');
        }
    }

    showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        errorDiv.textContent = message;
        errorDiv.classList.remove('hidden');
    }

    setToken(token) {
        localStorage.setItem(this.tokenKey, token);
    }

    getToken() {
        return localStorage.getItem(this.tokenKey);
    }

    setUser(user) {
        localStorage.setItem(this.userKey, JSON.stringify(user));
    }

    getUser() {
        const user = localStorage.getItem(this.userKey);
        return user ? JSON.parse(user) : null;
    }

    clearAuth() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.userKey);
    }

    isAuthenticated() {
        return !!this.getToken();
    }

    logout() {
        this.clearAuth();
        window.location.href = 'login.html';
    }

    async makeAuthenticatedRequest(url, options = {}) {
        const token = this.getToken();
        if (!token) {
            throw new Error('Not authenticated');
        }

        const headers = {
            'Authorization': `Bearer ${token}`,
            ...options.headers
        };

        return fetch(url, { ...options, headers });
    }
}

// Initialize
const authManager = new AuthManager();
