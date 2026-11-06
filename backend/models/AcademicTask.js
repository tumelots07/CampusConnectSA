
const mongoose = require("mongoose");

const academicTaskSchema = new mongoose.Schema(
    {
        student: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true
        },

        title: {
            type: String,
            required: [true, "Task title is required"],
            trim: true,
            minlength: 2,
            maxlength: 150
        },

        taskType: {
            type: String,
            enum: ["Assignment", "Test", "Examination", "Project", "Other"],
            required: true
        },

        module: {
            type: String,
            trim: true,
            maxlength: 100,
            default: ""
        },

        priority: {
            type: String,
            enum: ["Low", "Medium", "High"],
            default: "Medium"
        },

        dueDate: {
            type: Date,
            required: [true, "Due date is required"]
        },

        completed: {
            type: Boolean,
            default: false
        }
    },
    {
        timestamps: true
    }
);

module.exports = mongoose.model("AcademicTask", academicTaskSchema);
