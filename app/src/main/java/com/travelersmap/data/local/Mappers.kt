package com.travelersmap.data.local

import com.travelersmap.domain.model.Difficulty
import com.travelersmap.domain.model.PlaceCategory
import com.travelersmap.domain.model.TouristPlace

fun TouristPlaceEntity.toDomain(): TouristPlace = TouristPlace(
    id = id,
    countryCode = countryCode,
    name = name,
    city = city,
    region = region.ifBlank { city },
    category = runCatching { PlaceCategory.valueOf(category) }.getOrDefault(PlaceCategory.HISTORICAL),
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
    difficulty = runCatching { Difficulty.valueOf(difficulty) }.getOrDefault(Difficulty.EASY),
    familyFriendly = familyFriendly,
    rating = rating,
    nearbyIds = nearbyIdsCsv.split("|").filter { it.isNotBlank() },
    website = website,
    phone = phone,
    accentColorHex = accentColorHex
)

fun TouristPlace.toEntity(): TouristPlaceEntity = TouristPlaceEntity(
    id = id,
    countryCode = countryCode,
    name = name,
    city = city,
    region = region,
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
    website = website,
    phone = phone,
    accentColorHex = accentColorHex
)
