
const express = require("express");
const mongoose = require("mongoose");
const Notification = require("../models/Notification");
const { protect, adminOnly } = require("../middleware/authMiddleware");

const router = express.Router();

router.use(protect);

// GET MY NOTIFICATIONS
router.get("/", async (req, res) => {
    try {
        const notifications = await Notification.find({
            recipient: req.user._id
        }).sort({ createdAt: -1 });

        return res.json({
            status: "success",
            count: notifications.length,
            unreadCount: notifications.filter(n => !n.isRead).length,
            notifications
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to retrieve notifications."
        });
    }
});

// CREATE NOTIFICATION - ADMIN ONLY
router.post("/", adminOnly, async (req, res) => {
    try {
        const { recipient, title, message, category } = req.body;

        if (!recipient || !title || !message) {
            return res.status(400).json({
                status: "error",
                message: "Recipient, title and message are required."
            });
        }

        if (!mongoose.Types.ObjectId.isValid(recipient)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid recipient ID."
            });
        }

        const notification = await Notification.create({
            recipient,
            title,
            message,
            category
        });

        return res.status(201).json({
            status: "success",
            message: "Notification created successfully.",
            notification
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

// MARK NOTIFICATION AS READ
router.patch("/:id/read", async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid notification ID."
            });
        }

        const notification = await Notification.findOneAndUpdate(
            {
                _id: req.params.id,
                recipient: req.user._id
            },
            { isRead: true },
            { new: true }
        );

        if (!notification) {
            return res.status(404).json({
                status: "error",
                message: "Notification not found."
            });
        }

        return res.json({
            status: "success",
            message: "Notification marked as read.",
            notification
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to update notification."
        });
    }
});

// DELETE MY NOTIFICATION
router.delete("/:id", async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid notification ID."
            });
        }

        const notification = await Notification.findOneAndDelete({
            _id: req.params.id,
            recipient: req.user._id
        });

        if (!notification) {
            return res.status(404).json({
                status: "error",
                message: "Notification not found."
            });
        }

        return res.json({
            status: "success",
            message: "Notification deleted successfully."
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to delete notification."
        });
    }
});

module.exports = router;
