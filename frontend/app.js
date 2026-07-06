// ==========================================================================
// DELIVO APP.JS - ENTERPRISE MARKETPLACE & LOGISTICS ROUTER
// ==========================================================================

// Global Store & Database States
let cartItems = [];
let selectedPayment = "CARD";
let appliedDiscount = 0.0;
let selectedTip = 0.00;
let walletBalance = 1600.00; // Aligned with $1600.00 wallet in screenshot
let loyaltyPoints = 450;
let isDriverOnline = false;
let sessionTimeoutTimer = null;
let loginFailedAttempts = 0;

// User coordinates default to Maharashtra, India
let userCoordinates = { lat: 19.7515, lon: 75.7139 };

// Coupon database (Admin Hub Coupon Manager)
let couponDatabase = [
    { code: "MEGA50", value: 50, active: true },
    { code: "DELIVO20", value: 20, active: true },
    { code: "FREEFEES", value: 10, active: true }
];

// Mailbox Database (Ticket Visual Inbox parity compliance)
let mockEmails = [
    { id: "mail_1", folder: "inbox", from: "Delivo Support", subject: "Welcome to Delivo OS!", body: "Hi Alice,\n\nWelcome to your enterprise food delivery orchestrator. Explore microservice latencies, place test orders, and configure security flags inside the admin console.\n\nBest,\nDelivo Team", date: "2026-07-02 10:00", read: false },
    { id: "mail_2", folder: "inbox", from: "General Ledger Service", subject: "Daily Ledger Balance Reconciled", body: "Ledger report for 2026-07-01:\nTotal Debit: $1,420,550.00\nTotal Credit: $1,420,550.00\nStatus: Balanced.\nNo anomalies detected.", date: "2026-07-02 08:30", read: true },
    { id: "mail_3", folder: "inbox", from: "Fraud Monitoring System", subject: "Security Alert: Spoofing attempt blocked", body: "An anomaly score of 92% was flagged on user usr_9088 due to rapid double-submit checkout attempts. IP address blocked.", date: "2026-07-01 19:45", read: false },
    { id: "mail_4", folder: "drafts", from: "You", subject: "Refund request for order #tx_p90814", body: "Draft email requesting transaction ledger refund for cryptocurrency portal failure...", date: "2026-07-02 12:15", read: true }
];
let activeMailFolder = "inbox";
let activeMailId = null;

// Custom Toast notification system
function showToast(message, type = "info") {
    const container = document.getElementById("toast-container");
    if (!container) return;
    
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    
    let icon = "fa-circle-info";
    if (type === "success") icon = "fa-circle-check";
    else if (type === "error") icon = "fa-circle-xmark";
    else if (type === "warning") icon = "fa-circle-exclamation";
    
    toast.innerHTML = `
        <i class="fa-solid ${icon}"></i>
        <span style="flex-grow: 1;">${message}</span>
        <button style="background:none; border:none; color:var(--text-muted); cursor:pointer; font-size:14px; margin-left:10px; line-height: 1;" onclick="this.parentElement.remove()">&times;</button>
    `;
    
    container.appendChild(toast);
    
    // Auto dismiss after 4 seconds
    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(120%)";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Custom Confirm modal system
let confirmCallback = null;
function showConfirm(title, message, callback) {
    const modal = document.getElementById("confirm-modal");
    if (!modal) {
        // Fallback to confirm
        if (confirm(message)) callback();
        return;
    }
    document.getElementById("confirm-modal-title").innerText = title;
    document.getElementById("confirm-modal-message").innerText = message;
    modal.style.display = "flex";
    confirmCallback = callback;
}

// Leaflet Map Reference
let leafletMap = null;
let driverMarker = null;
let routeLine = null;
let homeMarker = null;
let restaurantMarker = null;
let activeMapStyle = "light";

// Chart.js Reference
let adminChart = null;

// PWA Install Event Ref
let deferredInstallPrompt = null;

// FAQ Mock Data (Ticket #28 compliance)
const faqData = [
    { q: "How do I track my order in real-time?", a: "Once your order is confirmed, a live tracking map appears showing your delivery partner's location updated every 5 seconds. You'll receive push notifications at each milestone — restaurant accepted, food being prepared, picked up, and arriving." },
    { q: "What payment methods are accepted?", a: "We accept credit/debit cards, UPI, net banking, and Delivoos Wallet. All transactions are PCI-DSS compliant with end-to-end encryption. You can also save cards securely for faster future checkouts." },
    { q: "Can I cancel or modify my order after placing it?", a: "Yes, you can cancel within 60 seconds of placing the order for a full refund. After the restaurant starts preparing, partial refunds may apply. Order modifications (add/remove items) are supported until the restaurant accepts." },
    { q: "What happens if my payment fails?", a: "If a payment transaction fails, our system automatically cancels the order request and unlocks the reserved funds in your wallet instantly. You won't be charged for failed transactions." },
    { q: "Are orders available offline?", a: "Yes, the service worker caches critical pages and your recent orders. If connectivity drops, you can browse menus and view past orders. Pending actions sync automatically when you're back online." },
    { q: "How is dynamic surge pricing calculated?", a: "Surge pricing is calculated using active order counts per geohash sector and real-time driver availability. Prices normalize within 15-20 minutes as demand balances. Admins can override surge multipliers from the dashboard." }
];

// Rich Mock Databases (30+ Restaurants, 100+ Foods generated dynamically)
const mockRestaurantsData = [
    { name: "Bella Napoli Pizza", cuisine: "Pizza & Pasta", image: "images/bella_napoli_pizza.png", desc: "Traditional wood-fired pizzas crafted with imported San Marzano tomatoes." },
    { name: "GBK Gourmet Burger", cuisine: "Burgers & Fast Food", image: "images/gourmet_burger_kitchen.png", desc: "Premium prime-rib custom burgers layered with sharp cheddar and avocado." },
    { name: "Sake Sushi House", cuisine: "Asian & Sushi", image: "images/sake_sushi_house.png", desc: "Freshly sliced sashimi grade salmon and signature handcrafted sushi rolls." },
    { name: "Beijing Bites Wok", cuisine: "Asian & Sushi", image: "https://images.unsplash.com/photo-1552611052-33e04de081de?auto=format&fit=crop&w=300&q=80", desc: "Classic stir-fried noodles, sweet & sour chicken, and steamed dumplings." },
    { name: "Pasta Palace Bistro", cuisine: "Pizza & Pasta", image: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=300&q=80", desc: "Creamy fettuccine alfredo and traditional Italian carbonara." },
    { name: "Waffle Garden Desserts", cuisine: "Desserts & Cakes", image: "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=300&q=80", desc: "Warm Belgian waffles drizzled with organic honey and fresh berries." },
    { name: "Green Garden Salad", cuisine: "healthy Choices", image: "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=300&q=80", desc: "Crisp organic baby greens, toasted quinoa, and cold-pressed olive oils." },
    { name: "Burger Craft", cuisine: "Burgers & Fast Food", image: "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=300&q=80", desc: "Juicy flame-grilled patties served with seasoned sweet potato fries." },
    { name: "Taco Loco Mexican", cuisine: "Burgers & Fast Food", image: "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=300&q=80", desc: "Soft corn tortilla tacos loaded with grilled steak, onions, and fresh cilantro." },
    { name: "Curry Bistro", cuisine: "Asian & Sushi", image: "https://images.unsplash.com/photo-1585938338392-50a592202c7b?auto=format&fit=crop&w=300&q=80", desc: "Slow-simmered rich butter chicken and traditional clay-oven garlic naan." },
    { name: "Tandoori Nights", cuisine: "Pizza & Pasta", image: "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?auto=format&fit=crop&w=300&q=80", desc: "Fusion pizzas topped with spiced tandoori paneer and mint chutney." },
    { name: "Healthy Bowl Cafe", cuisine: "healthy Choices", image: "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=300&q=80", desc: "High-protein vegan bowls topped with organic edamame and sesame seeds." },
    { name: "Biryani Express", cuisine: "Asian & Sushi", image: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=300&q=80", desc: "Traditional slow-cooked basmati rice loaded with aromatic saffron spices." },
    { name: "Cake House Bakeries", cuisine: "Desserts & Cakes", image: "https://images.unsplash.com/photo-1550617931-e17a7b70dce2?auto=format&fit=crop&w=300&q=80", desc: "Red velvet layer cakes, gluten-free brownies, and chocolate fudge." },
    { name: "Sol Sushi Lounge", cuisine: "Asian & Sushi", image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=300&q=80", desc: "Modern sushi bar serving premium tempura rolls and hot sake tea." }
];

const uniqueRestaurantImages = [
    "images/bella_napoli_pizza.png",
    "images/gourmet_burger_kitchen.png",
    "images/sake_sushi_house.png",
    "https://images.unsplash.com/photo-1552611052-33e04de081de?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1585938338392-50a592202c7b?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1550617931-e17a7b70dce2?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1565958011703-44f9829ba187?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1484723091739-30a097e8f929?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1529042410759-befb1204b468?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1493770308161-fdc199e7c1d8?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1506084868230-bb9d95c24759?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1533777857889-4be7c70b33f7?auto=format&fit=crop&w=300&q=80",
    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=300&q=80"
];

const mockStores = [];
const mockMenus = {};

// Build 30 distinct restaurants by adding variation suffix/prefix
for (let i = 1; i <= 30; i++) {
    const base = mockRestaurantsData[(i - 1) % mockRestaurantsData.length];
    const storeId = `store-${i}`;
    const restName = i <= 15 ? base.name : `${base.name} (East Branch)`;
    
    // Assign coordinates in Maharashtra, India
    const lat = 19.7515 + (Math.sin(i) * 0.15);
    const lon = 75.7139 + (Math.cos(i) * 0.15);
    
    mockStores.push({
        id: storeId,
        name: restName,
        cuisine: base.cuisine,
        rating: (4.1 + (i * 0.13) % 0.8).toFixed(1),
        time: `${15 + (i * 5) % 25} min`,
        desc: base.desc,
        image: uniqueRestaurantImages[i - 1] || "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=300&q=80",
        reviewsCount: 50 + (i * 18) % 350,
        deliveryFee: i % 4 === 0 ? 0.00 : (1.99 + (i * 0.4) % 3.00),
        minOrder: 10.00 + (i * 1.50) % 15.00,
        lat: lat,
        lon: lon,
        distanceVal: 0, // Calculated relative to user
        distance: "Calculating..."
    });

    // Populate dynamic menus divided into starters, mains, pizzas, burgers, drinks, and desserts
    mockMenus[storeId] = [
        // Starters
        { id: `${storeId}-m1`, name: "Mozzarella Sticks", price: 6.99, desc: "Crispy fried mozzarella cheese served with warm marinara dip.", calories: 380, image: "https://images.unsplash.com/photo-1531749668029-2db88e4b76ce?auto=format&fit=crop&w=80&h=80&q=80", category: "Starters" },
        { id: `${storeId}-m2`, name: "Garlic Parmesan Bread", price: 5.99, desc: "Toasted artisan sourdough brushed with garlic butter and parsley.", calories: 290, image: "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=80&h=80&q=80", category: "Starters" },
        
        // Mains
        { id: `${storeId}-m3`, name: "Pesto Penne Pasta", price: 14.99, desc: "Fresh basil pesto sauce tossed with pine nuts and cherry tomatoes.", calories: 720, image: "https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=80&h=80&q=80", category: "Main Course" },
        { id: `${storeId}-m4`, name: "Beijing Orange Chicken", price: 13.99, desc: "Crispy chicken breast chunks tossed in sweet citrus orange glaze.", calories: 680, image: "https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=80&h=80&q=80", category: "Main Course" },
        
        // Pizza
        { id: `${storeId}-m5`, name: "Margherita Classic Pizza", price: 12.99, desc: "Mozzarella, organic sweet basil, and extra virgin olive oil.", calories: 650, image: "https://images.unsplash.com/photo-1574071318508-1cdbab80d001?auto=format&fit=crop&w=80&h=80&q=80", category: "Pizza" },
        { id: `${storeId}-m6`, name: "Truffle Mushroom Pizza", price: 16.99, desc: "Wild mushrooms, white truffle oil, and creamy ricotta cheese.", calories: 850, image: "https://images.unsplash.com/photo-1571407970349-bc81e7e96d47?auto=format&fit=crop&w=80&h=80&q=80", category: "Pizza" },
        
        // Burgers
        { id: `${storeId}-m7`, name: "Classic Cheddar Burger", price: 9.99, desc: "Flame-grilled angus beef, sharp cheddar, lettuce, and secret sauce.", calories: 750, image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=80&h=80&q=80", category: "Burgers" },
        { id: `${storeId}-m8`, name: "Bacon Avocado Burger", price: 12.99, desc: "Crispy bacon, sliced avocado, swiss cheese, and garlic aioli.", calories: 920, image: "https://images.unsplash.com/photo-1553979459-d2229ba7433b?auto=format&fit=crop&w=80&h=80&q=80", category: "Burgers" },
        
        // Drinks
        { id: `${storeId}-m9`, name: "Cold-Pressed Detox Juice", price: 5.99, desc: "Fresh organic cucumber, green apple, spinach, and ginger root.", calories: 80, image: "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=80&h=80&q=80", category: "Drinks" },
        
        // Desserts
        { id: `${storeId}-m10`, name: "Double Chocolate Belgian Waffle", price: 8.99, desc: "Topped with warm dark fudge, milk chocolate chips, and whipped cream.", calories: 720, image: "https://images.unsplash.com/photo-1562376502-6f769499c886?auto=format&fit=crop&w=80&h=80&q=80", category: "Desserts" }
    ];
}

// Saved Card details for Ticket #37
let savedCards = [
    { id: "c1", number: "**** **** **** 4242", expiry: "12/28" }
];

// Wallet transaction logs
let walletLedger = [
    { id: "tx_ledger_1", time: "2026-06-30 18:22:15", type: "TOPUP", amount: 200.00, bal: 1600.00 }
];

// Active/Past Order history
let orderHistory = [
    // 5 PENDING
    { id: "tx_ord_101", date: "2026-07-02", items: "2x Margherita Classic Pizza", amount: 25.98, status: "PENDING", customerName: "Alice Smith", customerImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_102", date: "2026-07-02", items: "1x Classic Cheddar Burger, 1x Loaded Cheese Fries", amount: 16.98, status: "PENDING", customerName: "Bob Johnson", customerImage: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_103", date: "2026-07-02", items: "3x Salmon Nigiri Sushi platters", amount: 47.97, status: "PENDING", customerName: "Charlie Brown", customerImage: "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_104", date: "2026-07-02", items: "1x Avocado Quinoa Green Bowl, 1x Detox Juice", amount: 17.98, status: "PENDING", customerName: "Diana Prince", customerImage: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_105", date: "2026-07-02", items: "2x Double Chocolate Waffles", amount: 17.98, status: "PENDING", customerName: "Evan Wright", customerImage: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=48&h=48&q=80" },
    
    // 5 PREPARING / ACTIVE
    { id: "tx_ord_106", date: "2026-07-02", items: "1x Margherita Classic Pizza, 1x Pesto Pasta", amount: 27.98, status: "PREPARING", customerName: "Fiona Gallagher", customerImage: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_107", date: "2026-07-02", items: "2x Bacon Avocado Burger", amount: 25.98, status: "ASSIGNED", customerName: "George Clark", customerImage: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_108", date: "2026-07-02", items: "1x Orange Chicken, 1x Spicy Ramen Bowl", amount: 28.98, status: "PICKED_UP", customerName: "Hannah Abbott", customerImage: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_109", date: "2026-07-02", items: "2x Organic Tofu & Sesame Salad", amount: 21.98, status: "EN_ROUTE", customerName: "Ian Malcolm", customerImage: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_110", date: "2026-07-02", items: "1x Red Velvet Cake, 1x Vanilla Shake", amount: 11.98, status: "PREPARING", customerName: "Julia Roberts", customerImage: "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=48&h=48&q=80" },

    // 5 DELIVERED
    { id: "tx_ord_111", date: "2026-07-01", items: "1x Master Chef Specialty Dish", amount: 22.82, status: "DELIVERED", customerName: "Kevin Bacon", customerImage: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_112", date: "2026-07-01", items: "2x Classic Cheddar Burger", amount: 19.98, status: "DELIVERED", customerName: "Laura Croft", customerImage: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_113", date: "2026-07-01", items: "1x Salmon Nigiri Sushi platters", amount: 15.99, status: "DELIVERED", customerName: "Marcus Aurelius", customerImage: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_114", date: "2026-07-01", items: "1x Avocado Quinoa Green Bowl", amount: 11.99, status: "DELIVERED", customerName: "Natalie Portman", customerImage: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=48&h=48&q=80" },
    { id: "tx_ord_115", date: "2026-07-01", items: "1x Double Chocolate Waffle", amount: 8.99, status: "DELIVERED", customerName: "Oliver Twist", customerImage: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=48&h=48&q=80" }
];

// ==========================================================================
// INIT APP & BIND EVENT DELEGATORS (TICKET #2 COMPLIANCE)
// ==========================================================================
document.addEventListener("DOMContentLoaded", () => {
    // Apply visual theme from cache
    const cachedTheme = localStorage.getItem("delivo-theme") || "light";
    document.body.setAttribute("data-theme", cachedTheme);
    const themeIcon = document.getElementById("theme-icon");
    if (themeIcon) {
        themeIcon.className = cachedTheme === "light" ? "fa-solid fa-moon" : "fa-solid fa-sun";
    }

    checkAuthOnLoad();

    // Load wallet and profiles
    syncLocalWalletBalances();
    renderDynamicFaqs();
    renderSavedCards();
    renderWalletLedger();
    renderOrderHistory();
    renderKdsBoard();
    renderFraudAnomalies();
    startSessionTimeoutMonitor();

    // Render original catalog list
    renderCatalog(mockStores);

    // Global Event Delegation
    document.addEventListener("click", handleGlobalClicks);
    document.addEventListener("input", handleGlobalInputs);

    // Debounced Search Autocomplete setup
    const searchBar = document.getElementById("search-bar-input");
    if (searchBar) {
        searchBar.addEventListener("input", debounce(e => {
            triggerSearch(e.target.value);
        }, 300));
    }

    // Menu Search & Sorting inputs (Screenshot 2 compliance)
    const menuSearch = document.getElementById("menu-search-input");
    const menuSort = document.getElementById("menu-sort-select");
    if (menuSearch) {
        menuSearch.addEventListener("input", e => {
            const sortBy = menuSort ? menuSort.value : "default";
            openMenu(currentOpenRestaurant, e.target.value, sortBy);
        });
    }
    if (menuSort) {
        menuSort.addEventListener("change", e => {
            const searchVal = menuSearch ? menuSearch.value : "";
            openMenu(currentOpenRestaurant, searchVal, e.target.value);
        });
    }

    // Geolocation & Connection Status Watchers
    checkOnlineConnection();
    window.addEventListener("online", checkOnlineConnection);
    window.addEventListener("offline", checkOnlineConnection);

    // Watch PWA Install Events
    window.addEventListener("beforeinstallprompt", e => {
        e.preventDefault();
        deferredInstallPrompt = e;
        document.getElementById("pwa-install-banner").style.display = "flex";
    });

    const passField = document.getElementById("profile-password-field");
    if (passField) {
        passField.addEventListener("input", e => {
            const val = e.target.value;
            const bar = document.getElementById("password-strength-bar");
            const txt = document.getElementById("password-strength-text");
            if (!bar || !txt) return;
            if (val.length < 5) {
                bar.style.width = "30%";
                bar.style.background = "var(--primary)";
                txt.innerText = "Weak Password";
                txt.style.color = "var(--primary)";
            } else if (val.length < 8) {
                bar.style.width = "60%";
                bar.style.background = "var(--secondary)";
                txt.innerText = "Medium Strength";
                txt.style.color = "var(--secondary)";
            } else {
                bar.style.width = "100%";
                bar.style.background = "var(--neon-green)";
                txt.innerText = "Strong Password";
                txt.style.color = "var(--neon-green)";
            }
        });
    }

    const flagBoxes = document.querySelectorAll(".admin-flag-checkbox");
    flagBoxes.forEach(box => {
        box.addEventListener("change", () => {
            const flagName = box.getAttribute("data-flag");
            const isChecked = box.checked;
            addLog("admin-service", `Feature policy [${flagName}] updated dynamically: ${isChecked}`);
        });
    });

    // PWA banner direct click handlers
    const installApprove = document.getElementById("btn-pwa-install-approve");
    const installDismiss = document.getElementById("btn-pwa-install-dismiss");
    if (installApprove) {
        installApprove.addEventListener("click", () => {
            if (deferredInstallPrompt) {
                deferredInstallPrompt.prompt();
                deferredInstallPrompt.userChoice.then(choice => {
                    deferredInstallPrompt = null;
                    const banner = document.getElementById("pwa-install-banner");
                    if (banner) banner.style.display = "none";
                });
            } else {
                showToast("App installation is ready. Add to home screen from browser settings if prompt is disabled.", "info");
                const banner = document.getElementById("pwa-install-banner");
                if (banner) banner.style.display = "none";
            }
        });
    }
    if (installDismiss) {
        installDismiss.addEventListener("click", () => {
            const banner = document.getElementById("pwa-install-banner");
            if (banner) banner.style.display = "none";
        });
    }

    // Sync initial cart counter (Ticket #22 compliance - "cart value default 0 not 4")
    updateCartCounter();

    // Newsletter subscription listener
    const newsForm = document.getElementById("newsletter-form");
    if (newsForm) {
        newsForm.addEventListener("submit", e => {
            e.preventDefault();
            const email = document.getElementById("newsletter-email").value;
            showToast(`Thanks for subscribing! Outbox update stream notifications will be sent to: ${email}`, "success");
            document.getElementById("newsletter-email").value = "";
        });
    }

    // --- MAILBOX EVENT BINDINGS ---
    const mailTrigger = document.getElementById("btn-mailbox-trigger");
    if (mailTrigger) {
        mailTrigger.addEventListener("click", () => {
            switchView("view-mailbox", mailTrigger);
            renderMailboxList();
            const pane = document.getElementById("mailbox-content-pane");
            if (pane) pane.innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
        });
    }

    const folderBtns = document.querySelectorAll(".mailbox-folder-btn");
    folderBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            folderBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            activeMailFolder = btn.getAttribute("data-folder");
            activeMailId = null;
            renderMailboxList();
            const pane = document.getElementById("mailbox-content-pane");
            if (pane) pane.innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
        });
    });

    const mailComposeBtn = document.getElementById("btn-mailbox-compose");
    if (mailComposeBtn) {
        mailComposeBtn.addEventListener("click", showComposeBox);
    }

    const mailSearch = document.getElementById("mailbox-search");
    if (mailSearch) {
        mailSearch.addEventListener("input", renderMailboxList);
    }

    // --- WALLET EVENT BINDINGS ---
    const presetBtns = document.querySelectorAll(".preset-amount-btn");
    presetBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            presetBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            const valInput = document.getElementById("wallet-input-amount");
            if (valInput) valInput.value = btn.getAttribute("data-amount");
        });
    });

    const payModeRadios = document.querySelectorAll('input[name="wallet-pay-mode"]');
    payModeRadios.forEach(radio => {
        radio.addEventListener("change", () => {
            const formVal = radio.value;
            // Hide all sub forms
            const fUpi = document.getElementById("wallet-form-upi");
            const fCard = document.getElementById("wallet-form-card");
            const fQr = document.getElementById("wallet-form-qr");
            const fNet = document.getElementById("wallet-form-netbanking");
            
            if (fUpi) fUpi.style.display = "none";
            if (fCard) fCard.style.display = "none";
            if (fQr) fQr.style.display = "none";
            if (fNet) fNet.style.display = "none";
            
            // Show selected
            const target = document.getElementById(`wallet-form-${formVal}`);
            if (target) target.style.display = "block";
        });
    });

    const walletAddBtn = document.getElementById("btn-wallet-add-now");
    if (walletAddBtn) {
        walletAddBtn.addEventListener("click", addWalletLedgerFunds);
    }

    const txSearch = document.getElementById("wallet-tx-search");
    if (txSearch) {
        txSearch.addEventListener("input", renderWalletLedgerView);
    }

    const txFilter = document.getElementById("wallet-tx-filter");
    if (txFilter) {
        txFilter.addEventListener("change", renderWalletLedgerView);
    }

    const txExport = document.getElementById("btn-export-wallet-csv");
    if (txExport) {
        txExport.addEventListener("click", exportWalletTransactions);
    }

    // Add Money/Transactions navigation triggers (Sidebar navigation links)
    const walletTrigger = document.querySelector('[data-view="view-wallet"]');
    if (walletTrigger) {
        walletTrigger.addEventListener("click", () => {
            switchView("view-wallet", walletTrigger);
            renderWalletLedgerView();
        });
    }

    // --- ADMIN MANAGER EVENT BINDINGS ---
    const adminCouponAddBtn = document.getElementById("btn-admin-add-coupon");
    if (adminCouponAddBtn) {
        adminCouponAddBtn.addEventListener("click", addAdminCoupon);
    }

    // Render admin managers on load
    renderAdminCoupons();
    renderAdminRefunds();

    // Trigger browser geolocation center coordinates verification
    initUserLocation();

    // Persist sort options configuration
    const restSort = document.getElementById("restaurant-sort");
    if (restSort) {
        restSort.value = currentSortOption;
        restSort.addEventListener("change", e => {
            currentSortOption = e.target.value;
            localStorage.setItem("delivo-restaurant-sort", currentSortOption);
            refreshCatalogDisplay();
        });
    }

    // --- CONFIRM MODAL BUTTONS BINDINGS ---
    const confirmYes = document.getElementById("btn-confirm-yes");
    const confirmNo = document.getElementById("btn-confirm-no");
    if (confirmYes && confirmNo) {
        confirmYes.addEventListener("click", () => {
    const modal = document.getElementById("confirm-modal");
    if (modal) modal.style.display = "none";
    if (typeof confirmCallback === "function") {
        confirmCallback();
    }
    confirmCallback = null;
});
        confirmNo.addEventListener("click", () => {
            const modal = document.getElementById("confirm-modal");
            if (modal) modal.style.display = "none";
            confirmCallback = null;
        });
    }

    addLog("api-gateway", "Service Discovery registration completed. All nodes online.");
    
    // Hash routing bindings
    window.addEventListener("hashchange", handleHashChange);
    handleHashChange();
});

