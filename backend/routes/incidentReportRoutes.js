
const express = require("express");
const mongoose = require("mongoose");

const IncidentReport = require("../models/IncidentReport");
const { protect, adminOnly } = require("../middleware/authMiddleware");

const router = express.Router();

router.use(protect);

// STUDENT: SUBMIT AN INCIDENT REPORT
router.post("/", async (req, res) => {
    try {
        const { incidentType, location, description } = req.body;

        if (!incidentType || !location || !description) {
            return res.status(400).json({
                status: "error",
                message: "Incident type, location and description are required."
            });
        }

        const report = await IncidentReport.create({
            student: req.user._id,
            incidentType,
            location,
            description
        });

        return res.status(201).json({
            status: "success",
            message: "Incident report submitted successfully.",
            report
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

// STUDENT: VIEW OWN REPORTS
router.get("/my-reports", async (req, res) => {
    try {
        const reports = await IncidentReport.find({
            student: req.user._id
        }).sort({ createdAt: -1 });

        return res.json({
            status: "success",
            count: reports.length,
            reports
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to retrieve your incident reports."
        });
    }
});

// ADMIN: VIEW ALL REPORTS
router.get("/", adminOnly, async (req, res) => {
    try {
        const reports = await IncidentReport.find()
            .populate("student", "fullName email studentNumber")
            .sort({ createdAt: -1 });

        return res.json({
            status: "success",
            count: reports.length,
            reports
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to retrieve incident reports."
        });
    }
});

// ADMIN: UPDATE REPORT STATUS AND NOTES
router.patch("/:id", adminOnly, async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid incident report ID."
            });
        }

        const updates = {};
        const allowedStatuses = [
            "Submitted",
            "Under Review",
            "Resolved",
            "Closed"
        ];

        if (req.body.status !== undefined) {
            if (!allowedStatuses.includes(req.body.status)) {
                return res.status(400).json({
                    status: "error",
                    message: "Invalid incident status."
                });
            }

            updates.status = req.body.status;
        }

        if (req.body.adminNotes !== undefined) {
            updates.adminNotes = req.body.adminNotes;
        }

        const report = await IncidentReport.findByIdAndUpdate(
            req.params.id,
            updates,
            { new: true, runValidators: true }
        );

        if (!report) {
            return res.status(404).json({
                status: "error",
                message: "Incident report not found."
            });
        }

        return res.json({
            status: "success",
            message: "Incident report updated successfully.",
            report
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

module.exports = router;
