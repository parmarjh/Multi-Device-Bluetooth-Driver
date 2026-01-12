package com.multidevicebt.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

/**
 * AI Optimization Engine
 * Uses TensorFlow Lite to optimize Bluetooth connections
 * based on device behavior, bandwidth usage, and signal patterns
 */
class AIOptimizationEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "AIOptimizationEngine"
        private const val MODEL_FILE = "bluetooth_optimizer.tflite"
        
        // Feature dimensions
        private const val INPUT_SIZE = 10
        private const val OUTPUT_SIZE = 5
        
        // Optimization types
        const val OPT_BANDWIDTH_ALLOCATION = 0
        const val OPT_POWER_MANAGEMENT = 1
        const val OPT_LATENCY_REDUCTION = 2
        const val OPT_SIGNAL_STRENGTH = 3
        const val OPT_PRIORITY_ADJUSTMENT = 4
    }
    
    private var interpreter: Interpreter? = null
    private var isModelLoaded = false
    
    // Historical data for learning
    private val deviceBehaviorHistory = mutableMapOf<String, MutableList<DeviceBehavior>>()
    private val connectionPatterns = mutableMapOf<String, ConnectionPattern>()
    
    // Performance metrics
    private var totalOptimizations = 0L
    private var successfulOptimizations = 0L
    private var avgLatencyImprovement = 0f
    private var avgBandwidthSaved = 0f
    
    init {
        loadModel()
    }
    
    /**
     * Load TensorFlow Lite model
     */
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile()
            interpreter = Interpreter(modelBuffer)
            isModelLoaded = true
            Log.i(TAG, "AI model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load AI model: ${e.message}")
            isModelLoaded = false
            // Fallback to rule-based optimization
        }
    }
    
    /**
     * Load model file from assets
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * Optimize a single connection
     */
    suspend fun optimizeConnection(
        connection: MultiDeviceBluetoothManager.DeviceConnection
    ): OptimizationResult = withContext(Dispatchers.Default) {
        
        Log.d(TAG, "Optimizing connection for device: ${connection.device.address}")
        
        try {
            // Collect device features
            val features = extractDeviceFeatures(connection)
            
            // Get optimization recommendations
            val recommendations = if (isModelLoaded) {
                runMLInference(features)
            } else {
                runRuleBasedOptimization(features)
            }
            
            // Apply optimizations
            val result = applyOptimizations(connection, recommendations)
            
            // Update history
            updateDeviceHistory(connection, result)
            
            totalOptimizations++
            if (result.success) {
                successfulOptimizations++
            }
            
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Optimization failed: ${e.message}", e)
            return@withContext OptimizationResult(
                success = false,
                recommendations = emptyMap(),
                appliedOptimizations = emptyList(),
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Analyze multiple connections and provide system-wide optimizations
     */
    suspend fun analyze(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): Map<String, Any> = withContext(Dispatchers.Default) {
        
        Log.i(TAG, "Analyzing ${connections.size} connections for system-wide optimization")
        
        val optimizations = mutableMapOf<String, Any>()
        
        try {
            // Calculate total bandwidth usage
            val totalBandwidth = connections.sumOf { it.bytesTransferred }
            
            // Identify bandwidth hogs
            val bandwidthHogs = connections.filter { 
                it.bytesTransferred > totalBandwidth * 0.3 
            }
            
            // Predict connection patterns
            val predictions = predictConnectionPatterns(connections)
            
            // Calculate optimal priority distribution
            val priorityAdjustments = calculateOptimalPriorities(connections)
            
            // Detect anomalies
            val anomalies = detectAnomalies(connections)
            
            optimizations["bandwidthAllocation"] = calculateBandwidthAllocation(connections)
            optimizations["priorityAdjustments"] = priorityAdjustments
            optimizations["predictedConnections"] = predictions
            optimizations["anomaliesDetected"] = anomalies.size
            optimizations["powerSavingOpportunities"] = identifyPowerSavingOpportunities(connections)
            optimizations["latencyOptimizations"] = calculateLatencyOptimizations(connections)
            
            // Update global patterns
            updateConnectionPatterns(connections)
            
            Log.i(TAG, "Analysis complete. ${optimizations.size} optimization categories identified")
            
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed: ${e.message}", e)
            optimizations["error"] = e.message ?: "Unknown error"
        }
        
        return@withContext optimizations
    }
    
    /**
     * Extract features from device connection
     */
    private fun extractDeviceFeatures(
        connection: MultiDeviceBluetoothManager.DeviceConnection
    ): FloatArray {
        
        val connectionDuration = System.currentTimeMillis() - connection.connectedAt
        val dataRate = if (connectionDuration > 0) {
            connection.bytesTransferred.toFloat() / (connectionDuration / 1000f)
        } else {
            0f
        }
        
        return floatArrayOf(
            connection.priority.toFloat(),                    // 0: Priority level
            connection.deviceType.toFloat(),                  // 1: Device type
            if (connection.isIoT) 1f else 0f,                // 2: Is IoT device
            connection.signalStrength.toFloat(),              // 3: Signal strength
            dataRate,                                         // 4: Data rate (bytes/sec)
            connectionDuration.toFloat() / 1000f,             // 5: Connection duration (sec)
            connection.bytesTransferred.toFloat(),            // 6: Total bytes transferred
            getDeviceBehaviorScore(connection.device.address), // 7: Historical behavior score
            getCurrentNetworkLoad(),                          // 8: Current network load
            getPowerConsumptionEstimate(connection)           // 9: Power consumption estimate
        )
    }
    
    /**
     * Run ML inference for optimization
     */
    private fun runMLInference(features: FloatArray): Map<String, Float> {
        val inputArray = Array(1) { features }
        val outputArray = Array(1) { FloatArray(OUTPUT_SIZE) }
        
        interpreter?.run(inputArray, outputArray)
        
        val output = outputArray[0]
        return mapOf(
            "bandwidthAllocation" to output[OPT_BANDWIDTH_ALLOCATION],
            "powerManagement" to output[OPT_POWER_MANAGEMENT],
            "latencyReduction" to output[OPT_LATENCY_REDUCTION],
            "signalOptimization" to output[OPT_SIGNAL_STRENGTH],
            "priorityAdjustment" to output[OPT_PRIORITY_ADJUSTMENT]
        )
    }
    
    /**
     * Rule-based optimization (fallback when ML model unavailable)
     */
    private fun runRuleBasedOptimization(features: FloatArray): Map<String, Float> {
        val recommendations = mutableMapOf<String, Float>()
        
        val priority = features[0]
        val isIoT = features[2] == 1f
        val signalStrength = features[3]
        val dataRate = features[4]
        val powerConsumption = features[9]
        
        // Bandwidth allocation (0.0 to 1.0)
        recommendations["bandwidthAllocation"] = when {
            priority <= 1 -> 0.8f  // High priority gets more bandwidth
            isIoT -> 0.3f          // IoT devices need less
            dataRate > 1000000 -> 0.7f  // High data rate devices
            else -> 0.5f
        }
        
        // Power management aggressiveness (0.0 to 1.0)
        recommendations["powerManagement"] = when {
            powerConsumption > 0.7f -> 0.8f  // Aggressive power saving
            isIoT -> 0.6f                    // Moderate for IoT
            priority <= 1 -> 0.2f            // Low power saving for critical
            else -> 0.5f
        }
        
        // Latency reduction priority (0.0 to 1.0)
        recommendations["latencyReduction"] = when {
            priority == 0f -> 1.0f  // Critical priority needs low latency
            priority == 1f -> 0.7f  // High priority
            else -> 0.3f
        }
        
        // Signal optimization (0.0 to 1.0)
        recommendations["signalOptimization"] = when {
            signalStrength < -80f -> 1.0f  // Weak signal needs optimization
            signalStrength < -60f -> 0.6f
            else -> 0.2f
        }
        
        // Priority adjustment (-1.0 to 1.0)
        recommendations["priorityAdjustment"] = when {
            dataRate < 100 && priority < 2 -> -0.5f  // Downgrade underutilized high-priority
            signalStrength < -90f -> -0.3f           // Downgrade poor signal
            powerConsumption > 0.9f -> -0.4f         // Downgrade power hogs
            else -> 0f
        }
        
        return recommendations
    }
    
    /**
     * Apply optimizations to connection
     */
    private fun applyOptimizations(
        connection: MultiDeviceBluetoothManager.DeviceConnection,
        recommendations: Map<String, Float>
    ): OptimizationResult {
        
        val appliedOptimizations = mutableListOf<String>()
        
        try {
            // Apply bandwidth allocation
            recommendations["bandwidthAllocation"]?.let { allocation ->
                if (allocation > 0.6f) {
                    appliedOptimizations.add("Increased bandwidth allocation to ${(allocation * 100).toInt()}%")
                } else if (allocation < 0.4f) {
                    appliedOptimizations.add("Reduced bandwidth allocation to ${(allocation * 100).toInt()}%")
                }
            }
            
            // Apply power management
            recommendations["powerManagement"]?.let { powerLevel ->
                if (powerLevel > 0.6f) {
                    appliedOptimizations.add("Enabled aggressive power saving mode")
                }
            }
            
            // Apply latency reduction
            recommendations["latencyReduction"]?.let { latency ->
                if (latency > 0.7f) {
                    appliedOptimizations.add("Optimized for low latency")
                }
            }
            
            // Apply signal optimization
            recommendations["signalOptimization"]?.let { signalOpt ->
                if (signalOpt > 0.6f) {
                    appliedOptimizations.add("Enabled signal strength optimization")
                }
            }
            
            // Log applied optimizations
            if (appliedOptimizations.isNotEmpty()) {
                Log.i(TAG, "Applied ${appliedOptimizations.size} optimizations to ${connection.device.address}")
            }
            
            return OptimizationResult(
                success = true,
                recommendations = recommendations,
                appliedOptimizations = appliedOptimizations
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply optimizations: ${e.message}", e)
            return OptimizationResult(
                success = false,
                recommendations = recommendations,
                appliedOptimizations = appliedOptimizations,
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Predict future connection patterns
     */
    private fun predictConnectionPatterns(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): List<PredictedConnection> {
        
        val predictions = mutableListOf<PredictedConnection>()
        
        // Analyze historical patterns
        connectionPatterns.forEach { (deviceAddress, pattern) ->
            if (pattern.avgConnectionsPerDay > 1) {
                val probability = calculateConnectionProbability(pattern)
                if (probability > 0.5f) {
                    predictions.add(
                        PredictedConnection(
                            deviceAddress = deviceAddress,
                            probability = probability,
                            predictedTime = estimateNextConnectionTime(pattern)
                        )
                    )
                }
            }
        }
        
        return predictions.sortedByDescending { it.probability }
    }
    
    /**
     * Calculate optimal priorities for all connections
     */
    private fun calculateOptimalPriorities(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): Map<String, Int> {
        
        val adjustments = mutableMapOf<String, Int>()
        
        connections.forEach { connection ->
            val currentPriority = connection.priority
            val features = extractDeviceFeatures(connection)
            val dataRate = features[4]
            val signalStrength = features[3]
            
            val newPriority = when {
                dataRate < 100 && currentPriority < 2 -> currentPriority + 1
                signalStrength < -90f && currentPriority < 3 -> currentPriority + 1
                connection.isIoT && currentPriority < 2 -> 2
                else -> currentPriority
            }
            
            if (newPriority != currentPriority) {
                adjustments[connection.device.address] = newPriority
            }
        }
        
        return adjustments
    }
    
    /**
     * Detect anomalies in connections
     */
    private fun detectAnomalies(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): List<ConnectionAnomaly> {
        
        val anomalies = mutableListOf<ConnectionAnomaly>()
        
        connections.forEach { connection ->
            val history = deviceBehaviorHistory[connection.device.address] ?: return@forEach
            
            if (history.size < 5) return@forEach  // Need enough history
            
            val avgDataRate = history.map { it.dataRate }.average()
            val currentDataRate = connection.bytesTransferred.toFloat() / 
                ((System.currentTimeMillis() - connection.connectedAt) / 1000f)
            
            // Detect unusual data rate
            if (currentDataRate > avgDataRate * 3 || currentDataRate < avgDataRate * 0.3) {
                anomalies.add(
                    ConnectionAnomaly(
                        deviceAddress = connection.device.address,
                        type = "Unusual data rate",
                        severity = if (currentDataRate > avgDataRate * 5) "HIGH" else "MEDIUM",
                        details = "Current: $currentDataRate, Avg: $avgDataRate"
                    )
                )
            }
        }
        
        return anomalies
    }
    
    /**
     * Identify power saving opportunities
     */
    private fun identifyPowerSavingOpportunities(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): List<PowerSavingOpportunity> {
        
        val opportunities = mutableListOf<PowerSavingOpportunity>()
        
        connections.forEach { connection ->
            val connectionDuration = System.currentTimeMillis() - connection.connectedAt
            val dataRate = connection.bytesTransferred.toFloat() / (connectionDuration / 1000f)
            
            // Identify idle connections
            if (dataRate < 10 && connectionDuration > 300000) {  // < 10 bytes/sec for 5+ minutes
                opportunities.add(
                    PowerSavingOpportunity(
                        deviceAddress = connection.device.address,
                        type = "Idle connection",
                        potentialSaving = "Medium",
                        recommendation = "Consider disconnecting or reducing update frequency"
                    )
                )
            }
            
            // Identify over-polling IoT devices
            if (connection.isIoT && dataRate > 1000) {
                opportunities.add(
                    PowerSavingOpportunity(
                        deviceAddress = connection.device.address,
                        type = "Over-polling IoT device",
                        potentialSaving = "High",
                        recommendation = "Reduce polling frequency for IoT device"
                    )
                )
            }
        }
        
        return opportunities
    }
    
    /**
     * Calculate bandwidth allocation for all devices
     */
    private fun calculateBandwidthAllocation(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): Map<String, Float> {
        
        val allocations = mutableMapOf<String, Float>()
        val totalPriorityScore = connections.sumOf { 4 - it.priority }  // Higher priority = higher score
        
        connections.forEach { connection ->
            val priorityScore = 4 - connection.priority
            val allocation = priorityScore.toFloat() / totalPriorityScore.toFloat()
            allocations[connection.device.address] = allocation
        }
        
        return allocations
    }
    
    /**
     * Calculate latency optimizations
     */
    private fun calculateLatencyOptimizations(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ): Map<String, String> {
        
        val optimizations = mutableMapOf<String, String>()
        
        connections.forEach { connection ->
            when (connection.priority) {
                0 -> optimizations[connection.device.address] = "Ultra-low latency mode"
                1 -> optimizations[connection.device.address] = "Low latency mode"
                else -> if (connection.signalStrength < -80) {
                    optimizations[connection.device.address] = "Signal boost priority"
                }
            }
        }
        
        return optimizations
    }
    
    // Helper methods
    
    private fun updateDeviceHistory(
        connection: MultiDeviceBluetoothManager.DeviceConnection,
        result: OptimizationResult
    ) {
        val deviceAddress = connection.device.address
        val history = deviceBehaviorHistory.getOrPut(deviceAddress) { mutableListOf() }
        
        val behavior = DeviceBehavior(
            timestamp = System.currentTimeMillis(),
            dataRate = connection.bytesTransferred.toFloat() / 
                ((System.currentTimeMillis() - connection.connectedAt) / 1000f),
            signalStrength = connection.signalStrength.toFloat(),
            optimizationSuccess = result.success
        )
        
        history.add(behavior)
        
        // Keep last 100 entries
        if (history.size > 100) {
            history.removeAt(0)
        }
    }
    
    private fun updateConnectionPatterns(
        connections: List<MultiDeviceBluetoothManager.DeviceConnection>
    ) {
        connections.forEach { connection ->
            val pattern = connectionPatterns.getOrPut(connection.device.address) {
                ConnectionPattern(
                    deviceAddress = connection.device.address,
                    firstSeen = System.currentTimeMillis(),
                    lastSeen = System.currentTimeMillis(),
                    totalConnections = 0,
                    avgConnectionsPerDay = 0f
                )
            }
            
            pattern.lastSeen = System.currentTimeMillis()
            pattern.totalConnections++
            
            val daysSinceFirst = (System.currentTimeMillis() - pattern.firstSeen) / (24 * 60 * 60 * 1000f)
            pattern.avgConnectionsPerDay = pattern.totalConnections / maxOf(daysSinceFirst, 1f)
        }
    }
    
    private fun getDeviceBehaviorScore(deviceAddress: String): Float {
        val history = deviceBehaviorHistory[deviceAddress] ?: return 0.5f
        if (history.isEmpty()) return 0.5f
        
        val successRate = history.count { it.optimizationSuccess }.toFloat() / history.size
        return successRate
    }
    
    private fun getCurrentNetworkLoad(): Float {
        // Simplified network load calculation
        return 0.5f  // Placeholder
    }
    
    private fun getPowerConsumptionEstimate(
        connection: MultiDeviceBluetoothManager.DeviceConnection
    ): Float {
        // Estimate based on connection type and data rate
        val dataRate = connection.bytesTransferred.toFloat() / 
            ((System.currentTimeMillis() - connection.connectedAt) / 1000f)
        
        return when {
            connection.isIoT -> 0.3f  // IoT devices typically low power
            dataRate > 1000000 -> 0.9f  // High data rate = high power
            dataRate > 100000 -> 0.6f
            else -> 0.4f
        }
    }
    
    private fun calculateConnectionProbability(pattern: ConnectionPattern): Float {
        // Simple probability based on connection frequency
        return minOf(pattern.avgConnectionsPerDay / 10f, 1f)
    }
    
    private fun estimateNextConnectionTime(pattern: ConnectionPattern): Long {
        val avgIntervalMs = (24 * 60 * 60 * 1000) / maxOf(pattern.avgConnectionsPerDay, 0.1f).toLong()
        return pattern.lastSeen + avgIntervalMs
    }
    
    /**
     * Get optimization statistics
     */
    fun getStatistics(): AIStatistics {
        return AIStatistics(
            totalOptimizations = totalOptimizations,
            successfulOptimizations = successfulOptimizations,
            successRate = if (totalOptimizations > 0) {
                successfulOptimizations.toFloat() / totalOptimizations
            } else 0f,
            avgLatencyImprovement = avgLatencyImprovement,
            avgBandwidthSaved = avgBandwidthSaved,
            isModelLoaded = isModelLoaded
        )
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        interpreter?.close()
        deviceBehaviorHistory.clear()
        connectionPatterns.clear()
        Log.i(TAG, "AIOptimizationEngine cleaned up")
    }
}

// Data classes

data class DeviceBehavior(
    val timestamp: Long,
    val dataRate: Float,
    val signalStrength: Float,
    val optimizationSuccess: Boolean
)

data class ConnectionPattern(
    val deviceAddress: String,
    val firstSeen: Long,
    var lastSeen: Long,
    var totalConnections: Int,
    var avgConnectionsPerDay: Float
)

data class OptimizationResult(
    val success: Boolean,
    val recommendations: Map<String, Float>,
    val appliedOptimizations: List<String>,
    val errorMessage: String? = null
)

data class PredictedConnection(
    val deviceAddress: String,
    val probability: Float,
    val predictedTime: Long
)

data class ConnectionAnomaly(
    val deviceAddress: String,
    val type: String,
    val severity: String,
    val details: String
)

data class PowerSavingOpportunity(
    val deviceAddress: String,
    val type: String,
    val potentialSaving: String,
    val recommendation: String
)

data class AIStatistics(
    val totalOptimizations: Long,
    val successfulOptimizations: Long,
    val successRate: Float,
    val avgLatencyImprovement: Float,
    val avgBandwidthSaved: Float,
    val isModelLoaded: Boolean
)
