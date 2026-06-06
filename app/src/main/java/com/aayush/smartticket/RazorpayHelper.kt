package com.aayush.smartticket

import android.app.Activity
import com.razorpay.Checkout
import org.json.JSONObject

object RazorpayHelper {

    fun startPayment(
        activity: Activity,
        amountInRupees: Int,
        description: String
    ) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_RmkOBzkbaESj9n")

        val options = JSONObject().apply {
            put("name", "SmartTicket")
            put("description", description)
            put("currency", "INR")
            put("amount", amountInRupees) // paise
        }

        checkout.open(activity, options)
    }
}
