const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

/* ---------- EMAIL CONFIG ---------- */
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "kambleaayush136@gmail.com",
    pass: "AK@kamble03", // ← change this
  },
});

/* ---------- FIRESTORE TRIGGER ---------- */
exports.sendTrainTicketEmail = functions.firestore
  .document("bookings/{bookingId}")
  .onCreate(async (snap) => {

    const booking = snap.data();

    // ✅ correct field
    if (!booking || booking.mode !== "TRAIN") return null;

    const userDoc = await admin
      .firestore()
      .collection("users")
      .doc(booking.userId)
      .get();

    if (!userDoc.exists) return null;

    const user = userDoc.data();

    const mailOptions = {
      from: "QuickBoard <kambleaayush136@gmail.com>",
      to: user.email,
      subject: "🎟️ Train Ticket Confirmed - QuickBoard",
      html: `
        <h2>🚆 Train Ticket Confirmed</h2>
        <p><b>Name:</b> ${user.name}</p>
        <p><b>From:</b> ${booking.from}</p>
        <p><b>To:</b> ${booking.to}</p>
        <p><b>Adults:</b> ${booking.adults}</p>
        <p><b>Children:</b> ${booking.children}</p>
        <p><b>Ticket Type:</b> ${booking.ticketType}</p>
        <p><b>Train Type:</b> ${booking.trainType}</p>
        <p><b>Fare:</b> ₹${booking.fare}</p>
        <p><b>Status:</b> ${booking.status}</p>
        <br/>
        <p>Thank you for using <b>QuickBoard</b> ❤️</p>
      `,
    };

    await transporter.sendMail(mailOptions);
    console.log("✅ Train ticket email sent to:", user.email);

    return null;
  });

/* ---------- ADMIN DELETE USER ---------- */
exports.deleteUserAuth = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated");
  }

  const adminUid = context.auth.uid;

  const adminDoc = await admin.firestore()
    .collection("users")
    .doc(adminUid)
    .get();

  if (!adminDoc.exists || adminDoc.data().role !== "ADMIN") {
    throw new functions.https.HttpsError("permission-denied");
  }

  const uid = data.uid;

  try {
    await admin.auth().deleteUser(uid);
  } catch (err) {
    // Ignore if user already deleted
    if (err.code !== "auth/user-not-found") {
      throw new functions.https.HttpsError("internal", err.message);
    }
  }

  return { success: true };
});
