package com.aayush.smartticket

data class BestBusRoute(


    val busNumber: String,
    val stops: List<String>,
    val farePerStop: Int = 5   // ✅ ADD THIS
)

val bestBusRoutes = listOf(

    BestBusRoute(
        busNumber = "123",
        stops = listOf(
            "Colaba",
            "Churchgate",
            "Marine Lines",
            "Grant Road",
            "Mumbai Central",
            "Dadar",
            "Sion",
            "Kurla"
        )
    ),

    BestBusRoute(
        busNumber = "27",
        stops = listOf(
            "Andheri Station",
            "Jogeshwari",
            "Goregaon",
            "Malad",
            "Kandarpada"
        )
    ),

    BestBusRoute(
        busNumber = "521",
        stops = listOf(
            "Thane Station",
            "Mulund",
            "Bhandup",
            "Vikhroli",
            "Ghatkopar"
        )
    ),

    BestBusRoute(
        busNumber = "340",
        stops = listOf(
            "Goregaon East",
            "Jogeshwari East",
            "Andheri East",
            "Kurla",
            "Chembur"
        )
    )
)
