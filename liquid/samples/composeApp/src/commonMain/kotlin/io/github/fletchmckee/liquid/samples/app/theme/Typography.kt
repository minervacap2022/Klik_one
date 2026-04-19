// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import liquid_root.samples.composeapp.generated.resources.Res
import liquid_root.samples.composeapp.generated.resources.assistant_bold
import liquid_root.samples.composeapp.generated.resources.assistant_regular
import liquid_root.samples.composeapp.generated.resources.encodesans_sc_bold
import liquid_root.samples.composeapp.generated.resources.encodesans_sc_regular
import liquid_root.samples.composeapp.generated.resources.gantari_bold
import liquid_root.samples.composeapp.generated.resources.gantari_regular
import liquid_root.samples.composeapp.generated.resources.ibmplexsans_bold
import liquid_root.samples.composeapp.generated.resources.ibmplexsans_regular
import liquid_root.samples.composeapp.generated.resources.kanit_bold
import liquid_root.samples.composeapp.generated.resources.kanit_regular
import liquid_root.samples.composeapp.generated.resources.kantumruypro_bold
import liquid_root.samples.composeapp.generated.resources.kantumruypro_regular
import liquid_root.samples.composeapp.generated.resources.montserrat_bold
import liquid_root.samples.composeapp.generated.resources.montserrat_regular
import liquid_root.samples.composeapp.generated.resources.prompt_bold
import liquid_root.samples.composeapp.generated.resources.prompt_regular
import liquid_root.samples.composeapp.generated.resources.roboto_bold
import liquid_root.samples.composeapp.generated.resources.roboto_medium
import liquid_root.samples.composeapp.generated.resources.roboto_regular
import liquid_root.samples.composeapp.generated.resources.overpass_bold
import liquid_root.samples.composeapp.generated.resources.overpass_regular
import liquid_root.samples.composeapp.generated.resources.intertight_variable
import liquid_root.samples.composeapp.generated.resources.robotocondensed_variable
import liquid_root.samples.composeapp.generated.resources.robotoflex_variable
import liquid_root.samples.composeapp.generated.resources.archivonarrow_variable
import liquid_root.samples.composeapp.generated.resources.sourcesans3_variable
import liquid_root.samples.composeapp.generated.resources.ibmplexsanscondensed_thin
import liquid_root.samples.composeapp.generated.resources.firasanscondensed_thin
import liquid_root.samples.composeapp.generated.resources.barlowcondensed_thin
import liquid_root.samples.composeapp.generated.resources.encodesanscondensed_thin
import liquid_root.samples.composeapp.generated.resources.kanit_thin
import org.jetbrains.compose.resources.Font

// Font Collection Data Class
data class FontCollection(
    val name: String,
    val description: String,
    val fontFamily: FontFamily,
    val letterSpacingMultiplier: Float = 1f,
    val lineHeightMultiplier: Float = 1f
)

// Composable function to create FontFamily from resources
@Composable
fun assistantFontFamily(): FontFamily = FontFamily(
    Font(Res.font.assistant_regular, FontWeight.Normal),
    Font(Res.font.assistant_bold, FontWeight.Bold)
)

@Composable
fun montserratFontFamily(): FontFamily = FontFamily(
    Font(Res.font.montserrat_regular, FontWeight.Normal),
    Font(Res.font.montserrat_bold, FontWeight.Bold)
)

@Composable
fun promptFontFamily(): FontFamily = FontFamily(
    Font(Res.font.prompt_regular, FontWeight.Normal),
    Font(Res.font.prompt_bold, FontWeight.Bold)
)

@Composable
fun kanitFontFamily(): FontFamily = FontFamily(
    Font(Res.font.kanit_regular, FontWeight.Normal),
    Font(Res.font.kanit_bold, FontWeight.Bold)
)

@Composable
fun ibmPlexSansFontFamily(): FontFamily = FontFamily(
    Font(Res.font.ibmplexsans_regular, FontWeight.Normal),
    Font(Res.font.ibmplexsans_bold, FontWeight.Bold)
)

