
const express = require("express");
const jwt = require("jsonwebtoken");
const User = require("../models/User");
const { protect } = require("../middleware/authMiddleware");

const router = express.Router();

// Generate JWT authentication token.
const generateToken = (userId) => {
    return jwt.sign(
        { id: userId },
        process.env.JWT_SECRET,
        { expiresIn: "7d" }
    );
};

// Remove password from returned user details.
const publicUser = (user) => ({
    id: user._id,
    fullName: user.fullName,
    email: user.email,
    studentNumber: user.studentNumber || "",
    role: user.role
});

// STUDENT REGISTRATION
router.post("/register", async (req, res) => {
    try {
        const { fullName, email, studentNumber, password, confirmPassword } = req.body;

        if (!fullName || !email || !password || !confirmPassword) {
            return res.status(400).json({
                status: "error",
                message: "Please complete all required fields."
            });
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            return res.status(400).json({
                status: "error",
                message: "Please enter a valid email address."
            });
        }

        const strongPassword =
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

        if (!strongPassword.test(password)) {
            return res.status(400).json({
                status: "error",
                message: "Password must contain at least 8 characters, including an uppercase letter, lowercase letter, number and special character."
            });
        }

        if (password !== confirmPassword) {
            return res.status(400).json({
                status: "error",
                message: "Passwords do not match."
            });
        }

        const existingEmail = await User.findOne({
            email: email.trim().toLowerCase()
        });

        if (existingEmail) {
            return res.status(409).json({
                status: "error",
                message: "An account with this email already exists."
            });
        }

        if (studentNumber) {
            const existingStudent = await User.findOne({
                studentNumber: studentNumber.trim()
            });

            if (existingStudent) {
                return res.status(409).json({
                    status: "error",
                    message: "This student number is already registered."
                });
            }
        }

        // Public registration always creates a student account.
        const user = await User.create({
            fullName: fullName.trim(),
            email: email.trim().toLowerCase(),
            studentNumber: studentNumber ? studentNumber.trim() : undefined,
            password,
            role: "student"
        });

        const token = generateToken(user._id);

        return res.status(201).json({
            status: "success",
            message: "Student account created successfully.",
            token,
            user: publicUser(user)
        });

    } catch (error) {
        console.error("Registration error:", error.message);

        if (error.code === 11000) {
            return res.status(409).json({
                status: "error",
                message: "This email or student number is already registered."
            });
        }

        return res.status(500).json({
            status: "error",
            message: "Unable to register account."
        });
    }
});

// USER LOGIN
router.post("/login", async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                status: "error",
                message: "Email and password are required."
            });
        }

        const user = await User.findOne({
            email: email.trim().toLowerCase()
        }).select("+password");

        if (!user || !(await user.comparePassword(password))) {
            return res.status(401).json({
                status: "error",
                message: "Incorrect email or password."
            });
        }

        if (!user.isActive) {
            return res.status(403).json({
                status: "error",
                message: "Your account has been deactivated."
            });
        }

        const token = generateToken(user._id);

        return res.status(200).json({
            status: "success",
            message: "Login successful.",
            token,
            user: publicUser(user)
        });

    } catch (error) {
        console.error("Login error:", error.message);

        return res.status(500).json({
            status: "error",
            message: "Unable to log in."
        });
    }
});

// GET CURRENT LOGGED-IN USER
router.get("/me", protect, (req, res) => {
    res.status(200).json({
        status: "success",
        user: publicUser(req.user)
    });
});

