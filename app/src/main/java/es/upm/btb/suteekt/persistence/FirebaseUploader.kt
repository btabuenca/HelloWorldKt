package es.upm.btb.suteekt.persistence

import android.net.Uri
import android.content.Context
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseUploader(private val context: Context) {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    fun uploadFile(fileUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = storageReference.child("uploads/${fileUri.lastPathSegment}")
        val uploadTask = ref.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            onSuccess()
            Toast.makeText(context, "Archivo subido con éxito!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            onFailure(exception)
            Toast.makeText(context, "Error al subir archivo: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}
