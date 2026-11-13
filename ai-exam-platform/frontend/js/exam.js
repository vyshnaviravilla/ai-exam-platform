// Exam Module - Main Controller for Secure Exam
class ExamManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.aiServiceUrl = 'http://localhost:8000';
        this.authManager = new AuthManager();
        
        this.examSession = null;
        this.currentQuestion = 0;
        this.questions = [];
        this.mcqQuestions = [];
        this.userCode = {};
        this.mcqAnswers = {}; // Store MCQ answers
        this.examMode = 'coding'; // 'coding' or 'mcq'
        this.violations = [];
        this.riskScore = 0;
        this.timeRemaining = 90 * 60; // 90 minutes
        this.timerInterval = null;
        this.proctoringInterval = null;
        this.videoStream = null;
        this.screenStream = null;
        this.isFullscreen = false;
        this.monacoEditor = null;
        this.violationLimit = 10; // Auto-submit after 10 critical violations (was 5)
        this.faceNotDetectedStart = null; // Track when face first not detected
        this.faceDetectionTimeout = 30000; // 30 seconds grace period
        this.isSubmitting = false; // Flag to prevent violations during submission
        this.init();
    }

    async init() {
        this.checkAuth();
        await this.loadExamData();
        // Skip security check - go directly to device check
        this.showDeviceCheckPrompt();
    }

    checkAuth() {
        if (!this.authManager.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }
        
        const session = localStorage.getItem('exam_session');
        if (!session) {
            alert('No active exam session');
            window.location.href = 'dashboard.html';
            return;
        }
        
        this.examSession = JSON.parse(session);
    }

    async loadExamData() {
        try {
            const response = await this.authManager.makeAuthenticatedRequest(
                `${this.apiBaseUrl}/exam/questions`
            );
            
            if (!response.ok) throw new Error('Failed to load questions');
            
            this.questions = await response.json();
        } catch (error) {
            console.error('Error loading exam:', error);
            this.questions = this.getDefaultQuestions();
        }
        
        // Load MCQs (default for now)
        this.mcqQuestions = this.getDefaultMCQs();
        console.log('Loaded', this.questions.length, 'coding questions and', this.mcqQuestions.length, 'MCQs');
    }

    getDefaultQuestions() {
        return [
            {
                id: 1,
                title: "Two Sum",
                difficulty: "Medium",
                description: `Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.

You may assume that each input would have exactly one solution, and you may not use the same element twice.

You can return the answer in any order.`,
                examples: [
                    {
                        input: "nums = [2,7,11,15], target = 9",
                        output: "[0,1]",
                        explanation: "Because nums[0] + nums[1] == 9, we return [0, 1]."
                    },
                    {
                        input: "nums = [3,2,4], target = 6",
                        output: "[1,2]"
                    }
                ],
                constraints: [
                    "2 <= nums.length <= 10^4",
                    "-10^9 <= nums[i] <= 10^9",
                    "-10^9 <= target <= 10^9",
                    "Only one valid answer exists."
                ],
                starterCode: `public class Solution {
    public int[] twoSum(int[] nums, int target) {
        // Write your code here
        
    }
}`,
                testCases: [
                    { input: "[2,7,11,15], 9", expected: "[0,1]" },
                    { input: "[3,2,4], 6", expected: "[1,2]" },
                    { input: "[3,3], 6", expected: "[0,1]" }
                ]
            },
            {
                id: 2,
                title: "Reverse Linked List",
                difficulty: "Medium",
                description: `Given the head of a singly linked list, reverse the list, and return the reversed list.

A linked list can be reversed either iteratively or recursively.`,
                examples: [
                    {
                        input: "head = [1,2,3,4,5]",
                        output: "[5,4,3,2,1]"
                    },
                    {
                        input: "head = [1,2]",
                        output: "[2,1]"
                    }
                ],
                constraints: [
                    "The number of nodes in the list is the range [0, 5000]",
                    "-5000 <= Node.val <= 5000"
                ],
                starterCode: `/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
public class Solution {
    public ListNode reverseList(ListNode head) {
        // Write your code here
        
    }
}`,
                testCases: [
                    { input: "[1,2,3,4,5]", expected: "[5,4,3,2,1]" },
                    { input: "[1,2]", expected: "[2,1]" },
                    { input: "[]", expected: "[]" }
                ]
            }
        ];
    }

    getDefaultMCQs() {
        return [
            {
                id: 'mcq1',
                question: "What is the time complexity of binary search?",
                options: ["O(1)", "O(log n)", "O(n)", "O(n log n)"],
                correct: 1
            },
            {
                id: 'mcq2',
                question: "Which data structure uses LIFO (Last In First Out)?",
                options: ["Queue", "Stack", "Array", "Linked List"],
                correct: 1
            },
            {
                id: 'mcq3',
                question: "What is the default value of an int variable in Java?",
                options: ["0", "null", "undefined", "-1"],
                correct: 0
            },
            {
                id: 'mcq4',
                question: "Which keyword is used to prevent method overriding?",
                options: ["static", "private", "final", "abstract"],
                correct: 2
            },
            {
                id: 'mcq5',
                question: "What does JVM stand for?",
                options: ["Java Virtual Machine", "Java Variable Memory", "Java Visual Mode", "Java Version Manager"],
                correct: 0
            },
            {
                id: 'mcq6',
                question: "Which collection class allows you to store key-value pairs?",
                options: ["ArrayList", "LinkedList", "HashMap", "TreeSet"],
                correct: 2
            },
            {
                id: 'mcq7',
                question: "What is the size of int data type in Java?",
                options: ["16 bit", "32 bit", "64 bit", "8 bit"],
                correct: 1
            },
            {
                id: 'mcq8',
                question: "Which exception is thrown when array element is accessed with negative index?",
                options: ["NullPointerException", "ArrayIndexOutOfBoundsException", "NegativeArraySizeException", "IllegalArgumentException"],
                correct: 1
            },
            {
                id: 'mcq9',
                question: "What is the purpose of 'finally' block in Java?",
                options: ["Execute code when exception occurs", "Execute code regardless of exception", "Handle specific exceptions", "Skip exception handling"],
                correct: 1
            },
            {
                id: 'mcq10',
                question: "Which loop is guaranteed to execute at least once?",
                options: ["for loop", "while loop", "do-while loop", "for-each loop"],
                correct: 2
            },
            {
                id: 'mcq11',
                question: "What is the parent class of all Java classes?",
                options: ["Class", "Main", "Object", "Super"],
                correct: 2
            },
            {
                id: 'mcq12',
                question: "Which operator is used for string concatenation in Java?",
                options: ["+", "&", "||", "."],
                correct: 0
            },
            {
                id: 'mcq13',
                question: "What is the return type of main() method?",
                options: ["int", "void", "String", "boolean"],
                correct: 1
            },
            {
                id: 'mcq14',
                question: "Which package is automatically imported in all Java programs?",
                options: ["java.util", "java.io", "java.lang", "java.net"],
                correct: 2
            },
            {
                id: 'mcq15',
                question: "What is the correct syntax to create a new object in Java?",
                options: ["ClassName obj;", "ClassName obj = new ClassName();", "ClassName obj = ClassName();", "new ClassName obj;"],
                correct: 1
            }
        ];
    }

    async showSecurityCheck() {
        console.log('Showing security check');
        const prompt = document.getElementById('securityCheckPrompt');
        const statusDiv = document.getElementById('securityScanStatus');
        const blockedAppsDiv = document.getElementById('blockedAppsList');
        const appsList = document.getElementById('appsList');
        const continueBtn = document.getElementById('continueToExamBtn');
        const recheckBtn = document.getElementById('recheckBtn');
        
        prompt.classList.remove('hidden');
        
        // Scan for suspicious processes
        const scan = async () => {
            statusDiv.classList.remove('hidden');
            blockedAppsDiv.classList.add('hidden');
            continueBtn.classList.add('hidden');
            
            try {
                const response = await this.authManager.makeAuthenticatedRequest(
                    `${this.apiBaseUrl}/monitoring/processes`
                );
                
                if (response.ok) {
                    const data = await response.json();
                    
                    if (data.suspicious_found && data.processes.length > 0) {
                        // Show blocked apps
                        appsList.innerHTML = data.processes.map(proc => 
                            `<li><strong>${proc}</strong> <button onclick="examManager.terminateApp('${proc}')" class="btn btn-danger btn-small">Kill</button></li>`
                        ).join('');
                        blockedAppsDiv.classList.remove('hidden');
                        statusDiv.classList.add('hidden');
                    } else {
                        // All clear - show continue button
                        statusDiv.innerHTML = '<p class="success">✓ No suspicious apps detected</p>';
                        setTimeout(() => {
                            continueBtn.classList.remove('hidden');
                        }, 1000);
                    }
                }
            } catch (error) {
                console.error('Security scan error:', error);
                statusDiv.innerHTML = '<p class="warning">⚠ Could not scan processes. Continue with caution.</p>';
                continueBtn.classList.remove('hidden');
            }
        };
        
        // Initial scan
        await scan();
        
        // Recheck button
        recheckBtn?.addEventListener('click', scan);
        
        // Continue button - now goes to device check
        continueBtn?.addEventListener('click', () => {
            prompt.classList.add('hidden');
            this.showDeviceCheckPrompt();
        });
    }
    
    async terminateApp(processName) {
        try {
            const response = await this.authManager.makeAuthenticatedRequest(
                `${this.apiBaseUrl}/monitoring/terminate`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ processName })
                }
            );
            
            if (response.ok) {
                alert(`Terminated: ${processName}`);
                // Rescan
                this.showSecurityCheck();
            } else {
                alert('Failed to terminate. Please close manually.');
            }
        } catch (error) {
            alert('Could not terminate app. Please close it manually.');
        }
    }

    showFullscreenPrompt() {
        console.log('Showing fullscreen prompt');
        const prompt = document.getElementById('fullscreenPrompt');
        const enterBtn = document.getElementById('enterFullscreenBtn');
        
        if (!enterBtn) {
            console.error('ERROR: enterFullscreenBtn not found!');
            return;
        }
        
        // Remove any existing listeners to avoid duplicates
        enterBtn.replaceWith(enterBtn.cloneNode(true));
        const newBtn = document.getElementById('enterFullscreenBtn');
        
        newBtn.addEventListener('click', async (e) => {
            console.log('Enter Exam Mode clicked!');
            e.preventDefault();
            e.stopPropagation();
            await this.enterExamMode();
        });
        
        prompt.classList.remove('hidden');
        console.log('Fullscreen prompt shown');
    }

    async showDeviceCheckPrompt() {
        console.log('Showing device check prompt');
        const devicePrompt = document.createElement('div');
        devicePrompt.id = 'deviceCheckPrompt';
        devicePrompt.className = 'fullscreen-prompt';
        devicePrompt.innerHTML = `
            <div class="prompt-content">
                <div class="secure-ai-branding">
                    <div class="ai-logo">🛡️</div>
                    <h1>SecureExam AI</h1>
                    <p class="tagline">AI-Powered Proctoring & Assessment</p>
                </div>
                <h2>📹 Device Check</h2>
                <p>We need access to your camera and microphone for proctoring.</p>
                <div id="deviceStatus" class="device-status">
                    <div class="status-item" id="cameraStatus">
                        <span class="status-icon">📷</span>
                        <span>Camera: Checking...</span>
                    </div>
                    <div class="status-item" id="micStatus">
                        <span class="status-icon">🎤</span>
                        <span>Microphone: Checking...</span>
                    </div>
                </div>
                <button id="grantDeviceBtn" class="btn btn-primary btn-large">Grant Access</button>
                <p class="hint">Click "Allow" when browser prompts for permissions</p>
            </div>
        `;
        document.body.appendChild(devicePrompt);
        
        document.getElementById('grantDeviceBtn')?.addEventListener('click', async () => {
            const grantBtn = document.getElementById('grantDeviceBtn');
            grantBtn.textContent = 'Requesting...';
            
            try {
                // Request camera AND microphone
                this.videoStream = await navigator.mediaDevices.getUserMedia({
                    video: { width: 320, height: 240, facingMode: 'user' },
                    audio: true
                });
                
                // Update status
                document.getElementById('cameraStatus').innerHTML = '<span class="status-icon">✅</span><span>Camera: Granted</span>';
                document.getElementById('micStatus').innerHTML = '<span class="status-icon">✅</span><span>Microphone: Granted</span>';
                document.getElementById('deviceStatus').classList.add('success');
                
                setTimeout(() => {
                    devicePrompt.remove();
                    this.showScreenSharePrompt();
                }, 1000);
                
            } catch (error) {
                console.error('Device access error:', error);
                document.getElementById('cameraStatus').innerHTML = '<span class="status-icon">⚠️</span><span>Camera: Optional - Continue anyway</span>';
                document.getElementById('micStatus').innerHTML = '<span class="status-icon">⚠️</span><span>Microphone: Optional</span>';
                document.getElementById('deviceStatus').classList.add('warning');
                grantBtn.textContent = 'Continue to Exam';
                grantBtn.onclick = () => {
                    devicePrompt.remove();
                    this.showScreenSharePrompt();
                };
            }
        });
    }

    async showScreenSharePrompt() {
        console.log('Showing screen share prompt');
        const screenPrompt = document.createElement('div');
        screenPrompt.id = 'screenSharePrompt';
        screenPrompt.className = 'fullscreen-prompt';
        screenPrompt.innerHTML = `
            <div class="prompt-content">
                <div class="secure-ai-branding">
                    <div class="ai-logo">🛡️</div>
                    <h1>SecureExam AI</h1>
                </div>
                <h2>🖥️ Screen Sharing</h2>
                <p>Please share your entire screen for monitoring.</p>
                <div class="screen-share-preview">
                    <div class="preview-box">
                        <span>📺</span>
                        <p>Your screen will be monitored during the exam</p>
                    </div>
                </div>
                <button id="startScreenShareBtn" class="btn btn-primary btn-large">Share Screen</button>
                <p class="hint">Select "Your Entire Screen" when prompted</p>
            </div>
        `;
        document.body.appendChild(screenPrompt);
        
        document.getElementById('startScreenShareBtn')?.addEventListener('click', async () => {
            try {
                this.screenStream = await navigator.mediaDevices.getDisplayMedia({
                    video: { cursor: 'always' },
                    audio: false
                });
                
                this.screenStream.getVideoTracks()[0].onended = () => {
                    this.logViolation('Screen sharing stopped', 'CRITICAL');
                };
                
                screenPrompt.remove();
                this.showTourPrompt();
                
            } catch (error) {
                console.error('Screen share error:', error);
                // Skip screen sharing and continue
                screenPrompt.remove();
                this.showTourPrompt();
            }
        });
        
        // Add skip button for screen sharing
        const skipBtn = document.createElement('button');
        skipBtn.className = 'btn btn-secondary btn-large';
        skipBtn.textContent = 'Skip Screen Share';
        skipBtn.style.marginTop = '1rem';
        skipBtn.onclick = () => {
            screenPrompt.remove();
            this.showTourPrompt();
        };
        screenPrompt.querySelector('.prompt-content').appendChild(skipBtn);
    }

    showTourPrompt() {
        console.log('Showing tour prompt');
        const tourPrompt = document.createElement('div');
        tourPrompt.id = 'tourPrompt';
        tourPrompt.className = 'fullscreen-prompt';
        tourPrompt.innerHTML = `
            <div class="prompt-content">
                <div class="secure-ai-branding">
                    <div class="ai-logo">🛡️</div>
                    <h1>SecureExam AI</h1>
                </div>
                <h2>🎓 Quick Tour</h2>
                <p>Would you like a quick tour of the exam interface?</p>
                <div class="tour-preview">
                    <div class="tour-features">
                        <div class="tour-item">📝 Code Editor with syntax highlighting</div>
                        <div class="tour-item">▶️ Run code and see test results</div>
                        <div class="tour-item">👤 AI proctoring with face detection</div>
                        <div class="tour-item">⏱️ Timer with auto-submit</div>
                    </div>
                </div>
                <div class="tour-buttons">
                    <button id="takeTourBtn" class="btn btn-secondary">Take Tour</button>
                    <button id="skipTourBtn" class="btn btn-primary btn-large">Start Exam Now</button>
                </div>
            </div>
        `;
        document.body.appendChild(tourPrompt);
        
        document.getElementById('takeTourBtn')?.addEventListener('click', () => {
            tourPrompt.remove();
            this.showTour();
        });
        
        document.getElementById('skipTourBtn')?.addEventListener('click', () => {
            tourPrompt.remove();
            this.showFullscreenPrompt();
        });
    }

    showTour() {
        const tourSteps = [
            {
                element: '#monacoEditor',
                title: 'Code Editor',
                description: 'Write your solution here with full Java syntax highlighting and IntelliSense.'
            },
            {
                element: '#runCodeBtn',
                title: 'Run Code',
                description: 'Test your code against sample test cases before submitting.'
            },
            {
                element: '#cameraFeed',
                title: 'Live Proctoring',
                description: 'AI monitors your face and eyes to ensure exam integrity.'
            },
            {
                element: '#timeDisplay',
                title: 'Timer',
                description: 'You have 30 minutes. The exam auto-submits when time runs out.'
            }
        ];
        
        let currentStep = 0;
        
        const showStep = () => {
            if (currentStep >= tourSteps.length) {
                document.getElementById('tourOverlay')?.remove();
                this.showFullscreenPrompt();
                return;
            }
            
            const step = tourSteps[currentStep];
            const overlay = document.createElement('div');
            overlay.id = 'tourOverlay';
            overlay.className = 'tour-overlay';
            overlay.innerHTML = `
                <div class="tour-backdrop"></div>
                <div class="tour-tooltip" style="position: absolute;">
                    <h3>${step.title}</h3>
                    <p>${step.description}</p>
                    <button id="nextTourStep" class="btn btn-primary">${currentStep === tourSteps.length - 1 ? 'Finish' : 'Next'}</button>
                </div>
            `;
            
            document.body.appendChild(overlay);
            
            document.getElementById('nextTourStep')?.addEventListener('click', () => {
                overlay.remove();
                currentStep++;
                showStep();
            });
        };
        
        showStep();
    }

    showFullscreenPrompt() {
        console.log('Showing fullscreen prompt');
        const fullscreenPrompt = document.createElement('div');
        fullscreenPrompt.id = 'fullscreenPrompt';
        fullscreenPrompt.className = 'fullscreen-prompt';
        fullscreenPrompt.innerHTML = `
            <div class="prompt-content">
                <div class="secure-ai-branding">
                    <div class="ai-logo">🛡️</div>
                    <h1>SecureExam AI</h1>
                </div>
                <h2>🔒 Enter Fullscreen Mode</h2>
                <p>The exam requires fullscreen mode for security.</p>
                <div class="fullscreen-preview">
                    <div class="preview-box">
                        <span>🖥️</span>
                        <p>Your exam will start in fullscreen</p>
                    </div>
                </div>
                <button id="enterFullscreenBtn" class="btn btn-primary btn-large" onclick="examManager.enterExamMode()">Enter Exam Mode</button>
                <p class="hint">Click to start your exam</p>
            </div>
        `;
        document.body.appendChild(fullscreenPrompt);
        console.log('Fullscreen prompt created, button should be clickable');
    }

    async enterExamMode() {
        console.log('Entering exam mode...');
        const btn = document.getElementById('enterFullscreenBtn');
        if (btn) btn.textContent = 'Initializing...';
        
        try {
            // Step 0: Create backend session FIRST (no auth required)
            console.log('Creating exam session on backend...');
            const user = JSON.parse(localStorage.getItem('exam_user') || '{}');
            const startResponse = await fetch(
                `${this.apiBaseUrl}/exam/start`,
                { 
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: user.username || 'guest' })
                }
            );
            
            if (!startResponse.ok) {
                throw new Error('Failed to start exam session: ' + startResponse.status);
            }
            
            const sessionData = await startResponse.json();
            console.log('Session created:', sessionData);
            
            // Update localStorage with real session ID
            const examSession = JSON.parse(localStorage.getItem('exam_session') || '{}');
            examSession.sessionId = sessionData.sessionId;
            localStorage.setItem('exam_session', JSON.stringify(examSession));
            localStorage.setItem('exam_start_time', Date.now().toString());
            
            // Step 1: Request camera
            console.log('Requesting camera access...');
            await this.initCamera();
            console.log('Camera access granted');
            
            // Step 2: Request fullscreen
            console.log('Requesting fullscreen...');
            const elem = document.documentElement;
            if (elem.requestFullscreen) {
                await elem.requestFullscreen();
            } else if (elem.webkitRequestFullscreen) {
                await elem.webkitRequestFullscreen();
            } else if (elem.msRequestFullscreen) {
                await elem.msRequestFullscreen();
            }
            console.log('Fullscreen granted');
            
            // Hide prompt and show exam
            const prompt = document.getElementById('fullscreenPrompt');
            if (prompt) {
                prompt.remove();
                console.log('Fullscreen prompt removed');
            }
            
            // Show exam container
            const examContainer = document.getElementById('examContainer');
            if (examContainer) {
                examContainer.style.display = 'flex';
                examContainer.style.visibility = 'visible';
                examContainer.style.opacity = '1';
                console.log('Exam container shown');
            } else {
                console.error('examContainer not found!');
            }
            
            this.isFullscreen = true;
            
            // Initialize timeRemaining if not set
            if (!this.timeRemaining || this.timeRemaining <= 0) {
                this.timeRemaining = 90 * 60; // 90 minutes
                console.log('Timer initialized to 90 minutes');
            }
            
            // Initialize Monaco Editor FIRST (needs visible container)
            console.log('Initializing Monaco Editor...');
            this.initMonacoEditor();
            
            // Load first question
            console.log('Loading first question...');
            this.loadQuestion(0);
            
            // Start timer
            console.log('Starting timer...');
            this.startTimer();
            
            // Attach event listeners for exam buttons
            console.log('Attaching event listeners...');
            this.attachEventListeners();
            
            // Start security monitoring
            this.startSecurityMonitoring();
            
            // Start AI proctoring
            this.startProctoring();
            
            // Start screen sharing (only if not already started)
            if (!this.screenStream) {
                this.startScreenSharing();
            }
            
            console.log('Exam mode activated successfully!');
            
        } catch (error) {
            console.error('Error entering exam mode:', error);
            if (btn) btn.textContent = 'Enter Exam Mode';
            
            if (error.message.includes('Camera') || error.name === 'NotAllowedError') {
                alert('Camera access is required for the exam.\n\nPlease:\n1. Click the camera icon in your browser address bar\n2. Select "Allow"\n3. Click "Enter Exam Mode" again');
            } else if (error.message.includes('fullscreen') || error.name === 'TypeError') {
                alert('Fullscreen is required for the exam. Please click the button again.');
            } else {
                alert('Error: ' + error.message);
            }
        }
    }

    async initCamera() {
        try {
            // Check if mediaDevices is supported
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                throw new Error('Camera not supported in this browser');
            }
            
            this.videoStream = await navigator.mediaDevices.getUserMedia({
                video: { width: 320, height: 240, facingMode: 'user' },
                audio: false
            });
            
            const video = document.getElementById('cameraFeed');
            if (video) {
                video.srcObject = this.videoStream;
                video.onloadedmetadata = () => {
                    video.play();
                };
            }
            
            console.log('Camera initialized successfully');
        } catch (error) {
            console.error('Camera error:', error);
            throw new Error('Camera access denied: ' + error.message);
        }
    }

    initMonacoEditor() {
        require.config({ paths: { vs: 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs' }});
        
        require(['vs/editor/editor.main'], () => {
            const container = document.getElementById('monacoEditor');
            if (!container) {
                console.error('Monaco container not found');
                return;
            }
            
            this.monacoEditor = monaco.editor.create(container, {
                value: this.questions[0]?.starterCode || '',
                language: 'java',
                theme: 'vs-dark',
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                padding: { top: 16 },
                lineNumbers: 'on',
                roundedSelection: false,
                scrollbar: { verticalScrollbarSize: 8 }
            });
            
            // Force layout after creation
            setTimeout(() => {
                this.monacoEditor.layout();
            }, 100);
        });
    }

    loadQuestion(index) {
        if (index < 0 || index >= this.questions.length) return;
        
        this.currentQuestion = index;
        const q = this.questions[index];
        
        // Update UI
        document.getElementById('currentQuestion').textContent = index + 1;
        document.getElementById('totalQuestions').textContent = this.questions.length;
        document.getElementById('problemTitle').textContent = `Problem ${index + 1}: ${q.title}`;
        
        const badge = document.getElementById('difficultyBadge');
        badge.textContent = q.difficulty;
        badge.className = 'difficulty-badge ' + q.difficulty.toLowerCase();
        
        document.getElementById('problemDescription').innerHTML = q.description.replace(/\n/g, '<br>');
        
        // Examples
        const examplesDiv = document.getElementById('examples');
        examplesDiv.innerHTML = q.examples.map((ex, i) => `
            <div class="example-block">
                <div class="example-title">Example ${i + 1}:</div>
                <pre><strong>Input:</strong> ${ex.input}
<strong>Output:</strong> ${ex.output}${ex.explanation ? '\n<strong>Explanation:</strong> ' + ex.explanation : ''}</pre>
            </div>
        `).join('');
        
        // Constraints
        const constraintsDiv = document.getElementById('constraints');
        constraintsDiv.innerHTML = q.constraints.map(c => `<li>${c}</li>`).join('');
        
        // Load saved code or starter code
        if (this.monacoEditor) {
            const savedCode = this.userCode[q.id] || q.starterCode;
            this.monacoEditor.setValue(savedCode);
        }
    }

    startTimer() {
        // Check for saved time
        const savedStart = localStorage.getItem('exam_start_time');
        if (savedStart) {
            const elapsed = Math.floor((Date.now() - parseInt(savedStart)) / 1000);
            this.timeRemaining = Math.max(0, 30 * 60 - elapsed);
        }
        
        this.updateTimerDisplay();
        
        this.timerInterval = setInterval(() => {
            this.timeRemaining--;
            this.updateTimerDisplay();
            
            if (this.timeRemaining <= 0) {
                this.submitExam('Time expired');
            }
        }, 1000);
    }

    updateTimerDisplay() {
        const minutes = Math.floor(this.timeRemaining / 60);
        const seconds = this.timeRemaining % 60;
        const display = document.getElementById('timeDisplay');
        display.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        // Warning colors
        if (this.timeRemaining <= 300) {
            display.className = 'time-display danger';
        } else if (this.timeRemaining <= 600) {
            display.className = 'time-display warning';
        }
    }

    startSecurityMonitoring() {
        // Tab visibility detection - reduced sensitivity
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                // Delay to avoid false positives from quick tab switches
                setTimeout(() => {
                    if (document.hidden) {
                        this.logViolation('Tab switch detected', 'HIGH');
                    }
                }, 3000);
            }
        });
        
        // Window blur detection - reduced sensitivity
        window.addEventListener('blur', () => {
            // Only log if window is blurred for more than 2 seconds
            setTimeout(() => {
                if (document.visibilityState === 'hidden') {
                    this.logViolation('Window focus lost', 'MEDIUM');
                }
            }, 2000);
        });
        
        // Fullscreen exit detection - disable modal popup
        document.addEventListener('fullscreenchange', () => {
            if (!document.fullscreenElement && !this.isSubmitting) {
                this.isFullscreen = false;
                this.logViolation('Fullscreen exited', 'CRITICAL');
                // Modal disabled - too intrusive
                // this.showViolationModal('Fullscreen mode was exited. Please return to fullscreen immediately.', 'CRITICAL');
            }
        });
        
        // Prevent common shortcuts
        document.addEventListener('keydown', (e) => {
            // Prevent Alt+Tab, Ctrl+Alt+Del equivalents
            if (e.altKey && e.key === 'Tab') {
                e.preventDefault();
                this.logViolation('Alt+Tab attempted', 'HIGH');
            }
            
            // Prevent F12 (DevTools)
            if (e.key === 'F12') {
                e.preventDefault();
                this.logViolation('DevTools access attempted', 'CRITICAL');
            }
            
            // Prevent Ctrl+Shift+I/J/C
            if (e.ctrlKey && e.shiftKey && ['I', 'J', 'C'].includes(e.key)) {
                e.preventDefault();
                this.logViolation('DevTools shortcut attempted', 'CRITICAL');
            }
            
            // Prevent Ctrl+U (View source)
            if (e.ctrlKey && e.key === 'u') {
                e.preventDefault();
            }
            
            // Prevent Print Screen
            if (e.key === 'PrintScreen') {
                e.preventDefault();
                this.logViolation('Screenshot attempted', 'MEDIUM');
            }
        });
        
        // Right-click prevention
        document.addEventListener('contextmenu', (e) => {
            e.preventDefault();
        });
        
        // Monitor process via backend
        setInterval(() => this.checkSuspiciousProcesses(), 10000);
    }

    async startScreenSharing() {
        try {
            this.screenStream = await navigator.mediaDevices.getDisplayMedia({
                video: { cursor: 'always' },
                audio: false
            });
            
            this.screenStream.getVideoTracks()[0].onended = () => {
                this.logViolation('Screen sharing stopped', 'CRITICAL');
            };
            
            console.log('Screen sharing started');
        } catch (error) {
            console.error('Screen sharing error:', error);
        }
    }

    async stopScreenSharing() {
        if (this.screenStream) {
            this.screenStream.getTracks().forEach(track => track.stop());
            this.screenStream = null;
        }
    }

    async startProctoring() {
        // Capture frame and send to AI service every 3 seconds
        this.proctoringInterval = setInterval(() => this.captureAndAnalyze(), 3000);
    }

    async captureAndAnalyze() {
        const video = document.getElementById('cameraFeed');
        const canvas = document.getElementById('cameraCanvas');
        
        if (!video.videoWidth) return;
        
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0);
        
        // Convert to base64
        const frame = canvas.toDataURL('image/jpeg', 0.7).split(',')[1];
        
        try {
            const response = await fetch(`${this.aiServiceUrl}/analyze-frame`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ frame })
            });
            
            if (response.ok) {
                const result = await response.json();
                this.updateProctoringStatus(result);
            }
        } catch (error) {
            console.error('Proctoring error:', error);
        }
    }

    updateProctoringStatus(result) {
        const faceStatus = document.getElementById('faceStatus');
        const eyeStatus = document.getElementById('eyeStatus');
        const riskDisplay = document.getElementById('riskScore');
        
        // Update face status with grace period logic
        if (result.face_detected) {
            faceStatus.innerHTML = '<span class="icon live-indicator">👤</span><span class="status">Face Verified</span>';
            faceStatus.style.color = result.multiple_faces ? 'var(--danger-color)' : 'var(--success-color)';
            this.faceNotDetectedStart = null; // Reset timer when face detected
            
            if (result.multiple_faces) {
                this.logViolation('Multiple faces detected', 'CRITICAL');
            }
        } else {
            // Face not detected - start grace period
            if (!this.faceNotDetectedStart) {
                this.faceNotDetectedStart = Date.now();
            }
            
            const elapsed = Date.now() - this.faceNotDetectedStart;
            const remaining = Math.max(0, Math.ceil((this.faceDetectionTimeout - elapsed) / 1000));
            
            faceStatus.innerHTML = `<span class="icon">👤</span><span class="status">Face Lost - Auto-submit in ${remaining}s</span>`;
            faceStatus.style.color = 'var(--danger-color)';
            
            // Auto-submit after 30 seconds of no face
            if (elapsed >= this.faceDetectionTimeout) {
                this.submitExam('Auto-submitted: Face not detected for 30 seconds');
                return;
            }
        }
        
        // Update eye status - be more lenient, don't log violation for brief looks
        eyeStatus.innerHTML = '<span class="icon">👁️</span><span class="status">' + (result.looking_away ? 'Looking away!' : 'Focused') + '</span>';
        eyeStatus.style.color = result.looking_away ? 'var(--warning-color)' : 'var(--success-color)';
        
        // Only log looking away if it's a significant issue (multiple consecutive detections)
        // Reduced sensitivity: don't log MEDIUM violations for looking at keyboard briefly
        if (result.looking_away && result.risk_increment > 5) {
            this.logViolation('Candidate looking away', 'MEDIUM');
        }
        
        // Update risk score
        this.riskScore = Math.min(100, this.riskScore + (result.risk_increment || 0));
        riskDisplay.textContent = this.riskScore;
        
        if (this.riskScore < 30) {
            riskDisplay.className = 'risk-value low';
        } else if (this.riskScore < 70) {
            riskDisplay.className = 'risk-value medium';
        } else {
            riskDisplay.className = 'risk-value high';
        }
    }

    async checkSuspiciousProcesses() {
        try {
            const response = await this.authManager.makeAuthenticatedRequest(
                `${this.apiBaseUrl}/monitoring/processes`
            );
            
            if (response.ok) {
                const data = await response.json();
                if (data.suspicious_found) {
                    data.processes.forEach(proc => {
                        this.logViolation(`Suspicious process detected: ${proc}`, 'HIGH');
                    });
                }
            }
        } catch (error) {
            console.error('Process monitoring error:', error);
        }
    }

    logViolation(type, severity) {
        // Don't log violations during intentional submission
        if (this.isSubmitting) {
            console.log('Violation suppressed during submission:', type);
            return;
        }
        
        const violation = {
            type,
            severity,
            timestamp: new Date().toISOString(),
            questionIndex: this.currentQuestion
        };
        
        this.violations.push(violation);
        
        // Show warning only for CRITICAL (not HIGH)
        // MODAL DISABLED - too intrusive during exam
        // if (severity === 'CRITICAL') {
        //     this.showViolationModal(type, severity);
        // }
        
        // Auto-submit if too many violations (limit: 5 critical)
        const criticalCount = this.violations.filter(v => v.severity === 'CRITICAL').length;
        if (criticalCount >= this.violationLimit) {
            alert(`Violation limit reached (${criticalCount}/${this.violationLimit}). Auto-submitting exam...`);
            this.submitExam('Auto-submitted: Violation limit reached');
            return;
        }
        
        // Send to backend
        this.reportViolation(violation);
    }

    async reportViolation(violation) {
        try {
            const session = JSON.parse(localStorage.getItem('exam_session') || '{}');
            await this.authManager.makeAuthenticatedRequest(
                `${this.apiBaseUrl}/exam/violation`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        sessionId: session.sessionId,
                        type: violation.type,
                        severity: violation.severity,
                        questionIndex: violation.questionIndex
                    })
                }
            );
        } catch (error) {
            console.error('Failed to report violation:', error);
        }
    }

    showViolationModal(message, severity) {
        const overlay = document.getElementById('violationOverlay');
        const msgElement = document.getElementById('violationMessage');
        const countElement = document.getElementById('violationCount');
        
        msgElement.textContent = message;
        countElement.textContent = this.violations.length;
        
        overlay.classList.remove('hidden');
        
        document.getElementById('acknowledgeViolation').onclick = () => {
            overlay.classList.add('hidden');
            
            // Try to re-enter fullscreen
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen().catch(() => {});
            }
        };
    }

    attachEventListeners() {
        // Run code
        document.getElementById('runCodeBtn')?.addEventListener('click', () => this.runCode());
        
        // Submit code for current question
        document.getElementById('submitCodeBtn')?.addEventListener('click', () => this.submitCurrentQuestion());
        
        // Submit entire exam
        document.getElementById('submitExamBtn')?.addEventListener('click', () => this.submitExam('Manual submit'));
        
        // Reset code
        document.getElementById('resetCodeBtn')?.addEventListener('click', () => this.resetCode());
        
        // Clear output
        document.getElementById('clearOutputBtn')?.addEventListener('click', () => this.clearOutput());
    }

    async runCode() {
        if (!this.monacoEditor) return;
        
        const code = this.monacoEditor.getValue();
        const currentQ = this.questions[this.currentQuestion];
        const session = JSON.parse(localStorage.getItem('exam_session') || '{}');
        const sessionId = session.sessionId || 0;
        
        this.addOutput('Compiling and running...', 'info');
        
        try {
            const response = await this.authManager.makeAuthenticatedRequest(
                `${this.apiBaseUrl}/exam/execute`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        sessionId: parseInt(sessionId),
                        questionId: currentQ.id,
                        code: code
                    })
                }
            );
            
            const responseText = await response.text();
            console.log('Execute response:', response.status, responseText);
            
            if (!responseText) {
                this.addOutput('Error: Server returned empty response. Code execution service may be unavailable.', 'error');
                return;
            }
            
            let result;
            try {
                result = JSON.parse(responseText);
            } catch (parseError) {
                this.addOutput('Error: Invalid response from server', 'error');
                console.error('JSON parse error:', parseError, 'Response:', responseText);
                return;
            }
            
            if (result.success) {
                this.addOutput('Compilation successful!', 'success');
                this.displayTestResults(result.testResults);
            } else {
                this.addOutput(`Error: ${result.error}`, 'error');
            }
        } catch (error) {
            this.addOutput(`Execution error: ${error.message}`, 'error');
            console.error('Run code error:', error);
        }
    }

    displayTestResults(results) {
        const container = document.getElementById('testCaseResults');
        const testCasesDiv = document.getElementById('testCases');
        
        if (!results || !Array.isArray(results) || results.length === 0) {
            testCasesDiv.innerHTML = '<div class="test-case-item">No test results available</div>';
            container.classList.remove('hidden');
            return;
        }
        
        testCasesDiv.innerHTML = results.map((r, i) => {
            const passed = r.passed || r.isPassed;
            const expected = r.expected || r.getExpected;
            const actual = r.actual || r.getActual;
            
            return `
                <div class="test-case-item ${passed ? 'passed' : 'failed'}">
                    <span>${passed ? '✓' : '✗'}</span>
                    <span>Test Case ${i + 1}: ${passed ? 'Passed' : 'Failed'}</span>
                    ${!passed ? `<br><small>Expected: ${expected}, Got: ${actual}</small>` : ''}
                </div>
            `;
        }).join('');
        
        container.classList.remove('hidden');
    }

    async submitCurrentQuestion() {
        if (!this.monacoEditor) return;
        
        const code = this.monacoEditor.getValue();
        const currentQ = this.questions[this.currentQuestion];
        
        // Save code
        this.userCode[currentQ.id] = code;
        
        // Run tests
        await this.runCode();
        
        // Move to next question or switch to MCQ
        if (this.currentQuestion < this.questions.length - 1) {
            this.loadQuestion(this.currentQuestion + 1);
        } else {
            // Coding done - switch to MCQ
            this.switchToMCQMode();
        }
    }

    switchToMCQMode() {
        this.examMode = 'mcq';
        this.currentQuestion = 0;
        console.log('Switching to MCQ mode');
        
        // Hide coding UI, show MCQ UI
        const examContent = document.querySelector('.exam-content');
        if (examContent) {
            examContent.innerHTML = `
                <div id="mcqContainer" style="width: 100%; padding: 2rem; overflow-y: auto;">
                    <div class="mcq-header" style="text-align: center; margin-bottom: 2rem;">
                        <h2>Multiple Choice Questions</h2>
                        <p>Question <span id="mcqCurrent">1</span> of <span id="mcqTotal">${this.mcqQuestions.length}</span></p>
                    </div>
                    <div id="mcqQuestion" style="max-width: 800px; margin: 0 auto;"></div>
                    <div style="text-align: center; margin-top: 2rem;">
                        <button id="nextMcqBtn" class="btn btn-primary btn-large">Next Question</button>
                        <button id="submitMcqBtn" class="btn btn-success btn-large hidden">Submit Exam</button>
                    </div>
                </div>
            `;
        }
        
        this.loadMCQ(0);
        
        // Attach MCQ event listeners with debugging
        const nextBtn = document.getElementById('nextMcqBtn');
        const submitBtn = document.getElementById('submitMcqBtn');
        
        console.log('MCQ buttons found:', { nextBtn: !!nextBtn, submitBtn: !!submitBtn });
        
        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                console.log('Next MCQ clicked');
                this.nextMCQ();
            });
        }
        
        if (submitBtn) {
            submitBtn.addEventListener('click', (e) => {
                console.log('Submit button clicked!');
                e.preventDefault();
                e.stopPropagation();
                this.submitExam('Manual submit');
            });
            console.log('Submit button listener attached');
        }
    }

    loadMCQ(index) {
        if (index < 0 || index >= this.mcqQuestions.length) return;
        
        this.currentQuestion = index;
        const mcq = this.mcqQuestions[index];
        
        document.getElementById('mcqCurrent').textContent = index + 1;
        const container = document.getElementById('mcqQuestion');
        
        const savedAnswer = this.mcqAnswers[mcq.id];
        
        container.innerHTML = `
            <div class="mcq-card" style="background: var(--bg-card); padding: 2rem; border-radius: 1rem; margin-bottom: 1rem;">
                <h3 style="margin-bottom: 1.5rem; font-size: 1.25rem;">${index + 1}. ${mcq.question}</h3>
                <div class="mcq-options" style="display: flex; flex-direction: column; gap: 0.75rem;">
                    ${mcq.options.map((opt, i) => `
                        <label class="mcq-option" style="display: flex; align-items: center; gap: 0.75rem; padding: 1rem; background: var(--bg-panel); border-radius: 0.5rem; cursor: pointer; ${savedAnswer === i ? 'border: 2px solid var(--primary-color);' : ''}">
                            <input type="radio" name="mcq${mcq.id}" value="${i}" ${savedAnswer === i ? 'checked' : ''} style="width: 20px; height: 20px;">
                            <span>${opt}</span>
                        </label>
                    `).join('')}
                </div>
            </div>
        `;
        
        // Attach radio listeners
        container.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.mcqAnswers[mcq.id] = parseInt(e.target.value);
                console.log('MCQ answer saved:', mcq.id, this.mcqAnswers[mcq.id]);
            });
        });
        
        // Update buttons
        const nextBtn = document.getElementById('nextMcqBtn');
        const submitBtn = document.getElementById('submitMcqBtn');
        
        if (index === this.mcqQuestions.length - 1) {
            nextBtn?.classList.add('hidden');
            submitBtn?.classList.remove('hidden');
        } else {
            nextBtn?.classList.remove('hidden');
            submitBtn?.classList.add('hidden');
        }
    }

    nextMCQ() {
        // Save current answer
        const mcq = this.mcqQuestions[this.currentQuestion];
        const selected = document.querySelector(`input[name="mcq${mcq.id}"]:checked`);
        if (selected) {
            this.mcqAnswers[mcq.id] = parseInt(selected.value);
        }
        
        // Load next
        if (this.currentQuestion < this.mcqQuestions.length - 1) {
            this.loadMCQ(this.currentQuestion + 1);
        }
    }

    async submitExam(reason = 'Manual') {
        console.log('=== SUBMIT EXAM CALLED ===', reason);
        
        // Set flag to prevent violations during submission
        this.isSubmitting = true;
        
        // Save current code only if in coding mode
        if (this.monacoEditor && this.examMode === 'coding' && this.questions[this.currentQuestion]) {
            this.userCode[this.questions[this.currentQuestion].id] = this.monacoEditor.getValue();
        }
        
        // Save last MCQ answer if in MCQ mode
        if (this.examMode === 'mcq' && this.mcqQuestions[this.currentQuestion]) {
            const mcq = this.mcqQuestions[this.currentQuestion];
            const selected = document.querySelector(`input[name="mcq${mcq.id}"]:checked`);
            if (selected) {
                this.mcqAnswers[mcq.id] = parseInt(selected.value);
            }
        }
        
        console.log('Submitting with answers:', { coding: this.userCode, mcq: this.mcqAnswers });
        
        // Stop intervals
        clearInterval(this.timerInterval);
        clearInterval(this.proctoringInterval);
        
        // Stop camera
        if (this.videoStream) {
            this.videoStream.getTracks().forEach(track => track.stop());
        }
        
        // Stop screen sharing
        this.stopScreenSharing();
        
        // Exit fullscreen (this won't trigger violation due to isSubmitting flag)
        if (document.fullscreenElement) {
            await document.exitFullscreen();
        }
        
        const timeTaken = 90 * 60 - this.timeRemaining;
        const session = JSON.parse(localStorage.getItem('exam_session') || '{}');
        
        // Ensure sessionId is a number
        const sessionId = session.sessionId ? parseInt(session.sessionId) : Date.now();
        
        try {
            console.log('Submitting exam with sessionId:', sessionId);
            
            const response = await fetch(
                `${this.apiBaseUrl}/exam/submit`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        sessionId: sessionId,
                        answers: this.userCode,
                        mcqAnswers: this.mcqAnswers,
                        violations: this.violations.map(v => ({
                            type: v.type,
                            severity: v.severity
                        })),
                        timeTaken,
                        riskScore: this.riskScore,
                        submitReason: reason
                    })
                }
            );
            
            console.log('Submit response status:', response.status);
            const responseText = await response.text();
            console.log('Submit response text:', responseText);
            
            if (response.ok) {
                let result;
                try {
                    result = JSON.parse(responseText);
                } catch (e) {
                    alert('Failed to parse response: ' + responseText.substring(0, 200));
                    this.isSubmitting = false;
                    return;
                }
                
                // Clear session
                localStorage.removeItem('exam_session');
                localStorage.removeItem('exam_start_time');
                
                // Reset submitting flag before showing report
                this.isSubmitting = false;
                
                // Show report
                this.showFinalReport(result);
            } else if (response.status === 403) {
                this.isSubmitting = false;
                alert('Session expired. Please login again.');
                localStorage.removeItem('jwt_token');
                localStorage.removeItem('exam_user');
                window.location.href = 'login.html';
            } else {
                this.isSubmitting = false;
                console.error('Submit failed:', response.status, responseText);
                alert('Submit failed: ' + response.status + ' - ' + responseText.substring(0, 200));
            }
        } catch (error) {
            this.isSubmitting = false;
            console.error('Submit error:', error);
            alert('Error submitting exam: ' + error.message);
        }
    }

    showFinalReport(report) {
        // Prevent any further redirects by intercepting beforeunload
        window.onbeforeunload = null;
        
        // Hide exam container
        const examContainer = document.getElementById('examContainer');
        if (examContainer) {
            examContainer.style.display = 'none';
        }
        
        // Remove any other prompts
        document.querySelectorAll('.fullscreen-prompt, .tour-overlay').forEach(el => el.remove());
        
        // Create inline report overlay that covers everything
        const reportDiv = document.createElement('div');
        reportDiv.id = 'inlineReport';
        reportDiv.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: #0f172a;
            color: white;
            overflow-y: auto;
            z-index: 99999;
            padding: 2rem;
        `;
        
        const score = report.score || 0;
        const passed = score >= 60;
        
        reportDiv.innerHTML = `
            <div style="max-width: 800px; margin: 0 auto;">
                <div style="text-align: center; margin-bottom: 2rem; padding-top: 2rem;">
                    <div style="font-size: 4rem; margin-bottom: 1rem;">🎉</div>
                    <h1 style="color: ${passed ? '#10b981' : '#ef4444'}; font-size: 2.5rem;">Exam Completed!</h1>
                    <h2 style="font-size: 1.5rem; margin-top: 1rem; color: #94a3b8;">Your Report</h2>
                </div>
                
                <div style="background: #1e293b; padding: 2rem; border-radius: 1rem; margin-bottom: 1.5rem; text-align: center;">
                    <h3 style="color: #3b82f6; margin-bottom: 1rem;">📊 Overall Score</h3>
                    <div style="font-size: 5rem; font-weight: bold; color: ${passed ? '#10b981' : '#ef4444'};">${score}%</div>
                    <p style="font-size: 1.25rem; margin-top: 0.5rem; color: ${passed ? '#10b981' : '#ef4444'};">
                        ${passed ? '✅ PASSED' : '❌ FAILED'}
                    </p>
                </div>
                
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;">
                    <div style="background: #1e293b; padding: 1.5rem; border-radius: 1rem; text-align: center;">
                        <h4 style="color: #94a3b8; margin-bottom: 0.5rem;">Total Questions</h4>
                        <p style="font-size: 1.5rem; font-weight: bold;">${report.totalQuestions || 17}</p>
                    </div>
                    <div style="background: #1e293b; padding: 1.5rem; border-radius: 1rem; text-align: center;">
                        <h4 style="color: #94a3b8; margin-bottom: 0.5rem;">Correct Answers</h4>
                        <p style="font-size: 1.5rem; font-weight: bold; color: #10b981;">${report.correctAnswers || 0}</p>
                    </div>
                </div>
                
                <div style="background: #1e293b; padding: 1.5rem; border-radius: 1rem; margin-bottom: 1.5rem; text-align: center;">
                    <h4 style="color: #94a3b8; margin-bottom: 0.5rem;">⏱️ Time Taken</h4>
                    <p style="font-size: 1.25rem;">${Math.floor((report.timeTaken || 0) / 60)} minutes</p>
                </div>
                
                ${report.violations && report.violations.length > 0 ? `
                <div style="background: #1e293b; padding: 1.5rem; border-radius: 1rem; margin-bottom: 1.5rem;">
                    <h4 style="color: #94a3b8; margin-bottom: 1rem;">⚠️ Violations (${report.violations.length})</h4>
                    ${report.violations.map(v => `
                        <div style="padding: 0.5rem; background: rgba(239, 68, 68, 0.1); border-radius: 0.25rem; margin-bottom: 0.5rem;">
                            <span style="color: #ef4444; font-weight: bold;">${v.severity}</span>: ${v.type}
                        </div>
                    `).join('')}
                </div>
                ` : ''}
                
                <div style="text-align: center; padding: 2rem;">
                    <button onclick="window.location.href='login.html'" style="
                        background: #3b82f6;
                        color: white;
                        padding: 1rem 3rem;
                        border: none;
                        border-radius: 0.5rem;
                        font-size: 1.125rem;
                        cursor: pointer;
                        font-weight: bold;
                    ">Back to Login</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(reportDiv);
        
        // Stop all intervals
        clearInterval(this.timerInterval);
        clearInterval(this.proctoringInterval);
        
        console.log('Report displayed inline:', report);
    }

    resetCode() {
        if (!this.monacoEditor) return;
        const currentQ = this.questions[this.currentQuestion];
        this.monacoEditor.setValue(currentQ.starterCode);
        delete this.userCode[currentQ.id];
    }

    addOutput(message, type = 'info') {
        const console = document.getElementById('outputConsole');
        const line = document.createElement('div');
        line.className = `output-line ${type}`;
        line.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
        
        // Remove placeholder if exists
        const placeholder = console.querySelector('.placeholder');
        if (placeholder) placeholder.remove();
        
        console.appendChild(line);
        console.scrollTop = console.scrollHeight;
    }

    clearOutput() {
        const console = document.getElementById('outputConsole');
        console.innerHTML = '<p class="placeholder">Run your code to see output...</p>';
        document.getElementById('testCaseResults').classList.add('hidden');
    }
}

// Initialize
const examManager = new ExamManager();
