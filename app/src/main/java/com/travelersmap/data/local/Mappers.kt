package com.travelersmap.data.local

import com.travelersmap.domain.model.Difficulty
import com.travelersmap.domain.model.PlaceCategory
import com.travelersmap.domain.model.TouristPlace

fun TouristPlaceEntity.toDomain(): TouristPlace = TouristPlace(
    id = id,
    countryCode = countryCode,
    name = name,
    city = city,
    category = PlaceCategory.valueOf(category),
    shortDescription = shortDescription,
    description = description,
    history = history,
    latitude = latitude,
    longitude = longitude,
    photoUrls = photoUrlsCsv.split("|").filter { it.isNotBlank() },
    openingHours = openingHours,
    ticketPrice = ticketPrice,
    estimatedVisitMinutes = estimatedVisitMinutes,
    bestSeason = bestSeason,
    difficulty = Difficulty.valueOf(difficulty),
    familyFriendly = familyFriendly,
    rating = rating,
    nearbyIds = nearbyIdsCsv.split("|").filter { it.isNotBlank() },
    accentColorHex = accentColorHex
)

fun TouristPlace.toEntity(): TouristPlaceEntity = TouristPlaceEntity(
    id = id,
    countryCode = countryCode,
    name = name,
    city = city,
    category = category.name,
    shortDescription = shortDescription,
    description = description,
    history = history,
    latitude = latitude,
    longitude = longitude,
    photoUrlsCsv = photoUrls.joinToString("|"),
    openingHours = openingHours,
    ticketPrice = ticketPrice,
    estimatedVisitMinutes = estimatedVisitMinutes,
    bestSeason = bestSeason,
    difficulty = difficulty.name,
    familyFriendly = familyFriendly,
    rating = rating,
    nearbyIdsCsv = nearbyIds.joinToString("|"),
    accentColorHex = accentColorHex
)