// ==========================================================================
// SESSION TIMEOUT MONITOR (TICKET #36 COMPLIANCE)
// ==========================================================================
function startSessionTimeoutMonitor() {
    if (sessionTimeoutTimer) clearTimeout(sessionTimeoutTimer);
    
    // Warn user 5 minutes after inactivity
    sessionTimeoutTimer = setTimeout(() => {
        showToast("Session Security Warning: You will be logged out in 60 seconds due to inactivity.", "warning");
        sessionTimeoutTimer = setTimeout(() => {
            showToast("Session Expired: Logging out.", "error");
            setTimeout(() => {
                location.reload();
            }, 2000);
        }, 60000);
    }, 300000);
}

// ==========================================================================
// ROUTER & VIEW SWITCHER (TICKET #19 MAP SAFE REINIT)
// ==========================================================================
// ==========================================================================
// ROUTER & VIEW SWITCHER (TICKET #19 MAP SAFE REINIT)
// ==========================================================================
function navigateToPath(path) {
    const cleanPath = path.startsWith("#") ? path.substring(1) : path;
    const targetHash = "#" + (cleanPath.startsWith("/") ? cleanPath : "/" + cleanPath);
    if (window.location.hash === targetHash || (window.location.hash === "" && targetHash === "#/")) {
        handleHashChange();
    } else {
        window.location.hash = targetHash;
    }
}

function scrollToElement(id) {
    const landing = document.getElementById("view-landing");
    if (landing && !landing.classList.contains("active")) {
        navigateToPath("/");
    }
    setTimeout(() => {
        const el = document.getElementById(id);
        if (el) {
            el.scrollIntoView({ behavior: "smooth" });
        }
    }, 150);
}

function triggerVoiceSearch() {
    showToast("Voice Search Activation: Listening for audio query...", "info");
}

function triggerAiSuggestions() {
    showToast("AI Heuristic Engine: Generating predictive menu targets...", "info");
    const searchBar = document.getElementById("landing-search-input");
    if (searchBar) {
        searchBar.value = "Truffle Mushroom Pizza";
        triggerSearch("Truffle Mushroom Pizza");
    }
}

function showAuthPage(role) {
    if (role === "ADMIN") {
        navigateToPath("/auth/admin/login");
    } else {
        navigateToPath("/auth/customer/login");
    }
}

function populateLandingCatalog() {
    filterCategory("All");
}

function filterCategory(category) {
    const term = category.toLowerCase() === "chinese" ? "asian" : category.toLowerCase();
    let filtered = mockStores;
    if (category !== "All" && category !== "") {
        filtered = mockStores.filter(s => 
            s.cuisine.toLowerCase().includes(term) || 
            s.desc.toLowerCase().includes(term)
        );
    }

    // Populate dashboard grid
    const dashGrid = document.getElementById("catalog-grid");
    if (dashGrid) {
        renderCatalog(filtered);
    }
    
    // Populate landing page grid
    const landingGrid = document.getElementById("landing-catalog-grid");
    if (landingGrid) {
        landingGrid.innerHTML = "";
        filtered.forEach(s => {
            landingGrid.innerHTML += `
                <div class="store-card" onclick="showAuthPage('CUSTOMER')">
                    <div class="store-img">
                        <img src="${s.image}" alt="${s.name}" loading="lazy" style="width:100%; height:100%; object-fit:cover;">
                        <div class="store-tag">${s.cuisine}</div>
                        <button class="favorite-btn" id="landing-fav-btn-${s.id}">
                            <i class="fa-solid fa-star"></i>
                        </button>
                    </div>
                    <div class="store-info">
                        <h4>${s.name}</h4>
                        <p>${s.desc}</p>
                        <div class="store-meta">
                            <span><i class="fa-solid fa-star" style="color: #FFB703;"></i> ${s.rating}</span>
                            <span><i class="fa-solid fa-clock"></i> ${s.time}</span>
                        </div>
                    </div>
                </div>
            `;
        });
    }
}

function handleHashChange() {
    let path = window.location.hash.slice(1);
    if (!path || path === "") path = "/";

    const token = localStorage.getItem("delivo-token");
    const role = localStorage.getItem("delivo-role");

    // Guard dashboards
    if (path.startsWith("/customer/") || path === "/customer/dashboard") {
        if (!token || role !== "CUSTOMER") {
            showToast("Access Denied: Customer authorization required.", "error");
            window.location.hash = "/auth/customer/login";
            return;
        }
    }

    if (path.startsWith("/admin/") || path === "/admin/dashboard") {
        if (!token || role !== "ADMIN") {
            showToast("Access Denied: Admin authorization required.", "error");
            window.location.hash = "/auth/admin/login";
            return;
        }
    }

    // Hide all main views and auth subviews
    document.querySelectorAll(".app-view").forEach(view => {
        view.classList.remove("active");
    });
    document.querySelectorAll(".auth-subview").forEach(sub => {
        sub.style.display = "none";
    });

    // Public / Landing
    if (path === "/") {
        const landingView = document.getElementById("view-landing");
        if (landingView) landingView.classList.add("active");
        
        const mainTabs = document.getElementById("main-nav-tabs");
        const publicTabs = document.getElementById("public-nav-tabs");
        const guestAuthBtn = document.getElementById("guest-auth-buttons");
        const logoutBtn = document.getElementById("btn-logout");
        
        if (token) {
            if (mainTabs) mainTabs.style.display = "flex";
            if (publicTabs) publicTabs.style.display = "none";
            if (guestAuthBtn) guestAuthBtn.style.display = "none";
            if (logoutBtn) logoutBtn.style.display = "block";
        } else {
            if (mainTabs) mainTabs.style.display = "none";
            if (publicTabs) publicTabs.style.display = "flex";
            if (guestAuthBtn) guestAuthBtn.style.display = "flex";
            if (logoutBtn) logoutBtn.style.display = "none";
        }

        setTimeout(populateLandingCatalog, 100);
        return;
    }

    // Auth Views
    if (path.startsWith("/auth/")) {
        const authView = document.getElementById("view-auth");
        if (authView) authView.classList.add("active");

        if (path === "/auth/select") {
            const el = document.getElementById("subview-role-select");
            if (el) el.style.display = "flex";
        } else if (path === "/auth/customer/login") {
            const el = document.getElementById("subview-customer-login");
            if (el) el.style.display = "flex";
        } else if (path === "/auth/customer/register") {
            const el = document.getElementById("subview-customer-register");
            if (el) el.style.display = "flex";
        } else if (path === "/auth/admin/login") {
            const el = document.getElementById("subview-admin-login");
            if (el) el.style.display = "flex";
        } else if (path === "/auth/forgot-password") {
            const el = document.getElementById("subview-forgot-password");
            if (el) el.style.display = "flex";
        } else if (path === "/auth/reset-password") {
            const el = document.getElementById("subview-reset-password");
            if (el) el.style.display = "flex";
        }
        return;
    }

    // Dashboard Views
    if (path === "/customer/dashboard") {
        const customerView = document.getElementById("view-customer");
        if (customerView) customerView.classList.add("active");
        
        const mainTabs = document.getElementById("main-nav-tabs");
        const publicTabs = document.getElementById("public-nav-tabs");
        const guestAuthBtn = document.getElementById("guest-auth-buttons");
        const logoutBtn = document.getElementById("btn-logout");
        
        if (mainTabs) mainTabs.style.display = "flex";
        if (publicTabs) publicTabs.style.display = "none";
        if (guestAuthBtn) guestAuthBtn.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "block";
        return;
    }

    if (path === "/admin/dashboard") {
        const adminView = document.getElementById("view-admin");
        if (adminView) adminView.classList.add("active");
        
        const mainTabs = document.getElementById("main-nav-tabs");
        const publicTabs = document.getElementById("public-nav-tabs");
        const guestAuthBtn = document.getElementById("guest-auth-buttons");
        const logoutBtn = document.getElementById("btn-logout");
        
        if (mainTabs) mainTabs.style.display = "flex";
        if (publicTabs) publicTabs.style.display = "none";
        if (guestAuthBtn) guestAuthBtn.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "block";

        renderAdminCoupons();
        renderAdminRefunds();
        renderFraudAnomalies();
        renderOrderHistory();
        updateAdminDashboardCounters();
        setTimeout(initAdminChart, 200);
        return;
    }

    // Custom Order success and tracking routes
    if (path.startsWith("/customer/order-success/")) {
        const orderId = path.substring("/customer/order-success/".length);
        const successView = document.getElementById("view-order-success");
        if (successView) successView.classList.add("active");
        showOrderSuccessPage(orderId);
        return;
    }

    if (path.startsWith("/customer/track/")) {
        const orderId = path.substring("/customer/track/".length);
        const trackingView = document.getElementById("view-tracking");
        if (trackingView) trackingView.classList.add("active");
        showLiveTrackingPage(orderId);
        return;
    }
}

function switchView(viewId, element) {
    if (viewId === "view-customer") {
        navigateToPath("/customer/dashboard");
    } else if (viewId === "view-admin") {
        navigateToPath("/admin/dashboard");
    } else if (viewId === "view-landing") {
        navigateToPath("/");
    } else if (viewId === "view-auth") {
        navigateToPath("/auth/select");
    } else {
        document.querySelectorAll(".app-view").forEach(view => {
            view.classList.remove("active");
        });
        const targetView = document.getElementById(viewId);
        if (targetView) targetView.classList.add("active");

        if (element) {
            document.querySelectorAll(".nav-tab").forEach(tab => {
                tab.classList.remove("active");
            });
            element.classList.add("active");
        }

        if (viewId === "view-tracking") {
            setTimeout(initLeafletMap, 200);
        } else if (viewId === "view-restaurant") {
            renderKdsBoard();
        } else if (viewId === "view-driver") {
            setTimeout(initDriverMiniMap, 200);
        }
    }
}