// UPDATE CURRENT LOGGED-IN USER PROFILE
router.put("/me", protect, async (req, res) => {
    try {
        const { fullName, email, studentNumber } = req.body;

        if (!fullName || !email) {
            return res.status(400).json({
                status: "error",
                message: "Full name and email are required."
            });
        }

        const cleanName = fullName.trim();
        const cleanEmail = email.trim().toLowerCase();
        const cleanStudentNumber = studentNumber
            ? studentNumber.trim()
            : "";

        if (cleanName.length < 2) {
            return res.status(400).json({
                status: "error",
                message: "Please enter a valid full name."
            });
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail)) {
            return res.status(400).json({
                status: "error",
                message: "Please enter a valid email address."
            });
        }

        // Prevent another user from using this email.
        const existingEmail = await User.findOne({
            email: cleanEmail,
            _id: { $ne: req.user._id }
        });

        if (existingEmail) {
            return res.status(409).json({
                status: "error",
                message: "This email is already being used by another account."
            });
        }

        // Prevent another user from using this student number.
        if (cleanStudentNumber) {
            const existingStudent = await User.findOne({
                studentNumber: cleanStudentNumber,
                _id: { $ne: req.user._id }
            });

            if (existingStudent) {
                return res.status(409).json({
                    status: "error",
                    message: "This student number is already registered."
                });
            }
        }

        req.user.fullName = cleanName;
        req.user.email = cleanEmail;
        req.user.studentNumber = cleanStudentNumber || undefined;

        await req.user.save();

        return res.status(200).json({
            status: "success",
            message: "Profile updated successfully.",
            user: publicUser(req.user)
        });

    } catch (error) {
        console.error("Profile update error:", error.message);

        if (error.code === 11000) {
            return res.status(409).json({
                status: "error",
                message: "The email or student number is already in use."
            });
        }

        return res.status(500).json({
            status: "error",
            message: "Unable to update profile."
        });
    }
});


// GOOGLE LOGIN USING FIREBASE
router.post("/google", async (req, res) => {
    try {
        const { idToken } = req.body;

        // Check that the Android app supplied a token.
        if (!idToken) {
            return res.status(400).json({
                status: "error",
                message: "Firebase ID token is required."
            });
        }

        // Verify the token with Firebase Admin.
        const { verifyFirebaseIdToken } =
            require("../config/firebaseAdmin");

        const decodedToken =
            await verifyFirebaseIdToken(idToken);

        // Only accept verified email addresses.
        if (
            !decodedToken.email ||
            decodedToken.email_verified !== true
        ) {
            return res.status(401).json({
                status: "error",
                message: "A verified Google email address is required."
            });
        }

        const email = decodedToken.email.trim().toLowerCase();
        const firebaseUid = decodedToken.uid;
        const fullName =
            (decodedToken.name || email.split("@")[0]).trim();

        // First look for an account already linked to this Firebase user.
        let user = await User.findOne({ firebaseUid });

        // If not found, look for an existing account with the verified email.
        if (!user) {
            user = await User.findOne({ email });
        }

        if (user) {
            // Google login is only for student accounts.
            if (user.role !== "student") {
                return res.status(403).json({
                    status: "error",
                    message: "Google sign-in is only available for student accounts."
                });
            }

            if (!user.isActive) {
                return res.status(403).json({
                    status: "error",
                    message: "Your account has been deactivated."
                });
            }

            // Do not link a different Google identity to this account.
            if (
                user.firebaseUid &&
                user.firebaseUid !== firebaseUid
            ) {
                return res.status(409).json({
                    status: "error",
                    message: "This account is already linked to another Google identity."
                });
            }

            user.firebaseUid = firebaseUid;

            if (!Array.isArray(user.authProviders)) {
                user.authProviders = [];
            }

            if (!user.authProviders.includes("google")) {
                user.authProviders.push("google");
            }

            await user.save();

        } else {
            // Create a new student account for a first-time Google user.
            user = await User.create({
                fullName: fullName || "CampusConnect Student",
                email,
                firebaseUid,
                authProviders: ["google"],
                role: "student",
                isActive: true
            });
        }

        // Generate the normal CampusConnect backend JWT.
        const token = generateToken(user._id);

        return res.status(200).json({
            status: "success",
            message: "Google login successful.",
            token,
            user: publicUser(user)
        });

    } catch (error) {
        console.error("Google login error:", error.message);

        if (
            error.code === "auth/id-token-expired" ||
            error.code === "auth/argument-error" ||
            error.code === "auth/invalid-id-token"
        ) {
            return res.status(401).json({
                status: "error",
                message: "Your Google authentication token is invalid or expired. Please try again."
            });
        }

        if (error.code === 11000) {
            return res.status(409).json({
                status: "error",
                message: "This Google account is already linked."
            });
        }

        return res.status(500).json({
            status: "error",
            message: "Unable to complete Google login."
        });
    }
});



