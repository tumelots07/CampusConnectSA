
const express = require("express");
const mongoose = require("mongoose");
const AcademicTask = require("../models/AcademicTask");
const { protect } = require("../middleware/authMiddleware");

const router = express.Router();

// All Academic Planner routes require authentication.
router.use(protect);

// CREATE TASK
router.post("/", async (req, res) => {
    try {
        const { title, taskType, module, priority, dueDate } = req.body;

        if (!title || !taskType || !dueDate) {
            return res.status(400).json({
                status: "error",
                message: "Title, task type and due date are required."
            });
        }

        const task = await AcademicTask.create({
            student: req.user._id,
            title,
            taskType,
            module,
            priority,
            dueDate
        });

        return res.status(201).json({
            status: "success",
            message: "Academic task created successfully.",
            task
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

// GET ALL TASKS FOR LOGGED-IN STUDENT
router.get("/", async (req, res) => {
    try {
        const tasks = await AcademicTask.find({
            student: req.user._id
        }).sort({ dueDate: 1 });

        return res.status(200).json({
            status: "success",
            count: tasks.length,
            tasks
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to retrieve academic tasks."
        });
    }
});

// GET TASK SUMMARY
router.get("/summary", async (req, res) => {
    try {
        const tasks = await AcademicTask.find({
            student: req.user._id
        });

        const total = tasks.length;
        const completed = tasks.filter(task => task.completed).length;
        const pending = total - completed;

        const progress = total === 0
            ? 0
            : Math.round((completed / total) * 100);

        return res.status(200).json({
            status: "success",
            total,
            completed,
            pending,
            progress
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to calculate task summary."
        });
    }
});

// UPDATE TASK
router.put("/:id", async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid task ID."
            });
        }

        const allowedFields = [
            "title",
            "taskType",
            "module",
            "priority",
            "dueDate",
            "completed"
        ];

        const updates = {};

        for (const field of allowedFields) {
            if (req.body[field] !== undefined) {
                updates[field] = req.body[field];
            }
        }

        const task = await AcademicTask.findOneAndUpdate(
            {
                _id: req.params.id,
                student: req.user._id
            },
            updates,
            {
                new: true,
                runValidators: true
            }
        );

        if (!task) {
            return res.status(404).json({
                status: "error",
                message: "Task not found."
            });
        }

        return res.status(200).json({
            status: "success",
            message: "Academic task updated successfully.",
            task
        });

    } catch (error) {
        return res.status(400).json({
            status: "error",
            message: error.message
        });
    }
});

// DELETE TASK
router.delete("/:id", async (req, res) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({
                status: "error",
                message: "Invalid task ID."
            });
        }

        const task = await AcademicTask.findOneAndDelete({
            _id: req.params.id,
            student: req.user._id
        });

        if (!task) {
            return res.status(404).json({
                status: "error",
                message: "Task not found."
            });
        }

        return res.status(200).json({
            status: "success",
            message: "Academic task deleted successfully."
        });

    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Unable to delete academic task."
        });
    }
});

module.exports = router;
