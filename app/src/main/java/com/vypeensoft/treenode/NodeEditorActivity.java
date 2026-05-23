package com.vypeensoft.treenode;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.vypeensoft.treenode.databinding.ActivityNodeEditorBinding;

import java.util.List;

public class NodeEditorActivity extends AppCompatActivity {

    private ActivityNodeEditorBinding binding;
    private JsonStorageManager storageManager;
    private String documentId;
    private String nodeId;
    
    private TreeDocument document;
    private TreeNode targetNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNodeEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageManager = new JsonStorageManager(this);

        // Retrieve Intent Extras
        documentId = getIntent().getStringExtra("document_id");
        nodeId = getIntent().getStringExtra("node_id");

        // Load document
        document = storageManager.loadTreeDocument(documentId);
        if (document == null) {
            finish();
            return;
        }

        // Find target node recursively
        targetNode = findNodeById(document.getRootNodes(), nodeId);
        if (targetNode == null) {
            finish();
            return;
        }

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Note");
        }

        // Populate Views
        binding.editNodeTitle.setText(targetNode.getTitle());
        binding.editNodeContent.setText(targetNode.getContent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNodeChanges();
    }

    private void saveNodeChanges() {
        if (document != null && targetNode != null) {
            String newTitle = binding.editNodeTitle.getText().toString().trim();
            String newContent = binding.editNodeContent.getText().toString();

            // Sane fallback for empty title
            if (newTitle.isEmpty()) {
                newTitle = "Untitled Node";
            }

            targetNode.setTitle(newTitle);
            targetNode.setContent(newContent);

            storageManager.saveTreeDocument(document);
        }
    }

    private TreeNode findNodeById(List<TreeNode> list, String id) {
        if (list == null) return null;
        for (TreeNode node : list) {
            if (node.getId().equals(id)) {
                return node;
            }
            TreeNode found = findNodeById(node.getChildren(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Save and return
            saveNodeChanges();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

