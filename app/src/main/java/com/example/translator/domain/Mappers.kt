package com.example.translator.domain

import com.example.translator.data.local.models.LanguageEntity
import com.example.translator.data.local.models.TranslationEntity
import com.example.translator.data.remote.translation.models.LanguageDto
import com.example.translator.data.remote.translation.models.TranslationDto
import com.example.translator.domain.models.Language
import com.example.translator.domain.models.Translation

fun LanguageEntity.toDomain(): Language {
    return Language(
        code = code,
        title = title
    )
}

fun TranslationEntity.toDomain(): Translation {
    return Translation(
        id = id,
        sourceLang = sourceLang.toDomain(),
        targetLang = targetLang.toDomain(),
        sourceText = sourceText,
        text = text,
        isSelected = isSelected,
        isDeletedFromHistory = isDeletedFromHistory,
        created = created,
    )
}

fun LanguageDto.toDomain(): Language {
    return Language(
        code = code,
        title = title
    )
}

fun TranslationDto.toDomain(): Translation {
    return Translation(
        id = id,
        sourceLang = sourceLang.toDomain(),
        targetLang = targetLang.toDomain(),
        sourceText = sourceText,
        text = text,
        isSelected = isSelected,
        isDeletedFromHistory = isDeletedFromHistory,
        created = created
    )
}

fun Language.toDto(): LanguageDto {
    return LanguageDto(
        code = code,
        title = title
    )
}

fun Translation.toDto(): TranslationDto {
    return TranslationDto(
        id = id,
        sourceLang = sourceLang.toDto(),
        targetLang = targetLang.toDto(),
        sourceText = sourceText,
        text = text,
        isSelected = isSelected,
        isDeletedFromHistory = isDeletedFromHistory,
        created = created
    )
}

fun Language.toEntity(): LanguageEntity {
    return LanguageEntity(
        code = code,
        title = title
    )
}

fun Translation.toEntity(): TranslationEntity {
    return TranslationEntity(
        id = id,
        sourceLang = sourceLang.toEntity(),
        targetLang = targetLang.toEntity(),
        sourceText = sourceText,
        text = text,
        isSelected = isSelected,
        isDeletedFromHistory = isDeletedFromHistory,
        created = created
    )
}