@Composable
fun gantariFontFamily(): FontFamily = FontFamily(
    Font(Res.font.gantari_regular, FontWeight.Normal),
    Font(Res.font.gantari_bold, FontWeight.Bold)
)

@Composable
fun encodeSansSCFontFamily(): FontFamily = FontFamily(
    Font(Res.font.encodesans_sc_regular, FontWeight.Normal),
    Font(Res.font.encodesans_sc_bold, FontWeight.Bold)
)

@Composable
fun kantumruyProFontFamily(): FontFamily = FontFamily(
    Font(Res.font.kantumruypro_regular, FontWeight.Normal),
    Font(Res.font.kantumruypro_bold, FontWeight.Bold)
)

@Composable
fun robotoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.roboto_regular, FontWeight.Normal),
    Font(Res.font.roboto_medium, FontWeight.Medium),
    Font(Res.font.roboto_bold, FontWeight.Bold)
)

@Composable
fun overpassFontFamily(): FontFamily = FontFamily(
    Font(Res.font.overpass_regular, FontWeight.Normal),
    Font(Res.font.overpass_bold, FontWeight.Bold)
)

@Composable
fun interTightFontFamily(): FontFamily = FontFamily(
    Font(Res.font.intertight_variable, FontWeight.Normal),
    Font(Res.font.intertight_variable, FontWeight.Thin, androidx.compose.ui.text.font.FontStyle.Normal)
)

@Composable
fun robotoCondensedFontFamily(): FontFamily = FontFamily(
    Font(Res.font.robotocondensed_variable, FontWeight.Normal),
    Font(Res.font.robotocondensed_variable, FontWeight.Thin)
)

@Composable
fun robotoFlexFontFamily(): FontFamily = FontFamily(
    Font(Res.font.robotoflex_variable, FontWeight.Normal),
    Font(Res.font.robotoflex_variable, FontWeight.Thin)
)

@Composable
fun archivoNarrowFontFamily(): FontFamily = FontFamily(
    Font(Res.font.archivonarrow_variable, FontWeight.Normal),
    Font(Res.font.archivonarrow_variable, FontWeight.Thin)
)

@Composable
fun sourceSans3FontFamily(): FontFamily = FontFamily(
    Font(Res.font.sourcesans3_variable, FontWeight.Normal),
    Font(Res.font.sourcesans3_variable, FontWeight.ExtraLight)
)

@Composable
fun ibmPlexSansCondensedFontFamily(): FontFamily = FontFamily(
    Font(Res.font.ibmplexsanscondensed_thin, FontWeight.Thin)
)

@Composable
fun firaSansCondensedFontFamily(): FontFamily = FontFamily(
    Font(Res.font.firasanscondensed_thin, FontWeight.Thin)
)

@Composable
fun barlowCondensedFontFamily(): FontFamily = FontFamily(
    Font(Res.font.barlowcondensed_thin, FontWeight.Thin)
)

@Composable
fun encodeSansCondensedFontFamily(): FontFamily = FontFamily(
    Font(Res.font.encodesanscondensed_thin, FontWeight.Thin)
)

@Composable
fun kanitThinFontFamily(): FontFamily = FontFamily(
    Font(Res.font.kanit_thin, FontWeight.Thin)
)