// ==========================================================================
// GLOBAL EVENT HANDLERS DELEGATOR
// ==========================================================================
function handleGlobalClicks(e) {
    startSessionTimeoutMonitor(); // Reset activity timer

    // 1. Navbar views navigation
    const navTab = e.target.closest("[data-view]");
    if (navTab) {
        const viewId = navTab.getAttribute("data-view");
        switchView(viewId, navTab);
        return;
    }

    // 2. Theme switcher
    if (e.target.closest("#theme-toggle-btn")) {
        toggleTheme();
        return;
    }

    // 3. Cart slider toggles
    if (e.target.closest("#cart-indicator-btn") || e.target.closest("#btn-checkout-now")) {
        if (e.target.closest("#btn-checkout-now")) {
            toggleCartSlide();
            goToCheckout();
        } else {
            openCartSlide();
        }
        return;
    }
    if (e.target.closest("#btn-close-cart") || e.target.closest("#cart-backdrop")) {
        toggleCartSlide();
        return;
    }

    // 4. Food Categories click filter
    const categoryCard = e.target.closest(".food-category-card");
    if (categoryCard) {
        document.querySelectorAll(".food-category-card").forEach(c => c.classList.remove("active"));
        categoryCard.classList.add("active");
        filterCategory(categoryCard.getAttribute("data-category"));
        return;
    }

    // 5. Navigation: back to catalog button
    if (e.target.closest("#btn-back-to-catalog")) {
        showCatalog();
        return;
    }
    if (e.target.closest("#btn-back-to-cart-view")) {
        switchView("view-customer");
        return;
    }

    // 6. Apply Promo coupon code click
    if (e.target.closest("#btn-apply-promo")) {
        const promoInput = document.getElementById("promo-code-field").value.trim();
        checkCoupon(promoInput);
        return;
    }

    // 7. Finalize checkout Saga
    if (e.target.closest("#btn-submit-checkout")) {
        dispatchOrderSaga();
        return;
    }

    // 8. Wallet Topup clicks
    if (e.target.closest("#btn-quick-topup")) {
        topUpCustomerWallet();
        return;
    }

    // 9. MAP FLOATING CONTROLS (TICKET #19 & #24)
    if (e.target.closest("#btn-map-locate")) {
        if (leafletMap && homeMarker) {
            leafletMap.setView(homeMarker.getLatLng(), 16);
        }
        return;
    }
    if (e.target.closest("#btn-map-compass")) {
        if (leafletMap) leafletMap.setBearing(45); // Set map rotation
        return;
    }
    if (e.target.closest("#btn-map-layers")) {
        cycleMapStyle();
        return;
    }
    if (e.target.closest("#btn-map-traffic")) {
        toggleTrafficVisuals();
        return;
    }
    if (e.target.closest("#btn-map-satellite")) {
        toggleSatelliteView();
        return;
    }
    if (e.target.closest("#btn-map-fullscreen")) {
        toggleMapFullscreen();
        return;
    }

    // 10. PDF/CSV Exporter (Ticket #37 compliance)
    if (e.target.closest("#btn-export-orders-csv")) {
        exportOrdersToCSV();
        return;
    }

    // 11. Tip selections (Ticket #22 tip updates)
    const tipBtn = e.target.closest(".tip-btn");
    if (tipBtn) {
        document.querySelectorAll(".tip-btn").forEach(b => b.classList.remove("active"));
        tipBtn.classList.add("active");
        selectedTip = parseFloat(tipBtn.getAttribute("data-tip"));
        document.getElementById("custom-tip-amount").value = "";
        updateCartCounter();
        return;
    }

    // 12. PWA installers (Ticket #32 compliance)
    if (e.target.closest("#btn-pwa-install-approve")) {
        if (deferredInstallPrompt) {
            deferredInstallPrompt.prompt();
            deferredInstallPrompt.userChoice.then(choice => {
                if (choice.outcome === "accepted") {
                    console.log("PWA install accepted.");
                }
                deferredInstallPrompt = null;
                document.getElementById("pwa-install-banner").style.display = "none";
            });
        } else {
            showToast("App is already installed or browser installation is not supported.", "info");
            document.getElementById("pwa-install-banner").style.display = "none";
        }
        return;
    }
    if (e.target.closest("#btn-pwa-install-dismiss")) {
        document.getElementById("pwa-install-banner").style.display = "none";
        return;
    }

    // 13. Notifications bell triggers (Ticket #28 compliance)
    if (e.target.closest("#btn-notification-bell")) {
        const dd = document.getElementById("notification-dropdown");
        dd.style.display = dd.style.display === "none" ? "block" : "none";
        const badge = document.getElementById("notification-badge");
        if (badge) badge.style.display = "none";
        return;
    }
    if (e.target.closest("#btn-clear-notifications")) {
        showConfirm("Clear Notifications", "Are you sure you want to delete all notification alerts?", () => {
            const list = document.getElementById("notification-list-container");
            if (list) {
                list.innerHTML = '<div style="font-size: 11px; text-align: center; color: var(--text-muted); padding: 24px;">No new notifications.</div>';
            }
            const badge = document.getElementById("notification-badge");
            if (badge) badge.style.display = "none";
        });
        return;
    }

    // 14. Profile Security triggers (Ticket #6, #7, #8 compliance)
    if (e.target.closest("#btn-toggle-password-visibility")) {
        const field = document.getElementById("profile-password-field");
        const icon = document.getElementById("password-eye-icon");
        if (field.type === "password") {
            field.type = "text";
            icon.className = "fa-solid fa-eye-slash";
        } else {
            field.type = "password";
            icon.className = "fa-solid fa-eye";
        }
        return;
    }
    if (e.target.closest("#btn-revoke-iphone-session")) {
        showConfirm("Revoke Device Session", "Are you sure you want to terminate the active iPhone session?", () => {
            const el = document.getElementById("session-device-iphone");
            if (el) el.remove();
            showToast("Concurrent session terminated successfully.", "success");
        });
        return;
    }
    if (e.target.closest("#mfa-enabled-checkbox")) {
        const isChecked = document.getElementById("mfa-enabled-checkbox").checked;
        document.getElementById("mfa-backup-codes-box").style.display = isChecked ? "block" : "none";
        return;
    }

    // 15. Driver Portal Actions (Screenshot 10 & 11 compliance)
    if (e.target.closest("#btn-driver-status-toggle")) {
        isDriverOnline = !isDriverOnline;
        const btn = document.getElementById("btn-driver-status-toggle");
        const label = document.getElementById("driver-status-label");
        if (isDriverOnline) {
            btn.innerText = "Go Offline";
            btn.style.background = "var(--primary)";
            label.innerText = "Duty Status: Online";
            label.style.color = "var(--neon-green)";
            addLog("driver-service", "Courier Rahul Sharma status set to: Online");
            setTimeout(initDriverMiniMap, 200);
        } else {
            btn.innerText = "Go Online";
            btn.style.background = "var(--neon-green)";
            label.innerText = "Duty Status: Offline";
            label.style.color = "var(--text-muted)";
            addLog("driver-service", "Courier Rahul Sharma status set to: Offline");
        }
        return;
    }

    if (e.target.closest("#btn-request-payout")) {
        const balanceDisplay = document.getElementById("driver-wallet-balance");
        const balance = balanceDisplay ? parseFloat(balanceDisplay.innerText.replace("$", "")) : 0;
        if (balance <= 0) {
            showToast("No earnings available for payout!", "warning");
            return;
        }
        showConfirm("Request Courier Payout", `Transfer $${balance.toFixed(2)} driver earnings to your registered bank account?`, () => {
            showToast(`Payout of $${balance.toFixed(2)} requested successfully! The amount will be transferred to your registered bank account.`, "success");
            if (balanceDisplay) balanceDisplay.innerText = "$0.00";
            const todayEarn = document.getElementById("driver-today-earnings");
            if (todayEarn) todayEarn.innerText = "$0.00";
            const todayTrips = document.getElementById("driver-today-deliveries");
            if (todayTrips) todayTrips.innerText = "0 trips";
            addLog("driver-service", `Courier earnings payout requested: $${balance.toFixed(2)}`);
        });
        return;
    }

    if (e.target.closest("#btn-driver-accept")) {
        const acceptBtn = document.getElementById("btn-driver-accept");
        const declineBtn = document.getElementById("btn-driver-decline");
        acceptBtn.innerText = "Accepting...";
        acceptBtn.disabled = true;
        declineBtn.disabled = true;
        setTimeout(() => {
            const card = document.getElementById("driver-offer-card-bella");
            if (card) card.innerHTML = '<div style="font-size: 11px; text-align: center; color: var(--neon-green); padding: 15px;">Offer Accepted! En route to pickup.</div>';
            addLog("driver-service", "Courier Rahul Sharma accepted offer #bella-napoli");
        }, 1500);
        return;
    }
    if (e.target.closest("#btn-driver-decline")) {
        const acceptBtn = document.getElementById("btn-driver-accept");
        const declineBtn = document.getElementById("btn-driver-decline");
        declineBtn.innerText = "Declining...";
        acceptBtn.disabled = true;
        declineBtn.disabled = true;
        setTimeout(() => {
            const card = document.getElementById("driver-offer-card-bella");
            if (card) card.remove();
            addLog("driver-service", "Offer #bella-napoli declined by courier");
        }, 1000);
        return;
    }
}

function handleGlobalInputs(e) {
    // Custom tip input
    if (e.target.closest("#custom-tip-amount")) {
        document.querySelectorAll(".tip-btn").forEach(b => b.classList.remove("active"));
        const tipVal = parseFloat(e.target.value);
        selectedTip = isNaN(tipVal) || tipVal < 0 ? 0.00 : tipVal;
        updateCartCounter();
    }
}

function renderKdsBoard() {
    const pendingList = document.getElementById("kanban-pending-list");
    const prepList = document.getElementById("kanban-prep-list");
    const readyList = document.getElementById("kanban-ready-list");
    const doneList = document.getElementById("kanban-done-list");
    if (!pendingList || !prepList || !doneList) return;

    pendingList.innerHTML = "";
    prepList.innerHTML = "";
    if (readyList) readyList.innerHTML = "";
    doneList.innerHTML = "";

    // Seed mock KDS orders if orderHistory is empty
    const mockKdsOrders = [
        { id: "ORD-38271", status: "PENDING", items: "2× Margherita Pizza, 1× Garlic Bread", customerName: "Priya Sharma", amount: 18.50 },
        { id: "ORD-38272", status: "PENDING", items: "1× Butter Chicken, 2× Naan", customerName: "Arjun Patel", amount: 22.00 },
        { id: "ORD-38273", status: "PENDING", items: "3× Veg Spring Roll, 1× Manchow Soup", customerName: "Sneha Roy", amount: 14.75 },
        { id: "ORD-38274", status: "PREPARING", items: "1× Chicken Biryani, 1× Raita", customerName: "Rahul Verma", amount: 16.99 },
        { id: "ORD-38275", status: "PREPARING", items: "2× Paneer Tikka, 1× Lassi", customerName: "Anita Desai", amount: 19.50 },
        { id: "ORD-38276", status: "PREPARING", items: "1× Fish Curry, 2× Rice", customerName: "Vikram Singh", amount: 21.00 },
        { id: "ORD-38277", status: "PREPARING", items: "1× Dosa Platter, 1× Filter Coffee", customerName: "Meera Nair", amount: 13.25 },
        { id: "ORD-38278", status: "READY", items: "2× Tandoori Chicken, 1× Roti", customerName: "Karan Mehta", amount: 24.50 },
        { id: "ORD-38279", status: "READY", items: "1× Chole Bhature, 1× Mango Shake", customerName: "Divya Gupta", amount: 11.99 },
        { id: "ORD-38280", status: "READY", items: "3× Momos, 1× Thukpa", customerName: "Tenzing Dorji", amount: 15.00 },
        { id: "ORD-38281", status: "READY", items: "1× Mutton Rogan Josh, 2× Naan", customerName: "Sameer Khan", amount: 26.75 },
        { id: "ORD-38282", status: "READY", items: "2× Idli Sambar, 1× Vada", customerName: "Lakshmi Iyer", amount: 9.50 },
        { id: "ORD-38283", status: "DELIVERED", items: "1× Hyderabadi Biryani, 1× Mirchi Ka Salan", customerName: "Aditi Reddy", amount: 20.00 },
        { id: "ORD-38284", status: "DELIVERED", items: "2× Hakka Noodles, 1× Chilli Chicken", customerName: "Rohan Das", amount: 17.50 },
        { id: "ORD-38285", status: "DELIVERED", items: "1× Masala Dosa, 1× Coffee", customerName: "Pooja Joshi", amount: 8.75 },
        { id: "ORD-38286", status: "DELIVERED", items: "2× Pav Bhaji, 1× Buttermilk", customerName: "Nikhil Kulkarni", amount: 12.00 },
    ];

    const allOrders = orderHistory.length > 0 ? orderHistory : mockKdsOrders;

    let countPending = 0;
    let countPrep = 0;
    let countReady = 0;
    let countDone = 0;

    const avatars = [
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=32&h=32&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=32&h=32&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=32&h=32&q=80",
        "https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=32&h=32&q=80",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=32&h=32&q=80",
    ];

    allOrders.forEach((ord, idx) => {
        const statusColor = ord.status === 'DELIVERED' ? 'var(--neon-green)' : (ord.status === 'READY' ? '#00B4D8' : (ord.status === 'PENDING' ? 'var(--primary)' : 'var(--neon-yellow)'));
        const ordId = ord.id.startsWith("ORD-") ? ord.id : `#${ord.id.substring(7)}`;
        const avatar = ord.customerImage || avatars[idx % avatars.length];
        const cardHtml = `
            <div class="kds-card" style="margin-bottom:12px; border: 1px solid var(--border); padding: 12px; border-radius: var(--radius-sm); background: var(--bg);">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                    <div class="kds-card-title" style="margin:0; font-family:var(--font-display); font-weight:700;">${ordId}</div>
                    <span class="badge" style="background:${statusColor}; color:white; font-size:9px; padding:2px 6px;">${ord.status}</span>
                </div>
                <div class="kds-card-items" style="margin-bottom:10px; font-size:11px; line-height:1.4; color:var(--text-main); font-weight:500;">${ord.items}</div>
                <div style="display:flex; gap:8px; align-items:center; border-top:1px solid var(--border); padding-top:8px; margin-top:8px;">
                    <img src="${avatar}" alt="${ord.customerName}" style="width:24px; height:24px; border-radius:50%; object-fit:cover; border:1px solid var(--border);">
                    <div style="flex-grow:1; min-width:0;">
                        <div style="font-weight:700; font-size:10px; color:var(--text-main); text-overflow:ellipsis; overflow:hidden; white-space:nowrap;">${ord.customerName || 'Guest'}</div>
                        <div style="font-size:9px; color:var(--text-muted);">Amount: $${ord.amount.toFixed(2)}</div>
                    </div>
                </div>
            </div>
        `;
        if (ord.status === "PENDING") {
            pendingList.innerHTML += cardHtml;
            countPending++;
        } else if (ord.status === "PREPARING" || ord.status === "ASSIGNED" || ord.status === "PICKED_UP" || ord.status === "EN_ROUTE") {
            prepList.innerHTML += cardHtml;
            countPrep++;
        } else if (ord.status === "READY") {
            if (readyList) readyList.innerHTML += cardHtml;
            countReady++;
        } else if (ord.status === "DELIVERED") {
            doneList.innerHTML += cardHtml;
            countDone++;
        }
    });

    if (countPending === 0) {
        pendingList.innerHTML = '<div style="font-size: 11px; text-align: center; color: var(--text-muted); padding: 24px;">No pending orders in queue.</div>';
    }
    if (countPrep === 0) {
        prepList.innerHTML = '<div style="font-size: 11px; text-align: center; color: var(--text-muted); padding: 24px;">No active preparations.</div>';
    }
    if (readyList && countReady === 0) {
        readyList.innerHTML = '<div style="font-size: 11px; text-align: center; color: var(--text-muted); padding: 24px;">No packed orders waiting.</div>';
    }
    if (countDone === 0) {
        doneList.innerHTML = '<div style="font-size: 11px; text-align: center; color: var(--text-muted); padding: 24px;">No completed orders today.</div>';
    }

    document.getElementById("count-pending").innerText = countPending;
    document.getElementById("count-prep").innerText = countPrep;
    const countReadyEl = document.getElementById("count-ready");
    if (countReadyEl) countReadyEl.innerText = countReady;
    document.getElementById("count-done").innerText = countDone;
}

let mockAnomalies = [
    { id: "usr_9088", type: "Multiple IP Spoofing", score: 92, action: "Review Required" },
    { id: "usr_1022", type: "Rapid Session Hijacking", score: 88, action: "Review Required" },
    { id: "usr_8766", type: "Geofence Radius Breach", score: 74, action: "Review Required" }
];

function renderFraudAnomalies() {
    const container = document.getElementById("fraud-anomalies-body");
    if (!container) return;

    container.innerHTML = "";
    if (mockAnomalies.length === 0) {
        container.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">No pending anomaly reports.</td></tr>';
        return;
    }

    mockAnomalies.forEach((a, idx) => {
        container.innerHTML += `
            <tr id="anomaly-row-${idx}">
                <td>${a.id}</td>
                <td>${a.type}</td>
                <td><strong class="color-primary">${a.score}%</strong></td>
                <td>
                    <div style="display:flex; gap:8px;">
                        <button class="header-btn anomaly-action-btn" onclick="resolveAnomaly(${idx}, 'Blocked')" style="background:var(--primary); color:white; padding:2px 8px; font-size:10px;">Block</button>
                        <button class="header-btn anomaly-action-btn" onclick="resolveAnomaly(${idx}, 'Dismissed')" style="background:var(--neon-green); color:white; padding:2px 8px; font-size:10px;">Dismiss</button>
                    </div>
                </td>
            </tr>
        `;
    });
}

function resolveAnomaly(index, decision) {
    const anomaly = mockAnomalies[index];
    if (!anomaly) return;

    showToast(`Anomaly resolved. User [${anomaly.id}] has been ${decision}.`, "success");
    addLog("fraud-detection", `Anomaly resolved for ${anomaly.id}. Action: ${decision}`);
    
    mockAnomalies.splice(index, 1);
    renderFraudAnomalies();
}

// ==========================================================================
// COMPACT DYNAMIC FAQ RENDERER (TICKET #28 COMPLIANCE)
// ==========================================================================
function renderDynamicFaqs() {
    const list = document.getElementById("faq-accordion-list");
    if (!list) return;
    list.innerHTML = "";
    
    faqData.forEach((faq, index) => {
        list.innerHTML += `
            <div class="faq-item">
                <button class="faq-trigger" id="faq-trigger-${index}" aria-expanded="false" aria-controls="faq-answer-${index}">
                    <span>${faq.q}</span>
                    <i class="fa-solid fa-chevron-down"></i>
                </button>
                <div class="faq-answer" id="faq-answer-${index}" role="region" aria-labelledby="faq-trigger-${index}">
                    ${faq.a}
                </div>
            </div>
        `;
    });

    document.querySelectorAll(".faq-trigger").forEach(trigger => {
        trigger.addEventListener("click", () => {
            const answerId = trigger.getAttribute("aria-controls");
            const answerEl = document.getElementById(answerId);
            const isExpanded = trigger.getAttribute("aria-expanded") === "true";
            
            trigger.setAttribute("aria-expanded", !isExpanded);
            answerEl.style.display = isExpanded ? "none" : "block";
            trigger.querySelector("i").className = isExpanded ? "fa-solid fa-chevron-down" : "fa-solid fa-chevron-up";
        });
    });
}

// ==========================================================================
// 60FPS SMOOTH MAP TRACKING INTERPOLATION (TICKET #24 COMPLIANCE)
// ==========================================================================
// User location markers & map variables
let userWatchId = null;

function initUserLocation() {
    try {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                position => {
                    const lat = position.coords.latitude;
                    const lon = position.coords.longitude;
                    userCoordinates = { lat, lon };
                    updateMapWithLocation(lat, lon);
                    watchUserLocation();
                },
                error => {
                    console.log("Geolocation error:", error);
                    showToast("GPS Geolocation access denied or unavailable. Centering on Maharashtra, India.", "warning");
                    updateMapWithLocation(19.7515, 75.7139, false); // Default center
                },
                { timeout: 10000, enableHighAccuracy: true }
            );
        } else {
            showToast("Geolocation is not supported by your browser.", "error");
            updateMapWithLocation(19.7515, 75.7139, false);
        }
    } catch (e) {
        console.error("Geolocation init error:", e);
        updateMapWithLocation(19.7515, 75.7139, false);
    }
}

function watchUserLocation() {
    try {
        if (navigator.geolocation) {
            if (userWatchId) navigator.geolocation.clearWatch(userWatchId);
            userWatchId = navigator.geolocation.watchPosition(
                position => {
                    const lat = position.coords.latitude;
                    const lon = position.coords.longitude;
                    userCoordinates = { lat, lon };
                    
                    // Update marker position on map without panning constantly
                    if (homeMarker && leafletMap) {
                        homeMarker.setLatLng([lat, lon]);
                    }
                    
                    // Re-calculate distances
                    mockStores.forEach(s => {
                        let dist = calculateDistance(lat, lon, s.lat, s.lon);
                        if (dist > 15.0) {
                            dist = (dist % 13.6) + 1.2;
                        }
                        s.distanceVal = dist;
                        s.distance = dist.toFixed(1) + " km";
                    });
                    
                    // Refresh list if customer view is open
                    refreshCatalogDisplay();
                },
                err => console.log("Watch position error:", err),
                { timeout: 15000, enableHighAccuracy: true }
            );
        }
    } catch (e) {
        console.error("Geolocation watch error:", e);
    }
}

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

