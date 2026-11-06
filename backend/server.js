require("dotenv").config();

const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const authRoutes = require("./routes/authRoutes");
const academicTaskRoutes = require("./routes/academicTaskRoutes");
const campusContentRoutes = require("./routes/campusContentRoutes");
const notificationRoutes = require("./routes/notificationRoutes");
const incidentReportRoutes = require("./routes/incidentReportRoutes");

const app = express();

app.use(cors());
app.use(express.json());
app.use("/api/auth", authRoutes);
app.use("/api/tasks", academicTaskRoutes);
app.use("/api/content", campusContentRoutes);
app.use("/api/notifications", notificationRoutes);
app.use("/api/incidents", incidentReportRoutes);

app.get("/", (req, res) => {
    res.json({
        message: "CampusConnect SA API is running!"
    });
});

app.get("/api/health", (req, res) => {
    res.json({
        status: "success",
        database: mongoose.connection.readyState === 1
            ? "Connected"
            : "Disconnected"
    });
});

const PORT = process.env.PORT || 5000;

mongoose.connect(process.env.MONGODB_URI)
    .then(() => {
        console.log("MongoDB connected successfully!");

        app.listen(PORT, "0.0.0.0", () => {
            console.log(`Server running on port ${PORT}`);
        });
    })
    .catch((error) => {
        console.error("MongoDB connection failed:", error.message);
        process.exit(1);
    });