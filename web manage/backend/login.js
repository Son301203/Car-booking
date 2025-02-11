import { initializeApp } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-auth.js";
const firebaseConfig = {
  apiKey: "AIzaSyAk20GvUdvTXCN8ytCrcfu_c0xhEi-uGps",
  authDomain: "bookcar-ce16f.firebaseapp.com",
  projectId: "bookcar-ce16f",
  storageBucket: "bookcar-ce16f.firebasestorage.app",
  messagingSenderId: "32472269058",
  appId: "1:32472269058:web:31bdda1b7790d584f6421b",
  measurementId: "G-MSLG5YBGE5"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

const loginForm = document.getElementById("loginForm");

loginForm.addEventListener("submit", (e) => {
  e.preventDefault(); 

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  signInWithEmailAndPassword(auth, email, password)
    .then((userCredential) => {
      const user = userCredential.user;
      console.log("Login successful:", user);
      window.location.href = "arange_driver.html";
    })
    .catch((error) => {
      const errorCode = error.code;
      const errorMessage = error.message;
      console.error("Error logging in:", errorCode, errorMessage);

      alert("Login failed: " + errorMessage);
    });
});