function reverseGeocode(lat, lon) {
    fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`)
        .then(res => res.json())
        .then(data => {
            if (data && data.display_name) {
                const addr = data.display_name;
                const input = document.getElementById("checkout-address-input");
                if (input) input.value = addr;
                showToast(`Location verified: ${addr.substring(0, 45)}...`, "success");
                addLog("search-service", `Reverse geocoded location: [${lat}, ${lon}] -> ${addr}`);
            }
        })
        .catch(err => {
            console.log("Geocoding fetch error:", err);
            const input = document.getElementById("checkout-address-input");
            if (input) input.value = `Highway 48, Maharashtra, India (${lat.toFixed(4)}, ${lon.toFixed(4)})`;
        });
}

function updateMapWithLocation(lat, lon, fetchAddress = true) {
    if (leafletMap) {
        leafletMap.setView([lat, lon], 13);
    }
    if (homeMarker) {
        homeMarker.setLatLng([lat, lon]);
    }
    if (fetchAddress) {
        reverseGeocode(lat, lon);
    }
    
    // Calculate distance for all stores
    mockStores.forEach(s => {
        let dist = calculateDistance(lat, lon, s.lat, s.lon);
        if (dist > 15.0) {
            dist = (dist % 13.6) + 1.2;
        }
        s.distanceVal = dist;
        s.distance = dist.toFixed(1) + " km";
    });
    refreshCatalogDisplay();
}

let driverMiniMap = null;
function initDriverMiniMap() {
    if (typeof L === "undefined") {
        console.warn("Leaflet library is not loaded.");
        return;
    }
    const container = document.getElementById("driver-mini-map");
    if (!container) return;
    if (driverMiniMap) {
        driverMiniMap.invalidateSize();
        return;
    }
    // Centered in Maharashtra, India
    driverMiniMap = L.map("driver-mini-map").setView([userCoordinates.lat, userCoordinates.lon], 13);
    L.tileLayer("https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}", {
        maxZoom: 20,
        subdomains: ["mt0", "mt1", "mt2", "mt3"]
    }).addTo(driverMiniMap);
    
    // Add delivery radius circle matching geofence in MH, India
    L.circle([userCoordinates.lat, userCoordinates.lon], {
        color: 'var(--primary)',
        fillColor: 'var(--primary)',
        fillOpacity: 0.1,
        radius: 3500 // Increased range for MH
    }).addTo(driverMiniMap);

    // Add a marker for active geohash
    const courierIcon = L.divIcon({
        html: '<i class="fa-solid fa-motorcycle" style="color: var(--neon-green); font-size: 20px;"></i>',
        className: 'leaflet-custom-marker',
        iconSize: [20, 20]
    });
    L.marker([userCoordinates.lat + 0.01, userCoordinates.lon + 0.01], { icon: courierIcon }).addTo(driverMiniMap).bindPopup("Current Dispatch Position");
}

function initLeafletMap() {
    if (typeof L === "undefined") {
        console.warn("Leaflet library is not loaded.");
        return;
    }
    const mapContainer = document.getElementById("map");
    if (!mapContainer) return;

    if (leafletMap) {
        leafletMap.invalidateSize();
        return;
    }

    leafletMap = L.map("map").setView([userCoordinates.lat, userCoordinates.lon], 13);

    L.tileLayer("https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}", {
        maxZoom: 20,
        subdomains: ["mt0", "mt1", "mt2", "mt3"]
    }).addTo(leafletMap);

    const homeIcon = L.divIcon({
        html: '<div style="background: white; border: 2.5px solid #FF5A54; border-radius: 50%; width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 10px rgba(0,0,0,0.15);"><i class="fa-solid fa-house" style="color: #FF5A54; font-size: 14px;"></i></div>',
        className: 'leaflet-custom-marker',
        iconSize: [34, 34],
        iconAnchor: [17, 17]
    });

    const storeIcon = L.divIcon({
        html: '<div style="background: white; border: 2.5px solid #00B4D8; border-radius: 50%; width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 10px rgba(0,0,0,0.15);"><i class="fa-solid fa-store" style="color: #00B4D8; font-size: 14px;"></i></div>',
        className: 'leaflet-custom-marker',
        iconSize: [34, 34],
        iconAnchor: [17, 17]
    });

    const bikeIcon = L.divIcon({
        html: '<div class="pulse-marker" style="background: #2EC4B6; border: 2px solid white; border-radius: 50%; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; box-shadow: 0 0 15px #2EC4B6; animation: pulse 1.5s infinite;"><i class="fa-solid fa-motorcycle" style="color: white; font-size: 16px;"></i></div>',
        className: 'leaflet-custom-marker',
        iconSize: [36, 36],
        iconAnchor: [18, 18]
    });

    homeMarker = L.marker([userCoordinates.lat, userCoordinates.lon], { icon: homeIcon }).addTo(leafletMap).bindPopup("Your Home Location");
    
    // Choose one store to map as delivery partner
    const activeStore = mockStores[0];
    restaurantMarker = L.marker([activeStore.lat, activeStore.lon], { icon: storeIcon }).addTo(leafletMap).bindPopup(activeStore.name);

    routeLine = L.polyline([
        [activeStore.lat, activeStore.lon],
        [activeStore.lat - 0.005, activeStore.lon - 0.005],
        [userCoordinates.lat + 0.005, userCoordinates.lon + 0.005],
        [userCoordinates.lat, userCoordinates.lon]
    ], { color: "#FF5A54", weight: 5, dashArray: "6, 9" }).addTo(leafletMap);

    driverMarker = L.marker([activeStore.lat, activeStore.lon], { icon: bikeIcon }).addTo(leafletMap).bindPopup("Courier Rahul Sharma");
    leafletMap.fitBounds(routeLine.getBounds(), { padding: [40, 40] });
}

// 60FPS marker interpolation along polyline route
function triggerDriverMovement(txId) {
    if (!leafletMap || !driverMarker) return;

    let segmentIndex = 0;
    const pathCoordinates = [
        [40.7180, -74.0010],
        [40.7170, -74.0020],
        [40.7160, -74.0030],
        [40.7150, -74.0040],
        [40.7140, -74.0050],
        [40.7128, -74.0060]
    ];

    function interpolateBetweenPoints(start, end, duration, callback) {
        const startTime = performance.now();
        
        function tick(now) {
            const progress = Math.min((now - startTime) / duration, 1.0);
            const lat = start[0] + (end[0] - start[0]) * progress;
            const lng = start[1] + (end[1] - start[1]) * progress;
            callback([lat, lng]);

            if (progress < 1.0) {
                requestAnimationFrame(tick);
            } else {
                advanceRoute();
            }
        }
        requestAnimationFrame(tick);
    }

    function advanceRoute() {
        if (segmentIndex >= pathCoordinates.length - 1) {
            // Reached Destination
            document.getElementById("live-eta-val").innerText = "Delivered!";
            document.getElementById("step-en-route").className = "stepper-node completed";
            document.getElementById("step-delivered").className = "stepper-node completed";
            addLog("delivery-service", "Rider successfully arrived at destination coordinates.");
            
            // Update order status and refresh KDS
            const ord = orderHistory.find(o => o.id === txId);
            if (ord) ord.status = "DELIVERED";
            renderOrderHistory();
            renderKdsBoard();
            return;
        }

        const startPt = pathCoordinates[segmentIndex];
        const endPt = pathCoordinates[segmentIndex + 1];
        segmentIndex++;

        // Interpolate over 2.5 seconds per segment (60fps)
        interpolateBetweenPoints(startPt, endPt, 2500, (coords) => {
            driverMarker.setLatLng(coords);
            leafletMap.panTo(coords);
            document.getElementById("live-distance-val").innerText = `${(2.4 - (segmentIndex * 0.4)).toFixed(1)} km`;
        });
    }

    advanceRoute();
}

// Map layer and display controls togglers
function cycleMapStyle() {
    activeMapStyle = activeMapStyle === "light" ? "dark" : "light";
    showToast(`Map layer switched to ${activeMapStyle.toUpperCase()} style.`, "info");
}
function toggleTrafficVisuals() {
    routeLine.setStyle({ color: routeLine.options.color === "#FF5A54" ? "#39FF14" : "#FF5A54" });
    showToast("Traffic layer visual toggled.", "info");
}
function toggleSatelliteView() {
    showToast("Satellite view overlay toggled.", "info");
}
function toggleMapFullscreen() {
    const mapEl = document.getElementById("map");
    if (!document.fullscreenElement) {
        mapEl.requestFullscreen().catch(err => console.error(err));
    } else {
        document.exitFullscreen();
    }
}

// ==========================================================================
// REAL-TIME CHARTS (TICKET #18 COMPLIANCE)
// ==========================================================================
let adminChartInterval = null;
function initAdminChart() {
    if (typeof Chart === "undefined") {
        console.warn("Chart.js is not loaded.");
        return;
    }
    const canvas = document.getElementById("admin-kpi-chart");
    if (!canvas) return;

    if (adminChart) {
        adminChart.destroy();
    }
    if (adminChartInterval) {
        clearInterval(adminChartInterval);
    }

    const ctx = canvas.getContext("2d");
    const isDark = document.body.getAttribute("data-theme") === "dark";
    
    // Gradient fill setup - Coral to Teal/Transparent
    const gradient = ctx.createLinearGradient(0, 0, 0, 250);
    gradient.addColorStop(0, 'rgba(255, 90, 95, 0.35)');
    gradient.addColorStop(0.5, 'rgba(0, 180, 216, 0.08)');
    gradient.addColorStop(1, 'rgba(0, 180, 216, 0.0)');

    let labels = [];
    let dataPoints = [];
    
    const now = new Date();
    for (let i = 9; i >= 0; i--) {
        const time = new Date(now.getTime() - i * 5000);
        labels.push(time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
        dataPoints.push(150 + Math.floor(Math.random() * 150));
    }

    adminChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Saga Latency (ms)',
                    data: dataPoints,
                    borderColor: '#FF5A5F',
                    borderWidth: 3.5,
                    backgroundColor: gradient,
                    tension: 0.4,
                    fill: true,
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderColor: '#FF5A5F',
                    pointBorderWidth: 3,
                    pointRadius: 5,
                    pointHoverBackgroundColor: '#FF5A5F',
                    pointHoverBorderColor: '#FFFFFF',
                    pointHoverBorderWidth: 3,
                    pointHoverRadius: 7
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    align: 'end',
                    labels: {
                        color: isDark ? '#F1F5F9' : '#0F172A',
                        usePointStyle: true,
                        pointStyle: 'circle',
                        padding: 15,
                        boxWidth: 8,
                        boxHeight: 8,
                        font: {
                            family: 'Space Grotesk',
                            size: 12,
                            weight: '600'
                        }
                    }
                },
                tooltip: {
                    backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
                    titleColor: isDark ? '#F1F5F9' : '#0F172A',
                    bodyColor: isDark ? '#94A3B8' : '#64748B',
                    borderColor: isDark ? '#334155' : '#E2E8F0',
                    borderWidth: 1,
                    cornerRadius: 10,
                    padding: 12,
                    boxPadding: 6,
                    usePointStyle: true,
                    font: {
                        family: 'Plus Jakarta Sans'
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: 'var(--text-muted)',
                        font: {
                            family: 'Plus Jakarta Sans',
                            size: 10
                        }
                    }
                },
                y: {
                    grid: {
                        color: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.04)',
                        drawBorder: false
                    },
                    ticks: {
                        color: 'var(--text-muted)',
                        font: {
                            family: 'Plus Jakarta Sans',
                            size: 10
                        }
                    },
                    min: 0,
                    max: 400
                }
            },
            animation: {
                duration: 400
            }
        }
    });

    // Real-time animation loop (updates every 3 seconds)
    adminChartInterval = setInterval(() => {
        const nextTime = new Date();
        const timeStr = nextTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        
        labels.shift();
        labels.push(timeStr);
        
        dataPoints.shift();
        const baseVal = 180 + Math.sin(Date.now() / 10000) * 80;
        const randomSwing = Math.floor(Math.random() * 40) - 20;
        dataPoints.push(Math.round(baseVal + randomSwing));
        
        adminChart.update();
        
        // Update System Throughput text card dynamically as well
        const tp = document.getElementById("metric-throughput");
        if (tp) {
            tp.innerText = `${(100 + Math.random() * 15).toFixed(1)}K events/s`;
        }
    }, 3000);
}

// ==========================================================================
// DEBOUNCED SEARCH (TICKET #20 COMPLIANCE)
// ==========================================================================
function debounce(func, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), delay);
    };
}

function triggerSearch(query) {
    if (!query) {
        renderCatalog(mockStores);
        return;
    }
    const filtered = mockStores.filter(s => 
        s.name.toLowerCase().includes(query.toLowerCase()) || 
        s.cuisine.toLowerCase().includes(query.toLowerCase())
    );
    renderCatalog(filtered);
}

// ==========================================================================
// CATALOG & CART MANAGEMENT (TICKET #22 COMPLIANCE)
// ==========================================================================
function renderCatalog(stores) {
    const container = document.getElementById("catalog-grid");
    if (!container) return;
    container.innerHTML = "";
    
    stores.forEach(s => {
        container.innerHTML += `
            <div class="store-card" onclick="openMenu('${s.id}')">
                <div class="store-img">
                    <img src="${s.image}" alt="${s.name}" loading="lazy" style="width:100%; height:100%; object-fit:cover;">
                    <div class="store-tag">${s.cuisine}</div>
                    <button class="favorite-btn" id="fav-btn-${s.id}">
                        <i class="fa-solid fa-star"></i>
                    </button>
                </div>
                <div class="store-info">
                    <h4>${s.name}</h4>
                    <p>${s.desc}</p>
                    <div class="store-meta">
                        <span><i class="fa-solid fa-star" style="color: #FFB703;"></i> ${s.rating}</span>
                        <span><i class="fa-solid fa-clock"></i> ${s.time}</span>
                    </div>
                </div>
            </div>
        `;
    });
}

function openMenu(storeId, searchFilter = "", sortBy = "default") {
    const store = mockStores.find(s => s.id === storeId);
    if (!store) return;

    currentOpenRestaurant = storeId;
    document.getElementById("catalog-grid").style.display = "none";
    document.getElementById("catalog-end-state").style.display = "none";
    document.getElementById("restaurant-menu-details").style.display = "block";
    document.getElementById("selected-restaurant-name").innerText = store.name;

    const list = document.getElementById("menu-items-list");
    const sidebar = document.getElementById("menu-categories-sidebar");
    
    list.innerHTML = "";
    sidebar.innerHTML = "";

    // Gather store menu
    let items = [...mockMenus[storeId]];
    if (searchFilter) {
        const query = searchFilter.toLowerCase().trim();
        items = items.filter(item => item.name.toLowerCase().includes(query) || item.desc.toLowerCase().includes(query));
    }

    // Sort items
    if (sortBy === "price-low") {
        items.sort((a, b) => a.price - b.price);
    } else if (sortBy === "price-high") {
        items.sort((a, b) => b.price - a.price);
    }

    if (items.length === 0) {
        list.innerHTML = `<div style="text-align:center; padding:30px; color:var(--text-muted); font-size:13px;">No items match your search.</div>`;
        return;
    }

    // Group by category
    const categories = ["Starters", "Main Course", "Pizza", "Burgers", "Drinks", "Desserts"];
    const grouped = {};
    categories.forEach(c => grouped[c] = []);

    items.forEach(item => {
        const cat = item.category || "Main Course";
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(item);
    });

    let activeCats = [];
    categories.forEach((cat, idx) => {
        const catItems = grouped[cat];
        if (catItems && catItems.length > 0) {
            activeCats.push(cat);
            const catId = `cat-sec-${idx}`;
            
            // Append category button to sidebar
            const isActive = activeCats.length === 1;
            sidebar.innerHTML += `
                <button class="menu-category-link ${isActive ? 'active' : ''}" onclick="scrollToCategory('${catId}', this)">
                    ${cat}
                </button>
            `;

            // Append category section to list
            let sectionHtml = `
                <div id="${catId}" class="menu-category-section">
                    <h5 style="color: var(--secondary); font-size: 13px; font-weight: 700; border-bottom: 1px solid var(--border); padding-bottom: 6px; margin-bottom: 12px; font-family: var(--font-display);">${cat.toUpperCase()}</h5>
                    <div style="display: flex; flex-direction: column; gap: 12px;">
            `;

            catItems.forEach(item => {
                const cartItem = cartItems.find(c => c.id === item.id);
                const actionHtml = cartItem 
                    ? `<div style="display:flex; align-items:center; gap:8px;">
                           <button class="header-btn" onclick="updateMenuQty('${item.id}', -1, '${storeId}', event)" style="padding:2px 8px;">-</button>
                           <span style="font-weight:700; font-size:13px;">${cartItem.qty}</span>
                           <button class="header-btn" onclick="updateMenuQty('${item.id}', 1, '${storeId}', event)" style="padding:2px 8px;">+</button>
                       </div>`
                    : `<button class="header-btn" onclick="addToCart('${item.id}', '${storeId}', event)" style="padding: 2px 10px;">Add +</button>`;

                sectionHtml += `
                    <div class="menu-item-row" style="display:flex; justify-content:space-between; align-items:center; padding:12px; border-bottom:1px solid var(--border); gap:15px; background: rgba(255,255,255,0.01); border-radius: 6px;">
                        <div style="display:flex; gap:12px; align-items:center;">
                            <div style="position: relative; width: 50px; height: 50px; border-radius: var(--radius-sm); overflow: hidden; background: var(--border);">
                                <div class="img-skeleton"></div>
                                <img src="${item.image}" alt="${item.name}" loading="lazy" onload="this.previousElementSibling.style.display='none';" onerror="this.src='https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=80&h=80&q=80'; this.previousElementSibling.style.display='none';" style="width:100%; height:100%; object-fit:cover;">
                            </div>
                            <div>
                                <h5 style="font-size:14px; font-weight:700; color:var(--text-main); margin: 0;">${item.name}</h5>
                                <p style="font-size:11px; color:var(--text-muted); margin: 4px 0 0 0;">${item.desc} <span style="font-size:10px; color:var(--text-muted);">(${item.calories} kcal)</span></p>
                            </div>
                        </div>
                        <div style="display:flex; align-items:center; gap:15px;">
                            <span style="font-weight:700; color:var(--text-main); font-size:13px;">$${item.price.toFixed(2)}</span>
                            ${actionHtml}
                        </div>
                    </div>
                `;
            });

            sectionHtml += `
                    </div>
                </div>
            `;
            list.innerHTML += sectionHtml;
        }
    });
}

function scrollToCategory(secId, btn) {
    const sec = document.getElementById(secId);
    if (sec) {
        sec.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        document.querySelectorAll(".menu-category-link").forEach(l => l.classList.remove("active"));
        btn.classList.add("active");
    }
}

function showCatalog() {
    currentOpenRestaurant = null;
    document.getElementById("catalog-grid").style.display = "grid";
    document.getElementById("catalog-end-state").style.display = "block";
    document.getElementById("restaurant-menu-details").style.display = "none";
}

function addToCart(itemId, storeId, event) {
    const item = mockMenus[storeId].find(i => i.id === itemId);
    if (!item) return;

    if (cartItems.length > 0 && cartItems[0].storeId !== storeId) {
        showConfirm("Clear Previous Cart?", "You already have items from another restaurant in your cart. Clear them to start a new order?", () => {
            cartItems = [];
            updateCartCounter();
            openCustomizationModal(item, storeId);
        });
        return;
    }

    openCustomizationModal(item, storeId);
}

function updateMenuQty(itemId, val, storeId, event) {
    const cartItem = cartItems.find(c => c.id === itemId);
    if (!cartItem) return;
    
    cartItem.qty += val;
    if (cartItem.qty <= 0) {
        cartItems = cartItems.filter(c => c.id !== itemId);
    }
    updateCartCounter();
    openMenu(storeId);
    if (val > 0 && event) triggerFlyingDot(event);
}

function updateCartCounter() {
    const count = cartItems.reduce((acc, curr) => acc + curr.qty, 0);
    const badge = document.getElementById("cart-item-count");
    if (badge) {
        badge.innerText = count;
        badge.style.display = count === 0 ? "none" : "inline-block";
    }

    const subtotal = cartItems.reduce((acc, curr) => acc + (curr.price * curr.qty), 0);
    const taxes = subtotal * 0.0825; // 8.25% VAT
    const deliveryFee = subtotal > 0 ? 3.99 : 0.00;
    const finalTotal = (subtotal + taxes + deliveryFee + selectedTip) * (1.0 - appliedDiscount);

    const slideSub = document.getElementById("cart-slide-subtotal");
    if (slideSub) slideSub.innerText = `$${subtotal.toFixed(2)}`;

    const checkSub = document.getElementById("checkout-subtotal");
    if (checkSub) checkSub.innerText = `$${subtotal.toFixed(2)}`;

    const checkTax = document.getElementById("checkout-taxes");
    if (checkTax) checkTax.innerText = `$${taxes.toFixed(2)}`;

    const checkFee = document.getElementById("checkout-delivery-fee");
    if (checkFee) checkFee.innerText = `$${deliveryFee.toFixed(2)}`;

    const checkTip = document.getElementById("checkout-tip");
    if (checkTip) checkTip.innerText = `$${selectedTip.toFixed(2)}`;

    const checkGrand = document.getElementById("checkout-grand-total");
    if (checkGrand) checkGrand.innerText = `$${finalTotal.toFixed(2)}`;

    const splitInput = document.getElementById("split-bill-members");
    const members = splitInput ? (parseInt(splitInput.value) || 1) : 1;
    const splitAmount = document.getElementById("split-bill-amount");
    if (splitAmount) {
        splitAmount.innerText = `$${(finalTotal / Math.max(1, members)).toFixed(2)}`;
    }

    // Remaining money display (Screenshot 4 & 5 compliance)
    const remainingVal = walletBalance - finalTotal;
    const remainingEl = document.getElementById("checkout-remaining-balance");
    if (remainingEl) {
        remainingEl.innerText = `$${remainingVal.toFixed(2)}`;
        if (remainingVal < 0) {
            remainingEl.style.color = "var(--primary)";
        } else {
            remainingEl.style.color = "var(--secondary)";
        }
    }

    renderCartSlideout();
}

function renderCartSlideout() {
    const container = document.getElementById("cart-items-list-container");
    if (!container) return;

    if (cartItems.length === 0) {
        container.innerHTML = '<div style="font-size: 13px; color: var(--text-muted); text-align: center; margin-top: 50px;">Your checkout cart is empty.</div>';
        return;
    }

    container.innerHTML = "";
    cartItems.forEach(item => {
        container.innerHTML += `
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; background:rgba(255,255,255,0.02); padding:10px; border-radius:8px; border:1px solid var(--border);">
                <div>
                    <div style="font-weight:700; font-size:13px;">${item.name}</div>
                    <div style="font-size:11px; color:var(--text-muted);">$${item.price.toFixed(2)}</div>
                </div>
                <div style="display:flex; align-items:center; gap:8px;">
                    <button class="header-btn" onclick="adjustCartQty('${item.id}', -1)" style="padding:2px 8px;">-</button>
                    <span style="font-size:13px; font-weight:700;">${item.qty}</span>
                    <button class="header-btn" onclick="adjustCartQty('${item.id}', 1)" style="padding:2px 8px;">+</button>
                </div>
            </div>
        `;
    });
}

function adjustCartQty(itemId, val) {
    const item = cartItems.find(c => c.id === itemId);
    if (!item) return;
    item.qty += val;
    if (item.qty <= 0) {
        cartItems = cartItems.filter(c => c.id !== itemId);
    }
    updateCartCounter();
}

function goToCheckout() {
    updateCartCounter();
    const summary = document.getElementById("checkout-summary-items");
    if (!summary) return;
    summary.innerHTML = "";

    cartItems.forEach(item => {
        summary.innerHTML += `
            <div style="display:flex; justify-content:space-between; font-size:12px; margin-bottom:10px;">
                <span>${item.qty}x ${item.name}</span>
                <span>$${(item.price * item.qty).toFixed(2)}</span>
            </div>
        `;
    });
    switchView("view-checkout");
}

function checkCoupon(code) {
    const status = document.getElementById("promo-status");
    if (code === "DELIVO10") {
        appliedDiscount = 0.10;
        status.innerText = "Coupon DELIVO10 applied: 10% Discount active!";
    } else {
        appliedDiscount = 0;
        status.innerText = "";
    }
    updateCartCounter();
}

// Place Order Saga
function dispatchOrderSaga() {
    if (cartItems.length === 0) {
        showToast("Cart is empty!", "warning");
        return;
    }

    const totalVal = parseFloat(document.getElementById("checkout-grand-total").innerText.replace("$", ""));
    if (walletBalance < totalVal) {
        showToast("Insufficient wallet balance! Please add funds to your wallet.", "error");
        return;
    }

    showConfirm("Confirm Order Payment", `Are you sure you want to finalize this order for $${totalVal.toFixed(2)}?`, () => {
        // Show processing state
        const loader = document.getElementById("payment-processing-loader");
        if (loader) loader.style.display = "flex";

        const restaurantName = document.getElementById("selected-restaurant-name") ? document.getElementById("selected-restaurant-name").innerText : "Bella Napoli Pizza";
        const itemsList = cartItems.map(i => `${i.qty}x ${i.name}`).join(", ");
        
        // Post payment to backend API
        fetch("/api/orders/pay", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                restaurantName: restaurantName,
                amount: totalVal,
                address: currentSelectedAddress,
                paymentMethod: "Wallet (Delivo Ledger)",
                items: itemsList
            })
        })
        .then(res => {
            if (!res.ok) {
                throw new Error("Payment transaction declined by gateway.");
            }
            return res.json();
        })
        .then(data => {
            // Success flow
            walletBalance -= totalVal;
            syncLocalWalletBalances();
            
            // Push debit transaction ledger entry
            walletLedger.push({
                id: "tx_ledger_" + Math.random().toString(36).substr(2, 9),
                time: new Date().toISOString().replace('T', ' ').substring(0, 19),
                type: "DEBIT",
                amount: totalVal,
                bal: walletBalance
            });
            renderWalletLedger();

            // Clear cart
            cartItems = [];
            updateCartCounter();

            // Hide loader
            if (loader) loader.style.display = "none";

            showToast("Payment successful! Booking order.", "success");

            // Navigate to Order Success page
            navigateToPath(`/customer/order-success/${data.orderId}`);
        })
        .catch(err => {
            // Failure flow
            if (loader) loader.style.display = "none";
            showToast(err.message, "error");
        });
    });
}

let currentActiveTransactionId = null;
let currentActiveTotalVal = 0;
let currentActiveItemsList = "";

function executeSagaSteps(transactionId, totalVal, itemsList) {
    currentActiveTransactionId = transactionId;
    currentActiveTotalVal = totalVal;
    currentActiveItemsList = itemsList;

    // Reset timelines
    const fillBar = document.getElementById("saga-timeline-fill-bar");
    if (fillBar) fillBar.style.height = "0%";
    
    document.querySelectorAll(".stepper-node").forEach(node => {
        node.classList.remove("completed", "active");
        const icon = node.querySelector(".node-bullet i");
        if (icon) icon.className = "";
    });

    // Add Order to History
    orderHistory.push({
        id: transactionId,
        date: new Date().toISOString().split('T')[0],
        items: itemsList,
        amount: totalVal,
        status: "PENDING",
        customerName: "Alice Smith (You)",
        customerImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=48&h=48&q=80"
    });
    renderOrderHistory();
    renderKdsBoard();

    document.getElementById("promo-code-field").value = "";

    // Stage 1: Accepted (immediately)
    const nodeAccepted = document.getElementById("step-accepted");
    if (nodeAccepted) {
        nodeAccepted.classList.add("active");
    }
    if (fillBar) fillBar.style.height = "12%";
    addLog("payment-service", `Payment validated. Debited wallet: $${totalVal.toFixed(2)}`);
    showToast("Order placed successfully! Waiting for restaurant confirmation.", "success");

    // Stage 2: Preparing Food (after 4s)
    setTimeout(() => {
        const nodeAccepted = document.getElementById("step-accepted");
        const nodePrep = document.getElementById("step-preparing");
        if (nodeAccepted) {
            nodeAccepted.classList.remove("active");
            nodeAccepted.classList.add("completed");
            const icon = nodeAccepted.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        if (nodePrep) nodePrep.classList.add("active");
        if (fillBar) fillBar.style.height = "25%";

        const ord = orderHistory.find(o => o.id === transactionId);
        if (ord) ord.status = "PREPARING";
        renderOrderHistory();
        renderKdsBoard();
        showToast("Restaurant accepted order! Food is being prepared.", "success");
    }, 4000);

    // Stage 3: Driver Assigned (after 8s)
    setTimeout(() => {
        const nodePrep = document.getElementById("step-preparing");
        const nodeAssign = document.getElementById("step-assigned");
        if (nodePrep) {
            nodePrep.classList.remove("active");
            nodePrep.classList.add("completed");
            const icon = nodePrep.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        if (nodeAssign) nodeAssign.classList.add("active");
        if (fillBar) fillBar.style.height = "37%";

        // Show driver card
        const driverTrip = document.getElementById("driver-active-trip");
        if (driverTrip) {
            driverTrip.style.display = "block";
            document.getElementById("driver-trip-status").innerText = "En Route to Restaurant";
        }
        
        showToast("Rider matched! Courier Rahul Sharma is on the way.", "info");
    }, 8000);

    // Stage 4: Rider Reached Restaurant (after 12s)
    setTimeout(() => {
        const nodeAssign = document.getElementById("step-assigned");
        const nodeArrived = document.getElementById("step-arrived");
        if (nodeAssign) {
            nodeAssign.classList.remove("active");
            nodeAssign.classList.add("completed");
            const icon = nodeAssign.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        if (nodeArrived) nodeArrived.classList.add("active");
        if (fillBar) fillBar.style.height = "50%";

        const driverTrip = document.getElementById("driver-active-trip");
        if (driverTrip) {
            document.getElementById("driver-trip-status").innerText = "Arrived at Restaurant";
            const reachedBtn = document.getElementById("btn-driver-reached-restaurant");
            if (reachedBtn) reachedBtn.style.display = "none";
            const pickedBtn = document.getElementById("btn-driver-picked-up");
            if (pickedBtn) pickedBtn.style.display = "block";
        }

        showToast("Rider arrived at restaurant. Checking items...", "info");
    }, 12000);

    // Stage 5: Food Picked Up (after 16s)
    setTimeout(() => {
        const nodeArrived = document.getElementById("step-arrived");
        const nodePicked = document.getElementById("step-picked-up");
        if (nodeArrived) {
            nodeArrived.classList.remove("active");
            nodeArrived.classList.add("completed");
            const icon = nodeArrived.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        if (nodePicked) nodePicked.classList.add("active");
        if (fillBar) fillBar.style.height = "62%";

        const driverTrip = document.getElementById("driver-active-trip");
        if (driverTrip) {
            document.getElementById("driver-trip-status").innerText = "Food Picked Up";
            const pickedBtn = document.getElementById("btn-driver-picked-up");
            if (pickedBtn) pickedBtn.style.display = "none";
            const otpBox = document.getElementById("driver-otp-box");
            if (otpBox) otpBox.style.display = "block";
        }

        const ord = orderHistory.find(o => o.id === transactionId);
        if (ord) ord.status = "ASSIGNED";
        renderOrderHistory();
        renderKdsBoard();

        showToast("Rider picked up package! Preparing dispatch.", "info");
    }, 16000);

    // Stage 6: Live Route Active (after 20s)
    setTimeout(() => {
        const nodePicked = document.getElementById("step-picked-up");
        const nodeEnRoute = document.getElementById("step-en-route");
        if (nodePicked) {
            nodePicked.classList.remove("active");
            nodePicked.classList.add("completed");
            const icon = nodePicked.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        if (nodeEnRoute) nodeEnRoute.classList.add("active");
        if (fillBar) fillBar.style.height = "75%";

        triggerDriverMovement(transactionId);
        showToast("Rider is en route to your address.", "info");
    }, 20000);

    // Stage 7: Driver Near You (after 24s)
    setTimeout(() => {
        const nodeEnRoute = document.getElementById("step-en-route");
        const nodeNear = document.getElementById("step-near-you");
        if (nodeEnRoute) {
            nodeEnRoute.classList.remove("active");
            nodeEnRoute.classList.add("completed");
            const icon = nodeEnRoute.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
        if (nodeNear) nodeNear.classList.add("active");
        if (fillBar) fillBar.style.height = "87%";

        showToast("Rider is near your neighborhood! Get your delivery OTP ready.", "warning");
    }, 24000);
}

// ==========================================================================
// PROFILE, WALLET LEDGERS & EXPORT INVOICE (TICKET #37 COMPLIANCE)
// ==========================================================================
function syncLocalWalletBalances() {
    document.getElementById("customer-wallet-display").innerText = `$${walletBalance.toFixed(2)}`;
}

function topUpCustomerWallet() {
    const input = document.getElementById("quick-topup-input");
    const amount = parseFloat(input.value);

    if (isNaN(amount) || amount <= 0) {
        showToast("Please enter a valid amount.", "warning");
        return;
    }

    if (amount > 100.0) {
        showToast("Maximum wallet top-up increment limit is $100.", "warning");
        return;
    }

    if (walletBalance + amount > 2000.0) {
        showToast("Maximum account wallet balance limit reached: $2,000.", "error");
        return;
    }

    walletBalance += amount;
    syncLocalWalletBalances();

    // Write top-up transaction ledger entry
    walletLedger.push({
        id: "tx_ledger_" + (walletLedger.length + 1),
        time: new Date().toISOString().replace('T', ' ').substring(0, 19),
        type: "TOPUP",
        amount: amount,
        bal: walletBalance
    });
    renderWalletLedger();

    showToast(`Successfully topped up wallet balance by $${amount.toFixed(2)}.`, "success");
}

function syncProfileRealtime() {
    const fn = document.getElementById("profile-first-name").value;
    const ln = document.getElementById("profile-last-name").value;
    const logoEl = document.getElementById("app-logo-text");
    if (logoEl) {
        logoEl.innerText = `${fn} (${((fn[0] || "") + (ln[0] || "")).toUpperCase()}) - Delivo`;
    }
}

function savePersonalInfo() {
    showToast("Personal profile settings saved.", "success");
}

function saveSocialLinks() {
    const insta = document.getElementById("profile-insta-link").value;
    const fb = document.getElementById("profile-fb-link").value;
    const lk = document.getElementById("profile-linkedin-link").value;

    document.getElementById("social-insta-anchor").setAttribute("href", insta);
    document.getElementById("social-facebook-anchor").setAttribute("href", fb);
    document.getElementById("social-linkedin-anchor").setAttribute("href", lk);

    showToast("Social links saved.", "success");
}

function renderSavedCards() {
    const container = document.getElementById("saved-cards-list");
    if (!container) return;
    container.innerHTML = "";
    
    savedCards.forEach(card => {
        container.innerHTML += `
            <div style="display:flex; justify-content:space-between; align-items:center; background:rgba(0,0,0,0.02); padding:10px; border-radius:5px; border:1px solid var(--border); font-size:12px;">
                <span><i class="fa-solid fa-credit-card"></i> ${card.number} (Expiry: ${card.expiry})</span>
                <button class="header-btn" style="padding:2px 8px; margin:0;" onclick="removeCard('${card.id}')">Delete</button>
            </div>
        `;
    });
}

function removeCard(id) {
    savedCards = savedCards.filter(c => c.id !== id);
    renderSavedCards();
}

function renderWalletLedger() {
    const body = document.getElementById("wallet-ledger-body");
    if (!body) return;
    body.innerHTML = "";
    
    walletLedger.forEach(log => {
        body.innerHTML += `
            <tr>
                <td>${log.id}</td>
                <td>${log.time}</td>
                <td><span class="badge" style="background:var(--secondary); color:white;">${log.type}</span></td>
                <td>$${log.amount.toFixed(2)}</td>
                <td>$${log.bal.toFixed(2)}</td>
            </tr>
        `;
    });
}

function renderOrderHistory() {
    const body = document.getElementById("order-history-body");
    if (!body) return;
    body.innerHTML = "";
    
    orderHistory.forEach(ord => {
        body.innerHTML += `
            <tr>
                <td>${ord.id}</td>
                <td>${ord.date}</td>
                <td>${ord.items}</td>
                <td>$${ord.amount.toFixed(2)}</td>
                <td><span class="badge" style="background:${ord.status === 'DELIVERED' ? 'var(--neon-green)' : 'var(--neon-yellow)'}; color:white;">${ord.status}</span></td>
                <td><button class="header-btn" style="padding:2px 8px; margin:0;" onclick="reorderItems('${ord.id}')">Reorder</button></td>
            </tr>
        `;
    });
}

function reorderItems(orderId) {
    showToast(`Reorder of transaction ${orderId} placed into catalog queue.`, "success");
    addLog("order-service", `Reorder dispatch process running for transaction ID: ${orderId}`);
}

function exportOrdersToCSV() {
    let csv = "Order ID,Date,Items,Amount,Status\n";
    orderHistory.forEach(ord => {
        csv += `${ord.id},${ord.date},"${ord.items}",${ord.amount},${ord.status}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.setAttribute('href', url);
    a.setAttribute('download', 'delivo_order_history.csv');
    a.click();
}

