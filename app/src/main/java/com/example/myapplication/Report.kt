import com.google.firebase.firestore.GeoPoint

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: GeoPoint? = null, // Use GeoPoint here
    val imageUrl: String = ""
)
