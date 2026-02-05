package com.swnih.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for handling page routing and redirects.
 */
@Controller
public class PageController {

    /**
     * Handle root URL and serve main page directly.
     */
    @GetMapping("/")
    @ResponseBody
    public String index() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>SWNIH - Smart Web Notification Intelligence Hub</title>
                
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        min-height: 100vh;
                        overflow-x: hidden;
                    }

                    /* Header */
                    .header {
                        position: fixed;
                        top: 0;
                        left: 0;
                        right: 0;
                        background: rgba(255, 255, 255, 0.1);
                        backdrop-filter: blur(20px);
                        padding: 1rem 2rem;
                        z-index: 1000;
                        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
                    }

                    .nav {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        max-width: 1200px;
                        margin: 0 auto;
                    }

                    .logo {
                        display: flex;
                        align-items: center;
                        gap: 0.5rem;
                        font-size: 1.5rem;
                        font-weight: 700;
                    }

                    .nav-links {
                        display: flex;
                        gap: 2rem;
                        align-items: center;
                    }

                    .nav-link {
                        color: white;
                        text-decoration: none;
                        font-weight: 500;
                        transition: all 0.3s ease;
                        padding: 0.5rem 1rem;
                        border-radius: 8px;
                    }

                    .nav-link:hover {
                        background: rgba(255, 255, 255, 0.1);
                        transform: translateY(-1px);
                    }

                    /* Hero Section */
                    .hero {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        padding: 2rem;
                        text-align: center;
                    }

                    .hero-content {
                        max-width: 800px;
                        animation: fadeInUp 1s ease-out;
                    }

                    .hero-badge {
                        display: inline-block;
                        background: rgba(255, 255, 255, 0.2);
                        padding: 0.5rem 1.5rem;
                        border-radius: 50px;
                        font-size: 0.9rem;
                        font-weight: 600;
                        margin-bottom: 2rem;
                        border: 1px solid rgba(255, 255, 255, 0.3);
                    }

                    .hero-title {
                        font-size: clamp(3rem, 8vw, 5rem);
                        font-weight: 800;
                        margin-bottom: 1.5rem;
                        background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        line-height: 1.1;
                    }

                    .hero-subtitle {
                        font-size: 1.25rem;
                        margin-bottom: 3rem;
                        opacity: 0.9;
                        line-height: 1.6;
                        max-width: 600px;
                        margin-left: auto;
                        margin-right: auto;
                    }

                    .cta-buttons {
                        display: flex;
                        gap: 1rem;
                        justify-content: center;
                        flex-wrap: wrap;
                        margin-bottom: 4rem;
                    }

                    .btn {
                        padding: 1rem 2rem;
                        border: none;
                        border-radius: 12px;
                        font-size: 1rem;
                        font-weight: 600;
                        cursor: pointer;
                        transition: all 0.3s ease;
                        text-decoration: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 0.5rem;
                        min-width: 160px;
                        justify-content: center;
                    }

                    .btn-primary {
                        background: linear-gradient(135deg, #ff6b6b, #ee5a24);
                        color: white;
                        box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
                    }

                    .btn-primary:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 8px 25px rgba(255, 107, 107, 0.6);
                    }

                    .btn-secondary {
                        background: rgba(255, 255, 255, 0.1);
                        color: white;
                        border: 2px solid rgba(255, 255, 255, 0.3);
                        backdrop-filter: blur(10px);
                    }

                    .btn-secondary:hover {
                        background: rgba(255, 255, 255, 0.2);
                        border-color: rgba(255, 255, 255, 0.5);
                        transform: translateY(-2px);
                    }

                    /* Features Grid */
                    .features {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                        gap: 2rem;
                        max-width: 1000px;
                        margin: 0 auto;
                    }

                    .feature-card {
                        background: rgba(255, 255, 255, 0.1);
                        backdrop-filter: blur(20px);
                        border-radius: 20px;
                        padding: 2rem;
                        text-align: center;
                        border: 1px solid rgba(255, 255, 255, 0.2);
                        transition: all 0.3s ease;
                    }

                    .feature-card:hover {
                        transform: translateY(-5px);
                        background: rgba(255, 255, 255, 0.15);
                        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
                    }

                    .feature-icon {
                        font-size: 3rem;
                        margin-bottom: 1rem;
                        display: block;
                    }

                    .feature-title {
                        font-size: 1.25rem;
                        font-weight: 700;
                        margin-bottom: 1rem;
                    }

                    .feature-description {
                        opacity: 0.9;
                        line-height: 1.6;
                    }

                    /* Stats Section */
                    .stats {
                        display: flex;
                        justify-content: center;
                        gap: 3rem;
                        margin: 4rem 0;
                        flex-wrap: wrap;
                    }

                    .stat {
                        text-align: center;
                    }

                    .stat-number {
                        font-size: 3rem;
                        font-weight: 800;
                        display: block;
                        margin-bottom: 0.5rem;
                        background: linear-gradient(135deg, #ffeb3b, #ffc107);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                    }

                    .stat-label {
                        font-size: 1rem;
                        opacity: 0.8;
                        font-weight: 500;
                    }

                    /* Footer */
                    .footer {
                        text-align: center;
                        padding: 2rem;
                        margin-top: 4rem;
                        border-top: 1px solid rgba(255, 255, 255, 0.1);
                    }

                    .footer-text {
                        opacity: 0.7;
                        font-size: 0.9rem;
                    }

                    /* Animations */
                    @keyframes fadeInUp {
                        from {
                            opacity: 0;
                            transform: translateY(30px);
                        }
                        to {
                            opacity: 1;
                            transform: translateY(0);
                        }
                    }

                    .feature-card {
                        animation: fadeInUp 0.6s ease-out;
                    }

                    .feature-card:nth-child(1) { animation-delay: 0.1s; }
                    .feature-card:nth-child(2) { animation-delay: 0.2s; }
                    .feature-card:nth-child(3) { animation-delay: 0.3s; }

                    /* Responsive */
                    @media (max-width: 768px) {
                        .header {
                            padding: 1rem;
                        }
                        
                        .nav {
                            flex-direction: column;
                            gap: 1rem;
                        }
                        
                        .nav-links {
                            gap: 1rem;
                        }
                        
                        .hero {
                            padding: 1rem;
                            padding-top: 120px;
                        }
                        
                        .cta-buttons {
                            flex-direction: column;
                            align-items: center;
                        }
                        
                        .features {
                            grid-template-columns: 1fr;
                            padding: 0 1rem;
                        }
                        
                        .stats {
                            gap: 2rem;
                        }
                        
                        .stat-number {
                            font-size: 2.5rem;
                        }
                    }

                    /* Floating Elements */
                    .floating-element {
                        position: absolute;
                        opacity: 0.1;
                        animation: float 6s ease-in-out infinite;
                    }

                    .floating-element:nth-child(1) {
                        top: 20%;
                        left: 10%;
                        animation-delay: 0s;
                    }

                    .floating-element:nth-child(2) {
                        top: 60%;
                        right: 10%;
                        animation-delay: 2s;
                    }

                    .floating-element:nth-child(3) {
                        bottom: 20%;
                        left: 20%;
                        animation-delay: 4s;
                    }

                    @keyframes float {
                        0%, 100% { transform: translateY(0px); }
                        50% { transform: translateY(-20px); }
                    }
                </style>
            </head>
            <body>
                <!-- Floating Background Elements -->
                <div class="floating-element">🧠</div>
                <div class="floating-element">📧</div>
                <div class="floating-element">⚡</div>

                <!-- Header -->
                <header class="header">
                    <nav class="nav">
                        <div class="logo">
                            🧠 SWNIH
                        </div>
                        <div class="nav-links">
                            <a href="/login" class="nav-link">Login</a>
                            <a href="/register" class="nav-link">Sign Up</a>
                            <a href="/test" class="nav-link">Demo</a>
                        </div>
                    </nav>
                </header>

                <!-- Hero Section -->
                <section class="hero">
                    <div class="hero-content">
                        <div class="hero-badge">
                            ✨ AI-Powered Email Intelligence
                        </div>
                        
                        <h1 class="hero-title">
                            Smart Email<br>
                            Management
                        </h1>
                        
                        <p class="hero-subtitle">
                            Transform your inbox chaos into organized intelligence. 
                            Let AI prioritize your emails so you can focus on what matters most.
                        </p>
                        
                        <div class="cta-buttons">
                            <a href="/register" class="btn btn-primary">
                                🚀 Get Started Free
                            </a>
                            <a href="/login" class="btn btn-secondary">
                                📧 Sign In
                            </a>
                        </div>

                        <!-- Stats -->
                        <div class="stats">
                            <div class="stat">
                                <span class="stat-number">99.9%</span>
                                <span class="stat-label">Accuracy</span>
                            </div>
                            <div class="stat">
                                <span class="stat-number">10x</span>
                                <span class="stat-label">Faster</span>
                            </div>
                            <div class="stat">
                                <span class="stat-number">24/7</span>
                                <span class="stat-label">Active</span>
                            </div>
                        </div>

                        <!-- Features -->
                        <div class="features">
                            <div class="feature-card">
                                <span class="feature-icon">🤖</span>
                                <h3 class="feature-title">AI Priority Detection</h3>
                                <p class="feature-description">
                                    Advanced machine learning automatically categorizes your emails by importance and urgency.
                                </p>
                            </div>
                            
                            <div class="feature-card">
                                <span class="feature-icon">📊</span>
                                <h3 class="feature-title">Smart Dashboard</h3>
                                <p class="feature-description">
                                    Beautiful, intuitive interface that shows your email insights at a glance.
                                </p>
                            </div>
                            
                            <div class="feature-card">
                                <span class="feature-icon">⚡</span>
                                <h3 class="feature-title">Lightning Fast</h3>
                                <p class="feature-description">
                                    Process thousands of emails in seconds with real-time Gmail integration.
                                </p>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Footer -->
                <footer class="footer">
                    <p class="footer-text">
                        © 2026 SWNIH - Smart Web Notification Intelligence Hub
                    </p>
                </footer>

                <script>
                    // Add smooth scrolling and interactive effects
                    document.addEventListener('DOMContentLoaded', function() {
                        // Animate stats on scroll
                        const stats = document.querySelectorAll('.stat-number');
                        const observer = new IntersectionObserver((entries) => {
                            entries.forEach(entry => {
                                if (entry.isIntersecting) {
                                    entry.target.style.transform = 'scale(1.1)';
                                    setTimeout(() => {
                                        entry.target.style.transform = 'scale(1)';
                                    }, 200);
                                }
                            });
                        });

                        stats.forEach(stat => observer.observe(stat));

                        // Add hover effects to buttons
                        const buttons = document.querySelectorAll('.btn');
                        buttons.forEach(button => {
                            button.addEventListener('mouseenter', function() {
                                this.style.transform = 'translateY(-2px) scale(1.02)';
                            });
                            
                            button.addEventListener('mouseleave', function() {
                                this.style.transform = 'translateY(0) scale(1)';
                            });
                        });

                        // Parallax effect for floating elements
                        window.addEventListener('scroll', () => {
                            const scrolled = window.pageYOffset;
                            const parallax = document.querySelectorAll('.floating-element');
                            
                            parallax.forEach((element, index) => {
                                const speed = 0.5 + (index * 0.1);
                                element.style.transform = `translateY(${scrolled * speed}px)`;
                            });
                        });

                        console.log('🎉 SWNIH Homepage loaded successfully!');
                    });
                </script>
            </body>
            </html>
            """;
    }
}