// ==========================================================================
// CORE SYSTEM TELEMETRY LOGGER
// ==========================================================================
function toggleTheme() {
    const theme = document.body.getAttribute("data-theme") === "light" ? "dark" : "light";
    document.body.setAttribute("data-theme", theme);
    localStorage.setItem("delivo-theme", theme);
    document.getElementById("theme-icon").className = theme === "light" ? "fa-solid fa-moon" : "fa-solid fa-sun";
}

function checkOnlineConnection() {
    const offlineScreen = document.getElementById("error-screen-offline");
    if (!navigator.onLine) {
        offlineScreen.style.display = "flex";
    } else {
        offlineScreen.style.display = "none";
    }
}

function addLog(service, msg) {
    const term = document.getElementById("log-feed-terminal");
    if (!term) return;
    const time = new Date().toISOString();
    term.innerHTML += `<div class="log-line">[${time}] INFO [${service}]: ${msg}</div>`;
    term.scrollTop = term.scrollHeight;
}

function toggleCartSlide() {
    document.getElementById("cart-slideout").classList.toggle("open");
    document.getElementById("cart-backdrop").classList.toggle("open");
}

function openCartSlide() {
    document.getElementById("cart-slideout").classList.add("open");
    document.getElementById("cart-backdrop").classList.add("open");
}

function toggleLanguage() {
    const nextLang = document.getElementById("lang-toggle-btn").innerText.trim() === "EN" ? "ES" : "EN";
    document.getElementById("lang-toggle-btn").innerText = nextLang;
}

// Persistent filter/search states
let currentCuisineFilter = "All";
let currentSearchQuery = "";
let currentSortOption = localStorage.getItem("delivo-restaurant-sort") || "default";

function filterCategory(cat) {
    currentCuisineFilter = cat;
    refreshCatalogDisplay();
}

function triggerSearch(query) {
    currentSearchQuery = query.toLowerCase().trim();
    refreshCatalogDisplay();
}

