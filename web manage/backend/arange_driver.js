import { initializeApp } from "https://www.gstatic.com/firebasejs/11.3.0/firebase-app.js";
import { 
  getFirestore, 
  collection, 
  getDocs, 
  query, 
  where 
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

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// Function to fetch users and their sub-collections (orders)
async function fetchUserData() {
  const usersCollection = collection(db, "users");
  const usersSnapshot = await getDocs(usersCollection);
  let userDataArray = [];

  // Iterate over each user document
  for (const userDoc of usersSnapshot.docs) {
    const userData = userDoc.data();
    const userId = userDoc.id;
    
    // Access the sub-collection "orders" for each user
    const ordersCollection = collection(db, `users/${userId}/orders`);

    // Create a query to only fetch documents where state is "booked"
    const q = query(ordersCollection, where("state", "==", "Booked"));
    const ordersSnapshot = await getDocs(q);

    // Process each order that matches the query
    ordersSnapshot.forEach((orderDoc) => {
      const orderData = orderDoc.data();
      userDataArray.push({
        username: userData.username,
        phone: userData.phone,
        pickup: orderData.pickup,
        destination: orderData.destination,
        departureDate: orderData.departureDate,
        returnDate: orderData.returnDate,
      });
    });
  }

  renderTable(userDataArray);
}

// Function to render data into the table
function renderTable(userDataArray) {
  const tbody = document.getElementById("tbody_user_info");
  tbody.innerHTML = "";

  userDataArray.forEach((user, index) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${index + 1}</td>
      <td>${user.username}</td>
      <td>${user.phone}</td>
      <td>${user.pickup}</td>
      <td>${user.destination}</td>
      <td>${user.departureDate}</td>
      <td>${user.returnDate}</td>
    `;
    tbody.appendChild(row);
  });
}

fetchUserData();
