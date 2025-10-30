package com.example.sae41;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PuzzleActivity extends Activity {
    private Puzzle currentPuzzle;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        InputStream input = loadAssetFile("puzzles/exemple.xml");
        if (input == null) {
            Toast.makeText(this, "Erreur de chargement du puzzle", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentPuzzle = PuzzleParser.parsePuzzle(input, "exemple.xml");
        super.onCreate(savedInstanceState);
        // On utilise le layout défini dans activity_puzzle.xml
        setContentView(R.layout.activity_puzzle);

        // Récupérer le nom du fichier puzzle transmis via l'intent
        Intent intent = getIntent();
        String puzzleFile = intent.getStringExtra("puzzleFile");
        if (puzzleFile == null) {
            Toast.makeText(this, "Aucun puzzle sélectionné", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentPuzzle = PuzzleParser.parsePuzzle(loadAssetFile("puzzles/" + puzzleFile), puzzleFile);
        if (!currentPuzzle.isValid()) {
            Toast.makeText(this, "Puzzle invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Récupérer la vue DrawView depuis le layout XML et assigner le puzzle
        drawView = findViewById(R.id.drawView);
        drawView.setPuzzle(currentPuzzle);

        if (savedInstanceState != null) {
            ArrayList<int[]> savedPaths = (ArrayList<int[]>) savedInstanceState.getSerializable("paths");
            drawView.restorePaths(savedPaths);
        }
    }

    private InputStream loadAssetFile(String path) {
        AssetManager assetManager = getAssets();
        try {
            return assetManager.open(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        // Retour au menu principal
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<int[]> serializablePaths = drawView.savePaths();
        outState.putSerializable("paths", serializablePaths);
    }
}
