import { initializeApp } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-app.js";
import {
  getFirestore,
  collection,
  getDocs,
  getDoc,
  query,
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

let customerData = [];

// 1. Fetch Customers 
async function fetchUserData() {
  const usersCollection = collection(db, "users");
  const usersSnapshot = await getDocs(usersCollection);
  customerData = [];

  for (const userDoc of usersSnapshot.docs) {
    const userData = userDoc.data();
    const userId = userDoc.id;

    // Get orders with state "Booked"
    const ordersCollection = collection(db, `users/${userId}/orders`);
    const q = query(ordersCollection); // You could add a where clause if needed
    const ordersSnapshot = await getDocs(q);

    ordersSnapshot.forEach((orderDoc) => {
      const orderData = orderDoc.data();
      if (orderData.state === "Booked") {
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
      }
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
          data-phone="${customer.phone}"
          data-departuredate="${customer.departureDate}">
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

// Utility: Return an array of selected customer checkboxes
function getSelectedCustomers() {
  const checkboxes = document.querySelectorAll(".customer-checkbox:checked");
  return Array.from(checkboxes);
}

//Fetch Driver
async function showDriverModal() {
  const modalDriverList = document.getElementById("modalDriverList");
  modalDriverList.innerHTML = "<p>Loading drivers...</p>";

  // Query all drivers (since there's only one role now)
  const driversCollection = collection(db, "drivers");
  const q = query(driversCollection);
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
    html = "<p>No drivers found.</p>";
  }

  modalDriverList.innerHTML = html;

  // Show the modal using Bootstrap's modal API
  const driverModalEl = document.getElementById("driverModal");
  const driverModal = new bootstrap.Modal(driverModalEl);
  driverModal.show();
}


async function arrangeDriver(selectedDriverId) {
  const selectedCustomers = getSelectedCustomers();
  if (selectedCustomers.length === 0) {
    alert("Please select at least one customer.");
    return;
  }
  
  // Get the trip start time from the modal input
  const tripStartTimeInput = document.getElementById("tripStartTime");
  if (!tripStartTimeInput.value) {
    alert("Please select the trip start time.");
    return;
  }
  const tripStartTime = tripStartTimeInput.value;
  
  // Use the departure date from the first selected customer for the trip date
  const firstCheckbox = selectedCustomers[0];
  const tripDate = firstCheckbox.dataset.departuredate;
  
  // Create a new trip document in the driver's trips collection with both dateTrip and startTime
  const tripDocRef = await addDoc(collection(db, `drivers/${selectedDriverId}/trips`), {
    dateTrip: tripDate,
    startTime: tripStartTime,
  });
  
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
    
    // Add client data to the trip's "clients" subcollection
    await addDoc(collection(db, `drivers/${selectedDriverId}/trips/${tripDocRef.id}/clients`), {
      customerId: userId,
      customerName: username,
      phone: phone,
      pickupCoordinates: pickupCoordinates,
      destinationCoordinates: destinationCoordinates,
    });

    // Update the order document with the arrangement info
    const orderDocRef = doc(db, "users", userId, "orders", orderId);
    await updateDoc(orderDocRef, {
      state: "Arranged"
    });
  }
  
  alert("Driver arranged for selected customers in one trip!");
  fetchUserData();
}


document.getElementById("showDrivers").addEventListener("click", () => {
  showDriverModal();
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