// Get all font collections as a composable list
@Composable
fun getFontCollections(): List<FontCollection> = listOf(
    // 0. System Default - Clean and familiar
    FontCollection(
        name = "System",
        description = "Clean, familiar system fonts",
        fontFamily = FontFamily.Default,
        letterSpacingMultiplier = 1f,
        lineHeightMultiplier = 1f
    ),

    // 1. Montserrat - Modern geometric sans-serif
    FontCollection(
        name = "Montserrat",
        description = "Modern geometric elegance",
        fontFamily = montserratFontFamily(),
        letterSpacingMultiplier = 0.98f,
        lineHeightMultiplier = 1.02f
    ),

    // 2. Assistant - Clean Hebrew-inspired
    FontCollection(
        name = "Assistant",
        description = "Friendly, approachable",
        fontFamily = assistantFontFamily(),
        letterSpacingMultiplier = 1f,
        lineHeightMultiplier = 1f
    ),

    // 3. Prompt - Thai-inspired modern
    FontCollection(
        name = "Prompt",
        description = "Sharp, contemporary Thai",
        fontFamily = promptFontFamily(),
        letterSpacingMultiplier = 0.95f,
        lineHeightMultiplier = 1.05f
    ),

    // 4. Kanit - Bold Thai geometric
    FontCollection(
        name = "Kanit",
        description = "Bold geometric Thai style",
        fontFamily = kanitFontFamily(),
        letterSpacingMultiplier = 0.92f,
        lineHeightMultiplier = 1.02f
    ),

    // 5. IBM Plex Sans - Professional tech
    FontCollection(
        name = "IBM Plex",
        description = "Professional tech aesthetic",
        fontFamily = ibmPlexSansFontFamily(),
        letterSpacingMultiplier = 1f,
        lineHeightMultiplier = 1.05f
    ),

    // 6. Gantari - Modern Indonesian
    FontCollection(
        name = "Gantari",
        description = "Clean modern Indonesian",
        fontFamily = gantariFontFamily(),
        letterSpacingMultiplier = 0.98f,
        lineHeightMultiplier = 1f
    ),

    // 7. Encode Sans Semi Condensed - Space efficient
    FontCollection(
        name = "Encode Sans",
        description = "Compact, space-efficient",
        fontFamily = encodeSansSCFontFamily(),
        letterSpacingMultiplier = 0.9f,
        lineHeightMultiplier = 0.98f
    ),

    // 8. Kantumruy Pro - Elegant Khmer-inspired
    FontCollection(
        name = "Kantumruy",
        description = "Elegant Khmer-inspired",
        fontFamily = kantumruyProFontFamily(),
        letterSpacingMultiplier = 1f,
        lineHeightMultiplier = 1.08f
    ),

    // 9. Roboto - Android standard
    FontCollection(
        name = "Roboto",
        description = "Google's versatile classic",
        fontFamily = robotoFontFamily(),
        letterSpacingMultiplier = 1f,
        lineHeightMultiplier = 1f
    ),

    // 10. Overpass - Highway inspired
    FontCollection(
        name = "Overpass",
        description = "Highway signage inspired",
        fontFamily = overpassFontFamily(),
        letterSpacingMultiplier = 1.02f,
        lineHeightMultiplier = 1.02f
    ),

    // 11. Classic Serif (system)
    FontCollection(
        name = "Classic Serif",
        description = "Timeless, elegant serif",
        fontFamily = FontFamily.Serif,
        letterSpacingMultiplier = 1.02f,
        lineHeightMultiplier = 1.05f
    ),

    // 12. Mono Tech (system)
    FontCollection(
        name = "Mono Tech",
        description = "Technical, developer-friendly",
        fontFamily = FontFamily.Monospace,
        letterSpacingMultiplier = 0.9f,
        lineHeightMultiplier = 1.1f
    ),

    // New Thin/Condensed Fonts
    FontCollection("Inter Tight", "Modern tight spacing", interTightFontFamily(), 0.95f, 1.05f),
    FontCollection("Roboto Condensed", "Space-efficient standard", robotoCondensedFontFamily(), 0.9f, 1f),
    FontCollection("Roboto Flex", "Versatile variable font", robotoFlexFontFamily(), 1f, 1f),
    FontCollection("Archivo Narrow", "Grotesque narrow", archivoNarrowFontFamily(), 0.9f, 1f),
    FontCollection("Source Sans 3", "UI optimization", sourceSans3FontFamily(), 1f, 1.05f),
    FontCollection("IBM Plex Condensed", "Tech condensed", ibmPlexSansCondensedFontFamily(), 0.9f, 1f),
    FontCollection("Fira Sans Condensed", "Humanist condensed", firaSansCondensedFontFamily(), 0.92f, 1f),
    FontCollection("Barlow Condensed", "Low contrast condensed", barlowCondensedFontFamily(), 0.9f, 1f),
    FontCollection("Encode Sans Condensed", "Industrial condensed", encodeSansCondensedFontFamily(), 0.85f, 1f),
    FontCollection("Kanit Thin", "Modern Thin Thai", kanitThinFontFamily(), 0.95f, 1.05f)
)

