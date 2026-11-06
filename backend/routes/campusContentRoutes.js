
const express = require("express");
const mongoose = require("mongoose");

const Resource = require("../models/Resource");
const CampusService = require("../models/CampusService");
const Transport = require("../models/Transport");
const Announcement = require("../models/Announcement");

const { protect, adminOnly } = require("../middleware/authMiddleware");

const router = express.Router();

router.use(protect);

// Model configuration for each content type.
const contentTypes = {
    resources: Resource,
    services: CampusService,
    transport: Transport,
    announcements: Announcement
};

function getModel(type) {
    return contentTypes[type];
}

// GET CONTENT
router.get("/:type", async (req, res) => {
    try {
        const Model = getModel(req.params.type);

        if (!Model) {
            return res.status(404).json({
                status: "error",
                message: "Content type not found."
            });
        }

        const filter = req.params.type === "announcements"
            ? {}
            : { isActive: true };

        const items = await Model.find(filter).sort({ createdAt: -1 });

        return res.json({
            status: "success",
            count: items.length,
            items
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to retrieve content."
        });
    }
});

// CREATE CONTENT - ADMIN ONLY
router.post("/:type", adminOnly, async (req, res) => {
    try {
        const Model = getModel(req.params.type);

        if (!Model) {
            return res.status(404).json({
                status: "error",
                message: "Content type not found."
            });
        }

        const data = { ...req.body };

        // Never allow a client to impersonate an announcement publisher.
        if (req.params.type === "announcements") {
            data.publishedBy = req.user._id;
        }

        const item = await Model.create(data);

        return res.status(201).json({
            status: "success",
            message: "Content created successfully.",
            item
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

// UPDATE CONTENT - ADMIN ONLY
router.put("/:type/:id", adminOnly, async (req, res) => {
    try {
        const Model = getModel(req.params.type);

        if (!Model) {
            return res.status(404).json({
                status: "error",
                message: "Content type not found."
            });
        }

        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid content ID."
            });
        }

        const updates = { ...req.body };

        // Prevent changing the original announcement publisher.
        delete updates.publishedBy;

        const item = await Model.findByIdAndUpdate(
            req.params.id,
            updates,
            { new: true, runValidators: true }
        );

        if (!item) {
            return res.status(404).json({
                status: "error",
                message: "Content not found."
            });
        }

        return res.json({
            status: "success",
            message: "Content updated successfully.",
            item
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

// DELETE CONTENT - ADMIN ONLY
router.delete("/:type/:id", adminOnly, async (req, res) => {
    try {
        const Model = getModel(req.params.type);

        if (!Model) {
            return res.status(404).json({
                status: "error",
                message: "Content type not found."
            });
        }

        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid content ID."
            });
        }

        const item = await Model.findByIdAndDelete(req.params.id);

        if (!item) {
            return res.status(404).json({
                status: "error",
                message: "Content not found."
            });
        }

        return res.json({
            status: "success",
            message: "Content deleted successfully."
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to delete content."
        });
    }
});

module.exports = router;