function refreshCatalogDisplay() {
    let filtered = [...mockStores];
    
    // 1. Cuisine Filter
    if (currentCuisineFilter && currentCuisineFilter !== "All") {
        filtered = filtered.filter(s => {
            const sc = s.cuisine.toLowerCase();
            const cf = currentCuisineFilter.toLowerCase();
            return sc.includes(cf) || cf.includes(sc);
        });
    }
    
    // 2. Search Query Filter
    if (currentSearchQuery) {
        filtered = filtered.filter(s => 
            s.name.toLowerCase().includes(currentSearchQuery) || 
            s.desc.toLowerCase().includes(currentSearchQuery) ||
            s.cuisine.toLowerCase().includes(currentSearchQuery)
        );
    }
    
    // 3. Sorting Options
    if (currentSortOption === "rating") {
        filtered.sort((a, b) => parseFloat(b.rating) - parseFloat(a.rating));
    } else if (currentSortOption === "distance") {
        filtered.sort((a, b) => a.distanceVal - b.distanceVal);
    } else if (currentSortOption === "time") {
        filtered.sort((a, b) => parseInt(a.time) - parseInt(b.time));
    } else if (currentSortOption === "price-low") {
        filtered.sort((a, b) => a.minOrder - b.minOrder);
    } else if (currentSortOption === "price-high") {
        filtered.sort((a, b) => b.minOrder - a.minOrder);
    } else if (currentSortOption === "popularity") {
        filtered.sort((a, b) => b.reviewsCount - a.reviewsCount);
    } else if (currentSortOption === "newest") {
        filtered.sort((a, b) => b.id.localeCompare(a.id));
    }
    
    renderCatalog(filtered);

    // Populate landing page grid as well!
    const landingGrid = document.getElementById("landing-catalog-grid");
    if (landingGrid) {
        landingGrid.innerHTML = "";
        filtered.forEach(s => {
            const isFav = savedRestaurants.includes(s.id);
            landingGrid.innerHTML += `
                <div class="store-card" onclick="showAuthPage('CUSTOMER')">
                    <div class="store-img" style="position: relative; width: 100%; height: 160px; overflow: hidden; background: var(--border);">
                        <img src="${s.image}" alt="${s.name}" loading="lazy" style="width:100%; height:100%; object-fit:cover;">
                        <div class="store-tag">${s.cuisine}</div>
                        <button class="favorite-btn ${isFav ? 'active' : ''}" id="landing-fav-btn-${s.id}">
                            <i class="fa-solid fa-star" style="${isFav ? 'color: #FFB703;' : ''}"></i>
                        </button>
                    </div>
                    <div class="store-info">
                        <h4>${s.name}</h4>
                        <p>${s.desc}</p>
                        <div class="store-meta">
                            <span><i class="fa-solid fa-star" style="color: #FFB703;"></i> ${s.rating}</span>
                            <span><i class="fa-solid fa-clock"></i> ${s.time}</span>
                            <span><i class="fa-solid fa-route"></i> ${s.distance}</span>
                        </div>
                    </div>
                </div>
            `;
        });
    }
}

// Saved / Favorite restaurants toggle
let savedRestaurants = JSON.parse(localStorage.getItem("delivo-fav-stores") || "[]");
function toggleFavoriteStore(storeId, event) {
    if (event) event.stopPropagation();
    const idx = savedRestaurants.indexOf(storeId);
    if (idx > -1) {
        savedRestaurants.splice(idx, 1);
        showToast("Removed from saved restaurants.", "info");
    } else {
        savedRestaurants.push(storeId);
        showToast("Added to saved restaurants!", "success");
    }
    localStorage.setItem("delivo-fav-stores", JSON.stringify(savedRestaurants));
    refreshCatalogDisplay();
}

function renderCatalog(stores) {
    const container = document.getElementById("catalog-grid");
    if (!container) return;
    container.innerHTML = "";
    
    if (stores.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-muted); font-size: 13px;">
                <i class="fa-solid fa-store-slash" style="font-size: 32px; margin-bottom: 10px; display: block;"></i>
                No matching restaurants found. Try altering your filters.
            </div>
        `;
        return;
    }
    
    stores.forEach(s => {
        const isFav = savedRestaurants.includes(s.id);
        container.innerHTML += `
            <div class="store-card" onclick="openMenu('${s.id}')">
                <div class="store-img" style="position: relative; width: 100%; height: 160px; overflow: hidden; background: var(--border);">
                    <div class="img-skeleton"></div>
                    <img src="${s.image}" alt="${s.name}" loading="lazy" 
                        onload="this.previousElementSibling.style.display='none';" 
                        onerror="this.src='https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=300&q=80'; this.previousElementSibling.style.display='none';" 
                        style="width:100%; height:100%; object-fit:cover;">
                    <div class="store-tag">${s.cuisine}</div>
                    <button class="favorite-btn ${isFav ? 'active' : ''}" id="fav-btn-${s.id}" onclick="toggleFavoriteStore('${s.id}', event)">
                        <i class="fa-solid fa-star" style="${isFav ? 'color: #FFB703;' : ''}"></i>
                    </button>
                </div>
                <div class="store-info">
                    <h4>${s.name}</h4>
                    <p>${s.desc}</p>
                    <div class="store-meta">
                        <span><i class="fa-solid fa-star" style="color: #FFB703;"></i> ${s.rating} (${s.reviewsCount})</span>
                        <span><i class="fa-solid fa-clock"></i> ${s.time}</span>
                        <span><i class="fa-solid fa-route"></i> ${s.distance}</span>
                    </div>
                </div>
            </div>
        `;
    });
}

// Mailbox System Logic
function renderMailboxList() {
    const listContainer = document.getElementById("mailbox-message-list");
    if (!listContainer) return;
    listContainer.innerHTML = "";
    
    const searchVal = document.getElementById("mailbox-search").value.toLowerCase().trim();
    
    const filtered = mockEmails.filter(m => {
        if (m.folder !== activeMailFolder) return false;
        if (searchVal) {
            return m.from.toLowerCase().includes(searchVal) || 
                   m.subject.toLowerCase().includes(searchVal) || 
                   m.body.toLowerCase().includes(searchVal);
        }
        return true;
    });

    const inboxUnread = mockEmails.filter(m => m.folder === "inbox" && !m.read).length;
    const navBadge = document.getElementById("mailbox-inbox-badge");
    const folderBadge = document.getElementById("mailbox-unread-count");
    
    if (folderBadge) folderBadge.innerText = inboxUnread;
    if (navBadge) {
        navBadge.innerText = inboxUnread;
        navBadge.style.display = inboxUnread > 0 ? "inline-block" : "none";
    }

    if (filtered.length === 0) {
        listContainer.innerHTML = `<div style="text-align:center; padding:30px; color:var(--text-muted); font-size:12px;">No messages in this folder.</div>`;
        return;
    }

    filtered.forEach(m => {
        const isSelected = m.id === activeMailId;
        listContainer.innerHTML += `
            <div class="mailbox-item ${m.read ? '' : 'unread'} ${isSelected ? 'active' : ''}" onclick="selectMail('${m.id}')" style="border-bottom:1px solid var(--border); padding:12px; cursor:pointer;">
                <div style="display:flex; justify-content:space-between; font-size:12px;">
                    <span style="color:var(--secondary); font-weight:700;">${m.from}</span>
                    <span style="color:var(--text-muted); font-size:10px;">${m.date}</span>
                </div>
                <div style="font-size:12px; color:var(--text-main); margin-top:2px; text-overflow:ellipsis; overflow:hidden; white-space:nowrap; font-weight:${m.read ? '500':'700'};">${m.subject}</div>
                <div style="font-size:11px; color:var(--text-muted); text-overflow:ellipsis; overflow:hidden; white-space:nowrap; margin-top:2px;">${m.body.substring(0, 40)}...</div>
            </div>
        `;
    });
}

function selectMail(id) {
    activeMailId = id;
    const email = mockEmails.find(m => m.id === id);
    if (!email) return;

    email.read = true;
    renderMailboxList();

    const pane = document.getElementById("mailbox-content-pane");
    if (!pane) return;

    pane.innerHTML = `
        <div style="border-bottom:1px solid var(--border); padding-bottom:15px; margin-bottom:15px;">
            <div style="display:flex; justify-content:space-between; align-items:start; flex-wrap:wrap; gap:10px;">
                <div>
                    <h3 style="margin: 0 0 6px 0; font-size:16px;">${email.subject}</h3>
                    <div style="font-size:12px; color:var(--text-muted);">From: <strong style="color:var(--secondary);">${email.from}</strong></div>
                </div>
                <div style="font-size:11px; color:var(--text-muted); text-align:right;">${email.date}</div>
            </div>
            
            <div style="margin-top:15px; display:flex; gap:10px;">
                <button class="header-btn" style="padding:4px 12px; font-size:11px; margin:0;" onclick="showReplyBox('${email.id}')"><i class="fa-solid fa-reply"></i> Reply</button>
                <button class="header-btn" style="padding:4px 12px; font-size:11px; margin:0;" onclick="deleteMail('${email.id}')"><i class="fa-solid fa-trash"></i> Delete</button>
                <button class="header-btn" style="padding:4px 12px; font-size:11px; margin:0;" onclick="markMailUnread('${email.id}')">Mark Unread</button>
            </div>
        </div>
        <div style="font-size:13px; line-height:1.6; color:var(--text-main); white-space:pre-line; background: rgba(255,255,255,0.01); padding:15px; border-radius:6px; border:1px solid var(--border);">
            ${email.body}
        </div>
        <div id="mailbox-reply-container" style="margin-top:20px;"></div>
    `;
}

function showReplyBox(id) {
    const email = mockEmails.find(m => m.id === id);
    if (!email) return;

    const container = document.getElementById("mailbox-reply-container");
    if (!container) return;

    container.innerHTML = `
        <div class="checkout-box" style="margin:0; background:rgba(255,255,255,0.02); border:1px solid var(--border); padding:15px;">
            <h5 style="margin:0 0 10px 0; font-size:12px;">Reply to ${email.from}</h5>
            <textarea id="reply-body-text" class="input-field" rows="4" placeholder="Type your reply here..." style="width:100%; font-size:12px; margin-bottom:10px; resize:none;"></textarea>
            <button class="btn-submit-order" style="padding:8px 16px; font-size:12px;" onclick="sendReplyMessage('${email.id}')">Send Reply</button>
        </div>
    `;
}

function sendReplyMessage(id) {
    const email = mockEmails.find(m => m.id === id);
    const text = document.getElementById("reply-body-text").value.trim();
    if (!text || !email) {
        showToast("Please enter a reply message.", "warning");
        return;
    }

    mockEmails.push({
        id: "mail_" + (mockEmails.length + 1),
        folder: "sent",
        from: "You",
        subject: `Re: ${email.subject}`,
        body: text,
        date: new Date().toISOString().replace('T', ' ').substring(0, 16),
        read: true
    });

    showToast("Reply sent successfully!", "success");
    activeMailFolder = "sent";
    
    // Toggle sidebar active highlights
    document.querySelectorAll(".mailbox-folder-btn").forEach(b => {
        b.classList.remove("active");
        if (b.getAttribute("data-folder") === "sent") b.classList.add("active");
    });

    renderMailboxList();
    const pane = document.getElementById("mailbox-content-pane");
    if (pane) pane.innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
}

function deleteMail(id) {
    const email = mockEmails.find(m => m.id === id);
    if (!email) return;

    if (email.folder === "trash") {
        // Delete permanently
        showConfirm("Permanently Delete", "Delete this email permanently from trash?", () => {
            mockEmails = mockEmails.filter(m => m.id !== id);
            showToast("Email deleted permanently.", "info");
            renderMailboxList();
            document.getElementById("mailbox-content-pane").innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
        });
    } else {
        email.folder = "trash";
        showToast("Message moved to Trash.", "info");
        renderMailboxList();
        document.getElementById("mailbox-content-pane").innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
    }
}

function markMailUnread(id) {
    const email = mockEmails.find(m => m.id === id);
    if (email) {
        email.read = false;
        showToast("Marked as unread.", "info");
        renderMailboxList();
        document.getElementById("mailbox-content-pane").innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
    }
}

function showComposeBox() {
    activeMailId = null;
    renderMailboxList();

    const pane = document.getElementById("mailbox-content-pane");
    if (!pane) return;

    pane.innerHTML = `
        <div style="border-bottom:1px solid var(--border); padding-bottom:12px; margin-bottom:15px;">
            <h3 style="margin: 0; font-size:15px;">Compose New Message</h3>
        </div>
        <div class="checkout-box" style="margin:0; background:rgba(255,255,255,0.01); border:1px solid var(--border); padding:15px; display:flex; flex-direction:column; gap:12px;">
            <div>
                <label style="display:block; font-size:11px; color:var(--text-muted); margin-bottom:4px;">RECIPIENT</label>
                <input type="text" id="compose-to" class="input-field" placeholder="e.g. support@delivo.com" style="margin:0; font-size:12px;">
            </div>
            <div>
                <label style="display:block; font-size:11px; color:var(--text-muted); margin-bottom:4px;">SUBJECT</label>
                <input type="text" id="compose-subject" class="input-field" placeholder="Enter message subject" style="margin:0; font-size:12px;">
            </div>
            <div>
                <label style="display:block; font-size:11px; color:var(--text-muted); margin-bottom:4px;">MESSAGE</label>
                <textarea id="compose-body" class="input-field" rows="6" placeholder="Type your message details here..." style="margin:0; font-size:12px; resize:none;"></textarea>
            </div>
            <button class="btn-submit-order" style="padding:10px 20px; font-size:13px;" onclick="sendComposeMessage()">Send Message</button>
        </div>
    `;
}

function sendComposeMessage() {
    const to = document.getElementById("compose-to").value.trim();
    const subject = document.getElementById("compose-subject").value.trim();
    const body = document.getElementById("compose-body").value.trim();

    if (!to || !subject || !body) {
        showToast("Please fill in all compose fields.", "warning");
        return;
    }

    mockEmails.push({
        id: "mail_" + (mockEmails.length + 1),
        folder: "sent",
        from: "You",
        subject: subject,
        body: `To: ${to}\n\n${body}`,
        date: new Date().toISOString().replace('T', ' ').substring(0, 16),
        read: true
    });

    showToast("Email dispatched successfully!", "success");
    activeMailFolder = "sent";
    
    document.querySelectorAll(".mailbox-folder-btn").forEach(b => {
        b.classList.remove("active");
        if (b.getAttribute("data-folder") === "sent") b.classList.add("active");
    });

    renderMailboxList();
    document.getElementById("mailbox-content-pane").innerHTML = `<div style="text-align:center; padding:50px; color:var(--text-muted); font-size:13px;">Select an email to read.</div>`;
}

// Wallet Operations & Presets logic
function renderWalletLedgerView() {
    const tbody = document.getElementById("wallet-transactions-tbody");
    const emptyState = document.getElementById("wallet-tx-empty-state");
    if (!tbody) return;
    tbody.innerHTML = "";

    document.getElementById("wallet-balance-val").innerText = `$${walletBalance.toFixed(2)}`;

    const filterVal = document.getElementById("wallet-tx-filter").value;
    const searchVal = document.getElementById("wallet-tx-search").value.toLowerCase().trim();

    const filtered = walletLedger.filter(log => {
        // Filter by type
        if (filterVal !== "ALL") {
            if (filterVal === "DEBIT" && log.type !== "DEBIT") return false;
            if (filterVal === "CASHBACK" && log.type !== "CASHBACK") return false;
            if (filterVal === "UPI" && log.type !== "UPI" && log.type !== "QR") return false;
            if (filterVal === "CARD" && log.type !== "CARD" && log.type !== "TOPUP" && log.type !== "NETBANKING") return false;
        }
        // Filter by search
        if (searchVal) {
            return log.id.toLowerCase().includes(searchVal) || 
                   log.type.toLowerCase().includes(searchVal) ||
                   log.amount.toString().includes(searchVal);
        }
        return true;
    });

    if (filtered.length === 0) {
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    filtered.forEach(log => {
        tbody.innerHTML += `
            <tr>
                <td><strong>${log.id}</strong></td>
                <td>${log.time}</td>
                <td><span class="badge" style="background: ${log.type === 'DEBIT' ? 'var(--primary)' : 'var(--neon-green)'}; color:white;">${log.type}</span></td>
                <td><strong style="color: ${log.type === 'DEBIT' ? 'var(--primary)' : 'var(--neon-green)'};">${log.type === 'DEBIT' ? '-' : '+'}$${log.amount.toFixed(2)}</strong></td>
                <td><button class="header-btn" style="padding:2px 8px; margin:0;" onclick="downloadReceipt('${log.id}')">Receipt</button></td>
            </tr>
        `;
    });
}

function downloadReceipt(txId) {
    const log = walletLedger.find(l => l.id === txId);
    if (!log) return;

    let doc = `=========================================
DELIVO OS TRANSACTION INVOICE RECEIPT
=========================================
Transaction ID: ${log.id}
Date & Time:    ${log.time}
Ledger Type:    ${log.type}
Amount:         $${log.amount.toFixed(2)}
Remaining Bal:  $${log.bal.toFixed(2)}
Status:         SETTLED & CLEARED
=========================================
Thank you for using Delivo OS!
`;
    const blob = new Blob([doc], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.setAttribute('href', url);
    a.setAttribute('download', `receipt_${log.id}.txt`);
    a.click();
    showToast("Downloaded transaction receipt.", "success");
}

function addWalletLedgerFunds() {
    const input = document.getElementById("wallet-input-amount");
    const amount = parseFloat(input.value);

    if (isNaN(amount) || amount <= 0) {
        showToast("Please enter a valid fund amount.", "warning");
        return;
    }

    if (amount > 500) {
        showToast("Transaction limit exceeded. Maximum ledger top-up is $500 per transaction.", "error");
        return;
    }

    const payMode = document.querySelector('input[name="wallet-pay-mode"]:checked').value;
    
    // Simulate transaction
    walletBalance += amount;
    syncLocalWalletBalances();

    // Create entry
    walletLedger.push({
        id: "tx_ledger_" + Math.random().toString(36).substr(2, 9),
        time: new Date().toISOString().replace('T', ' ').substring(0, 19),
        type: payMode.toUpperCase(),
        amount: amount,
        bal: walletBalance
    });

    showToast(`Successfully credited $${amount.toFixed(2)} via ${payMode.toUpperCase()}!`, "success");
    input.value = "";
    
    renderWalletLedgerView();
}

function exportWalletTransactions() {
    let csv = "Transaction ID,Timestamp,Payment Method,Amount,Balance\n";
    walletLedger.forEach(log => {
        csv += `${log.id},${log.time},${log.type},${log.amount},${log.bal}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.setAttribute('href', url);
    a.setAttribute('download', 'delivo_wallet_transactions.csv');
    a.click();
    showToast("Exported transaction ledger to CSV.", "success");
}

// Admin Hub Managers
function renderAdminCoupons() {
    const tbody = document.getElementById("admin-coupons-tbody");
    if (!tbody) return;
    tbody.innerHTML = "";
    couponDatabase.forEach(c => {
        tbody.innerHTML += `
            <tr>
                <td><strong>${c.code}</strong></td>
                <td>${c.value}%</td>
                <td><span style="color:var(--neon-green)">ACTIVE</span></td>
                <td><button class="header-btn" style="padding:2px 8px; margin:0; background:var(--primary);" onclick="deleteAdminCoupon('${c.code}')">Delete</button></td>
            </tr>
        `;
    });
}

function addAdminCoupon() {
    const code = document.getElementById("admin-new-coupon").value.toUpperCase().trim();
    const discount = parseInt(document.getElementById("admin-new-discount").value);

    if (!code || isNaN(discount) || discount <= 0 || discount > 90) {
        showToast("Please enter a valid code and discount value (1-90%).", "warning");
        return;
    }

    if (couponDatabase.some(c => c.code === code)) {
        showToast("Coupon code already exists.", "warning");
        return;
    }

    couponDatabase.push({ code, value: discount, active: true });
    showToast(`Coupon ${code} added successfully.`, "success");
    
    document.getElementById("admin-new-coupon").value = "";
    document.getElementById("admin-new-discount").value = "";
    renderAdminCoupons();
}

function deleteAdminCoupon(code) {
    couponDatabase = couponDatabase.filter(c => c.code !== code);
    showToast(`Coupon ${code} deleted.`, "info");
    renderAdminCoupons();
}

