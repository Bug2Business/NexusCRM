package com.android.nexuscrm.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.android.nexuscrm.model.Lead

object FirebaseFirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    fun addLead(
        lead: Lead,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("leads")
            .add(lead)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun getLeads(
        onResult: (List<Lead>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("leads")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull {
                    it.toObject(Lead::class.java)
                }
                onResult(list)
            }
            .addOnFailureListener {
                onError(it.message ?: "Error")
            }
    }
}