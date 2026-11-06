
const mongoose = require("mongoose");

const announcementSchema = new mongoose.Schema({
    title: {
        type: String,
        required: true,
        trim: true
    },
    message: {
        type: String,
        required: true
    },
    category: {
        type: String,
        enum: ["Academic", "Campus", "Urgent"],
        default: "Campus"
    },
    priority: {
        type: String,
        enum: ["Normal", "High", "Urgent"],
        default: "Normal"
    },
    pinned: {
        type: Boolean,
        default: false
    },
    publishedBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        required: true
    }
}, { timestamps: true });

module.exports = mongoose.model("Announcement", announcementSchema);
