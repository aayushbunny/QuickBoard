package com.aayush.smartticket.location

import android.location.Location

data class Station(
    val name: String,
    val lat: Double,
    val lng: Double
)

object StationGeoFence {

    private const val PLATFORM_RADIUS = 40f
    private const val STATION_RADIUS = 150f

    enum class BlockReason { PLATFORM, STATION, NONE }

    val stations = listOf(
        Station("Andheri", 19.119698, 72.846420),
        Station("Bandra", 19.054979, 72.840220),
        Station("Bhandup", 19.143868, 72.938433),
        Station("Bhayandar", 19.310185, 72.852699),
        Station("Borivali", 19.229068, 72.857363),
        Station("Byculla", 18.976622, 72.832794),
        Station("Charni Road", 18.951844, 72.818240),
        Station("Chembur", 19.061213, 72.897591),
        Station("Chembur", 19.062632, 72.901140),
        Station("Chinchpokli", 18.987198, 72.832672),
        Station("Churchgate", 18.935480, 72.827174),
        Station("Churchgate", 18.931495, 72.826939),
        Station("Cotton Green", 18.987187, 72.843896),
        Station("Currey Road", 18.994551, 72.832871),
        Station("Dadar", 19.017734, 72.843752),
        Station("Dadar", 19.019227, 72.842848),
        Station("Dadar", 19.023624, 72.839221),
        Station("Dahisar", 19.249357, 72.859630),
        Station("Dockyard Road", 18.967477, 72.844548),
        Station("Dombivli", 19.218049, 73.086135),
        Station("Ghatkopar", 19.085693, 72.908367),
        Station("Goregaon", 19.164869, 72.849549),
        Station("Govandi", 19.055369, 72.915070),
        Station("Grant Road", 18.963355, 72.815826),
        Station("Grant Road", 18.963218, 72.818044),
        Station("Khar Road", 19.068241, 72.840041),
        Station("Lower Parel", 18.995680, 72.830276),
        Station("Lower Parel", 18.993120, 72.831470),
        Station("Mahalaxmi", 18.982520, 72.824220),
        Station("Mahalaxmi", 18.979467, 72.825401),
        Station("Malad", 19.186719, 72.848588),
        Station("Mankhurd", 19.048518, 72.932336),
        Station("Mankhurd", 19.049192, 72.931151),
        Station("Marine Lines", 18.945764, 72.823719),
        Station("Masjid", 18.952295, 72.838177),
        Station("Matunga", 19.027436, 72.850147),
        Station("Matunga Road", 19.027815, 72.846681),
        Station("Mira Road", 19.281697, 72.856068),
        Station("Mulund", 19.172176, 72.956238),
        Station("Mumbai Central", 18.969586, 72.819315),
        Station("Mumbra", 19.189943, 73.023075),
        Station("Nahur", 19.155074, 72.946919),
        Station("Naigaon", 19.351093, 72.846523),
        Station("Naigaon", 19.009539, 72.847871),
        Station("Panvel", 18.989424, 73.122695),
        Station("Parel", 19.009482, 72.837661),
        Station("Prabhadevi", 19.007472, 72.835897),
        Station("Reay Road", 18.977551, 72.844101),
        Station("Sandhurst Road", 18.960924, 72.839372),
        Station("Santacruz", 19.079289, 72.847118),
        Station("Sewri", 18.998780, 72.854422),
        Station("Sion", 19.046521, 72.863283),
        Station("Thane", 19.186483, 72.975766),
        Station("Vasai Road", 19.382668, 72.832025),
        Station("Vidyavihar", 19.079542, 72.897117),
        Station("Vikhroli", 19.111480, 72.928021),
        Station("Vile Parle", 19.099910, 72.844004),
        Station("Virar", 19.455306, 72.811816),
    )

    fun checkUserLocation(userLat: Double, userLng: Double): BlockReason {

        stations.forEach { station ->
            val result = FloatArray(1)
            Location.distanceBetween(
                userLat, userLng,
                station.lat, station.lng,
                result
            )

            val distance = result[0]

            if (distance <= PLATFORM_RADIUS) {
                return BlockReason.PLATFORM
            }

            if (distance <= STATION_RADIUS) {
                return BlockReason.STATION
            }
        }

        return BlockReason.NONE
    }
}
