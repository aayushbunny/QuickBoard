package com.aayush.smartticket

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object TicketPdfGenerator {

    fun generateTrainTicketPdf(
        context: Context,
        bookingId: String,
        status: String = "CONFIRMED"
    ): File {

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            textSize = 16f
        }

        var y = 80f

        fun row(label: String, value: String) {
            canvas.drawText(label, 40f, y, paint)
            canvas.drawText(value, 220f, y, paint)
            y += 32f
        }

        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("QuickBoard – Train Ticket", 40f, y, paint)

        y += 50f
        paint.textSize = 16f
        paint.isFakeBoldText = false

        row("From", LocalTrainBookingState.from)
        row("To", LocalTrainBookingState.to)
        row(
            "Passengers",
            "${LocalTrainBookingState.adults} Adult, ${LocalTrainBookingState.children} Child"
        )
        row("Train Type", LocalTrainBookingState.trainType)
        row("Ticket Type", LocalTrainBookingState.ticketType)
        row("Fare", "₹${LocalTrainBookingState.fare}")
        row("Status", status)
        row("Booking ID", bookingId)

        pdfDocument.finishPage(page)

        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "QuickBoardTickets"
        )
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "TrainTicket_$bookingId.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
}
