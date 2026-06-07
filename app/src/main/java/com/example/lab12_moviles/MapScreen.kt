package com.example.lab12_moviles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Polygon
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.Polyline
import androidx.compose.foundation.layout.PaddingValues

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.ui.platform.LocalContext

import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Dot

import androidx.compose.material3.TextField
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.location.Geocoder
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color as ComposeColor

fun bitmapDescriptorFromImage(context: Context, resId: Int, width: Int, height: Int): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    
    val ArequipaLocation = LatLng(-16.4040102, -71.559611)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ArequipaLocation, 12f)
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.2520984, -71.6836503), 12f),
            durationMs = 3000
        )
    }

    val locations = listOf(
        LatLng(-16.433415,-71.5442652),
        LatLng(-16.4205151,-71.4945209),
        LatLng(-16.3524187,-71.5675994)
    )

    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145), LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310), LatLng(-16.432566, -71.508853)
    )
    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830), LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110), LatLng(-16.423050, -71.530600)
    )
    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961), LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289), LatLng(-16.399299, -71.536721)
    )

    val route1 = listOf(LatLng(-16.398866, -71.536961), LatLng(-16.432292, -71.509145))
    val pattern = listOf(Dot(), Gap(20f), Dash(30f), Gap(20f))

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = mapType, isMyLocationEnabled = true),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true),
            contentPadding = PaddingValues(top = 550.dp)
        ) {
            Marker(
                state = rememberMarkerState(position = ArequipaLocation),
                icon = bitmapDescriptorFromImage(context, R.drawable.arequipa_escudo, 150, 150),
                title = "Arequipa, Perú"
            )

            locations.forEach { location ->
                Marker(state = rememberMarkerState(position = location), title = "Ubicación")
            }

            Polygon(points = plazaDeArmasPolygon, strokeColor = Color.Red, fillColor = Color.Blue.copy(alpha = 0.5f))
            Polygon(points = parqueLambramaniPolygon, strokeColor = Color.Red, fillColor = Color.Blue.copy(alpha = 0.5f))
            Polygon(points = mallAventuraPolygon, strokeColor = Color.Red, fillColor = Color.Blue.copy(alpha = 0.5f))

            Polyline(points = route1, color = Color.Magenta, width = 10f)
            Polyline(points = locations, color = Color.Blue, width = 8f, geodesic = true)
            Polyline(
                points = listOf(ArequipaLocation, LatLng(-16.3524187, -71.5675994)),
                color = Color.DarkGray,
                width = 5f,
                pattern = pattern
            )
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(androidx.compose.ui.Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar lugar...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ComposeColor.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = ComposeColor.White.copy(alpha = 0.8f)
                )
            )
            Button(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        scope.launch {
                            try {
                                val geocoder = Geocoder(context)
                                val addresses = geocoder.getFromLocationName(searchQuery, 1)
                                if (addresses != null && addresses.isNotEmpty()) {
                                    val address = addresses[0]
                                    val newLocation = LatLng(address.latitude, address.longitude)
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(newLocation, 15f),
                                        durationMs = 2000
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp).align(androidx.compose.ui.Alignment.End)
            ) {
                Text("Buscar")
            }
        }

        Column(
            modifier = Modifier
                .padding(bottom = 32.dp, start = 16.dp)
                .align(androidx.compose.ui.Alignment.BottomStart)
        ) {
            Row {
                Button(onClick = { mapType = MapType.NORMAL }, modifier = Modifier.padding(2.dp)) { Text("Normal") }
                Button(onClick = { mapType = MapType.SATELLITE }, modifier = Modifier.padding(2.dp)) { Text("Satélite") }
            }
            Row {
                Button(onClick = { mapType = MapType.HYBRID }, modifier = Modifier.padding(2.dp)) { Text("Híbrido") }
                Button(onClick = { mapType = MapType.TERRAIN }, modifier = Modifier.padding(2.dp)) { Text("Terreno") }
            }
        }
    }
}
