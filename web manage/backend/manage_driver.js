import { initializeApp } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-app.js";
import { getFirestore, collection, addDoc, getDocs, deleteDoc, doc, updateDoc } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-firestore.js";

// Firebase Configuration
const firebaseConfig = {
    apiKey: "AIzaSyAk20GvUdvTXCN8ytCrcfu_c0xhEi-uGps",
    authDomain: "bookcar-ce16f.firebaseapp.com",
    projectId: "bookcar-ce16f",
    storageBucket: "bookcar-ce16f.firebasestorage.app",
    messagingSenderId: "32472269058",
    appId: "1:32472269058:web:31bdda1b7790d584f6421b",
    measurementId: "G-MSLG5YBGE5"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

const driverForm = document.getElementById("driverForm");
const driverList = document.getElementById("driverList");
const editModal = document.getElementById("editModal");
const updateButton = document.getElementById("updateDriver");
let editDriverId = "";

// Add Driver
async function addDriver(e) {
    e.preventDefault();
    
    const name = document.getElementById("name").value;
    const phone = document.getElementById("phone").value;
    const license = document.getElementById("license").value;
    const identification = document.getElementById("identification").value;
    const date_of_birth = document.getElementById("date_of_birth").value;
    const email = document.getElementById("email").value;
    const gender = document.getElementById("gender").value;
    const roll = document.getElementById("roll").value;
    
    try {
        await addDoc(collection(db, "drivers"), {
            name, phone, license, identification, date_of_birth, email, gender, roll
        });
        alert("Driver added successfully!");
        driverForm.reset();
        fetchDrivers();
    } catch (error) {
        console.error("Error adding driver: ", error);
    }
}

driverForm.addEventListener("submit", addDriver);

// Fetch & Display Drivers
async function fetchDrivers() {
    driverList.innerHTML = "";
    const querySnapshot = await getDocs(collection(db, "drivers"));
    querySnapshot.forEach((doc) => {
        const driver = doc.data();
        driverList.innerHTML += `
            <tr>
                <td>${driver.name}</td>
                <td>${driver.phone}</td>
                <td>${driver.license}</td>
                <td>${driver.identification}</td>
                <td>${driver.date_of_birth}</td>
                <td>${driver.email}</td>
                <td>${driver.gender}</td>
                <td>${driver.roll}</td>
                <td>
                    <button class="btn btn-warning btn-sm" onclick="openEditForm('${doc.id}', '${driver.name}', '${driver.phone}', '${driver.license}', '${driver.identification}', '${driver.date_of_birth}', '${driver.email}', '${driver.gender}', '${driver.roll}')">✏️ Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteDriver('${doc.id}')">🗑️ Delete</button>
                </td>
            </tr>
        `;
    });
}

// Open Edit Form
window.openEditForm = function (id, name, phone, license, identification, date_of_birth, email, gender, roll) {
    document.getElementById("edit_name").value = name;
    document.getElementById("edit_phone").value = phone;
    document.getElementById("edit_license").value = license;
    document.getElementById("edit_identification").value = identification;
    document.getElementById("edit_date_of_birth").value = date_of_birth;
    document.getElementById("edit_email").value = email;
    document.getElementById("edit_gender").value = gender;
    document.getElementById("edit_roll").value = roll;
    editDriverId = id;
    editModal.style.display = "block";
};

// Update Driver
updateButton.addEventListener("click", async () => {
    try {
        await updateDoc(doc(db, "drivers", editDriverId), {
            name: document.getElementById("edit_name").value,
            phone: document.getElementById("edit_phone").value,
            license: document.getElementById("edit_license").value,
            identification: document.getElementById("edit_identification").value,
            date_of_birth: document.getElementById("edit_date_of_birth").value,
            email: document.getElementById("edit_email").value,
            gender: document.getElementById("edit_gender").value,
            roll: document.getElementById("edit_roll").value
        });
        alert("Driver updated successfully!");
        editModal.style.display = "none";
        fetchDrivers();
    } catch (error) {
        console.error("Error updating driver: ", error);
    }
});

// Delete Driver
window.deleteDriver = async function (id) {
    if (confirm("Are you sure you want to delete this driver?")) {
        try {
            await deleteDoc(doc(db, "drivers", id));
            alert("Driver deleted successfully!");
            fetchDrivers();
        } catch (error) {
            console.error("Error deleting driver: ", error);
        }
    }
};

fetchDrivers();