// SECURE FIRST ADMINISTRATOR SETUP
router.post("/setup-admin", async (req, res) => {
    try {
        const { fullName, email, password, setupKey } = req.body;

        // Verify the private setup key.
        if (!process.env.ADMIN_SETUP_KEY ||
            setupKey !== process.env.ADMIN_SETUP_KEY) {
            return res.status(403).json({
                status: "error",
                message: "Invalid administrator setup key."
            });
        }

        // Only allow setup when no administrator exists.
        const existingAdmin = await User.findOne({ role: "admin" });

        if (existingAdmin) {
            return res.status(403).json({
                status: "error",
                message: "An administrator account already exists."
            });
        }

        const strongPassword =
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

        if (!fullName || !email || !password || !strongPassword.test(password)) {
            return res.status(400).json({
                status: "error",
                message: "Provide a name, valid email and a strong password containing at least 8 characters, uppercase and lowercase letters, a number and a special character."
            });
        }

        const normalizedEmail = email.trim().toLowerCase();

        const existingUser = await User.findOne({
            email: normalizedEmail
        });

        if (existingUser) {
            return res.status(409).json({
                status: "error",
                message: "This email is already registered."
            });
        }

        const admin = await User.create({
            fullName: fullName.trim(),
            email: normalizedEmail,
            password,
            role: "admin"
        });

        return res.status(201).json({
            status: "success",
            message: "First administrator created successfully.",
            user: {
                id: admin._id,
                fullName: admin.fullName,
                email: admin.email,
                role: admin.role
            }
        });

    } catch (error) {
        console.error("Admin setup error:", error.message);

        return res.status(500).json({
            status: "error",
            message: "Unable to create administrator account."
        });
    }
});


// GOOGLE SIGN-IN WITH FIREBASE
router.post("/google", async (req, res) => {
    try {
        const { idToken } = req.body;

        if (!idToken) {
            return res.status(400).json({
                status: "error",
                message: "Firebase ID token is required."
            });
        }

        // Verify the Firebase token using the trusted backend.
        const decodedToken = await require("../config/firebaseAdmin")
            .verifyFirebaseIdToken(idToken);

        // Only accept verified Google email identities.
        if (!decodedToken.email || decodedToken.email_verified !== true) {
            return res.status(401).json({
                status: "error",
                message: "A verified Google email address is required."
            });
        }

        const email = decodedToken.email.trim().toLowerCase();
        const firebaseUid = decodedToken.uid;
        const fullName =
            (decodedToken.name || email.split("@")[0]).trim();

        // Find an existing account by Firebase UID first,
        // then by its verified email address.
        let user = await User.findOne({ firebaseUid });

        if (!user) {
            user = await User.findOne({ email });
        }

        if (user) {
            // Do not allow Google login to an administrator account.
            if (user.role !== "student") {
                return res.status(403).json({
                    status: "error",
                    message: "Google sign-in is only available for student accounts."
                });
            }

            if (!user.isActive) {
                return res.status(403).json({
                    status: "error",
                    message: "Your account has been deactivated."
                });
            }

            // Associate this verified Google identity with the account.
            if (user.firebaseUid && user.firebaseUid !== firebaseUid) {
                return res.status(409).json({
                    status: "error",
                    message: "This account is already linked to another Google identity."
                });
            }

            user.firebaseUid = firebaseUid;

            if (!user.authProviders.includes("google")) {
                user.authProviders.push("google");
            }

            await user.save();

        } else {
            // Create a new student account without a local password.
            user = await User.create({
                fullName: fullName || "CampusConnect Student",
                email,
                firebaseUid,
                authProviders: ["google"],
                role: "student",
                isActive: true
            });
        }

        // Issue the existing CampusConnect JWT.
        const token = generateToken(user._id);

        return res.status(200).json({
            status: "success",
            message: "Google login successful.",
            token,
            user: publicUser(user)
        });

    } catch (error) {
        console.error("Google login error:", error.message);

        if (error.code === "auth/id-token-expired") {
            return res.status(401).json({
                status: "error",
                message: "Google sign-in has expired. Please try again."
            });
        }

        if (
            error.code === "auth/argument-error" ||
            error.code === "auth/invalid-id-token"
        ) {
            return res.status(401).json({
                status: "error",
                message: "Invalid Google authentication token."
            });
        }

        if (error.code === 11000) {
            return res.status(409).json({
                status: "error",
                message: "This Google account is already linked."
            });
        }

        return res.status(500).json({
            status: "error",
            message: "Unable to complete Google login."
        });
    }
});


module.exports = router;