function renderAdminRefunds() {
    const tbody = document.getElementById("admin-refunds-tbody");
    if (!tbody) return;
    tbody.innerHTML = "";

    // List recent DEBIT transactions from walletLedger as potential refund requests
    const debits = walletLedger.filter(l => l.type === "DEBIT");
    if (debits.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-muted);">No refund dispute claims pending.</td></tr>`;
        return;
    }

    debits.forEach(d => {
        tbody.innerHTML += `
            <tr>
                <td><strong>${d.id}</strong></td>
                <td>$${d.amount.toFixed(2)}</td>
                <td>Gateway Overload</td>
                <td><button class="header-btn" style="padding:2px 8px; margin:0; background:var(--secondary); color:var(--bg);" onclick="approveAdminRefund('${d.id}')">Approve</button></td>
            </tr>
        `;
    });
}

function approveAdminRefund(txId) {
    const log = walletLedger.find(l => l.id === txId);
    if (!log) return;

    showConfirm("Approve Refund Claim", `Process refund of $${log.amount.toFixed(2)} back to user wallet ledger?`, () => {
        walletBalance += log.amount;
        syncLocalWalletBalances();
        
        // Remove debit or add a refund entry
        walletLedger.push({
            id: "tx_ref_" + Math.random().toString(36).substr(2, 9),
            time: new Date().toISOString().replace('T', ' ').substring(0, 19),
            type: "CASHBACK",
            amount: log.amount,
            bal: walletBalance
        });
        
        showToast("Refund transaction completed successfully!", "success");
        renderAdminRefunds();
        renderWalletLedgerView();
    });
}

// Micro-interaction Flying dot animator
function triggerFlyingDot(event) {
    const target = event.currentTarget || event.target;
    if (!target) return;

    const startRect = target.getBoundingClientRect();
    const cartBtn = document.getElementById("cart-indicator-btn");
    if (!cartBtn) return;
    const endRect = cartBtn.getBoundingClientRect();

    const dot = document.createElement("div");
    dot.className = "flying-dot";
    dot.style.left = `${startRect.left + startRect.width / 2 - 7}px`;
    dot.style.top = `${startRect.top + startRect.height / 2 - 7}px`;
    document.body.appendChild(dot);

    // Force repaint
    dot.offsetWidth;

    dot.style.transform = `translate(${endRect.left - startRect.left}px, ${endRect.top - startRect.top}px) scale(0.3)`;
    dot.style.opacity = "0.1";

    setTimeout(() => {
        dot.remove();
        // Shake badge
        cartBtn.classList.add("pulse");
        setTimeout(() => cartBtn.classList.remove("pulse"), 200);
    }, 800);
}

// Footer policy overlay
function openLegalModal(docName) {
    const text = `DELIVO OS LEGAL CHARTER - ${docName.toUpperCase()}
    
    This legal charter constitutes a binding agreement conforming to ISO 27001 data sovereignty guidelines. 
    1. Wallet deposits remain fully bonded in the system credit ledger.
    2. Geofence validations map live coordinates relative to Maharashtra, India coordinates.
    3. Custom coupon entries calculate dynamic cart subtotals prior to saga submission.
    
    Verification hash: SHA256-${Math.random().toString(36).substr(2, 9)}`;
    
    document.getElementById("confirm-modal-title").innerText = docName;
    document.getElementById("confirm-modal-message").innerText = text;
    document.getElementById("confirm-modal").style.display = "flex";
    
    // Clear callback to disable execution
    confirmCallback = null;
}

function updateAdminDashboardCounters() {
    const totalEl = document.getElementById("admin-orders-total");
    const pendingEl = document.getElementById("admin-orders-pending");
    const completedEl = document.getElementById("admin-orders-completed");
    const cancelledEl = document.getElementById("admin-orders-cancelled");
    
    if (!totalEl) return;
    
    const total = orderHistory.length;
    const pending = orderHistory.filter(o => o.status === "PENDING").length;
    const completed = orderHistory.filter(o => o.status === "DELIVERED").length;
    const cancelled = orderHistory.filter(o => o.status === "CANCELLED").length;
    
    totalEl.innerText = total;
    pendingEl.innerText = pending;
    completedEl.innerText = completed;
    cancelledEl.innerText = cancelled;

    // Calculate revenue values from DELIVERED orders
    const totalDeliveredSum = orderHistory.filter(o => o.status === "DELIVERED").reduce((acc, curr) => acc + curr.amount, 0);
    const dailyRev = document.getElementById("admin-revenue-daily");
    const weeklyRev = document.getElementById("admin-revenue-weekly");
    const monthlyRev = document.getElementById("admin-revenue-monthly");
    
    if (dailyRev) {
        dailyRev.innerText = `$${(totalDeliveredSum + 1840.50).toFixed(2)}`;
    }
    if (weeklyRev) {
        weeklyRev.innerText = `$${(totalDeliveredSum + 12450.00).toFixed(2)}`;
    }
    if (monthlyRev) {
        monthlyRev.innerText = `$${(totalDeliveredSum + 48250.00).toFixed(2)}`;
    }
}

// ==========================================================================
// PRODUCTION JWT BACKEND AUTHENTICATION WIRING
// ==========================================================================
let currentUserRole = null;

function checkAuthOnLoad() {
    const token = localStorage.getItem("delivo-token");
    const role = localStorage.getItem("delivo-role");
    const email = localStorage.getItem("delivo-email");

    const tabs = document.getElementById("main-nav-tabs");
    const logoutBtn = document.getElementById("btn-logout");

    if (token && role) {
        if (tabs) tabs.style.display = "flex";
        if (logoutBtn) logoutBtn.style.display = "block";
        
        // Show corresponding views
        if (role === "ADMIN") {
            switchView("view-admin");
        } else {
            switchView("view-customer");
        }
    } else {
        if (tabs) tabs.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "none";
        switchView("view-landing");
    }
}

function showAuthPage(role) {
    currentUserRole = role;
    if (role === "ADMIN") {
        navigateToPath("/auth/admin/login");
    } else {
        navigateToPath("/auth/customer/login");
    }
}

function toggleAuthForm(type) {
    const loginForm = document.getElementById("auth-login-form");
    const registerForm = document.getElementById("auth-register-form");
    const forgotForm = document.getElementById("auth-forgot-form");
    
    const switchText = document.getElementById("auth-switch-text");
    const switchLink = document.getElementById("auth-switch-link");

    if (loginForm) loginForm.style.display = type === "login" ? "block" : "none";
    if (registerForm) registerForm.style.display = type === "register" ? "block" : "none";
    if (forgotForm) forgotForm.style.display = type === "forgot" ? "block" : "none";

    if (type === "login") {
        if (switchText) switchText.innerText = "New to Delivo?";
        if (switchLink) {
            switchLink.innerText = "Create an Account";
            switchLink.setAttribute("onclick", "toggleAuthForm('register')");
            switchLink.style.display = "inline";
        }
    } else if (type === "register") {
        if (switchText) switchText.innerText = "Already have an account?";
        if (switchLink) {
            switchLink.innerText = "Sign In";
            switchLink.setAttribute("onclick", "toggleAuthForm('login')");
            switchLink.style.display = "inline";
        }
    } else {
        if (switchText) switchText.innerText = "Remember your password?";
        if (switchLink) {
            switchLink.innerText = "Sign In";
            switchLink.setAttribute("onclick", "toggleAuthForm('login')");
            switchLink.style.display = "inline";
        }
    }
}

function handleAuthSubmit(event) {
    event.preventDefault();
    const isAdminForm = event.target.id === "admin-login-form";

    const emailField = isAdminForm ? document.getElementById("admin-email") : document.getElementById("auth-email");
    const passField = isAdminForm ? document.getElementById("admin-password") : document.getElementById("auth-password");

    const email = emailField ? emailField.value.trim() : "";
    const password = passField ? passField.value : "";

    fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email: email, password: password })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Invalid username or password");
        }
        return res.json();
    })
    .then(data => {
        if (data.token || data.success) {
            const token = data.token || "dummy-dev-token";
            const emailVal = data.email || email;
            const roles = data.roles || [data.role];
            const primaryRole = isAdminForm ? "ADMIN" : ((roles && roles.length > 0) ? roles[0] : "CUSTOMER");

            localStorage.setItem("delivo-token", token);
            localStorage.setItem("delivo-email", emailVal);
            localStorage.setItem("delivo-role", primaryRole);
            
            showToast("Authenticated successfully!", "success");
            
            const tabs = document.getElementById("main-nav-tabs");
            const logoutBtn = document.getElementById("btn-logout");
            if (tabs) tabs.style.display = "flex";
            if (logoutBtn) logoutBtn.style.display = "block";
            
            if (primaryRole === "ADMIN") {
                switchView("view-admin");
            } else {
                switchView("view-customer");
            }
        }
    })
    .catch(err => {
        showToast(err.message, "error");
    });
}

function handleRegisterSubmit(event) {
    event.preventDefault();
    const email = document.getElementById("reg-email").value.trim();
    const password = document.getElementById("reg-password").value;
    const confirm = document.getElementById("reg-confirm").value;

    if (password !== confirm) {
        showToast("Passwords do not match!", "error");
        return;
    }

    fetch("/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email: email, password: password })
    })
    .then(res => {
        if (!res.ok) {
            if (res.status === 404) {
                return "Mock registration success (Dev Server)";
            }
            return res.text().then(text => { throw new Error(text) });
        }
        return res.text();
    })
    .then(msg => {
        showToast("Registration successful! You can now log in.", "success");
        toggleAuthForm("login");
    })
    .catch(err => {
        showToast(err.message, "error");
    });
}

function handleForgotSubmit(event) {
    event.preventDefault();
    const email = document.getElementById("forgot-email").value.trim();
    showToast(`Password recovery link dispatched to ${email}`, "info");
    toggleAuthForm("login");
}

function handleLogout() {
    localStorage.removeItem("delivo-token");
    localStorage.removeItem("delivo-role");
    localStorage.removeItem("delivo-email");
    
    const tabs = document.getElementById("main-nav-tabs");
    const logoutBtn = document.getElementById("btn-logout");
    if (tabs) tabs.style.display = "none";
    if (logoutBtn) logoutBtn.style.display = "none";
    
    showToast("Logged out successfully.", "info");
    switchView("view-landing");
}

// ==========================================================================
// ZOMATO/SWIGGY-LEVEL PRODUCTION FLOW LOGIC
// ==========================================================================

// 1. OTP Authentication
let activeCustomerAuthMode = "pass";
function toggleCustomerAuthMode(mode) {
    activeCustomerAuthMode = mode;
    const passGroup = document.getElementById("customer-pass-group");
    const otpGroup = document.getElementById("customer-otp-group");
    const forgotLink = document.getElementById("auth-forgot-link");

    const passBtn = document.getElementById("btn-auth-pass");
    const otpBtn = document.getElementById("btn-auth-otp");

    if (mode === "pass") {
        if (passGroup) passGroup.style.display = "block";
        if (otpGroup) otpGroup.style.display = "none";
        if (forgotLink) forgotLink.style.display = "inline";
        if (passBtn) {
            passBtn.classList.add("active");
            passBtn.style.borderBottom = "2px solid var(--secondary)";
            passBtn.style.color = "var(--text-main)";
        }
        if (otpBtn) {
            otpBtn.classList.remove("active");
            otpBtn.style.borderBottom = "none";
            otpBtn.style.color = "var(--text-muted)";
        }
    } else {
        if (passGroup) passGroup.style.display = "none";
        if (otpGroup) otpGroup.style.display = "block";
        if (forgotLink) forgotLink.style.display = "none";
        if (passBtn) {
            passBtn.classList.remove("active");
            passBtn.style.borderBottom = "none";
            passBtn.style.color = "var(--text-muted)";
        }
        if (otpBtn) {
            otpBtn.classList.add("active");
            otpBtn.style.borderBottom = "2px solid var(--secondary)";
            otpBtn.style.color = "var(--text-main)";
        }
    }
}

function sendAuthOtp() {
    const email = document.getElementById("auth-email").value.trim();
    if (!email) {
        showToast("Please enter an email address first.", "warning");
        return;
    }
    showToast("OTP Code '123456' successfully sent to your email/phone.", "success");
    document.getElementById("auth-otp-code").value = "123456";
}

// 2. Location Services
let currentSelectedAddress = "Bandra East, Mumbai, Maharashtra";
function triggerLocationDetect() {
    showToast("Scanning GPS satellite geofence coordinates...", "info");
    setTimeout(() => {
        currentSelectedAddress = "IT Tech Park, Bandra East, Mumbai, Maharashtra (Auto-Detected)";
        const txt = document.getElementById("current-location-text");
        if (txt) txt.innerText = currentSelectedAddress;
        
        const field = document.getElementById("delivery-address-field");
        if (field) field.value = currentSelectedAddress;

        showToast("Auto-detected location coordinates: 19.0760° N, 72.8777° E.", "success");
        closeLocationModal();
    }, 1200);
}

function openLocationModal() {
    const modal = document.getElementById("location-modal");
    if (modal) modal.style.display = "flex";
}

function closeLocationModal() {
    const modal = document.getElementById("location-modal");
    if (modal) modal.style.display = "none";
}

function selectSavedAddress(label, address) {
    currentSelectedAddress = `${label}: ${address}`;
    const txt = document.getElementById("current-location-text");
    if (txt) txt.innerText = currentSelectedAddress;
    
    const field = document.getElementById("delivery-address-field");
    if (field) field.value = address;

    showToast(`Selected saved address: ${label}`, "success");
    closeLocationModal();
}

function saveLocationSelection() {
    const manual = document.getElementById("manual-address-input").value.trim();
    if (manual) {
        currentSelectedAddress = manual;
        const txt = document.getElementById("current-location-text");
        if (txt) txt.innerText = currentSelectedAddress;
        
        const field = document.getElementById("delivery-address-field");
        if (field) field.value = manual;
    }
    showToast("Delivery address confirmed.", "success");
    closeLocationModal();
}

// 3. Item Customization Modal
let pendingCustomizationItem = null;
let pendingCustomizationStoreId = null;

function openCustomizationModal(item, storeId) {
    pendingCustomizationItem = item;
    pendingCustomizationStoreId = storeId;

    document.getElementById("custom-item-name").innerText = `Customize ${item.name}`;
    document.getElementById("custom-item-desc").innerText = item.desc;
    
    // Reset check inputs
    document.getElementById("addon-cheese").checked = false;
    document.getElementById("addon-sauce").checked = false;
    document.querySelector('input[name="portion-size"][value="regular"]').checked = true;

    const modal = document.getElementById("customization-modal");
    if (modal) modal.style.display = "flex";
}

function closeCustomizationModal() {
    const modal = document.getElementById("customization-modal");
    if (modal) modal.style.display = "none";
}

function confirmCustomizations() {
    if (!pendingCustomizationItem) return;

    let finalPrice = pendingCustomizationItem.price;
    let addOnTexts = [];

    // Portion Size check
    const portion = document.querySelector('input[name="portion-size"]:checked').value;
    if (portion === "large") {
        finalPrice += 2.50;
        addOnTexts.push("Large");
    }

    // Addons check
    if (document.getElementById("addon-cheese").checked) {
        finalPrice += 1.50;
        addOnTexts.push("Extra Cheese");
    }
    if (document.getElementById("addon-sauce").checked) {
        finalPrice += 0.75;
        addOnTexts.push("Extra Sauce");
    }

    const customizedItemName = pendingCustomizationItem.name + (addOnTexts.length > 0 ? ` (${addOnTexts.join(", ")})` : "");

    const existing = cartItems.find(c => c.name === customizedItemName);
    if (existing) {
        existing.qty++;
    } else {
        cartItems.push({
            id: pendingCustomizationItem.id + "_" + Math.random().toString(36).substr(2, 5),
            name: customizedItemName,
            price: finalPrice,
            qty: 1,
            storeId: pendingCustomizationStoreId,
            image: pendingCustomizationItem.image
        });
    }

    updateCartCounter();
    openMenu(pendingCustomizationStoreId);
    showToast(`Added ${customizedItemName} to cart.`, "success");
    closeCustomizationModal();
}

// 4. Group Ordering
function openGroupModal() {
    const modal = document.getElementById("group-order-modal");
    if (modal) modal.style.display = "flex";
}

function closeGroupModal() {
    const modal = document.getElementById("group-order-modal");
    if (modal) modal.style.display = "none";
}

function copyInviteLink() {
    const input = document.getElementById("group-invite-url");
    input.select();
    document.execCommand("copy");
    showToast("Invite link copied to clipboard!", "success");
}

function simulateFriendJoin() {
    const list = document.getElementById("group-members-list");
    if (!list) return;

    list.innerHTML += `
        <div style="display: flex; justify-content: space-between; font-size: 11px; padding: 6px; border-bottom: 1px solid var(--border);">
            <span>👤 Anjali Nair</span>
            <span style="color: var(--neon-green); font-weight: 700;">Joined</span>
        </div>
    `;

    // Add item to cart on behalf of friend
    if (pendingCustomizationStoreId || cartItems.length > 0) {
        const storeId = pendingCustomizationStoreId || cartItems[0].storeId;
        const mockItem = mockMenus[storeId][0]; // Margherita or similar first item
        cartItems.push({
            id: mockItem.id + "_friend",
            name: `${mockItem.name} (Anjali Nair)`,
            price: mockItem.price,
            qty: 1,
            storeId: storeId,
            image: mockItem.image
        });
        updateCartCounter();
        openMenu(storeId);
        showToast("Anjali Nair added Classic Margherita Pizza to the group cart!", "info");
    }
}

// 5. Scheduled Orders
let scheduledOrderDetails = null;
function openSchedulerModal() {
    const modal = document.getElementById("scheduler-modal");
    if (modal) modal.style.display = "flex";
}

function closeSchedulerModal() {
    const modal = document.getElementById("scheduler-modal");
    if (modal) modal.style.display = "none";
}

function confirmOrderSchedule() {
    const date = document.getElementById("schedule-date-input").value;
    const time = document.getElementById("schedule-time-input").value;
    const repeat = document.getElementById("schedule-repeat-select").value;

    if (!date || !time) {
        showToast("Please choose a valid date and time.", "warning");
        return;
    }

    scheduledOrderDetails = { date, time, repeat };
    
    const statusLabel = document.getElementById("cart-schedule-status");
    if (statusLabel) {
        statusLabel.innerText = `Scheduled for: ${date} at ${time} (${repeat === 'none' ? 'One-time' : repeat})`;
        statusLabel.style.display = "block";
    }

    showToast(`Order scheduled successfully for ${date} at ${time}.`, "success");
    closeSchedulerModal();
}

// 6. Timeline Stepper OTP Verification
let generatedDeliveryOtp = "2841";
function verifyDeliveryOtp() {
    const inputVal = document.getElementById("delivery-verification-otp").value.trim();
    if (inputVal !== generatedDeliveryOtp) {
        showToast("Invalid Delivery OTP. Please double check with the driver.", "error");
        return;
    }

    // Complete Stepper
    const fillBar = document.getElementById("saga-timeline-fill-bar");
    if (fillBar) fillBar.style.height = "100%";

    const nodeNear = document.getElementById("step-near-you");
    const nodeDelivered = document.getElementById("step-delivered");
    if (nodeNear) {
        nodeNear.classList.remove("active");
        nodeNear.classList.add("completed");
        const icon = nodeNear.querySelector(".node-bullet");
        if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
    }
    if (nodeDelivered) {
        nodeDelivered.classList.add("completed");
        const icon = nodeDelivered.querySelector(".node-bullet");
        if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
    }

    const valBox = document.getElementById("otp-validation-box");
    if (valBox) valBox.style.display = "none";

    // Complete order in history
    const ord = orderHistory.find(o => o.id === currentActiveTransactionId);
    if (ord) ord.status = "DELIVERED";
    renderOrderHistory();
    renderKdsBoard();

    // Show feedback popup
    showToast("Order delivered successfully!", "success");
    setTimeout(() => {
        const postModal = document.getElementById("post-delivery-modal");
        if (postModal) postModal.style.display = "flex";
    }, 1000);
}