// Non-composable version for static contexts (when Composable context is not available)
val FontCollections = listOf(
    FontCollection("System", "Clean, familiar system fonts", FontFamily.Default),
    FontCollection("Montserrat", "Modern geometric elegance", FontFamily.SansSerif, 0.98f, 1.02f),
    FontCollection("Assistant", "Friendly, approachable", FontFamily.SansSerif),
    FontCollection("Prompt", "Sharp, contemporary Thai", FontFamily.SansSerif, 0.95f, 1.05f),
    FontCollection("Kanit", "Bold geometric Thai style", FontFamily.SansSerif, 0.92f, 1.02f),
    FontCollection("IBM Plex", "Professional tech aesthetic", FontFamily.SansSerif, 1f, 1.05f),
    FontCollection("Gantari", "Clean modern Indonesian", FontFamily.SansSerif, 0.98f, 1f),
    FontCollection("Encode Sans", "Compact, space-efficient", FontFamily.SansSerif, 0.9f, 0.98f),
    FontCollection("Kantumruy", "Elegant Khmer-inspired", FontFamily.SansSerif, 1f, 1.08f),
    FontCollection("Roboto", "Google's versatile classic", FontFamily.SansSerif),
    FontCollection("Overpass", "Highway signage inspired", FontFamily.SansSerif, 1.02f, 1.02f),
    FontCollection("Classic Serif", "Timeless, elegant serif", FontFamily.Serif, 1.02f, 1.05f),
    FontCollection("Mono Tech", "Technical, developer-friendly", FontFamily.Monospace, 0.9f, 1.1f)
)

// Function to get Typography based on font collection index (composable version)
@Composable
fun getTypographyForFontComposable(fontIndex: Int): Typography {
    val collections = getFontCollections()
    val collection = collections[fontIndex]
    
    val fontSizeScale = LocalFontSizeScale.current
    val letterSpacingScale = LocalLetterSpacingScale.current
    val lineHeightScale = LocalLineHeightScale.current

    return createTypography(collection, fontSizeScale, letterSpacingScale, lineHeightScale)
}

// Function to get Typography based on font collection index (non-composable version)
fun getTypographyForFont(fontIndex: Int): Typography {
    val collection = FontCollections[fontIndex]
    return createTypography(collection)
}

// Helper to create Typography from a FontCollection
private fun createTypography(
    collection: FontCollection,
    fontSizeScale: Float = 1f,
    letterSpacingScale: Float = 1f,
    lineHeightScale: Float = 1f
): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontSizeScale).sp,
            lineHeight = (24 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0.5f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontSizeScale).sp,
            lineHeight = (20 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0.25f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * fontSizeScale).sp,
            lineHeight = (16 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0.4f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (22 * fontSizeScale).sp,
            lineHeight = (28 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * fontSizeScale).sp,
            lineHeight = (24 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0.15f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        titleSmall = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontSizeScale).sp,
            lineHeight = (20 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0.1f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (32 * fontSizeScale).sp,
            lineHeight = (40 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (28 * fontSizeScale).sp,
            lineHeight = (36 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (24 * fontSizeScale).sp,
            lineHeight = (32 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        labelSmall = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * fontSizeScale).sp,
            lineHeight = (16 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        labelMedium = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * fontSizeScale).sp,
            lineHeight = (16 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0.5f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = collection.fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (16 * fontSizeScale).sp,
            lineHeight = (24 * collection.lineHeightMultiplier * fontSizeScale * lineHeightScale).sp,
            letterSpacing = (0f * collection.letterSpacingMultiplier * fontSizeScale * letterSpacingScale).sp,
        ),
    )
}

// Default Typography (using IBM Plex font - index 5)
val Typography = getTypographyForFont(5)
