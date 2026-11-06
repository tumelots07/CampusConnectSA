
const mongoose = require("mongoose");

const incidentReportSchema = new mongoose.Schema(
    {
        student: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true
        },

        incidentType: {
            type: String,
            enum: [
                "Security",
                "Medical",
                "Harassment",
                "Theft",
                "Accident",
                "Other"
            ],
            required: true
        },

        location: {
            type: String,
            required: true,
            trim: true,
            maxlength: 200
        },

        description: {
            type: String,
            required: true,
            trim: true,
            minlength: 10,
            maxlength: 2000
        },

        status: {
            type: String,
            enum: ["Submitted", "Under Review", "Resolved", "Closed"],
            default: "Submitted"
        },

        adminNotes: {
            type: String,
            default: ""
        }
    },
    {
        timestamps: true
    }
);

module.exports = mongoose.model("IncidentReport", incidentReportSchema);
