
const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");

const userSchema = new mongoose.Schema(
    {
        fullName: {
            type: String,
            required: [true, "Full name is required"],
            trim: true,
            minlength: 2,
            maxlength: 100
        },

        email: {
            type: String,
            required: [true, "Email address is required"],
            unique: true,
            lowercase: true,
            trim: true,
            match: [/^[^\s@]+@[^\s@]+\.[^\s@]+$/, "Enter a valid email address"]
        },

        studentNumber: {
            type: String,
            trim: true,
            sparse: true,
            unique: true
        },

        // Local accounts use a password.
        // Google accounts authenticate through Firebase instead.
        password: {
            type: String,
            required: false,
            minlength: 8,
            select: false
        },

        // Allows an account to support both password and Google login.
        authProviders: {
            type: [{
                type: String,
                enum: ["password", "google"]
            }],
            default: ["password"]
        },

        // Firebase UID for Google-authenticated accounts.
        firebaseUid: {
            type: String,
            unique: true,
            sparse: true
        },

        role: {
            type: String,
            enum: ["student", "admin"],
            default: "student"
        },

        isActive: {
            type: Boolean,
            default: true
        }
    },
    {
        timestamps: true
    }
);

// Hash a password only when a local password is supplied or changed.
userSchema.pre("save", async function () {
    if (!this.isModified("password") || !this.password) {
        return;
    }

    this.password = await bcrypt.hash(this.password, 12);
});

// Compare a password safely. Google-only accounts have no password.
userSchema.methods.comparePassword = async function (enteredPassword) {
    if (!this.password || !enteredPassword) {
        return false;
    }

    return bcrypt.compare(enteredPassword, this.password);
};

module.exports = mongoose.model("User", userSchema);
