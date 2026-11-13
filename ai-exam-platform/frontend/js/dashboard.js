// Dashboard Module
class DashboardManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.authManager = new AuthManager();
        this.init();
    }

    init() {
        console.log('Dashboard initializing...');
        this.checkAuth();
        this.loadUserInfo();
        this.attachEventListeners();
    }

    checkAuth() {
        if (!this.authManager.isAuthenticated()) {
            console.log('Not authenticated, redirecting to login');
            window.location.href = 'login.html';
            return;
        }
        console.log('Authenticated as:', this.authManager.getUser()?.username);
    }

    loadUserInfo() {
        const user = this.authManager.getUser();
        if (user) {
            document.getElementById('candidateName').textContent = user.username;
            document.getElementById('welcomeName').textContent = user.username;
        }
    }

    attachEventListeners() {
        const startBtn = document.getElementById('startExamBtn');
        const logoutBtn = document.getElementById('logoutBtn');

        console.log('Start button found:', !!startBtn);

        if (startBtn) {
            startBtn.addEventListener('click', (e) => {
                console.log('Start Exam clicked!');
                e.preventDefault();
                this.startExam();
            });
        } else {
            console.error('ERROR: startExamBtn not found in DOM!');
        }

        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.authManager.logout());
        }
    }

    async startExam() {
        console.log('Starting exam...');
        try {
            const response = await this.authManager.makeAuthenticatedRequest(
                `${this.apiBaseUrl}/exam/start`,
                { method: 'POST' }
            );

            console.log('Response status:', response.status);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server error:', errorText);
                throw new Error('Failed to start exam: ' + errorText);
            }

            const data = await response.json();
            console.log('Exam session created:', data);
            
            localStorage.setItem('exam_session', JSON.stringify(data));
            localStorage.setItem('exam_start_time', Date.now().toString());
            
            window.location.href = 'exam.html';
        } catch (error) {
            console.error('Error starting exam:', error);
            alert('Error starting exam: ' + error.message);
        }
    }
}

// Initialize
const dashboardManager = new DashboardManager();
