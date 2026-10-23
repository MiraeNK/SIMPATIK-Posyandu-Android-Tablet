package com.example.simpatikposyandu

import android.content.Context
import android.util.Base64
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Nearby transport for small, authenticated Posyandu sync snapshots. */
class NearbyClusterManager(
    context: Context,
    private val deviceId: String,
    private val onStatus: (String) -> Unit,
    private val onSnapshot: (String) -> Unit,
) {
    private val client: ConnectionsClient = Nearby.getConnectionsClient(context.applicationContext)
    private val serviceId = context.packageName
    private val pending = ConcurrentHashMap.newKeySet<String>()
    private val verified = ConcurrentHashMap.newKeySet<String>()
    @Volatile private var groupId: String? = null
    @Volatile private var groupKey: ByteArray? = null
    @Volatile private var endpointName: String = "Tablet SIMPATIK"

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) return
            val raw = payload.asBytes()?.toString(StandardCharsets.UTF_8) ?: return
            try {
                val envelope = JSONObject(raw)
                val signature = envelope.optString("signature")
                val expected = sign(canonical(envelope))
                if (signature.isEmpty() || !MessageDigest.isEqual(
                        signature.toByteArray(StandardCharsets.UTF_8),
                        expected.toByteArray(StandardCharsets.UTF_8),
                    )
                ) {
                    disconnect(endpointId, "Perangkat ditolak: verifikasi grup gagal")
                    return
                }
                if (envelope.optString("groupId") != groupId) {
                    disconnect(endpointId, "Perangkat berada di Posyandu/RW lain")
                    return
                }
                val sentAt = envelope.optLong("sentAt", 0L)
                if (sentAt <= 0L || kotlin.math.abs(System.currentTimeMillis() - sentAt) > MAX_MESSAGE_AGE_MS) {
                    disconnect(endpointId, "Pesan perangkat kedaluwarsa")
                    return
                }
                when (envelope.optString("kind")) {
                    "hello" -> {
                        if (envelope.optJSONObject("data")?.optString("deviceId").isNullOrBlank()) {
                            disconnect(endpointId, "Identitas tablet tidak valid")
                            return
                        }
                        if (verified.add(endpointId)) onStatus("Terhubung ke ${verified.size} tablet dalam grup ini")
                    }
                    "snapshot" -> if (endpointId in verified) {
                        val data = envelope.optJSONObject("data") ?: return
                        onSnapshot(data.toString())
                    }
                }
            } catch (_: Exception) {
                disconnect(endpointId, "Pesan perangkat tidak valid")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Both devices prove possession of the server-issued RW key before
            // any operational snapshot is accepted or sent.
            client.acceptConnection(endpointId, payloadCallback)
                .addOnFailureListener { disconnect(endpointId, "Gagal menerima koneksi perangkat") }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            pending.remove(endpointId)
            if (resolution.status.isSuccess) {
                sendHello(endpointId)
            } else {
                client.disconnectFromEndpoint(endpointId)
                onStatus("Koneksi perangkat gagal")
            }
        }

        override fun onDisconnected(endpointId: String) {
            pending.remove(endpointId)
            verified.remove(endpointId)
            onStatus(if (verified.isEmpty()) "Mencari tablet dalam grup Posyandu…" else "Terhubung ke ${verified.size} tablet dalam grup ini")
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            if (pending.size + verified.size >= MAX_PEERS || endpointId in pending || endpointId in verified) return
            pending.add(endpointId)
            client.requestConnection(endpointName, endpointId, lifecycleCallback)
                .addOnFailureListener {
                    pending.remove(endpointId)
                    onStatus("Gagal menghubungkan tablet sekitar")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            if (endpointId in pending && endpointId !in verified) pending.remove(endpointId)
        }
    }

    fun start(id: String, secretHex: String, name: String): Boolean {
        val key = secretHex.hexToBytes() ?: return false
        if (!id.matches(Regex("[A-Za-z0-9:_-]{1,96}")) || key.size < 32) return false
        stop()
        groupId = id
        groupKey = key
        endpointName = name.take(48).ifBlank { "Tablet SIMPATIK" }
        val advertising = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        val discovery = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        client.startAdvertising(endpointName, serviceId, lifecycleCallback, advertising)
            .addOnFailureListener { onStatus("Izin atau radio Nearby belum siap untuk mengiklankan tablet") }
        client.startDiscovery(serviceId, discoveryCallback, discovery)
            .addOnSuccessListener { onStatus("Mencari tablet dalam grup Posyandu…") }
            .addOnFailureListener { onStatus("Nearby tidak dapat mencari tablet saat ini") }
        return true
    }

    fun broadcastSnapshot(json: String): Int {
        val data = try { JSONObject(json) } catch (_: Exception) { return 0 }
        val message = signedEnvelope("snapshot", data).toByteArray(StandardCharsets.UTF_8)
        if (message.size > MAX_PAYLOAD_BYTES) {
            onStatus("Snapshot lokal terlalu besar untuk dikirim sekaligus")
            return 0
        }
        val recipients = verified.toList()
        recipients.forEach { endpoint ->
            client.sendPayload(endpoint, Payload.fromBytes(message))
                .addOnFailureListener { verified.remove(endpoint) }
        }
        return recipients.size
    }

    fun stop() {
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
        pending.clear()
        verified.clear()
        groupId = null
        groupKey = null
        onStatus("Komunikasi antar-tablet berhenti")
    }

    fun connectedCount(): Int = verified.size

    private fun sendHello(endpointId: String) {
        val bytes = signedEnvelope("hello", JSONObject().put("deviceId", deviceId))
            .toByteArray(StandardCharsets.UTF_8)
        client.sendPayload(endpointId, Payload.fromBytes(bytes))
            .addOnFailureListener { disconnect(endpointId, "Verifikasi grup tidak terkirim") }
    }

    private fun signedEnvelope(kind: String, data: JSONObject): String {
        val envelope = JSONObject()
            .put("version", 1)
            .put("groupId", groupId)
            .put("senderId", deviceId)
            .put("kind", kind)
            .put("sentAt", System.currentTimeMillis())
            .put("data", data)
        return envelope.put("signature", sign(canonical(envelope))).toString()
    }

    private fun canonical(envelope: JSONObject): String = listOf(
        envelope.optInt("version").toString(),
        envelope.optString("groupId"),
        envelope.optString("senderId"),
        envelope.optString("kind"),
        envelope.optLong("sentAt").toString(),
        envelope.optJSONObject("data")?.let(::canonicalValue) ?: "{}",
    ).joinToString("\n")

    private fun canonicalValue(value: Any): String = when (value) {
        is JSONObject -> value.keys().asSequence().toList().sorted().joinToString(",", "{", "}") { key ->
            "${JSONObject.quote(key)}:${canonicalValue(value.get(key))}"
        }
        is org.json.JSONArray -> (0 until value.length()).joinToString(",", "[", "]") { index ->
            canonicalValue(value.get(index))
        }
        JSONObject.NULL -> "null"
        is String -> JSONObject.quote(value)
        is Number, is Boolean -> value.toString()
        else -> JSONObject.quote(value.toString())
    }

    private fun sign(message: String): String {
        val key = groupKey ?: throw IllegalStateException("Grup belum dipilih")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun disconnect(endpointId: String, reason: String) {
        verified.remove(endpointId)
        pending.remove(endpointId)
        client.disconnectFromEndpoint(endpointId)
        onStatus(reason)
    }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0 || !matches(Regex("[A-Fa-f0-9]+"))) return null
        return try { chunked(2).map { it.toInt(16).toByte() }.toByteArray() } catch (_: Exception) { null }
    }

    companion object {
        private const val MAX_PEERS = 8
        private const val MAX_PAYLOAD_BYTES = 900_000
        private const val MAX_MESSAGE_AGE_MS = 24 * 60 * 60 * 1000L
    }
}
