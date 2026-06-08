const crypto = require("crypto");
const fs = require("fs");
const path = require("path");
const cors = require("cors");
const dotenv = require("dotenv");
const express = require("express");
const morgan = require("morgan");

dotenv.config();

const app = express();
const port = Number(process.env.PORT || 8082);
const dataDir = path.join(__dirname, "..", "data");
const dbFile = path.join(dataDir, "db.json");

const products = [
  {
    id: "satin-evening-dress",
    name: "Satin Evening Dress",
    category: "Women",
    tag: "Elegant",
    price: 95000,
    currency: "TZS",
  },
  {
    id: "smart-linen-shirt",
    name: "Smart Linen Shirt",
    category: "Men",
    tag: "Daily wear",
    price: 52000,
    currency: "TZS",
  },
  {
    id: "premium-denim-jacket",
    name: "Premium Denim Jacket",
    category: "Men",
    tag: "Best seller",
    price: 78000,
    currency: "TZS",
  },
  {
    id: "leather-crossbody-bag",
    name: "Leather Crossbody Bag",
    category: "Accessories",
    tag: "Accessory",
    price: 68000,
    currency: "TZS",
  },
];

function allowedOrigins() {
  return (process.env.CORS_ORIGIN || "")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean);
}

app.use(
  cors({
    origin(origin, callback) {
      const origins = allowedOrigins();
      if (!origin || origins.length === 0 || origins.includes(origin)) {
        callback(null, true);
        return;
      }
      callback(new Error(`Origin ${origin} is not allowed by CORS`));
    },
    credentials: true,
  })
);
app.use(express.json({ limit: "1mb" }));
app.use(express.urlencoded({ extended: true }));
app.use(morgan("dev"));

function initialDb() {
  return {
    users: [],
    orders: [],
    contacts: [],
  };
}

function ensureDb() {
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
  }

  if (!fs.existsSync(dbFile)) {
    fs.writeFileSync(dbFile, JSON.stringify(initialDb(), null, 2));
  }
}

function readDb() {
  ensureDb();
  return JSON.parse(fs.readFileSync(dbFile, "utf8"));
}

function writeDb(db) {
  ensureDb();
  fs.writeFileSync(dbFile, JSON.stringify(db, null, 2));
}

function now() {
  return new Date().toISOString();
}

function requiredFields(body, fields) {
  return fields.filter((field) => !String(body[field] || "").trim());
}

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function hashPassword(password) {
  return crypto.createHash("sha256").update(String(password)).digest("hex");
}

function publicUser(user) {
  return {
    id: user.id,
    name: user.name,
    email: user.email,
    createdAt: user.createdAt,
  };
}

app.get("/", (_req, res) => {
  res.json({
    name: "FK Collection API",
    status: "running",
    baseUrl: "/api",
  });
});

app.get("/api/health", (_req, res) => {
  res.json({
    status: "ok",
    service: "fadhil-backend",
    timestamp: now(),
  });
});

app.get("/api/products", (_req, res) => {
  res.json({
    success: true,
    data: products,
  });
});

app.post("/api/auth/register", (req, res) => {
  const missing = requiredFields(req.body, ["name", "email", "password"]);
  if (missing.length) {
    return res.status(400).json({
      success: false,
      message: `Missing required fields: ${missing.join(", ")}`,
    });
  }

  const db = readDb();
  const email = normalizeEmail(req.body.email);
  const existingUser = db.users.find((user) => user.email === email);

  if (existingUser) {
    return res.status(409).json({
      success: false,
      message: "A user with this email already exists.",
    });
  }

  const user = {
    id: crypto.randomUUID(),
    name: String(req.body.name).trim(),
    email,
    passwordHash: hashPassword(req.body.password),
    createdAt: now(),
  };

  db.users.push(user);
  writeDb(db);

  res.status(201).json({
    success: true,
    message: "User registered successfully.",
    data: publicUser(user),
  });
});

app.post("/api/auth/login", (req, res) => {
  const missing = requiredFields(req.body, ["email", "password"]);
  if (missing.length) {
    return res.status(400).json({
      success: false,
      message: `Missing required fields: ${missing.join(", ")}`,
    });
  }

  const db = readDb();
  const email = normalizeEmail(req.body.email);
  const user = db.users.find((item) => item.email === email);

  if (!user || user.passwordHash !== hashPassword(req.body.password)) {
    return res.status(401).json({
      success: false,
      message: "Invalid email or password.",
    });
  }

  res.json({
    success: true,
    message: "Logged in successfully.",
    data: {
      user: publicUser(user),
      token: crypto.randomUUID(),
    },
  });
});

app.post("/api/orders", (req, res) => {
  const body = req.body || {};
  const fullName = body.fullName || body.name;
  const productRequest = body.productRequest || body.product || body.request;
  const missing = [];

  if (!String(fullName || "").trim()) missing.push("fullName");
  if (!String(productRequest || "").trim()) missing.push("productRequest");

  if (missing.length) {
    return res.status(400).json({
      success: false,
      message: `Missing required fields: ${missing.join(", ")}`,
    });
  }

  const db = readDb();
  const order = {
    id: crypto.randomUUID(),
    fullName: String(fullName).trim(),
    phoneOrEmail: String(body.phoneOrEmail || body.contact || "").trim(),
    productRequest: String(productRequest).trim(),
    size: String(body.size || "").trim(),
    delivery: String(body.delivery || "Pickup").trim(),
    details: String(body.details || "").trim(),
    status: "new",
    createdAt: now(),
  };

  db.orders.unshift(order);
  writeDb(db);

  res.status(201).json({
    success: true,
    message: "Order request received.",
    data: order,
  });
});

app.get("/api/orders", (_req, res) => {
  const db = readDb();
  res.json({
    success: true,
    data: db.orders,
  });
});

app.post("/api/contact", (req, res) => {
  const missing = requiredFields(req.body, ["fullName", "phoneOrEmail", "message"]);
  if (missing.length) {
    return res.status(400).json({
      success: false,
      message: `Missing required fields: ${missing.join(", ")}`,
    });
  }

  const db = readDb();
  const contact = {
    id: crypto.randomUUID(),
    fullName: String(req.body.fullName).trim(),
    phoneOrEmail: String(req.body.phoneOrEmail).trim(),
    topic: String(req.body.topic || "Order an item").trim(),
    message: String(req.body.message).trim(),
    status: "new",
    createdAt: now(),
  };

  db.contacts.unshift(contact);
  writeDb(db);

  res.status(201).json({
    success: true,
    message: "Message received.",
    data: contact,
  });
});

app.get("/api/contact", (_req, res) => {
  const db = readDb();
  res.json({
    success: true,
    data: db.contacts,
  });
});

app.use((err, _req, res, _next) => {
  res.status(500).json({
    success: false,
    message: err.message || "Server error",
  });
});

app.use((_req, res) => {
  res.status(404).json({
    success: false,
    message: "Endpoint not found",
  });
});

app.listen(port, () => {
  console.log(`FK Collection API running on http://localhost:${port}`);
});
