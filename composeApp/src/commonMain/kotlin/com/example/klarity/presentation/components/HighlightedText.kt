package com.example.klarity.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Text component that highlights matching search query.
 * 
 * Useful for displaying search results with highlighted matches.
 */

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier
) {
    if (query.isEmpty()) {
        Text(text = text, modifier = modifier)
        return
    }
    
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        
        while (currentIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
            if (matchIndex == -1) {
                append(text.substring(currentIndex))
                break
            }
            
            // Text before match
            if (matchIndex > currentIndex) {
                append(text.substring(currentIndex, matchIndex))
            }
            
            // Highlighted match
            withStyle(
                SpanStyle(
                    background = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(matchIndex, matchIndex + query.length))
            }
            
            currentIndex = matchIndex + query.length
        }
    }
    
    Text(annotatedString, modifier = modifier)
}

/**
 * Highlights multiple search terms in text.
 */
@Composable
fun MultiHighlightedText(
    text: String,
    queries: List<String>,
    modifier: Modifier = Modifier
) {
    if (queries.isEmpty()) {
        Text(text = text, modifier = modifier)
        return
    }
    
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val positions = mutableListOf<Pair<Int, Int>>() // start, end
        
        // Find all match positions
        queries.forEach { query ->
            if (query.isNotEmpty()) {
                val lowerQuery = query.lowercase()
                var searchIndex = 0
                while (searchIndex < lowerText.length) {
                    val matchIndex = lowerText.indexOf(lowerQuery, searchIndex)
                    if (matchIndex == -1) break
                    
                    positions.add(matchIndex to matchIndex + query.length)
                    searchIndex = matchIndex + query.length
                }
            }
        }
        
        // Sort and merge overlapping positions
        val sortedPositions = positions.sortedBy { it.first }
        val mergedPositions = mutableListOf<Pair<Int, Int>>()
        
        sortedPositions.forEach { pos ->
            if (mergedPositions.isEmpty() || pos.first > mergedPositions.last().second) {
                mergedPositions.add(pos)
            } else {
                // Merge overlapping
                val last = mergedPositions.removeLast()
                mergedPositions.add(last.first to maxOf(last.second, pos.second))
            }
        }
        
        // Build annotated string
        mergedPositions.forEach { (start, end) ->
            if (start > currentIndex) {
                append(text.substring(currentIndex, start))
            }
            
            withStyle(
                SpanStyle(
                    background = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(start, end))
            }
            
            currentIndex = end
        }
        
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
    
    Text(annotatedString, modifier = modifier)
}
