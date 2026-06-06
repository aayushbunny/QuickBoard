package com.aayush.smartticket

enum class ConcessionType(val label: String, val discountPercent: Int) {

    NONE("No Concession", 0),

    SENIOR_CITIZEN_MALE("Senior Citizen (Male)", 40),
    SENIOR_CITIZEN_FEMALE("Senior Citizen (Female)", 50),

    BLIND_WITH_ESCORT("Blind with Escort", 75),
    BLIND_ALONE("Blind Alone", 75),

    DEAF_DUMB("Deaf & Dumb", 50),

    PHYSICALLY_HANDICAPPED("Physically Handicapped", 75),
    ESCORT_PHYSICALLY_HANDICAPPED("Escort for Physically Handicapped", 75),

    MENTALLY_HANDICAPPED("Mentally Handicapped", 75);
}
