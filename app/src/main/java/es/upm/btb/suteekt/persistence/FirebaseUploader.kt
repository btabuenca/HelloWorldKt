package es.upm.btb.suteekt.persistence

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseUploader {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    fun uploadFile(fileUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val ref = storageReference.child("uploads/${fileUri.lastPathSegment}")
        val uploadTask = ref.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }
}
