import { initializeApp } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-app.js";
import {
  getFirestore,
  collection,
  getDocs,
  getDoc,
  query,
  where,
  addDoc,
  updateDoc,
  doc
} from "https://www.gstatic.com/firebasejs/11.3.0/firebase-firestore.js";

// Firebase configuration
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
const db = getFirestore(app);

// Global array to store customer data
let customerData = [];

// Global variable to store the current driver role ("transit" or "main")
let currentRole = "";


async function fetchUserData() {
  const usersCollection = collection(db, "users");
  const usersSnapshot = await getDocs(usersCollection);
  customerData = [];

  for (const userDoc of usersSnapshot.docs) {
    const userData = userDoc.data();
    const userId = userDoc.id;

    // Access the subcollection "orders" for each user and only get orders where state is "Booked"
    const ordersCollection = collection(db, `users/${userId}/orders`);
    const q = query(ordersCollection, where("state", "==", "Booked"));
    const ordersSnapshot = await getDocs(q);

    ordersSnapshot.forEach((orderDoc) => {
      const orderData = orderDoc.data();
      customerData.push({
        userId,
        orderId: orderDoc.id,
        username: userData.username,
        phone: userData.phone,
        pickup: orderData.pickup,
        destination: orderData.destination,
        departureDate: orderData.departureDate,
        returnDate: orderData.returnDate,
      });
    });
  }

  renderTable(customerData);
}

function renderTable(dataArray) {
  const tbody = document.getElementById("tbody_user_info");
  tbody.innerHTML = "";
  dataArray.forEach((customer, index) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>
        <input type="checkbox" class="customer-checkbox"
          data-userid="${customer.userId}"
          data-orderid="${customer.orderId}"
          data-username="${customer.username}"
          data-phone="${customer.phone}">
      </td>
      <td>${index + 1}</td>
      <td>${customer.username}</td>
      <td>${customer.phone}</td>
      <td>${customer.pickup}</td>
      <td>${customer.destination}</td>
      <td>${customer.departureDate}</td>
      <td>${customer.returnDate}</td>
    `;
    tbody.appendChild(row);
  });
}

//Return an array of selected customer checkboxes
function getSelectedCustomers() {
  const checkboxes = document.querySelectorAll(".customer-checkbox:checked");
  return Array.from(checkboxes);
}


// show driver for roll
async function showDriverModal(role) {
  currentRole = role; 
  const modalDriverList = document.getElementById("modalDriverList");
  modalDriverList.innerHTML = "<p>Loading drivers...</p>";

  const driversCollection = collection(db, "drivers");
  const q = query(driversCollection, where("roll", "==", role));
  const driversSnapshot = await getDocs(q);

  let html = "";
  driversSnapshot.forEach((doc) => {
    const driver = doc.data();
    html += `
      <div>
        <input type="radio" name="selectedDriver" value="${doc.id}" id="driver_${doc.id}">
        <label for="driver_${doc.id}">${driver.name} - ${driver.phone}</label>
      </div>
    `;
  });

  if (html === "") {
    html = "<p>No drivers found for role: " + role + "</p>";
  }

  modalDriverList.innerHTML = html;

  const driverModalEl = document.getElementById("driverModal");
  const driverModal = new bootstrap.Modal(driverModalEl);
  driverModal.show();
}

// Arrange Selected Driver for Each Selected Customer

async function arrangeDriver(selectedDriverId) {
  const selectedCustomers = getSelectedCustomers();
  if (selectedCustomers.length === 0) {
    alert("Please select at least one customer.");
    return;
  }
  
  for (const checkbox of selectedCustomers) {
    const userId = checkbox.dataset.userid;
    const orderId = checkbox.dataset.orderid;
    const username = checkbox.dataset.username;
    const phone = checkbox.dataset.phone;
    
    // Fetch pickup coordinates
    const departureCol = collection(db, `users/${userId}/orders/${orderId}/departureCoordinates`);
    const coordsSnapshot = await getDocs(departureCol);
    let pickupCoordinates = null;
    coordsSnapshot.forEach((doc) => {
      pickupCoordinates = doc.data(); 
    });

    // Fetch destination coordinates
    const destinationCol = collection(db, `users/${userId}/orders/${orderId}/destinationCoordinates`);
    const destCoordsSnapshot = await getDocs(destinationCol);
    let destinationCoordinates = null;
    destCoordsSnapshot.forEach((doc) => {
      destinationCoordinates = doc.data(); 
    });
    
    // Add a new document to the driver's trips subcollection
    await addDoc(collection(db, `drivers/${selectedDriverId}/trips`), {
      customerId: userId,
      customerName: username,
      phone: phone,
      pickupCoordinates: pickupCoordinates,
      destinationCoordinates: destinationCoordinates,
      arrangedAt: new Date()
    });

    // Build the update object for the order based on the current role
    let updateData = {};
    if (currentRole === "transit") {
      updateData.transitDriverArranged = true;
      updateData.transitDriverId = selectedDriverId;
    } else if (currentRole === "main") {
      updateData.mainDriverArranged = true;
      updateData.mainDriverId = selectedDriverId;
    }

    // Update the order document with the arrangement info
    const orderDocRef = doc(db, "users", userId, "orders", orderId);
    await updateDoc(orderDocRef, updateData);

    // Retrieve the updated order document to check if both drivers have been arranged
    const orderSnapshot = await getDoc(orderDocRef);
    const orderData = orderSnapshot.data();
    if (orderData.transitDriverArranged && orderData.mainDriverArranged) {
      //update the state to "Arranged"
      await updateDoc(orderDocRef, { state: "Arranged" });
    }
  }
  
  alert("Driver arranged for selected customers!");
  fetchUserData();
}

document.getElementById("showTransitDrivers").addEventListener("click", () => {
  showDriverModal("transit");
});

document.getElementById("showMainDrivers").addEventListener("click", () => {
  showDriverModal("main");
});

document.getElementById("modalArrangeDriverBtn").addEventListener("click", () => {
  const selectedRadio = document.querySelector('input[name="selectedDriver"]:checked');
  if (!selectedRadio) {
    alert("Please select a driver.");
    return;
  }
  const driverId = selectedRadio.value;
  arrangeDriver(driverId);
});


fetchUserData();
