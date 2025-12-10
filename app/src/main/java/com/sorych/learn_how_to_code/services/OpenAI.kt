package com.sorych.learn_how_to_code.services

import android.util.Log
import androidx.compose.ui.unit.IntOffset
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sorych.learn_how_to_code.ui.game.PathConfig
import com.sorych.learn_how_to_code.ui.game.Solution
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Request/Response models
data class ChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1000
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// Game models for parsing GPT response
data class LevelResponse(
    val games: List<GameDto>
)

data class GameDto(
    val startCell: CellDto,
    val allPaths: List<PathDto>
)

data class CellDto(
    val x: Int,
    val y: Int
)

data class PathDto(
    val id: String,
    val startCell: CellDto,
    val endCell: CellDto
)

// API Interface
interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}

// Service class
class OpenAIService(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    private val gson = Gson()

    suspend fun generateLevel(currentLevel: Int, gridSize: Int): LevelResponse {
        val prompt = """
            Generate a new level for a turtle coding game.
            The game teaches kids how to build algorithms and basic programming logic.
            Level: ${currentLevel}
            Grid size: ${gridSize}x13 (rows x cols)
            
            Rules:
            - Create exactly 3 games for this level
            - Paths in the same game don't intersect
            - No diagonal paths allowed
            - Coordinates must be within bounds: x=[0-12], y=[0-6]
            - Each path must have a unique string ID like "p1", "p2", "b1", etc.
            - Paths should connect: one path's endCell should match the next path's startCell
            
            Difficulty progression based on level and the game:
            - Game 1: Simple (3 paths)
            - Game 2: Medium (5 paths)
            - Game 3: Complex (7 paths)
            
            Return ONLY valid JSON with this EXACT structure (no markdown, no explanations):
            {
                "games": [
                    {
                        "startCell": {"x": 0, "y": 0},
                        "allPaths": [
                            {"id": "p1", "startCell": {"x": 0, "y": 0}, "endCell": {"x": 6, "y": 0}},
                            {"id": "p2", "startCell": {"x": 6, "y": 0}, "endCell": {"x": 6, "y": 3}}
                        ]
                    },
                    {
                        "startCell": {"x": 0, "y": 0},
                        "allPaths": [
                            {"id": "p1", "startCell": {"x": 0, "y": 0}, "endCell": {"x": 3, "y": 0}},
                            {"id": "p2", "startCell": {"x": 3, "y": 0}, "endCell": {"x": 3, "y": 3}},
                            {"id": "p3", "startCell": {"x": 3, "y": 3}, "endCell": {"x": 8, "y": 3}}
                        ]
                    },
                    {
                        "startCell": {"x": 0, "y": 0},
                        "allPaths": [
                            {"id": "p1", "startCell": {"x": 0, "y": 0}, "endCell": {"x": 4, "y": 0}},
                            {"id": "p2", "startCell": {"x": 4, "y": 0}, "endCell": {"x": 4, "y": 2}},
                            {"id": "p3", "startCell": {"x": 4, "y": 2}, "endCell": {"x": 8, "y": 2}},
                            {"id": "p4", "startCell": {"x": 8, "y": 2}, "endCell": {"x": 8, "y": 5}}
                        ]
                    }
                ]
            }
            
            Randomize the path coordinates and IDs. Do not reuse patterns from previous levels.

        """.trimIndent()

        val request = ChatRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                Message(role = "system", content = "You are a game level designer. Return only valid JSON without any markdown formatting or explanations."),
                Message(role = "user", content = prompt)
            ),
            temperature = 0.7,
            maxTokens = 2000
        )

        Log.d("OpenAIService", "Sending request to GPT...")

        val response = api.createChatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )

        val jsonContent = response.choices.firstOrNull()?.message?.content
            ?: throw Exception("No response from OpenAI API")

        Log.d("OpenAIService", "GPT Response: $jsonContent")

        // Clean the response (remove markdown if present)
        val cleanJson = jsonContent
            .replace("```json", "")
            .replace("```", "")
            .trim()

        Log.d("OpenAIService", "Cleaned JSON: $cleanJson")

        // Parse the JSON response
        return try {
            gson.fromJson(cleanJson, LevelResponse::class.java)
        } catch (e: Exception) {
            Log.e("OpenAIService", "Failed to parse JSON: ${e.message}")
            throw Exception("Failed to parse GPT response: ${e.message}")
        }
    }

    // Convert DTO to your game models
    fun convertToPathConfig(pathDto: PathDto): PathConfig {
        return PathConfig(
            startCell = IntOffset(pathDto.startCell.x, pathDto.startCell.y),
            endCell = IntOffset(pathDto.endCell.x, pathDto.endCell.y),
            id = pathDto.id
        )
    }

    // Compute direction from startCell to endCell
    fun computeDirection(path: PathConfig): Int {
        return when {
            path.endCell.x > path.startCell.x -> 4 // RIGHT
            path.endCell.x < path.startCell.x -> 3 // LEFT
            path.endCell.y > path.startCell.y -> 2 // DOWN
            path.endCell.y < path.startCell.y -> 1 // UP
            else -> 0 // No movement (shouldn't happen)
        }
    }
}