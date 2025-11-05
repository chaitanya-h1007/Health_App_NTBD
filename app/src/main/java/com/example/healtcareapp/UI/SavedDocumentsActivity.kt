package com.example.healtcareapp.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healtcareapp.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class DocumentModel(
    val name: String = "",
    val url: String = "",
    val timestamp: Long = 0
)

class SavedDocumentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DocumentsAdapter
    private val documentsList = mutableListOf<DocumentModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_documents)

        recyclerView = findViewById(R.id.recyclerViewDocuments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DocumentsAdapter(documentsList)
        recyclerView.adapter = adapter

        fetchDocuments()
    }

    private fun fetchDocuments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("documents")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                documentsList.clear()
                for (doc in result) {
                    val document = DocumentModel(
                        name = doc.getString("name") ?: "",
                        url = doc.getString("url") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                    documentsList.add(document)
                }
                adapter.notifyDataSetChanged()
            }
    }
}

class DocumentsAdapter(private val items: List<DocumentModel>) :
    RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_document, parent, false)) {
        val tvName: TextView = itemView.findViewById(R.id.tvDocName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDocDate)
        val btnView: Button = itemView.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DocumentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = items[position]
        holder.tvName.text = document.name

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(document.timestamp))

        holder.btnView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(document.url), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}