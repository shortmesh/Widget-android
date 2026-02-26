package io.shortmesh.sdk


import android.content.Context
import android.content.Intent

object ShortMesh {

    fun verify(
        context: Context,
        phoneNumber: String,
        apiEndpoint: String
    ) {
        val intent = Intent(context, ShortMeshActivity::class.java)
        intent.putExtra("phoneNumber", phoneNumber)
        intent.putExtra("apiEndpoint", apiEndpoint)
        context.startActivity(intent)
    }
}
