
const mongoose = require("mongoose");

const campusServiceSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    description: String,
    category: {
        type: String,
        enum: ["Academic", "Student", "Library", "Financial", "Computer"],
        required: true
    },
    location: String,
    contact: String,
    website: String,
    icon: String,
    isActive: {
        type: Boolean,
        default: true
    }
}, { timestamps: true });

module.exports = mongoose.model("CampusService", campusServiceSchema);
