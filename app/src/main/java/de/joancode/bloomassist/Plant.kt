package de.joancode.bloomassist

data class Plant(
    val id: Int,
    val name: String,
    val type: String,
    val location: String,
    val date: String,
    val moisture: String,
    val wateringTip: String,
    val lightTip: String,
    val fertilizerTip: String,
    val notes: String
)