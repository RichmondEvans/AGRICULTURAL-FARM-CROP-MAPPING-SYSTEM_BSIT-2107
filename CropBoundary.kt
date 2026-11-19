
package com.example.agricultural

/**
 * Represents the map boundary of a crop.
 * cropId refers to the Crop record, so it is not
 * treated as a separate database record ID.
 */
data class CropBoundary(
    val cropId: Int,
    val cropName: String,
    val points: List<Pair<Double, Double>>
)
