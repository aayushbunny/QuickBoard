@file:OptIn(ExperimentalMaterial3Api::class)


package com.aayush.smartticket
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.draw.alpha

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Check




val AppBlue = Color(0xFF2563EB)
val ScreenBg = Color(0xFFF5F7FA)
val SoftBlue = Color(0xFFEFF6FF)

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}

/* ====================================================
   CORE / MODERN COMPONENTS (TRAIN / PLATFORM)
==================================================== */

/* ---------- SUMMARY ROW ---------- */

@Composable
fun SummaryRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.DarkGray)
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/* ---------- PASSENGER STEPPER ---------- */

@Composable
fun PassengerStepper(
    label: String,
    count: Int,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    Column {
        Text(label, fontSize = 13.sp, color = Color.DarkGray)
        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF1F4F7)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinus, enabled = count > 0) {
                    Text("-", fontSize = 22.sp)
                }

                Text(
                    count.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(onClick = onPlus) {
                    Text("+", fontSize = 20.sp)
                }
            }
        }
    }
}

/* ---------- STATION FIELD ---------- */

@Composable
fun StationField(
    label: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val isSelected = value.isNotBlank()

    val bgColor by animateColorAsState(
        if (isSelected) Color(0xFFE8F0FE) else Color(0xFFF1F4F7),
        label = ""
    )

    val borderColor by animateColorAsState(
        if (isSelected) Color(0xFF0D47A1) else Color.Transparent,
        label = ""
    )

    Column {
        if (label.isNotBlank()) {
            Text(label, fontSize = 13.sp, color = Color.DarkGray)
            Spacer(Modifier.height(6.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) "Select" else value,
                    modifier = Modifier.weight(1f),
                    color = if (isSelected) Color(0xFF0D47A1) else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )

                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF0D47A1) else Color.Gray
                )
            }
        }
    }
}

/* ---------- STATION PICKER BOTTOM SHEET ---------- */

@Composable
fun StationPickerBottomSheet(
    title: String,
    stations: List<Station>,
    onSelect: (Station) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredStations = remember(query) {
        if (query.isBlank()) stations
        else stations.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }


    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(50))
            )

            Spacer(Modifier.height(12.dp))

            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F4F7),
                    unfocusedContainerColor = Color(0xFFF1F4F7),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn {
                items(filteredStations) { station ->
                    Text(
                        station.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(station)
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        fontSize = 15.sp
                    )

                }
            }
        }
    }
}

/* ====================================================
   ADAPTER COMPONENTS (FOR BUS BOOKING SCREEN)
==================================================== */

/* ---------- OLD NAMES → NEW IMPLEMENTATION ---------- */

@Composable
fun SectionContainer(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}


@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}


@Composable
fun SelectField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Text(
            text = if (value.isEmpty()) placeholder else value,
            modifier = Modifier.padding(16.dp),
            color = if (value.isEmpty()) Color.Gray else Color.Black,
            fontSize = 16.sp
        )
    }
}
@Composable
fun PassengerRow(
    label: String,
    count: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    PassengerStepper(
        label = label,
        count = count,
        onMinus = onMinus,
        onPlus = onPlus
    )
}

@Composable
fun BottomSheetPicker(
    title: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }

    val filteredItems = items.filter {
        it.contains(search, ignoreCase = true)
    }

    ModalBottomSheet(onDismissRequest = { }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp)
        ) {

            Text(title, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(12.dp))

            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search") }
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn {
                items(filteredItems) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(14.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


@Composable
fun BusSummaryRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    SummaryRow(label, value, bold)
}
@Composable
fun RowScope.ModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF2563EB) else Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color.Black
        )
    }
}
@Composable
fun SoftField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color(0xFFEFF6FF), RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (value.isBlank()) "Select" else value,
                fontSize = 14.sp
            )
        }
    }
}
@Composable
fun SegmentedCard(
    title: String,
    leftText: String,
    rightText: String,
    selectedLeft: Boolean,
    enabled: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Row(Modifier.fillMaxSize()) {
                    Segment(leftText, selectedLeft, enabled, onLeft)
                    Segment(rightText, !selectedLeft, enabled, onRight)
                }
            }
        }
    }
}