// 7. Context Support Chat Widget
let supportChatOpen = false;
function toggleSupportChatWindow() {
    supportChatOpen = !supportChatOpen;
    const win = document.getElementById("support-chat-window");
    const icon = document.getElementById("support-chat-icon");

    if (win) win.style.display = supportChatOpen ? "flex" : "none";
    if (icon) icon.className = supportChatOpen ? "fa-solid fa-xmark" : "fa-solid fa-headset";
}

function handleSupportChatKeyPress(e) {
    if (e.key === "Enter") {
        sendSupportChatMessage();
    }
}

function sendSupportChatMessage() {
    const input = document.getElementById("support-chat-input");
    const text = input.value.trim();
    if (!text) return;

    const msgBox = document.getElementById("support-chat-messages");
    msgBox.innerHTML += `
        <div style="align-self: flex-end; background: var(--primary); color: white; padding: 8px 12px; border-radius: 8px; max-width: 80%;">
            ${text}
        </div>
    `;

    input.value = "";
    msgBox.scrollTop = msgBox.scrollHeight;

    // AI Mock Response
    setTimeout(() => {
        let reply = "I am processing your query. Please hold on...";
        if (text.toLowerCase().includes("status") || text.toLowerCase().includes("track")) {
            reply = "Your order is currently active. You can view the live progress stepper on the tracking screen.";
        } else if (text.toLowerCase().includes("refund") || text.toLowerCase().includes("cancel")) {
            reply = "To request a refund, please raise a claim under the Refund Disputes section in the Help menu.";
        } else if (text.toLowerCase().includes("late") || text.toLowerCase().includes("arrive")) {
            reply = "We apologize for the delay. The rider has been notified to speed up delivery.";
        }

        msgBox.innerHTML += `
            <div style="align-self: flex-start; background: rgba(255,255,255,0.03); border: 1px solid var(--border); padding: 8px 12px; border-radius: 8px; max-width: 80%;">
                ${reply}
            </div>
        `;
        msgBox.scrollTop = msgBox.scrollHeight;
    }, 1000);
}

// 8. Live Driver Chat widget
function openDriverChat() {
    const win = document.getElementById("driver-chat-window");
    if (win) win.style.display = "flex";
}

function closeDriverChat() {
    const win = document.getElementById("driver-chat-window");
    if (win) win.style.display = "none";
}

function sendQuickReply(text) {
    const msgBox = document.getElementById("driver-chat-messages");
    msgBox.innerHTML += `
        <div style="align-self: flex-end; background: var(--secondary); color: var(--bg); padding: 8px 12px; border-radius: 8px; max-width: 80%; font-weight: 600;">
            ${text}
        </div>
    `;
    msgBox.scrollTop = msgBox.scrollHeight;

    simulateRiderResponse(text);
}

function handleDriverChatKeyPress(e) {
    if (e.key === "Enter") {
        sendDriverChatMessage();
    }
}

function sendDriverChatMessage() {
    const input = document.getElementById("driver-chat-input");
    const text = input.value.trim();
    if (!text) return;

    const msgBox = document.getElementById("driver-chat-messages");
    msgBox.innerHTML += `
        <div style="align-self: flex-end; background: var(--secondary); color: var(--bg); padding: 8px 12px; border-radius: 8px; max-width: 80%; font-weight: 600;">
            ${text}
        </div>
    `;

    input.value = "";
    msgBox.scrollTop = msgBox.scrollHeight;

    simulateRiderResponse(text);
}

function simulateRiderResponse(userText) {
    const typing = document.getElementById("driver-typing-indicator");
    const msgBox = document.getElementById("driver-chat-messages");

    setTimeout(() => {
        if (typing) typing.style.display = "block";
        msgBox.scrollTop = msgBox.scrollHeight;
    }, 1000);

    setTimeout(() => {
        if (typing) typing.style.display = "none";

        let reply = "Understood! I will update you once I reach your building.";
        if (userText.toLowerCase().includes("gate")) {
            reply = "Got it, leaving the package at the main gate reception desk.";
        } else if (userText.toLowerCase().includes("bell")) {
            reply = "Sure, I won't ring the bell. Leaving it outside the door.";
        }

        msgBox.innerHTML += `
            <div style="align-self: flex-start; background: rgba(255,255,255,0.03); border: 1px solid var(--border); padding: 8px 12px; border-radius: 8px; max-width: 80%;">
                ${reply} <span style="font-size: 8px; color: var(--text-muted); display: block; text-align: right; margin-top: 4px;">Read ✓✓</span>
            </div>
        `;
        msgBox.scrollTop = msgBox.scrollHeight;
    }, 3000);
}

// 9. Post-Delivery Feedback Modal
let currentPostRating = 5;
function setPostFeedbackRating(stars) {
    currentPostRating = stars;
    const starBtns = document.querySelectorAll(".star-rating-btn");
    starBtns.forEach((btn, idx) => {
        if (idx < stars) {
            btn.style.color = "#FFB703";
        } else {
            btn.style.color = "var(--text-muted)";
        }
    });
}

let isRestaurantFavorite = false;
function toggleFavoriteRestaurant() {
    isRestaurantFavorite = !isRestaurantFavorite;
    const icon = document.getElementById("fav-heart-icon");
    if (icon) {
        icon.style.color = isRestaurantFavorite ? "var(--primary)" : "var(--text-muted)";
    }
    showToast(isRestaurantFavorite ? "Added restaurant to favorites!" : "Removed restaurant from favorites.", "info");
}

function triggerReorder() {
    showToast("Re-adding items from previous transaction to checkout cart...", "success");
    const postModal = document.getElementById("post-delivery-modal");
    if (postModal) postModal.style.display = "none";
    
    // Quick navigate to cart
    toggleCartSlide();
}

function submitPostFeedback() {
    const comments = document.getElementById("post-feedback-comments").value;
    showToast("Thank you for your feedback rating! Return to catalog.", "success");
    
    const postModal = document.getElementById("post-delivery-modal");
    if (postModal) postModal.style.display = "none";

    navigateToPath("/");
}

// 10. Driver Dashboard Operational controls
function driverReachedRestaurant() {
    const status = document.getElementById("driver-trip-status");
    if (status) {
        status.innerText = "Waiting for Merchant Food Prep...";
        status.style.color = "var(--neon-yellow)";
    }
    showToast("Rider status updated: Arrived at restaurant location.", "info");
}

function driverPickedUpFood() {
    const status = document.getElementById("driver-trip-status");
    if (status) {
        status.innerText = "Delivering to Customer...";
        status.style.color = "var(--secondary)";
    }
    const otpBox = document.getElementById("driver-otp-box");
    if (otpBox) otpBox.style.display = "block";
    
    showToast("Rider status updated: Food container picked up.", "success");
}

function driverVerifyOtp() {
    const otp = document.getElementById("driver-verification-otp-input").value.trim();
    if (otp !== generatedDeliveryOtp) {
        showToast("Verification code does not match customer's active token.", "error");
        return;
    }

    const status = document.getElementById("driver-trip-status");
    if (status) {
        status.innerText = "Completed & Transferred";
        status.style.color = "var(--neon-green)";
    }

    // Add payout amount to wallet
    const balanceDisplay = document.getElementById("driver-wallet-balance");
    if (balanceDisplay) {
        balanceDisplay.innerText = "$433.00";
    }

    showToast("OTP verified successfully. Payout added to rider wallet ledger.", "success");
}

let orderSuccessPollingTimer = null;
let liveTrackingPollingTimer = null;
let lastNotifiedStatusMap = {};

function showOrderSuccessPage(orderId) {
    if (orderSuccessPollingTimer) clearInterval(orderSuccessPollingTimer);
    if (liveTrackingPollingTimer) clearInterval(liveTrackingPollingTimer);

    fetch(`/api/orders/${orderId}`)
    .then(res => {
        if (!res.ok) throw new Error("Order not found");
        return res.json();
    })
    .then(order => {
        // Populate UI details
        document.getElementById("success-amount-paid").innerText = `$${order.amount.toFixed(2)} Paid Successfully`;
        document.getElementById("success-order-id").innerText = order.orderId;
        document.getElementById("success-payment-id").innerText = order.paymentId;
        document.getElementById("success-transaction-id").innerText = order.transactionId;
        document.getElementById("success-restaurant-name").innerText = order.restaurantName;
        document.getElementById("success-eta").innerText = order.eta;
        document.getElementById("success-address").innerText = order.address;
        document.getElementById("success-payment-method").innerText = order.paymentMethod;

        updateSuccessTimeline(order.currentStatus);

        // Bind button actions
        document.getElementById("btn-success-track").onclick = () => {
            navigateToPath(`/customer/track/${orderId}`);
        };
        document.getElementById("btn-success-invoice").onclick = () => {
            downloadInvoice(order);
        };
        document.getElementById("btn-success-view-order").onclick = () => {
            showToast(`Order items detail: ${order.items}`, "info");
        };
        document.getElementById("btn-success-home").onclick = () => {
            navigateToPath("/customer/dashboard");
        };

        // Start status polling
        orderSuccessPollingTimer = setInterval(() => {
            fetch(`/api/orders/${orderId}/status`)
            .then(res => res.json())
            .then(data => {
                updateSuccessTimeline(data.status);
                triggerStatusNotification(orderId, data.status);
            });
        }, 5000);
    })
    .catch(err => {
        showToast("Error loading success context: " + err.message, "error");
    });
}

function triggerStatusNotification(orderId, status) {
    const key = `${orderId}_${status}`;
    if (lastNotifiedStatusMap[key]) return;

    lastNotifiedStatusMap[key] = true;
    let label = "Status Update";
    let desc = `Order status is now ${status}.`;

    if (status === "PAYMENT_SUCCESS") {
        label = "Payment Successful";
        desc = "Your wallet was debited and payment was registered.";
    } else if (status === "ORDER_CONFIRMED") {
        label = "Order Confirmed";
        desc = "Your order has been confirmed by the platform.";
    } else if (status === "RESTAURANT_ACCEPTED") {
        label = "Restaurant Confirmed";
        desc = "Bella Napoli Pizza approved your food transaction.";
    } else if (status === "PREPARING") {
        label = "Preparing Food";
        desc = "Chef started preparing your items under sanitation guidelines.";
    } else if (status === "READY_FOR_PICKUP") {
        label = "Packed & Ready";
        desc = "Your meal is hot, packed, and awaiting courier pickup.";
    } else if (status === "DRIVER_ASSIGNED") {
        label = "Rider Matched";
        desc = "Courier Rahul Sharma is assigned to pick up your package.";
    } else if (status === "PICKED_UP") {
        label = "Food Dispatched";
        desc = "Rider picked up your food and is heading to your neighborhood.";
    } else if (status === "OUT_FOR_DELIVERY") {
        label = "Out for Delivery";
        desc = "Rider is nearby. Please get your Delivery OTP ready.";
    } else if (status === "DELIVERED") {
        label = "Delivered";
        desc = "Food delivered successfully! Rate your feedback.";
    }

    showToast(`🔔 ${label}: ${desc}`, "success");
}

function updateSuccessTimeline(status) {
    const statusLevels = [
        "PAYMENT_SUCCESS",
        "ORDER_CONFIRMED",
        "RESTAURANT_ACCEPTED",
        "PREPARING",
        "READY_FOR_PICKUP",
        "DRIVER_ASSIGNED",
        "PICKED_UP",
        "OUT_FOR_DELIVERY",
        "DELIVERED"
    ];

    const currentLevelIdx = statusLevels.indexOf(status);

    const nodes = [
        { id: "snode-payment", minLevelIdx: 0, label: "Payment Completed ✓" },
        { id: "snode-confirmed", minLevelIdx: 1, label: "Waiting Restaurant Confirmation ⏳" },
        { id: "snode-preparing", minLevelIdx: 3, label: "Preparing Food" },
        { id: "snode-assigned", minLevelIdx: 5, label: "Driver Assigned" },
        { id: "snode-route", minLevelIdx: 7, label: "Out for Delivery" },
        { id: "snode-delivered", minLevelIdx: 8, label: "Delivered" }
    ];

    nodes.forEach(n => {
        const el = document.getElementById(n.id);
        if (!el) return;

        const bullet = el.querySelector(".snode-bullet");
        const span = el.querySelector("span");

        if (currentLevelIdx >= n.minLevelIdx) {
            el.classList.add("completed");
            if (bullet) {
                bullet.style.background = "var(--secondary)";
                bullet.style.borderColor = "var(--secondary)";
                bullet.innerHTML = '<i class="fa-solid fa-check" style="font-size: 8px; color: var(--bg); display: block; text-align: center; line-height: 10px;"></i>';
            }
            if (span) {
                span.style.color = "var(--text-main)";
                span.innerText = n.label;
            }
        } else {
            el.classList.remove("completed");
            if (bullet) {
                bullet.style.background = "var(--border)";
                bullet.style.borderColor = "var(--border)";
                bullet.innerHTML = '';
            }
            if (span) {
                span.style.color = "var(--text-muted)";
            }
        }
    });

    // Fill line indicator
    const fillLine = document.getElementById("success-timeline-line");
    if (fillLine) {
        let percent = 0;
        if (currentLevelIdx >= 8) percent = 100;
        else if (currentLevelIdx >= 7) percent = 80;
        else if (currentLevelIdx >= 5) percent = 60;
        else if (currentLevelIdx >= 3) percent = 40;
        else if (currentLevelIdx >= 1) percent = 20;
        else if (currentLevelIdx >= 0) percent = 5;
        fillLine.style.background = `linear-gradient(to bottom, var(--secondary) ${percent}%, var(--border) ${percent}%)`;
    }
}

function showLiveTrackingPage(orderId) {
    if (orderSuccessPollingTimer) clearInterval(orderSuccessPollingTimer);
    if (liveTrackingPollingTimer) clearInterval(liveTrackingPollingTimer);

    // Track active order transaction ID
    currentActiveTransactionId = orderId;

    fetch(`/api/orders/${orderId}`)
    .then(res => {
        if (!res.ok) throw new Error("Order not found");
        return res.json();
    })
    .then(order => {
        // Initialize Map
        setTimeout(initLeafletMap, 200);

        // Update tracking status
        updateLiveTrackingTimeline(order.currentStatus);

        // Map order parameters
        const trackingOtp = document.getElementById("tracking-delivery-otp");
        if (trackingOtp) trackingOtp.innerText = generatedDeliveryOtp;

        const customerOtpInput = document.getElementById("delivery-verification-otp");
        if (customerOtpInput) {
            customerOtpInput.value = generatedDeliveryOtp;
        }

        // Start status polling
        liveTrackingPollingTimer = setInterval(() => {
            fetch(`/api/orders/${orderId}/status`)
            .then(res => res.json())
            .then(data => {
                updateLiveTrackingTimeline(data.status);
                triggerStatusNotification(orderId, data.status);
            });
        }, 5000);
    })
    .catch(err => {
        showToast("Error loading live tracking context: " + err.message, "error");
    });
}

function updateLiveTrackingTimeline(status) {
    const fillBar = document.getElementById("saga-timeline-fill-bar");
    if (!fillBar) return;

    const statusLevels = [
        "PAYMENT_SUCCESS",
        "ORDER_CONFIRMED",
        "RESTAURANT_ACCEPTED",
        "PREPARING",
        "READY_FOR_PICKUP",
        "DRIVER_ASSIGNED",
        "PICKED_UP",
        "OUT_FOR_DELIVERY",
        "DELIVERED"
    ];

    const currentLevelIdx = statusLevels.indexOf(status);

    const stepMapping = [
        { id: "step-accepted", minLevelIdx: 1 },
        { id: "step-preparing", minLevelIdx: 3 },
        { id: "step-assigned", minLevelIdx: 5 },
        { id: "step-arrived", minLevelIdx: 5 },
        { id: "step-picked-up", minLevelIdx: 6 },
        { id: "step-en-route", minLevelIdx: 7 },
        { id: "step-near-you", minLevelIdx: 7 },
        { id: "step-delivered", minLevelIdx: 8 }
    ];

    stepMapping.forEach(step => {
        const node = document.getElementById(step.id);
        if (!node) return;

        if (currentLevelIdx > step.minLevelIdx) {
            node.className = "stepper-node completed";
            const icon = node.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-check"></i>';
        } else if (currentLevelIdx === step.minLevelIdx) {
            node.className = "stepper-node active";
            const icon = node.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i>';
        } else {
            node.className = "stepper-node";
            const icon = node.querySelector(".node-bullet");
            if (icon) icon.innerHTML = '';
        }
    });

    // Fill percent
    let percent = 0;
    if (currentLevelIdx >= 8) {
        percent = 100;
        // Show rating prompt if status is DELIVERED and not yet rated
        const postModal = document.getElementById("post-delivery-modal");
        if (postModal && postModal.style.display !== "flex") {
            postModal.style.display = "flex";
            if (liveTrackingPollingTimer) clearInterval(liveTrackingPollingTimer);
        }
    }
    else if (currentLevelIdx >= 7) percent = 80;
    else if (currentLevelIdx >= 6) percent = 65;
    else if (currentLevelIdx >= 5) percent = 50;
    else if (currentLevelIdx >= 3) percent = 33;
    else if (currentLevelIdx >= 1) percent = 15;
    fillBar.style.height = `${percent}%`;

    // Toggle driver details block
    const driverTrip = document.getElementById("driver-active-trip");
    if (driverTrip) {
        if (currentLevelIdx >= 5) {
            driverTrip.style.display = "block";
            const statusLabel = document.getElementById("driver-trip-status");
            if (statusLabel) {
                if (currentLevelIdx === 5) statusLabel.innerText = "Courier Sharma is arriving at restaurant";
                else if (currentLevelIdx === 6) statusLabel.innerText = "Food container picked up";
                else if (currentLevelIdx === 7) statusLabel.innerText = "Out for Delivery (Near You)";
                else statusLabel.innerText = "Completed";
            }
        } else {
            driverTrip.style.display = "none";
        }
    }

    // Toggle client OTP validation block
    const valBox = document.getElementById("otp-validation-box");
    if (valBox) {
        valBox.style.display = (currentLevelIdx === 7) ? "flex" : "none";
    }
}

function downloadInvoice(order) {
    let invoiceContent = `DELIVO TRANSACTION INVOICE\n`;
    invoiceContent += `==========================================\n`;
    invoiceContent += `Order ID: ${order.orderId}\n`;
    invoiceContent += `Payment ID: ${order.paymentId}\n`;
    invoiceContent += `Transaction ID: ${order.transactionId}\n`;
    invoiceContent += `Restaurant Name: ${order.restaurantName}\n`;
    invoiceContent += `Total Amount Paid: $${order.amount.toFixed(2)}\n`;
    invoiceContent += `Estimated Delivery Time: ${order.eta}\n`;
    invoiceContent += `Payment Method: ${order.paymentMethod}\n`;
    invoiceContent += `Delivery Address: ${order.address}\n`;
    invoiceContent += `Items Purchased: ${order.items}\n`;
    invoiceContent += `==========================================\n`;
    invoiceContent += `Date of Invoice: ${new Date().toLocaleString()}\n`;
    invoiceContent += `Thank you for ordering with Delivo! Enjoy your meal.\n`;

    const blob = new Blob([invoiceContent], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.setAttribute('href', url);
    a.setAttribute('download', `delivo_invoice_${order.orderId}.txt`);
    a.click();
    showToast("Invoice downloaded successfully!", "success");
}
