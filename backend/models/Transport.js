
const mongoose = require("mongoose");

const transportSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    transportType: {
        type: String,
        enum: ["Shuttle", "Bus", "Taxi", "Intercity"],
        required: true
    },
    origin: String,
    destination: String,
    schedule: String,
    fare: String,
    contact: String,
    website: String,
    isActive: {
        type: Boolean,
        default: true
    }
}, { timestamps: true });

module.exports = mongoose.model("Transport", transportSchema);