@Composable
private fun RowScope.Segment(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF2563EB) else Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color.Black
        )
    }
}
@Composable
fun SectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}@Composable
fun BusTypeChip(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) AppBlue else Color(0xFFF1F3F6)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BottomPayBar(
    amount: Int,
    enabled: Boolean,
    onPay: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "₹$amount",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onPay,
                enabled = enabled,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Pay & Book")
            }
        }
    }
}
@Composable
fun BusJourneySummaryCard(
    route: String,
    fromStop: String,
    toStop: String,
    adults: Int,
    children: Int,
    busType: String,
    stops: Int,
    fare: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Journey Summary",
                fontWeight = FontWeight.SemiBold
            )

            SummaryRow("Number", route)
            SummaryRow("From", fromStop)
            SummaryRow("To", toStop)
            SummaryRow("Type", busType)
            SummaryRow(
                "Passengers",
                "Adults: $adults${if (children > 0) ", Children: $children" else ""}"
            )
            SummaryRow("Stops", "$stops stops")

            Divider()

            SummaryRow(
                label = "Total Fare",
                value = "₹$fare",
                bold = true
            )
        }
    }
}
@Composable
fun BookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F3F6)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.mode,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                StatusChip(booking.status)
            }

            if (booking.from.isNotBlank()) {
                Text(
                    text = "${booking.from} → ${booking.to}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = formatDate(booking.createdAt),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Passengers: ${booking.adults + booking.children}",
                    fontSize = 13.sp
                )

                Text(
                    text = "₹${booking.fare}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
@Composable
fun StatusChip(status: String) {
    val bgColor = when (status.uppercase()) {
        "CONFIRMED" -> Color(0xFFD1FAE5)
        "PENDING" -> Color(0xFFFEF3C7)
        else -> Color(0xFFE5E7EB)
    }

    val textColor = when (status.uppercase()) {
        "CONFIRMED" -> Color(0xFF065F46)
        "PENDING" -> Color(0xFF92400E)
        else -> Color.DarkGray
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun QrCodeBox(data: String) {
    val bitmap = remember(data) {
        generateQrCodeBitmap(data)
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code",
        modifier = Modifier.size(180.dp)
    )
}
fun shareBitmap(context: Context, bitmap: Bitmap) {
    val file = File(context.cacheDir, "ticket.png")

    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share Ticket")
    )
}
fun shareBitmapAsPdf(context: Context, bitmap: Bitmap) {
    val pdfFile = File(context.cacheDir, "ticket.pdf")

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(
        bitmap.width,
        bitmap.height,
        1
    ).create()

    val page = document.startPage(pageInfo)
    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
    document.finishPage(page)

    FileOutputStream(pdfFile).use {
        document.writeTo(it)
    }
    document.close()

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        pdfFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share Ticket PDF")
    )
}
@Composable
fun TicketDropdownCard(
    title: String,
    value: String,
    enabled: Boolean = true,
    placeholder: String = "",
    helperText: String? = null,
    onClick: () -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .alpha(if (enabled) 1f else 0.4f)
                .clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF1F4F7)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) placeholder else value,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isBlank()) Color.Gray else Color.Black
                )


                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }

        if (helperText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                helperText,
                fontSize = 12.sp,)
        }

    }}
@Composable
fun TrainTypeBottomSheet(
    travelClass: TravelClass,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val acEmuEnabled = travelClass == TravelClass.FIRST

    ModalBottomSheet(onDismissRequest = onDismiss) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(50))
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Select Train Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            // ORDINARY
            ListItem(
                headlineContent = { Text("Ordinary") },
                trailingContent = {
                    if (selected == "ORDINARY") {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                modifier = Modifier.clickable {
                    onSelect("ORDINARY")
                    onDismiss()
                }
            )
// AC EMU (DISABLED FOR ALL CLASSES)
            ListItem(
                headlineContent = {
                    Text(
                        "AC EMU",
                        color = if (acEmuEnabled) Color.Black else Color.Gray
                    )
                },
                supportingContent = {
                    if (!acEmuEnabled) {
                        Text(
                            "Available only in First Class",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                },
                trailingContent = {
                    if (selected == "AC EMU TRAIN") {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                modifier = Modifier.clickable(enabled = acEmuEnabled) {
                    onSelect("AC EMU TRAIN")
                    onDismiss()
                }
            )
        }}}
            @Composable
fun ClassBottomSheet(
    selected: TravelClass,
    onSelect: (TravelClass) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {

        Text(
            "Select Class",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )

        // SECOND
        ListItem(
            headlineContent = { Text("Second") },
            trailingContent = {
                if (selected == TravelClass.SECOND) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            },
            modifier = Modifier.clickable {
                onSelect(TravelClass.SECOND)
                onDismiss()
            }
        )

        // FIRST
        ListItem(
            headlineContent = { Text("First") },
            trailingContent = {
                if (selected == TravelClass.FIRST) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            },
            modifier = Modifier.clickable {
                onSelect(TravelClass.FIRST)
                onDismiss()
            }
        )

        Spacer(Modifier.height(16.dp))
    }
}
