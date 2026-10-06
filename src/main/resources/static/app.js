const authSection = document.querySelector("#authSection");
const courseSection = document.querySelector("#courseSection");
const loginForm = document.querySelector("#loginForm");
const registerForm = document.querySelector("#registerForm");
const loginTab = document.querySelector("#loginTab");
const registerTab = document.querySelector("#registerTab");
const logoutButton = document.querySelector("#logoutButton");
const message = document.querySelector("#message");

let token = localStorage.getItem("studyhubToken");
let currentUser = JSON.parse(localStorage.getItem("studyhubUser") || "null");

function showMessage(text, error = false) {
    message.textContent = text;
    message.classList.toggle("error", error);
    message.classList.remove("hidden");
    setTimeout(() => message.classList.add("hidden"), 3500);
}

function selectTab(name) {
    const loginSelected = name === "login";
    loginForm.classList.toggle("hidden", !loginSelected);
    registerForm.classList.toggle("hidden", loginSelected);
    loginTab.classList.toggle("active", loginSelected);
    registerTab.classList.toggle("active", !loginSelected);
}

async function request(path, options = {}) {
    const headers = {"Content-Type": "application/json", ...options.headers};
    if (token) headers.Authorization = `Bearer ${token}`;
    const response = await fetch(path, {...options, headers});
    const body = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(body?.message || "Не удалось выполнить запрос");
    }
    return body;
}

function saveSession(data) {
    token = data.token;
    currentUser = {name: data.name, email: data.email, role: data.role};
    localStorage.setItem("studyhubToken", token);
    localStorage.setItem("studyhubUser", JSON.stringify(currentUser));
    showCourses();
}

function showAuth() {
    authSection.classList.remove("hidden");
    courseSection.classList.add("hidden");
    logoutButton.classList.add("hidden");
}

async function showCourses() {
    authSection.classList.add("hidden");
    courseSection.classList.remove("hidden");
    logoutButton.classList.remove("hidden");
    const role = currentUser?.role === "ADMIN" ? "администратор" : "пользователь";
    document.querySelector("#userInfo").textContent =
        `${currentUser?.name || "Пользователь"} · роль: ${role}`;
    await loadCourses();
}

async function loadCourses() {
    try {
        const courses = await request("/api/courses");
        const list = document.querySelector("#courseList");
        const empty = document.querySelector("#emptyMessage");
        list.replaceChildren();
        empty.classList.toggle("hidden", courses.length !== 0);

        for (const course of courses) {
            const card = document.createElement("article");
            card.className = "course-card";

            const title = document.createElement("h3");
            title.textContent = course.title;
            const description = document.createElement("p");
            description.textContent = course.description;
            const status = document.createElement("span");
            status.className = "course-status";
            status.textContent = course.published ? "Опубликован" : "Черновик";

            card.append(title, description, status);
            list.append(card);
        }
    } catch (error) {
        if (error.message.includes("JWT")) logout();
        showMessage(error.message, true);
    }
}

function logout() {
    token = null;
    currentUser = null;
    localStorage.removeItem("studyhubToken");
    localStorage.removeItem("studyhubUser");
    showAuth();
}

loginTab.addEventListener("click", () => selectTab("login"));
registerTab.addEventListener("click", () => selectTab("register"));
logoutButton.addEventListener("click", logout);
document.querySelector("#refreshButton").addEventListener("click", loadCourses);

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = await request("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: document.querySelector("#loginEmail").value,
                password: document.querySelector("#loginPassword").value
            })
        });
        saveSession(data);
        showMessage("Вход выполнен");
    } catch (error) {
        showMessage(error.message, true);
    }
});

registerForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = await request("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({
                name: document.querySelector("#registerName").value,
                email: document.querySelector("#registerEmail").value,
                password: document.querySelector("#registerPassword").value
            })
        });
        saveSession(data);
        showMessage("Регистрация завершена");
    } catch (error) {
        showMessage(error.message, true);
    }
});

document.querySelector("#courseForm").addEventListener("submit", async event => {
    event.preventDefault();
    try {
        await request("/api/courses", {
            method: "POST",
            body: JSON.stringify({
                title: document.querySelector("#courseTitle").value,
                description: document.querySelector("#courseDescription").value,
                published: document.querySelector("#coursePublished").checked,
                tagIds: []
            })
        });
        event.target.reset();
        await loadCourses();
        showMessage("Курс создан");
    } catch (error) {
        showMessage(error.message, true);
    }
});

if (token && currentUser) {
    showCourses();
} else {
    showAuth();
}
