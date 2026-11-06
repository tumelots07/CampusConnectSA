
const jwt = require("jsonwebtoken");
const User = require("../models/User");

const protect = async (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader || !authHeader.startsWith("Bearer ")) {
            return res.status(401).json({
                status: "error",
                message: "Authentication required. Please log in."
            });
        }

        const token = authHeader.split(" ")[1];

        const decoded = jwt.verify(token, process.env.JWT_SECRET);

        const user = await User.findById(decoded.id);

        if (!user || !user.isActive) {
            return res.status(401).json({
                status: "error",
                message: "Account not found or inactive."
            });
        }

        req.user = user;
        next();

    } catch (error) {
        return res.status(401).json({
            status: "error",
            message: "Invalid or expired authentication token."
        });
    }
};

// Restrict certain routes to administrators.
const adminOnly = (req, res, next) => {
    if (!req.user || req.user.role !== "admin") {
        return res.status(403).json({
            status: "error",
            message: "Administrator access required."
        });
    }

    next();
};

module.exports = { protect, adminOnly };
