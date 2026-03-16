/*==================== Toggle icon navbar ====================*/
document.addEventListener("DOMContentLoaded", function () {
    const menuIcon = document.querySelector('#menu-icon');
    const navbar = document.querySelector('.navbar');

    if (menuIcon && navbar) {
        menuIcon.addEventListener('click', () => {
            menuIcon.classList.toggle('bx-x');
            navbar.classList.toggle('active');
        });
    }

    /*==================== Scroll sections active link ====================*/
    const sections = document.querySelectorAll('section');
    const navLinks = document.querySelectorAll('header nav a');

    window.addEventListener('scroll', () => {
        const top = window.scrollY;

        sections.forEach(section => {
            const offset = section.offsetTop - 150;
            const height = section.offsetHeight;
            const id = section.getAttribute('id');

            if (top >= offset && top < offset + height) {
                navLinks.forEach(link => {
                    link.classList.remove('active');
                    const matchingLink = document.querySelector(`header nav a[href*="${id}"]`);
                    if (matchingLink) matchingLink.classList.add('active');
                });
            }
        });

        // Sticky header
        const header = document.querySelector('header');
        if (header) header.classList.toggle('sticky', window.scrollY > 100);

        // Close navbar on scroll
        if (menuIcon && navbar) {
            menuIcon.classList.remove('bx-x');
            navbar.classList.remove('active');
        }
    });

    /*==================== Scroll Reveal ====================*/
    if (typeof ScrollReveal !== 'undefined') {
        ScrollReveal({
            reset: true,
            distance: '80px',
            duration: 2000,
            delay: 200
        });

        ScrollReveal().reveal('.home-content, .heading', { origin: 'top' });
        ScrollReveal().reveal('.home-img, .services-container, .portfolio-box, .contact form', { origin: 'bottom' });
        ScrollReveal().reveal('.home-content h1, .about-img', { origin: 'left' });
        ScrollReveal().reveal('.home-content p, .about-content', { origin: 'right' });
    }

    /*==================== Typed.js ====================*/
    if (document.querySelector('.multiple-text')) {
        try {
            new Typed('.multiple-text', {
                strings: ['Computer Science Student'],
                typeSpeed: 100,
                backSpeed: 100,
                backDelay: 1000,
                loop: true
            });
        } catch (err) {
            console.warn("Typed.js failed to load or init:", err);
        }
    }

    /*==================== Login Form Handling ====================*/
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            const fullName = document.getElementById('loginFullName').value.trim();
            const email = document.getElementById('loginEmail').value.trim();
            const region = document.getElementById('loginRegion').value;

            if (!fullName || !email || !region) {
                alert("Please fill all fields.");
                return;
            }

            // Save user info to localStorage
            localStorage.setItem('fullName', fullName);
            localStorage.setItem('email', email);
            localStorage.setItem('region', region);

            try {
                // Use local backend URL
                const response = await fetch('http://localhost:8080/users/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ fullName, email, region })
                });

                if (!response.ok) throw new Error('Login/register failed');

                const data = await response.json();
                console.log('User saved in DB:', data);

                // Go to booking page
                window.location.href = 'booking.html';
            } catch (error) {
                console.error('Login error:', error);
                alert('Login failed. Try again.');
            }
        });
    }

    /*==================== Booking Form Handling ====================*/
    const bookingForm = document.getElementById('paymentForm');
    if (bookingForm) {
        bookingForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const userName = localStorage.getItem('fullName') || 'Unknown';
            const userEmail = localStorage.getItem('email');
            const userRegion = localStorage.getItem('region') || 'Unknown';

            const concertSelect = document.getElementById('concertSelect');
            const selectedOption = concertSelect.options[concertSelect.selectedIndex];
            const concertId = selectedOption.value;
            const price = selectedOption.getAttribute('data-price');
            const file = document.getElementById('proof').files[0];

            if (!file || !userEmail) {
                alert("Missing required data.");
                return;
            }

            const formData = new FormData();
            formData.append("proof", file);
            formData.append("userEmail", userEmail);
            formData.append("concertId", concertId);
            formData.append("price", parseFloat(price));

            try {
                // Upload payment
                const paymentRes = await fetch('http://localhost:8080/payments', {
                    method: 'POST',
                    body: formData
                });

                if (!paymentRes.ok) throw new Error('Payment failed');

                // Create booking
                const bookingRes = await fetch('http://localhost:8080/bookings', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ userEmail, concertId })
                });

                if (!bookingRes.ok) throw new Error('Booking failed');

                localStorage.setItem('bookedConcert', selectedOption.textContent);
                window.location.href = 'success.html';
            } catch (err) {
                console.error(err);
                alert(err.message);
            }
        });
    }
});
