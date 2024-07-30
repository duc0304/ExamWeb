// const sideLinks = document.querySelectorAll('.sidebar .side-menu li a:not(.logout)');
const sideLinks = document.querySelectorAll('.sidebar .side-menu li button:not(.logout)');
sideLinks.forEach(item => {
    const li = item.parentElement;
    item.addEventListener('click', () => {
        sideLinks.forEach(i => {
            i.parentElement.classList.remove('active');
        })
        li.classList.add('active');
    })
});

const menuBar = document.querySelector('.content nav .bx.bx-menu');
const sideBar = document.querySelector('.sidebar');

menuBar.addEventListener('click', () => {
    sideBar.classList.toggle('close');
});

const searchBtn = document.querySelector('.content nav form .form-input button');
const searchBtnIcon = document.querySelector('.content nav form .form-input button .bx');
const searchForm = document.querySelector('.content nav form');

searchBtn.addEventListener('click', function (e) {
    if (window.innerWidth < 576) {
        e.preventDefault;
        searchForm.classList.toggle('show');
        if (searchForm.classList.contains('show')) {
            searchBtnIcon.classList.replace('bx-search', 'bx-x');
        } else {
            searchBtnIcon.classList.replace('bx-x', 'bx-search');
        }
    }
});

window.addEventListener('resize', () => {
    if (window.innerWidth < 768) {
        sideBar.classList.add('close');
    } else {
        sideBar.classList.remove('close');
    }
    if (window.innerWidth > 576) {
        searchBtnIcon.classList.replace('bx-x', 'bx-search');
        searchForm.classList.remove('show');
    }
});

const toggler = document.getElementById('theme-toggle');

toggler.addEventListener('change', function () {
    if (this.checked) {
        document.body.classList.add('dark');
    } else {
        document.body.classList.remove('dark');
    }
});
document.getElementById("logoutForm").addEventListener("submit", function (event) {
    event.preventDefault(); // Ngăn chặn việc gửi biểu mẫu mặc định.
});

document.getElementById("logoutButton").addEventListener("click", function () {
    var confirmLogout = confirm("Bạn có muốn đăng xuất không?");
    if (confirmLogout) {
        // Nếu người dùng xác nhận đăng xuất, gửi biểu mẫu để chuyển hướng người dùng đến trang /logout.
        document.getElementById("logoutForm").submit();
    }
});

setTimeout(function () {
    var messageDiv = document.getElementById("messageDiv");
    if (messageDiv) {
        messageDiv.style.display = "none";
    }
}, 4000); // message disapear after 2s

checkSessionValidity();
checkAuthority();

function checkSessionValidity() {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                console.log('Session is valid');
                // Do something if the session is valid
            } else {
                console.log('Session is not valid');
                window.location.href = "/";
            }
        }
    };
    xhr.open('GET', '/api/v1/session/isSessionValid');
    xhr.send();
}

function checkAuthority() {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                console.log('Session is valid');
                // Do something if the session is valid
            } else {
                console.log('Session is not valid');
                window.location.href = "/403";
            }
        }
    };
    const currentURL = window.location.href;
    const url = new URL(currentURL);
    const pathname = url.pathname;
    const parts = pathname.split('/');
    const pagePermission = parts[1];
    xhr.open('GET', '/api/v1/session/isUserAuthorized?pagePermission=' + pagePermission);
    xhr.send();
}


